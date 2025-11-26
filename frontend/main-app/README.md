# VideoHub 主应用

这是 VideoHub 微前端项目的主应用（容器应用），负责管理和加载各个子应用。

## 功能

- 使用 qiankun 框架管理微前端应用
- 提供统一的导航和布局
- 支持多个子应用的动态加载

## 开发

```bash
npm install
npm run dev
```

## 构建

```bash
npm run build
```

## 配置

主应用配置在 `src/main.js` 中，可以在这里注册新的子应用。

