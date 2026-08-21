<template>
  <div class="cn-and-en-map-wrapper">
    <template v-for="(item, index) of tags" :key="index">
      <div class="item">
        <el-input v-model="item.name" placeholder="请输入中文名称" clearable style="width: 160px" />
        <el-input
          v-model="item.nameEn"
          placeholder="请输入英文名称"
          clearable
          class="ml16"
          style="width: 255px"
        />
        <el-button
          v-if="Number(index) !== 0"
          type="text"
          class="ml24"
          @click="handleDelete(Number(index))"
        >
          {{ delBtnText }}
        </el-button>
      </div>
    </template>
    <el-button class="add" type="text" @click="handleAdd">{{ addBtnText }}</el-button>
  </div>
</template>

<script lang="ts" setup>
defineProps({
  addBtnText: {
    type: String,
    default: '新增标签'
  },
  delBtnText: {
    type: String,
    default: '删除'
  }
})

const tags: any = defineModel()
// const tags = ref([
//   {
//     name: '',
//     nameEn: ''
//   }
// ])

onMounted(() => {
  if (!tags.value?.length) {
    tags.value = [{ name: '', nameEn: '' }]
  }
})

const handleAdd = () => {
  tags.value?.push({
    name: '',
    nameEn: ''
  })
}
const handleDelete = (index: number) => {
  if (tags.value?.length === 1) return
  tags.value?.splice(index, 1)
}
</script>

<style lang="scss" scoped>
.cn-and-en-map-wrapper {
  .ml16 {
    margin-left: 16px;
  }
  .ml24 {
    margin-left: 24px;
  }
  .item {
    display: flex;
    margin-bottom: 24px;
  }
  /* .add {
  } */
}
</style>
