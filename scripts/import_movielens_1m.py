"""
将 MovieLens-1M 数据集导入到本项目的 videohub 数据库中，用作推荐系统的真实行为数据。

映射规则：
- users.dat  -> users 表（账号 ml_user_{UserID}）
- movies.dat -> videos 表（video_id = 'ml_movie_{MovieID}'）
- ratings.dat -> play_history / video_likes / favorites：
  - 所有评分 -> play_history（根据评分映射观看进度）
  - rating >= 4 -> video_likes
  - rating == 5 -> favorites（收藏到默认收藏夹）

使用前准备：
1. 从官方地址下载 MovieLens-1M（ml-1m.zip）并解压到 scripts/ml-1m 目录，
   例如：scripts/ml-1m/ratings.dat, movies.dat, users.dat
2. 根据本地 MySQL 环境修改 DB_CONFIG。
3. 运行：python import_movielens_1m.py
"""

import datetime
import os
import random
from typing import Dict, List, Tuple

import pymysql


DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",          # TODO: 修改为你的 MySQL 用户名
    "password": "123456",    # TODO: 修改为你的 MySQL 密码
    "database": "videohub",
    "charset": "utf8mb4",
}

# MovieLens-1M 解压目录（默认放在 scripts/ml-1m 下，可自行调整）
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MOVIELENS_DIR = os.path.join(BASE_DIR, "ml-1m")

RATINGS_FILE = os.path.join(MOVIELENS_DIR, "ratings.dat")
MOVIES_FILE = os.path.join(MOVIELENS_DIR, "movies.dat")
USERS_FILE = os.path.join(MOVIELENS_DIR, "users.dat")

# 视频时长范围（秒）
MIN_DURATION = 60
MAX_DURATION = 1200

# 批量插入大小
BATCH_SIZE = 5000


def get_conn():
    return pymysql.connect(**DB_CONFIG)


def ensure_files_exist() -> bool:
    ok = True
    for path in (RATINGS_FILE, MOVIES_FILE, USERS_FILE):
        if not os.path.exists(path):
            print(f"缺少文件：{path}")
            ok = False
    if not ok:
        print("请确认 MovieLens-1M 已解压到 scripts/ml-1m 目录下。")
    return ok


def load_movies() -> Dict[int, Tuple[str, str]]:
    """
    读取 movies.dat -> {MovieID: (title, genres)}
    """
    movies: Dict[int, Tuple[str, str]] = {}
    with open(MOVIES_FILE, "r", encoding="latin-1") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            parts = line.split("::", 2)
            if len(parts) != 3:
                continue
            try:
                movie_id = int(parts[0])
            except ValueError:
                continue
            title = parts[1]
            genres = parts[2]
            movies[movie_id] = (title, genres)
    print(f"已加载电影数量: {len(movies)}")
    return movies


def load_existing_ml_users(cur) -> Dict[int, int]:
    """
    读取已存在的 ml_user_{UserID} 映射：{ml_user_id -> db_user_id}
    """
    cur.execute("SELECT id, account FROM users WHERE account LIKE 'ml_user_%'")
    mapping: Dict[int, int] = {}
    for db_id, account in cur.fetchall():
        try:
            ml_uid = int(account.replace("ml_user_", ""))
        except ValueError:
            continue
        mapping[ml_uid] = db_id
    return mapping


def load_existing_ml_videos(cur) -> set:
    """
    读取已存在的 ml_movie_{MovieID} 视频 ID 集合。
    """
    cur.execute("SELECT video_id FROM videos WHERE video_id LIKE 'ml_movie_%'")
    return {row[0] for row in cur.fetchall()}


def get_hashed_password(cur) -> str:
    """
    取一个已有用户的密码哈希（与 test/admin 相同），用于 ml 用户复用。
    """
    cur.execute("SELECT password FROM users ORDER BY id LIMIT 1")
    row = cur.fetchone()
    if not row:
        raise RuntimeError("users 表为空，无法获取密码哈希。")
    return row[0]


def ensure_ml_user(cur, ml_uid: int, pwd_hash: str, cache: Dict[int, int]) -> int:
    """
    确保 ml_user_{ml_uid} 存在，返回其数据库 user_id。
    """
    if ml_uid in cache:
        return cache[ml_uid]

    account = f"ml_user_{ml_uid}"
    username = account
    email = f"{account}@example.com"

    cur.execute("SELECT id FROM users WHERE account = %s", (account,))
    row = cur.fetchone()
    if row:
        db_id = row[0]
    else:
        cur.execute(
            """
            INSERT INTO users (username, account, password, email, status)
            VALUES (%s, %s, %s, %s, 1)
            """,
            (username, account, pwd_hash, email),
        )
        db_id = cur.lastrowid

    cache[ml_uid] = db_id
    return db_id


