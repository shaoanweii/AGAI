<script setup lang="ts">
import { reactive, computed, watch, onMounted } from 'vue'
import { debounce, forIn } from 'lodash-es'
import searchPng from '@/assets/imgs/rules/search.png'
import arrowRightPng from '@/assets/imgs/rules/chevron-right.png'
import { ruleSelect } from '@/api/rules'

// 双向绑定规则 ID 集合
const ruleId = defineModel<string[]>({ default: [] })

interface RuleItem {
  ruleId: string
  ruleName: string
  categoryId: string
  categoryName?: string
}

interface RawRuleData {
  // 接口返回结构：{ 分类名称: [规则列表] }
  [categoryLabel: string]: RuleItem[]
}

// 组件内部状态
const hub = reactive({
  loadingRules: false, // 规则数据加载中
  rawRuleData: {} as RawRuleData, // 接口原始返回数据：{ 分类名称: [规则列表] }
  rawRuleDataMap: {} as Record<string, RuleItem[]>, // 按 categoryId 聚合后的规则列表
  // 左侧分类列表：展示名称使用接口返回的 key，内部依然用 categoryId 作为唯一标识
  categories: [] as Array<{ categoryId: string; categoryName: string }>,
  activeCategory: '', // 当前激活的分类 categoryId
  categoryKeyword: '', // 左侧分类搜索关键字
  ruleKeyword: '', // 右侧规则搜索关键字
  catCheckAll: false, // 左侧头部「规则分类」全选状态
  catIndeterminate: false, // 左侧头部半选状态
  ruleCheckAll: false, // 右侧头部「规则名称」全选状态
  ruleIndeterminate: false // 右侧头部半选状态
})

// 根据关键字过滤后的分类列表（仅影响展示，不影响勾选状态）
const filteredCategories = computed(() => {
  if (!hub.categoryKeyword) return hub.categories
  const kw = hub.categoryKeyword.trim()
  return hub.categories.filter(c => c.categoryName?.includes(kw))
})

// 当前激活分类 + 关键字过滤后的规则列表
const ruleList = computed(() => {
  const baseList =
    (hub.activeCategory && hub.rawRuleDataMap[hub.activeCategory]) || ([] as RuleItem[])
  if (!hub.ruleKeyword) return baseList
  const kw = hub.ruleKeyword.trim()
  return baseList.filter(r => r.ruleName?.includes(kw))
})

// 计算单个分类的勾选状态（全选 / 半选 / 未选）
const getCategoryCheckState = (catId: string) => {
  const rules = hub.rawRuleDataMap[catId] || []
  if (!rules.length) return { checked: false, indeterminate: false }

  const selectedCount = rules.filter(r => ruleId.value.includes(r.ruleId)).length
  return {
    checked: selectedCount === rules.length,
    indeterminate: selectedCount > 0 && selectedCount < rules.length
  }
}

// 监听分类勾选状态，更新左侧头部的「全选 / 半选」状态
watch(
  () => [filteredCategories.value.length, ruleId.value.slice()],
  () => {
    let fullSelectedCount = 0
    let partialSelectedCount = 0

    filteredCategories.value.forEach(cat => {
      const state = getCategoryCheckState(cat.categoryId)
      if (state.checked) fullSelectedCount++
      else if (state.indeterminate) partialSelectedCount++
    })

    hub.catCheckAll =
      filteredCategories.value.length > 0 && fullSelectedCount === filteredCategories.value.length
    hub.catIndeterminate =
      (fullSelectedCount > 0 || partialSelectedCount > 0) &&
      fullSelectedCount < filteredCategories.value.length
  },
  { immediate: true }
)

// 监听当前分类下规则勾选状态，更新右侧头部的「全选 / 半选」状态
watch(
  () => [ruleList.value.length, ruleId.value.slice()],
  () => {
    const currentSet = new Set(ruleList.value.map(i => i.ruleId))
    const selectedInCurrent = ruleId.value.filter(id => currentSet.has(id))
    hub.ruleCheckAll =
      ruleList.value.length > 0 && selectedInCurrent.length === ruleList.value.length
    hub.ruleIndeterminate =
      selectedInCurrent.length > 0 && selectedInCurrent.length < ruleList.value.length
  },
  { immediate: true }
)

// 监听规则 ID 集合的变化，用于编辑场景：有回显数据时自动同步一次激活分类
watch(
  () => ruleId.value,
  (newVal, oldVal) => {
    const prev = Array.isArray(oldVal) ? oldVal : []
    const next = Array.isArray(newVal) ? newVal : []

    // 仅在从「无选中」变为「有选中」时触发一次，避免影响用户后续手动切换
    if (!prev.length && next.length) {
      syncActiveCategoryIfReady()
    }
  }
)

onMounted(() => {
  fetchRuleList()
})

