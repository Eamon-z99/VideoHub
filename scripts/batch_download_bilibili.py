#!/usr/bin/env python3
# -*- coding: utf-8 -*-
r"""
B站历史记录批量下载脚本
从 bilibili-history-2025-12-06.json 读取视频列表，使用 yt-dlp 下载
"""

import json
import subprocess
import sys
import os
from pathlib import Path
import re
import time

# ========== 配置区域 ==========
# JSON文件路径
HISTORY_JSON = r"D:\vue_project\VideoHub\scripts\bilibili-history-2025-12-06.json"

# 输出目录
OUTPUT_DIR = r"E:\Videos"

# 下载范围（从第几个到第几个，从1开始计数）
# 设置为 None 表示下载全部
START_INDEX = 567   # ⚠️ 修改这里：从第几个开始（从1开始）
END_INDEX = 1000    # ⚠️ 修改这里：到第几个结束（包含这个）

# yt-dlp路径（如果在PATH中，使用 "yt-dlp"，否则使用完整路径）
YT_DLP_PATH = "yt-dlp"

# FFmpeg路径（如果在PATH中，使用 "ffmpeg"，否则使用完整路径）
# 如果FFmpeg不在PATH中，请设置完整路径，例如：r"D:\ffmpeg\ffmpeg-8.0.1-essentials_build\bin\ffmpeg.exe"
FFMPEG_PATH = r"D:\ffmpeg\ffmpeg-8.0.1-essentials_build\bin\ffmpeg.exe"  # ⚠️ 修改这里：FFmpeg路径

# ========== 函数定义 ==========

def sanitize_filename(filename):
    """清理文件名，移除Windows不允许的字符"""
    # Windows不允许的字符: < > : " / \ | ? *
    invalid_chars = r'[<>:"/\\|?*]'
    # 替换为下划线
    filename = re.sub(invalid_chars, '_', filename)
    # 移除前后空格和点
    filename = filename.strip(' .')
    # 限制长度（Windows路径限制）
    if len(filename) > 200:
        filename = filename[:200]
    return filename


def load_history_json(json_path):
    """加载历史记录JSON文件"""
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f"❌ 读取JSON文件失败: {e}")
        return None


def filter_archive_videos(history_data):
    """过滤出 business="archive" 且有 bvid 的视频"""
    archive_videos = []
    
    for item in history_data:
        if (item.get("business") == "archive" and 
            item.get("bvid") and 
            item.get("bvid").startswith("BV")):
            archive_videos.append({
                "bvid": item.get("bvid"),
                "title": item.get("title", ""),
                "cover": item.get("cover", ""),
                "author": item.get("author_name", ""),
            })
    
    return archive_videos


