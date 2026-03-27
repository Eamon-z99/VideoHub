"""
removeM.py

需求：
在 `videoshub` 数据库中：
1) 删除 `videos.cover_url` 为空（NULL/空字符串/纯空格）的所有视频
2) 同步删除这些视频在关联表中的记录（最重要的 video_likes、favorites；以及 videos(video_id) 的外键引用子表）
3) 带进度条

锁等待处理（关键）：
- 使用 `SELECT ... FOR UPDATE SKIP LOCKED` 来跳过当前被其它事务锁住的视频行，避免一直等待导致 1205
- 删除失败只回滚并继续下一轮，而不是死磕同一批

运行示例：
  python scripts/removeM.py --batch-size 20 --lock-wait-timeout 5 --max-retries 10
"""

from __future__ import annotations

import argparse
import os
import sys
import time
from typing import List, Tuple

import pymysql


# videos(video_id) 外键引用的子表（来自 videoshub.sql）
VIDEO_CHILD_TABLES = [
    "comments",
    "video_coins",
    "video_complaints",
    "video_notes",
    "video_play_events",
    "play_history",
    "video_likes",
    "favorites",
]


LOCK_RELATED_CODES = {1205, 1213}  # lock wait timeout / deadlock


def is_lock_related_error(exc: BaseException) -> bool:
    code = None
    if getattr(exc, "args", None):
        if len(exc.args) >= 1 and isinstance(exc.args[0], int):
            code = exc.args[0]
    if code in LOCK_RELATED_CODES:
        return True
    msg = str(exc).lower()
    return "lock wait timeout exceeded" in msg or "deadlock" in msg


def print_progress(processed_deleted: int, total: int, width: int = 40) -> None:
    if total <= 0:
        return
    ratio = processed_deleted / total
    ratio = max(0.0, min(1.0, ratio))
    filled = int(width * ratio)
    bar = "#" * filled + "-" * (width - filled)
    pct = ratio * 100
    sys.stdout.write(f"\r[{bar}] {processed_deleted}/{total} ({pct:.2f}%)")
    sys.stdout.flush()


def get_conn() -> pymysql.connections.Connection:
    DB_HOST = os.environ.get("VIDEOSHUB_DB_HOST", "localhost")
    DB_PORT = int(os.environ.get("VIDEOSHUB_DB_PORT", "3306"))
    DB_USER = os.environ.get("VIDEOSHUB_DB_USER", "root")
    DB_PASSWORD = os.environ.get("VIDEOSHUB_DB_PASSWORD", "123456")
    DB_NAME = os.environ.get("VIDEOSHUB_DB_NAME", "videoshub")

    return pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.Cursor,
    )


def fetch_total(conn: pymysql.connections.Connection, where_sql: str) -> int:
    with conn.cursor() as cur:
        cur.execute(f"SELECT COUNT(*) FROM videos WHERE {where_sql}")
        row = cur.fetchone()
        return int(row[0]) if row else 0


def fetch_video_ids_plain(
    conn: pymysql.connections.Connection,
    where_sql: str,
    batch_size: int,
) -> List[str]:
    with conn.cursor() as cur:
        cur.execute(
            f"""
            SELECT video_id
            FROM videos
            WHERE {where_sql}
            ORDER BY video_id
            LIMIT %s
            """,
            (batch_size,),
        )
        rows = cur.fetchall()
        return [r[0] for r in rows] if rows else []


def fetch_video_ids_skip_locked(
    conn: pymysql.connections.Connection,
    where_sql: str,
    batch_size: int,
) -> List[str]:
    # 需要开启事务（autocommit=False）以便 FOR UPDATE 生效
    with conn.cursor() as cur:
        cur.execute(
            f"""
            SELECT video_id
            FROM videos
            WHERE {where_sql}
            ORDER BY video_id
            LIMIT %s
            FOR UPDATE SKIP LOCKED
            """,
            (batch_size,),
        )
        rows = cur.fetchall()
        return [r[0] for r in rows] if rows else []


def delete_child_then_parent(
    conn: pymysql.connections.Connection,
    video_ids: List[str],
    lock_wait_timeout: int,
    max_retries: int,
    base_backoff: float,
    skip_locked: bool,
) -> Tuple[int, bool]:
    """
    返回：
    - videos_deleted: 删除 videos 实际成功数（行数）
    - success: 是否成功删除这批
    """
    placeholders = ",".join(["%s"] * len(video_ids))

    for attempt in range(1, max_retries + 1):
        try:
            with conn.cursor() as cur:
                cur.execute("SET SESSION innodb_lock_wait_timeout = %s", (lock_wait_timeout,))

                # 先删外键子表，最后删父表：降低 InnoDB 内部级联导致的等待
                for tbl in VIDEO_CHILD_TABLES:
                    cur.execute(
                        f"DELETE FROM {tbl} WHERE video_id IN ({placeholders})",
                        video_ids,
                    )

                cur.execute(
                    f"DELETE FROM videos WHERE video_id IN ({placeholders})",
                    video_ids,
                )
                videos_deleted = cur.rowcount or 0

            conn.commit()
            return videos_deleted, True
        except (pymysql.err.OperationalError, pymysql.err.InternalError) as e:
            conn.rollback()
            if is_lock_related_error(e) and attempt < max_retries:
                backoff = base_backoff * (2 ** (attempt - 1))
                print(
                    f"\n锁相关错误：{e}\n"
                    f"重试：attempt={attempt}/{max_retries}，sleep={backoff:.1f}s，batch={len(video_ids)}"
                )
                time.sleep(backoff)
                continue

            # 失败就跳过，别卡死
            print(
                f"\n删除失败（跳过该批）：batch={len(video_ids)}, skip_locked={skip_locked}, err={e}"
            )
            return 0, False

    return 0, False


