<template>
  <el-drawer
    :size="1200"
    v-model="form.visible"
    class="app-drawer-wrapper"
    @open="open"
    @close="onClose"
    destroy-on-close
  >
    <template #header>
      {{ titleStr }}
    </template>
    <el-form ref="formRef" :rules="rules" :model="formData" :style="{ width: '884px' }">
      <el-form-item prop="tagType" label="标签类型">
        <el-select
          :data-testid="`application-form-10002`"
          v-model="formData.tagType"
          placeholder="请选择"
          clearable
          style="width: 100%"
          disabled
          @change="(val: any) => tagTypeChange(val)"
        >
          <el-option
            v-for="(item, index) of conditions.labelType"
            :data-testid="`application-form-10002-${index}`"
            :key="item.key"
            :label="item.value"
            :value="item.key"
          />
        </el-select>
      </el-form-item>

      <template v-if="formData.tagAttribute === 'FinalLabel'">
        <el-form-item prop="tagName" label="标签名称">
          <el-input
            :data-testid="`application-form-10003`"
            v-model.trim="formData.tagName"
            clearable
            :maxlength="50"
            placeholder="请输入"
          />
        </el-form-item>
        <el-form-item prop="tagNameEn" label="英文名称">
          <el-input
            :data-testid="`application-form-10004`"
            v-model.trim="formData.tagNameEn"
            clearable
            :maxlength="50"
            placeholder="请输入"
          />
        </el-form-item>
        <el-form-item prop="tagParentId" label="所属分类">
          <el-cascader
            :data-testid="`application-form-10005`"
            v-model="formData.tagParentId"
            :options="categoryTree"
            :props="{ value: 'id', label: 'tagName', children: 'child', checkStrictly: true }"
            :format-label="(options: any) => options.map((option: any) => option.tagName)?.join('#')"
            style="width: 100%"
            placeholder="全部"
          />
        </el-form-item>
        <el-form-item prop="tagDescription" label="标签定义">
          <el-input
            type="textarea"
            :data-testid="`application-form-10006`"
            v-model.trim="formData.tagDescription"
            show-word-limit
            :maxlength="1024"
            clearable
            placeholder="请输入相关描述说明"
            :autosize="{
              minRows: 5
            }"
          ></el-input>
          <template #extra>
            <div class="flex item-center">
              <i class="iconfont icon-information-line1" style="font-size: 16px"></i>
              <span class="ml-8"
                >标签定义便于模型更好的理解，确保标签更准确进行处理识别，1024个字符内</span
              >
            </div>
          </template>
        </el-form-item>
        <!--关联配置项-->
        <FormItemByTagType
          testid="application-form"
          :formData="formData"
          :configItem="configItem"
          :conditions="conditions"
        ></FormItemByTagType>
        <el-form-item prop="tagStatus" label="是否启用">
          <el-radio-group :data-testid="`application-form-10007`" v-model="formData.tagStatus">
            <el-radio
              v-for="(item, index) of conditions.stopOrEnable"
              :key="index"
              :data-testid="`application-form-10007-${index}`"
              :value="item.key"
              >{{ item.value }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </template>
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
import type { Form } from '@/hooks/table.d'
import {
  findTagLibClientCategoryTree,
  findTagLibRelatedItems,
  saveTagLibClient,
  updateTagLibClient
} from '@/api/tag'
import { debounce } from 'lodash-es'
import FormItemByTagType from '@/components/tag/FormItemByTagType.vue'
import { ElMessage } from 'element-plus'
import useUserStore from '@/stores/modules/user'

const { curCategorizeByParent, filter } = defineProps<{
  curCategorizeByParent: Record<any, any> | undefined
  filter: any
}>()

const emit = defineEmits(['refreshList'])
const form = inject('form') as Form
const conditions = inject('conditions') as Record<string, any>
let formData = form.data
let titleStr = ref('')
let userStore = useUserStore()

/**
 * 校验规则
 */
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
  tagAttribute: [
    {
      required: true,
      message: '标签属性必填'
    }
  ],
  tagName: [
    {
      required: true,
      message: '标签名称必填'
    }
  ],
  tagParentId: [
    {
      required: true,
      message: '所属分类必填'
    }
  ],
  energyType: [
    {
      required: true,
      message: '关联能源分类必填'
    }
  ],
  carType: [
    {
      required: true,
      message: '关联车辆类型必填'
    }
  ],
  seriousness: [
    {
      required: true,
      message: '严重性等级必填'
    }
  ],
  userJourney: [
    {
      required: true,
      message: '关联用户旅程必填'
    }
  ],
  tagStatus: [
    {
      required: true,
      message: '启用状态必填'
    }
  ]
})

/**
 * 分类树
 */
const categoryTree = ref<Record<string, any>[]>([])
const getCategoryTreeByTagType = async () => {
  try {
    categoryTree.value = await findTagLibClientCategoryTree(
      userStore.clientId,
      formData.tagType
    ).then(res => res.result)
  } catch (e: any) {
    ElMessage.error(e.message)
    categoryTree.value = []
  }
}

/**
 * 关联配置项
 */
const configItem = ref({})
const getConfigByTagType = async () => {
  try {
    configItem.value = await findTagLibRelatedItems({
      tagType: formData.tagType
    }).then(res => res.result)
  } catch (e: any) {
    ElMessage.error(e.message)
    configItem.value = {}
  }
}

const tagTypeChange = (val: string, isInit: boolean = true) => {
  if (isInit) {
    formData.tagParentId = ''
  }
  getCategoryTreeByTagType()
  getConfigByTagType()
}

watch(
  () => form.visible,
  (nv, ov) => {
    if (nv && !ov) {
      initializeFormData()
    }
  }
)

const initializeFormData = () => {
  formData = reactive({ ...form.data })
  formData.tagStatus = formData.tagStatus ?? '1'
  formData.tagAttribute = 'FinalLabel'
  if (form.operation == 'add') {
    titleStr.value = '新增标签'
    formData.tagType = curCategorizeByParent?.tagType || filter.tagType
    if (curCategorizeByParent?.tagParentId !== '-1') {
      formData.tagParentId = curCategorizeByParent?.id
    }
  } else if (form.operation == 'edit') {
    titleStr.value = '编辑标签'
  }
  tagTypeChange('', false)
}

const open = async () => {}

const onClose = () => {}

const handleCancel = () => {
  form.visible = false
}

const formRef = ref()
const handleOk = debounce(async () => {
  try {
    const valid = await formRef.value.validate()
    console.log('valid', valid)
    if (valid) {
      console.log('通过校验')
      const api = form.operation == 'add' ? saveTagLibClient : updateTagLibClient
      formData.tagParentId = formData.tagParentId ? formData.tagParentId : undefined
      formData.appClient = userStore.clientId
      api(formData)
        .then(res => {
          if (res.code === '200') {
            form.visible = false
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
}, 300)
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
