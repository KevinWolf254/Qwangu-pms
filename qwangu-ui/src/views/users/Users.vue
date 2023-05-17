<template>
    <div class="container mt-3">
        <div class="row">
            <div class="col-12 d-flex justify-content-between">
                <h3>Users</h3>
                <button type="button" class="btn btn-primary" @click="openModal('CREATE', userModal)">
                    <i class="bi bi-person-fill-add me-1" style="font-size: 1.1rem;"></i>
                    Create
                </button>
            </div>
            <hr class="my-4">
            <div class="col-12">
                <div class="container mt-4">
                    <div class="d-flex flex-row-reverse mt-4">
                        <div class="col-auto ms-2">
                            <button type="submit" class="btn btn-primary mb-3"
                                @click="search(searchEmailAddress)">Search</button>
                        </div>
                        <div class="mb-2">
                            <input type="email" class="form-control" id="searchEmailAddress" placeholder="email address"
                                style="width: 25rem;" v-model="searchEmailAddress">
                        </div>
                    </div>
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th scope="col">Full Names</th>
                                <th scope="col">Email Address</th>
                                <th scope="col">Role</th>
                                <th scope="col">Enabled</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="user in users" :key="user.id">
                                <td>{{ user!.person!.firstName }} {{ user!.person!.otherNames }} {{
                                    user!.person!.surname }}</td>
                                <td>{{ user.emailAddress }}</td>
                                <td>
                                    <span class="badge bg-info">{{ getUserRoleName(user.roleId!) }}</span>
                                </td>
                                <td><span class="badge bg-success">{{ user.isEnabled ? 'YES' : 'NO' }}</span></td>
                                <td>
                                    <button type="button" class="btn btn-outline-secondary me-1"
                                        style="font-size: .75rem;" @click="openModal('EDIT', userModal, user)">
                                        <i class="bi bi-pencil-square"></i>
                                        Edit
                                    </button>
                                    <button type="button" class="btn btn-outline-danger" style="font-size: .75rem;"
                                        @click="openModal('DELETE', deleteUserModal, user)">
                                        <i class="bi bi-trash-fill"></i>
                                        Delete
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="mt-4 d-flex flex-row-reverse">
                        <Pagination :items="allUsers" @items-sliced="handleSlicedUsers"></Pagination>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!-- create user modal -->
    <div class="modal fade" ref="userModal" tabindex="-1" aria-labelledby="userModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="userModalLabel">{{ label }} User</h5>
                    <button type="button" class="btn-close" @click="closeModal()" aria-label="Close"></button>
                </div>
                <form @submit.prevent="createOrUpdateUser(label.toUpperCase())" class="needs-validation" novalidate>
                    <div class="modal-body">
                        <div class="row g-3">
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="surname">Surname</label>
                                    <input type="text" class="form-control" id="surname"
                                        v-model="selectedUser!.person!.surname" placeholder="Enter surname"
                                        :class="{ 'is-invalid': !isSurnameValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter a surname with a minimum of 2 characters.
                                    </div>
                                </div>
                            </div>
                            <div class="col">
                                <div class="form-group mb-3">
                                    <label for="firstName">First Name</label>
                                    <input type="text" class="form-control" id="firstName"
                                        v-model="selectedUser!.person!.firstName" placeholder="Enter first name"
                                        :class="{ 'is-invalid': !isFirstValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter a first name with a minimum of 2 characters.
                                    </div>
                                </div>
                            </div>
                            <div class="col">
                                <div class="form-group mb-3">
                                    <label for="otherNames">Other Names</label>
                                    <input type="text" class="form-control" id="otherNames"
                                        v-model="selectedUser!.person!.otherNames" placeholder="Enter other names" />
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="emailAddress">Email Address</label>
                                    <input type="email" class="form-control" id="emailAddress"
                                        v-model="selectedUser!.emailAddress" placeholder="Enter email address"
                                        :class="{ 'is-invalid': !isEmailValid && submitted }" required />
                                    <div class="invalid-feedback">
                                        Please enter a valid email address.
                                    </div>
                                </div>
                            </div>
                            <div v-if="label == 'Edit'">
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="isEnabled" value="true" id="enable"
                                        v-model="selectedUser!.isEnabled">
                                    <label class="form-check-label" for="enable">
                                        Enable
                                    </label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="isEnabled" value="false" id="disable"
                                        v-model="selectedUser!.isEnabled">
                                    <label class="form-check-label" for="disable">
                                        Disable
                                    </label>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="form-group mb-3">
                                    <label for="role">Role</label>
                                    <select class="form-select" aria-label="role select" id="role"
                                        v-model="selectedUser!.roleId" :class="{ 'is-invalid': !isRoleValid && submitted }"
                                        required>
                                        <option disabled value="">Select role</option>
                                        <option v-for="userRole in userRoles" :key="userRole.id" :value="userRole.id"> {{
                                            userRole.name?.toUpperCase() }}</option>
                                    </select>
                                    <div class="invalid-feedback">
                                        Please select role.
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
    <!-- delete user modal -->
    <div class="modal fade" ref="deleteUserModal" tabindex="-1" aria-labelledby="deleteUserModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="deleteUserModalLabel">{{ label }} User</h5>
                    <button type="button" class="btn-close" @click="closeModal()" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    Are you sure you would like to delete user {{ selectedUser?.person?.firstName }} {{
                        selectedUser?.person?.otherNames }} {{ selectedUser?.person?.surname }} with email {{
        selectedUser?.emailAddress }}?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" @click="closeModal()">Close</button>
                    <button type="button" class="btn btn-danger" @click="deleteUser(selectedUser.id!)">Delete</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, Ref, ref, computed } from 'vue';
