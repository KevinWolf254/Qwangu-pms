import { NavigationGuardNext, RouteLocationNormalized, Router } from 'vue-router';
import { useJwt } from '@vueuse/integrations/useJwt';

const beforeEachGuard = (to: RouteLocationNormalized, _: RouteLocationNormalized, next: NavigationGuardNext) => {
    const authority = to.meta.authority as string | null;
    if (authority) {
        const token: string | null = localStorage.getItem('token') ;
        if (token) {
            try {
                const { payload } = useJwt(token) as any;
                const authorities: Array<string> = payload.value?.authorities;

                if (authorities && authorities.includes(authority)) {
                    next();
                } else {
                    next('/sign-in');
                }
            } catch (error) {
                next('/sign-in');
            }
        } else {
            next('/sign-in');
        }
    } else {
        // Route doesn't require authentication, allow navigation
        next();
    }

};

export default function setupRouterGuard(router: Router): void {
    router.beforeEach(beforeEachGuard);
}
