import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 新增服务器配置（解决跨域和代理问题）
  server: {
    host: '0.0.0.0', // 允许局域网访问
    port: 5173,      // 默认端口（可修改）
    open: true,      // 启动时自动打开浏览器
    cors: true,      // 默认启用CORS（开发环境）
    // 关键：配置代理（解决API跨域问题）
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端服务器地址
        changeOrigin: true,              // 允许跨域
        rewrite: (path) => path.replace(/^\/api/, ''), // 重写路径（可选）
        secure: false,                   // 如果是https，需要关闭证书验证
      }
    }
  },
  // 可选：生产环境构建配置
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false // 关闭sourcemap以加快构建速度
  }
})