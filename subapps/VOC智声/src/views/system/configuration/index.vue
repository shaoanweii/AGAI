<script setup lang="ts">
import { h, ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import emojiNeutralPng from '@/assets/images/system/emoji-neutral.png'
import emojiSatisfiedPng from '@/assets/images/system/emoji-satisfied.png'
import emojiFrownPng from '@/assets/images/system/emoji-frown.png'
import emojiSmilePng from '@/assets/images/system/emoji-smile.png'
import emojiAngryPng from '@/assets/images/system/emoji-angry.png'
import {
  getDisplayRuleList,
  createDisplayRule,
  updateDisplayRule,
  deleteDisplayRule
} from '@/api/system/configuration'
import type {
  DisplayRuleItem,
  DisplayRuleQueryParams,
  UpdateDisplayRuleRequest
} from '@/api/system/configuration/types'
import { appDialogConfirm } from '@/components/appDialog'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'

defineOptions({
  name: 'DisplayRuleConfiguration'
})

// 响应式数据
const loading = ref(false)
const ruleList = ref<DisplayRuleItem[]>([])

// 使用 WeakMap 记录各行上一次合法的区间，便于冲突时自动回退
const lastValidRanges = new WeakMap<DisplayRuleItem, { min: number; max: number }>()

const setLastValidRange = (row: DisplayRuleItem) => {
  const min = Number(row.rangeMin)
  const max = Number(row.rangeMax)
  if (Number.isFinite(min) && Number.isFinite(max)) {
    lastValidRanges.set(row, { min, max })
  }
}

const revertToLastValidRange = (row: DisplayRuleItem) => {
  const snap = lastValidRanges.get(row)
  if (snap) {
    row.rangeMin = snap.min as any
    row.rangeMax = snap.max as any
  }
}

// 查询参数
const queryParams = reactive<DisplayRuleQueryParams>({})

// 范围选项
// const scopeOptions = [
//   { value: 0, label: '0.00' },
//   { value: 20, label: '20.00' },
//   { value: 40, label: '40.00' },
//   { value: 60, label: '60.00' },
//   { value: 80, label: '80.00' },
//   { value: 100, label: '100.00' }
// ]
const scopeOptions = (() => {
  let arr = []
  for (let i = 0; i <= 100; i++) {
    arr.push({
      value: i,
      label: `${i}.00`
    })
  }
  return arr
})()

// 表情选项
const emojiList = [
  { key: '1', value: '愤怒', icon: emojiAngryPng },
  { key: '2', value: '失望', icon: emojiFrownPng },
  { key: '3', value: '一般', icon: emojiNeutralPng },
  { key: '4', value: '满意', icon: emojiSmilePng },
  { key: '5', value: '惊喜', icon: emojiSatisfiedPng }
]

// 组件挂载时获取数据
onMounted(async () => {
  fetchDisplayRuleList()
})

/**
 * 获取显示规则列表
 */
const fetchDisplayRuleList = async () => {
  loading.value = true
  try {
    const response = await getDisplayRuleList(queryParams)
    if (response.success && response.result) {
      ruleList.value = response.result
      // 初始化各规则的最近一次合法区间快照
      ruleList.value.forEach(item => {
        const min = Number(item.rangeMin)
        const max = Number(item.rangeMax)
        if (Number.isFinite(min) && Number.isFinite(max)) {
          item.rangeMin = min
          item.rangeMax = max
          lastValidRanges.set(item, { min, max })
        }
      })
      console.log('显示规则列表获取成功:', response.result)
    } else {
      ElMessage.error(response.message || '获取显示规则列表失败')
      ruleList.value = []
    }
  } catch (error) {
    console.error('获取显示规则列表失败:', error)
    ElMessage.error('获取显示规则列表失败，请稍后重试')
    ruleList.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 根据code获取对应的表情
 */
const getEmojiByCode = (code: string) => {
  const emojiItem = emojiList.find(item => item.key === code)
  return emojiItem && emojiItem.icon
}

/**
 * 添加规则
 * 点击“添加规则”只在本地新增一条，待填写完整后再创建
 */
const handleAdd = async () => {
  // 最多添加五条
  if (ruleList.value.length >= 5) {
    ElMessage.warning('最多添加五条规则')
    return
  }

  // 本地新增一条未保存的规则，等待用户填写完整信息后再创建
  const newRule: UpdateDisplayRuleRequest = {
    metricCode: 'negativeRate',
    metricName: '负面率',
    rangeMin: 0,
    rangeMax: 0,
    colorHex: '',
    backgroundColorHex: '',
    emojiKey: '',
    sortNo: ruleList.value.length + 1,
    status: 1
  }

  // 直接添加到本地列表，不调用接口
  ruleList.value.push(newRule)
}

/**
 * 删除规则
 */
const handleDelete = async (row: DisplayRuleItem) => {
  try {
    await appDialogConfirm(
      () =>
        h('div', { class: 'flex items-center' }, [
          h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
          h('span', { class: 'ml-8' }, '确定要删除该项显示规则吗？删除后无法恢复')
        ]),
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    throw error
  }

  // 未保存到后端的数据，直接本地移除
  if (!row.id) {
    const idx = ruleList.value.findIndex(item => item === row)
    if (idx > -1) {
      ruleList.value.splice(idx, 1)
    }
    ElMessage.success('删除规则成功')
    return
  }
  try {
    const response = await deleteDisplayRule(row.id)
    if (response.success) {
      ElMessage.success('删除规则成功')
      await fetchDisplayRuleList() // 重新获取列表
    } else {
      ElMessage.error(response.message || '删除规则失败')
    }
  } catch (error) {
    console.error('删除规则失败:', error)
    ElMessage.error('删除规则失败，请稍后重试')
  }
}

/**
 * 更新规则
 */
const handleUpdate = async (row: DisplayRuleItem) => {
  // 区间范围合法性校验（新旧规则都需要校验）
  // 先行处理：本地合法性与冲突校验 + 自动回退
  const min = Number(row.rangeMin)
  const max = Number(row.rangeMax)
  if (Number.isFinite(min) && Number.isFinite(max)) {
    if (min > max) {
      ElMessage.error('请检查区间范围')
      // 自动回退到上一次合法值
      revertToLastValidRange(row)
      return
    }
    const conflict = ruleList.value.find(item => {
      if (item === row) return false
      if (!Number.isFinite(Number(item.rangeMin)) || !Number.isFinite(Number(item.rangeMax)))
        return false
      const otherMin = Number(item.rangeMin)
      const otherMax = Number(item.rangeMax)
      return min < otherMax && otherMin < max
    })
    if (conflict) {
      ElMessage.error(`区间与已有规则冲突：${conflict.rangeMin}.00-${conflict.rangeMax}.00`)
      // 自动回退到上一次合法值
      revertToLastValidRange(row)
      return
    }
    // 无冲突，记录为最近一次合法区间
    setLastValidRange(row)
  }
  /*
  if (Number(row.rangeMin ?? 0) > Number(row.rangeMax ?? 0)) {
    ElMessage.error('请检查区间范围')
    return
  }
  */

  // 如果是未保存（无 id）的本地新规则：在信息填写完整时走接口
  const readyToCreate =
    row.metricCode &&
    row.metricName &&
    isValidNumber(row.rangeMin) &&
    isValidNumber(row.rangeMax) &&
    (row.colorHex ?? '') !== '' &&
    (row.backgroundColorHex ?? '') !== '' &&
    (row.emojiKey ?? '') !== ''

  if (!readyToCreate) {
    // 信息未填写完整，不调用接口
    return
  }
  if (!row.id) {
    const createData: UpdateDisplayRuleRequest = {
      // metricCode: row.metricCode,
      // metricName: row.metricName,
      // rangeMin: row.rangeMin,
      // rangeMax: row.rangeMax,
      // colorHex: row.colorHex,
      // emojiKey: row.emojiKey,
      // sortNo: row.sortNo,
      ...row,
      status: typeof row.status === 'number' ? row.status : 1
    }

    try {
      const response = await createDisplayRule(createData)
      if (response.success) {
        ElMessage.success('添加规则成功')
        await fetchDisplayRuleList() // 重新获取列表，拿到生成的 ID
      } else {
        ElMessage.error(response.message || '添加规则失败')
      }
    } catch (error) {
      console.error('添加规则失败:', error)
      ElMessage.error('添加规则失败，请稍后重试')
    }
    return
  }

  // 其余情况为已有规则的更新
  const updateData: UpdateDisplayRuleRequest = {
    ...row
  }

  try {
    const response = await updateDisplayRule(updateData)
    if (response.success) {
      ElMessage.success('更新规则成功')
      await fetchDisplayRuleList() // 重新获取列表
    } else {
      ElMessage.error(response.message || '更新规则失败')
    }
  } catch (error) {
    console.error('更新规则失败:', error)
    ElMessage.error('更新规则失败，请稍后重试')
  }
}

/**
 * 验证数值是否为有效的数字（支持字符串和数字类型）
 * @param value 待验证的值
 * @returns 是否为有效数字
 */
const isValidNumber = (value: unknown): boolean => {
  if (typeof value === 'number') {
    return !isNaN(value) && isFinite(value)
  }
  if (typeof value === 'string') {
    const num = Number(value)
    return !isNaN(num) && isFinite(num) && value.trim() !== ''
  }
  return false
}

/**
 * 刷新数据
 */
const handleRefresh = async () => {
  await fetchDisplayRuleList()
}
</script>

<template>
  <div class="h-full flex-col">
    <el-card class="flex-1 flex-col" shadow="never">
      <div class="flex-between items-center mb-24">
        <div class="text-h3" style="font-weight: 600">显示规则</div>
        <!--        <div class="flex gap-16">-->
        <!--          <el-button type="primary" @click="handleRefresh">刷新</el-button>-->
        <!--        </div>-->
      </div>
      <div class="flex-auto overflow-auto">
        <el-table v-loading="loading" :data="ruleList">
          <el-table-column prop="metricName" label="指标" width="200px" align="center" />
          <el-table-column prop="rangeMin" label="区间范围" align="center">
            <template #default="{ row }">
              <div class="flex-center">
                <el-select
                  v-model="row.rangeMin"
                  placeholder="请选择范围"
                  @change="handleUpdate(row)"
                  style="width: 154px"
                >
                  <el-option
                    v-for="item in scopeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
                <div class="pl-16 pr-16 fs-14 fw-500" style="color: #1d2129">-</div>
                <el-select
                  v-model="row.rangeMax"
                  placeholder="请选择范围"
                  @change="handleUpdate(row)"
                  style="width: 154px"
                >
                  <el-option
                    v-for="item in scopeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="colorHex" label="文字颜色" align="center">
            <template #default="{ row }">
              <el-color-picker v-model="row.colorHex" show-alpha @change="handleUpdate(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="backgroundColorHex" label="背景颜色" align="center">
            <template #default="{ row }">
              <el-color-picker
                v-model="row.backgroundColorHex"
                show-alpha
                @change="handleUpdate(row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="emojiKey" label="表情符号" align="center">
            <template #default="{ row }">
              <el-select
                v-model="row.emojiKey"
                value-key="key"
                size="large"
                placeholder="表情"
                class="emoji-select-class"
                style="width: 90px"
                @change="handleUpdate(row)"
              >
                <template #label="{ label }">
                  <div class="flex-y-center">
                    <el-image
                      v-if="getEmojiByCode(label)"
                      :src="getEmojiByCode(label)"
                      class="w-32 h-32"
                    />
                    <div v-else class="w-32 h-32"></div>
                  </div>
                </template>
                <el-option
                  v-for="item in emojiList"
                  :key="item.key"
                  :label="item.key"
                  :value="item.key"
                  class="h-45"
                  style="line-height: 45px"
                >
                  <div class="flex-y-center">
                    <el-image :src="item.icon" class="w-32 h-32 mr-8" />
                    <div class="ml-16">{{ item.value }}</div>
                  </div>
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="flex justify-center mt-24">
          <el-button type="primary" link @click="handleAdd">
            <el-icon color="#1677FF">
              <Plus />
            </el-icon>
            <span class="ml-8">添加规则</span>
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
:deep(.el-table .el-table__cell) {
  height: 55px;
  padding: 0 !important;
}

.flex-auto {
  flex: auto;
}

.overflow-auto {
  overflow: auto;
}

:deep(.el-table__header) {
  .el-table__cell {
    color: #1d2129;
    font-weight: 600;
  }
}

:deep(.el-table--fit .el-table__inner-wrapper:before) {
  width: 0 !important;
}

:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.el-table__body-wrapper) {
  .el-table__cell {
    color: #1d2129;
    font-weight: 400;
  }
}

:deep(.emoji-select-class) {
  .el-select__wrapper {
    box-shadow: none !important;
    background-color: transparent !important;
  }
}
</style>
