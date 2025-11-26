import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

let app = null;

/**
 * 渲染函数
 */
function render(props = {}) {
  const { container, routerBase } = props;
  // 如果是在 qiankun 环境中，使用容器内的 #app，否则使用 document 的 #app
  const mountElement = container ? container.querySelector('#app') : document.querySelector('#app');
  
  // 设置路由 base（如果从主应用传递过来）
  if (routerBase && window.__POWERED_BY_QIANKUN__) {
    window.__MICRO_APP_BASE_ROUTE__ = routerBase;
  }
  
  app = createApp(App);
  const pinia = createPinia();
  
  app.use(pinia);
  app.use(router);
  app.use(ElementPlus);
  
  // 注册 Element Plus 图标
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
  }
  
  app.mount(mountElement || '#app');
}

/**
 * 独立运行时
 */
if (!window.__POWERED_BY_QIANKUN__) {
  render();
}

/**
 * qiankun 生命周期函数
 */
export async function bootstrap() {
  console.log('[videohub-app] bootstraped');
}

export async function mount(props) {
  console.log('[videohub-app] props from main framework', props);
  render(props);
}

export async function unmount() {
  console.log('[videohub-app] unmount');
  if (app) {
    app.unmount();
    app = null;
  }
}