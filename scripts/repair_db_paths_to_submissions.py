"""
修复数据库路径：将已错误写入的 uploads/videos|covers 路径改为 uploads/submissions/videos|covers。

适用场景：
- 已执行过旧版迁移脚本，导致 videos 表中存在：
  - uploads/videos/YYYY/MM/...
  - uploads/covers/YYYY/MM/...
- 现需统一改为 submissions 目录体系：
  - uploads/submissions/videos/YYYY/MM/...
  - uploads/submissions/covers/YYYY/MM/...

运行：
    python scripts/repair_db_paths_to_submissions.py
    python scripts/repair_db_paths_to_submissions.py --apply
"""

from __future__ import annotations

import argparse

import pymysql


DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "videoshub",
    "charset": "utf8mb4",
    "cursorclass": pymysql.cursors.DictCursor,
}


def to_submissions_path(value: str | None) -> str | None:
    if not value:
        return value
    v = value.replace("\\", "/")
    if v.startswith("uploads/submissions/"):
        return v
    if v.startswith("uploads/videos/"):
        return "uploads/submissions/videos/" + v[len("uploads/videos/") :]
    if v.startswith("uploads/covers/"):
        return "uploads/submissions/covers/" + v[len("uploads/covers/") :]
    return v


def main() -> None:
    parser = argparse.ArgumentParser(description="修复 videos 表路径到 uploads/submissions")
    parser.add_argument("--apply", action="store_true", help="真正写库（默认 dry-run）")
    args = parser.parse_args()

    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT id, video_id, cover_url, storage_path, source_file
                FROM videos
                WHERE storage_path LIKE 'uploads/videos/%'
                   OR source_file LIKE 'uploads/videos/%'
                   OR cover_url LIKE 'uploads/covers/%'
                ORDER BY id ASC
                """
            )
            rows = cur.fetchall()

        updates: list[tuple[str | None, str | None, str | None, int]] = []
        for row in rows:
            new_cover = to_submissions_path(row["cover_url"])
            new_storage = to_submissions_path(row["storage_path"])
            new_source = to_submissions_path(row["source_file"])
            updates.append((new_cover, new_storage, new_source, row["id"]))

        print(f"待修复记录数: {len(updates)}")
        for row, upd in zip(rows[:20], updates[:20]):
            print("-" * 80)
            print(f"id={row['id']}, video_id={row['video_id']}")
            print(f"  cover_url  : {row['cover_url']} -> {upd[0]}")
            print(f"  storage    : {row['storage_path']} -> {upd[1]}")
            print(f"  source_file: {row['source_file']} -> {upd[2]}")
        if len(updates) > 20:
            print(f"... 其余 {len(updates) - 20} 条省略")

        if not args.apply:
            print("\n当前为 dry-run，未写库。加 --apply 才会执行 UPDATE。")
            return

        if not updates:
            print("\n没有需要修复的记录。")
            return

        with conn.cursor() as cur:
            cur.executemany(
                """
                UPDATE videos
                SET cover_url = %s,
                    storage_path = %s,
                    source_file = %s
                WHERE id = %s
                """,
                updates,
            )
        conn.commit()
        print(f"\n修复完成，已更新 {len(updates)} 条 records。")
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()