def main() -> None:
    parser = argparse.ArgumentParser(description="Remove empty cover_url videos (videoshub).")
    parser.add_argument("--batch-size", type=int, default=20, help="每批视频数（越小越稳）")
    parser.add_argument("--lock-wait-timeout", type=int, default=5, help="SET SESSION innodb_lock_wait_timeout（秒）")
    parser.add_argument("--max-retries", type=int, default=10, help="单批锁相关错误重试次数")
    parser.add_argument("--base-backoff", type=float, default=0.8, help="指数退避基数（秒）")
    parser.add_argument("--sleep-seconds", type=float, default=0.1, help="每批之间额外休眠（秒）")
    parser.add_argument(
        "--skip-locked",
        action="store_true",
        default=True,
        help="使用 FOR UPDATE SKIP LOCKED 跳过被锁住的视频（需要 MySQL 8+）",
    )
    parser.add_argument(
        "--no-skip-locked",
        dest="skip_locked",
        action="store_false",
        help="不使用 SKIP LOCKED（调试用）",
    )
    parser.add_argument("--dry-run", action="store_true", help="只统计不执行删除")
    args = parser.parse_args()

    where_sql = "cover_url IS NULL OR TRIM(cover_url) = ''"

    conn = get_conn()
    started_at = time.time()
    try:
        conn.autocommit(True)
        total = fetch_total(conn, where_sql)
        if total == 0:
            print("未找到 cover_url 为空的视频，无需清理。")
            return

        if args.dry_run:
            print(f"dry-run：共找到 {total} 个需要删除的视频，但不执行删除。")
            return

        print(f"共找到 {total} 个需要删除的视频。")
        print_progress(0, total)

        deleted_videos_total = 0
        empty_rounds = 0

        while True:
            # 开启事务以让 FOR UPDATE 生效
            conn.autocommit(False)

            try:
                conn.cursor().execute("SET SESSION innodb_lock_wait_timeout = %s", (args.lock_wait_timeout,))
            except Exception:
                # SET 失败不影响继续（某些版本/权限可能禁用）
                pass

            try:
                if args.skip_locked:
                    video_ids = fetch_video_ids_skip_locked(conn, where_sql, args.batch_size)
                else:
                    video_ids = fetch_video_ids_plain(conn, where_sql, args.batch_size)
            except (pymysql.err.OperationalError, pymysql.err.InternalError) as e:
                # 如果版本不支持 SKIP LOCKED，自动回退
                if args.skip_locked and ("SKIP LOCKED" in str(e).upper() or "syntax" in str(e).lower()):
                    print("\nSKIP LOCKED 不可用，自动回退为普通查询。")
                    args.skip_locked = False
                    video_ids = fetch_video_ids_plain(conn, where_sql, args.batch_size)
                else:
                    conn.rollback()
                    raise

            if not video_ids:
                conn.rollback()
                empty_rounds += 1
                # 如果长时间都拿不到未锁住的行，说明系统一直有锁占用
                if empty_rounds >= 50:
                    raise RuntimeError("长时间拿不到可删除的视频（可能锁一直被占用）。请检查数据库其它事务。")
                # 短暂等待后再试
                time.sleep(args.sleep_seconds)
                continue

            empty_rounds = 0

            # 用锁等待/退避处理删除；删除成功后 commit，删除失败会 rollback 并跳过
            videos_deleted, _ = delete_child_then_parent(
                conn=conn,
                video_ids=video_ids,
                lock_wait_timeout=args.lock_wait_timeout,
                max_retries=args.max_retries,
                base_backoff=args.base_backoff,
                skip_locked=args.skip_locked,
            )

            if videos_deleted > 0:
                deleted_videos_total += videos_deleted
                print_progress(deleted_videos_total, total)

            if args.sleep_seconds > 0:
                time.sleep(args.sleep_seconds)

    finally:
        try:
            conn.close()
        except Exception:
            pass

    elapsed = time.time() - started_at
    print("\n清理完成。")
    print(f"已删除 videos：{deleted_videos_total}")
    print(f"耗时：{elapsed:.1f}s")


if __name__ == "__main__":
    main()
    raise SystemExit(0)

"""
removeM.py

功能：
1) 在 `videoshub` 数据库中，删除 videos 表里 `cover_url` 为空（NULL/空字符串/纯空格）的记录
2) 同步删除这些视频在关联表里的记录（至少包括 video_likes、favorites）

为了降低锁等待/超时风险：
- 在删除 videos 之前，先按固定顺序手动删除所有引用 `videos(video_id)` 的外键子表记录
- 每批小事务、失败时对锁相关错误做 rollback + 指数退避重试
"""

import argparse
import os
import sys
import time
from typing import List

import pymysql


VIDEO_CHILD_TABLES = [
    "comments",
    "video_coins",
    "video_complaints",
    "video_notes",
    "video_play_events",
    "play_history",
    "video_likes",
    "favorites",
]


