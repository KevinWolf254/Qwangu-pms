<template>
    <div class="container mt-3">
        <div class="row">
            <div class="col-12 d-flex justify-content-between">
                <h3>Properties</h3>
                <button type="button" class="btn btn-primary" @click="openModal('CREATE', unitModal)">
                    <i class="bi bi-building-add me-1" style="font-size: 1.1rem;"></i>
                    Create
                </button>
            </div>
            <hr class="my-4">
            <div class="col-12">
                <div class="container mt-4">
                    <div class="d-flex flex-row-reverse mt-4">
                        <div class="col-auto ms-2">
                            <button type="submit" class="btn btn-primary mb-3"
                                @click="search(searchAccountNo)">Search</button>
                        </div>
                        <div class="mb-2">
                            <input type="text" class="form-control" id="searchAccountNo" placeholder="Enter Acct No."
                                style="width: 25rem;" v-model="searchAccountNo">
                        </div>
                    </div>
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th scope="col">Status</th>
                                <th scope="col">Type</th>
                                <th scope="col">Acct No.</th>
                                <th scope="col">Floor</th>
                                <th scope="col">Bedrooms</th>
                                <th scope="col">Rent</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="unit in units" :key="unit.id">
                                <td>{{ unit.status }}</td>
                                <td>{{ unit.type }}</td>
                                <td>{{ unit.number }} {{ unit.identifier }}</td>
                                <td>{{ unit.floorNo }}</td>
                                <td>{{ unit.noOfBedrooms }}</td>
                                <td>{{ unit.currency }} {{ unit.rentPerMonth }}</td>
                                <td>
                                    <button type="button" class="btn btn-outline-success me-1" style="font-size: .75rem;">
                                        <i class="bi bi-eye-fill"></i>
                                        View
                                    </button>
                                </td>
                                <td>
                                    <span class="badge bg-secondary me-1"></span>
                                    <button type="button" class="btn btn-outline-secondary me-1" style="font-size: .75rem;"
                                        @click="openModal('EDIT', unitModal, unit)">
                                        <i class="bi bi-pencil-square"></i>
                                        Edit
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="mt-4 d-flex flex-row-reverse">
                        <Pagination :items="allUnits" @items-sliced="handleSlicedUnits"></Pagination>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- create/edit unit modal -->
    <div class="modal fade" ref="unitModal" tabindex="-1" aria-labelledby="unitModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="unitModalLabel">{{ label }} Unit</h5>
                    <button type="button" class="btn-close" @click="closeModal()" aria-label="Close"></button>
                </div>
                <form @submit.prevent="createOrUpdateUnit(label.toUpperCase())" class="needs-validation" novalidate>
                    <div class="modal-body">

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
import { defineComponent, Ref, ref, onMounted } from 'vue';
import { Unit } from '../../types/Unit';
import * as bootstrap from 'bootstrap';
import { getUnits } from "../../api/unit-api";

export default defineComponent({
    name: "Units",
    setup() {
        const selectedUnit: Ref<Unit> = ref(new Unit());
        const allUnits: Ref<Unit[]> = ref([]);
        const searchAccountNo: Ref<string> = ref("");
        const units: Ref<Unit[]> = ref([]);
        const unitModal: Ref<HTMLDivElement | null> = ref(null);
        const submitted: Ref<boolean> = ref(false);

        let label: Ref<string> = ref("");
        let modal: bootstrap.Modal;

        const openModal = (action: string, refModal: HTMLDivElement | null, unit?: Unit) => {
            if (action === "CREATE") {
                label.value = "Create";
                selectedUnit.value = new Unit();
            }
            else if (action === "EDIT") {
                label.value = "Edit";
                selectedUnit.value = unit!;
            }
            else if (action === "DELETE") {
                label.value = "Delete";
                selectedUnit.value = unit!;
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

        const search = async (accountNo: string) => {
            allUnits.value = await getUnits(accountNo);
        };

        onMounted(async () => {
            allUnits.value = await getUnits();
        });

        const handleSlicedUnits = (slicedUnits: Unit[]) => {
            units.value = slicedUnits;
        };

        const createOrUpdateUnit = async (action: string) => {
            submitted.value = true;

            if (action === "CREATE") {
                // await createProperty(property);
            } else if (action === "EDIT") {
                // await updateProperty(property.id!, property);
            } else {
                console.error("Unknown command!");
            }
        }
        return {
            label,
            allUnits,
            searchAccountNo,
            units,
            unitModal,
            openModal,
            closeModal,
            search,
            handleSlicedUnits,
            createOrUpdateUnit
        }
    }
})
</script>

<style lang="">
    
</style>