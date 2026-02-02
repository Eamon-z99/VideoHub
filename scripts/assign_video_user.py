"""
为每个没有绑定上传者的 视频 绑定一个 user_id（上传用户）。

使用前准备：
1. 确保已将 `videohub.sql` 导入到本地 MySQL，库名为 `videohub`（或在 DB_CONFIG 中修改）。
2. 安装依赖：  pip install pymysql
3. 根据本地环境修改下方 DB_CONFIG 中的连接信息。
"""

import pymysql


# 根据你的本地 MySQL 环境修改这里
DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",          # TODO: 修改为你的 MySQL 用户名
    "password": "123456", # TODO: 修改为你的 MySQL 密码
    "database": "videohub",
    "charset": "utf8mb4",
}


def main() -> None:
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            # 1. 获取可用用户 ID（status = 1 的正常用户）
            cur.execute("SELECT id FROM users WHERE status = 1")
            user_rows = cur.fetchall()
            if not user_rows:
                print("没有可用用户（status = 1），脚本退出。")
                return

            user_ids = [row[0] for row in user_rows]
            print(f"共找到 {len(user_ids)} 个可用用户：{user_ids}")

            # 2. 找出还没有绑定 user_id 的视频
            # 如需把 user_id = 0 也视为未绑定，可以改为:
            # WHERE user_id IS NULL OR user_id = 0
            cur.execute("SELECT id FROM videos WHERE user_id IS NULL")
            video_rows = cur.fetchall()
            if not video_rows:
                print("所有视频都已经有 user_id，无需处理。")
                return

            print(f"共有 {len(video_rows)} 个视频需要绑定 user_id")

            # 3. 为每个视频分配一个用户
            # 这里使用“按主键取模”的方式稳定分配，而不是完全随机，
            # 这样多次运行脚本时结果一致，便于排查问题。
            updates = []
            user_count = len(user_ids)
            for (video_pk,) in video_rows:
                assigned_user_id = user_ids[video_pk % user_count]
                updates.append((assigned_user_id, video_pk))

            # 4. 批量更新
            cur.executemany(
                "UPDATE videos SET user_id = %s WHERE id = %s",
                updates,
            )
            conn.commit()
            print(f"成功更新 {cur.rowcount} 条视频记录。")
    finally:
        conn.close()


if __name__ == "__main__":
    main()