def is_lock_related_error(exc: BaseException) -> bool:
    # MySQL 常见锁相关错误码：
    # - 1205: Lock wait timeout exceeded
    # - 1213: Deadlock found when trying to get lock
    code = None
    if getattr(exc, "args", None):
        if len(exc.args) >= 1 and isinstance(exc.args[0], int):
            code = exc.args[0]
    if code in (1205, 1213):
        return True
    msg = str(exc).lower()
    return "lock wait timeout exceeded" in msg or "deadlock" in msg


def print_progress(processed: int, total: int, width: int = 40) -> None:
    if total <= 0:
        return
    ratio = processed / total
    ratio = max(0.0, min(1.0, ratio))
    filled = int(width * ratio)
    bar = "#" * filled + "-" * (width - filled)
    pct = ratio * 100
    sys.stdout.write(f"\r[{bar}] {processed}/{total} ({pct:.2f}%)")
    sys.stdout.flush()


def get_conn() -> pymysql.connections.Connection:
    DB_HOST = os.environ.get("VIDEOSHUB_DB_HOST", "localhost")
    DB_PORT = int(os.environ.get("VIDEOSHUB_DB_PORT", "3306"))
    DB_USER = os.environ.get("VIDEOSHUB_DB_USER", "root")
    DB_PASSWORD = os.environ.get("VIDEOSHUB_DB_PASSWORD", "123456")
    DB_NAME = os.environ.get("VIDEOSHUB_DB_NAME", "videoshub")

    return pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.Cursor,
    )


def fetch_total(conn: pymysql.connections.Connection, where_sql: str) -> int:
    with conn.cursor() as cur:
        cur.execute(f"SELECT COUNT(*) FROM videos WHERE {where_sql}")
        row = cur.fetchone()
        return int(row[0]) if row else 0


def fetch_video_ids(
    conn: pymysql.connections.Connection,
    where_sql: str,
    batch_size: int,
) -> List[str]:
    with conn.cursor() as cur:
        cur.execute(
            f"""
            SELECT video_id
            FROM videos
            WHERE {where_sql}
            ORDER BY video_id
            LIMIT %s
            """,
            (batch_size,),
        )
        rows = cur.fetchall()
        return [r[0] for r in rows] if rows else []


def delete_child_then_parent(
    conn: pymysql.connections.Connection,
    video_ids: List[str],
    lock_wait_timeout: int,
    max_retries: int,
    base_backoff: float,
) -> None:
    placeholders = ",".join(["%s"] * len(video_ids))

    for attempt in range(1, max_retries + 1):
        try:
            with conn.cursor() as cur:
                cur.execute("SET SESSION innodb_lock_wait_timeout = %s", (lock_wait_timeout,))

                # 先删外键子表，避免 videos 删除触发级联造成的大范围锁/内部级联扫描
                for tbl in VIDEO_CHILD_TABLES:
                    cur.execute(
                        f"DELETE FROM {tbl} WHERE video_id IN ({placeholders})",
                        video_ids,
                    )

                # 最后删父表
                cur.execute(
                    f"DELETE FROM videos WHERE video_id IN ({placeholders})",
                    video_ids,
                )

            conn.commit()
            return
        except (pymysql.err.OperationalError, pymysql.err.InternalError) as e:
            conn.rollback()
            if is_lock_related_error(e) and attempt < max_retries:
                backoff = base_backoff * (2 ** (attempt - 1))
                print(
                    f"\n锁相关错误：{e}\n"
                    f"重试：attempt={attempt}/{max_retries}，sleep={backoff:.1f}s，batch={len(video_ids)}"
                )
                time.sleep(backoff)
                continue
            raise


def main() -> None:
    parser = argparse.ArgumentParser(description="Remove empty cover_url videos (videoshub).")
    parser.add_argument("--batch-size", type=int, default=50, help="每批删除 video_id 数量（越小越不易超时）")
    parser.add_argument("--sleep-seconds", type=float, default=0.1, help="每批之间额外休眠，降低锁冲突")
    parser.add_argument("--max-retries", type=int, default=12, help="锁相关错误最大重试次数")
    parser.add_argument("--base-backoff", type=float, default=0.8, help="指数退避基数（秒）")
    parser.add_argument("--lock-wait-timeout", type=int, default=10, help="SET SESSION innodb_lock_wait_timeout（秒）")
    parser.add_argument("--dry-run", action="store_true", help="只统计不执行删除")
    args = parser.parse_args()

    # 兼容 NULL/''/纯空格
    where_sql = "cover_url IS NULL OR TRIM(cover_url) = ''"

    conn = get_conn()
    try:
        started_at = time.time()
        conn.autocommit(True)
        total = fetch_total(conn, where_sql)
        if total == 0:
            print("未找到 cover_url 为空的视频，无需清理。")
            return

        if args.dry_run:
            print(f"dry-run：共找到 {total} 个需要删除的视频，但不执行删除。")
            return

        print(f"共找到 {total} 个需要删除的视频。")
        print_progress(0, total)

        deleted_count = 0
        while True:
            video_ids = fetch_video_ids(conn, where_sql, args.batch_size)
            if not video_ids:
                break

            conn.autocommit(False)
            delete_child_then_parent(
                conn=conn,
                video_ids=video_ids,
                lock_wait_timeout=args.lock_wait_timeout,
                max_retries=args.max_retries,
                base_backoff=args.base_backoff,
            )

            deleted_count += len(video_ids)
            print_progress(deleted_count, total)

            if args.sleep_seconds > 0:
                time.sleep(args.sleep_seconds)

        sys.stdout.write("\n")
        elapsed = time.time() - started_at
        print("清理完成。")
        print(f"已删除 videos：{deleted_count}")
        print(f"耗时：{elapsed:.1f}s")
    finally:
        try:
            conn.close()
        except Exception:
            pass


