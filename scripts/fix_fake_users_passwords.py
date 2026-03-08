"""
修正前面生成的 300 个模拟用户：
- 用户 ID 范围：5 ~ 304（共 300 个）
- 登录账号改为：user1 ~ user300
- 用户名也同步改为：user1 ~ user300
- 密码统一改为：123456 的加密结果（与现有 test/testuser/admin 相同，不用明文）

说明：
- 不再新增用户，只更新已存在的 300 条记录。
- 依赖 pymysql，与其他脚本保持一致。
"""

import pymysql


DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",          # TODO: 修改为你的 MySQL 用户名
    "password": "123456",    # TODO: 修改为你的 MySQL 密码
    "database": "videohub",
    "charset": "utf8mb4",
}


def main() -> None:
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            # 1. 读取一个已存在的正常用户的加密密码（假定是 123456 的 bcrypt）
            cur.execute("SELECT password FROM users WHERE id = 1")
            row = cur.fetchone()
            if not row:
                raise RuntimeError("找不到 id=1 的用户，无法获取密码哈希。")
            hashed_password = row[0]
            print(f"将使用与 id=1 相同的密码哈希更新 5~304 号用户。")

            # 2. 更新 id 在 [5,304] 范围内的用户
            updates = []
            for user_id in range(5, 305):  # 5..304
                idx = user_id - 4  # 5->1, 304->300
                account = f"user{idx}"
                username = account
                updates.append((username, account, hashed_password, user_id))

            sql = """
                UPDATE users
                SET username = %s,
                    account = %s,
                    password = %s
                WHERE id = %s
            """
            cur.executemany(sql, updates)
            conn.commit()
            print(f"已更新 {cur.rowcount} 条用户记录（id 5~304）。")
    finally:
        conn.close()


if __name__ == "__main__":
    main()


