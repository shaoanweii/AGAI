<script setup lang="ts">
import { reactive, onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { appDialogConfirm } from '@/components/appDialog'
import {
  findDataResourceList,
  insDataResourceDelete,
  insDataResourceInsert,
  insDataResourceUpdate
} from '@/api/rules'
import { singleEventStore, singleRuleTypeValue } from './store'
import { useAutoFillListHeight } from '@/hooks/useAutoFillListHeight'

// 事件定义
interface Emits {
  (e: 'categoryItemClick', item: any): void
}
const emit = defineEmits<Emits>()

const loading = ref(false)
const loadingMore = ref(false) // 上拉加载中
const scrollRef = ref<HTMLElement | null>(null)
const ruleFormRef = ref<FormInstance>()

// 状态中心
const hub = reactive({
  currentCategory: {
    name: '',
    id: '',
    isActive: true,
    isDisable: false
  },
  // 查询条件
  queryParams: {
    pageNum: 1,
    pageSize: 10
  } as any,
  total: 0,
  categoryList: [] as any[],
  visible: false,
  categoryData: null as any,
  // 表单数据
  ruleForm: {
    name: '',
    // 规则类型（默认单点）
    ruleType: singleRuleTypeValue.value,
    type: 'closedLoop' // 当前仅支持闭环类目
  },
  // 表单校验
  rules: {
    name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }]
  }
})

// 是否没有更多
const noMore = computed(() => hub.categoryList.length >= hub.total && hub.total > 0)
// 是否可以继续加载
const canLoadMore = computed(() => !loading.value && !loadingMore.value && !noMore.value)