if __name__ == "__main__":
    main()

"""
清理 videos 表中 cover_url 为空的视频，并同步删除其在 video_likes、favorites 中的记录。

目标数据库：videoshub（不是 videohub，videohub 是备份）

用法（示例）：
  python scripts/removeM.py --batch-size 100

建议：
  锁等待/超时时，通过减小 batch-size、增加重试次数/退避来缓解。
"""
import argparse
import os
import sys
import time
from typing import List

import pymysql


def is_lock_related_error(exc: BaseException) -> bool:
    """
    MySQL 常见锁相关错误：
    - 1205: Lock wait timeout exceeded
    - 1213: Deadlock found when trying to get lock
    """

    code = None
    if getattr(exc, "args", None):
        if len(exc.args) >= 1 and isinstance(exc.args[0], int):
            code = exc.args[0]
    if code in (1205, 1213):
        return True
    msg = str(exc).lower()
    return "lock wait timeout exceeded" in msg or "deadlock" in msg


def print_progress(processed: int, total: int, width: int = 40) -> None:
    if total <= 0:
        return
    ratio = processed / total
    ratio = max(0.0, min(1.0, ratio))
    filled = int(width * ratio)
    bar = "#" * filled + "-" * (width - filled)
    pct = ratio * 100
    sys.stdout.write(f"\r[{bar}] {processed}/{total} ({pct:.2f}%)")
    sys.stdout.flush()


def fetch_total_videos(conn: pymysql.connections.Connection, where_sql: str) -> int:
    with conn.cursor() as cur:
        cur.execute(f"SELECT COUNT(*) FROM videos WHERE {where_sql}")
        row = cur.fetchone()
        return int(row[0]) if row else 0


def fetch_video_ids(
    conn: pymysql.connections.Connection,
    where_sql: str,
    batch_size: int,
) -> List[str]:
    with conn.cursor() as cur:
        cur.execute(
            f"""
            SELECT video_id
            FROM videos
            WHERE {where_sql}
            ORDER BY video_id
            LIMIT %s
            """,
            (batch_size,),
        )
        rows = cur.fetchall()
        return [r[0] for r in rows] if rows else []


def main() -> None:
    parser = argparse.ArgumentParser(description="Remove videos with empty cover_url (videoshub).")
    parser.add_argument("--batch-size", type=int, default=100, help="每批删除数量（越小越不易触发锁等待）")
    parser.add_argument("--sleep-seconds", type=float, default=0.2, help="每批之间休眠，降低锁冲突概率")
    parser.add_argument("--max-retries", type=int, default=8, help="锁相关错误重试次数")
    parser.add_argument("--base-backoff", type=float, default=1.0, help="指数退避基数（秒）")
    parser.add_argument("--lock-wait-timeout", type=int, default=30, help="SET SESSION innodb_lock_wait_timeout（秒）")
    parser.add_argument("--dry-run", action="store_true", help="只统计不执行删除")
    args = parser.parse_args()

    DB_HOST = os.environ.get("VIDEOSHUB_DB_HOST", "localhost")
    DB_PORT = int(os.environ.get("VIDEOSHUB_DB_PORT", "3306"))
    DB_USER = os.environ.get("VIDEOSHUB_DB_USER", "root")
    DB_PASSWORD = os.environ.get("VIDEOSHUB_DB_PASSWORD", "123456")
    DB_NAME = os.environ.get("VIDEOSHUB_DB_NAME", "videoshub")

    conn = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.Cursor,
    )

    # 同时覆盖：NULL、''、以及纯空格（TRIM 后为空）
    where_sql = "cover_url IS NULL OR TRIM(cover_url) = ''"

    started_at = time.time()
    deleted_videos = 0
    deleted_likes = 0
    deleted_favorites = 0

    try:
        conn.autocommit(True)
        total = fetch_total_videos(conn, where_sql)
        if total == 0:
            print("未找到 cover_url 为空的视频，无需清理。")
            return

        print(f"共找到 {total} 个需要删除的视频。")
        print_progress(0, total)

        if args.dry_run:
            print("\n dry-run 模式：不执行删除。")
            return

        while True:
            video_ids = fetch_video_ids(conn, where_sql, args.batch_size)
            if not video_ids:
                break

            placeholders = ",".join(["%s"] * len(video_ids))

            conn.autocommit(False)
            likes_batch_deleted = 0
            favorites_batch_deleted = 0
            videos_batch_deleted = 0

            for attempt in range(1, args.max_retries + 1):
                try:
                    with conn.cursor() as cur:
                        cur.execute("SET SESSION innodb_lock_wait_timeout = %s", (args.lock_wait_timeout,))

                        cur.execute(f"DELETE FROM video_likes WHERE video_id IN ({placeholders})", video_ids)
                        likes_batch_deleted = cur.rowcount or 0

                        cur.execute(f"DELETE FROM favorites WHERE video_id IN ({placeholders})", video_ids)
                        favorites_batch_deleted = cur.rowcount or 0

                        cur.execute(f"DELETE FROM videos WHERE video_id IN ({placeholders})", video_ids)
                        videos_batch_deleted = cur.rowcount or 0

                    conn.commit()
                    break
                except (pymysql.err.OperationalError, pymysql.err.InternalError) as e:
                    conn.rollback()
                    if is_lock_related_error(e) and attempt < args.max_retries:
                        backoff = args.base_backoff * (2 ** (attempt - 1))
                        print(
                            f"\n锁相关错误：{e}\n"
                            f"准备重试：batch={len(video_ids)} attempt={attempt}/{args.max_retries}，"
                            f"等待 {backoff:.1f}s 后继续。"
                        )
                        time.sleep(backoff)
                        continue
                    raise

            deleted_videos += videos_batch_deleted
            deleted_likes += likes_batch_deleted
            deleted_favorites += favorites_batch_deleted

            print_progress(deleted_videos, total)
            if args.sleep_seconds > 0:
                time.sleep(args.sleep_seconds)

        sys.stdout.write("\n")
        elapsed = time.time() - started_at
        print("清理完成。")
        print(f"删除 videos：{deleted_videos}")
        print(f"删除 video_likes：{deleted_likes}")
        print(f"删除 favorites：{deleted_favorites}")
        print(f"耗时：{elapsed:.1f}s")
    finally:
        try:
            conn.close()
        except Exception:
            pass


