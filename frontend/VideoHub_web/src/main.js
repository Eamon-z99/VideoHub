import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import { createAppRouter } from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

let app = null;

/**
 * 渲染函数
 */
function render(props = {}) {
  const { container, routerBase } = props;
  // 有 routerBase 即视为微应用模式（single-spa 传入）
  const isMicroApp = !!routerBase;

  // 获取挂载点：优先容器内的 #app，其次容器本身，再退回 document
  const mountElement =
    (container && (container.querySelector('#app') || container.firstElementChild || container)) ||
    document.querySelector('#app');

  // 计算路由 base：微应用模式优先 props.routerBase，否则默认 /videohub；独立运行取 import.meta.env.BASE_URL
  const base = isMicroApp ? routerBase || '/videohub' : import.meta.env.BASE_URL;

  // 设置全局 base，供路由内部判断
  if (isMicroApp) {
    window.__MICRO_APP_BASE_ROUTE__ = base;
  }

  app = createApp(App);
  const pinia = createPinia();
  const router = createAppRouter(base);

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
if (!isMicroFrontendEnv()) {
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

// 兼容 single-spa：直接挂到全局，供 dynamic import 后使用
window['videohub-app'] = { bootstrap, mount, unmount };

function isMicroFrontendEnv() {
  // 兼容 qiankun 注入标记（若未来仍需），否则依据是否存在全局标记或 routerBase
  return Boolean(window.__POWERED_BY_QIANKUN__ || window.__MICRO_APP_BASE_ROUTE__);
}