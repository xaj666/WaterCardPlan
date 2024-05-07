import Vue from 'vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import App from './App.vue';
import router from './router'; // 导入路由实例

Vue.use(ElementUI);

new Vue({
  el: '#app',
  router,
  render: h => h(App)
});
