import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Login from '../views/Login.vue'
import Hall from '../views/Hall.vue'
import Orders from '../views/Orders.vue'
import OrderDetail from '../views/OrderDetail.vue'
import Profile from '../views/Profile.vue'
import Income from '../views/Income.vue'
import Announcements from '../views/Announcements.vue'
import Complaints from '../views/Complaints.vue'
const router=createRouter({history:createWebHistory('/'),routes:[{path:'/login',component:Login},{path:'/',component:Hall},{path:'/orders',component:Orders},{path:'/orders/:id',component:OrderDetail},{path:'/profile',component:Profile},{path:'/income',component:Income},{path:'/announcements',component:Announcements},{path:'/complaints',component:Complaints}]})
router.beforeEach(to=>{const a=useAuthStore(); if(to.path!=='/login'&&!a.token)return'/login'})
export default router