if __name__ == "__main__":
    main()
    # 防止文件中意外叠加的重复脚本段落继续执行
    raise SystemExit(0)

"""
清理 videos 表中 cover_url 为空的视频，并同步删除其在 video_likes、favorites 中的记录。

目标数据库：videoshub（不是 videohub，videohub 是备份）

用法（示例）：
  python scripts/removeM.py --batch-size 100

建议：
  锁等待/超时时，通过减小 batch-size、增加重试次数/退避来缓解。
"""
import argparse
import os
import sys
import time
from typing import List

import pymysql


def is_lock_related_error(exc: BaseException) -> bool:
    """
    MySQL 常见锁相关错误：
    - 1205: Lock wait timeout exceeded
    - 1213: Deadlock found when trying to get lock
    """

    code = None
    if getattr(exc, "args", None):
        if len(exc.args) >= 1 and isinstance(exc.args[0], int):
            code = exc.args[0]
    if code in (1205, 1213):
        return True
    msg = str(exc).lower()
    return "lock wait timeout exceeded" in msg or "deadlock" in msg


def print_progress(processed: int, total: int, width: int = 40) -> None:
    if total <= 0:
        return
    ratio = processed / total
    ratio = max(0.0, min(1.0, ratio))
    filled = int(width * ratio)
    bar = "#" * filled + "-" * (width - filled)
    pct = ratio * 100
    sys.stdout.write(f"\r[{bar}] {processed}/{total} ({pct:.2f}%)")
    sys.stdout.flush()


def fetch_total_videos(conn: pymysql.connections.Connection, where_sql: str, params: tuple) -> int:
    with conn.cursor() as cur:
        cur.execute(f"SELECT COUNT(*) FROM videos WHERE {where_sql}", params)
        row = cur.fetchone()
        return int(row[0]) if row else 0


def fetch_video_ids(
    conn: pymysql.connections.Connection,
    where_sql: str,
    params: tuple,
    batch_size: int,
) -> List[str]:
    with conn.cursor() as cur:
        cur.execute(
            f"""
            SELECT video_id
            FROM videos
            WHERE {where_sql}
            ORDER BY video_id
            LIMIT %s
            """,
            params + (batch_size,),
        )
        rows = cur.fetchall()
        return [r[0] for r in rows] if rows else []


