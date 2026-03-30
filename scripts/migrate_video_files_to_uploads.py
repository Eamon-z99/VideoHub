"""
将 E:\\Videos 下旧规则文件迁移到 uploads 合规路径。

与 migrate_videos_db_paths.py 使用同一套稳定命名算法，确保路径可对齐：
- 视频：uploads/submissions/videos/YYYY/MM/{userId}-{uuid32}.{ext}
- 封面：uploads/submissions/covers/YYYY/MM/{userId}-{uuid32}.{ext}

说明：
- 默认 dry-run，只打印不移动文件。
- 只处理旧记录（典型 source_file=Videos/数字目录）。
- 数据库使用 videoshub。

运行示例：
    python scripts/migrate_video_files_to_uploads.py
    python scripts/migrate_video_files_to_uploads.py --apply
"""

from __future__ import annotations

import argparse
import datetime as dt
import os
import re
import shutil
import uuid
from dataclasses import dataclass
from pathlib import Path
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

MEDIA_ROOT = Path(r"E:\Videos")

VIDEO_EXTS = {".mp4", ".mkv", ".mov"}
IMAGE_EXTS = {".jpg", ".jpeg", ".png"}

RE_OLD_SOURCE_DIR = re.compile(r"^videos/\d+$", re.IGNORECASE)
RE_NEW_VIDEO = re.compile(
    r"^uploads/submissions/videos/\d{4}/\d{2}/\d+-[0-9a-f]{32}\.[A-Za-z0-9]+$",
    re.IGNORECASE,
)


@dataclass
class MoveItem:
    row_id: int
    video_id: str
    old_video_abs: Optional[Path]
    new_video_abs: Path
    old_cover_abs: Optional[Path]
    new_cover_abs: Optional[Path]


def normalize_slashes(value: str) -> str:
    return value.replace("\\", "/").strip("/")


def stable_hex(row_id: int, video_id: str, kind: str) -> str:
    seed = f"videos:{row_id}:{video_id}:{kind}"
    return uuid.uuid5(uuid.NAMESPACE_DNS, seed).hex


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


def get_year_month(create_time: Optional[dt.datetime]) -> tuple[int, int]:
    now = dt.datetime.now()
    if isinstance(create_time, dt.datetime):
        return create_time.year, create_time.month
    return now.year, now.month


def is_legacy_row(row: dict) -> bool:
    source_file = normalize_slashes(row.get("source_file") or "")
    storage_path = normalize_slashes(row.get("storage_path") or "")
    # 已是标准 uploads/submissions/videos 直接跳过
    if RE_NEW_VIDEO.match(storage_path):
        return False
    if source_file.lower().startswith("uploads/"):
        return False

    # 仅处理“旧 id 目录”数据，避免误处理 submissions/videoDrafts 等新规则记录
    if RE_OLD_SOURCE_DIR.match(source_file):
        return True
    return False


def resolve_existing_file(candidates: list[Path]) -> Optional[Path]:
    for p in candidates:
        if p and p.exists() and p.is_file():
            return p
    return None


def pick_first_by_ext(folder: Path, exts: set[str]) -> Optional[Path]:
    if not folder.exists() or not folder.is_dir():
        return None
    for p in folder.iterdir():
        if p.is_file() and p.suffix.lower() in exts:
            return p
    return None