def ensure_ml_movie(
    cur,
    movie_id: int,
    movies_info: Dict[int, Tuple[str, str]],
    existing_video_ids: set,
) -> str:
    """
    确保 ml_movie_{movie_id} 存在于 videos 表，返回 video_id 字符串。
    """
    video_id = f"ml_movie_{movie_id}"
    if video_id in existing_video_ids:
        return video_id

    title, genres = movies_info.get(movie_id, (f"Movie {movie_id}", ""))
    duration = random.randint(MIN_DURATION, MAX_DURATION)

    # storage_path 是必填字段，给一个虚拟路径（MovieLens 数据没有真实视频文件）
    storage_path = f"Movies/ml-1m/{video_id}.mp4"
    cur.execute(
        """
        INSERT INTO videos
          (video_id, title, description, duration, storage_path, status, view_count, like_count, favorite_count)
        VALUES (%s, %s, %s, %s, %s, 'DONE', 0, 0, 0)
        """,
        (video_id, title, genres, duration, storage_path),
    )
    existing_video_ids.add(video_id)
    return video_id


def ensure_default_folder(cur, cache: Dict[int, int], user_id: int) -> int:
    """
    确保每个用户有一个“默认收藏夹”，并缓存其 ID。
    """
    if user_id in cache:
        return cache[user_id]

    cur.execute(
        "SELECT id FROM favorite_folders WHERE user_id = %s AND name = '默认收藏夹'",
        (user_id,),
    )
    row = cur.fetchone()
    if row:
        folder_id = row[0]
    else:
        cur.execute(
            """
            INSERT INTO favorite_folders (user_id, name, is_public, description)
            VALUES (%s, '默认收藏夹', 1, NULL)
            """,
            (user_id,),
        )
        folder_id = cur.lastrowid
    cache[user_id] = folder_id
    return folder_id


def map_rating_to_progress(rating: float) -> float:
    """
    根据评分映射到观看进度比例（0~1）。
    """
    if rating >= 5:
        return random.uniform(0.9, 1.0)
    if rating >= 4:
        return random.uniform(0.6, 0.9)
    if rating >= 3:
        return random.uniform(0.3, 0.6)
    return random.uniform(0.05, 0.3)


def import_ratings():
    conn = get_conn()
    try:
        with conn.cursor() as cur:
            pwd_hash = get_hashed_password(cur)
            movies_info = load_movies()
            ml_user_map = load_existing_ml_users(cur)
            existing_video_ids = load_existing_ml_videos(cur)
            folder_cache: Dict[int, int] = {}

            play_rows: List[Tuple] = []
            like_rows: List[Tuple[int, str]] = []
            fav_rows: List[Tuple[int, str, int]] = []

            count = 0
            with open(RATINGS_FILE, "r", encoding="latin-1") as f:
                for line in f:
                    line = line.strip()
                    if not line:
                        continue
                    parts = line.split("::")
                    if len(parts) != 4:
                        continue
                    try:
                        ml_uid = int(parts[0])
                        movie_id = int(parts[1])
                        rating = float(parts[2])
                        ts = int(parts[3])
                    except ValueError:
                        continue

                    user_id = ensure_ml_user(cur, ml_uid, pwd_hash, ml_user_map)
                    video_id = ensure_ml_movie(cur, movie_id, movies_info, existing_video_ids)

                    # 映射到播放历史
                    duration = random.randint(MIN_DURATION, MAX_DURATION)
                    progress = map_rating_to_progress(rating)
                    play_time = int(duration * progress)
                    is_watched = 1 if progress >= 0.9 else 0
                    progress_percent = float(progress * 100.0)
                    dt = datetime.datetime.fromtimestamp(ts)

                    play_rows.append(
                        (
                            user_id,
                            video_id,
                            play_time,
                            duration,
                            is_watched,
                            progress_percent,
                            dt,
                            dt,
                        )
                    )

                    # 点赞：评分 >= 4
                    if rating >= 4.0:
                        like_rows.append((user_id, video_id))

                    # 收藏：评分 == 5
                    if rating >= 5.0:
                        folder_id = ensure_default_folder(cur, folder_cache, user_id)
                        fav_rows.append((user_id, video_id, folder_id))

                    count += 1

                    # 批量落库
                    if count % BATCH_SIZE == 0:
                        flush_batches(cur, play_rows, like_rows, fav_rows)
                        print(f"已处理评分记录: {count}")

            # 剩余批次
            flush_batches(cur, play_rows, like_rows, fav_rows)
            print(f"导入完成，总评分记录数: {count}")
            conn.commit()
    finally:
        conn.close()


def flush_batches(
    cur,
    play_rows: List[Tuple],
    like_rows: List[Tuple[int, str]],
    fav_rows: List[Tuple[int, str, int]],
) -> None:
    if play_rows:
        cur.executemany(
            """
            INSERT IGNORE INTO play_history
              (user_id, video_id, play_time, duration,
               is_watched, progress_percent,
               create_time, update_time)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """,
            play_rows,
        )
        play_rows.clear()

    if like_rows:
        cur.executemany(
            """
            INSERT IGNORE INTO video_likes (user_id, video_id)
            VALUES (%s, %s)
            """,
            like_rows,
        )
        like_rows.clear()

    if fav_rows:
        cur.executemany(
            """
            INSERT IGNORE INTO favorites (user_id, video_id, folder_id)
            VALUES (%s, %s, %s)
            """,
            fav_rows,
        )
        fav_rows.clear()


def main() -> None:
    if not ensure_files_exist():
        return
    import_ratings()


if __name__ == "__main__":
    main()


