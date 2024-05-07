import Vue from 'vue';
import VueRouter from 'vue-router';
import Home from '../views/HomePage.vue';
import About from '../views/AboutPage.vue';


Vue.use(VueRouter);

const routes = [
  { path: '/index', component: Home },
  { path: '/about', component: About },
  // 重定向
  {path: '/', redirect: '/index'}
];

const router = new VueRouter({
  routes
});

export default router;
