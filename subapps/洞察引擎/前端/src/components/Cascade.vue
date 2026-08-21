<template>
  <el-select
    v-if="level >= 1"
    v-model="first"
    placeholder="请选择"
    clearable
    :style="{ width: firstWidth }"
    @clear="() => handleClear(1)"
    @change="(val) => handleChange(val as string, 1)"
  >
    <el-option v-for="item of firstList" :key="item.key" :label="item.value" :value="item.key" />
  </el-select>
  <el-select
    v-if="level >= 2"
    v-model="second"
    placeholder="请选择"
    clearable
    :style="{ width: secondWidth, 'margin-left': space }"
    @clear="() => handleClear(2)"
    @change="(val) => handleChange(val as string, 2)"
  >
    <el-option v-for="item of secondList" :key="item.key" :label="item.value" :value="item.key" />
  </el-select>
  <el-select
    v-if="level >= 3"
    v-model="third"
    placeholder="请选择"
    clearable
    :style="{ width: thirdWidth, 'margin-left': space }"
    @clear="() => handleClear(3)"
    @change="(val) => handleChange(val as string, 3)"
  >
    <el-option v-for="item of thirdList" :key="item.key" :label="item.value" :value="item.key" />
  </el-select>
</template>

<script lang="ts" setup>
const emits = defineEmits(['change'])
const props = withDefaults(
  defineProps<{
    firstList?: Record<string, any>[]
    space?: string
    firstWidth?: string
    secondWidth?: string
    thirdWidth?: string
    level?: number
  }>(),
  {
    space: '16px',
    firstWidth: '160px',
    secondWidth: '160px',
    thirdWidth: '160px',
    level: 3
  }
)
const first = defineModel<any>('first')
const second = defineModel<any>('second')
const third = defineModel<any>('third')

const secondList = ref<Record<string, any>[]>([])
const thirdList = ref<Record<string, any>[]>([])
const firstList = computed(() => {
  return props.firstList
})

const handleClear = (level: number) => {
  if (level === 1) {
    first.value = ''
    second.value = ''
    third.value = ''
    secondList.value = []
    thirdList.value = []
  } else if (level === 2) {
    second.value = ''
    third.value = ''
    thirdList.value = []
  } else {
    third.value = ''
  }
  emits('change', level)
}
const handleChange = (val: string, level: number, isClear = true) => {
  if (level === 1) {
    const result = firstList.value?.find(el => el.key === val)
    secondList.value = result?.children || []
    if (isClear) {
      second.value = ''
      third.value = ''
    }
  }
  if (level === 2) {
    const result = secondList.value?.find(el => el.key === val)
    thirdList.value = result?.children || []
    if (isClear) {
      third.value = ''
    }
  }

  emits('change', level)
}

watch(
  [first, firstList],
  ([fnval, listnval], []) => {
    if (fnval || listnval) {
      handleChange(fnval as string, 1, false)
    }
    if (second.value || listnval) {
      handleChange(second.value as string, 2, false)
    }
  },
  {
    deep: true,
    immediate: true
  }
)
</script>

<style lang="scss" scoped></style>
