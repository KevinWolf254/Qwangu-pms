<template>
    <div class="container mt-3">
        <div class="row">
            <div class="col-12 d-flex justify-content-between">
                <h3>Units</h3>
                <button type="button" class="btn btn-primary" @click="openModal('CREATE', unitModal)">
                    <i class="bi bi-house-add me-1" style="font-size: 1.1rem;"></i>
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
                                <th scope="col">Hse No.</th>
                                <th scope="col">Bedrooms</th>
                                <th scope="col">Rent</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="unit in units" :key="unit.id">
                                <td>{{ unit.status }}</td>
                                <td>{{ unit.type }}</td>
                                <td>{{ unit.number }}</td>
                                <td>{{ unit.floorNo }}{{ unit.identifier }}</td>
                                <td>{{ unit.noOfBedrooms }}</td>
                                <td>{{ unit.currency }} {{ Number(unit.rentPerMonth).toLocaleString("en-US") }}</td>
                                <td>
                                    <button type="button" class="btn btn-outline-primary me-1" style="font-size: .75rem;"
                                        @click="goToUnit(unit.id!)">
                                        <i class="bi bi-eye-fill"></i>
                                        View
                                    </button>
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
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="unitModalLabel">{{ label }} Unit</h5>
                    <button type="button" class="btn-close" @click="closeModal()" aria-label="Close"></button>
                </div>
                <form @submit.prevent="createOrUpdateUnit(label.toUpperCase())" class="needs-validation" novalidate>
                    <div class="modal-body">
                        <div class="row g-3">
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="status">Status</label>
                                    <select class="form-select" aria-label="status select" id="status"
                                        v-model="selectedUnit!.status" required disabled>
                                        <option disabled value="">Select type</option>
                                        <option value="VACANT">VACANT</option>
                                        <option value="OCCUPIED">OCCUPIED</option>
                                    </select>
                                    <div class="invalid-feedback">
                                        Please select unit status.
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="type">Type</label>
                                    <select class="form-select" aria-label="type select" id="type"
                                        v-model="selectedUnit!.type"
                                        :class="{ 'is-invalid': !isUnitTypeValid && submitted }" required
                                        :disabled="label == 'Edit'">
                                        <option disabled value="">Select type</option>
                                        <option value="APARTMENT_UNIT">APARTMENT_UNIT</option>
                                        <option value="TOWN_HOUSE">TOWN HOUSE</option>
                                        <option value="MAISONETTES">MAISONETTES</option>
                                        <option value="VILLA">VILLA</option>
                                    </select>
                                    <div class="invalid-feedback">
                                        Please select unit type.
                                    </div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="form-group mb-3">
                                    <label for="propertyId">Property</label>
                                    <select class="form-select" aria-label="propertyId select" id="propertyId"
                                        v-model="selectedUnit!.propertyId"
                                        :class="{ 'is-invalid': !isPropertyIdValid && submitted }" required
                                        :disabled="label == 'Edit'">
                                        <option disabled value="">Select property</option>
                                        <option v-for="property in properties" :key="property.id" :value="property.id"> 
                                            {{ property.name?.toUpperCase() }}
                                        </option>
                                    </select>
                                    <div class="invalid-feedback">
                                        Please select unit type.
                                    </div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="form-group mb-3">
                                    <label for="floorNo">Floor</label>
                                    <input type="text" class="form-control" id="floorNo" v-model="selectedUnit!.floorNo"
                                        placeholder="Enter floor No."
                                        :class="{ 'is-invalid': !isFloorNoValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter a floorNo.
                                    </div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="form-group mb-3">
                                    <label for="identifier">Hse No.</label>
                                    <input type="text" class="form-control" id="identifier" v-model="selectedUnit!.identifier"
                                        placeholder="Enter hse No."
                                        :class="{ 'is-invalid': !isIdentifierValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter a hse no.
                                    </div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="form-group mb-3">
                                    <label for="noOfBedrooms">No. of Bedrooms</label>
                                    <input type="text" class="form-control" id="noOfBedrooms" v-model="selectedUnit!.noOfBedrooms"
                                        placeholder="Enter hse No."
                                        :class="{ 'is-invalid': !isNoOfBedroomsValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter No. of bedrooms.
                                    </div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="form-group mb-3">
                                    <label for="noOfBathrooms">No. of Bathrooms</label>
                                    <input type="text" class="form-control" id="noOfBathrooms" v-model="selectedUnit!.noOfBathrooms"
                                        placeholder="Enter hse No."
                                        :class="{ 'is-invalid': !isNoOfBathroomsValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter No. of Bathrooms.
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="currency">Currency</label>
                                    <select class="form-select" aria-label="currency select" id="currency"
                                        v-model="selectedUnit!.currency"
                                        :class="{ 'is-invalid': !isCurrencyValid && submitted }" required>
                                        <option disabled value="">Select currency</option>
                                        <option value="KES">KES</option>
                                        <option value="DOLLAR">DOLLAR</option>
                                        <option value="POUND">POUND</option>
                                    </select>
                                    <div class="invalid-feedback">
                                        Please select a currency
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="advanceInMonths">Advance (months)</label>
                                    <input type="text" class="form-control" id="advanceInMonths" v-model="selectedUnit!.advanceInMonths"
                                        placeholder="Enter advance in months"
                                        :class="{ 'is-invalid': !isAdvanceInMonthsValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter advance in months
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="rentPerMonth">Rent per month</label>
                                    <input type="text" class="form-control" id="rentPerMonth" v-model="selectedUnit!.rentPerMonth"
                                        placeholder="Enter rent per month"
                                        :class="{ 'is-invalid': !isRentPerMonthValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter rent per month
                                    </div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="form-group mb-3">
                                    <label for="securityPerMonth">Security per month</label>
                                    <input type="text" class="form-control" id="securityPerMonth" v-model="selectedUnit!.securityPerMonth"
                                        placeholder="Enter rent per month"
                                        :class="{ 'is-invalid': !isSecurityPerMonthValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter security per month
                                    </div>
                                </div>
                            </div>
                            <div class="col-6">
                                <div class="form-group mb-3">
                                    <label for="garbagePerMonth">Garbage per month</label>
                                    <input type="text" class="form-control" id="garbagePerMonth" v-model="selectedUnit!.garbagePerMonth"
                                        placeholder="Enter rent per month"
                                        :class="{ 'is-invalid': !isGarbagePerMonthValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter garbage per month
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
import { Unit } from '../../types/Unit';
import * as bootstrap from 'bootstrap';
import { getUnits, createUnit, updateUnit } from "../../api/unit-api";
import { Property } from '../../types/Property';
import { getProperties } from "../../api/property-api";
import { AxiosError } from 'axios';
import { Response } from '../../types/Response';
import { useRouter } from "vue-router";

