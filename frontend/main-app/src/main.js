import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { registerMicroApps, start, setDefaultMountApp } from 'qiankun'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#main-app')

// 注册微应用
registerMicroApps([
  {
    name: 'videohub-app', // 子应用名称
    entry: '//localhost:5173', // 子应用地址（开发环境）
    container: '#subapp-container', // 子应用挂载的容器
    activeRule: '/videohub', // 激活路由规则
    props: {
      routerBase: '/videohub', // 传递给子应用的路由 base
    }
  }
], {
  // 全局生命周期钩子
  beforeLoad: (app) => {
    console.log('[主应用] before load', app.name)
    return Promise.resolve()
  },
  beforeMount: (app) => {
    console.log('[主应用] before mount', app.name)
    return Promise.resolve()
  },
  afterMount: (app) => {
    console.log('[主应用] after mount', app.name)
    return Promise.resolve()
  },
  beforeUnmount: (app) => {
    console.log('[主应用] before unmount', app.name)
    return Promise.resolve()
  },
  afterUnmount: (app) => {
    console.log('[主应用] after unmount', app.name)
    return Promise.resolve()
  }
})

// 设置默认进入的子应用
// setDefaultMountApp('/videohub')

// 启动 qiankun
start({
  sandbox: {
    strictStyleIsolation: true, // 开启样式隔离
    experimentalStyleIsolation: true
  },
  prefetch: true, // 预加载
  singular: false // 是否单实例模式
})

