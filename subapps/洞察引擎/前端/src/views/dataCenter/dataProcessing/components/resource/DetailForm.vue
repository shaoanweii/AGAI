<template>
  <el-drawer v-model="form.visible" :size="1200" @open="handleOpen" destroy-on-close>
    <template #header>
      <h4 class="fw-600">
        {{ form.operation === 'add' ? '新增' : form.operation === 'edit' ? '编辑' : '查看' }}数据
      </h4>
    </template>
    <template #default>
      <div class="body-wrapper">
        <el-form
          ref="formRef"
          :model="form.data"
          :rules="rules"
          label-width="120px"
          class="form"
          :style="{ width: '884px' }"
        >
          <el-form-item prop="resourceId" label="资源组名称">
            <!--<el-select-->
            <!--  v-model="form.data.customer"-->
            <!--  placeholder="全部"-->
            <!--  clearable-->
            <!--  :disabled="form.operation === 'edit'"-->
            <!--  style="width: 112px"-->
            <!--  :data-testid="`founding-index-detail-form-40001`"-->
            <!--  @change="(val: any) => customerChange(val)"-->
            <!--&gt;-->
            <!--  <el-option-->
            <!--    v-for="(item, index) in conditions.appClient"-->
            <!--    :label="item.value"-->
            <!--    :value="item.key"-->
            <!--    :data-testid="`founding-index-detail-form-40001-${index}`"-->
            <!--  />-->
            <!--</el-select>-->
            <!--style="width: 672px; margin-left: 16px;"-->
            <el-select
              v-model="form.data.resourceId"
              placeholder="全部"
              :disabled="form.operation === 'edit'"
              clearable
              :data-testid="`founding-index-detail-form-40002`"
            >
              <el-option
                v-for="(item, index) in resourceOptions"
                :key="index"
                :label="item.name"
                :value="item.id"
                :data-testid="`founding-index-detail-form-40002-${index}`"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="name" label="数据详情">
            <el-input
              v-model.trim="form.data.name"
              placeholder="请输入"
              :maxlength="50"
              :data-testid="`founding-index-detail-form-40003`"
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
import { inject } from 'vue'
import type { Form } from '@/hooks/table.d'
import { findResourceGroupByAppClient, insertDesc, updateDesc } from '@/api/dataCenter'
import { ElMessage } from 'element-plus'
// import type { ConditionsDetailItem } from '@/types'

// const conditions = inject('insDataResourceConditions') as Record<string, ConditionsDetailItem[]>
const emit = defineEmits(['refreshDetailList'])

const form = inject('form') as Form<{
  resourceId: string | undefined
  customer: string | undefined
  name: string | undefined
}>

const formRef = ref()

const props = defineProps({
  filter: Object
})

const resourceOptions = ref<Record<string, any>[]>([])

const getResourceData = (customer?: string) => {
  if (!customer) return
  findResourceGroupByAppClient({ customer })
    .then(res => {
      if (res.code === '200') {
        resourceOptions.value = res.result as any
      } else {
        resourceOptions.value = []
      }
    })
    .catch(() => {
      resourceOptions.value = []
    })
}

// const customerChange = (val: string) => {
//   form.data.resourceId = ''
//   getResourceData(val)
// }

const handleOpen = () => {
  console.log('handleOpen', form.operation, form.data)
  if (form.operation == 'add') {
    // 初始化表单数据
    form.data = {
      resourceId: props.filter?.resourceId || '',
      customer: props.filter?.customer || '',
      name: ''
    }
    if (props.filter?.customer) {
      getResourceData(props.filter?.customer)
    }
  } else if (form.operation == 'edit') {
    getResourceData(form.data.customer)
  }
  console.log('handleOpen 后的 form.data:', form.data)
}

const rules = {
  resourceId: [{ required: true, message: '请输入资源组名称' }],
  name: [{ required: true, message: '请输入数据详情' }]
}

const handleCancel = () => {
  form.visible = false
}

const handleOk = async () => {
  console.log('handleOk 被调用', form.data)
  try {
    await formRef.value.validate()
    console.log('表单验证通过', form.data)
    // 验证通过，执行提交逻辑
    if (form.operation == 'add') {
      console.log('执行新增操作', form.data)
      insertDesc(form.data)
        .then(res => {
          console.log('新增响应:', res)
          if (res.code == '200') {
            ElMessage.success(res.message)
            refreshDetailList()
            form.visible = false
          } else {
            ElMessage.error(res.message)
          }
        })
        .catch(err => {
          console.error('新增失败:', err)
          ElMessage.error(err.message || '操作失败')
        })
    } else if (form.operation == 'edit') {
      console.log('执行编辑操作', form.data)
      updateDesc(form.data)
        .then((res: any) => {
          console.log('编辑响应:', res)
          if (res.code == '200') {
            ElMessage.success(res.message)
            refreshDetailList()
            form.visible = false
          } else {
            ElMessage.error(res.message)
          }
        })
        .catch(err => {
          console.error('编辑失败:', err)
          ElMessage.error(err.message || '操作失败')
        })
    }
  } catch (error) {
    // 验证失败，不执行提交逻辑
    console.log('表单验证失败:', error)
  }
}

const refreshDetailList = () => {
  emit('refreshDetailList')
}
</script>

<style lang="scss">
.body-wrapper {
  padding: 12px 8px;
}
</style>
