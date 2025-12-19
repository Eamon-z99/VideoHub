"""
为 videos 表批量生成 view_count、like_count、favorite_count。

使用前请确认：
1. 已经将 videohub.sql 导入到本地 MySQL（默认库名 videohub）。
2. videos 表包含字段：video_id、view_count、like_count、favorite_count。
3. 如有需要，修改下方 DB_* 配置。

运行方式（在 D:\\vue_project\\VideoHub 目录下）：
    python scripts/fill_video_counts.py
"""

import random

import pymysql


# ======== 数据库配置，根据你本地环境修改 =========
DB_HOST = "localhost"
DB_PORT = 3306
DB_USER = "root"
DB_PASSWORD = "123456"
DB_NAME = "videohub"  # 对应 videohub.sql 中的库名


# ======== 随机数生成策略，可根据需要调整 =========
VIEW_MIN = 1_000
VIEW_MAX = 100_000


def generate_counts():
    """生成单条视频的播放量、点赞数、收藏数。"""
    view_count = random.randint(VIEW_MIN, VIEW_MAX)

    like_min = max(1, int(view_count * 0.01))
    like_max = max(like_min + 1, int(view_count * 0.10))
    like_count = random.randint(like_min, like_max)

    fav_min = max(1, int(view_count * 0.005))
    fav_max = max(fav_min + 1, int(view_count * 0.05))
    favorite_count = random.randint(fav_min, fav_max)

    return view_count, like_count, favorite_count


def main():
    conn = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )

    try:
        with conn.cursor() as cursor:
            cursor.execute("SELECT video_id FROM videos")
            rows = cursor.fetchall()
            print(f"Found {len(rows)} videos")

            for row in rows:
                video_id = row["video_id"]
                view_count, like_count, favorite_count = generate_counts()

                # 只给当前为 0 或 NULL 的记录填充，如需覆盖所有记录可去掉 WHERE 条件
                sql = """
                    UPDATE videos
                    SET view_count = %s,
                        like_count = %s,
                        favorite_count = %s
                    WHERE video_id = %s
                """
                cursor.execute(
                    sql,
                    (view_count, like_count, favorite_count, video_id),
                )

            conn.commit()
            print("All videos updated successfully.")
    except Exception as e:
        conn.rollback()
        print("Error occurred, transaction rolled back:", e)
    finally:
        conn.close()


if __name__ == "__main__":
    main()


