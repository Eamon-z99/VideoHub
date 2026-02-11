"""
将 videos 表里的 description 字段，批量更新为 title 重复 5 遍的结果。

使用前准备：
1. 确保本地 MySQL 已有 videohub 库，并已导入数据（参考 videohub.sql）。
2. 安装依赖：在 scripts 目录下执行:  pip install -r requirements.txt
3. 如有需要，根据本地环境修改 DB_CONFIG 中的连接信息。
"""

import pymysql


# 根据你的本地 MySQL 环境修改这里（与 assign_video_user.py 保持一致）
DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",          # 如有需要，请改成你自己的 MySQL 用户
    "password": "123456",    # 如有需要，请改成你自己的 MySQL 密码
    "database": "videohub",
    "charset": "utf8mb4",
}


def main() -> None:
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            # 直接使用 MySQL 的 REPEAT 函数：description = title 重复 5 次
            sql = "UPDATE videos SET description = REPEAT(title, 5);"
            affected = cur.execute(sql)
        conn.commit()
        print(f"更新完成，受影响的行数：{affected}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()