// 获取列表；append=true 表示上拉追加，否则重载
const fetchcategoryList = async (append = false, isRefreshActive = true) => {
  if (append) {
    loadingMore.value = true
  } else {
    loading.value = true
  }
  try {
    const params: any = {
      type: 'closedLoop',
      pageNum: hub.queryParams.pageNum,
      pageSize: hub.queryParams.pageSize
    }
    const response = await findDataResourceList(params)
    if (response.success && response.result) {
      const mapped =
        response.result.records?.map((item: any) => ({
          ...item,
          isActive: false,
          isDisable: false
        })) || []
      hub.total = response.result.total

      if (append) {
        // 上拉追加
        hub.categoryList.push(...mapped)
      } else {
        // 首屏/刷新重载
        hub.categoryList = mapped
        // 默认选中第一项
        if (hub.categoryList.length > 0 && isRefreshActive) {
          const firstItem = hub.categoryList[0]
          firstItem.isActive = true
          hub.currentCategory = { ...firstItem }
          emit('categoryItemClick', firstItem)
        }
        // 回到顶部
        if (scrollRef.value) scrollRef.value.scrollTop = 0
      }
    } else {
      ElMessage.error(response.message || '获取分类列表失败')
      if (!append) hub.categoryList = []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败，请稍后重试')
    if (!append) hub.categoryList = []
  } finally {
    if (append) {
      loadingMore.value = false
    } else {
      loading.value = false
    }
  }
}

// 列表项点击
const itemClick = (item: any) => {
  if (item.isDisable) return
  hub.categoryList.forEach((category: any) => {
    category.isActive = false
  })
  item.isActive = true
  hub.currentCategory = { ...item }
  emit('categoryItemClick', item)
}

// 新增/编辑
const editItem = (item: any | null) => {
  hub.categoryData = item
  ruleFormRef.value?.resetFields()
  ruleFormRef.value?.clearValidate()
  if (item) {
    hub.ruleForm.name = item.name ?? ''
    hub.ruleForm.ruleType = item.ruleType ?? singleRuleTypeValue.value
  } else {
    hub.ruleForm.name = ''
    hub.ruleForm.ruleType = singleRuleTypeValue.value
  }
  hub.visible = true
}

// 删除
const deleteItem = async (item: any) => {
  try {
    await appDialogConfirm(`确认要删除分类「${item.name}」吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      dialogAttrs: {
        width: '480px'
      }
    })
    if (!item.id) {
      return ElMessage.error('数据异常，请联系管理员')
    }
    const response = await insDataResourceDelete({ id: item.id, type: 'closedLoop' })
    if (response.success) {
      ElMessage.success('删除成功')
      // 删除后回到第一页，避免页码越界
      refreshList()
    } else {
      ElMessage.error(response.message || '删除分类失败')
    }
  } catch (e: any) {
    if (e === 'cancel' || e === 'close' || e?.message === 'cancel') {
      return
    }

    ElMessage.error(e.message || '删除分类失败')
  }
}

// 上拉加载更多
const loadMore = async () => {
  if (!canLoadMore.value) return
  hub.queryParams.pageNum += 1
  await fetchcategoryList(true)
}

// 使用通用 hooks：首屏自动补齐列表高度（列表过短时自动加载下一页，直到撑满或没有更多数据）
const { autoFillListHeight } = useAutoFillListHeight(scrollRef, {
  canLoadMore: () => canLoadMore.value,
  loadMore
})

// 滚动监听：接近底部触发加载
const onScroll = (e: Event) => {
  const el = e.target as HTMLElement
  const threshold = 40 // 距底部 40px 触发
  if (el.scrollTop + el.clientHeight + threshold >= el.scrollHeight) {
    loadMore()
  }
}

// 刷新
const refreshList = async (isRefreshActive?: boolean) => {
  hub.queryParams.pageNum = 1
  await fetchcategoryList(false, isRefreshActive)
  // 刷新后同样做一次高度补齐，避免删除/新增后出现大面积空白
  await autoFillListHeight()
}

onMounted(async () => {
  await fetchcategoryList(false)
  // 首次进入时自动拉取多页数据，尽量填满左侧列表区域
  await autoFillListHeight()
})

// 弹窗确认提交
const onDialogConfirm = async ({ close }: { close: () => void }) => {
  if (!ruleFormRef.value) return
  const valid = await ruleFormRef.value.validate().catch(() => false)
  if (!valid) return
  const isEdit = !!hub.categoryData
  const msg = isEdit ? '编辑成功' : '新增成功'
  const errorMsg = isEdit ? '编辑失败' : '新增失败'
  try {
    const requestParams = {
      id: hub.categoryData?.id || '',
      ...hub.ruleForm
    }
    let response: any = null
    if (isEdit) {
      response = await insDataResourceUpdate(requestParams)
    } else {
      response = await insDataResourceInsert(requestParams)
    }
    if (response.success && response.result) {
      ElMessage.success(msg)
      // 成功后回到第一页重新拉取，确保数据最新
      refreshList()
    } else {
      ElMessage.error(response.message || errorMsg)
    }
    close()
  } catch (e: any) {
    // 校验失败不关闭
    ElMessage.error(e.message || errorMsg)
  }
}

defineExpose({
  refreshList
})
</script>

<template>
  <div class="flex-col h-full">
    <div class="flex-between items-center mb-24">
      <div class="header-title-class">主题分类</div>
      <el-button type="primary" @click="() => editItem(null)">
        <template #icon>
          <Plus />
        </template>
        新建分类</el-button
      >
    </div>
    <div class="flex-1 flex-col">
      <div class="h-55 flex-y-center title-class pl-16 pr-16">分类名称</div>
      <div class="category-list flex-1">
        <div
          ref="scrollRef"
          class="category-scroll"
          v-loading="loading"
          @scroll="onScroll"
          style="max-height: calc(100vh - 320px); overflow: auto"
        >
          <div
            v-for="(item, index) in hub.categoryList"
            :key="`${item.name}-${index}`"
            @click="itemClick(item)"
          >
            <div
              class="category-item flex-y-center flex-between h-55 pl-16 pr-16"
              :class="{ active: !item.isDisable && item.id === hub.currentCategory.id }"
            >
              <div class="flex-y-center overflow-hidden">
                <div class="flex-y-center">
                  <SvgIcon
                    name="rules-file-list-line"
                    style="width: 20px; height: 20px"
                    :color="
                      !item.isDisable && item.id === hub.currentCategory.id ? '#1677ff' : '#4E5969'
                    "
                  />
                </div>
                <div class="category-name flex-y-center overflow-hidden ml-8">
                  <div class="single-line-ellipsis">{{ item.name }}</div>
                  （{{ item.cnt || 0 }}）
                </div>
              </div>
              <div class="category-action">
                <SvgIcon
                  name="rules-edit"
                  @click.stop="editItem(item)"
                  style="width: 16px; height: 16px; margin: 8px"
                  color="#4E5969"
                />
                <SvgIcon
                  v-if="!item.cnt"
                  name="rules-delete"
                  @click.stop="deleteItem(item)"
                  style="width: 16px; height: 16px; margin: 8px"
                  color="#4E5969"
                />
              </div>
            </div>
          </div>
          <div class="list-footer">
            <span v-if="loadingMore">加载中...</span>
            <!-- <span v-else-if="noMore && hub.categoryList.length > hub.queryParams.pageSize"
              >没有更多了</span
            > -->
            <span v-else>&nbsp;</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <AppDialog
      v-model:visible="hub.visible"
      :title="hub.categoryData ? '编辑分类' : '新建分类'"
      :confirm="onDialogConfirm"
    >
      <el-form
        ref="ruleFormRef"
        style="padding-left: 20px"
        :model="hub.ruleForm"
        :rules="hub.rules"
        label-width="auto"
        class="category-form"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="hub.ruleForm.name" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-radio-group v-model="hub.ruleForm.ruleType" class="rules-radio-group-class">
            <el-radio-button
              v-for="opt in singleEventStore.conditions.closedRuleType"
              :key="opt.key"
              :value="opt.key"
              :label="opt.value"
            />
          </el-radio-group>
        </el-form-item>
      </el-form>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.header-title-class {
  font-weight: 600;
  font-size: 20px;
  color: #333333;
  line-height: 32px;
}
.title-class {
  background: #f2f4f7;
  font-weight: 500;
  font-size: 14px;
  color: #1d2129;
}

.h-55 {
  height: 54px;
}

.category-item {
  border-top: 1px solid #e5e6eb;
  font-weight: 500;
  font-size: 14px;
  color: #1f2733;

  .category-action {
    display: none;
  }
}

.active {
  background: #e2f3fe;
  color: #1677ff;

  .category-action {
    display: flex;
  }
}

.category-scroll {
  position: relative;
}
.list-footer {
  text-align: center;
  padding: 8px 0 12px;
  color: #86909c;
}

.rules-radio-group-class {
  .el-radio-button + .el-radio-button {
    margin-left: 16px;
  }
  :deep(.el-radio-button__inner) {
    border-radius: 4px 4px 4px 4px !important;
    border: 1px solid #dfe2e8 !important;
    font-weight: 500 !important;
    font-size: 14px !important;
  }
  :deep(.el-radio-button.is-active .el-radio-button__inner) {
    color: #1677ff !important;
    border: 1px solid #1677ff !important;
    background-color: transparent !important;
    box-shadow: none !important;
  }
}
.category-form {
  :deep(.el-input__inner) {
    color: #1d2129 !important;
  }
}
</style>
