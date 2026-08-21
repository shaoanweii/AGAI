<template>
  <el-drawer
    v-model="visible"
    :size="1200"
    :data-testid="`baseSetting-region-drawer`"
    destroy-on-close
    @open="handleOpen"
    @close="handleClose"
  >
    <template #header>
      <!--<h4 class="fw-600">{{ form.operation == 'add' ? '新增' : form.operation == 'edit' ? '编辑' : '查看' }}数据源</h4>-->
      <h4 class="fw-600">{{ titleMapByType[type][viewStatus] }}</h4>
    </template>
    <template #default>
      <div class="body-wrapper">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="form">
          <el-form-item prop="name" :label="nameLabel">
            <!--<el-input v-model="form.name" :maxlength="10" placeholder="请输入"/>-->
            <FtInput
              v-model.allTrim="form.name"
              :data-testid="`baseSetting-region-form-10001`"
              :maxlength="10"
              clearable
              placeholder="请输入"
            />
          </el-form-item>
          <el-form-item prop="nameEn" label="英文名称">
            <el-input
              v-model.trim="form.nameEn"
              :maxlength="20"
              :data-testid="`baseSetting-region-form-10002`"
              placeholder="请输入"
              clearable
            />
          </el-form-item>
          <el-form-item v-if="type === 2" prop="regionCode" label="省份城市">
            <el-cascader
              :data-testid="`baseSetting-region-form-10003`"
              v-model="form.regionCode"
              :options="conditions.province"
              :props="{ value: 'key', label: 'value', children: 'children', multiple: true }"
              style="width: 100%"
              :max-collapse-tags="3"
              placeholder="请选择"
              clearable
              :show-all-levels="false"
            />
          </el-form-item>
          <el-form-item prop="parentId" label="所属分类">
            <el-cascader
              :data-testid="`baseSetting-region-form-10005`"
              v-model="form.parentId"
              :options="transitionCategorizeTree"
              :props="{ value: 'id', label: 'name', children: 'child' }"
              style="width: 100%"
              placeholder="请选择"
              clearable
              :show-all-levels="false"
            />
          </el-form-item>
          <el-form-item v-if="type === 2" prop="status" label="启用状态">
            <el-radio-group :data-testid="`baseSetting-region-form-10004`" v-model="form.status">
              <el-radio
                v-for="(item, index) of conditions.stopOrEnable"
                :key="index"
                :data-testid="`baseSetting-region-form-10004-op-${index}`"
                :value="item.key"
                >{{ item.value }}
              </el-radio>
            </el-radio-group>
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
import type { ConditionsDetailItem, ViewStatus } from '@/types'
import type { CategorizeItem, ChannelParams, Province } from '@/types/baseSeting.types'
import {
  saveRegionCategory,
  updateRegionCategory,
  saveRegion,
  updateRegion
} from '@/api/baseSettings'
import { ElMessage } from 'element-plus'
import { excludeNodeById } from '@/utils'
import useUserStore from '@/stores/modules/user'
import FtInput from '@/components/FtInput.vue'

const conditions = inject('conditions') as Record<string, ConditionsDetailItem[]>
const emit = defineEmits(['refreshList'])
const props = withDefaults(
  defineProps<{
    // 1 分类; 2 区域
    type: 1 | 2
    viewStatus: ViewStatus
    categorizeTree: any
    curCategorize?: any | undefined
    editRecord: any
  }>(),
  {
    viewStatus: 'add'
  }
)
// 允许新增分类的最大层级
const LEVEL_MAX = 5

const { type, viewStatus, categorizeTree, editRecord, curCategorize } = toRefs(props)

const visible = defineModel({ default: false })

const titleMapByType = {
  '1': {
    add: '新增分类',
    edit: '编辑分类',
    view: '查看分类'
  },
  '2': {
    add: '新增区域',
    edit: '编辑区域',
    view: '查看区域'
  }
}

