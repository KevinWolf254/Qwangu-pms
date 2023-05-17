<template>
    <div class="container mt-3">
        <div class="row">
            <div class="col-12 d-flex justify-content-between">
                <h3>Properties</h3>
                <button type="button" class="btn btn-primary" @click="openModal('CREATE', propertyModal)">
                    <i class="bi bi-building-add me-1" style="font-size: 1.1rem;"></i>
                    Create
                </button>
            </div>
            <hr class="my-4">
            <div class="col-12">
                <div class="container mt-4">
                    <div class="d-flex flex-row-reverse mt-4">
                        <div class="col-auto ms-2">
                            <button type="submit" class="btn btn-primary mb-3" @click="search(searchName)">Search</button>
                        </div>
                        <div class="mb-2">
                            <input type="text" class="form-control" id="searchName" placeholder="property name"
                                style="width: 25rem;" v-model="searchName">
                        </div>
                    </div>
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th scope="col">Type</th>
                                <th scope="col">Name</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="property in properties" :key="property.id">
                                <td>{{ property.type }}</td>
                                <td>{{ property.name }}</td>
                                <td>
                                    <button type="button" class="btn btn-outline-secondary me-1" style="font-size: .75rem;"
                                        @click="openModal('EDIT', propertyModal, property)">
                                        <i class="bi bi-pencil-square"></i>
                                        Edit
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="mt-4 d-flex flex-row-reverse">
                        <Pagination :items="allProperties" @items-sliced="handleSlicedProperties"></Pagination>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!-- create property modal -->
    <div class="modal fade" ref="propertyModal" tabindex="-1" aria-labelledby="propertyModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="propertyModalLabel">{{ label }} Property</h5>
                    <button type="button" class="btn-close" @click="closeModal()" aria-label="Close"></button>
                </div>
                <form @submit.prevent="createOrUpdateProperty(label.toUpperCase())" class="needs-validation" novalidate>
                    <div class="modal-body">
                        <div class="row g-3">
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="type">Property Type</label>
                                    <select class="form-select" aria-label="type select" id="type"
                                        v-model="selectedProperty!.type"
                                        :class="{ 'is-invalid': !isPropertyTypeValid && submitted }" required
                                        :disabled="label == 'Edit'">
                                        <option disabled value="">Select type</option>
                                        <option value="APARTMENT">APARTMENT</option>
                                        <option value="HOUSE">HOUSE</option>
                                    </select>
                                    <div class="invalid-feedback">
                                        Please select property type.
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="name">Property Name</label>
                                    <input type="text" class="form-control" id="name" v-model="selectedProperty!.name"
                                        placeholder="Enter property name"
                                        :class="{ 'is-invalid': !isNameValid && submitted }" required
                                        :disabled="label == 'Edit'" />
                                    <div class="invalid-feedback">
                                        Please enter a property name with a minimum of 2 characters.
                                    </div>
                                </div>
                            </div>
                            <div class="col-12 mb-3">
                                <label for="description" class="form-label">Description</label>
                                <textarea class="form-control" id="description" rows="5"
                                    v-model="selectedProperty!.description"></textarea>
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
import { defineComponent, Ref, ref, computed, onMounted } from 'vue';
import * as bootstrap from 'bootstrap';
import { Property } from '../../types/Property';
import { AxiosError } from 'axios';
import { Response } from '../../types/Response';
import { getProperties, createProperty, updateProperty } from "../../api/property-api";
import Pagination from '../../components/Pagination.vue';

export default defineComponent({
    name: "Properties",
    setup() {
        const selectedProperty: Ref<Property> = ref(new Property());
        const submitted: Ref<boolean> = ref(false);
        const propertyModal: Ref<HTMLDivElement | null> = ref(null);
        const allProperties: Ref<Property[]> = ref([]);
        const properties: Ref<Property[]> = ref([]);
        const searchName: Ref<string> = ref("");

        let label: Ref<string> = ref("");
        let modal: bootstrap.Modal;

        const openModal = (action: string, refModal: HTMLDivElement | null, property?: Property) => {
            if (action === "CREATE") {
                label.value = "Create";
                selectedProperty.value = new Property();
            }
            else if (action === "EDIT") {
                label.value = "Edit";
                selectedProperty.value = property!;
            }
            else if (action === "DELETE") {
                label.value = "Delete";
                selectedProperty.value = property!;
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

        const createOrUpdateProperty = async (action: string) => {
            submitted.value = true;
            if (isPropertyTypeValid.value && isNameValid.value) {
                try {
                    const property = selectedProperty.value;
                    console.log(property)
                    if (action === "CREATE") {
                        await createProperty(property);
                    } else if (action === "EDIT") {
                        await updateProperty(property.id!, property);
                    } else {
                        console.error("Unknown command!");
                    }
                    allProperties.value = await getProperties();
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

        const isPropertyTypeValid = computed((): boolean => {
            const type = selectedProperty.value.type;
            if (type) {
                return true;
            }
            return false;
        });

        const isNameValid = computed((): boolean => {
            const name = selectedProperty.value.name;
            if (name) {
                if (name.length < 2)
                    return false;
                return true;
            }
            return false;
        });

        const search = async (name: string) => {
            allProperties.value = await getProperties(undefined, name);
        };

        const handleSlicedProperties = (slicedProperties: Property[]) => {
            properties.value = slicedProperties;
        };

        onMounted(async () => {
            allProperties.value = await getProperties();
        });

        return {
            submitted,
            label,
            propertyModal,
            isPropertyTypeValid,
            isNameValid,
            selectedProperty,
            properties,
            allProperties,
            searchName,
            handleSlicedProperties,
            search,
            openModal,
            closeModal,
            createOrUpdateProperty
        };
    }
})
</script>

<style lang="">
    
</style>