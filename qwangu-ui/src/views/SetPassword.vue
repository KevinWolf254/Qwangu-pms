<template>
    <div class="container d-flex align-items-center justify-content-center vh-100">
        <div class="col-sm-6 col-lg-4">
            <div class="card m-auto">
                <div class="card-body">
                    <div class="d-flex justify-content-center my-4">
                        <h3 class="mb-0">Set Password</h3>
                    </div>
                    <form @submit.prevent="setPassword">
                        <div class="form-group mb-3">
                            <label for="new-password">New Password</label>
                            <input type="password" class="form-control" id="new-password" v-model="newPassword"
                                placeholder="Enter new password" />
                        </div>
                        <div class="form-group mb-3">
                            <label for="confirm-password">Confirm Password</label>
                            <input type="password" class="form-control" id="confirm-password" v-model="confirmPassword"
                                placeholder="Confirm new password" />
                        </div>
                        <div class="d-flex justify-content-center my-4">
                            <button type="submit" class="btn btn-outline-secondary btn-block" style="width: 15rem;">
                                Set Password
                            </button>
                        </div>
                    </form>
                    <div class="d-flex justify-content-center">
                        <router-link to="/sign-in">Back to Sign In</router-link>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
  
<script lang="ts">
import { defineComponent, Ref, ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";

export default defineComponent({
    setup() {
        const newPassword: Ref<string> = ref('');
        const confirmPassword: Ref<string> = ref('');
        const router = useRouter();
        const route = useRoute();

        const checkTokenValidity = (token: string) => {
            console.log(token);
        }

        const setPassword = () => {
            console.log({
                password: newPassword,
                confirmPassword: confirmPassword
            });
        }

        onMounted(() => {
            const token = route.query.token as string;
            if (!token) {
                router.push({ path: '/sign-in' });
            } else {
                // continue to load the page
                // perform validation checks on the token here
                checkTokenValidity(token);
            }

        })

        return {
            newPassword,
            confirmPassword,
            checkTokenValidity,
            setPassword

        };
    }
});
</script>
  
<style>
/* Additional styling here */
</style>
  