"""
批量创建模拟用户，并为这些用户生成行为数据（点赞 / 收藏 / 播放历史），
用于放大推荐系统的训练样本。

使用前准备：
1. 确保已将 `videohub.sql` 导入到本地 MySQL，库名为 `videohub`（或在 DB_CONFIG 中修改）。
2. 安装依赖：  pip install -r requirements.txt  （或至少安装 pymysql）
3. 根据本地环境修改下方 DB_CONFIG 中的连接信息。

设计说明：
- 脚本会创建一批账号名形如 fake_user_001 ~ fake_user_300 的用户；
- 密码统一使用一个固定的（这里直接存明文示例，实际可替换为 bcrypt 后的值）；
- 为每个新用户随机生成若干：
  - 播放历史（play_history）
  - 点赞（video_likes）
  - 收藏（favorites，挂在默认收藏夹下，如无则自动创建）
- 所有写入都使用 INSERT IGNORE 或唯一键控制，脚本可安全多次运行。
"""

import datetime
import random
from typing import List, Tuple

import pymysql


DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",          # TODO: 修改为你的 MySQL 用户名
    "password": "123456",    # TODO: 修改为你的 MySQL 密码
    "database": "videohub",
    "charset": "utf8mb4",
}


# 要创建的模拟用户数量
NUM_FAKE_USERS = 300

# 每个模拟用户的行为规模（区间内随机）
MIN_VIDEOS_PER_USER = 30
MAX_VIDEOS_PER_USER = 120
LIKE_RATIO = 0.4          # 看过的视频中有多少会被点赞
FAVORITE_RATIO = 0.3      # 看过的视频中有多少会被收藏

MIN_DURATION = 60         # 模拟视频总时长（秒）
MAX_DURATION = 1200


def get_conn():
    return pymysql.connect(**DB_CONFIG)


def fetch_all_videos(cur) -> List[str]:
    cur.execute("SELECT video_id FROM videos")
    return [row[0] for row in cur.fetchall()]


def ensure_default_folder(cur, user_id: int) -> int:
    """
    确保用户有一个“默认收藏夹”，返回该收藏夹 ID。
    如果不存在则创建。
    """
    cur.execute(
        "SELECT id FROM favorite_folders WHERE user_id = %s AND name = '默认收藏夹'",
        (user_id,),
    )
    row = cur.fetchone()
    if row:
        return row[0]

    cur.execute(
        """
        INSERT INTO favorite_folders (user_id, name, is_public, description)
        VALUES (%s, '默认收藏夹', 1, NULL)
        """,
        (user_id,),
    )
    return cur.lastrowid


def create_fake_users(cur) -> List[int]:
    """
    创建模拟用户，返回新建用户的 ID 列表。
    如果用户已存在（按 account 唯一约束），则直接复用其 ID。
    """
    user_ids: List[int] = []

    for idx in range(1, NUM_FAKE_USERS + 1):
        account = f"fake_user_{idx:03d}"
        username = f"模拟用户{idx:03d}"
        email = f"{account}@example.com"

        # 先查是否已存在
        cur.execute("SELECT id FROM users WHERE account = %s", (account,))
        row = cur.fetchone()
        if row:
            user_ids.append(row[0])
            continue

        # 简化：密码直接用明文占位（真实环境应存加密后的密码）
        password = "password"
        cur.execute(
            """
            INSERT INTO users (username, account, password, email, status)
            VALUES (%s, %s, %s, %s, 1)
            """,
            (username, account, password, email),
        )
        user_ids.append(cur.lastrowid)

    return user_ids


def generate_behaviors_for_user(
    cur,
    user_id: int,
    all_video_ids: List[str],
) -> None:
    """
    为单个用户生成播放历史 / 点赞 / 收藏行为。
    """
    if not all_video_ids:
        return

    # 随机抽取该用户涉及的视频
    count = random.randint(MIN_VIDEOS_PER_USER, MAX_VIDEOS_PER_USER)
    count = min(count, len(all_video_ids))
    watched = random.sample(all_video_ids, count)

    like_count = max(1, int(len(watched) * LIKE_RATIO))
    fav_count = max(1, int(len(watched) * FAVORITE_RATIO))
    liked = set(random.sample(watched, like_count))
    favorited = set(random.sample(watched, fav_count))

    now = datetime.datetime.now()

    # 1）play_history
    play_rows: List[Tuple] = []
    for vid in watched:
        duration = random.randint(MIN_DURATION, MAX_DURATION)
        progress = random.choices(
            [0.1, 0.3, 0.6, 0.9],
            weights=[1, 2, 3, 2],
        )[0]
        play_time = int(duration * progress)
        is_watched = 1 if progress >= 0.9 else 0

        play_rows.append(
            (
                user_id,
                vid,
                play_time,
                duration,
                is_watched,
                float(progress * 100),
                now,
                now,
            )
        )

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

    # 2）video_likes
    like_rows = [(user_id, vid) for vid in liked]
    if like_rows:
        cur.executemany(
            """
            INSERT IGNORE INTO video_likes (user_id, video_id)
            VALUES (%s, %s)
            """,
            like_rows,
        )

    # 3）favorites：挂到默认收藏夹
    if favorited:
        folder_id = ensure_default_folder(cur, user_id)
        fav_rows = [(user_id, vid, folder_id) for vid in favorited]
        cur.executemany(
            """
            INSERT IGNORE INTO favorites (user_id, video_id, folder_id)
            VALUES (%s, %s, %s)
            """,
            fav_rows,
        )


def main() -> None:
    conn = get_conn()
    try:
        with conn.cursor() as cur:
            # 1. 创建 / 获取模拟用户
            fake_user_ids = create_fake_users(cur)
            print(f"模拟用户数量: {len(fake_user_ids)}")

            # 2. 获取所有视频
            all_videos = fetch_all_videos(cur)
            print(f"可用视频数量: {len(all_videos)}")
            if not all_videos:
                print("没有视频数据，脚本退出。")
                return

            # 3. 为每个模拟用户生成行为
            for uid in fake_user_ids:
                generate_behaviors_for_user(cur, uid, all_videos)
                print(f"user {uid} 行为生成完成")

            conn.commit()
            print("所有模拟用户行为数据生成完成，已提交。")
    finally:
        conn.close()


if __name__ == "__main__":
    main()


