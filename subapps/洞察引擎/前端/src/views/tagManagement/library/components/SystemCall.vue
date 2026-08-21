<template>
  <el-drawer
    :size="1200"
    v-model="visible"
    class="app-drawer-wrapper"
    @open="open"
    @close="onClose"
    destroy-on-close
  >
    <template #header>
      <span>系统调用标签</span>
    </template>
    <el-form ref="formRef" :rules="rules" :model="formData" auto-label-width>
      <!--<el-form-item prop="appClient" label="应用客户" required :style="{ width: '884px' }">-->
      <!--  <el-select :data-testid="`application-sys-call-10001`" v-model="formData.appClient" placeholder="请选择"-->
      <!--            clearable style="width: 100%" @change="(val: any) => appClientChange(val)">-->
      <!--    <el-option v-for="(item, index) of conditions.client" :data-testid="`application-sys-call-10001-${index}`"-->
      <!--              :key="item.key" :label="item.value" :value="item.key"/>-->
      <!--  </el-select>-->
      <!--</el-form-item>-->
      <el-form-item prop="tagType" label="标签类型" required :style="{ width: '884px' }">
        <el-select
          :data-testid="`application-sys-call-10002`"
          v-model="formData.tagType"
          placeholder="请选择"
          clearable
          style="width: 100%"
          @change="tagTypeChange"
        >
          <el-option
            v-for="(item, index) of conditions.labelType"
            :data-testid="`application-sys-call-10002-${index}`"
            :key="item.key"
            :label="item.value"
            :value="item.key"
          />
        </el-select>
      </el-form-item>
      <el-form-item prop="tagParentIds" label="调用标签" required :style="{ width: '884px' }">
        <el-cascader
          :data-testid="`application-sys-call-10003`"
          v-model="formData.tagParentIds"
          :options="categoryTree"
          :props="{ value: 'id', label: 'tagName', children: 'child' }"
          :format-label="(options: any) => options.map((option: any) => option.tagName)?.join('#')"
          multiple
          :max-collapse-tags="1"
          style="width: 100%"
          placeholder="全部"
          @change="(val: any) => tagParentIdsChange(val)"
        />
      </el-form-item>
      <el-form-item prop="tagStatus" label="是否启用" required :style="{ width: '884px' }">
        <el-radio-group :data-testid="`application-sys-call-10004`" v-model="formData.tagStatus">
          <el-radio
            v-for="(item, index) of conditions.stopOrEnable"
            :key="index"
            :data-testid="`application-sys-call-10004-${index}`"
            :value="item.key"
            >{{ item.value }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <!-- 底部按钮区域 -->
    <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleOk">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>
<script setup lang="ts">
import { inject } from 'vue'
import { copyTagLibClient, findCalledTagLibClient, findTagLibTree } from '@/api/tag'
import { ElMessage } from 'element-plus'

const props = defineProps({
  appClient: {
    type: String,
    default: ''
  }
})
const { appClient } = toRefs(props)

const emit = defineEmits(['refreshList'])
const visible = defineModel({ required: true, default: false })
const conditions = inject('conditions') as Record<string, any>
let formData = reactive({
  appClient: '',
  tagType: '',
  tagParentIds: [],
  tagStatus: ''
})

const resetFormData = () => {
  formData.appClient = ''
  formData.tagType = ''
  formData.tagParentIds = []
  formData.tagStatus = ''
}

const rules = reactive({
  appClient: [
    {
      required: true,
      message: '应用客户必填'
    }
  ],
  tagType: [
    {
      required: true,
      message: '标签类型必填'
    }
  ],
  tagParentIds: [
    {
      required: true,
      message: '所属分类必填'
    }
  ],
  tagStatus: [
    {
      required: true,
      message: '启用状态必填'
    }
  ]
})

const categoryTree = ref<Record<string, any>[]>([])
const getCategoryTreeByTagType = async () => {
  if (!formData.tagType) return
  try {
    categoryTree.value = await findTagLibTree(formData.tagType).then(res => res.result)
    await findCalledTagByClientId()
  } catch (e: any) {
    ElMessage.error(e.message)
    categoryTree.value = []
  }
}

const tagTypeChange = () => {
  formData.tagParentIds = []
  getCategoryTreeByTagType()
}
// const appClientChange = (val: any) => {
//   formData.tagParentIds = []
//   findCalledTagByClientId()
// }

const initializeFormData = () => {
  formData.tagStatus = '1'
  formData.appClient = appClient.value
  formData.tagType = conditions.labelType?.[0].key
  console.log('formData', formData)
  getCategoryTreeByTagType()
}

/**
 * 递归处理调用标签禁用选项
 * @param tree
 * @param disabledList
 */
const transitionCategoryTree = (tree: any, disabledList: string[]) => {
  tree.forEach((el: any) => {
    if (disabledList.includes(el.id)) {
      el.disabled = true
    } else {
      if (el.child && el.child?.length > 0) {
        transitionCategoryTree(el.child, disabledList)
      }
    }
  })
}

const tagParentIdsdisabledList = ref<any[]>([])
// 获取已被当前客户调用过的标签
const findCalledTagByClientId = async () => {
  try {
    const response = await findCalledTagLibClient({
      appClient: formData.appClient,
      tagType: formData.tagType
    })
    if (response.code === '200') {
      tagParentIdsdisabledList.value = response.result || []
      formData.tagParentIds = response.result || []
      transitionCategoryTree(categoryTree.value, response.result)
    }
  } catch (e: any) {
    tagParentIdsdisabledList.value = []
    formData.tagParentIds = []
    ElMessage.error(e.message)
  }
}

const tagParentIdsChange = (val: any) => {
  formData.tagParentIds = Object.assign([], val, tagParentIdsdisabledList.value as any)
}

const open = async () => {
  initializeFormData()
}

const onClose = () => {
  resetFormData()
}

const handleCancel = () => {
  visible.value = false
}

const formRef = ref()
const handleOk = async () => {
  try {
    const valid = await formRef.value.validate()
    console.log('valid', valid)
    if (valid) {
      console.log('通过校验')
      const params = Object.assign({}, formData, {
        tagParentIds: formData.tagParentIds.filter(
          el => !tagParentIdsdisabledList.value.includes(el)
        )
      })
      copyTagLibClient(params)
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
  } catch (error) {
    console.log('表单验证失败:', error)
  }
}
</script>

<style lang="scss">
.app-drawer-wrapper {
  .el-drawer-body {
    padding: 12px 16px 12px 40px;
  }

  .line {
    width: 1120px;
    border-bottom: 1px dashed #e5e6eb;
    margin: 0 0 20px;
    box-sizing: border-box;
  }
}

.app-trigger-wrapper {
  background-color: #fff;
  width: 469px;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.25);
  box-sizing: border-box;
  min-height: 322px;
}
</style>
