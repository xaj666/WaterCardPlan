<<<<<<< HEAD
import Vue from 'vue';// 导入Vue框架
import ElementUI from 'element-ui'; // 导入Element UI组件库
import 'element-ui/lib/theme-chalk/index.css';// 导入Element UI样式
import App from './App.vue';// 导入根组件
import router from './router'; // 导入路由实例


Vue.use(ElementUI);
=======
import Vue from 'vue'
import App from './App.vue'
import './plugins/element.js'

Vue.config.productionTip = false
>>>>>>> parent of 051b4d8 (完成大致UI 添加用户UI 删除UI)

new Vue({
  render: h => h(App),
}).$mount('#app')
