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
      <!--<h4 class="fw-600">{{ form.operation == 'add' ? '新增' : form.operation == 'edit' ? '编辑' : '查看' }}数据源</h4>-->
      <h4 class="fw-600">{{ titleMapByType[viewStatus] }}</h4>
    </template>
    <template #default>
      <div class="body-wrapper">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="form">
          <el-form-item prop="tagType" label="标签类型">
            <!--<el-input v-model="form.name" :maxlength="10" placeholder="请输入"/>-->
            <el-select
              :data-testid="`application-10002`"
              v-model="form.tagType"
              placeholder="全部"
              clearable
              disabled
            >
              <el-option
                v-for="(item, index) in conditions.labelType"
                :key="index"
                :data-testid="`application-10002-${index}`"
                :label="item.value"
                :value="item.key"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="tagName" label="分类名称">
            <!--<el-input v-model="form.name" :maxlength="10" placeholder="请输入"/>-->
            <FtInput
              v-model.allTrim="form.tagName"
              :data-testid="`baseSetting-channel-form-10001`"
              :maxlength="10"
              clearable
              placeholder="请输入"
            />
          </el-form-item>
          <el-form-item prop="tagNameEn" label="英文名称">
            <el-input
              v-model.trim="form.tagNameEn"
              :maxlength="20"
              :data-testid="`baseSetting-channel-form-10002`"
              placeholder="请输入"
              clearable
            />
          </el-form-item>
          <el-form-item prop="tagParentId" label="所属分类">
            <el-cascader
              :data-testid="`baseSetting-channel-form-10003`"
              v-model="form.tagParentId"
              :options="transitionCategorizeTree"
              :props="{ value: 'id', label: 'tagName', children: 'child', checkStrictly: true }"
              style="width: 100%"
              placeholder="请选择"
              clearable
              :show-all-levels="false"
            />
          </el-form-item>
          <!-- <el-form-item v-if="type === 2" prop="status" label="启用状态">
            <el-radio-group :data-testid="`baseSetting-channel-form-10004`" v-model="form.status">
              <el-radio
                v-for="(item, index) of conditions.stopOrEnable"
                :key="index"
                :data-testid="`baseSetting-channel-form-10004-op-${index}`"
                :value="item.key"
                >{{ item.value }}
              </el-radio>
            </el-radio-group>
          </el-form-item> -->
        </el-form>
      </div>
    </template>

    <!-- 底部按钮区域 -->
    <template #footer>
      <div style="text-align: right">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleOk" :loading="submitting">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>
<script lang="ts" setup>
import { inject, nextTick } from 'vue'
import type { ConditionsDetailItem, ViewStatus } from '@/types'
import type { CategorizeItem } from '@/types/baseSeting.types'
import { ElMessage } from 'element-plus'
import { excludeNodeById } from '@/utils'
import useUserStore from '@/stores/modules/user'
import FtInput from '@/components/FtInput.vue'
import { debounce } from 'lodash-es'
import { saveTagLibClient, updateTagLibClient } from '@/api/tag'

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const emit = defineEmits(['refreshList'])
const props = withDefaults(
  defineProps<{
    viewStatus: ViewStatus
    categorizeTree: any
    editRecord: any
    filter: any
    type?: number
  }>(),
  {
    viewStatus: 'add',
    type: 1
  }
)

const { viewStatus, categorizeTree, editRecord, filter } = toRefs(props)

const visible = defineModel({ default: false })

const titleMapByType = {
  add: '新增分类',
  edit: '编辑分类',
  view: '查看分类'
}

const rules = ref({
  tagName: [
    { required: true, message: `请输入分类名称`, trigger: 'blur' },
    { min: 2, max: 10, message: '请输入2-10个字符', trigger: 'blur' }
  ],
  tagType: [{ required: true, message: '请选择标签类型', trigger: 'change' }],
  tagParentId: [
    {
      required: true,
      message: '请选择所属分类',
      trigger: 'change'
    }
  ]
})

