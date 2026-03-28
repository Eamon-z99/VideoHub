"""
为 videoshub 数据库初始化用户等级/经验（Python 版）

功能：
1) 可选：创建 user_exp_daily_grants 表（用于每日经验发放限额/去重）
2) 为所有用户随机生成 exp（0~28800），并按阈值计算 level

依赖：
  pip install -r scripts/requirements.txt
"""

import random
from typing import Tuple

import pymysql


DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",          # TODO: 改成你的 MySQL 用户名
    "password": "123456",    # TODO: 改成你的 MySQL 密码
    "database": "videoshub",
    "charset": "utf8mb4",
    "autocommit": False,
}


# 经验阈值（累计经验 >= 阈值 即为该等级）
LEVEL_THRESHOLDS = [
    (6, 28800),
    (5, 10800),
    (4, 4500),
    (3, 1500),
    (2, 200),
    (1, 50),
    (0, 0),
]

MAX_EXP = 28800


def compute_level(exp: int) -> int:
    exp = max(0, min(int(exp), MAX_EXP))
    for level, threshold in LEVEL_THRESHOLDS:
        if exp >= threshold:
            return level
    return 0


def ensure_columns(cur) -> None:
    # users.level
    cur.execute(
        """
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'users'
          AND COLUMN_NAME = 'level'
        """
    )
    has_level = cur.fetchone()[0] > 0
    if not has_level:
        cur.execute(
            """
            ALTER TABLE `users`
              ADD COLUMN `level` TINYINT UNSIGNED NOT NULL DEFAULT 0
              COMMENT '用户等级（0-6，预留更高等级）'
              AFTER `coin_balance`
            """
        )

    # users.exp
    cur.execute(
        """
        SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'users'
          AND COLUMN_NAME = 'exp'
        """
    )
    has_exp = cur.fetchone()[0] > 0
    if not has_exp:
        cur.execute(
            """
            ALTER TABLE `users`
              ADD COLUMN `exp` BIGINT UNSIGNED NOT NULL DEFAULT 0
              COMMENT '当前经验值（用于计算等级）'
              AFTER `level`
            """
        )


def ensure_daily_grants_table(cur) -> None:
    cur.execute(
        """
        CREATE TABLE IF NOT EXISTS `user_exp_daily_grants`  (
          `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '发放记录ID',
          `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
          `grant_date` date NOT NULL COMMENT '发放日期（按日）',
          `exp_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '经验类型：LOGIN/WATCH/COIN_GIFT',
          `exp_granted` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '当日累计发放经验值',
          `coin_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '当日累计投币次数（仅 COIN_GIFT 使用）',
          `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
          `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
          PRIMARY KEY (`id`) USING BTREE,
          UNIQUE KEY `uk_user_date_type` (`user_id`, `grant_date`, `exp_type`),
          KEY `idx_user_id` (`user_id`),
          KEY `idx_grant_date` (`grant_date`),
          CONSTRAINT `user_exp_daily_grants_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
        ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户每日经验发放记录'
        """
    )


def fetch_user_ids(cur) -> list[int]:
    cur.execute("SELECT id FROM users")
    return [int(r[0]) for r in cur.fetchall()]


def random_exp_and_level() -> Tuple[int, int]:
    exp = random.randint(0, MAX_EXP)
    level = compute_level(exp)
    return exp, level


def main() -> None:
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            ensure_columns(cur)
            ensure_daily_grants_table(cur)

            user_ids = fetch_user_ids(cur)
            if not user_ids:
                print("users 表为空，脚本结束。")
                conn.commit()
                return

            updates = []
            for uid in user_ids:
                exp, level = random_exp_and_level()
                updates.append((level, exp, uid))

            cur.executemany(
                "UPDATE users SET level = %s, exp = %s WHERE id = %s",
                updates,
            )

            conn.commit()
            print(f"已随机初始化 {len(user_ids)} 个用户的 level/exp。")
    finally:
        conn.close()


if __name__ == "__main__":
    main()

