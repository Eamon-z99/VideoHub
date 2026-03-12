"""
导出推荐结果列表并计算推荐指标的脚本（用于论文实验）

功能：
1. 通过后端“实验接口”获取三种推荐算法的完整推荐列表：
   - User-CF:  /api/recommendations/experiment/user/{userId}
   - Item-CF:  /api/recommendations/experiment/item/{userId}
   - MF:       /api/recommendations/experiment/mf/{userId}?epochs=E
2. 将每个用户、每种算法的 Top-K 推荐逐条展开保存到 CSV，列包括：
   tag, user_id, algo, rank, video_id, title, like_count, view_count, raw_json
3. （可选）如果提供攻击目标视频ID列表 --targets，则计算常见推荐评估指标：
   - HR@K（Hit Rate 命中率）
   - MRR@K（Mean Reciprocal Rank 平均倒数排名）
   并绘制适合论文展示的图像：
   - 不同算法的 HR@K 对比图
   - 不同算法的 MRR@K 对比图

典型使用流程：
1）投毒前：  python export_recommendation_results.py --tag baseline --user-ids 1-5
2）执行投毒：python poison_attack.py --attack-type all --user-count 30 --target-count 5
   记录控制台打印的 目标视频: [vid1, vid2, ...]
3）投毒后：  python export_recommendation_results.py --tag poisoned --user-ids 1-50 --targets vid1,vid2,...
4）（如有防御）开启防御后再跑一遍 --tag defended
"""

import argparse
import json
import os
from typing import List, Dict, Any, Optional

import requests
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib

# 中文字体与风格配置
matplotlib.rcParams["font.sans-serif"] = ["SimHei", "Microsoft YaHei"]
matplotlib.rcParams["axes.unicode_minus"] = False
plt.style.use("seaborn-v0_8")


# 根据你提供的 token，用于脚本访问后端实验接口
JWT_TOKEN = (
    "eyJhbGciOiJIUzI1NiJ9."
    "eyJ1c2VySWQiOjEsImFjY291bnQiOiJ0ZXN0IiwidXNlcm5hbWUiOiLpmL_lt7TpmL_lt7TlhL8iLCJzdWIiOiIxIiwiaWF0IjoxNzczMDQxNjUwLCJleHAiOjE3NzMxMjgwNTB9."
    "EQ0kjPLJXKfztVw9XuIDllPGYgWUkGzSMVN80uWN3bE"
)


def parse_user_ids(user_ids_str: str) -> List[int]:
    """解析 user-ids 字符串为整型列表。支持 1-20,1,5,8 这种格式。"""
    ids: List[int] = []
    for part in user_ids_str.split(","):
        part = part.strip()
        if not part:
            continue
        if "-" in part:
            start, end = part.split("-", 1)
            ids.extend(range(int(start), int(end) + 1))
        else:
            ids.append(int(part))
    return sorted(set(ids))


def parse_targets(targets_str: Optional[str]) -> List[str]:
    """解析目标视频ID字符串。"""
    if not targets_str:
        return []
    return [t.strip() for t in targets_str.split(",") if t.strip()]


def call_experiment_api(
    base_url: str, algo: str, user_id: int, limit: int, epochs: int
) -> Dict[str, Any]:
    """调用实验接口，返回 JSON：{ok, data 或 error}"""
    session = requests.Session()
    session.headers.update({"Authorization": f"Bearer {JWT_TOKEN}"})

    if algo == "user":
        url = f"{base_url}/api/recommendations/experiment/user/{user_id}"
        params = {"limit": limit}
    elif algo == "item":
        url = f"{base_url}/api/recommendations/experiment/item/{user_id}"
        params = {"limit": limit}
    elif algo == "mf":
        url = f"{base_url}/api/recommendations/experiment/mf/{user_id}"
        params = {"limit": limit, "epochs": epochs}
    else:
        raise ValueError(f"未知算法类型: {algo}")

    try:
        resp = session.get(url, params=params)
        resp.raise_for_status()
        data = resp.json()
        return {"ok": True, "data": data}
    except Exception as e:
        print(f"[ERROR] 调用接口失败 algo={algo}, user_id={user_id}: {e}")
        return {"ok": False, "error": str(e), "data": None}


