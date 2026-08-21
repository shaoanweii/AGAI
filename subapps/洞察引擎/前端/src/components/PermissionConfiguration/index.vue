<script setup lang="ts">
import MenuGroup from '@/components/PermissionConfiguration/MenuGroup.vue'

const permissionIdList = defineModel()
const props = withDefaults(
  defineProps<{
    roleAuthTree: Api.Role.PermissionTree[]
  }>(),
  {}
)
const { roleAuthTree } = toRefs(props)
const roleTree = computed(() => (Array.isArray(roleAuthTree.value) ? roleAuthTree.value : []))

onMounted(() => {})

/**
 * 获取已选择的id
 * @param node
 * @param checkedIds
 */
const collectCheckedIds = (node: Api.Role.PermissionTree, checkedIds: string[]) => {
  if (node.checked) {
    checkedIds.push(node.id)
  }
  if (node.children) {
    node.children.forEach(child => collectCheckedIds(child, checkedIds))
  }
}

watch(
  roleTree,
  () => {
    if (roleTree.value.length > 0) {
      const checkedIds: string[] = []
      roleTree.value.forEach(node => {
        collectCheckedIds(node, checkedIds)
      })
      permissionIdList.value = checkedIds
    } else {
      permissionIdList.value = []
    }
  },
  {
    deep: true,
    immediate: true
  }
)

const checkedAll = ref(false)

// 半选状态
const indeterminate = computed(() => {
  if (roleTree.value.length === 0) return false
  const result = roleTree.value.filter(item => item.checked)
  const indeterminateResult = roleTree.value.filter(item => item.indeterminate)
  if (indeterminateResult.length > 0) {
    checkedAll.value = false
    return true
  }
  if (result?.length === roleTree.value.length) {
    checkedAll.value = true
    return false
  } else if (result?.length === 0) {
    checkedAll.value = false
    return false
  } else {
    checkedAll.value = false
    return true
  }
})

const refMap: any = {}
// 设置每个模块的ref
const setMenuGroupRef = (index: number) => {
  const key = `group-${index}Ref`
  if (!refMap[key]) {
    refMap[key] = ref(null)
  }
  return refMap[key]
}

// 全选/反选
const changeAllChecked = (val: boolean) => {
  roleTree.value.forEach((node, index) => {
    nextTick(() => {
      checkedAll.value = val
      refMap[`group-${index}Ref`].value?.[0]?.chenckedAllChange(val, node)
    })
  })
}
</script>

<template>
  <div class="flex flex-direction-column w-full" :data-testid="`PermissionConfiguration`">
    <el-checkbox
      v-model="checkedAll"
      :data-testid="`PermissionConfiguration-all`"
      :indeterminate="indeterminate"
      @change="(val: any) => changeAllChecked(val)"
      style="line-height: 32px"
      >全选</el-checkbox
    >
    <template v-for="(configItem, index) of roleTree" :key="configItem.id || index">
      <MenuGroup
        :ref="setMenuGroupRef(index)"
        :testid="`mg-${index}`"
        :auth="configItem"
        :config="configItem"
      ></MenuGroup>
    </template>
  </div>
</template>

<style lang="scss">
.check-box-group {
  margin-top: 20px;
  &:nth-child(2) {
    margin-top: 20px;
  }
}
.group {
  margin-top: 24px;
  &:nth-child(2) {
    margin-top: 20px;
  }
}
</style>