export default defineComponent({
    name: "Units",
    setup() {
        const selectedUnit: Ref<Unit> = ref(new Unit());
        const allUnits: Ref<Unit[]> = ref([]);
        const searchAccountNo: Ref<string> = ref("");
        const units: Ref<Unit[]> = ref([]);
        const unitModal: Ref<HTMLDivElement | null> = ref(null);
        const submitted: Ref<boolean> = ref(false);
        const properties: Ref<Property[]> = ref([]);
        const router = useRouter();

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

        const goToUnit = (unitId: string) => {
            router?.push(`/units/${unitId}`);
        };

        const search = async (accountNo: string) => {
            allUnits.value = await getUnits(accountNo);
        };

        onMounted(async () => {
            allUnits.value = await getUnits();
            properties.value = await getProperties();
        });

        const handleSlicedUnits = (slicedUnits: Unit[]) => {
            units.value = slicedUnits;
        };

        const createOrUpdateUnit = async (action: string) => {
            submitted.value = true;
            const unit = selectedUnit.value;
            try {
                if (action === "CREATE") {
                    await createUnit(unit);
                } else if (action === "EDIT") {
                    await updateUnit(unit.id!, unit);
                } else {
                    console.error("Unknown command!");
                }
                allUnits.value = await getUnits();
                closeModal();
            } catch (error) {
                if (error instanceof AxiosError) {
                    console.log((error.response?.data as Response<any>).message)
                } else {
                    console.error(error);
                }                
            }
        };
        
        const isPropertyIdValid = computed((): boolean => {
            const propertyId = selectedUnit.value.propertyId;
            if (propertyId) {
                return true;
            }
            return false;
        });
        
        const isUnitTypeValid = computed((): boolean => {
            const type = selectedUnit.value.type;
            if (type) {
                return true;
            }
            return false;
        });
        
        const isFloorNoValid = computed((): boolean => {
            const floorNo = selectedUnit.value.floorNo;
            if (floorNo != undefined && floorNo != null) {
                return true;
            }
            return false;
        });
        
        const isIdentifierValid = computed((): boolean => {
            const identifier = selectedUnit.value.identifier;
            if (identifier) {
                return true;
            }
            return false;
        });
        
        const isNoOfBedroomsValid = computed((): boolean => {
            const noOfBedrooms = selectedUnit.value.noOfBedrooms;
            if (noOfBedrooms) {
                return true;
            }
            return false;
        });
        
        const isNoOfBathroomsValid = computed((): boolean => {
            const noOfBathrooms = selectedUnit.value.noOfBathrooms;
            if (noOfBathrooms) {
                return true;
            }
            return false;
        });
        
        const isAdvanceInMonthsValid = computed((): boolean => {
            const advanceInMonths = selectedUnit.value.advanceInMonths;
            if (advanceInMonths) {
                return true;
            }
            return false;
        });
        
        const isCurrencyValid = computed((): boolean => {
            const currency = selectedUnit.value.currency;
            if (currency) {
                return true;
            }
            return false;
        });
        
        const isRentPerMonthValid = computed((): boolean => {
            const rentPerMonth = selectedUnit.value.rentPerMonth;
            if (rentPerMonth) {
                return true;
            }
            return false;
        });
        
        const isSecurityPerMonthValid = computed((): boolean => {
            const securityAdvance = selectedUnit.value.securityPerMonth;
            if (securityAdvance) {
                return true;
            }
            return false;
        });
        
        const isGarbagePerMonthValid = computed((): boolean => {
            const garbagePerMonth = selectedUnit.value.garbagePerMonth;
            if (garbagePerMonth) {
                return true;
            }
            return false;
        });

        return {
            label,
            allUnits,
            searchAccountNo,
            units,
            unitModal,
            submitted,
            selectedUnit,
            properties,
            openModal,
            closeModal,
            search,
            handleSlicedUnits,
            createOrUpdateUnit,
            goToUnit,
            isPropertyIdValid,
            isUnitTypeValid,
            isFloorNoValid,
            isIdentifierValid,
            isNoOfBedroomsValid,
            isNoOfBathroomsValid,
            isAdvanceInMonthsValid,
            isCurrencyValid,
            isRentPerMonthValid,
            isSecurityPerMonthValid,
            isGarbagePerMonthValid
        }
    }
})
</script>

<style lang="">
    
</style>