// 分类名称点击：只切换右侧数据，不改变勾选状态
const categoryClick = (catId: string) => {
  hub.activeCategory = catId
  // 切换分类时清空右侧规则搜索关键字
  hub.ruleKeyword = ''
}

// 根据当前已选规则，同步一个「最合适」的激活分类
const syncActiveCategoryIfReady = () => {
  const selectedRuleIds = Array.isArray(ruleId.value) ? ruleId.value : []
  if (!selectedRuleIds.length) return
  if (!hub.categories.length) return

  // 按左侧展示顺序，找到第一个包含已选规则的分类
  let targetCatId = ''
  for (const cat of hub.categories) {
    const list = hub.rawRuleDataMap[cat.categoryId] || []
    const hasSelected = list.some(rule => selectedRuleIds.includes(rule.ruleId))
    if (hasSelected) {
      targetCatId = cat.categoryId
      break
    }
  }

  // 如果没有找到命中的分类，但存在分类列表，则兜底选中第一个分类
  if (!targetCatId && hub.categories.length) {
    targetCatId = hub.categories[0].categoryId
  }

  if (!targetCatId) return

  hub.activeCategory = targetCatId
  hub.ruleKeyword = ''
}

// 对外暴露的方法：在需要时由父组件显式触发分类同步（例如编辑弹框手动控制时机）
const syncCategoriesFromRules = () => {
  syncActiveCategoryIfReady()
}

// 分类 checkbox 勾选联动：左侧勾选 / 取消勾选分类，对应右侧该分类下规则全选 / 全部取消
const handleCategoryCheckChange = (catId: string, val: boolean) => {
  hub.activeCategory = catId
  const rules = hub.rawRuleDataMap[catId] || []
  const ruleIdsInCat = rules.map(r => r.ruleId)

  if (val) {
    // 勾选分类：将该分类下所有规则加入选中集合
    ruleId.value = [...new Set([...ruleId.value, ...ruleIdsInCat])]
  } else {
    // 取消分类：将该分类下所有规则从选中集合移除
    const toRemove = new Set(ruleIdsInCat)
    ruleId.value = ruleId.value.filter(id => !toRemove.has(id))
  }
}

// 左侧头部「规则分类」全选 / 取消全选
const handleCheckAllChange = () => {
  if (hub.catCheckAll) {
    // 全选：选中当前过滤出的所有分类下的全部规则
    const allRuleIds: string[] = []
    filteredCategories.value.forEach(cat => {
      const rules = hub.rawRuleDataMap[cat.categoryId] || []
      rules.forEach(r => allRuleIds.push(r.ruleId))
    })
    ruleId.value = [...new Set(allRuleIds)]
  } else {
    // 取消全选：清空所有选中规则
    ruleId.value = []
  }
}

// 右侧头部「规则名称」全选 / 取消全选
const handleRuleCheckAllChange = () => {
  if (hub.ruleCheckAll) {
    // 全选当前激活分类（并经过规则搜索过滤后）的所有规则
    ruleId.value = ruleList.value.map(i => i.ruleId)
  } else {
    // 只取消当前列表中的规则勾选，保留其它分类已选规则
    const currentSet = new Set(ruleList.value.map(i => i.ruleId))
    ruleId.value = ruleId.value.filter(id => !currentSet.has(id))
  }
  hub.ruleIndeterminate = false
}

// 分类搜索防抖（真正过滤逻辑由 computed 完成）
const keywordCategoriesSearch = debounce(() => {
  const list = filteredCategories.value

  if (!list.length) {
    hub.activeCategory = ''
    hub.ruleKeyword = ''
    return
  }

  const exist = list.some(item => item.categoryId === hub.activeCategory)
  if (!exist) {
    hub.activeCategory = list[0].categoryId
    hub.ruleKeyword = ''
  }
}, 300)
// 规则搜索防抖
const keywordRuleSearch = debounce(() => {
  hub.ruleKeyword = hub.ruleKeyword.trim()
}, 300)

// 获取规则数据
const fetchRuleList = async () => {
  try {
    hub.loadingRules = true
    const res: any = await ruleSelect({})
    const data: RawRuleData = res?.result || {}

    hub.rawRuleData = data
    hub.rawRuleDataMap = {}
    hub.categories = []

    // 将 { 分类名称: [规则列表] } 结构整理为：
    // 1）左侧分类展示使用 key；
    // 2）内部通过 categoryId 做映射，防止分类名称重复导致的问题。
    forIn(data, (rules, key) => {
      const list = Array.isArray(rules) ? (rules as RuleItem[]) : []
      if (!list.length) return

      const first = list[0]
      const categoryId = first?.categoryId || String(key)

      hub.rawRuleDataMap[categoryId] = list
      hub.categories.push({
        categoryId,
        categoryName: String(key)
      })
    })

    // 数据加载完成后，如果当前已经有选中规则（编辑回显场景），补一次激活分类同步
    syncActiveCategoryIfReady()
  } catch (e) {
    hub.rawRuleData = {}
  } finally {
    hub.loadingRules = false
  }
}