def collect_recommendations(
    base_url: str,
    tag: str,
    user_ids: List[int],
    limit: int,
    epochs: int,
    output_dir: str = "recommendation_experiments",
) -> pd.DataFrame:
    """采集推荐列表，展开为逐条记录的 DataFrame。"""
    os.makedirs(output_dir, exist_ok=True)
    records: List[Dict[str, Any]] = []
    algos = ["user", "item", "mf"]

    print(f"开始采集推荐结果，tag={tag}, 用户数={len(user_ids)}")

    for uid in user_ids:
        for algo in algos:
            print(f"\n==== 用户 {uid} | 算法 {algo} ====")
            result = call_experiment_api(base_url, algo, uid, limit, epochs)
            if not result["ok"] or not result["data"]:
                records.append(
                    {
                        "tag": tag,
                        "user_id": uid,
                        "algo": algo,
                        "rank": None,
                        "video_id": None,
                        "title": None,
                        "like_count": None,
                        "view_count": None,
                        "is_target": None,
                        "raw_json": None,
                        "ok": False,
                        "error": result.get("error"),
                    }
                )
                continue

            data = result["data"]
            video_list = data.get("list") or []
            for rank, v in enumerate(video_list, start=1):
                # v 是后端序列化的 VideoItem，通常包含 videoId/title/likeCount/viewCount 等字段
                records.append(
                    {
                        "tag": tag,
                        "user_id": uid,
                        "algo": algo,
                        "rank": rank,
                        "video_id": v.get("videoId") or v.get("id") or v.get("vid"),
                        "title": v.get("title"),
                        "like_count": v.get("likeCount"),
                        "view_count": v.get("viewCount"),
                        "is_target": None,  # 后面根据 targets 填
                        "raw_json": json.dumps(v, ensure_ascii=False),
                        "ok": True,
                        "error": None,
                    }
                )

    df = pd.DataFrame(records)
    csv_path = os.path.join(output_dir, f"recommendations_{tag}.csv")
    df.to_csv(csv_path, index=False, encoding="utf-8-sig")
    print(f"\n推荐结果明细已保存至: {csv_path}")
    return df


def mark_targets(df: pd.DataFrame, targets: List[str]) -> pd.DataFrame:
    """在 DataFrame 中标记哪些行是攻击目标视频。"""
    if not targets:
        return df
    df = df.copy()
    df["is_target"] = df["video_id"].isin(targets)
    return df


def compute_metrics(df: pd.DataFrame, targets: List[str], k: int) -> pd.DataFrame:
    """
    计算 HR@K 和 MRR@K：
    - HR@K：有多少用户的 Top-K 中至少出现过一个目标视频
    - MRR@K：对每个用户取目标视频的最佳排名 r，记 1/r，取平均
    返回：每个 algo 一行的 DataFrame
    """
    if not targets:
        print("未提供 targets，跳过指标计算。")
        return pd.DataFrame()

    df = mark_targets(df, targets)
    df_ok = df[df["ok"] == True].dropna(subset=["rank", "video_id"])
    df_ok = df_ok[df_ok["rank"] <= k]

    metrics_records: List[Dict[str, Any]] = []
    for algo in ["user", "item", "mf"]:
        sub = df_ok[df_ok["algo"] == algo]
        if sub.empty:
            continue

        # 每个用户的最优目标排名
        best_rank_per_user = (
            sub[sub["is_target"] == True]
            .groupby("user_id")["rank"]
            .min()
        )

        all_users = sub["user_id"].unique()
        hits = best_rank_per_user.index.nunique()
        hr = hits / len(all_users) if all_users.size > 0 else 0.0

        # MRR：没命中的用户记 0
        mrr_sum = 0.0
        for uid in all_users:
            r = best_rank_per_user.get(uid)
            if pd.notna(r):
                mrr_sum += 1.0 / float(r)
        mrr = mrr_sum / len(all_users) if all_users.size > 0 else 0.0

        metrics_records.append(
            {
                "algo": algo,
                "HR@{}".format(k): hr,
                "MRR@{}".format(k): mrr,
                "users": len(all_users),
                "hits": hits,
            }
        )

    metrics_df = pd.DataFrame(metrics_records)
    return metrics_df