def main() -> None:
    parser = argparse.ArgumentParser(description="Remove videos with empty cover_url (videoshub).")
    parser.add_argument("--batch-size", type=int, default=100, help="每批删除数量（越小越不易触发锁等待）")
    parser.add_argument("--sleep-seconds", type=float, default=0.2, help="每批之间休眠，降低锁冲突概率")
    parser.add_argument("--max-retries", type=int, default=8, help="锁相关错误重试次数")
    parser.add_argument("--base-backoff", type=float, default=1.0, help="指数退避基数（秒）")
    parser.add_argument("--lock-wait-timeout", type=int, default=30, help="SET SESSION innodb_lock_wait_timeout（秒）")
    parser.add_argument("--dry-run", action="store_true", help="只统计不执行删除")
    args = parser.parse_args()

    DB_HOST = os.environ.get("VIDEOSHUB_DB_HOST", "localhost")
    DB_PORT = int(os.environ.get("VIDEOSHUB_DB_PORT", "3306"))
    DB_USER = os.environ.get("VIDEOSHUB_DB_USER", "root")
    DB_PASSWORD = os.environ.get("VIDEOSHUB_DB_PASSWORD", "123456")
    DB_NAME = os.environ.get("VIDEOSHUB_DB_NAME", "videoshub")

    # 对于读取/选择：不强制放长事务；真正删除时才开启事务并 commit。
    conn = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.Cursor,
    )

    # 同时覆盖：NULL、''、以及纯空格（TRIM 后为空）
    where_sql = "cover_url IS NULL OR TRIM(cover_url) = ''"
    where_params: tuple = tuple()  # where_sql 当前不需要额外参数

    started_at = time.time()
    deleted_videos = 0
    deleted_likes = 0
    deleted_favorites = 0

    try:
        conn.autocommit(True)
        total = fetch_total_videos(conn, where_sql, where_params)
        if total == 0:
            print("未找到 cover_url 为空的视频，无需清理。")
            return

        print(f"共找到 {total} 个需要删除的视频。")
        print_progress(0, total)

        if args.dry_run:
            print("\n dry-run 模式：不执行删除。")
            return

        # 循环：每次只取一小批待删 video_id，然后在一个事务里删子表再删 videos。
        conn.autocommit(True)
        while True:
            video_ids = fetch_video_ids(conn, where_sql, where_params, args.batch_size)
            if not video_ids:
                break

            placeholders = ",".join(["%s"] * len(video_ids))

            likes_batch_deleted = 0
            favorites_batch_deleted = 0
            videos_batch_deleted = 0

            conn.autocommit(False)
            for attempt in range(1, args.max_retries + 1):
                try:
                    with conn.cursor() as cur:
                        cur.execute("SET SESSION innodb_lock_wait_timeout = %s", (args.lock_wait_timeout,))

                        cur.execute(f"DELETE FROM video_likes WHERE video_id IN ({placeholders})", video_ids)
                        likes_batch_deleted = cur.rowcount or 0

                        cur.execute(f"DELETE FROM favorites WHERE video_id IN ({placeholders})", video_ids)
                        favorites_batch_deleted = cur.rowcount or 0

                        cur.execute(f"DELETE FROM videos WHERE video_id IN ({placeholders})", video_ids)
                        videos_batch_deleted = cur.rowcount or 0

                    conn.commit()
                    break
                except (pymysql.err.OperationalError, pymysql.err.InternalError) as e:
                    conn.rollback()
                    if is_lock_related_error(e) and attempt < args.max_retries:
                        backoff = args.base_backoff * (2 ** (attempt - 1))
                        print(
                            f"\n锁相关错误：{e}\n"
                            f"准备重试：batch={len(video_ids)} attempt={attempt}/{args.max_retries}，"
                            f"等待 {backoff:.1f}s 后继续。"
                        )
                        time.sleep(backoff)
                        continue
                    raise

            deleted_videos += videos_batch_deleted
            deleted_likes += likes_batch_deleted
            deleted_favorites += favorites_batch_deleted

            print_progress(deleted_videos, total)
            if args.sleep_seconds > 0:
                time.sleep(args.sleep_seconds)

        sys.stdout.write("\n")
        elapsed = time.time() - started_at
        print("清理完成。")
        print(f"删除 videos：{deleted_videos}")
        print(f"删除 video_likes：{deleted_likes}")
        print(f"删除 favorites：{deleted_favorites}")
        print(f"耗时：{elapsed:.1f}s")
    finally:
        try:
            conn.close()
        except Exception:
            pass


if __name__ == "__main__":
    main()

"""
清理 videos 表中 cover_url 为空的视频，并同步删除其在 video_likes、favorites 中的记录。

目标数据库：videoshub（不是 videohub，videohub 是备份）

用法（示例）：
  python scripts/removeM.py --batch-size 100

依赖：
  pip install pymysql
"""
import argparse
import math
import os
import sys
import time
from typing import Iterable, List

import pymysql


def chunked(seq: List[str], batch_size: int) -> Iterable[List[str]]:
    for i in range(0, len(seq), batch_size):
        yield seq[i : i + batch_size]


def is_lock_related_error(err: BaseException) -> bool:
    """
    MySQL 常见锁相关错误：
    - 1205: Lock wait timeout exceeded
    - 1213: Deadlock found when trying to get lock; try restarting transaction
    """
    # PyMySQL: err.args 通常形如 (code, msg)
    code = None
    if getattr(err, "args", None):
        if len(err.args) >= 1 and isinstance(err.args[0], int):
            code = err.args[0]
    if code in (1205, 1213):
        return True
    msg = str(err).lower()
    return "lock wait timeout exceeded" in msg or "deadlock" in msg


def print_progress(processed: int, total: int, width: int = 40) -> None:
    if total <= 0:
        return
    ratio = processed / total
    filled = int(width * ratio)
    bar = "#" * filled + "-" * (width - filled)
    pct = ratio * 100
    sys.stdout.write(f"\r[{bar}] {processed}/{total} ({pct:.2f}%)")
    sys.stdout.flush()


