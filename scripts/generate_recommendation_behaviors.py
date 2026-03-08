"""
为推荐系统批量生成用户行为数据（点赞 + 播放历史），喂给协同过滤算法使用。

使用前准备：
1. 确保已将 `videohub.sql` 导入到本地 MySQL，库名为 `videohub`（或在 DB_CONFIG 中修改）。
2. 安装依赖：  pip install -r requirements.txt  （或至少安装 pymysql）
3. 根据本地环境修改下方 DB_CONFIG 中的连接信息。

生成逻辑（与后端推荐实现对齐）：
- 协同过滤使用的数据来自三个表：video_likes、favorites、play_history。
- 这里重点生成 video_likes 和 play_history：
  - 每个用户随机“看过”一批视频 -> 插入 play_history
  - 其中一部分视频被“点赞” -> 插入 video_likes
  - 使用 INSERT IGNORE，避免与现有记录冲突。

脚本是幂等的，多次运行只会继续补充缺失行为，不会破坏已有数据。
"""

import datetime
import random
from typing import List, Tuple

import pymysql


# 根据你的本地 MySQL 环境修改这里（与其他脚本保持风格一致）
DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",          # TODO: 修改为你的 MySQL 用户名
    "password": "123456",    # TODO: 修改为你的 MySQL 密码
    "database": "videohub",  # 导入 videohub.sql 时使用的库名
    "charset": "utf8mb4",
}


# ===== 可调参数：控制“喂数据”的规模与强度 =====
MAX_VIDEOS_PER_USER = 80       # 每个用户最多“看过”的视频数量（包含点赞子集）
MIN_VIDEOS_PER_USER = 30       # 每个用户最少“看过”的视频数量
LIKE_RATIO = 0.4               # 看过的视频里，有多少会被点赞（0~1）
MIN_DURATION = 60              # 随机生成的视频总时长下限（秒）
MAX_DURATION = 1200            # 随机生成的视频总时长上限（秒）


def fetch_all_users(cur) -> List[int]:
    """获取所有正常用户 ID。"""
    cur.execute("SELECT id FROM users WHERE status = 1")
    rows = cur.fetchall()
    return [row[0] for row in rows]


def fetch_all_videos(cur) -> List[str]:
    """获取所有视频的 video_id。"""
    cur.execute("SELECT video_id FROM videos")
    rows = cur.fetchall()
    return [row[0] for row in rows]


def generate_play_rows(user_id: int, video_ids: List[str]) -> List[Tuple]:
    """
    为单个用户生成 play_history 插入数据：
    (user_id, video_id, play_time, duration, is_watched, progress_percent, create_time, update_time)
    """
    now = datetime.datetime.now()
    rows: List[Tuple] = []

    for vid in video_ids:
        duration = random.randint(MIN_DURATION, MAX_DURATION)
        # 观看进度：更偏向于 30% 以上，少量 10%
        progress = random.choices(
            [0.1, 0.3, 0.6, 0.9],
            weights=[1, 2, 3, 2],
        )[0]
        play_time = int(duration * progress)
        is_watched = 1 if progress >= 0.9 else 0

        rows.append(
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

    return rows


def generate_like_rows(user_id: int, video_ids: List[str]) -> List[Tuple[int, str]]:
    """为单个用户生成 video_likes 插入数据：(user_id, video_id)。"""
    return [(user_id, vid) for vid in video_ids]


def main() -> None:
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            user_ids = fetch_all_users(cur)
            if not user_ids:
                print("没有可用用户（status = 1），脚本退出。")
                return

            video_ids = fetch_all_videos(cur)
            if not video_ids:
                print("没有视频记录，脚本退出。")
                return

            print(f"用户数量: {len(user_ids)}, 视频数量: {len(video_ids)}")

            for uid in user_ids:
                # 为每个用户随机挑选一批“看过”的视频
                count = random.randint(MIN_VIDEOS_PER_USER, MAX_VIDEOS_PER_USER)
                count = min(count, len(video_ids))
                watched = random.sample(video_ids, count)

                # 点赞的子集
                like_count = max(1, int(len(watched) * LIKE_RATIO))
                liked = random.sample(watched, like_count)

                # 生成 play_history 行
                play_rows = generate_play_rows(uid, watched)
                if play_rows:
                    sql_play = """
                        INSERT IGNORE INTO play_history
                          (user_id, video_id, play_time, duration,
                           is_watched, progress_percent,
                           create_time, update_time)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                    """
                    cur.executemany(sql_play, play_rows)
                    print(f"[user {uid}] 插入/忽略 play_history 行数: {cur.rowcount}")

                # 生成 video_likes 行
                like_rows = generate_like_rows(uid, liked)
                if like_rows:
                    sql_like = """
                        INSERT IGNORE INTO video_likes (user_id, video_id)
                        VALUES (%s, %s)
                    """
                    cur.executemany(sql_like, like_rows)
                    print(f"[user {uid}] 插入/忽略 video_likes 行数: {cur.rowcount}")

            conn.commit()
            print("批量行为数据生成完成，已提交。")
    finally:
        conn.close()


if __name__ == "__main__":
    main()


