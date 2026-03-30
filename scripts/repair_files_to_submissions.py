"""
修复磁盘文件路径：将已错误移动到 uploads/videos|covers 的文件，
迁移到 uploads/submissions/videos|covers。

仅处理文件系统，不改数据库。数据库请配合运行：
    repair_db_paths_to_submissions.py

运行：
    python scripts/repair_files_to_submissions.py
    python scripts/repair_files_to_submissions.py --apply
"""

from __future__ import annotations

import argparse
import shutil
from pathlib import Path


MEDIA_ROOT = Path(r"E:\Videos")


def to_target_path(src: Path) -> Path | None:
    rel = src.relative_to(MEDIA_ROOT).as_posix()
    if rel.startswith("uploads/videos/"):
        rest = rel[len("uploads/videos/") :]
        return MEDIA_ROOT / "uploads" / "submissions" / "videos" / rest
    if rel.startswith("uploads/covers/"):
        rest = rel[len("uploads/covers/") :]
        return MEDIA_ROOT / "uploads" / "submissions" / "covers" / rest
    return None


def gather_files() -> list[Path]:
    result: list[Path] = []
    roots = [
        MEDIA_ROOT / "uploads" / "videos",
        MEDIA_ROOT / "uploads" / "covers",
    ]
    for root in roots:
        if not root.exists():
            continue
        for p in root.rglob("*"):
            if p.is_file():
                result.append(p)
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description="修复 uploads 文件到 submissions 目录")
    parser.add_argument("--apply", action="store_true", help="真正移动文件（默认 dry-run）")
    args = parser.parse_args()

    files = gather_files()
    print(f"扫描到待判定文件数: {len(files)}")

    moved = 0
    skipped_exists = 0
    skipped_same = 0
    plans = 0

    for src in files:
        dst = to_target_path(src)
        if dst is None:
            continue
        plans += 1
        if src.resolve() == dst.resolve():
            skipped_same += 1
            continue
        if dst.exists():
            skipped_exists += 1
            print(f"[skip-exists] {src} -> {dst}")
            continue

        if not args.apply:
            print(f"[dry-run] {src} -> {dst}")
            continue

        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.move(str(src), str(dst))
        moved += 1
        print(f"[moved] {src} -> {dst}")

    mode = "APPLY" if args.apply else "DRY-RUN"
    print("\n" + "=" * 80)
    print(f"模式: {mode}")
    print(f"迁移计划数: {plans}")
    print(f"实际移动数: {moved}")
    print(f"跳过(目标已存在): {skipped_exists}")
    print(f"跳过(同路径): {skipped_same}")
    if not args.apply:
        print("当前为 dry-run，未实际移动文件。加 --apply 才会执行。")


if __name__ == "__main__":
    main()

