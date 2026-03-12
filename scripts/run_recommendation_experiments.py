"""
基于后端实验接口的推荐算法对比脚本

功能：
1. 调用后端的三种“完整版”推荐接口：
   - User-CF:  /api/recommendations/experiment/user/{userId}
   - Item-CF:  /api/recommendations/experiment/item/{userId}
   - MF:       /api/recommendations/experiment/mf/{userId}?epochs=20
2. 对一批用户采样，记录每种算法的：
   - 接口耗时（durationMs）
   - 返回的推荐数量（total）
3. 将结果保存为 CSV，并绘制图像：
   - 不同算法的平均耗时对比柱状图
   - 不同算法的耗时分布箱线图
   - 不同算法的推荐数量分布柱状图

使用场景：
- 毕业论文实验：展示 User-CF / Item-CF / MF 三种算法在同一数据集上的
  运行时间和推荐规模差异，可配合投毒前/后分别运行两次，便于对比。
"""

import argparse
import os
import time
from typing import List, Dict, Any

import requests
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib


# 中文显示配置
matplotlib.rcParams["font.sans-serif"] = ["SimHei", "Microsoft YaHei"]
matplotlib.rcParams["axes.unicode_minus"] = False


def call_api(base_url: str, algo: str, user_id: int, limit: int, epochs: int) -> Dict[str, Any]:
    """调用后端实验接口，返回 JSON 结果（出错时返回简单结构，避免脚本中断）"""
    session = requests.Session()
    # 如果需要带登录态（JWT），在这里设置 Authorization 头
    # 当前根据用户提供的 token 进行实验调用
    session.headers.update({
        "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImFjY291bnQiOiJ0ZXN0IiwidXNlcm5hbWUiOiLpmL_lt7TpmL_lt7TlhL8iLCJzdWIiOiIxIiwiaWF0IjoxNzczMDQxNjUwLCJleHAiOjE3NzMxMjgwNTB9.EQ0kjPLJXKfztVw9XuIDllPGYgWUkGzSMVN80uWN3bE"
    })
    url = ""
    params = {}

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
        # 这里不设置 timeout，让后端完整跑完
        resp = session.get(url, params=params)
        resp.raise_for_status()
        data = resp.json()
        return {
            "ok": True,
            "raw": data,
            "durationMs": data.get("durationMs"),
            "total": data.get("total"),
        }
    except Exception as e:
        print(f"[ERROR] 调用接口失败 algo={algo}, user_id={user_id}: {e}")
        return {
            "ok": False,
            "raw": None,
            "durationMs": None,
            "total": None,
            "error": str(e),
        }


def run_experiments(
    base_url: str,
    user_ids: List[int],
    limit: int,
    epochs: int,
    tag: str,
    output_dir: str = "recommendation_experiments",
) -> pd.DataFrame:
    """对一批用户执行三种推荐算法实验，并返回结果 DataFrame"""
    os.makedirs(output_dir, exist_ok=True)

    records: List[Dict[str, Any]] = []
    algos = ["user", "item", "mf"]

    print(f"开始运行推荐实验，base_url={base_url}, 用户数={len(user_ids)}, tag={tag}")

    for uid in user_ids:
        for algo in algos:
            print(f"\n==== 用户 {uid} | 算法 {algo} ====")
            t0 = time.time()
            result = call_api(base_url, algo, uid, limit, epochs)
            t1 = time.time()
            elapsed = (t1 - t0) * 1000

            duration = result.get("durationMs") or elapsed
            total = result.get("total")

            print(f"接口返回耗时(durationMs)={result.get('durationMs')}, 实测耗时={elapsed:.2f} ms, total={total}")

            records.append(
                {
                    "tag": tag,
                    "user_id": uid,
                    "algo": algo,
                    "duration_ms_reported": result.get("durationMs"),
                    "duration_ms_measured": elapsed,
                    "duration_ms_used": duration,
                    "total": total,
                    "ok": result.get("ok", False),
                    "error": result.get("error"),
                }
            )

    df = pd.DataFrame(records)
    csv_path = os.path.join(output_dir, f"results_{tag}.csv")
    df.to_csv(csv_path, index=False, encoding="utf-8-sig")
    print(f"\n实验结果已保存至: {csv_path}")
    return df


