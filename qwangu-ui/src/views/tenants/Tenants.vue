<template>
    <div class="container mt-3">
        <div class="row">
            <div class="col-12 d-flex justify-content-between">
                <h3>Tenants</h3>
                <button type="button" class="btn btn-primary" @click="openModal('CREATE', tenantModal)">
                    <i class="bi bi-person-add me-1" style="font-size: 1.1rem;"></i>
                    Create
                </button>
            </div>
            <hr class="my-4">
            <div class="col-12">
                <div class="container mt-4">
                    <div class="d-flex flex-row-reverse mt-4">
                        <div class="col-auto ms-2">
                            <button type="submit" class="btn btn-primary mb-3" @click="search(searchMobileNumber)">Search</button>
                        </div>
                        <div class="mb-2">
                            <input type="text" class="form-control" id="searchMobileNumber" placeholder="tenant's mobile no."
                                style="width: 25rem;" v-model="searchMobileNumber">
                        </div>
                    </div>
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th scope="col">Names</th>
                                <th scope="col">Mobile No.</th>
                                <th scope="col">Email Address</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="tenant in tenants" :key="tenant.id">
                                <td>{{ tenant.firstName }} {{ tenant.middleName }} {{ tenant.surname }}</td>
                                <td>{{ tenant.mobileNumber }}</td>
                                <td>{{ tenant.emailAddress }}</td>
                                <td>
                                    <button type="button" class="btn btn-outline-primary me-1" style="font-size: .75rem;"
                                        @click="goToTenant(tenant.id!)">
                                        <i class="bi bi-eye-fill"></i>
                                        View
                                    </button>
                                    <button type="button" class="btn btn-outline-secondary me-1" style="font-size: .75rem;"
                                        @click="openModal('EDIT', tenantModal, tenant)">
                                        <i class="bi bi-pencil-square"></i>
                                        Edit
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="mt-4 d-flex flex-row-reverse">
                        <Pagination :items="allTenants" @items-sliced="handleSlicedTenants"></Pagination>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!-- create property modal -->
    <div class="modal fade" ref="tenantModal" tabindex="-1" aria-labelledby="tenantModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="tenantModalLabel">{{ label }} Tenant</h5>
                    <button type="button" class="btn-close" @click="closeModal()" aria-label="Close"></button>
                </div>
                <form @submit.prevent="createOrUpdateTenant(label.toUpperCase())" class="needs-validation" novalidate>
                    <div class="modal-body">
                        <div class="row g-3">
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="surname">Surname</label>
                                    <input type="text" class="form-control" id="surname" v-model="selectedTenant!.surname"
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
                                    <input type="text" class="form-control" id="firstName" v-model="selectedTenant!.firstName"
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
                                    <input type="text" class="form-control" id="middleName" v-model="selectedTenant!.middleName"
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
                                    <input type="text" class="form-control" id="emailAddress" v-model="selectedTenant!.emailAddress"
                                        placeholder="Enter email address" :class="{'is-invalid': !isEmailAddressValid && submitted}" required/>
                                    <div class="invalid-feedback">
                                        Please enter a email address.
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="mobileNumber">Mobile No.</label>
                                    <input type="text" class="form-control" id="mobileNumber" v-model="selectedTenant!.mobileNumber"
                                        placeholder="Enter mobile no."
                                        :class="{ 'is-invalid': !isMobileNumberValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter mobile number.
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" @click="closeModal()">Close</button>
                        <button type="submit" class="btn btn-primary">Save changes</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent, Ref, ref, onMounted, computed } from 'vue';
import { Tenant } from "../../types/Tenant";
import * as bootstrap from 'bootstrap';
import { getTenants, createTenant, updateTenant } from '../../api/tenant-api';
import { AxiosError } from 'axios';
import { Response } from '../../types/Response';
import { useRouter } from "vue-router";