def plot_recommendation_overview(
    df: pd.DataFrame, tag: str, output_dir: str = "recommendation_experiments"
) -> None:
    """
    对推荐结果做整体可视化（不依赖攻击目标），适合投毒前/基线场景。

    图1：不同算法推荐视频的平均受欢迎程度（平均点赞数 & 平均播放量）
    图2：不同算法的覆盖率和列表长度（去重视频数 & 每用户平均推荐数量）
    """
    os.makedirs(output_dir, exist_ok=True)
    df_ok = df[(df["ok"] == True) & df["rank"].notna()]
    if df_ok.empty:
        print("无有效推荐记录，跳过概览绘图。")
        return

    # 只保留必要列，避免类型问题
    df_ok = df_ok.copy()
    # 防止 like_count / view_count 为字符串
    df_ok["like_count"] = pd.to_numeric(df_ok["like_count"], errors="coerce")
    df_ok["view_count"] = pd.to_numeric(df_ok["view_count"], errors="coerce")

    # 图1：平均点赞 & 播放量
    stats_pop = (
        df_ok.groupby("algo")[["like_count", "view_count"]]
        .mean()
        .reset_index()
    )
    algos_display = {"user": "User-CF", "item": "Item-CF", "mf": "MF"}
    x_labels = [algos_display.get(a, a) for a in stats_pop["algo"]]

    x = range(len(x_labels))
    width = 0.35

    plt.figure(figsize=(7, 4.5))
    plt.bar(
        [i - width / 2 for i in x],
        stats_pop["like_count"],
        width=width,
        label="平均点赞数",
        color="#4caf50",
    )
    plt.bar(
        [i + width / 2 for i in x],
        stats_pop["view_count"],
        width=width,
        label="平均播放量",
        color="#2196f3",
    )
    plt.xticks(list(x), x_labels)
    plt.ylabel("平均受欢迎程度")
    plt.title(f"不同推荐算法的推荐视频受欢迎程度概览（场景：{tag}）")
    plt.legend()
    plt.grid(axis="y", alpha=0.3)
    plt.tight_layout()
    path_pop = os.path.join(output_dir, f"overview_popularity_{tag}.png")
    plt.savefig(path_pop, dpi=300)
    plt.close()
    print(f"推荐受欢迎程度概览图已保存: {path_pop}")

    # 图2：覆盖率 & 每用户平均列表长度
    coverage_records = []
    for algo in ["user", "item", "mf"]:
        sub = df_ok[df_ok["algo"] == algo]
        if sub.empty:
            continue
        unique_videos = sub["video_id"].nunique()
        users = sub["user_id"].nunique()
        avg_len = len(sub) / users if users > 0 else 0.0
        coverage_records.append(
            {
                "algo": algo,
                "unique_videos": unique_videos,
                "avg_list_len": avg_len,
                "users": users,
            }
        )
    if not coverage_records:
        return

    stats_cov = pd.DataFrame(coverage_records)
    x_labels_cov = [algos_display.get(a, a) for a in stats_cov["algo"]]
    x = range(len(x_labels_cov))

    fig, ax1 = plt.subplots(figsize=(7, 4.5))
    ax2 = ax1.twinx()

    bars1 = ax1.bar(
        [i - width / 2 for i in x],
        stats_cov["unique_videos"],
        width=width,
        color="#ff9800",
        label="去重推荐视频数",
    )
    bars2 = ax2.bar(
        [i + width / 2 for i in x],
        stats_cov["avg_list_len"],
        width=width,
        color="#9c27b0",
        label="每用户平均推荐数量",
    )

    ax1.set_xticks(list(x))
    ax1.set_xticklabels(x_labels_cov)
    ax1.set_ylabel("去重推荐视频数")
    ax2.set_ylabel("每用户平均推荐数量")
    plt.title(f"不同推荐算法的覆盖率与列表长度（场景：{tag}）")

    # 合并图例
    lines = bars1 + bars2
    labels = [b.get_label() for b in [bars1, bars2]]
    ax1.legend(lines, labels, loc="upper center")

    ax1.grid(axis="y", alpha=0.3)
    fig.tight_layout()
    path_cov = os.path.join(output_dir, f"overview_coverage_{tag}.png")
    plt.savefig(path_cov, dpi=300)
    plt.close()
    print(f"覆盖率与列表长度概览图已保存: {path_cov}")