// 暴露给父组件的方法
defineExpose({ syncCategoriesFromRules })
</script>

<template>
  <div class="rule-chooser">
    <!-- 左侧：规则分类 -->
    <div v-loading="hub.loadingRules" class="rule-pane">
      <el-checkbox
        class="pane-header"
        v-model="hub.catCheckAll"
        :indeterminate="hub.catIndeterminate"
        @change="handleCheckAllChange"
      >
        规则分类
      </el-checkbox>
      <div class="pane-body">
        <el-input
          v-model.trim="hub.categoryKeyword"
          placeholder="请输入内容"
          class="mb-8"
          @change="keywordCategoriesSearch"
        >
          <template #suffix>
            <el-image :src="searchPng" style="width: 20px; height: 20px" />
          </template>
        </el-input>
        <el-scrollbar max-height="174" class="pl-8">
          <div v-if="filteredCategories.length" class="category-list">
            <div
              v-for="c in filteredCategories"
              :key="c.categoryId"
              class="category-item"
              :class="{ active: hub.activeCategory === c.categoryId }"
            >
              <el-checkbox
                :model-value="getCategoryCheckState(c.categoryId).checked"
                :indeterminate="getCategoryCheckState(c.categoryId).indeterminate"
                @change="val => handleCategoryCheckChange(c.categoryId, val)"
              />
              <el-tooltip
                :disabled="c.categoryName && c.categoryName.length < 10"
                :content="c.categoryName"
                placement="top"
                :show-after="500"
                popper-class="common-tooltip"
              >
                <span
                  class="category-text single-line-ellipsis"
                  @click="categoryClick(c.categoryId)"
                >
                  {{ c.categoryName }}
                </span>
              </el-tooltip>
            </div>
          </div>
          <div v-else class="empty-tip">暂无数据</div>
        </el-scrollbar>
      </div>
    </div>

    <div class="split">
      <el-image :src="arrowRightPng" style="width: 20px; height: 20px" />
    </div>

    <!-- 右侧：规则名称 -->
    <div v-loading="hub.loadingRules" class="rule-pane flex-1">
      <el-checkbox
        class="pane-header"
        v-model="hub.ruleCheckAll"
        :indeterminate="hub.ruleIndeterminate"
        @change="handleRuleCheckAllChange"
      >
        规则名称
      </el-checkbox>
      <div class="pane-body">
        <div class="flex mb-8">
          <el-input
            v-model.trim="hub.ruleKeyword"
            placeholder="请输入内容"
            class="flex-1"
            @change="keywordRuleSearch"
          >
            <template #suffix>
              <el-image :src="searchPng" style="width: 20px; height: 20px" />
            </template>
          </el-input>
        </div>
        <el-scrollbar max-height="174" class="pl-8">
          <el-checkbox-group v-if="ruleList.length" v-model="ruleId">
            <el-checkbox
              v-for="r in ruleList"
              :key="r.ruleId"
              :value="r.ruleId"
              style="display: flex; align-items: center; padding: 4px 0 0; margin-right: 0"
            >
              <el-tooltip
                :disabled="r.ruleName && r.ruleName.length < 28"
                :content="r.ruleName"
                placement="top"
                :show-after="500"
                popper-class="common-tooltip"
              >
                <span class="single-line-ellipsis right-rule-name-item" style="display: block">{{
                  r.ruleName
                }}</span>
              </el-tooltip>
            </el-checkbox>
          </el-checkbox-group>
          <div v-else class="empty-tip">暂无数据</div>
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.rule-chooser {
  display: flex;
  width: 100%;
  gap: 10px;
}
.rule-chooser .rule-pane {
  width: 180px;
  height: 272px;
  padding: 8px 16px;
  box-sizing: border-box;
  background: #ffffff;
  border-radius: 6px;
  border: 1px solid #dcdcdc;
  display: flex;
  flex-direction: column;
}
.rule-chooser .rule-pane.flex-1 {
  width: auto;
  flex: 1;
}
.rule-chooser .rule-pane .pane-header {
  font-weight: 400;
  color: #1d2129;
}
.rule-chooser .rule-pane .pane-body {
  padding-top: 8px;
}
.category-list {
  display: flex;
  flex-direction: column;
}
.category-item {
  display: flex;
  align-items: center;
  padding: 4px 0 0;
  cursor: pointer;
}
.category-item.active .category-text {
  color: #1677ff;
  font-weight: 500;
  display: inline-block;
}
.category-text {
  flex: 1;
  margin-left: 8px;
  user-select: none;
}
.right-rule-name-item {
  width: 335px;
}
.rule-chooser .split {
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c9cdd4;
}
.empty-tip {
  padding: 40px 0;
  text-align: center;
  color: #86909c;
  font-size: 14px;
}
</style>
