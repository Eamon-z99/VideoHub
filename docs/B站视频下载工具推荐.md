# B站视频下载工具推荐（直接下载MP4）

> 这些工具可以直接下载MP4格式，无需处理m4s合并问题

---

## 🚀 推荐工具（按易用性排序）

### 1. **BBDown**（推荐⭐⭐⭐⭐⭐）

**特点：**
- 命令行工具，功能强大
- 支持直接下载MP4格式
- 支持多清晰度选择
- 支持批量下载
- 开源免费

**下载地址：**
- GitHub: https://github.com/nilaoda/BBDown
- Releases: https://github.com/nilaoda/BBDown/releases

**使用方法：**
```bash
# 下载单个视频（直接MP4）
BBDown.exe "https://www.bilibili.com/video/BV12j4izaEzL"

# 指定清晰度
BBDown.exe "BV12j4izaEzL" --qn 80  # 1080P

# 批量下载
BBDown.exe "BV12j4izaEzL" --playlist
```

**优点：**
- 直接输出MP4，无需合并
- 支持选择清晰度
- 支持下载封面、字幕、弹幕

---

### 2. **you-get**（推荐⭐⭐⭐⭐）

**特点：**
- Python工具，跨平台
- 支持B站、YouTube等多个网站
- 可以直接下载MP4

**安装：**
```bash
pip install you-get
```

**使用方法：**
```bash
# 下载视频（自动选择最高清晰度）
you-get "https://www.bilibili.com/video/BV12j4izaEzL"

# 指定清晰度
you-get --format=dash-flv720 "https://www.bilibili.com/video/BV12j4izaEzL"

# 查看可用清晰度
you-get -i "https://www.bilibili.com/video/BV12j4izaEzL"
```

**优点：**
- 简单易用
- 支持多网站
- 自动合并音视频

---

### 3. **yt-dlp**（推荐⭐⭐⭐⭐⭐）

**特点：**
- you-get的增强版
- 功能最强大
- 支持直接下载MP4

**安装：**
```bash
pip install yt-dlp
```

**使用方法：**
```bash
# 下载视频
yt-dlp "https://www.bilibili.com/video/BV12j4izaEzL"

# 指定格式（MP4）
yt-dlp -f "bestvideo+bestaudio" --merge-output-format mp4 "BV12j4izaEzL"

# 批量下载
yt-dlp -f "bestvideo+bestaudio" --merge-output-format mp4 "https://www.bilibili.com/playlist/xxx"
```

**优点：**
- 功能最全
- 支持几乎所有视频网站
- 可以自定义输出格式

---

### 4. **BilibiliVideoDownload**（GUI工具）

**特点：**
- 图形界面，操作简单
- 支持批量下载
- 可以直接下载MP4

**下载地址：**
- GitHub搜索：BilibiliVideoDownload
- 或搜索"B站视频下载器"

**优点：**
- 图形界面，无需命令行
- 适合不熟悉命令行的用户

---

### 5. **Jijidown**（官方工具）

**特点：**
- B站官方出品的下载工具
- 功能完整
- 支持直接下载MP4

**下载地址：**
- 访问：https://www.jijidown.com/

**优点：**
- 官方工具，稳定可靠
- 支持登录，可下载会员视频

---

## 📊 工具对比

| 工具 | 类型 | 易用性 | 功能 | 推荐度 |
|------|------|--------|------|--------|
| BBDown | 命令行 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| you-get | 命令行 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| yt-dlp | 命令行 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| BilibiliVideoDownload | GUI | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| Jijidown | GUI | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

---

## 🎯 针对你的需求（1000个视频）

### 推荐方案：**yt-dlp** 或 **BBDown**

**原因：**
1. **批量下载能力强**：支持播放列表、批量处理
2. **直接输出MP4**：无需处理m4s合并
3. **可编程**：可以用Python脚本批量调用

### 批量下载脚本示例（yt-dlp）

```python
import subprocess
import json

# 读取视频列表（BV号列表）
video_list = [
    "BV12j4izaEzL",
    "BV1e228BxEkk",
    # ... 更多BV号
]

output_dir = r"E:\Videos"

for bv in video_list:
    url = f"https://www.bilibili.com/video/{bv}"
    cmd = [
        "yt-dlp",
        "-f", "bestvideo+bestaudio",  # 选择最佳清晰度
        "--merge-output-format", "mp4",  # 合并为MP4
        "-o", f"{output_dir}/%(title)s.%(ext)s",  # 输出文件名
        url
    ]
    subprocess.run(cmd)
```

---

## 💡 快速开始

### 方案A：使用 yt-dlp（推荐）

```bash
# 1. 安装
pip install yt-dlp

# 2. 下载单个视频
yt-dlp --merge-output-format mp4 "https://www.bilibili.com/video/BV12j4izaEzL"

# 3. 批量下载（从播放列表）
yt-dlp --merge-output-format mp4 "https://www.bilibili.com/playlist/xxx"
```

### 方案B：使用 BBDown

```bash
# 1. 下载 BBDown.exe
# 从 https://github.com/nilaoda/BBDown/releases 下载

# 2. 下载视频
BBDown.exe "BV12j4izaEzL" --format mp4

# 3. 批量下载
BBDown.exe "BV12j4izaEzL" --playlist
```

---

## 🔧 如果工具下载的也是m4s格式

如果工具下载的仍然是m4s，可以：

1. **使用工具的合并功能**：大多数工具都自带合并功能
2. **使用工具的参数**：指定输出格式为MP4
3. **使用我创建的脚本**：`scripts/m4s_to_mp4.py`（虽然当前有问题，但可以继续优化）

---

## 📝 总结

**对于你的需求（1000个视频）：**

1. **首选**：使用 **yt-dlp** 或 **BBDown** 批量下载
2. **优势**：直接输出MP4，无需处理m4s合并
3. **效率**：可以写脚本批量处理，比手动转换快得多

**建议：**
- 先用 yt-dlp 测试下载1-2个视频
- 确认能直接得到MP4后，写批量脚本处理1000个视频
- 这样比先下载m4s再转换要快得多
python scripts/batch_download_bilibili.py