const initializeData = (): any => ({
  id: undefined,
  appClient: '',
  tagType: '',
  tagName: '',
  tagNameEn: '',
  tagParentId: ''
})

let form = reactive<any>(initializeData())
const submitting = ref(false)

const transitionCategorizeTree = computed(() => {
  const list = categorizeTree.value.filter((item: CategorizeItem) => item.id !== '-1')
  if (viewStatus.value === 'add') {
    return list
  } else {
    // 编辑时需过滤自身及所有子节点
    return excludeNodeById(list, editRecord.value?.id)
  }
})

const addInit = () => {
  form.tagType = editRecord.value?.tagType || filter.value?.tagType
  form.appClient = useUserStore().clientId!
  if (editRecord.value?.id !== '-1') {
    form.tagParentId = editRecord.value.id
  }
}

const editInit = () => {
  Object.assign(form, editRecord.value)
  form.appClient = form.appClient ? form.appClient : useUserStore().clientId!
  if (editRecord.value.id !== '-1') {
    form.tagParentId =
      editRecord.value.tagParentId === '0' ? undefined : editRecord.value.tagParentId
  }
}
const handleOpen = () => {
  form.tagAttribute = 'Category'
  console.log('editRecord.value', editRecord.value)

  if (viewStatus.value === 'add') {
    addInit()
  } else if (viewStatus.value === 'edit') {
    editInit()
  }

  // 根据是否为根分类设置验证规则
  rules.value.tagParentId[0].required = editRecord.value?.id !== '-1'
  console.log('表单数据初始化完成:', form)
  console.log('验证规则:', rules.value)
}

const handleCancel = () => {
  visible.value = false
}

const handleClose = () => {
  // 重置表单数据
  Object.assign(form, initializeData())
  // 重置表单验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
  // 重置提交状态
  submitting.value = false
}

/**
 * 获取当前选择的分类是第几个层级
 */
// const findNodeLevelById = (tree: any, targetId: string, level = 0): any => {
//   for (const node of tree) {
//     if (node.id === targetId) {
//       return level + 1 // 加1是因为层级是从1开始计数的
//     }
//     if (node.child && node.child.length > 0) {
//       const foundLevel = findNodeLevelById(node.child, targetId, level + 1)
//       if (foundLevel !== undefined) {
//         return foundLevel
//       }
//     }
//   }
//   return 0 // 如果没有找到对应ID的节点，返回undefined
// }

const formRef = ref()
const handleOk = debounce(async () => {
  if (submitting.value) return

  try {
    submitting.value = true
    console.log('开始表单验证...')

    const valid = await formRef.value.validate()
    console.log('表单验证结果:', valid)

    if (valid) {
      console.log('通过校验，提交表单数据:', form)
      const api = viewStatus.value === 'add' ? saveTagLibClient : updateTagLibClient

      // 处理 tagParentId，如果为空字符串则设为 undefined
      const submitData = {
        ...form,
        tagParentId: form.tagParentId || undefined
      }

      console.log('提交的数据:', submitData)

      const res = await api(submitData)
      console.log('API 响应:', res)

      if (res.code === '200') {
        ElMessage.success(viewStatus.value === 'add' ? '新增成功' : '编辑成功')
        visible.value = false
        emit('refreshList')
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } else {
      console.log('表单验证失败')
    }
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error.message || '操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}, 300)
</script>

<style lang="scss" scoped>
.body-wrapper {
  padding: 12px 30px;
  padding-right: 100px;

  .upload-area {
    background-color: rgb(229, 241, 255);
    text-align: center;
    padding: 40px 60px;

    i {
      font-size: 24px;
      color: var(--color-primary);
    }

    p {
      color: var(--color-primary);
      margin-bottom: 8px;
    }

    span {
      font-size: 12px;
      color: var(--color-low);
    }
  }

  .sub-form {
    padding: 24px;
  }

  ::v-deep(.el-tabs) {
    .el-tabs-tab-active {
      background-color: var(--color-primary);

      .el-tabs-tab-title {
        color: #fff;
      }
    }

    .el-tabs-content {
      padding: 0;
    }
  }
}
</style>