def main() -> None:
    parser = argparse.ArgumentParser(description="Remove videos with empty cover_url (videoshub).")
    parser.add_argument("--batch-size", type=int, default=100, help="每批删除的 video_id 数量（建议 50~300）")
    parser.add_argument("--sleep-seconds", type=float, default=0.2, help="批次之间的休眠时间，减少对线上锁的冲击")
    parser.add_argument("--max-retries", type=int, default=8, help="锁相关错误时的最大重试次数")
    parser.add_argument("--base-backoff", type=float, default=1.0, help="重试退避基数（指数退避）")
    parser.add_argument("--lock-wait-timeout", type=int, default=30, help="SET SESSION innodb_lock_wait_timeout")
    parser.add_argument("--dry-run", action="store_true", help="只统计不执行删除")
    args = parser.parse_args()

    # 与项目中其他脚本保持一致的配置方式；建议按你本地环境修改。
    DB_HOST = os.environ.get("VIDEOSHUB_DB_HOST", "localhost")
    DB_PORT = int(os.environ.get("VIDEOSHUB_DB_PORT", "3306"))
    DB_USER = os.environ.get("VIDEOSHUB_DB_USER", "root")
    DB_PASSWORD = os.environ.get("VIDEOSHUB_DB_PASSWORD", "123456")
    DB_NAME = os.environ.get("VIDEOSHUB_DB_NAME", "videoshub")

    conn = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.Cursor,
        autocommit=False,
    )

    started_at = time.time()
    total_video_ids = 0
    deleted_videos = 0
    deleted_likes = 0
    deleted_favorites = 0

    try:
        # 先用 autocommit 模式仅做查询，避免长事务影响其他会话（查询本身不需要加锁）。
        conn.autocommit(True)
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT video_id
                FROM videos
                WHERE cover_url IS NULL OR TRIM(cover_url) = ''
                """
            )
            rows = cur.fetchall()
            video_ids = [r[0] for r in rows] if rows else []

        total_video_ids = len(video_ids)
        if total_video_ids == 0:
            print("未找到 cover_url 为空的视频，无需清理。")
            return

        print(f"共找到 {total_video_ids} 个需要删除的视频。")

        if args.dry_run:
            print("dry-run 模式：不执行删除。")
            return

        conn.autocommit(False)
        total_batches = math.ceil(total_video_ids / args.batch_size)
        print(f"开始分批删除：batch_size={args.batch_size}，batch 总数={total_batches}")

        processed = 0

        for batch_index, batch_ids in enumerate(chunked(video_ids, args.batch_size), start=1):
            placeholders = ",".join(["%s"] * len(batch_ids))
            likes_batch_deleted = 0
            favorites_batch_deleted = 0
            videos_batch_deleted = 0

            for attempt in range(1, args.max_retries + 1):
                try:
                    with conn.cursor() as cur:
                        # 给当前会话设置锁等待超时时间（可配合重试减少“卡住”风险）。
                        cur.execute("SET SESSION innodb_lock_wait_timeout = %s", (args.lock_wait_timeout,))

                        # 删除子表，减少被锁定/级联带来的额外等待时间。
                        cur.execute(f"DELETE FROM video_likes WHERE video_id IN ({placeholders})", batch_ids)
                        likes_batch_deleted = cur.rowcount or 0

                        cur.execute(f"DELETE FROM favorites WHERE video_id IN ({placeholders})", batch_ids)
                        favorites_batch_deleted = cur.rowcount or 0

                        cur.execute(f"DELETE FROM videos WHERE video_id IN ({placeholders})", batch_ids)
                        videos_batch_deleted = cur.rowcount or 0

                    conn.commit()
                    break
                except (pymysql.err.OperationalError, pymysql.err.InternalError) as e:
                    conn.rollback()
                    if is_lock_related_error(e) and attempt < args.max_retries:
                        backoff = args.base_backoff * (2 ** (attempt - 1))
                        # 换行输出，避免进度条覆盖信息
                        print(
                            f"\n[批次 {batch_index}/{total_batches}] 锁相关错误，"
                            f"第 {attempt} 次重试，等待 {backoff:.1f}s 后继续。错误：{e}"
                        )
                        time.sleep(backoff)
                        continue
                    raise

            deleted_likes += likes_batch_deleted
            deleted_favorites += favorites_batch_deleted
            deleted_videos += videos_batch_deleted

            processed += len(batch_ids)
            print_progress(processed, total_video_ids)

            if args.sleep_seconds > 0 and batch_index < total_batches:
                time.sleep(args.sleep_seconds)

        sys.stdout.write("\n")
        elapsed = time.time() - started_at
        print("清理完成。")
        print(f"删除 videos：{deleted_videos}")
        print(f"删除 video_likes：{deleted_likes}")
        print(f"删除 favorites：{deleted_favorites}")
        print(f"耗时：{elapsed:.1f}s")
    finally:
        try:
            conn.close()
        except Exception:
            pass


if __name__ == "__main__":
    main()

"""
删除 videos 表中 cover_url 为空的视频，并清理对应记录：
- video_likes：清理所有对应 video_id 的点赞记录
- favorites：清理所有对应 video_id 的收藏记录

注意：使用数据库 `videoshub`（不是 `videohub` 备份库）。