def build_move_item(row: dict) -> MoveItem:
    row_id = int(row["id"])
    video_id = str(row["video_id"])
    user_id = int(row["user_id"] or 0)
    year, month = get_year_month(row.get("create_time"))

    source_file = normalize_slashes(row.get("source_file") or "")
    storage_path = normalize_slashes(row.get("storage_path") or "")
    cover_url = normalize_slashes(row.get("cover_url") or "")

    source_dir = MEDIA_ROOT / source_file if source_file else MEDIA_ROOT
    source_dir_alt = source_dir
    if source_file.lower().startswith("videos/"):
        # 兼容两种历史目录：
        # 1) E:\Videos\Videos\123
        # 2) E:\Videos\123
        stripped = source_file.split("/", 1)[1] if "/" in source_file else source_file
        source_dir_alt = MEDIA_ROOT / stripped

    video_ext = ext_from_path(storage_path or source_file, "mp4")
    cover_ext = ext_from_path(cover_url, "jpg")
    video_uuid = stable_hex(row_id, video_id, "video")
    cover_uuid = stable_hex(row_id, video_id, "cover")

    new_video_rel = (
        f"uploads/submissions/videos/{year}/{month:02d}/{user_id}-{video_uuid}.{video_ext}"
    )
    new_cover_rel = (
        f"uploads/submissions/covers/{year}/{month:02d}/{user_id}-{cover_uuid}.{cover_ext}"
        if cover_url
        else None
    )

    # 旧视频位置候选
    video_candidates = [
        MEDIA_ROOT / storage_path,  # storage_path 已含路径的情况
        source_dir / storage_path,  # source_file=目录 + storage_path=文件名
        source_dir_alt / storage_path,
        MEDIA_ROOT / source_file,   # source_file 本身是文件
    ]
    old_video_abs = resolve_existing_file(video_candidates)
    if old_video_abs is None and source_dir.exists():
        old_video_abs = pick_first_by_ext(source_dir, VIDEO_EXTS)
    if old_video_abs is None and source_dir_alt.exists():
        old_video_abs = pick_first_by_ext(source_dir_alt, VIDEO_EXTS)

    old_cover_abs = None
    new_cover_abs = None
    if cover_url:
        cover_candidates = [
            MEDIA_ROOT / cover_url,
            source_dir / cover_url,
            source_dir_alt / cover_url,
        ]
        old_cover_abs = resolve_existing_file(cover_candidates)
        if old_cover_abs is None and source_dir.exists():
            old_cover_abs = pick_first_by_ext(source_dir, IMAGE_EXTS)
        if old_cover_abs is None and source_dir_alt.exists():
            old_cover_abs = pick_first_by_ext(source_dir_alt, IMAGE_EXTS)
        new_cover_abs = MEDIA_ROOT / new_cover_rel

    return MoveItem(
        row_id=row_id,
        video_id=video_id,
        old_video_abs=old_video_abs,
        new_video_abs=MEDIA_ROOT / new_video_rel,
        old_cover_abs=old_cover_abs,
        new_cover_abs=new_cover_abs,
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


def safe_move(src: Path, dst: Path, apply: bool) -> str:
    if src.resolve() == dst.resolve():
        return "same-path-skip"
    if dst.exists():
        # 已存在目标文件，跳过避免覆盖
        return "target-exists-skip"
    if not apply:
        return "dry-run"
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.move(str(src), str(dst))
    return "moved"


def main() -> None:
    parser = argparse.ArgumentParser(description="迁移旧视频文件到 uploads 合规路径")
    parser.add_argument(
        "--apply",
        action="store_true",
        help="真正执行文件移动（默认仅预览）",
    )
    args = parser.parse_args()

    conn = pymysql.connect(**DB_CONFIG)
    try:
        rows = fetch_rows(conn)
    finally:
        conn.close()

    plans: list[MoveItem] = []
    for row in rows:
        if is_legacy_row(row):
            plans.append(build_move_item(row))

    print(f"待处理记录: {len(plans)}")
    moved_video = 0
    moved_cover = 0
    missing_video = 0
    missing_cover = 0

    for item in plans:
        print("-" * 80)
        print(f"id={item.row_id}, video_id={item.video_id}")

        if item.old_video_abs is None:
            missing_video += 1
            print("  [video] missing")
        else:
            v_status = safe_move(item.old_video_abs, item.new_video_abs, args.apply)
            if v_status == "moved":
                moved_video += 1
            print(f"  [video] {item.old_video_abs} -> {item.new_video_abs} [{v_status}]")

        if item.new_cover_abs is not None:
            if item.old_cover_abs is None:
                missing_cover += 1
                print("  [cover] missing")
            else:
                c_status = safe_move(item.old_cover_abs, item.new_cover_abs, args.apply)
                if c_status == "moved":
                    moved_cover += 1
                print(
                    f"  [cover] {item.old_cover_abs} -> {item.new_cover_abs} [{c_status}]"
                )

    mode = "APPLY" if args.apply else "DRY-RUN"
    print("\n" + "=" * 80)
    print(f"模式: {mode}")
    print(f"视频移动成功: {moved_video}")
    print(f"封面移动成功: {moved_cover}")
    print(f"缺失视频文件: {missing_video}")
    print(f"缺失封面文件: {missing_cover}")
    if not args.apply:
        print("当前为 dry-run，未实际移动文件。加 --apply 才会执行。")


if __name__ == "__main__":
    main()
