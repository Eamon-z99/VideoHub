# VideoHub - 视频分享平台

一个基于 Vue 3 + Vite 构建的现代化视频分享平台，提供视频浏览、播放、用户管理等功能。

## 🚀 项目特性

- **现代化技术栈**: Vue 3 + Vite + Pinia + Vue Router
- **响应式设计**: 适配多种屏幕尺寸，提供良好的用户体验
- **组件化开发**: 高度模块化的组件设计，便于维护和扩展
- **状态管理**: 使用 Pinia 进行全局状态管理
- **路由管理**: Vue Router 4 实现单页面应用路由
- **UI 组件**: 集成 Element Plus 组件库
- **样式预处理**: 使用 SCSS 进行样式开发
- **HTTP 请求**: Axios 封装，支持请求/响应拦截器
- **用户认证**: 完整的登录/注册系统，支持 Token 认证

## 📁 项目结构

```
VideoHub_web/
├── public/                     # 静态资源目录
│   ├── assets/                # 图片资源
│   │   ├── channel/           # 频道相关图标
│   │   ├── *.png             # 功能图标
│   │   └── header.png        # 头部背景图
│   └── favicon.ico           # 网站图标
├── src/                       # 源代码目录
│   ├── components/           # 公共组件
│   │   ├── Login.vue         # 登录组件
│   │   └── VideoCard.vue     # 视频卡片组件
│   ├── views/                # 页面组件
│   │   ├── HomeView/         # 首页
│   │   │   ├── home.vue      # 主首页
│   │   │   └── HomeView.vue  # 备用首页
│   │   ├── VideoStart/       # 视频播放页
│   │   ├── FeedHome/         # 动态首页
│   │   ├── History/          # 历史记录页
│   │   └── UserProfile/      # 用户资料页
│   ├── router/               # 路由配置
│   │   └── index.js          # 路由定义
│   ├── stores/               # 状态管理
│   │   ├── user.js           # 用户状态
│   │   └── counter.js        # 计数器状态
│   ├── utils/                # 工具函数
│   │   └── request.js        # HTTP 请求封装
│   ├── assets/               # 静态资源
│   │   ├── base.css          # 基础样式
│   │   ├── main.css          # 主样式
│   │   └── logo.svg          # Logo
│   ├── App.vue               # 根组件
│   └── main.js               # 入口文件
├── package.json              # 项目配置
├── vite.config.js            # Vite 配置
└── README.md                 # 项目说明
```

## 🛠️ 技术栈

### 核心框架
- **Vue 3.5.13** - 渐进式 JavaScript 框架
- **Vite 6.1.0** - 下一代前端构建工具
- **Vue Router 4.5.0** - 官方路由管理器

### 状态管理
- **Pinia 3.0.1** - Vue 官方状态管理库
- **Vuex 4.1.0** - 传统状态管理（备用）

### UI 组件库
- **Element Plus 2.10.7** - Vue 3 组件库
- **@element-plus/icons-vue 2.3.2** - Element Plus 图标

### 样式处理
- **Sass 1.90.0** - CSS 预处理器

### HTTP 请求
- **Axios 1.8.4** - Promise 基础的 HTTP 客户端

### 开发工具
- **@vitejs/plugin-vue 5.2.1** - Vue 单文件组件支持
- **vite-plugin-vue-devtools 7.7.2** - Vue 开发工具

## 🚀 快速开始

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖
```bash
npm install
```

### 开发环境运行
```bash
npm run dev
```
项目将在 `http://localhost:5173` 启动

### 生产环境构建
```bash
npm run build
```

### 预览生产构建
```bash
npm run preview
```

## 📱 功能模块

### 🏠 首页 (HomeView)
- **导航栏**: 包含首页、番剧、直播、游戏中心等导航
- **搜索功能**: 支持视频内容搜索
- **用户操作**: 登录、消息、动态、收藏、历史等
- **快速入口**: 动态、热门按钮
- **分类标签**: 番剧、国创、综艺、动画等分类
- **功能区域**: 专栏、活动、社区中心、直播、课堂、新歌热榜
- **轮播图**: 推荐内容展示
- **视频推荐**: 个性化视频推荐
- **视频列表**: 正在热播视频展示

### 🎬 视频播放页 (VideoStart)
- **视频播放器**: 支持视频播放控制
- **视频信息**: 标题、播放量、弹幕数等
- **互动功能**: 点赞、收藏、分享
- **标签系统**: 视频分类标签
- **评论区**: 用户评论和互动
- **推荐视频**: 相关视频推荐

### 📝 动态首页 (FeedHome)
- **用户卡片**: 用户信息和统计数据
- **动态发布**: 内容创作和发布
- **动态流**: 关注用户的动态展示
- **分类筛选**: 全部动态、视频投稿、追番追剧等

### 📚 历史记录 (History)
- **时间线**: 按时间分组的历史记录
- **记录控制**: 开启/关闭浏览历史记录
- **分类筛选**: 综合、视频、直播等分类
- **批量操作**: 清空历史记录功能

### 👤 用户资料 (UserProfile)
- **个人资料**: 头像、昵称、等级等
- **统计数据**: 关注、粉丝、获赞、播放量
- **内容管理**: 视频、专栏、收藏等
- **社交功能**: 关注、发消息等

### 🔐 用户认证 (Login)
- **登录功能**: 账号密码登录
- **注册功能**: 新用户注册
- **Token 管理**: 自动登录状态维护
- **安全验证**: 用户协议和隐私政策

## 🔧 配置说明

### 开发环境配置
项目使用 Vite 作为构建工具，配置文件为 `vite.config.js`：

```javascript
// 开发服务器配置
server: {
  host: '0.0.0.0',        // 允许局域网访问
  port: 5173,             // 开发服务器端口
  open: true,             // 自动打开浏览器
  cors: true,             // 启用 CORS
  proxy: {                // API 代理配置
    '/api': {
      target: 'http://localhost:8080',  // 后端服务器地址
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

### API 配置
HTTP 请求配置在 `src/utils/request.js`：

```javascript
// 基础配置
baseURL: 'http://localhost:8080'

// 请求拦截器：自动添加 Token
// 响应拦截器：处理 401 未授权
```

## 🎨 样式规范

### SCSS 结构
项目使用 SCSS 进行样式开发，采用树形嵌套结构：

```scss
.component {
  // 组件根样式
  
  .sub-component {
    // 子组件样式
    
    &:hover {
      // 伪类样式
    }
  }
}
```

### 响应式设计
- 支持多种屏幕尺寸
- 使用 CSS Grid 和 Flexbox 布局
- 移动端适配优化

## 🔒 安全特性

- **Token 认证**: JWT Token 用户认证
- **请求拦截**: 自动添加认证头
- **响应拦截**: 处理未授权状态
- **本地存储**: 安全的用户信息存储

## 📦 部署说明

### 构建生产版本
```bash
npm run build
```

### 部署到服务器
1. 将 `dist` 目录上传到服务器
2. 配置 Web 服务器（Nginx/Apache）
3. 配置后端 API 地址
4. 确保 HTTPS 配置（生产环境推荐）

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 项目 Issues: [GitHub Issues](https://github.com/your-username/VideoHub/issues)
- 邮箱: your-email@example.com

## 🙏 致谢

感谢以下开源项目的支持：
- [Vue.js](https://vuejs.org/)
- [Vite](https://vitejs.dev/)
- [Element Plus](https://element-plus.org/)
- [Pinia](https://pinia.vuejs.org/)

---

**注意**: 这是一个学习项目，仅供学习和参考使用。在生产环境中使用前，请确保进行充分的安全测试和性能优化。