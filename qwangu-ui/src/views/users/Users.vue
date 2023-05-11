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
                                <button type="submit" class="btn btn-primary mb-3" @click="search(searchEmailAddress)">Search</button>
                            </div>
                            <div class="mb-2">
                                <input type="email" class="form-control" id="searchEmailAddress"
                                    placeholder="email address" style="width: 25rem;" v-model="searchEmailAddress">
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
                                    <td>{{ user!.person!.firstName }} {{ user!.person!.otherNames }} {{ user!.person!.surname }}</td>
                                    <td>{{ user.emailAddress }}</td>
                                    <td>
                                        <span class="badge bg-info">{{ user.roleId }}</span>
                                    </td>
                                    <td><span class="badge bg-success">{{ user.isEnabled }}</span></td>
                                    <td>
                                        <span class="badge bg-secondary me-1"></span>
                                        <span class="badge bg-danger"></span>
                                        <button type="button" class="btn btn-outline-secondary me-1"
                                            style="font-size: .75rem;">
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
                    <button type="button" class="btn-close" @click="closeModal(label.toUpperCase())" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-12">
                            <div class="form-group mb-3">
                                <label for="surname">Surname</label>
                                <input type="text" class="form-control" id="surname" v-model="selectedUser!.person!.surname"
                                    placeholder="Enter surname" />
                            </div>
                        </div>
                        <div class="col">
                            <div class="form-group mb-3">
                                <label for="firstName">First Name</label>
                                <input type="text" class="form-control" id="firstName" v-model="selectedUser!.person!.firstName"
                                    placeholder="Enter first name" />
                            </div>
                        </div>
                        <div class="col">
                            <div class="form-group mb-3">
                                <label for="otherNames">Other Names</label>
                                <input type="text" class="form-control" id="otherNames" v-model="selectedUser!.person!.otherNames"
                                    placeholder="Enter other names" />
                            </div>
                        </div>                        
                        <div class="col-12">
                            <div class="form-group mb-3">
                                <label for="emailAddress">Email Address</label>
                                <input type="email" class="form-control" id="emailAddress" v-model="selectedUser!.emailAddress"
                                    placeholder="Enter email address" />
                            </div>
                        </div>                      
                        <div class="col-12">
                            <div class="form-group mb-3">
                                <label for="role">Role</label>
                                <select class="form-select" aria-label="role select" id="role" v-model="selectedUser!.roleId">
                                    <option selected value="">Select role</option>
                                    <option value="1">ACCOUNTANT</option>
                                    <option value="2">ADMIN</option>
                                    <option value="3">SUPERVISOR</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" @click="closeModal(label.toUpperCase())">Close</button>
                    <button type="button" class="btn btn-primary" @click="saveOrUpdateUser(label.toUpperCase())">Save changes</button>
                </div>
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
                    Are you sure you would like to delete user {{ selectedUser?.person?.firstName }} {{ selectedUser?.person?.otherNames }} {{ selectedUser?.person?.surname }} ?
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
import { defineComponent, onMounted, Ref, ref } from 'vue';
import * as bootstrap from 'bootstrap';
import { getUsers } from "../../api/user-api";
import { User } from "../../types/user";

export default defineComponent({
    name: 'Users',
    setup() {
        const userModal: Ref<HTMLDivElement | null> = ref(null);
        const deleteUserModal: Ref<HTMLDivElement | null> = ref(null);

        let label:  Ref<string> = ref('');

        const users: Ref<User[]> = ref([]);
        const selectedUser: Ref<User> = ref(new User());

        const searchEmailAddress: Ref<string> = ref('');
            
        let modal: bootstrap.Modal;

        const openModal = (action: string, refModal: HTMLDivElement | null, user?: User) => {
            if(action === 'CREATE') {
                label.value = 'Create';
                selectedUser.value = new User();
            } else if (action === 'EDIT') {
                label.value = 'Edit';
                selectedUser.value = user!;
            } else if (action === 'DELETE') {
                label.value = 'Delete';
                selectedUser.value = user!;
            } else {

            }
            modal = new bootstrap.Modal(refModal!);
            modal?.show();
        };

        const closeModal = (action: string) => {
            if(action === 'CREATE') {
                
            } else if (action === 'EDIT') {
                
            } else if (action === 'DELETE') {
                
            } else {

            }
            modal?.hide();
        };

        const saveOrUpdateUser = (action: string) => {
            if(action === 'CREATE') {
                
            } else if (action === 'EDIT') {
                
            } else {

            }
            closeModal(action);
        };

        const deleteUser = () => {
            closeModal("DELETE");
        }

        const search = (emailAddress: string) => {
            console.log("searching " +emailAddress);
        };

        onMounted(async () => {
            users.value = await getUsers();
        });

        return {
            userModal,
            deleteUserModal,
            label,
            // surname,
            // firstName,
            // otherNames,
            // emailAddress,
            // role,
            users,
            selectedUser,
            searchEmailAddress,
            openModal,
            closeModal,
            search,
            saveOrUpdateUser,
            deleteUser
        };
    },
});
</script>

<style lang="">
    
</style>