def plot_results(df: pd.DataFrame, tag: str, output_dir: str = "recommendation_experiments") -> None:
    """基于实验结果 DataFrame 绘制三种算法的耗时和推荐数量对比图"""
    os.makedirs(output_dir, exist_ok=True)
    df_ok = df[df["ok"] == True].copy()
    if df_ok.empty:
        print("没有成功的请求记录，无法绘图。")
        return

    # 1. 算法平均耗时柱状图
    grouped = df_ok.groupby("algo")["duration_ms_used"].agg(["mean", "std", "count"]).reset_index()
    plt.figure(figsize=(8, 5))
    plt.bar(grouped["algo"], grouped["mean"], yerr=grouped["std"], capsize=5, color=["#4caf50", "#2196f3", "#ff9800"])
    plt.ylabel("平均耗时 (ms)")
    plt.title(f"不同推荐算法平均耗时对比（tag={tag}）")
    for i, row in grouped.iterrows():
        plt.text(i, row["mean"], f"{row['mean']:.0f}", ha="center", va="bottom", fontsize=10)
    plt.grid(axis="y", alpha=0.3)
    path1 = os.path.join(output_dir, f"algo_avg_latency_{tag}.png")
    plt.tight_layout()
    plt.savefig(path1, dpi=300)
    plt.close()
    print(f"平均耗时对比图已保存: {path1}")

    # 2. 算法耗时分布箱线图
    plt.figure(figsize=(8, 5))
    data = [df_ok[df_ok["algo"] == a]["duration_ms_used"] for a in ["user", "item", "mf"]]
    plt.boxplot(data, labels=["User-CF", "Item-CF", "MF"], showfliers=True)
    plt.ylabel("耗时 (ms)")
    plt.title(f"不同推荐算法耗时分布（tag={tag}）")
    plt.grid(axis="y", alpha=0.3)
    path2 = os.path.join(output_dir, f"algo_latency_boxplot_{tag}.png")
    plt.tight_layout()
    plt.savefig(path2, dpi=300)
    plt.close()
    print(f"耗时分布箱线图已保存: {path2}")

    # 3. 算法推荐数量分布柱状图（平均 total）
    total_grouped = df_ok.groupby("algo")["total"].mean().reset_index()
    plt.figure(figsize=(8, 5))
    plt.bar(total_grouped["algo"], total_grouped["total"], color=["#4caf50", "#2196f3", "#ff9800"])
    plt.ylabel("平均推荐数量")
    plt.title(f"不同推荐算法平均推荐数量对比（tag={tag}）")
    for i, row in total_grouped.iterrows():
        plt.text(i, row["total"], f"{row['total']:.1f}", ha="center", va="bottom", fontsize=10)
    plt.grid(axis="y", alpha=0.3)
    path3 = os.path.join(output_dir, f"algo_avg_total_{tag}.png")
    plt.tight_layout()
    plt.savefig(path3, dpi=300)
    plt.close()
    print(f"平均推荐数量对比图已保存: {path3}")


def parse_user_ids(user_ids_str: str) -> List[int]:
    """
    解析命令行参数中的 user_ids：
    - "1,2,3,10-20" -> [1,2,3,10,11,...,20]
    """
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
    # 去重并排序
    return sorted(set(ids))


def main():
    parser = argparse.ArgumentParser(description="运行推荐算法实验并绘制结果图像")
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
        help="每个算法返回的推荐数量上限",
    )
    parser.add_argument(
        "--epochs",
        type=int,
        default=20,
        help="MF 训练轮数（仅对 /experiment/mf 生效）",
    )
    parser.add_argument(
        "--tag",
        type=str,
        default="baseline",
        help="实验标签（会出现在文件名中，便于区分如 baseline / poisoned / defended）",
    )
    args = parser.parse_args()

    user_ids = parse_user_ids(args.user_ids)
    df = run_experiments(
        base_url=args.base_url,
        user_ids=user_ids,
        limit=args.limit,
        epochs=args.epochs,
        tag=args.tag,
    )
    plot_results(df, tag=args.tag)


if __name__ == "__main__":
    main()


