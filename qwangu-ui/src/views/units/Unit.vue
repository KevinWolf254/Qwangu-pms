<template>
    <div class="container mt-3">
        <div class="row">
            <div class="col-12 d-flex justify-content-between">
                <h3>Unit - {{ unitId }}</h3>
            </div>
            <hr class="my-4">
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, Ref, ref, onMounted } from 'vue';
import { useRoute } from 'vue-router'
import { getUnit } from "../../api/unit-api";
import { Unit } from '../../types/Unit';

export default defineComponent({
    setup() {
        const route = useRoute();

        let unitId: Ref<string> = ref("");
        let unit: Ref<Unit> = ref(new Unit());

        onMounted(async () => {
            unitId.value = route.params.id as string;
            unit.value = await getUnit(unitId.value);
            console.log(unit.value);
        });

        return {
            unitId
        }
    }
})
</script>
<style lang="">
    
</style>