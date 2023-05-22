import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import SignIn from '../views/SignIn.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
import SetPassword from '../views/SetPassword.vue'
import Home from '../views/Home.vue'
import Users from "../views/users/Users.vue";
import Properties from '../views/properties/Properties.vue'
import Units from "../views/units/Units.vue";
import Unit from '../views/units/Unit.vue'
import Tenants from '../views/tenants/Tenants.vue'
import Tenant from '../views/tenants/Tenant.vue'

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
        },
        {
          path: '/units',
          children: [
            {
              path: '',
              name: 'Units',
              component: Units,
              meta: { authority: 'UNIT_READ' }
            },
            {
              path: ':id',
              name: 'Unit',
              component: Unit,
              meta: { authority: 'UNIT_READ' }
            },
          ]
        },
        {
          path: '/tenants',
          children: [
            {
              path: '',
              name: 'Tenants',
              component: Tenants,
              meta: { authority: 'TENANT_READ' }
            },
            {
              path: ':id',
              name: 'Tenant',
              component: Tenant,
              meta: { authority: 'TENANT_READ' }
            },
          ]
        },
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