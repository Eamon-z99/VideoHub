# FFmpeg 安装指南（Windows）

## 方案一：下载并添加到 PATH（推荐）

### Step 1：下载 FFmpeg

1. 访问 FFmpeg 官网：https://ffmpeg.org/download.html
2. 点击 **Windows builds from gyan.dev** 或直接访问：https://www.gyan.dev/ffmpeg/builds/
3. 下载 **ffmpeg-release-essentials.zip**（完整版，包含所有功能）

### Step 2：解压

1. 解压到任意目录，例如：`C:\ffmpeg`
2. 解压后的目录结构应该是：
   ```
   C:\ffmpeg\
     ├─ bin\
     │  ├─ ffmpeg.exe
     │  ├─ ffprobe.exe
     │  └─ ...
     ├─ doc\
     └─ ...
   ```

### Step 3：添加到 PATH

#### 方法A：通过系统设置（推荐）

1. 右键 **此电脑** → **属性**
2. 点击 **高级系统设置**
3. 点击 **环境变量**
4. 在 **系统变量** 中找到 `Path`，点击 **编辑**
5. 点击 **新建**，添加：`C:\ffmpeg\bin`
6. 点击 **确定** 保存所有窗口

#### 方法B：通过命令行（临时，重启后失效）

```cmd
setx PATH "%PATH%;C:\ffmpeg\bin"
```

### Step 4：验证安装

**重新打开命令行窗口**（重要！），然后运行：

```cmd
ffmpeg -version
```

如果显示版本信息，说明安装成功！

---

## 方案二：使用完整路径（无需安装到 PATH）

如果不想修改系统 PATH，可以直接在脚本中指定 FFmpeg 的完整路径。

### Step 1：下载 FFmpeg

同上，下载并解压到任意目录，例如：`C:\ffmpeg`

### Step 2：修改脚本

编辑 `scripts/m4s_to_mp4.py`，修改第19行：

```python
# 修改前
FFMPEG_PATH = "ffmpeg"

# 修改后（使用完整路径）
FFMPEG_PATH = r"C:\ffmpeg\bin\ffmpeg.exe"
```

同时修改 `scripts/batch_transcode.py` 中的 FFmpeg 路径。

---

## 快速下载链接（多个源）

### 方案1：官方源（如果慢，尝试下面的镜像）

- **完整版（推荐）**：https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip
- **仅基础版**：https://www.gyan.dev/ffmpeg/builds/ffmpeg-release.zip

### 方案2：GitHub Releases（通常更快）

- **完整版**：https://github.com/BtbN/FFmpeg-Builds/releases
  - 下载 `ffmpeg-master-latest-win64-gpl.zip` 或 `ffmpeg-master-latest-win64-gpl-shared.zip`
- **优点**：GitHub在国内访问相对稳定，可以使用GitHub加速服务

### 方案3：使用GitHub加速服务（推荐）

如果GitHub也慢，可以使用以下加速服务：

1. **GitHub Proxy**：
   - 访问：https://ghproxy.com/
   - 输入GitHub下载链接，获取加速链接

2. **FastGit**：
   - 将 `github.com` 替换为 `download.fastgit.org`
   - 例如：`https://download.fastgit.org/BtbN/FFmpeg-Builds/releases`

3. **GitHub加速网站**：
   - https://ghproxy.com/
   - https://github.com.cnpmjs.org/
   - https://hub.fastgit.xyz/

### 方案4：国内镜像/网盘（最快）

1. **百度网盘**（搜索关键词：ffmpeg windows）
2. **蓝奏云**（搜索关键词：ffmpeg）
3. **123云盘**（搜索关键词：ffmpeg）

### 方案5：使用包管理器（如果已安装）

如果你安装了 **Chocolatey** 或 **Scoop**：

```cmd
# Chocolatey
choco install ffmpeg

# Scoop
scoop install ffmpeg
```

### 方案6：使用国内开发者提供的编译版本

- 搜索 "FFmpeg Windows 下载 国内" 可以找到很多国内开发者提供的下载链接

---

## 验证安装

安装完成后，运行：

```cmd
ffmpeg -version
ffprobe -version
```

两个命令都应该显示版本信息。

---

## 常见问题

### Q: 添加 PATH 后仍然提示找不到命令？

**A:** 需要**重新打开命令行窗口**，PATH 的修改才会生效。

### Q: 不想修改系统 PATH？

**A:** 使用方案二，直接在脚本中指定完整路径。

### Q: 下载速度慢？

**A:** 可以尝试使用国内镜像或下载工具。

---

## 推荐配置

建议使用方案一（添加到 PATH），这样：
- 所有脚本都可以直接使用 `ffmpeg` 命令
- 不需要修改每个脚本的路径配置
- 其他工具也可以使用 FFmpeg