export default defineComponent({
    name: "Tenants",
    setup() {
        const tenantModal: Ref<HTMLDivElement | null> = ref(null);
        const submitted: Ref<boolean> = ref(false);
        const selectedTenant: Ref<Tenant> = ref(new Tenant());
        const searchMobileNumber: Ref<string> = ref("");
        const allTenants: Ref<Tenant[]> = ref([]);
        const tenants: Ref<Tenant[]> = ref([]);
        const router = useRouter();

        let label: Ref<string> = ref("");
        let modal: bootstrap.Modal;

        const openModal = (action: string, refModal: HTMLDivElement | null, tenant?: Tenant) => {
            if (action === "CREATE") {
                label.value = "Create";
                selectedTenant.value = new Tenant();
            }
            else if (action === "EDIT") {
                label.value = "Edit";
                selectedTenant.value = tenant!;
            }
            else if (action === "DELETE") {
                label.value = "Delete";
                selectedTenant.value = tenant!;
            }
            else {
                console.error("Unknownn command!");
            }
            modal = new bootstrap.Modal(refModal!);
            modal?.show();
        };

        const closeModal = () => {
            submitted.value = false;
            modal?.hide();
        };

        const goToTenant = (tenantId: string) => {
            router?.push(`/tenants/${tenantId}`);
        };

        const createOrUpdateTenant = async (action: string) => {
            submitted.value = true;
            if (isFirstNameValid.value && isMiddleNameValid.value && isSurnameValid.value
            && isEmailAddressValid && isMobileNumberValid) {
                try {
                    const tenant = selectedTenant.value;
                    if (action === "CREATE") {
                        await createTenant(tenant);
                    } else if (action === "EDIT") {
                        await updateTenant(tenant.id!, tenant);
                    } else {
                        console.error("Unknown command!");
                    }
                    allTenants.value = await getTenants();
                    closeModal();
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
            const name = selectedTenant.value.firstName;
            if (name) {
                if (name.length < 2)
                    return false;
                return true;
            }
            return false;
        });

        const isMiddleNameValid = computed((): boolean => {
            const name = selectedTenant.value.middleName;
            if (name) {
                if (name.length < 2)
                    return false;
                return true;
            }
            return true;
        });

        const isSurnameValid = computed((): boolean => {
            const name = selectedTenant.value.surname;
            if (name) {
                if (name.length < 2)
                    return false;
                return true;
            }
            return false;
        });

        const isEmailAddressValid = computed((): boolean => {            
            const pattern = /\S+@\S+\.\S+/;
            const result = pattern.test(selectedTenant.value.emailAddress!);
            return result;
        });

        const isMobileNumberValid = computed((): boolean => {
            const name = selectedTenant.value.mobileNumber;
            if (name) {
                if (name.length < 8)
                    return false;
                return true;
            }
            return false;
        });

        const search = async (mobileNumber: string) => {
            try {
                allTenants.value = await getTenants(mobileNumber);
            } catch (error) {
                if (error instanceof AxiosError) {
                    console.log((error.response?.data as Response<any>).message)
                } else {
                    console.error(error);
                }
            }
        };

        const handleSlicedTenants = (slicedTenants: Tenant[]) => {
            tenants.value = slicedTenants;
        };

        onMounted(async () => {
            try {
                allTenants.value = await getTenants();
            } catch (error) {
                if (error instanceof AxiosError) {
                    console.log((error.response?.data as Response<any>).message)
                } else {
                    console.error(error);
                }
            }
        });

        return {
            tenantModal,
            searchMobileNumber,
            tenants,
            allTenants,
            label,
            selectedTenant,
            submitted,
            openModal,
            search,
            handleSlicedTenants,
            closeModal,
            createOrUpdateTenant,
            goToTenant,
            isFirstNameValid,
            isSurnameValid,
            isMiddleNameValid,
            isEmailAddressValid,
            isMobileNumberValid
        }
    }
})
</script>

<style lang="">
    
</style>