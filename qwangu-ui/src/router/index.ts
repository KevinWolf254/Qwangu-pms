import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import SignIn from '../views/SignIn.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
import SetPassword from '../views/SetPassword.vue'
import Home from '../views/Home.vue'
import Users from "../views/users/Users.vue";
import Properties from '../views/properties/Properties.vue'

const routes: Array<RouteRecordRaw> = [
    {
      path: '/sign-in',
      name: 'SignIn',
      component: SignIn,
    },
    {
      path: '/forgot-password',
      name: 'ForgotPassword',
      component: ForgotPassword,
    },
    {
      path: '/set-password',
      name: 'SetPassword',
      component: SetPassword,
    },
    {
      path: '/',
      name: 'Home',
      component: Home,
      children: [
        {
          path: '/users',
          name: 'Users',
          component: Users,
        },
        {
          path: '/properties',
          name: 'Properties',
          component: Properties,
        }      
      ]
    },
    // catch-all route for unknown paths
    {
      path: '/:pathMatch(.*)*',
      redirect: '/sign-in',
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
    linkActiveClass: 'active'
  });
  
  export default router;