import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('../views/Products.vue')
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/Cart.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/custom/:orderId',
    name: 'Custom',
    component: () => import('../views/Custom.vue')
  },
  {
    path: '/trace/:orderId',
    name: 'Trace',
    component: () => import('../views/Trace.vue')
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue')
  },
  {
    path: '/admin/products',
    name: 'AdminProducts',
    component: () => import('../views/AdminProducts.vue')
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: () => import('../views/AdminOrders.vue')
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('../views/AdminUsers.vue')
  },
  {
    path: '/admin/logistics/pack',
    name: 'AdminLogisticsPack',
    component: () => import('../views/AdminLogisticsPack.vue')
  },
  {
    path: '/admin/logistics/workshop',
    name: 'AdminLogisticsWorkshop',
    component: () => import('../views/AdminLogisticsWorkshop.vue')
  },
  {
    path: '/admin/logistics/production',
    name: 'AdminLogisticsProduction',
    component: () => import('../views/AdminLogisticsProduction.vue')
  },
  {
    path: '/admin/logistics/shipment',
    name: 'AdminLogisticsShipment',
    component: () => import('../views/AdminLogisticsShipment.vue')
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('../views/Messages.vue')
  },
  {
    path: '/admin/console',
    name: 'AdminConsole',
    component: () => import('../views/AdminConsole.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  const requiresAuth = ['Custom', 'Trace', 'Orders', 'Profile', 'AdminProducts', 'AdminOrders', 'AdminUsers', 'AdminLogisticsPack', 'AdminLogisticsWorkshop', 'AdminLogisticsProduction', 'AdminLogisticsShipment', 'Messages', 'AdminConsole']
  const adminRoutes = ['AdminProducts', 'AdminOrders', 'AdminUsers', 'AdminLogisticsPack', 'AdminLogisticsWorkshop', 'AdminLogisticsProduction', 'AdminLogisticsShipment', 'AdminConsole']
  
  // 1. 需要登录但未登录 -> 跳转登录页
  if (requiresAuth.includes(to.name) && !token) {
    next({ name: 'Login' })
    return
  }

  // 2. 访问管理员页面
  if (adminRoutes.includes(to.name)) {
    try {
      const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')
      if (userInfo.role !== 'ADMIN') {
        // 权限不足 -> 跳转首页
        next({ name: 'Home' })
        return
      }
      // 权限验证通过 -> 放行
      next()
      return
    } catch (e) {
      // 数据异常 -> 跳转首页
      next({ name: 'Home' })
      return
    }
  }

  // 3. 其他情况 -> 放行
  next()
})

export default router
