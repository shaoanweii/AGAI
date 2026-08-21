<script setup lang="ts">
import { queryRoleALlList } from '@/api/user'
import SceneLeft from '@views/system/scene/scene/components/SceneLeft.vue'
import SceneRight from '@views/system/scene/scene/components/SceneRight.vue'
import { provide, ref } from 'vue'

const sceneRightRef = ref()
const roleOptions = ref<any>([])
//获取角色类型列表数据
const getRoleList = async () => {
  const response = await queryRoleALlList({})
  if (response.success) {
    roleOptions.value = response.result?.filter((el: any) => el.roleType?.toString() === '1') || []
  }
}

const init = async () => {
  await getRoleList()
}

init()

provide('roleOptions', roleOptions)

// 点击分类
const classifyItemClick = (item: any) => {
  console.log('classifyItemClick', item)
  if (sceneRightRef.value) {
    sceneRightRef.value.leftChange(item)
  }
}
</script>
<template>
  <div class="page-container flex-col h-full">
    <el-card class="table-card h-full flex-col" shadow="never">
      <div class="flex h-full">
        <SceneLeft @classify-item-click="classifyItemClick" class="left-class pr-24"></SceneLeft>
        <SceneRight ref="sceneRightRef" class="flex-1 h-full"></SceneRight>
      </div>
    </el-card>
  </div>
</template>
<style lang="scss" scoped>
.left-class {
  width: 423px;
  border-right: 1px solid $border-dark;
}
:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}
</style>
