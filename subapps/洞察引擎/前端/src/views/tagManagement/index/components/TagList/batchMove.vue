<template>
  <el-drawer
    v-model="visible"
    :size="1200"
    :data-testid="`baseSetting-channel-drawer`"
    destroy-on-close
    @open="handleOpen"
    @close="handleClose"
  >
    <template #header>
      <h4 class="fw-600">批量移动</h4>
    </template>
    <template #default>
      <div class="body-wrapper">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="form">
          <el-form-item prop="ids" label="已选标签">
            <el-cascader
              :data-testid="`baseSetting-channel-form-10003`"
              v-model="form.ids"
              :options="tagTree"
              :props="{
                value: 'id',
                label: 'tagName',
                children: 'child',
                checkStrictly: true,
                multiple: true
              }"
              :format-label="(options: any) => options.map((option: any) => option.tagName)?.join('#')"
              :max-collapse-tags="1"
              collapse-tags
              style="width: 100%"
              placeholder="请选择"
              clearable
              disabled
            />
          </el-form-item>
          <el-form-item prop="tagParentId" label="移动至分类">
            <el-cascader
              :data-testid="`baseSetting-channel-form-10003`"
              v-model="form.tagParentId"
              :options="categoryTree"
              :props="{ value: 'id', label: 'tagName', children: 'child', checkStrictly: true }"
              :format-label="(options: any) => options.map((option: any) => option.tagName)?.join('#')"
              filterable
              style="width: 100%"
              placeholder="请选择"
              clearable
            />
          </el-form-item>
        </el-form>
      </div>
    </template>

    <!-- 底部按钮区域 -->
    <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleOk">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>
<script lang="ts" setup>
import {
  batchMoveTagLibClient,
  findTagLibClientCategoryTree,
  findTagLibClientTree
} from '@/api/tag'
import useUserStore from '@/stores/modules/user'
// import { excludeNodeById } from '@/utils'
import { ElMessage } from 'element-plus'
import to from 'await-to-js'
import { debounce } from 'lodash-es'

const emit = defineEmits(['refreshList'])

const { selectedKeys, filter, curCategorize } = defineProps<{
  allFinalTagLib: Record<any, any> | undefined
  selectedKeys: any
  filter: any
  curCategorize: any
}>()

const visible = defineModel({ default: false })
const userStore = useUserStore()
const form = ref({
  tagParentId: '',
  ids: []
})

const rules = ref({
  ids: [
    {
      required: true
    }
  ],
  tagParentId: [
    {
      required: true,
      message: '请选择所属分类'
    }
  ]
})

// const transitionCategorizeTree = computed(() => {
//   return []
//   //   const list = categorizeTree.value.filter((item: CategorizeItem) => item.id !== '-1')
//   //   if (viewStatus.value === 'add') {
//   //     return list
//   //   } else {
//   //     // 编辑时需过滤自身及所有子节点
//   //     // return excludeNodeById(list, editRecord.value?.id)
//   //   }
// })

const categoryTree = ref<Record<any, any>[]>([])
// const allFinalTagLib = ref<Record<any, any>[]>([])

// 获取所有分类
const getCategoryTree = async () => {
  const [errs, data] = await to(findTagLibClientCategoryTree(userStore.clientId))
  if (errs) {
    ElMessage.error(errs.message)
  } else {
    categoryTree.value = data.result
  }
}

const tagTree = ref<any>()
const getTagTree = async () => {
  const [errs, data] = await to(
    findTagLibClientTree({
      appClient: userStore.clientId,
      tagType: curCategorize.tagType || filter?.tagType
    })
  )
  if (errs) {
    ElMessage.error(errs.message)
  } else {
    tagTree.value = data.result
  }
}

const handleOpen = () => {
  form.value.ids = selectedKeys
  // getFinalTagLib()
  getCategoryTree()
  getTagTree()
}

const handleCancel = () => {
  visible.value = false
}
const handleClose = () => {
  form.value.tagParentId = ''
}

const formRef = ref()
const handleOk = debounce(async () => {
  formRef.value.validate((errs: any) => {
    console.log('errs', errs)
    if (!errs) {
      console.log('通过校验')
      batchMoveTagLibClient({
        appClient: userStore.clientId,
        ids: form.value.ids,
        tagParentId: form.value.tagParentId
      })
        .then(res => {
          if (res.code === '200') {
            visible.value = false
            emit('refreshList')
          } else {
            ElMessage.error(res.message)
          }
        })
        .catch((err: any) => {
          ElMessage.error(err.message)
        })
    }
  })
}, 300)
</script>

<style lang="scss" scoped>
.body-wrapper {
  padding: 12px 30px;
  padding-right: 100px;
}
</style>
