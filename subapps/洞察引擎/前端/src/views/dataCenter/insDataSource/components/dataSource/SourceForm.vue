<template>
  <el-drawer
    v-model="form.visible"
    :data-testid="`dataSource-form-drawer`"
    :size="1200"
    destroy-on-close
    @open="handleOpen"
    @close="handleClose"
  >
    <template #header>
      <h4 class="fw-600">
        {{ form.operation == 'add' ? '新增' : form.operation == 'edit' ? '编辑' : '查看' }}数据源
      </h4>
    </template>
    <template #default>
      <div class="body-wrapper">
        <el-form ref="formRef" :model="form.data" :rules="rules" label-width="120px" class="form">
          <el-form-item prop="dataSourceName" label="数据源名称">
            <el-input
              v-model.trim="form.data.dataSourceName"
              :data-testid="`dataSource-lform-10001`"
              clearable
              placeholder="请输入"
            />
          </el-form-item>
          <!-- <el-form-item prop="dataSourceType" label="数据格式">
            <el-radio-group
              v-model="form.data.dataSourceType"
              :data-testid="`dataSource-lform-10002`"
              :disabled="form.operation === 'edit'"
            >
              <el-radio
                :value="item.key"
                v-for="(item, index) in conditions.dataType"
                :key="index"
                :data-testid="`dataSource-lform-10002-op-${index}`"
                :disabled="item.key !== 'text'"
              >
                {{ item.value }}
              </el-radio>
            </el-radio-group>
          </el-form-item> -->
          <!-- <el-form-item prop="dataSourceAccessWay" label="接入方式">
            <el-radio-group
              v-model="form.data.dataSourceAccessWay"
              :data-testid="`dataSource-lform-10003`"
              :disabled="form.operation === 'edit'"
            >
              <el-radio
                :value="item.key"
                v-for="(item, index) in conditions.dataSourceAccessWay"
                :key="index"
                :data-testid="`dataSource-lform-10003-op-${index}`"
                :disabled="item.key !== 'upload'"
                >{{ item.value }}
              </el-radio>
            </el-radio-group>
          </el-form-item> -->
          <el-form-item prop="labelType" label="标签类型">
            <!-- @change="labelTypeChange" -->
            <el-checkbox-group
              v-model="form.data.labelType"
              style="margin-top: 2px"
              :data-testid="`dataSource-lform-10004`"
            >
              <el-checkbox
                :value="item.key"
                v-for="(item, index) in conditions.labelAndModel"
                :key="index"
                :data-testid="`dataSource-lform-10004-op-${index}`"
                >{{ item.value }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <!-- <el-form-item prop="modelType" label="处理模型">
            <el-radio-group v-model="form.data.modelType" :data-testid="`dataSource-lform-10005`">
              <el-radio
                :value="item.key"
                v-for="(item, index) in processingModelOptions"
                :key="index"
                :data-testid="`dataSource-lform-10005-op-${index}`"
                :disabled="item.disabled"
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
        <el-button type="primary" @click="handleOk">确定</el-button>
      </div>
    </template>
  </el-drawer>
</template>
<script lang="ts" setup>
import { inject } from 'vue'
import type { Form } from '@/hooks/table.d'
import type { ConditionsDetailItem } from '@/types'
import { saveDataSource, updateDataSource } from '@/api/dataCenter'
import { ElMessage } from 'element-plus'
import useUserStore from '@/stores/modules/user'

const emit = defineEmits(['refreshList'])
const form = inject('form') as Form
const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>

const userStore = useUserStore()

const rules = {
  dataSourceName: [{ required: true, message: '请输入数据源名称' }],
  // dataSourceType: [{ required: true, message: '请选择数据格式' }],
  // dataSourceAccessWay: [{ required: true, message: '请选择接入方式' }],
  labelType: [{ required: true, message: '请选择标签类型' }]
  // modelType: [{ required: true, message: '请选择处理模型' }]
}

function processArray(arr: any) {
  const optionsMap = new Map()
  const keyMap = new Map()
  for (const item of arr) {
    if (optionsMap.has(item.key)) {
      keyMap.set(item.key, keyMap.get(item.key) + 1)
      item.count = keyMap.get(item.key)
      optionsMap.set(item.key, item)
    } else {
      keyMap.set(item.key, 1)
      item.count = keyMap.get(item.key)
      optionsMap.set(item.key, item)
    }
  }

  return Array.from(optionsMap.values())
}

const processingModelOptions = computed(() => {
  if (!form.data.labelType || form.data.labelType?.length === 0 || !conditions.labelAndModel) {
    return conditions.processingModel
  }
  // 汇总所有标签类型级联出来的模型
  const totalOptions = conditions.labelAndModel?.reduce((pre: any[], cur: any) => {
    if (form.data.labelType?.includes(cur.key)) {
      pre.push(...cur.children)
    }
    return pre
  }, [])

  const resultOptions = processArray(totalOptions).map(el => {
    return {
      ...el,
      disabled: el.count !== form.data.labelType?.length
    }
  })

  if (resultOptions?.length === 1) {
    return conditions.processingModel.map(el => {
      return {
        ...el,
        disabled: el.key !== resultOptions[0]?.key
      }
    })
  }

  return resultOptions
})
const labelTypeChange = () => {
  form.data.modelType = ''
}

const handleCancel = () => {
  form.visible = false
}
const formRef = ref()
const handleOk = async () => {
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      const api = form.operation == 'add' ? saveDataSource : updateDataSource
      api(form.data)
        .then(res => {
          if (res.code === '200') {
            emit('refreshList')
            handleCancel()
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

const handleOpen = () => {
  form.data.clientId = userStore.clientId
  if (form.operation == 'add') {
    form.data.dataSourceType = 'text'
    form.data.dataSourceAccessWay = 'upload'
  }
}
const handleClose = () => {
  Object.keys(form.data).forEach(key => {
    form.data[key] = null
  })
}
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
