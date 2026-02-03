import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue()
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 新增服务器配置（解决跨域和代理问题）
  server: {
    host: '0.0.0.0', // 允许局域网访问
    port: 5174,      // 默认端口（可修改）
    open: false,     // 作为子应用时不需要自动打开
    cors: true,      // 默认启用CORS（开发环境）
    headers: {
      'Access-Control-Allow-Origin': '*', // 允许跨域，qiankun 需要
    },
    // 关键：配置代理（解决API跨域问题）
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端服务器地址
        changeOrigin: true,              // 允许跨域
        secure: false,                   // 如果是https，需要关闭证书验证
      },
      '/local-videos': {
        target: 'http://localhost:8080', // 后端服务器地址
        changeOrigin: true,
        secure: false,
      },
      '/avatars': {
        target: 'http://localhost:8080', // 后端服务器地址
        changeOrigin: true,
        secure: false,
        // 不重写路径，直接转发 /avatars/... 到后端
      }
    }
  },
  // 可选：生产环境构建配置
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false, // 关闭sourcemap以加快构建速度
  },
  // 配置 base，支持作为子应用部署
  base: process.env.NODE_ENV === 'production' ? '/videohub/' : '/'
})