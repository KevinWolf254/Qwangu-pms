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
export default {
    // Component logic here
    data() {
        return {
            newPassword: '',
            confirmPassword: '',
            // token: null
        }
    },
    async mounted() {
        const token = this.$route.query.token as string;
        if (!token) {
            this.$router.push({ path: '/sign-in' });
        } else {
            // continue to load the page
            // perform validation checks on the token here
            await this.checkTokenValidity(token);
        }
    },
    methods: {
        async checkTokenValidity(token: string) {
            console.log(token)
        },
        async setPassword() {
            console.log({
                password: this.newPassword,
                confirmPassword: this.confirmPassword
            })
        }
    }
};
</script>
  
<style>
/* Additional styling here */
</style>
  