"""
为 videos 表的每条记录随机写入 tags 字段（JSON 数组字符串）。

规则：
- 从固定的 22 个分区类标签中，为每个视频随机抽取 4～7 个（不重复），数量也随机。
- 与 videoshub.sql 中 tags 字段定义一致：varchar(2000)，存 JSON 数组。

使用前准备：
1. 本地 MySQL 已有 videoshub 库并已导入数据。
2. pip install pymysql（或 pip install -r requirements.txt）
3. 按环境修改 DB_CONFIG。
"""

from __future__ import annotations

import json
import random
import argparse
from typing import List, Tuple

import pymysql

# 与图中及首页导航一致的 22 个标签（顺序仅便于对照，抽取时为随机）
TAG_POOL: List[str] = [
    "番剧",
    "国创",
    "综艺",
    "动画",
    "鬼畜",
    "舞蹈",
    "娱乐",
    "科技",
    "美食",
    "汽车",
    "运动",
    "VLOG",
    "单机游戏",
    "公益",
    "电影",
    "电视剧",
    "纪录片",
    "音乐",
    "知识",
    "资讯",
    "生活",
    "时尚",
]

# 默认连 videoshub（与 videoshub.sql 一致）；若你库名不同请改 database
DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "videoshub",
    "charset": "utf8mb4",
}


def tags_to_json(tags: List[str]) -> str:
    s = json.dumps(tags, ensure_ascii=False, separators=(",", ":"))
    if len(s) > 2000:
        raise ValueError(f"tags JSON 超过 2000 字符: len={len(s)}")
    return s


def pick_tags_for_video(rng: random.Random) -> List[str]:
    k = rng.randint(4, 7)
    return rng.sample(TAG_POOL, k)


def main() -> None:
    parser = argparse.ArgumentParser(description="为 videos 随机写入 tags（4～7 个）")
    parser.add_argument(
        "--seed",
        type=int,
        default=None,
        help="随机种子；指定后多次运行结果可复现",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="只打印将要更新的条数，不写库",
    )
    args = parser.parse_args()

    rng = random.Random(args.seed)

    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT id FROM videos ORDER BY id")
            rows = cur.fetchall()
            if not rows:
                print("videos 表无数据，退出。")
                return

            updates: List[Tuple[str, int]] = []
            for (pk,) in rows:
                chosen = pick_tags_for_video(rng)
                updates.append((tags_to_json(chosen), pk))

            print(f"共 {len(updates)} 条视频将更新 tags。")
            if args.dry_run:
                for i, (js, pk) in enumerate(updates[:3]):
                    print(f"  示例 id={pk}: {js}")
                if len(updates) > 3:
                    print("  ...")
                print("dry-run：未提交数据库。")
                return

            cur.executemany("UPDATE videos SET tags = %s WHERE id = %s", updates)
        conn.commit()
        print(f"已提交，更新行数：{len(updates)}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