def download_video(video_info, output_base_dir, index, total, ffmpeg_available=True, ffmpeg_path=None):
    """下载单个视频"""
    bvid = video_info["bvid"]
    title = video_info["title"]
    url = f"https://www.bilibili.com/video/{bvid}"
    
    # 清理标题作为文件夹名
    safe_title = sanitize_filename(title)
    if not safe_title:
        safe_title = bvid  # 如果标题为空，使用BV号
    
    # 创建视频文件夹
    video_dir = Path(output_base_dir) / safe_title
    video_dir.mkdir(parents=True, exist_ok=True)
    
    print(f"\n{'='*60}")
    print(f"[{index}/{total}] 开始下载: {title}")
    print(f"  BV号: {bvid}")
    print(f"  文件夹: {video_dir}")
    
    # 检查是否已下载（yt-dlp默认会在当前目录下载，需要检查video_dir）
    # 但yt-dlp的-o参数需要指定完整路径，所以我们先检查
    video_files = list(video_dir.glob("*.mp4"))
    thumbnail_files = list(video_dir.glob("*.jpg")) + list(video_dir.glob("*.png")) + list(video_dir.glob("*.webp"))
    
    if video_files and thumbnail_files:
        print(f"  ⏭️ 视频和封面已存在，跳过")
        return True
    
    # yt-dlp 命令
    # --write-thumbnail: 下载封面
    # --format: 优先选择H.264编码（避免AV1编码，兼容性更好）
    #   "bv[ext=mp4][vcodec^=avc]+ba[ext=m4a]/bv[ext=mp4]+ba[ext=m4a]/b"
    #   意思是：优先H.264视频+音频，其次MP4视频+音频，最后最佳合并格式
    cmd = [
        YT_DLP_PATH,
        "--write-thumbnail",  # 下载封面
        "--format", "bv[ext=mp4][vcodec^=avc]+ba[ext=m4a]/bv[ext=mp4]+ba[ext=m4a]/b",  # 优先H.264，避免AV1
        url
    ]
    
    # 如果FFmpeg可用，添加合并参数
    if ffmpeg_available:
        cmd.insert(-1, "--merge-output-format")
        cmd.insert(-1, "mp4")
    
    try:
        # 显示完整命令
        cmd_str = ' '.join(f'"{arg}"' if ' ' in str(arg) or '://' in str(arg) else str(arg) for arg in cmd)
        print(f"  执行命令: {cmd_str}")
        print(f"  下载目录: {video_dir}")
        print(f"  开始下载（显示实时进度）...\n")
        
        # 实时显示输出，让用户看到下载进度
        # 如果指定了FFmpeg路径，设置环境变量让yt-dlp能找到ffmpeg
        env = None
        if ffmpeg_available and ffmpeg_path and ffmpeg_path != "ffmpeg":
            import os
            ffmpeg_dir = str(Path(ffmpeg_path).parent)
            env = os.environ.copy()
            env["PATH"] = ffmpeg_dir + os.pathsep + env.get("PATH", "")
        
        result = subprocess.run(
            cmd,
            timeout=1800,  # 30分钟超时
            cwd=str(video_dir),  # 在指定目录执行
            env=env  # 使用修改后的环境变量（如果指定了FFmpeg路径）
        )
        
        if result.returncode == 0:
            # 检查下载的文件
            video_files = list(video_dir.glob("*.mp4"))
            thumbnail_files = list(video_dir.glob("*.jpg")) + list(video_dir.glob("*.png"))
            
            if video_files:
                video_size = video_files[0].stat().st_size / 1024 / 1024
                print(f"  ✅ 下载成功！")
                print(f"  视频文件: {video_files[0].name} ({video_size:.2f} MB)")
                if thumbnail_files:
                    print(f"  封面文件: {thumbnail_files[0].name}")
                return True
            else:
                print(f"  ⚠️ 命令成功但未找到视频文件")
                return False
        else:
            print(f"\n  ❌ 下载失败 (退出码: {result.returncode})")
            return False
            
    except subprocess.TimeoutExpired:
        print(f"  ❌ 下载超时（超过30分钟）")
        return False
    except FileNotFoundError:
        print(f"  ❌ 未找到 yt-dlp，请先安装: pip install yt-dlp")
        return False
    except Exception as e:
        print(f"  ❌ 下载异常: {e}")
        return False


