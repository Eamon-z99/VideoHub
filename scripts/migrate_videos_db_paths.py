"""
将 videos 表中历史的旧路径（按 ID 目录/文件名）迁移为当前上传规范路径。

新规范（按当前 submissions 目录）：
- 视频：uploads/submissions/videos/YYYY/MM/{userId}-{uuid32}.{ext}
- 封面：uploads/submissions/covers/YYYY/MM/{userId}-{uuid32}.{ext}

说明：
- 默认 dry-run，只打印不写库。
- 使用 videos.id + video_id + kind 生成稳定 uuid，保证重复执行结果一致。
- 本脚本连接的是 videoshub（不是 videohub）。

运行示例：
    python scripts/migrate_videos_db_paths.py
    python scripts/migrate_videos_db_paths.py --apply
"""

from __future__ import annotations

import argparse
import datetime as dt
import os
import re
import uuid
from dataclasses import dataclass
from typing import Optional

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

VIDEO_EXT_FALLBACK = "mp4"
COVER_EXT_FALLBACK = "jpg"

RE_NEW_VIDEO = re.compile(
    r"^uploads/submissions/videos/\d{4}/\d{2}/\d+-[0-9a-f]{32}\.[A-Za-z0-9]+$",
    re.IGNORECASE,
)
RE_NEW_COVER = re.compile(
    r"^uploads/submissions/covers/\d{4}/\d{2}/\d+-[0-9a-f]{32}\.[A-Za-z0-9]+$",
    re.IGNORECASE,
)
RE_OLD_SOURCE_DIR = re.compile(r"^videos/\d+$", re.IGNORECASE)


@dataclass
class PlanItem:
    row_id: int
    video_id: str
    old_cover_url: Optional[str]
    old_storage_path: str
    old_source_file: Optional[str]
    new_cover_url: Optional[str]
    new_storage_path: str
    new_source_file: str


def normalize_slashes(value: str) -> str:
    return value.replace("\\", "/").strip("/")


def ext_from_path(path_value: Optional[str], fallback: str) -> str:
    if not path_value:
        return fallback
    name = os.path.basename(path_value)
    if "." not in name:
        return fallback
    ext = name.rsplit(".", 1)[-1].lower().strip()
    if not ext:
        return fallback
    if len(ext) > 10:
        return fallback
    return ext


def stable_hex(row_id: int, video_id: str, kind: str) -> str:
    seed = f"videos:{row_id}:{video_id}:{kind}"
    return uuid.uuid5(uuid.NAMESPACE_DNS, seed).hex


def get_year_month(create_time: Optional[dt.datetime]) -> tuple[int, int]:
    now = dt.datetime.now()
    if isinstance(create_time, dt.datetime):
        return create_time.year, create_time.month
    return now.year, now.month


def is_legacy_row(row: dict) -> bool:
    storage_path = normalize_slashes(row.get("storage_path") or "")
    source_file = normalize_slashes(row.get("source_file") or "")
    cover_url = normalize_slashes(row.get("cover_url") or "")

    # 新规则数据直接跳过
    if RE_NEW_VIDEO.match(storage_path) and RE_NEW_VIDEO.match(source_file):
        if not cover_url or RE_NEW_COVER.match(cover_url):
            return False

    # 明确老规则：source_file = Videos/{id}
    if RE_OLD_SOURCE_DIR.match(source_file):
        return True

    # 避免误迁移 submissions/videoDrafts 等已上传业务数据
    if source_file.lower().startswith("uploads/"):
        return False
    if storage_path.lower().startswith("uploads/"):
        return False
    if cover_url.lower().startswith("uploads/"):
        return False

    return False


def build_plan_item(row: dict) -> PlanItem:
    row_id = int(row["id"])
    video_id = str(row["video_id"])
    user_id = int(row["user_id"] or 0)
    year, month = get_year_month(row.get("create_time"))

    storage_path = normalize_slashes(row.get("storage_path") or "")
    source_file = normalize_slashes(row.get("source_file") or "")
    cover_url = normalize_slashes(row.get("cover_url") or "")

    video_ext = ext_from_path(storage_path or source_file, VIDEO_EXT_FALLBACK)
    cover_ext = ext_from_path(cover_url, COVER_EXT_FALLBACK)

    video_uuid = stable_hex(row_id, video_id, "video")
    cover_uuid = stable_hex(row_id, video_id, "cover")

    new_storage_path = (
        f"uploads/submissions/videos/{year}/{month:02d}/{user_id}-{video_uuid}.{video_ext}"
    )
    new_source_file = new_storage_path
    new_cover_url = None
    if cover_url:
        new_cover_url = (
            f"uploads/submissions/covers/{year}/{month:02d}/{user_id}-{cover_uuid}.{cover_ext}"
        )

    return PlanItem(
        row_id=row_id,
        video_id=video_id,
        old_cover_url=row.get("cover_url"),
        old_storage_path=row.get("storage_path"),
        old_source_file=row.get("source_file"),
        new_cover_url=new_cover_url,
        new_storage_path=new_storage_path,
        new_source_file=new_source_file,
    )


def fetch_rows(conn) -> list[dict]:
    sql = """
        SELECT id, video_id, user_id, create_time, cover_url, storage_path, source_file
        FROM videos
        ORDER BY id ASC
    """
    with conn.cursor() as cur:
        cur.execute(sql)
        return cur.fetchall()


def apply_updates(conn, items: list[PlanItem]) -> int:
    sql = """
        UPDATE videos
        SET cover_url = %s,
            storage_path = %s,
            source_file = %s
        WHERE id = %s
    """
    payload = [
        (it.new_cover_url, it.new_storage_path, it.new_source_file, it.row_id)
        for it in items
    ]
    with conn.cursor() as cur:
        cur.executemany(sql, payload)
    return len(payload)


def print_preview(items: list[PlanItem], limit: int = 20) -> None:
    print(f"待迁移记录数: {len(items)}")
    for it in items[:limit]:
        print("-" * 80)
        print(f"id={it.row_id}, video_id={it.video_id}")
        print(f"  cover_url  : {it.old_cover_url} -> {it.new_cover_url}")
        print(f"  storage    : {it.old_storage_path} -> {it.new_storage_path}")
        print(f"  source_file: {it.old_source_file} -> {it.new_source_file}")
    if len(items) > limit:
        print(f"... 其余 {len(items) - limit} 条省略")


def main() -> None:
    parser = argparse.ArgumentParser(description="迁移 videos 表路径字段为 uploads 规范")
    parser.add_argument(
        "--apply",
        action="store_true",
        help="真正写入数据库（默认仅预览）",
    )
    args = parser.parse_args()

    conn = pymysql.connect(**DB_CONFIG)
    try:
        rows = fetch_rows(conn)
        items: list[PlanItem] = []
        for row in rows:
            if is_legacy_row(row):
                items.append(build_plan_item(row))

        print_preview(items)

        if not args.apply:
            print("\n当前为 dry-run，未写库。加 --apply 才会执行 UPDATE。")
            return

        if not items:
            print("\n没有需要迁移的记录。")
            return

        changed = apply_updates(conn, items)
        conn.commit()
        print(f"\n迁移完成，已更新 {changed} 条 videos 记录。")
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
