import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { registerApplication, start } from 'single-spa'
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

// single-spa 注册子应用（使用动态 import 加载子应用生命周期）
const loadVideoHubApp = () =>
  import(/* @vite-ignore */ 'http://localhost:5173/src/main.js')

registerApplication({
  name: 'videohub-app',
  app: loadVideoHubApp,
  activeWhen: (location) => location.pathname.startsWith('/videohub'),
  customProps: {
    routerBase: '/videohub',
    // 将容器节点传给子应用，便于它选择挂载点
    get container() {
      return document.querySelector('#subapp-container')
    }
  }
})

// 路由就绪后跳到子应用首页并启动 single-spa
router.isReady().then(() => {
  router.replace('/videohub')
  start()
})

