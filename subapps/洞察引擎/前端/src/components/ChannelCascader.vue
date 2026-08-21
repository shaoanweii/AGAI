<script setup lang="ts">
// 渠道查询控件
import { getGlobalChannelTreeByClientId } from '@/api/main'
import useUserStore from '@/stores/modules/user'
// import FtCascader from '@/components/FtCascader.vue'
import FtCascader from '@/components/FCascader/index.vue'

// 类型定义
interface CascaderFieldNames {
  value?: string
  label?: string
  children?: string
  disabled?: string
  leaf?: string
}

const props = withDefaults(
  defineProps<{
    testid?: string
    width?: string
    placeholder?: string
    controller?: string
    maxTagCount?: number
    multiple?: boolean
    fieldNames?: CascaderFieldNames
    formatLabel?: (options: any) => string
    subLength?: number
  }>(),
  {
    maxTagCount: 1,
    width: '350px',
    placeholder: '全部',
    multiple: false,
    fieldNames: () => ({ value: 'id', label: 'name', children: 'child' }),
    subLength: 2
  }
)
const { testid, maxTagCount, controller, width, fieldNames } = toRefs(props)
const emits = defineEmits(['getChannelOptions'])

const channel = defineModel<any>()
const options = ref<any[]>([])

let userStore = useUserStore()

const getChannelTree = async () => {
  try {
    // const temp = sessionStorage.getItem(`${controller.value}-${userStore.clientId}`)
    // if (temp) {
    //   options.value = JSON.parse(temp)
    //   return
    // }
    let clientId
    if (['commonDataBase'].includes(controller.value!)) {
      clientId = '0'
    } else {
      clientId = userStore.clientId
    }
    const result = await getGlobalChannelTreeByClientId(clientId, controller.value).then(
      res => res.result as any[]
    )
    // 过滤渠道信息
    if (
      result.length === 1 &&
      result[0].id === '-1' &&
      ['commonDataBase'].includes(controller.value!)
    ) {
      options.value = result[0]?.child || []
    } else {
      options.value = result
    }
    emits('getChannelOptions', options.value)
    // if (options.value?.length === 1)
    // if (options.value) {
    //   sessionStorage.setItem(`${controller.value}-${userStore.clientId}`, JSON.stringify(options.value))
    // }
  } catch (e) {
    options.value = []
  }
}

// useEmitt({
//   name: emittName.clientChange,
//   callback: (val: string) => {
//     getChannelTree()
//   },
// })

watch(
  () => userStore.clientId,
  () => {
    getChannelTree()
    // channel.value = undefined
  },
  {
    deep: true
  }
)

onMounted(() => {
  getChannelTree()
})
</script>

<template>
  <!--<el-cascader-->
  <!--  :data-testid="testid"-->
  <!--  v-model="channel"-->
  <!--  :options="options"-->
  <!--  :props="{value: 'id', label: 'name', children: 'child'}"-->
  <!--  clearable-->
  <!--  :max-collapse-tags="maxTagCount"-->
  <!--  :style="{width: width}"-->
  <!--  :placeholder="placeholder"-->
  <!--  :multiple="multiple"-->
  <!--  :format-label="formatLabel"-->
  <!--/>-->
  <!--v-if="options"-->
  <FtCascader
    :data-testid="testid"
    v-model="channel"
    :options="options"
    :field-names="fieldNames"
    clearable
    :subLength="subLength"
    :max-collapse-tags="maxTagCount"
    :style="{ width: width }"
    :placeholder="placeholder"
    :multiple="multiple"
    :format-label="formatLabel"
  ></FtCascader>
</template>

<style scoped lang="scss"></style>