def plot_quality_metrics(
    metrics_df: pd.DataFrame, tag: str, k: int, output_dir: str = "recommendation_experiments"
) -> None:
    """绘制 HR@K 和 MRR@K 的对比图，风格适合论文插图。"""
    if metrics_df.empty:
        return

    os.makedirs(output_dir, exist_ok=True)
    algos_display = {"user": "User-CF", "item": "Item-CF", "mf": "MF"}
    x_labels = [algos_display.get(a, a) for a in metrics_df["algo"]]

    # HR@K
    hr_col = f"HR@{k}"
    plt.figure(figsize=(6, 4))
    bars = plt.bar(x_labels, metrics_df[hr_col], color=["#4caf50", "#2196f3", "#ff9800"])
    plt.ylabel(f"HR@{k}")
    plt.ylim(0, 1.0)
    plt.title(f"不同推荐算法的 Hit Rate HR@{k}（场景：{tag}）")
    for bar, val in zip(bars, metrics_df[hr_col]):
        plt.text(bar.get_x() + bar.get_width() / 2, val + 0.01, f"{val:.2f}", ha="center", va="bottom", fontsize=10)
    plt.grid(axis="y", alpha=0.3)
    plt.tight_layout()
    path_hr = os.path.join(output_dir, f"quality_hr_at_{k}_{tag}.png")
    plt.savefig(path_hr, dpi=300)
    plt.close()
    print(f"HR@{k} 对比图已保存: {path_hr}")

    # MRR@K
    mrr_col = f"MRR@{k}"
    plt.figure(figsize=(6, 4))
    bars = plt.bar(x_labels, metrics_df[mrr_col], color=["#4caf50", "#2196f3", "#ff9800"])
    plt.ylabel(f"MRR@{k}")
    plt.ylim(0, 1.0)
    plt.title(f"不同推荐算法的 MRR@{k}（场景：{tag}）")
    for bar, val in zip(bars, metrics_df[mrr_col]):
        plt.text(bar.get_x() + bar.get_width() / 2, val + 0.01, f"{val:.2f}", ha="center", va="bottom", fontsize=10)
    plt.grid(axis="y", alpha=0.3)
    plt.tight_layout()
    path_mrr = os.path.join(output_dir, f"quality_mrr_at_{k}_{tag}.png")
    plt.savefig(path_mrr, dpi=300)
    plt.close()
    print(f"MRR@{k} 对比图已保存: {path_mrr}")


