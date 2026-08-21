<template>
  <el-drawer v-model="form.visible" :size="1200" @open="open" destroy-on-close>
    <template #header>
      <h4 class="fw-600">
        {{ form.operation == 'add' ? '新增' : form.operation == 'edit' ? '编辑' : '查看' }}资源组
      </h4>
    </template>
    <template #default>
      <div class="body-wrapper">
        <el-form ref="formRef" :model="form.data" :rules="rules" label-width="120px" class="form">
          <el-form-item prop="name" label="资源组名称">
            <el-input
              v-model.trim="form.data.name"
              placeholder="请输入"
              :maxlength="50"
              :data-testid="`founding-index-group-form-10002`"
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
import { insertGroup, updateGroup } from '@/api/dataCenter'
import { ElMessage } from 'element-plus'
import useUserStore from '@/stores/modules/user'

const emit = defineEmits(['refreshList'])

const formRef = ref()

const form = inject('form') as Form<{
  name: string
  customer: string
  type: string
}>

let rules = reactive({
  name: [{ required: true, message: '请输入资源组名称' }]
})

const userStore = useUserStore()

const open = () => {
  console.log('open', form)
  if (form.operation === 'add') {
    // 初始化表单数据
    form.data = {
      name: '',
      customer: userStore.clientId,
      type: ''
    }
  }
  console.log('open 后的 form.data:', form.data)
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
      insertGroup(form.data)
        .then(res => {
          console.log('新增响应:', res)
          if (res.code == '200') {
            ElMessage.success(res.message)
            refreshList()
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
      updateGroup(form.data)
        .then((res: any) => {
          console.log('编辑响应:', res)
          if (res.code == '200') {
            ElMessage.success(res.message)
            refreshList()
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

const refreshList = () => {
  emit('refreshList')
}
</script>

<style lang="scss">
.body-wrapper {
  padding: 12px 30px;
  padding-right: 100px;
}
</style>
