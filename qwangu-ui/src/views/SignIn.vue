<template>
    <div class="container d-flex align-items-center justify-content-center vh-100">
        <div class="col-sm-6 col-lg-4">
            <div class="card m-auto">
                <div class="card-body">
                    <div class="d-flex justify-content-center my-4">
                        <h3 class="mb-0">Sign In</h3>
                    </div>
                    <form @submit.prevent="signIn" class="needs-validation" novalidate>
                        <div class="form-group has-validation mb-3">
                            <label for="username">Username</label>
                            <input type="text" class="form-control" id="username" v-model="request!.username"
                                placeholder="Enter username" :class="{'is-invalid': !isEmailValid && submitted}" required/>
                            <div class="invalid-feedback">
                                Please enter a username.
                            </div>
                        </div>
                        <div class="form-group mb-3">
                            <label for="password">Password</label>
                            <input type="password" class="form-control" id="password" v-model="request!.password"
                                placeholder="Enter password" :class="{'is-invalid': !isPasswordValid && submitted}" required/>
                            <div class="invalid-feedback">
                                Please enter a password.
                            </div>
                        </div>
                        <div class="d-flex flex-row-reverse">
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
import { defineComponent, Ref, ref, computed } from "vue";
import { useRouter } from "vue-router";
import { SignInRequest } from "../types/SignIn";
import { signInUser } from "../api/user-api";
import { AxiosError } from 'axios';
import { Response } from '../types/Response';

export default defineComponent({
    name: "SignIn",
    setup() {
        const request: Ref<SignInRequest> = ref(new SignInRequest());
        const submitted: Ref<boolean> = ref(false);
        const router = useRouter();

        const signIn = async () => {
            submitted.value = true;
            if (isEmailValid.value && isPasswordValid.value) {
                try {
                    const response = await signInUser(request.value);
                    if (response) {
                        localStorage.setItem('token', response.token);
                        router?.push(`/users`);
                    }
                } catch (error) {                    
                    if(error instanceof AxiosError) {
                        console.log((error.response?.data as Response<any>).message)
                    } else {
                        console.error(error);
                    }
                }
            }
        }

        const isEmailValid = computed(() => {
            const pattern = /\S+@\S+\.\S+/;
            const result = pattern.test(request.value.username!);
            return result;
        });

        const isPasswordValid = computed(() => {
            const result = request.value.password != null && request.value.password != undefined;
            return result;
        });

        return {
            request,
            submitted,
            isEmailValid,
            isPasswordValid,
            signIn
        };
    }
});
</script>