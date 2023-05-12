import { createApp } from 'vue'
import './style.css'
import App from './App.vue';
import router from './router';
import 'bootstrap-icons/font/bootstrap-icons.css';
import PaginationVue from './components/Pagination.vue';
;

createApp(App)
.component('Pagination', PaginationVue)
.use(router)
.mount('#app')
