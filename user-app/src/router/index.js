import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Orders from '../views/Orders.vue'
import OrderDetail from '../views/OrderDetail.vue'
import Payment from '../views/Payment.vue'
import Profile from '../views/Profile.vue'
import Announcements from '../views/Announcements.vue'
import Complaints from '../views/Complaints.vue'

const router = createRouter({
  history: createWebHistory('/'),
  routes: [
    { path: '/login', component: Login },
    { path: '/', component: Dashboard },
    { path: '/orders', component: Orders },
    { path: '/orders/:id', component: OrderDetail },
    { path: '/payment/:orderId', component: Payment },
    { path: '/profile', component: Profile },
    { path: '/announcements', component: Announcements },
    { path: '/complaints', component: Complaints }
  ]
})
router.beforeEach(to => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.token) return '/login'
})
export default router
