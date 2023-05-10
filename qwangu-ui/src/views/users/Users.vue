<template>
    <div>
        <div class="container mt-3">
            <div class="row">
                <div class="col-12 d-flex justify-content-between">
                    <h3>Users</h3>
                    <!-- <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#exampleModal">
                        <i class="bi bi-person-fill-add me-1" style="font-size: 1.1rem;"></i>
                        Create
                    </button> -->
                    <button type="button" class="btn btn-primary" @click="openModal">
                        <i class="bi bi-person-fill-add me-1" style="font-size: 1.1rem;"></i>
                        Create
                    </button>
                </div>
                <hr class="my-4">
                <div class="col-12">
                    <div class="container mt-4">
                        <div class="d-flex flex-row-reverse mt-4">
                            <div class="col-auto ms-2">
                                <button type="submit" class="btn btn-primary mb-3">Search</button>
                            </div>
                            <div class="mb-2">
                                <input type="email" class="form-control" id="exampleFormControlInput1"
                                    placeholder="email address" style="width: 25rem;">
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
                                <tr>
                                    <th scope="row">1</th>
                                    <td>Mark Otto Man</td>
                                    <td>mark.Otto@company.co.ke</td>
                                    <td>
                                        <span class="badge bg-primary">ACCOUNTANT</span>
                                    </td>
                                    <td><span class="badge bg-success">Yes</span></td>
                                    <td>
                                        <button type="button" class="btn btn-outline-secondary me-1"
                                            style="font-size: .75rem;">
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
                                        <button type="button" class="btn btn-outline-secondary me-1"
                                            style="font-size: .75rem;">
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
                                        <button type="button" class="btn btn-outline-secondary me-1"
                                            style="font-size: .75rem;">
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
    
    <div class="modal fade" ref="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="exampleModalLabel">Modal title</h5>
            <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            ...
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Close</button>
            <button type="button" class="btn btn-primary">Save changes</button>
          </div>
        </div>
      </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, Ref, ref } from 'vue';
import bootstrap from 'bootstrap';

export default defineComponent({
    name: 'Users',
    setup() {
    const exampleModal: Ref<HTMLDivElement | null> = ref(null);

    const openModal = () => {
      const modal = new bootstrap.Modal(exampleModal.value!);
      modal.show();
    };

    const closeModal = () => {
      const modal = new bootstrap.Modal(exampleModal.value!);
      modal.hide();
    };

    return {
      exampleModal,
      openModal,
      closeModal,
    };
  },
});
</script>
<style lang="">
    
</style>