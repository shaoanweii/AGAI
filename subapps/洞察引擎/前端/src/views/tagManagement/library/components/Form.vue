<template>
  <el-drawer :size="1200" v-model="form.visible" destroy-on-close>
    <template #header>
      {{ titleStr }}
    </template>
    <el-form ref="formRef" :rules="rules" :model="formData" :style="{ width: '884px' }">
      <el-form-item prop="tagType" label="标签类型">
        <el-select
          :data-testid="`library-form-10001`"
          v-model="formData.tagType"
          placeholder="全部"
          style="width: 100%"
          @change="(val: any) => tagTypeChange(val)"
        >
          <el-option
            v-for="(item, index) in conditions.labelType"
            :key="index"
            :data-testid="`library-form-10001-${index}`"
            :label="item.value"
            :value="item.key"
          />
        </el-select>
      </el-form-item>
      <el-form-item prop="tagAttribute" label="标签属性">
        <SelectType
          v-model="formData.tagAttribute"
          :data="conditions.tagLibAttribute"
          testid="library-form-"
          default-actice="FinalLabel"
        ></SelectType>
      </el-form-item>
      <template v-if="formData.tagAttribute === 'FinalLabel'">
        <el-form-item prop="tagName" label="标签名称">
          <el-input
            :data-testid="`library-form-10002`"
            v-model.trim="formData.tagName"
            :maxlength="50"
            clearable
            placeholder="请输入"
          />
        </el-form-item>
        <el-form-item prop="tagNameEn" label="英文名称">
          <el-input
            :data-testid="`library-form-10003`"
            v-model.trim="formData.tagNameEn"
            :maxlength="50"
            clearable
            placeholder="请输入"
          />
        </el-form-item>
        <el-form-item prop="tagParentId" label="所属分类">
          <el-cascader
            :data-testid="`library-form-10004`"
            v-model="formData.tagParentId"
            :options="categoryTree"
            :props="{ value: 'id', label: 'tagName', children: 'child', checkStrictly: true }"
            style="width: 100%"
            placeholder="全部"
            :show-all-levels="false"
          />
        </el-form-item>
        <el-form-item prop="tagDescription" label="标签定义">
          <el-input
            type="textarea"
            :data-testid="`library-form-10005`"
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
          testid="library-form-"
          :formData="formData"
          :configItem="configItem"
          :conditions="conditions"
        ></FormItemByTagType>
        <el-form-item prop="tagStatus" label="是否启用">
          <el-radio-group :data-testid="`library-form-10006`" v-model="formData.tagStatus">
            <el-radio
              v-for="(item, index) of conditions.stopOrEnable"
              :key="index"
              :data-testid="`library-form-10006-${index}`"
              :value="item.key"
              >{{ item.value }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </template>
      <template v-else-if="formData.tagAttribute === 'Category'">
        <el-form-item prop="tagName" label="分类名称" required>
          <el-input
            :data-testid="`library-form-10007`"
            v-model.trim="formData.tagName"
            :maxlength="50"
            clearable
            placeholder="请输入"
          />
        </el-form-item>
        <el-form-item label="英文名称">
          <el-input
            :data-testid="`library-form-10008`"
            v-model.trim="formData.tagNameEn"
            :maxlength="50"
            clearable
            placeholder="请输入"
          />
        </el-form-item>
        <el-form-item label="所属分类">
          <el-cascader
            :data-testid="`library-form-10009`"
            v-model="formData.tagParentId"
            :options="categoryTree"
            :props="{ value: 'id', label: 'tagName', children: 'child' }"
            :format-label="(options: any) => options.map((option: any) => option.tagName)?.join('#')"
            check-strictly
            style="width: 100%"
            placeholder="全部"
          />
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
import SelectType from '@/components/SelectType.vue'
import type { ConditionsDetailItem } from '@/types'
import FormItemByTagType from '@/components/tag/FormItemByTagType.vue'
import { findTagLibCategoryTree, findTagLibRelatedItems, saveTagLib, updateTagLib } from '@/api/tag'
import { debounce } from 'lodash-es'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['refreshList'])
const form = inject('form') as Form
const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
let formData = form.data
let titleStr = ref('')

const rules = reactive({
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

watch(
  () => form.visible,
  (nv, ov) => {
    if (nv && !ov) {
      initializeFormData()
    }
  }
)

const categoryTree = ref<Record<string, any>[]>([])
const getCategoryTreeByTagType = async () => {
  try {
    categoryTree.value = await findTagLibCategoryTree(formData.tagType).then(res => res.result)
  } catch (e: any) {
    ElMessage.error(e.message)
    categoryTree.value = []
  }
}
const configItem = ref({})
const getConfigByTagType = async () => {
  try {
    configItem.value = await findTagLibRelatedItems({ tagType: formData.tagType }).then(
      res => res.result
    )
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

const initializeFormData = () => {
  formData = reactive({ ...form.data })
  console.log('formData---initializeFormData', formData)
  formData.tagStatus = formData.tagStatus ?? '1'
  formData.tagAttribute = 'FinalLabel'
  if (form.operation == 'add') {
    titleStr.value = '新增标签'
    formData.tagType = 'BIZ'
  } else if (form.operation == 'edit') {
    titleStr.value = '编辑标签'
  }
  tagTypeChange('', false)
}

const handleCancel = () => {
  form.visible = false
}

const formRef = ref()
const handleOk = debounce(async () => {
  console.log('formData', formData)
  try {
    const valid = await formRef.value.validate()
    console.log('valid', valid)
    if (valid) {
      console.log('通过校验')
      const api = form.operation == 'add' ? saveTagLib : updateTagLib
      formData.tagParentId = formData.tagParentId ? formData.tagParentId : undefined
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
