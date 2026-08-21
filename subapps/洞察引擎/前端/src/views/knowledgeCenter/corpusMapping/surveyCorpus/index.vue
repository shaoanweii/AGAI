<script setup lang="ts">
import TextCorpus from '../textCorpus/index.vue'

defineOptions({
  name: 'KnowledgeCenterSurveyCorpus'
})

interface Props {
  variant?: 'corpus' | 'textNewWord' | 'surveyNewWord'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'corpus'
})
</script>

<template>
  <TextCorpus :opinion-type="0" :variant="props.variant" />
</template>
