<template>
    <div>
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
                                    <th scope="col">#</th>
                                    <th scope="col">Full Names</th>
                                    <th scope="col">Email Address</th>
                                    <th scope="col">Role</th>
                                    <th scope="col">Enabled</th>
                                    <th scope="col">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(user, index) in users" :key="user.id">
                                    <th scope="row">{{ index + 1 }}</th>
                                    <td>{{ user!.person!.firstName }} {{ user!.person!.otherNames }} {{
                                        user!.person!.surname }}</td>
                                    <td>{{ user.emailAddress }}</td>
                                    <td>
                                        <span class="badge bg-info">{{ getUserRoleName(user.roleId!) }}</span>
                                    </td>
                                    <td><span class="badge bg-success">{{ user.isEnabled ? 'YES' : 'NO' }}</span></td>
                                    <td>
                                        <span class="badge bg-secondary me-1"></span>
                                        <span class="badge bg-danger"></span>
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
                            <nav aria-label="Page navigation example">
                                <ul class="pagination">
                                    <li class="page-item"><a class="page-link" href="#">Previous</a></li>
                                    <li class="page-item"><a class="page-link" href="#">1</a></li>
                                    <li class="page-item"><a class="page-link" href="#">2</a></li>
                                    <li class="page-item"><a class="page-link" href="#">3</a></li>
                                    <li class="page-item"><a class="page-link" href="#">Next</a></li>
                                </ul>
                            </nav>
                        </div>
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
                    <button type="button" class="btn-close" @click="closeModal(label.toUpperCase())"
                        aria-label="Close"></button>
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
                        <button type="button" class="btn btn-secondary"
                            @click="closeModal(label.toUpperCase())">Close</button>
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
                    <button type="button" class="btn-close" @click="closeModal('DELETE')" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    Are you sure you would like to delete user {{ selectedUser?.person?.firstName }} {{
                        selectedUser?.person?.otherNames }} {{ selectedUser?.person?.surname }} ?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" @click="closeModal('DELETE')">Close</button>
                    <button type="button" class="btn btn-danger" @click="deleteUser">Delete</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, Ref, ref, computed } from 'vue';
import * as bootstrap from 'bootstrap';
import { getUsers, createUser } from "../../api/user-api";
import { CreateUserRequest, Person, User } from "../../types/User";
import { UserRole } from "../../types/UserRole";
import { getUserRoles } from '../../api/user-role-api';
import Pagination from '../../components/Pagination.vue';

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
                console.log(selectedUser.value);
            }
            else if (action === "DELETE") {
                label.value = "Delete";
                selectedUser.value = user!;
            }
            else {
            }
            modal = new bootstrap.Modal(refModal!);
            modal?.show();
        };

        const closeModal = (action: string) => {
            if (action === "CREATE") {

            }
            else if (action === "EDIT") {
            }
            else if (action === "DELETE") {
            }
            else {
            }
            submitted.value = false;
            modal?.hide();
        };

        const createOrUpdateUser = async (action: string) => {
            submitted.value = true;
            console.log(selectedUser.value);
            if (isEmailValid.value && isFirstValid.value && isRoleValid.value && isSurnameValid.value) {
                if (action === "CREATE") {
                    await createUser(new CreateUserRequest(selectedUser.value.emailAddress, selectedUser.value.roleId,
                        new Person(selectedUser.value.person?.firstName, selectedUser.value.person?.otherNames, selectedUser.value.person?.surname)));
                    allUsers.value = await getUsers();
                    userRoles.value = await getUserRoles();

                }
                else if (action === "EDIT") {
                    
                }
                else {

                }
                selectedUser.value = new User();
                closeModal(action);
            }
        };

        const deleteUser = () => {
            closeModal("DELETE");
        };

        const search = (emailAddress: string) => {
            console.log("searching " + emailAddress);
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
            console.log('Sliced: ' + slicedUsers);
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