为尽量避免锁等待/超时：
- 每批次小量删除（默认 200）
- 每批次 DELETE 后立刻 commit
- 设置会话 innodb_lock_wait_timeout
- 遇到锁等待/死锁会重试，并逐步增加等待
"""
import argparse
import sys
import time
from typing import List, Tuple, Optional

import pymysql
from pymysql.err import OperationalError


DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "videoshub",
    "charset": "utf8mb4",
}


def build_in_clause_placeholders(n: int) -> str:
    return "(" + ",".join(["%s"] * n) + ")"


def cover_empty_condition_sql() -> str:
    # 同时覆盖：NULL、''、以及纯空格的情况
    return "(cover_url IS NULL OR TRIM(cover_url) = '')"


def print_progress(processed: int, total: int, bar_width: int = 40) -> None:
    if total <= 0:
        msg = f"\r已处理: {processed}"
        sys.stdout.write(msg)
        sys.stdout.flush()
        return

    ratio = processed / total
    ratio = 0.0 if ratio < 0 else 1.0 if ratio > 1 else ratio
    filled = int(bar_width * ratio)
    bar = "#" * filled + "-" * (bar_width - filled)
    percent = ratio * 100
    sys.stdout.write(f"\r[{bar}] {processed}/{total} ({percent:6.2f}%)")
    sys.stdout.flush()


def is_lock_wait_or_deadlock(err: OperationalError) -> bool:
    # MySQL 常见错误码：
    # - 1205: Lock wait timeout exceeded
    # - 1213: Deadlock found when trying to get lock
    # 其它可能的锁冲突错误也可视情况加到这里
    code = None
    if err.args:
        try:
            code = int(err.args[0])
        except Exception:
            code = None
    return code in (1205, 1213)


def fetch_video_ids(
    cur,
    batch_size: int,
) -> List[str]:
    # 每批只取需要删除的 video_id
    sql = f"""
        SELECT video_id
        FROM videos
        WHERE {cover_empty_condition_sql()}
        ORDER BY video_id
        LIMIT %s
    """
    cur.execute(sql, (batch_size,))
    rows = cur.fetchall()
    return [r[0] for r in rows]


def count_total(cur) -> int:
    sql = f"""
        SELECT COUNT(*)
        FROM videos
        WHERE {cover_empty_condition_sql()}
    """
    cur.execute(sql)
    row = cur.fetchone()
    return int(row[0]) if row else 0


def delete_by_video_ids(cur, video_ids: List[str]) -> Tuple[int, int, int]:
    """
    返回：video_likes_deleted, favorites_deleted, videos_deleted
    """
    if not video_ids:
        return 0, 0, 0

    in_clause = build_in_clause_placeholders(len(video_ids))
    params: List[str] = video_ids

    # 先删点赞/收藏，再删 videos（减少级联工作量）
    cur.execute(f"DELETE FROM video_likes WHERE video_id IN {in_clause}", params)
    likes_deleted = cur.rowcount

    cur.execute(f"DELETE FROM favorites WHERE video_id IN {in_clause}", params)
    fav_deleted = cur.rowcount

    cur.execute(f"DELETE FROM videos WHERE video_id IN {in_clause}", params)
    videos_deleted = cur.rowcount

    return likes_deleted, fav_deleted, videos_deleted


def main() -> None:
    parser = argparse.ArgumentParser(description="删除封面为空的视频及其点赞/收藏记录")
    parser.add_argument("--batch-size", type=int, default=200, help="单批处理数量（越小越不易锁等待）")
    parser.add_argument("--max-retries", type=int, default=8, help="遇到锁等待/死锁的最大重试次数")
    parser.add_argument("--lock-wait-timeout", type=int, default=8, help="设置会话 innodb_lock_wait_timeout（秒）")
    parser.add_argument("--retry-sleep", type=float, default=0.5, help="重试等待起始时间（秒），会指数增长")
    parser.add_argument("--sleep-between-batches", type=float, default=0.0, help="批次之间额外等待（秒）")
    args = parser.parse_args()

    conn = pymysql.connect(
        **DB_CONFIG,
        autocommit=False,
    )

    try:
        with conn.cursor() as cur:
            # 降低单次锁等待时间，避免一直超时卡住
            cur.execute("SET SESSION innodb_lock_wait_timeout = %s", (args.lock_wait_timeout,))

            total = count_total(cur)
            processed = 0
            batch_no = 0

            print(f"待删除视频总数: {total}")
            if total == 0:
                return

            print_progress(processed, total)

            while True:
                batch_no += 1
                video_ids = fetch_video_ids(cur, args.batch_size)
                if not video_ids:
                    break

                # 重试：锁等待/死锁时 rollback 并重试同一批
                last_err: Optional[OperationalError] = None
                for attempt in range(1, args.max_retries + 1):
                    try:
                        likes_deleted, fav_deleted, videos_deleted = delete_by_video_ids(cur, video_ids)
                        conn.commit()
                        processed += len(video_ids)
                        print_progress(processed, total)

                        # 额外输出（避免刷屏太多）
                        sys.stdout.write(
                            f"\n批次 {batch_no}，第 {attempt} 次提交："
                            f"video_likes={likes_deleted}, favorites={fav_deleted}, videos={videos_deleted}\n"
                        )
                        sys.stdout.flush()
                        break
                    except OperationalError as e:
                        conn.rollback()
                        last_err = e
                        if not is_lock_wait_or_deadlock(e) or attempt >= args.max_retries:
                            raise

                        sleep_s = args.retry_sleep * (2 ** (attempt - 1))
                        sys.stdout.write(f"\n批次 {batch_no} 锁冲突，重试 {attempt}/{args.max_retries}，等待 {sleep_s:.2f}s... \n")
                        sys.stdout.flush()
                        time.sleep(sleep_s)

                # 仍然不幸完全失败（理论上上面已经 raise 了）
                if last_err is not None and (attempt >= args.max_retries):
                    raise last_err

                if args.sleep_between_batches > 0:
                    time.sleep(args.sleep_between_batches)

            sys.stdout.write("\n完成。\n")
            sys.stdout.flush()
    finally:
        conn.close()


if __name__ == "__main__":
    main()

