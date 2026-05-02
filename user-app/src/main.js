import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles.css'
import App from './App.vue'
import router from './router'

const APP_TITLE = '用户端'
document.title = APP_TITLE
router.afterEach(() => {
  document.title = APP_TITLE
})

createApp(App).use(createPinia()).use(router).use(ElementPlus).mount('#app')
