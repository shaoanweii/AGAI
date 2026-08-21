<script setup lang="ts">
import type { LabelTag } from '@/api/common/index.d'
import { findTagLabelByType } from '@/api/common'
import { ProductFilterTagName, ServiceFilterTagName, TagType } from '@/constants'
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { getAllByType } from '@/utils/tags'

defineOptions({
  name: 'TagCascader'
})
const modelValue = defineModel<any>()

const { pageName = '', teleported = true } = defineProps<{
  pageName?: string
  teleported?: boolean
}>()

const route = useRoute()

const tagOptions = ref<any[]>([])

const getTagOptions = async (tagType: TagType) => {
  try {
    const res = await findTagLabelByType({ tagLibType: tagType })
    return res.result
  } catch {
    return []
  }
}

const init = async () => {
  const _PRName = pageName || (route.name as string)
  if (['journeyAnalysis'].includes(_PRName)) {
    const _tagTree = await getTagOptions(TagType.UserJourney)
    tagOptions.value = getAllByType(TagType.UserJourney, _PRName, _tagTree)
  } else if (['serviceAnalysis'].includes(_PRName)) {
    const _tagTree = await getTagOptions(TagType.Domain)
    tagOptions.value = _tagTree.filter((item: LabelTag) => item.tagName === ServiceFilterTagName)
  } else if (['productAnalysis'].includes(_PRName)) {
    const _tagTree = await getTagOptions(TagType.Domain)
    tagOptions.value = _tagTree.filter((item: LabelTag) => item.tagName === ProductFilterTagName)
  } else if (['voiceManagement', 'selfServiceOriginalSoundQuery'].includes(_PRName)) {
    const _tagTree = await getTagOptions(TagType.Domain)
    tagOptions.value = _tagTree
  }
}

init()
</script>

<template>
  <el-cascader
    v-model="modelValue"
    :options="tagOptions"
    :props="{
      value: 'tagCode',
      label: 'tagName',
      children: 'child',
      checkStrictly: true,
      checkOnClickLeaf: false
    }"
    :teleported="teleported"
  />
</template>

<style lang="scss" scoped></style>
