import { DirectiveBinding } from 'vue';
import { useJwt } from '@vueuse/integrations/useJwt';

function hasAuthority(role: string): boolean {  
  const token: string | null = localStorage.getItem('token') ;
  if (token) {
    try {
        const { payload } = useJwt(token) as any;
        const roles: Array<string> = payload.value?.authorities;
        if (roles && roles.includes(role)) {
            return true;
        } 
    } catch (error) {
        return false;
    }
  }
  return false;
}

export const hasAuthorityDirective = {
  mounted(el: HTMLElement, binding: DirectiveBinding): void {
    const role = binding.value;
    if (!hasAuthority(role)) {
      el.style.display = 'none'; // Hide the element
    }
  },
};