def plot_user_topk_lists(
    df: pd.DataFrame,
    tag: str,
    user_ids: List[int],
    max_k: int,
    output_dir: str = "recommendation_experiments",
) -> None:
    """
    针对指定的用户，绘制“Top-K 推荐列表”图：
    - Y 轴：视频标题（MovieLens 场景下就是电影名）
    - X 轴：推荐顺序分数（rank 越靠前数值越大，便于条形图展示）
    每个 (user, algo) 生成一张图，文件名形如 list_user{uid}_{algo}_{tag}.png
    """
    if not user_ids:
        return

    os.makedirs(output_dir, exist_ok=True)
    df_ok = df[(df["ok"] == True) & df["rank"].notna()]
    if df_ok.empty:
        print("无有效推荐记录，跳过用户 Top-K 列表绘图。")
        return

    algo_names = {"user": "User-CF", "item": "Item-CF", "mf": "MF"}

    for uid in user_ids:
        for algo in ["user", "item", "mf"]:
            sub = df_ok[(df_ok["tag"] == tag) & (df_ok["user_id"] == uid) & (df_ok["algo"] == algo)]
            if sub.empty:
                continue
            sub = sub.sort_values("rank").head(max_k).copy()
            max_rank = sub["rank"].max()
            sub["score_for_plot"] = max_rank + 1 - sub["rank"]

            plt.figure(figsize=(9, 6))
            plt.barh(sub["title"], sub["score_for_plot"], color="#4caf50")
            plt.gca().invert_yaxis()  # rank 越小越靠上
            plt.xlabel("推荐顺序（数值越大表示越靠前）")
            plt.title(f"用户 {uid} 的 {algo_names.get(algo, algo)} Top-{len(sub)} 推荐（场景：{tag}）")
            plt.tight_layout()

            out_path = os.path.join(output_dir, f"list_user{uid}_{algo}_{tag}.png")
            plt.savefig(out_path, dpi=300)
            plt.close()
            print(f"用户 {uid}，算法 {algo} 的 Top-K 列表图已保存: {out_path}")


def main():
    parser = argparse.ArgumentParser(description="导出推荐结果列表并计算推荐指标（论文实验）")
    parser.add_argument(
        "--base-url",
        type=str,
        default="http://localhost:8080",
        help="后端服务基础 URL（例如 http://localhost:8080）",
    )
    parser.add_argument(
        "--user-ids",
        type=str,
        default="1-20",
        help='用于实验的用户ID列表，格式如 "1-20" 或 "1,2,5-10"',
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=50,
        help="每个算法返回的推荐数量上限（Top-K）",
    )
    parser.add_argument(
        "--epochs",
        type=int,
        default=20,
        help="MF 训练轮数",
    )
    parser.add_argument(
        "--tag",
        type=str,
        default="baseline",
        help="实验标签（baseline / poisoned / defended 等）",
    )
    parser.add_argument(
        "--targets",
        type=str,
        default="",
        help='攻击目标视频ID列表，逗号分隔，例如 "ml_movie_1,ml_movie_2"',
    )
    parser.add_argument(
        "--k",
        type=int,
        default=50,
        help="计算 HR@K / MRR@K 时使用的 K，通常与 limit 一致",
    )
    parser.add_argument(
        "--plot-users",
        type=str,
        default="",
        help="需要绘制 Top-K 推荐列表图的用户ID集合，如 '1,1001-1003'；为空则不绘制",
    )
    args = parser.parse_args()

    user_ids = parse_user_ids(args.user_ids)
    targets = parse_targets(args.targets)

    df = collect_recommendations(
        base_url=args.base_url,
        tag=args.tag,
        user_ids=user_ids,
        limit=args.limit,
        epochs=args.epochs,
    )

    # 无论是否有攻击目标，都先画整体概览图（适合投毒前基线展示）
    plot_recommendation_overview(df, tag=args.tag)

    if targets:
        metrics_df = compute_metrics(df, targets, k=args.k)
        if not metrics_df.empty:
            metrics_path = os.path.join("recommendation_experiments", f"metrics_k{args.k}_{args.tag}.csv")
            metrics_df.to_csv(metrics_path, index=False, encoding="utf-8-sig")
            print(f"推荐指标已保存: {metrics_path}")
            plot_quality_metrics(metrics_df, tag=args.tag, k=args.k)

    # 可选：为指定用户绘制 Top-K 推荐列表（适合展示 MovieLens 用户的电影推荐）
    if args.plot_users:
        plot_user_ids = parse_user_ids(args.plot_users)
        plot_user_topk_lists(df, tag=args.tag, user_ids=plot_user_ids, max_k=args.k)


if __name__ == "__main__":
    main()


