import Vue from 'vue';
import VueRouter from 'vue-router';
import Home from '../views/HomePage.vue';
import Orders from '../views/OrdersPage.vue';

Vue.use(VueRouter);

const routes = [
  { 
    path: '/index', 
    component: Home,
    meta: { 
      title: '用户管理',
      icon: 'el-icon-user'
    }
  },
  { 
    path: '/orders', 
    component: Orders,
    meta: { 
      title: '订单管理',
      icon: 'el-icon-s-order'
    }
  },
  // 重定向
  {path: '/', redirect: '/index'}
];

const router = new VueRouter({
  routes
});

// 路由守卫，记录访问历史
router.beforeEach((to, from, next) => {
  // 可以在这里处理访问历史记录
  next()
})

export default router;
