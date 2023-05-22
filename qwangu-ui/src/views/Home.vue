<template>
    <!-- Side-Nav -->
    <div class="side-navbar bg-dark active-nav d-flex justify-content-between flex-wrap flex-column" id="sidebar"
        ref="sidebar">
        <ul class="nav flex-column text-white w-100">
            <div class="h3 text-white my-4">
                Qwangu PMS
            </div>
            <router-link to="/dashboard" :class="{'custom-nav-link': true}">
                <li class="nav-link">
                    <i class="bi bi-file-earmark-bar-graph-fill"></i>
                    <span class="mx-2">Dashboard</span>
                </li>
            </router-link>
            <router-link to="/users" :class="{'custom-nav-link': true}">
                <li class="nav-link">
                    <i class="bi bi-people-fill"></i>
                    <span class="mx-2">Users</span>
                </li>
            </router-link>
            <router-link to="/properties" :class="{'custom-nav-link': true}">
                <li class="nav-link">
                    <i class="bi bi-buildings"></i>
                    <span class="mx-2">Properties</span>
                </li>
            </router-link>
            <router-link to="/units" :class="{'custom-nav-link': true}" v-has-authority="'UNIT_READ'">
                <li class="nav-link">
                    <i class="bi bi-house-door-fill"></i>
                    <span class="mx-2">Units</span>
                </li>
            </router-link>
            <router-link to="/tenants" :class="{'custom-nav-link': true}">
                <li class="nav-link">
                    <i class="bi bi-file-earmark-person-fill"></i>
                    <span class="mx-2">Tenants</span>
                </li>
            </router-link>
            <router-link to="/billing" :class="{'custom-nav-link': true}">
                <li class="nav-link">
                    <i class="bi bi-receipt"></i>
                    <span class="mx-2">Billing</span>
                </li>
            </router-link>
        </ul>
    </div>

    <!-- Main Wrapper -->
    <div class="my-container active-cont" ref="container">
        <!-- Top Nav -->
        <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
            <div class="container-fluid">
                <a class="navbar-brand" href="#" id="menu-btn" @click="toggleSidebar" ref="menuBtn">
                    <i class="bi bi-list"></i>
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                    aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse justify-content-end" id="navbarNav">
                    <ul class="navbar-nav">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" id="profileDropdown" role="button"
                                data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-person"></i>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="profileDropdown">
                                <li><a class="dropdown-item" href="#">Profile</a></li>
                                <li><a class="dropdown-item" href="#" @click="logout" >Logout</a></li>
                            </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>
        <!--End Top Nav -->
        <RouterView></RouterView>
    </div>
</template>
  
<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useRouter } from 'vue-router';
import { hasAuthorityDirective } from '../directives/hasAuthorityDirective';

export default defineComponent({
    name: 'Home',
    directives: {
        'has-authority': hasAuthorityDirective,
    },
    setup() {
        const menuBtn = ref<HTMLButtonElement>();
        const sidebar = ref<HTMLDivElement>();
        const container = ref<HTMLDivElement>();
        const router = useRouter();

        const toggleSidebar = () => {
            sidebar.value?.classList.toggle('active-nav');
            container.value?.classList.toggle('active-cont');
        };

        const logout = () => {
            router?.push(`/sign-in`);
        };

        return {
            menuBtn,
            sidebar,
            container,
            toggleSidebar,
            logout
        };
    }
});
</script>

<style scoped>
.side-navbar {
    width: 200px;
    height: 100%;
    position: fixed;
    margin-left: -300px;
    transition: 0.5s;
}

.custom-nav-link.active .nav-link, 
.nav-link:active,
.nav-link:focus,
.nav-link:hover {
    background-color: #ffffff;
    color: black;
    font-weight: bolder;
    border-top-left-radius: 1rem;
    border-bottom-left-radius: 2rem;
    border-bottom-style: groove;
}

.my-container {
    transition: 0.4s;
}

.active-nav {
    margin-left: 0;
}

/* for main section */
.active-cont {
    margin-left: 200px;
}

/* .my-container input {
    border-radius: 2rem;
    padding: 2px 20px;
} */
.custom-nav-link, .nav-link {
    color: white;
    text-decoration: none;
}
/* .custom-nav-link.active {
    background-color: #ffffff;
    color: black;
    font-weight: bolder;
} */
</style>
  