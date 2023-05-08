import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import SignIn from '../views/SignInView.vue'
import ForgotPassword from '../views/ForgotPasswordView.vue'
import SetPassword from '../views/SetPasswordView.vue'
import Home from '../views/HomeView.vue'

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
  });
  
  export default router;