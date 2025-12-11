# FFmpeg 快速下载方案

如果官方源下载慢，可以使用以下方法：

## 🚀 最快方案：GitHub Releases + 加速

### Step 1：访问 GitHub Releases

打开：https://github.com/BtbN/FFmpeg-Builds/releases

### Step 2：下载文件

找到最新的 release，下载：
- **`ffmpeg-master-latest-win64-gpl-shared.zip`**（推荐，包含所有依赖）
- 或 `ffmpeg-master-latest-win64-gpl.zip`

### Step 3：如果 GitHub 也慢，使用加速

#### 方法A：使用 GitHub Proxy

1. 复制 GitHub 下载链接（右键下载按钮 → 复制链接地址）
2. 访问：https://ghproxy.com/
3. 粘贴链接，点击下载

#### 方法B：使用 FastGit

将下载链接中的 `github.com` 替换为 `download.fastgit.org`

例如：
```
原链接：https://github.com/BtbN/FFmpeg-Builds/releases/download/autobuild-2024-01-01-12-00/ffmpeg-master-latest-win64-gpl-shared.zip

加速链接：https://download.fastgit.org/BtbN/FFmpeg-Builds/releases/download/autobuild-2024-01-01-12-00/ffmpeg-master-latest-win64-gpl-shared.zip
```

---

## 📦 方案2：使用包管理器（推荐，自动安装）

### 如果已安装 Chocolatey

```cmd
# 以管理员身份运行 PowerShell
choco install ffmpeg
```

### 如果已安装 Scoop

```cmd
scoop install ffmpeg
```

### 如果都没安装，可以快速安装 Scoop

```powershell
# 在 PowerShell 中运行
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
irm get.scoop.sh | iex
scoop install ffmpeg
```

---

## 🔍 方案3：国内网盘/镜像

### 搜索关键词

在以下平台搜索：
- **百度网盘**：搜索 "ffmpeg windows 64位"
- **蓝奏云**：搜索 "ffmpeg"
- **123云盘**：搜索 "ffmpeg"

### 推荐下载

- 搜索 "ffmpeg 5.1 windows" 或 "ffmpeg 6.0 windows"
- 选择包含 `bin` 目录的完整版本

---

## ⚡ 方案4：使用下载工具

### 使用 IDM（Internet Download Manager）

1. 安装 IDM
2. 复制官方下载链接
3. IDM 会自动接管下载，速度更快

### 使用 Aria2

```cmd
# 安装 aria2 后
aria2c -x 16 -s 16 https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip
```

---

## 🎯 推荐流程（最快）

1. **先尝试 GitHub Releases**：
   - https://github.com/BtbN/FFmpeg-Builds/releases
   - 如果慢，用 GitHub Proxy 加速

2. **如果还是慢，使用包管理器**：
   - 安装 Scoop（1分钟）
   - `scoop install ffmpeg`（自动下载安装）

3. **最后选择：国内网盘**
   - 搜索下载，手动解压配置

---

## ✅ 验证安装

下载解压后，验证：

```cmd
# 如果添加到PATH
ffmpeg -version

# 如果使用完整路径
C:\ffmpeg\bin\ffmpeg.exe -version
```

---

## 💡 小贴士

- **GitHub Releases 通常比官方源快**，因为CDN更好
- **使用下载工具**（如IDM）可以多线程下载，速度更快
- **包管理器最省事**，一条命令自动安装配置







