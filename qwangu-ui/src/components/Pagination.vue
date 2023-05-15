<template>
    <div>
        <nav aria-label="Page navigation">
            <ul class="pagination">
                <li class="page-item" :class="{ disabled: currentPage === 1 }"><a class="page-link" href="#"
                        @click.prevent="previousPage">Previous</a></li>
                <li class="page-item" v-for="pageNumber in totalPages" :key="pageNumber"
                    :class="{ active: currentPage === pageNumber }"><a class="page-link" href="#"
                        @click.prevent="currentPage = pageNumber">{{ pageNumber }}</a></li>
                <li class="page-item" :class="{ disabled: currentPage === totalPages }"><a class="page-link" href="#"
                        @click.prevent="nextPage">Next</a></li>
            </ul>
        </nav>
    </div>
</template>

<script lang="ts">
import { defineComponent, Ref, ref, computed, watchEffect } from "vue";

export default defineComponent({
    props: {
        items: { type: Array, required: true }
    },
    setup(props, { emit }) {
        const currentPage: Ref<number> = ref(1);
        const pageSize: Ref<number> = ref(10);
        const slicedItems: Ref<unknown[]> = ref<unknown[]>([]);

        const totalPages = computed((): number => {
            const result = Math.ceil((props.items.length!) / pageSize.value);
            return result;
        });

        const calculateSlicedItems = () => {
            const startIndex = (currentPage.value - 1) * pageSize.value;
            const endIndex = startIndex + pageSize.value;
            slicedItems.value = props.items.slice(startIndex, endIndex);
            emit('items-sliced', slicedItems.value);
        };

        const previousPage = () => {
            if (currentPage.value > 1) {
                currentPage.value--;
            }
        };

        const nextPage = () => {
            if (currentPage.value < totalPages.value) {
                currentPage.value++;
            }
        };

        watchEffect(calculateSlicedItems);

        return {
            previousPage,
            nextPage,
            currentPage,
            pageSize,
            totalPages,
            calculateSlicedItems
        }
    }
})
</script>

<style></style>