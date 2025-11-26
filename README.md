# VideoHub 项目

VideoHub 是一个视频平台项目，采用微前端架构，前后端分离。

## 📁 项目结构

```
VideoHub/
├── frontend/                    # 前端项目目录
│   ├── main-app/               # 主应用（qiankun 容器应用）
│   └── VideoHub_web/           # 子应用（视频中心）
│
├── backend/                     # 后端项目目录
│   └── VideoBackend/           # Spring Boot 后端服务
│
├── 微前端重构说明.md            # 微前端详细说明
├── 启动说明.md                  # 启动指南
└── 重构完成总结.md              # 重构总结
```

## 🚀 快速开始

### 前端启动

#### 1. 启动子应用（VideoHub_web）

打开第一个终端：

```bash
cd frontend/VideoHub_web
npm install
npm run dev
```

子应用将在 `http://localhost:5173` 启动

#### 2. 启动主应用（main-app）

打开第二个终端：

```bash
cd frontend/main-app
npm install
npm run dev
```

主应用将在 `http://localhost:8080` 启动

#### 3. 访问应用

在浏览器中访问：`http://localhost:8080/videohub`

### 后端启动

```bash
cd backend/VideoBackend
# 使用 Maven 启动
mvn spring-boot:run

# 或使用 Maven Wrapper
./mvnw spring-boot:run  # Linux/Mac
.\mvnw.cmd spring-boot:run  # Windows
```

后端服务将在 `http://localhost:8080` 启动（注意：如果与前端主应用端口冲突，需要修改后端端口）

## 📝 详细文档

- [微前端重构说明.md](./微前端重构说明.md) - 微前端架构详细说明
- [启动说明.md](./启动说明.md) - 详细启动指南
- [重构完成总结.md](./重构完成总结.md) - 重构完成总结

## 🛠️ 技术栈

### 前端
- **主应用**: Vue 3 + Vite + qiankun
- **子应用**: Vue 3 + Vite + Element Plus + Pinia

### 后端
- Spring Boot
- Maven

## 📌 注意事项

1. **端口配置**:
   - 子应用：5173
   - 主应用：8080
   - 后端：8080（如果冲突，请修改后端端口）

2. **开发环境**:
   - 前端需要同时启动主应用和子应用
   - 后端需要单独启动

3. **生产环境**:
   - 需要分别构建和部署前端主应用、子应用和后端服务
   - 更新主应用中的子应用 entry 地址为生产环境地址