import * as bootstrap from 'bootstrap';
import { getUsers, createUser, updateUser, deleteUser as deleteUserRequest } from "../../api/user-api";
import { CreateUserRequest, Person, User, UpdateUserRequest } from "../../types/User";
import { UserRole } from "../../types/UserRole";
import { getUserRoles } from '../../api/user-role-api';
import Pagination from '../../components/Pagination.vue';
import { AxiosError } from 'axios';
import { Response } from '../../types/Response';

export default defineComponent({
    name: "Users",
    setup() {
        const userModal: Ref<HTMLDivElement | null> = ref(null);
        const deleteUserModal: Ref<HTMLDivElement | null> = ref(null);
        const submitted: Ref<boolean> = ref(false);
        let label: Ref<string> = ref("");
        const users: Ref<User[]> = ref([]);
        const allUsers: Ref<User[]> = ref([]);
        const selectedUser: Ref<User> = ref(new User());
        const userRoles: Ref<UserRole[]> = ref([]);
        const searchEmailAddress: Ref<string> = ref("");
        let modal: bootstrap.Modal;

        const openModal = (action: string, refModal: HTMLDivElement | null, user?: User) => {
            if (action === "CREATE") {
                label.value = "Create";
                selectedUser.value = new User();
            }
            else if (action === "EDIT") {
                label.value = "Edit";
                selectedUser.value = user!;
            }
            else if (action === "DELETE") {
                label.value = "Delete";
                selectedUser.value = user!;
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

        const createOrUpdateUser = async (action: string) => {
            submitted.value = true;
            if (isEmailValid.value && isFirstValid.value && isRoleValid.value && isSurnameValid.value) {
                try {
                    const user = selectedUser.value;
                    if (action === "CREATE") {
                        await createUser(new CreateUserRequest(user.emailAddress, user.roleId,
                            new Person(user.person?.firstName, user.person?.otherNames, user.person?.surname)));
                    } else if (action === "EDIT") {
                        await updateUser(user.id!, new UpdateUserRequest(user.emailAddress, user.roleId,
                            new Person(user.person?.firstName, user.person?.otherNames, user.person?.surname), selectedUser.value.isEnabled));
                    } else {
                        console.error("Unknown command!");
                    }
                    allUsers.value = await getUsers();
                    userRoles.value = await getUserRoles();
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

        const deleteUser = async (userId: string) => {
            try {
                await deleteUserRequest(userId);
                allUsers.value = await getUsers();
                userRoles.value = await getUserRoles();
                closeModal();
            } catch (error) {
                if (error instanceof AxiosError) {
                    console.log((error.response?.data as Response<any>).message)
                } else {
                    console.error(error);
                }
            }
        };

        const search = async (emailAddress: string) => {
            allUsers.value = await getUsers(emailAddress);
            userRoles.value = await getUserRoles();
        };

        const isEmailValid = computed(() => {
            const pattern = /\S+@\S+\.\S+/;
            const result = pattern.test(selectedUser.value.emailAddress!);
            return result;
        });

        const isSurnameValid = computed((): boolean => {
            const surname = selectedUser.value.person?.surname;
            if (surname) {
                if (surname.length < 2)
                    return false;
                return true;
            }
            return false;
        });

        const isFirstValid = computed((): boolean => {
            const firstName = selectedUser.value.person?.firstName;
            if (firstName) {
                if (firstName.length < 2)
                    return false;
                return true;
            }
            return false;
        });

        const isRoleValid = computed((): boolean => {
            const roleId = selectedUser.value.roleId;
            return roleId ? true : false;
        });

        const getUserRoleName = (userRoleId: string): string => {
            return userRoles.value.find(role => role.id === userRoleId)?.name!;
        };

        const handleSlicedUsers = (slicedUsers: User[]) => {
            users.value = slicedUsers;
        };

        onMounted(async () => {
            allUsers.value = await getUsers();
            userRoles.value = await getUserRoles();

            const childComponent = document.querySelector('Pagination');
            childComponent?.addEventListener('items', () => { console.log('here!!!') });
        });

        return {
            userModal,
            submitted,
            deleteUserModal,
            label,
            users,
            allUsers,
            userRoles,
            selectedUser,
            searchEmailAddress,
            isEmailValid,
            isSurnameValid,
            isFirstValid,
            isRoleValid,
            getUserRoleName,
            openModal,
            closeModal,
            search,
            createOrUpdateUser,
            deleteUser,
            handleSlicedUsers
        };
    },
    components: { Pagination }
});
</script>

<style lang="">
    
</style>