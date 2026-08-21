<template>
  <el-drawer :size="1200" v-model="form.visible" destroy-on-close>
    <template #header>
      {{ titleStr }}
    </template>
    <el-form ref="formRef" :rules="rules" :model="formData" :style="{ width: '800px' }">
      <el-form-item prop="tagCategory" label="所属分类" required>
        <SelectType
          v-model="formData.tagCategory"
          :data="conditions.categoryType"
          :disabled="isEidt"
          testid="discovery-form-10001-"
        ></SelectType>
      </el-form-item>
      <el-form-item prop="riskKeywords" label="风险关键词">
        <el-input
          v-model.trim="formData.riskKeywords"
          :data-testid="`discovery-form-10002`"
          :disabled="isEidt"
          :maxlength="50"
          clearable
          placeholder="请输入"
        />
      </el-form-item>
      <el-form-item label="扩展词">
        <el-input
          type="textarea"
          v-model.trim="formData.extendedWord"
          :data-testid="`discovery-form-10003`"
          clearable
          placeholder="请输入"
          :auto-size="{ minRows: 3 }"
        ></el-input>
      </el-form-item>
      <el-form-item prop="seriousLevel" label="严重性等级">
        <el-radio-group
          v-model="formData.seriousLevel"
          :data-testid="`discovery-form-10004`"
          default-value="Default"
        >
          <el-radio
            v-for="(item, index) of conditions.seriousness"
            :key="index"
            :data-testid="`discovery-form-10004-op-${index}`"
            :value="item.key"
            >{{ item.value }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="enableStatus" label="启用状态">
        <el-radio-group v-model="formData.enableStatus" :data-testid="`discovery-form-10005`">
          <el-radio
            v-for="(item, index) of conditions.stopOrEnable"
            :key="index"
            :data-testid="`discovery-form-10005-op-${index}`"
            :value="item.key"
            >{{ item.value }}
          </el-radio>
          <!--<el-radio value="已启用">启用</el-radio>-->
          <!--<el-radio value="已禁用">禁用</el-radio>-->
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
import type { Form } from '@/hooks/table.d'
import SelectType from '@/components/SelectType.vue'
import type { ConditionsDetailItem } from '@/types'
import { ElMessage } from 'element-plus'
import { addRisKeywords } from '@/api/discovery'
import useUserStore from '@/stores/modules/user'

const emit = defineEmits(['refreshList'])
const form = inject('form') as Form
const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
let formData = form.data
let titleStr = ref('')

const rules = reactive({
  tagCategory: [
    {
      required: true,
      message: '所属分类必填'
    }
  ],
  riskKeywords: [
    {
      required: true,
      message: '风险关键词必填'
    }
  ],
  seriousLevel: [
    {
      required: true,
      message: '严重性等级必填'
    }
  ],
  enableStatus: [
    {
      required: true,
      message: '启用状态必填'
    }
  ]
})

const isEidt = computed(() => {
  // 手动输入的可编辑
  return form.operation == 'edit' && formData.increaseType !== '2'
})

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
  console.log('formData', formData)
  formData.enableStatus = formData.enableStatus ?? '1'
  if (form.operation == 'add') {
    titleStr.value = '新增风险词'
  } else if (form.operation == 'edit') {
    titleStr.value = '编辑关键词'
  }
}

const handleCancel = () => {
  form.visible = false
}

const formRef = ref()
const handleOk = async () => {
  console.log('formData', formData)
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      console.log('通过校验')
      addRisKeywords(Object.assign({}, formData, { clientId: useUserStore().clientId }))
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
}
</script>
