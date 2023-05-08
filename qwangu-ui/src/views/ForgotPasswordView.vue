<template>
    <div class="container d-flex align-items-center justify-content-center vh-100">
      <div class="col-sm-6 col-lg-4">
        <div class="card m-auto">
          <div class="card-body">
            <div class="d-flex justify-content-center my-4">
              <h3 class="mb-0">Forgot Password</h3>
            </div>
            <form @submit.prevent="resetPassword">
              <div class="form-group mb-3">
                <label for="email">Email</label>
                <input type="email" class="form-control" id="email" v-model="email"
                  placeholder="Enter your email" />
              </div>
              <div class="d-flex justify-content-between">
                <div class="text-sm">
                  <router-link to="/">
                    Back to Sign In
                  </router-link>
                </div>
              </div>
              <div class="d-flex justify-content-center my-4">
                <button type="submit" class="btn btn-outline-secondary btn-block" style="width: 15rem;">
                  Reset Password
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script lang="ts">
  export default {
    data() {
      return {
        email: '',
      };
    },
    methods: {
      resetPassword() {
        // implement password reset logic here
      },
    },
  };
  </script>
  