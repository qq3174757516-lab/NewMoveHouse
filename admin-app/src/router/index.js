import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Users from '../views/Users.vue'
import Drivers from '../views/Drivers.vue'
import Orders from '../views/Orders.vue'
import Pricing from '../views/Pricing.vue'
import VehicleTypes from '../views/VehicleTypes.vue'
import Reviews from '../views/Reviews.vue'
import Announcements from '../views/Announcements.vue'
import Complaints from '../views/Complaints.vue'
const router=createRouter({history:createWebHistory('/'),routes:[{path:'/login',component:Login},{path:'/',component:Dashboard},{path:'/users',component:Users},{path:'/drivers',component:Drivers},{path:'/orders',component:Orders},{path:'/pricing',component:Pricing},{path:'/vehicle-types',component:VehicleTypes},{path:'/admin/vehicle-types',component:VehicleTypes},{path:'/reviews',component:Reviews},{path:'/announcements',component:Announcements},{path:'/complaints',component:Complaints}]})
router.beforeEach(to=>{const a=useAuthStore(); if(to.path!=='/login'&&!a.token)return'/login'})
export default router