const nameLabel = computed(() => {
  return type.value === 1 ? '分类名称' : '区域名称'
})
const rules = {
  name: [
    { required: true, message: `请输入${nameLabel.value}` },
    { minLength: 2, maxLength: 10, message: '请输入2-10个字符' }
  ],
  regionCode: [{ required: true, message: `请选择省份城市` }],
  parentId: [{ required: type.value === 2, message: `请选择所属分类` }],
  status: [{ required: true, message: '请选择启用状态' }]
}

const initializeData = (): ChannelParams => ({
  id: undefined,
  name: '',
  nameEn: undefined,
  clientId: '',
  type: 'Category',
  parentId: undefined,
  status: '1',
  region: []
})

let form = reactive<ChannelParams>(initializeData())

const transitionCategorizeTree = computed(() => {
  if (type.value === 1) {
    const list = categorizeTree.value.filter((item: CategorizeItem) => item.id !== '-1')
    if (viewStatus.value === 'add') {
      return list
    } else {
      // 编辑时需过滤自身及所有子节点
      return excludeNodeById(list, editRecord.value?.id)
    }
  } else {
    return categorizeTree.value
  }
})

const addInit = () => {
  form.status = '1'
  form.clientId = useUserStore().clientId!
  if (type.value === 1 && editRecord.value?.id !== '-1') {
    form.parentId = editRecord.value.id
  }
  if (type.value === 2) {
    form.parentId = editRecord.value.id
  }
}

const editInit = () => {
  Object.assign(form, editRecord.value)
  form.clientId = form.clientId ? form.clientId : useUserStore().clientId!
  // if (type.value === 1 && editRecord.value.id !== '-1') {
  if (editRecord.value.id !== '-1') {
    form.parentId = editRecord.value.parentId || curCategorize.value.id
  }
  const region = editRecord.value.region?.reduce((pre: string[], cur: Province) => {
    return [
      ...pre,
      ...cur.areas.map(item => {
        return [curCategorize.value.name, editRecord.value.name, cur.provinceCode, item.areaCode]
      })
    ]
  }, [])
  form.regionCode = region
}
const handleOpen = () => {
  if (type.value === 1) {
    form.type = 'Category'
  } else if (type.value === 2) {
    form.type = 'Channel'
  }
  if (viewStatus.value === 'add') {
    addInit()
  } else if (viewStatus.value === 'edit') {
    editInit()
  }
}

const handleCancel = () => {
  visible.value = false
}
const handleClose = () => {
  form = reactive(initializeData())
}

/**
 * 获取当前选择的分类是第几个层级
 */
const findNodeLevelById = (tree: any, targetId: string, level = 0): any => {
  for (const node of tree) {
    if (node.id === targetId) {
      return level + 1 // 加1是因为层级是从1开始计数的
    }
    if (node.child && node.child.length > 0) {
      const foundLevel = findNodeLevelById(node.child, targetId, level + 1)
      if (foundLevel !== undefined) {
        return foundLevel
      }
    }
  }
  return 0 // 如果没有找到对应ID的节点，返回undefined
}

const formRef = ref()
const getRegionName = (code: string) => {
  const provinceObj = toRaw(conditions.province)
  const findObj = provinceObj.find(item => item.key === code)
  return findObj?.value || ''
}
const getCityName = (codeArr: string[]) => {
  const provinceObj = toRaw(conditions.province)
  const findObj = provinceObj.find(item => item.key === codeArr[0])
  return findObj?.children?.find(item => item.key === codeArr[1])?.value || ''
}
const handleOk = async () => {
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      if (
        type.value === 1 &&
        findNodeLevelById(transitionCategorizeTree.value, form.parentId!) === LEVEL_MAX
      ) {
        return ElMessage.error('当前区域分类仅支持五级分类，请修改后重新提交。')
      }
      const api =
        viewStatus.value === 'add'
          ? type.value === 1
            ? saveRegionCategory
            : saveRegion
          : type.value === 1
          ? updateRegionCategory
          : updateRegion
      form.region = form.regionCode?.map((item: string[]): Province => {
        return {
          provinceCode: item[0],
          provinceName: getRegionName(item[0]),
          areas: [
            {
              areaCode: item[1],
              areaName: getCityName(item)
            }
          ]
        }
      })
      console.log('form', form)
      // return
      api(form)
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
