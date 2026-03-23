# VideoHub 管理端（VideoAdmin）

基于 **Vue 3 + TypeScript + Vite + Element Plus + Pinia** 的独立管理后台，对接后端：

- `POST /api/admin/auth/login`（**独立管理员账号**，与 `users` 无关）
- `GET /api/admin/me`（校验管理端 JWT）
- `GET /api/admin/video-submissions/pending` / `approved-unpublished`
- 审核、拒绝、发布、`publish-due` 等接口

## 环境

- Node `^20.19.0 || >=22.12.0`
- 后端 Spring Boot 默认 `http://localhost:8080`

## 开发

```bash
cd VideoAdmin
npm install
npm run dev
```

浏览器访问：**http://localhost:5175**（在 `vite.config.ts` 中配置）

开发时通过 Vite 代理：`/api`、`/local-videos`、`/avatars` → `localhost:8080`。

## 管理员账号

后台账号在 **`admins`** 表中维护（**独立** `account` + BCrypt `password`，**不关联** `users`）。

**建表：** 执行 `scripts/admins_table.sql` 或 `scripts/videohubc_supplement_admins.sql`（若曾用过旧版 `admins(user_id)`，需先 `DROP TABLE admins` 再执行）。

**新增管理员：** 插入 `account`、`password`（BCrypt）、`display_name` 等；禁用：`UPDATE admins SET status = 0 WHERE account = ?;`

## 生产构建

```bash
npm run build
```

若前后端不同域，在项目根目录创建 `.env.production`：

```env
VITE_API_BASE_URL=https://你的后端域名
```

并确保后端 CORS 允许管理端来源。

## 脚本说明

| 命令            | 说明           |
| --------------- | -------------- |
| `npm run dev`   | 开发热更新     |
| `npm run build` | 类型检查 + 构建 |
| `npm run lint`  | 代码检查       |
