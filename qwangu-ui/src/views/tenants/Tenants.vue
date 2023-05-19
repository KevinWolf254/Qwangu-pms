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
            </div>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent, Ref, ref } from 'vue';
import { Tenant } from "../../types/Tenant";
import * as bootstrap from 'bootstrap';

export default defineComponent({
    name: "Tenants",
    setup() {
        const tenantModal: Ref<HTMLDivElement | null> = ref(null);
        const selectedTenant: Ref<Tenant> = ref(new Tenant());

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

        return {
            tenantModal,
            openModal
        }
    }
})
</script>

<style lang="">
    
</style>