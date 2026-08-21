<script setup lang="ts">
import useUserStore from '@/stores/modules/user'
import { emittName, useEmitt } from '@/hooks/useEmitt'

const props = withDefaults(
  defineProps<{
    placeholder?: string
    width?: string
    testid?: string
    size?: 'mini' | 'medium' | 'large' | 'small' | undefined
    showStandard?: boolean
  }>(),
  {
    placeholder: '全部',
    width: '160px',
    testid: 'custom-option',
    size: 'small',
    showStandard: false
  }
)
const { size, testid, width, placeholder, showStandard } = toRefs(props)
const userStore = useUserStore()
userStore.setCilenId(userStore.defaultClientId!)

const emit = defineEmits(['change'])

const clientId = defineModel<any>()

watchEffect(() => {
  // 设置客户默认选项
  clientId.value = userStore.clientId
})

const clientChange = (val: string) => {
  userStore.setCilenId(val)
  useEmitt().emitter.emit(emittName.clientChange, val)
  emit('change', val)
}

const options = computed(() => {
  if (showStandard.value) {
    return userStore.clientIds
  } else {
    return userStore.clientIds?.filter((item: any) => item.key !== '0')
  }
})
</script>

<template>
  <el-select
    v-if="userStore.isAdmin"
    :data-testid="`${testid}`"
    v-model="clientId"
    :placeholder="placeholder"
    :style="{ width: width }"
    :size="size"
    @change="(val: any) => clientChange(val)"
  >
    <el-option
      v-for="(item, index) in options"
      :key="index"
      :data-testid="`${testid}-ol-${index}`"
      :label="item.value"
      :value="item.key"
    />
  </el-select>
</template>

<style scoped lang="scss"></style>
