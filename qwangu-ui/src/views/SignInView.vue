<template>
    <div class="container d-flex align-items-center justify-content-center vh-100">
        <div class="col-sm-6 col-lg-4">
            <div class="card m-auto">
                <div class="card-body">
                    <div class="d-flex justify-content-center my-4">
                        <h3 class="mb-0">Sign In</h3>
                    </div>
                    <form @submit.prevent="signIn">
                        <div class="form-group mb-3">
                            <label for="username">Username</label>
                            <input type="text" class="form-control" id="username" v-model="username"
                                placeholder="Enter username" />
                        </div>
                        <div class="form-group mb-3">
                            <label for="password">Password</label>
                            <input type="password" class="form-control" id="password" v-model="password"
                                placeholder="Enter password" />
                        </div>
                        <div class="d-flex justify-content-between">
                            <div class="form-group form-check">
                                <input type="checkbox" class="form-check-input" id="remember-me" v-model="rememberMe" />
                                <label class="form-check-label" for="remember-me">Remember me</label>
                            </div>
                            <div class="text-sm">
                                <router-link to="/forgot-password">
                                    Forgot your password?
                                </router-link>
                            </div>
                        </div>
                        <div class="d-flex justify-content-center my-4">
                            <button type="submit" class="btn btn-outline-secondary btn-block" style="width: 15rem;">
                                Sign In
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</template>
  
<style>
body {
    background-color: #f8f9fa;
}

.card {
    border-radius: 1.5rem;
}
</style>
  
<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
    name: "SignIn",
    data() {
        return {
            username: "",
            password: "",
            rememberMe: false,
        };
    },
    methods: {
        signIn() {
            // TODO: Implement login logic
            console.log({
                username: this.username,
                password: this.password,
                rememberMe: this.rememberMe,
            });
        },
    },
});
</script>