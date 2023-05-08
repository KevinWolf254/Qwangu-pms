<template>
    <div>
        <div class="container mt-3">
            <div class="row">
                <div class="col-12 d-flex justify-content-between">
                    <h3>Users</h3>
                    <button type="button" class="btn btn-primary">
                        <i class="bi bi-person-fill-add me-1" style="font-size: 1.1rem;"></i>
                        Create
                    </button>
                </div>
                <hr class="my-4">
                <div class="col-12">
                    <div class="container mt-4">
                        <table class="table">
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
                                <tr>
                                    <th scope="row">1</th>
                                    <td>Mark Otto Man</td>
                                    <td>mark.Otto@company.co.ke</td>
                                    <td>
                                        <span class="badge bg-primary">ACCOUNTANT</span>
                                    </td>
                                    <td><span class="badge bg-success">Yes</span></td>
                                    <td>
                                        <button type="button" class="btn btn-outline-secondary me-1" style="font-size: .75rem;">
                                            <i class="bi bi-pencil-square"></i>
                                            Edit
                                        </button>
                                        <button type="button" class="btn btn-outline-danger" style="font-size: .75rem;">
                                            <i class="bi bi-trash-fill"></i>
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                                <tr>
                                    <th scope="row">2</th>
                                    <td>Jacob Thornton Guy</td>
                                    <td>Jacob.Thornton@company.co.ke</td>
                                    <td>
                                        <span class="badge bg-success">ADMIN</span>
                                    </td>
                                    <td><span class="badge bg-danger">No</span></td>
                                    <td>
                                        <button type="button" class="btn btn-outline-secondary me-1" style="font-size: .75rem;">
                                            <i class="bi bi-pencil-square"></i>
                                            Edit
                                        </button>
                                        <button type="button" class="btn btn-outline-danger" style="font-size: .75rem;">
                                            <i class="bi bi-trash-fill"></i>
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                                <tr>
                                    <th scope="row">3</th>
                                    <td>Larry the Bird</td>
                                    <td>Larry.Bird@company.co.ke</td>
                                    <td>
                                        <span class="badge bg-info">SUPERVISOR</span>
                                    </td>
                                    <td><span class="badge bg-success">Yes</span></td>
                                    <td>
                                        <span class="badge bg-secondary me-1"></span>
                                        <span class="badge bg-danger"></span>
                                        <button type="button" class="btn btn-outline-secondary me-1" style="font-size: .75rem;">
                                            <i class="bi bi-pencil-square"></i>
                                            Edit
                                        </button>
                                        <button type="button" class="btn btn-outline-danger" style="font-size: .75rem;">
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
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'Users',
    setup() {

    }
});
</script>
<style lang="">
    
</style>