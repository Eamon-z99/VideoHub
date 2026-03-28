"""
为 videos 表的 `partition` 字段写入 JSON 数组（与首页右侧六个快捷入口一致）。

规则：
- 从以下 6 个标签中，为每条视频随机抽取 2～4 个（不重复），个数亦随机。
- 存储格式与 tags 一致：JSON 数组字符串。
- 运行前请将 `partition` 列加宽（默认 varchar(50) 不够）：本脚本可使用 --ensure-column 自动执行 ALTER。

使用前准备：
1. 本地 MySQL 已有 videoshub（或你的库名）并已导入数据。
2. pip install pymysql
3. 按环境修改 DB_CONFIG。
"""

from __future__ import annotations

import argparse
import json
import random
from typing import List, Tuple

import pymysql

# 与首页 utility 区六个入口文案一致
PARTITION_POOL: List[str] = [
    "专栏",
    "活动",
    "社区中心",
    "直播",
    "课堂",
    "新歌热榜",
]

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "videoshub",
    "charset": "utf8mb4",
}

MAX_JSON_LEN = 2000


def partitions_to_json(parts: List[str]) -> str:
    s = json.dumps(parts, ensure_ascii=False, separators=(",", ":"))
    if len(s) > MAX_JSON_LEN:
        raise ValueError(f"partition JSON 超过 {MAX_JSON_LEN} 字符: len={len(s)}")
    return s


def pick_partitions_for_video(rng: random.Random) -> List[str]:
    k = rng.randint(2, 4)
    return rng.sample(PARTITION_POOL, k)


def ensure_partition_column(cur) -> None:
    cur.execute(
        """
        ALTER TABLE videos
        MODIFY COLUMN `partition` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
        NULL DEFAULT NULL COMMENT '分区导航（JSON 数组，对应首页六个快捷入口）'
        """
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="为 videos 随机写入 partition（2～4 个快捷入口标签）")
    parser.add_argument("--seed", type=int, default=None, help="随机种子，便于复现")
    parser.add_argument("--dry-run", action="store_true", help="只预览，不写库")
    parser.add_argument(
        "--ensure-column",
        action="store_true",
        help="执行 ALTER，将 partition 列改为 varchar(2000)（仅需首次执行）",
    )
    args = parser.parse_args()

    rng = random.Random(args.seed)

    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            if args.ensure_column and not args.dry_run:
                ensure_partition_column(cur)
                print("已执行 ALTER TABLE … MODIFY COLUMN `partition` varchar(2000) …")

            cur.execute("SELECT id FROM videos ORDER BY id")
            rows = cur.fetchall()
            if not rows:
                print("videos 表无数据，退出。")
                return

            updates: List[Tuple[str, int]] = []
            for (pk,) in rows:
                chosen = pick_partitions_for_video(rng)
                updates.append((partitions_to_json(chosen), pk))

            print(f"共 {len(updates)} 条视频将更新 `partition`。")
            if args.dry_run:
                for i, (js, pk) in enumerate(updates[:3]):
                    print(f"  示例 id={pk}: {js}")
                if len(updates) > 3:
                    print("  ...")
                print("dry-run：未提交数据库。")
                return

            cur.executemany("UPDATE videos SET `partition` = %s WHERE id = %s", updates)
        conn.commit()
        print(f"已提交，更新行数：{len(updates)}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
