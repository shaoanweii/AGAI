<template>
  <el-drawer
    class="ft-drawer"
    :size="1200"
    v-model="form.visible"
    @close="handleClose"
    :data-testid="`projectManagement-form-drawer`"
    destroy-on-close
  >
    <template #header>
      <div>{{ titleStr }}</div>
    </template>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      auto-label-width
      :style="{ width: '100%' }"
    >
      <FtButtonGroup v-model="type" :group="group"> </FtButtonGroup>
      <div class="group-wrapper">
        <div v-show="type === 1">
          <el-form-item prop="projectName" label="项目名称">
            <el-input
              :data-testid="`projectManagement-form-1001`"
              v-model.trim="formData.projectName"
              clearable
              :maxlength="50"
              placeholder="请输入"
            />
          </el-form-item>
          <el-form-item prop="remark" label="项目描述">
            <el-input
              type="textarea"
              :data-testid="`projectManagement-form-1008`"
              v-model.trim="formData.projectDesc"
              :maxlength="300"
              placeholder="请输入相关描述说明"
              clearable
            />
          </el-form-item>
        </div>
        <div v-show="type === 2">
          <el-form-item prop="brand">
            <div style="margin-top: 5px; width: 100%">
              <BrandConfig v-model="formData"></BrandConfig>
            </div>
          </el-form-item>
        </div>
        <div
          v-show="type === 3"
          v-if="
            formData.brand && formData.brand.length && formData.brand.every((item: any) => item.brandCode)
          "
        >
          <!-- <el-form-item prop="brand" label="关联品牌">
            <el-radio-group
              v-model="formData.activeBrandCodeIndex"
              :default-value="0"
              type="button"
              size="large"
            >
              <el-radio v-for="(item, index) of formData.brand" :key="index" :value="index">{{
                item.brandName
              }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="brand" label="车系配置">
            <div style="margin-top: 5px; width: 100%">
              <VehicleConfig
                v-model="formData.brand[formData.activeBrandCodeIndex]"
              ></VehicleConfig>
            </div>
          </el-form-item> -->
          <el-form-item prop="brand" no-style :hide-asterisk="true">
            <VehicleConfig v-model="formData"></VehicleConfig>
          </el-form-item>
        </div>
        <div
          v-show="type === 4"
          v-if="formData.brand && formData.brand.length && formData.brand.every((item: any) => item.brandCode)"
        >
          <!-- <el-form-item prop="brand" label="关联品牌">
            <el-radio-group
              v-model="formData.activeDataManagerIndex"
              :default-value="0"
              type="button"
              size="large"
              @change="handleBrandChange"
            >
              <el-radio v-for="(item, index) of formData.brand" :key="index" :value="index">{{
                item.brandName
              }}</el-radio>
            </el-radio-group>
          </el-form-item> -->
          <el-form-item prop="brand" no-style :hide-asterisk="true">
            <!-- <DataManager
              :key="formData.activeDataManagerIndex"
              v-model="formData.brand[formData.activeDataManagerIndex]"
            /> -->
            <DataManager v-model="formData" />
          </el-form-item>
        </div>
        <div
          v-show="type === 5"
          v-if="formData.brand && formData.brand.length && formData.brand.every((item: any) => item.tags.length)"
        >
          <!-- <el-form-item prop="brand" label="关联品牌">
            <el-radio-group
              v-model="formData.activeRiskConfigurationIndex"
              :default-value="1"
              type="button"
              size="large"
            >
              <el-radio v-for="(item, index) of formData.brand" :key="index" :value="index">{{
                item.brandName
              }}</el-radio>
            </el-radio-group>
          </el-form-item> -->
          <!-- <WarningConfiguration v-model="formData.brand[formData.activeRiskConfigurationIndex]" /> -->
          <el-form-item prop="brand" no-style :hide-asterisk="true">
            <WarningConfiguration v-model="formData" />
            <!-- <div style="margin-top: 5px; width: 100%">
            </div> -->
          </el-form-item>
        </div>
      </div>

      <el-form-item prop="status" label="启用状态" required>
        <el-radio-group :data-testid="`projectManagement-form-1009`" v-model="formData.status">
          <el-radio
            v-for="(item, index) of conditions.stopOrEnable"
            :key="index"
            :data-testid="`projectManagement-form-1009-op-${index}`"
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
import type { Form } from '@/hooks/table.d'
import { ElMessage } from 'element-plus'
import { debounce } from 'lodash-es'
import FtButtonGroup from '@/components/FtButtonGroup.vue'
import type { ConditionsDetailItem } from '@/types'
import { useFormRules } from '@/hooks/useForm'
import useUserStore from '@/stores/modules/user'
// import useComputedCascaderWidth from '@/hooks/useComputedCascaderWidth'
import { saveProjectInfo, updateProjectInfo, findProjectInfo } from '@/api/project'
import VehicleConfig from './components/VehicleConfig/index.vue'
import BrandConfig from './components/BrandConfig/index.vue'
import DataManager from './components/DataManager/index.vue'
import WarningConfiguration from './components/WarningConfiguration/index.vue'
import type { Brand, RiskEarlyWarning, RiskLevel, RiskSetting } from '@/types/project'

const group = [
  { label: '基本信息', value: 1 },
  { label: '品牌配置', value: 2 },
  { label: '车系配置', value: 3 },
  { label: '数据管理', value: 4 },
  { label: '预警配置', value: 5 }
]

const INITFORMDATA = {
  projectName: '',
  projectDesc: '',
  status: '1',
  clientId: '',
  brand: []
}

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const emit = defineEmits(['refreshList'])
const form = inject('form') as Form
let formData = form.data
let titleStr = ref('')
const type = ref(1)
provide('type', type)
// const roleAuthTreeList = ref<Api.Role.PermissionTree[]>();

let userStore = useUserStore()

const { createStrLengthRule, createRequiredRule } = useFormRules()

/**
 * @description: 校验品牌是否有重复项
 * @param {*} arr
 * @return {*}
 */
function hasDuplicateBrandCode(arr: any) {
  const brandCodes = new Set()
  for (const item of arr) {
    if (brandCodes.has(item.brandCode)) {
      return true
    }
    brandCodes.add(item.brandCode)
  }
  return false
}

const rules = {
  projectName: [createStrLengthRule('项目名称', 2, 50)],
  brand: [
    {
      required: true,
      validator: (rule: any, value: any, callback: (error?: string) => void) => {
        console.log(value, 'value')
        if (!value.length) {
          callback('本品品牌必填')
        } else if (value.some((item: Brand) => !item.brandCode)) {
          callback('本品品牌有选项未填')
        } else if (hasDuplicateBrandCode(value)) {
          callback('本品品牌有重复项')
        } else if (
          value.some((item: Brand) => {
            return !item.carSeries.length
          })
        ) {
          callback('本品车系选项未填')
        } else if (
          value.some((item: Brand) => {
            return item.carSeries.some(
              (series: any) => !(series.carSeriesCode && series.core && series.haltSales)
            )
          })
        ) {
          callback('本品车系有选项未填')
        } else if (
          value.some((item: Brand) => {
            return !item.tags.length
          })
        ) {
          callback('本品标签选项未填')
        } else if (
          value.some((item: Brand) => {
            return !item.dataSource.length
          })
        ) {
          callback('本品数据源选项未填')
        } else if (
          value.some((item: Brand) => {
            return !item.channel.length
          })
        ) {
          callback('本品渠道选项未填')
        } else if (
          value.some((item: Brand) => {
            return !item.region.length
          })
        ) {
          callback('本品区域选项未填')
        } else if (
          value.some((item: Brand) => {
            return item.riskEarlyWarning.some((warningObj: RiskEarlyWarning) => {
              return !warningObj.riskSetting.some(
                (riskSettingItem: RiskSetting) => riskSettingItem.isApply
              )
            })
          })
        ) {
          callback('基础值设置至少应用一项')
        } else if (
          value.some((item: Brand) => {
            return item.riskEarlyWarning.some((warningObj: RiskEarlyWarning) => {
              return !warningObj.riskLevel.some((riskLevelItem: RiskLevel) => riskLevelItem.isApply)
            })
          })
        ) {
          callback('风险等级定义至少应用一项')
        } else {
          // 验证通过时必须调用 callback()
          callback()
        }
      }
    }
  ],
  status: [createRequiredRule('启用状态必填')]
}

watch(
  () => form.visible,
  (nv, ov) => {
    if (nv && !ov) {
      initializeFormData()
    }
  }
)

const initializeFormData = async () => {
  formData = reactive({ ...form.data })
  // Object.assign(formData, INITFORMDATA)

  formData.status = formData.status ? formData.status : '1'

  try {
    if (form.operation == 'add') {
      titleStr.value = '新增项目'
      Object.assign(formData, INITFORMDATA)
    } else if (form.operation == 'edit') {
      const response = await findProjectInfo({
        id: form.data.id,
        clientId: userStore.clientId
      }).then(res => res.result)
      Object.assign(formData, response)
      titleStr.value = '编辑项目'
    }
  } catch (e) {}
}

const handleCancel = () => {
  form.visible = false
}

const handleClose = () => {
  formData = reactive({})
  type.value = 1
}

const formRef = ref()
const handleOk = debounce(() => {
  console.log('handleOk', formData)
  // return

  formRef.value?.validate(async (errs: any) => {
    console.log('errs', errs)

    if (!errs) {
      const params = {
        ...formData
      }
      params.id = form.operation === 'add' ? '' : formData.id
      params.clientId = userStore.clientId

      const api = form.operation === 'add' ? saveProjectInfo : updateProjectInfo

      api(params)
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
    } else {
      let keys = Object.keys(errs)
      for (let i = 0; i < keys.length; i++) {
        ElMessage.error(errs[keys[i]].message)
      }
    }
  })
}, 300)
</script>

<style lang="scss">
.ft-drawer {
  :deep(.el-drawer__header) {
    padding: 20px 40px;
    border-bottom: 1px solid var(--border-color);
    margin-bottom: 0;
  }

  :deep(.el-drawer__title) {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-high);
  }

  :deep(.el-drawer__body) {
    padding: 24px 40px;
  }

  .group-wrapper {
    width: 100%;
    height: 693px;
    border: 1px solid var(--border-color);
    margin-top: -1px;
    box-sizing: border-box;
    overflow-y: auto;
    padding: 24px 40px;
    margin-bottom: 24px;
    background-color: var(--color-white);
  }

  // 优化表单项样式
  :deep(.el-form-item) {
    margin-bottom: 24px;

    .el-form-item__label {
      color: var(--color-high);
      font-weight: 500;
      line-height: 32px;
    }

    .el-form-item__content {
      line-height: 32px;
    }
  }

  // 优化输入框样式
  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: 4px;
    }
  }

  // 优化文本域样式
  :deep(.el-textarea) {
    .el-textarea__inner {
      border-radius: 4px;
    }
  }

  // 优化单选按钮样式
  :deep(.el-radio-group) {
    .el-radio {
      margin-right: 24px;

      .el-radio__label {
        color: var(--color-medium);
      }

      &.is-checked {
        .el-radio__label {
          color: var(--color-primary);
        }
      }
    }
  }
}

.radio-wrapper {
  display: flex;
}

.radio-item {
  width: 88px;
  height: 32px;
  text-align: center;
  line-height: 32px;
  cursor: pointer;
}

.active-radio {
  color: var(--color-primary);
  background: var(--bgc-def);
  border-radius: 100px;
}
</style>
