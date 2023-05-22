<template>
    <div class="container mt-3">
        <div class="row">
            <div class="col-12 d-flex justify-content-between">
                <h3>Tenant - {{ tenantId }}</h3>
            </div>
            <hr class="my-4">
            <div class="col-3">
                <img src="../../../public/tenant.jpg" class="img-thumbnail" alt="tenant profile">
            </div>
            <div class="col-9">
                <ul class="nav nav-tabs" id="myTab" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active" id="personalInfo-tab" data-bs-toggle="tab"
                            data-bs-target="#personalInfo" type="button" role="tab" aria-controls="personalInfo"
                            aria-selected="true">Personal Info.</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="occupationInfo-tab" data-bs-toggle="tab"
                            data-bs-target="#occupationInfo" type="button" role="tab" aria-controls="occupationInfo"
                            aria-selected="false">Occupation Info.</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="rentPayments-tab" data-bs-toggle="tab" data-bs-target="#rentPayments"
                            type="button" role="tab" aria-controls="rentPayments" aria-selected="false">Rent
                            Payments</button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="notifications-tab" data-bs-toggle="tab" data-bs-target="#notifications"
                            type="button" role="tab" aria-controls="notifications" aria-selected="false">Vacate
                            Notiifications</button>
                    </li>
                </ul>
                <div class="tab-content" id="myTabContent">
                    <div class="tab-pane fade show active" id="personalInfo" role="tabpanel"
                        aria-labelledby="personalInfo-tab">
                        <form @submit.prevent="editPersonalInfo(tenant)" class="needs-validation" novalidate>
                            <div class="container row g-3 mt-3">
                                <div class="col-12">
                                    <div class="form-group mb-3">
                                        <label for="surname">Surname</label>
                                        <input type="text" class="form-control" id="surname" v-model="tenant!.surname"
                                            placeholder="Enter middle name"
                                            :class="{ 'is-invalid': !isSurnameValid && submitted }" required />
                                        <div class="invalid-feedback">
                                            Please enter surname with a minimum of 2 characters.
                                        </div>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="form-group mb-3">
                                        <label for="firstName">First Name</label>
                                        <input type="text" class="form-control" id="firstName" v-model="tenant!.firstName"
                                            placeholder="Enter first name"
                                            :class="{ 'is-invalid': !isFirstNameValid && submitted }" required />
                                        <div class="invalid-feedback">
                                            Please enter first name with a minimum of 2 characters.
                                        </div>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="form-group mb-3">
                                        <label for="middleName">Middle Name</label>
                                        <input type="text" class="form-control" id="middleName" v-model="tenant!.middleName"
                                            placeholder="Enter middle name"
                                            :class="{ 'is-invalid': !isMiddleNameValid && submitted }" required />
                                        <div class="invalid-feedback">
                                            Please enter middle name with a minimum of 2 characters.
                                        </div>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <div class="form-group mb-3">
                                        <label for="emailAddress">Email Address</label>
                                        <input type="text" class="form-control" id="emailAddress"
                                            v-model="tenant!.emailAddress" placeholder="Enter email address"
                                            :class="{ 'is-invalid': !isEmailAddressValid && submitted }" required />
                                        <div class="invalid-feedback">
                                            Please enter a email address.
                                        </div>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <div class="form-group mb-3">
                                        <label for="mobileNumber">Mobile No.</label>
                                        <input type="text" class="form-control" id="mobileNumber"
                                            v-model="tenant!.mobileNumber" placeholder="Enter mobile no."
                                            :class="{ 'is-invalid': !isMobileNumberValid && submitted }" required />
                                        <div class="invalid-feedback">
                                            Please enter mobile number.
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-12 d-flex flex-row-reverse">
                                <button type="submit" class="btn btn-primary">Save changes</button>
                            </div>
                        </form>
                    </div>
                    <div class="tab-pane fade" id="occupationInfo" role="tabpanel" aria-labelledby="occupationInfo-tab">
                        <div class="container mt-4">
                            <div v-for="occupation in occupations" :key="occupation.id" class="card" style="width: 18rem;">
                                <img class="card-img-top" src="../../../house.jpg" alt="Card image cap">
                                <div class="card-body">
                                    <h5 class="card-title">{{ occupation.number }}</h5>
                                    <p class="card-text">Some quick example text to build on the card title and make up the
                                        bulk of the card's content.</p>
                                    <a href="#" class="btn btn-primary">Go somewhere</a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="tab-pane fade" id="rentPayments" role="tabpanel" aria-labelledby="rentPayments-tab">...
                    </div>
                </div>

            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, Ref, ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router'
import { Tenant } from '../../types/Tenant';
import { Occupation } from '../../types/Occupation';
import { getTenant, updateTenant } from '../../api/tenant-api';
import { AxiosError } from 'axios';
import { Response } from '../../types/Response';

export default defineComponent({
    setup() {
        const route = useRoute();
        const submitted: Ref<boolean> = ref(false);
        const occupations: Ref<Occupation[]> = ref([]);

        let tenantId: Ref<string> = ref("");
        let tenant: Ref<Tenant> = ref(new Tenant());

        onMounted(async () => {
            tenantId.value = route.params.id as string;
            try {
                tenant.value = await getTenant(tenantId.value);
            } catch (error) {
                if (error instanceof AxiosError) {
                    console.log((error.response?.data as Response<any>).message)
                } else {
                    console.error(error);
                }
            }
        });

        const editPersonalInfo = async (tenant: Tenant) => {
            submitted.value = true;
            if (isFirstNameValid.value && isMiddleNameValid.value && isSurnameValid.value
                && isEmailAddressValid && isMobileNumberValid) {
                try {
                    tenant = await updateTenant(tenant.id!, tenant);
                } catch (error) {
                    if (error instanceof AxiosError) {
                        console.log((error.response?.data as Response<any>).message)
                    } else {
                        console.error(error);
                    }
                }
            }
        };

        const isFirstNameValid = computed((): boolean => {
            const name = tenant.value.firstName;
            if (name) {
                if (name.length < 2)
                    return false;
                return true;
            }
            return false;
        });

        const isMiddleNameValid = computed((): boolean => {
            const name = tenant.value.middleName;
            if (name) {
                if (name.length < 2)
                    return false;
                return true;
            }
            return true;
        });

        const isSurnameValid = computed((): boolean => {
            const name = tenant.value.surname;
            if (name) {
                if (name.length < 2)
                    return false;
                return true;
            }
            return false;
        });

        const isEmailAddressValid = computed((): boolean => {
            const pattern = /\S+@\S+\.\S+/;
            const result = pattern.test(tenant.value.emailAddress!);
            return result;
        });

        const isMobileNumberValid = computed((): boolean => {
            const name = tenant.value.mobileNumber;
            if (name) {
                if (name.length < 8)
                    return false;
                return true;
            }
            return false;
        });

        return {
            tenantId,
            tenant,
            submitted,
            isMobileNumberValid,
            isEmailAddressValid,
            isSurnameValid,
            isMiddleNameValid,
            isFirstNameValid,
            occupations,
            editPersonalInfo
        }
    }
})
</script>
<style lang="">
    
</style>