def batch_download():
    """批量下载视频"""
    print(f"\n{'='*60}")
    print(f"B站历史记录批量下载工具")
    print(f"{'='*60}\n")
    
    # 检查yt-dlp
    try:
        result = subprocess.run(
            [YT_DLP_PATH, "--version"],
            capture_output=True,
            check=True,
            timeout=5
        )
        print(f"✓ yt-dlp 可用")
    except (subprocess.CalledProcessError, FileNotFoundError, subprocess.TimeoutExpired):
        print(f"❌ 未找到 yt-dlp")
        print(f"\n请先安装: pip install yt-dlp")
        sys.exit(1)
    
    # 检查FFmpeg（用于合并视频和音频）
    ffmpeg_available = False
    ffmpeg_path = None
    
    # 先尝试使用配置的路径
    if FFMPEG_PATH and Path(FFMPEG_PATH).exists():
        try:
            result = subprocess.run(
                [FFMPEG_PATH, "-version"],
                capture_output=True,
                check=True,
                timeout=5
            )
            ffmpeg_available = True
            ffmpeg_path = FFMPEG_PATH
            print(f"✓ FFmpeg 可用（路径: {ffmpeg_path}）")
            print(f"  可以合并视频和音频\n")
        except (subprocess.CalledProcessError, FileNotFoundError, subprocess.TimeoutExpired):
            pass
    
    # 如果配置的路径不可用，尝试系统PATH中的ffmpeg
    if not ffmpeg_available:
        try:
            result = subprocess.run(
                ["ffmpeg", "-version"],
                capture_output=True,
                check=True,
                timeout=5
            )
            ffmpeg_available = True
            ffmpeg_path = "ffmpeg"
            print(f"✓ FFmpeg 可用（系统PATH）")
            print(f"  可以合并视频和音频\n")
        except (subprocess.CalledProcessError, FileNotFoundError, subprocess.TimeoutExpired):
            print(f"⚠️ 未找到 FFmpeg（视频和音频可能无法自动合并）")
            if FFMPEG_PATH:
                print(f"   配置路径: {FFMPEG_PATH}")
                if not Path(FFMPEG_PATH).exists():
                    print(f"   ❌ 路径不存在，请检查配置")
            print(f"   建议安装 FFmpeg 或检查路径配置")
            print(f"   安装方法: 参考 docs/FFmpeg安装指南.md\n")
    
    # 加载JSON文件
    print(f"读取历史记录文件: {HISTORY_JSON}")
    history_data = load_history_json(HISTORY_JSON)
    if not history_data:
        sys.exit(1)
    
    print(f"✓ 成功加载，共 {len(history_data)} 条记录\n")
    
    # 过滤出archive视频
    archive_videos = filter_archive_videos(history_data)
    total_count = len(archive_videos)
    print(f"找到 {total_count} 个视频（business=archive 且有 bvid）\n")
    
    if not archive_videos:
        print("❌ 没有找到可下载的视频")
        return
    
    # 选择下载范围
    if START_INDEX is not None and END_INDEX is not None:
        # 验证范围
        if START_INDEX < 1:
            print(f"❌ 起始索引必须 >= 1，当前为 {START_INDEX}")
            sys.exit(1)
        if END_INDEX < START_INDEX:
            print(f"❌ 结束索引 ({END_INDEX}) 必须 >= 起始索引 ({START_INDEX})")
            sys.exit(1)
        if START_INDEX > total_count:
            print(f"❌ 起始索引 ({START_INDEX}) 超出范围（总共 {total_count} 个视频）")
            sys.exit(1)
        
        # 转换为Python索引（从0开始）
        start_idx = START_INDEX - 1
        end_idx = min(END_INDEX, total_count)  # 确保不超出范围
        
        videos_to_download = archive_videos[start_idx:end_idx]
        print(f"📋 下载范围: 第 {START_INDEX} 个 到 第 {end_idx} 个（共 {len(videos_to_download)} 个）\n")
    else:
        videos_to_download = archive_videos
        print(f"📋 下载全部: {total_count} 个视频\n")
    
    # 确认
    print(f"准备下载 {len(videos_to_download)} 个视频")
    print(f"输出目录: {OUTPUT_DIR}")
    response = input("\n是否开始下载？(y/n): ")
    if response.lower() != 'y':
        print("已取消")
        return
    
    # 创建输出目录
    output_path = Path(OUTPUT_DIR)
    output_path.mkdir(parents=True, exist_ok=True)
    
    # 开始下载
    success_count = 0
    fail_count = 0
    skip_count = 0
    
    # 计算实际索引（用于显示）
    actual_start = START_INDEX if START_INDEX is not None else 1
    
    for idx, video_info in enumerate(videos_to_download, 1):
        try:
            actual_index = actual_start + idx - 1  # 实际在总列表中的位置
            result = download_video(video_info, OUTPUT_DIR, actual_index, total_count, ffmpeg_available, ffmpeg_path)
            if result:
                success_count += 1
            else:
                fail_count += 1
        except KeyboardInterrupt:
            print("\n\n用户中断")
            break
        except Exception as e:
            print(f"  ❌ 处理失败: {e}")
            fail_count += 1
        
        # 短暂延迟，避免请求过快
        if idx < len(videos_to_download):
            time.sleep(1)
    
    # 统计结果
    print(f"\n{'='*60}")
    print(f"下载完成！")
    print(f"成功: {success_count}")
    print(f"失败: {fail_count}")
    print(f"{'='*60}\n")


if __name__ == "__main__":
    batch_download()

