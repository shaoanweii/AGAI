<script setup lang="ts">
import { reactive, onMounted, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { EnumSingleOrBatch } from '@/views/rules/closedLoopRules/constant'
import {
  findDataResourceList,
  insDataResourceDelete,
  insDataResourceInsert,
  insDataResourceUpdate
} from '@/api/rules'
import { useAutoFillListHeight } from '@/hooks/useAutoFillListHeight'

// 事件定义
interface Emits {
  (e: 'categoryItemClick', item: any): void
}
const emit = defineEmits<Emits>()

const loading = ref(false)
const loadingMore = ref(false)
const scrollRef = ref<HTMLElement | null>(null)
const ruleFormRef = ref<FormInstance>()

// 本地状态中心
const hub = reactive({
  currentCategory: {
    name: '',
    id: '',
    isActive: true,
    isDisable: false
  },
  // 查询参数
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
    // 规则类型：（默认单点）
    ruleType: EnumSingleOrBatch.single, //是单点或者批量
    type: 'account' //当前分类是闭环规则得分类
  },
  // 表单校验
  rules: {
    name: [{ required: true, message: '分组名称不能为空', trigger: 'blur' }],
    ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }]
  }
})

// 是否没有更多
const noMore = computed(() => hub.categoryList.length >= hub.total && hub.total > 0)
// 是否可以继续加载
const canLoadMore = computed(() => !loading.value && !loadingMore.value && !noMore.value)

// 拉取列表；append=true 表示上拉追加，否则重载
const fetchcategoryList = async (append = false) => {
  if (append) {
    loadingMore.value = true
  } else {
    loading.value = true
  }
  try {
    const params: any = {
      type: 'account',
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
        if (hub.categoryList.length > 0) {
          const firstItem = hub.categoryList[0]
          firstItem.isActive = true
          hub.currentCategory = { ...firstItem }
          emit('categoryItemClick', firstItem)
        }
        // 回到顶部
        if (scrollRef.value) scrollRef.value.scrollTop = 0
      }
    } else {
      ElMessage.error(response.message || '获取分组列表失败')
      if (!append) hub.categoryList = []
    }
  } catch (error) {
    console.error('获取分组列表失败:', error)
    ElMessage.error('获取分组列表失败，请稍后重试')
    if (!append) hub.categoryList = []
  } finally {
    if (append) {
      loadingMore.value = false
    } else {
      loading.value = false
    }
  }
}

// 左侧点击分类
const itemClick = (item: any) => {
  if (item.isDisable) return
  hub.categoryList.forEach((category: any) => {
    category.isActive = false
  })
  item.isActive = true
  hub.currentCategory = { ...item }
  emit('categoryItemClick', item)
}

// 新建/编辑分组
const editItem = (item: any | null) => {
  hub.categoryData = item
  ruleFormRef.value?.resetFields()
  if (item) {
    hub.ruleForm.name = item.name ?? ''
    hub.ruleForm.ruleType = item.ruleType ?? EnumSingleOrBatch.single
  } else {
    hub.ruleForm.name = ''
    hub.ruleForm.ruleType = EnumSingleOrBatch.single
  }
  hub.visible = true
}

// 删除分类
const deleteItem = async (item: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除分组“${item.name}”吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (!item.id) {
      return ElMessage.error('数据异常，请联系管理员')
    }
    const response = await insDataResourceDelete({ id: item.id, type: 'account' })
    if (response.success) {
      ElMessage.success('删除成功')
      refreshList()
    } else {
      ElMessage.error(response.message || '删除分组失败')
    }
  } catch (e) {
    console.error('删除分组失败:', e)
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
  const threshold = 40
  if (el.scrollTop + el.clientHeight + threshold >= el.scrollHeight) {
    loadMore()
  }
}

// 刷新列表回到第一页
const refreshList = async () => {
  hub.queryParams.pageNum = 1
  await fetchcategoryList(false)
  // 刷新后同样做一次高度补齐，避免删除/新增后出现大面积空白
  await autoFillListHeight()
}

onMounted(async () => {
  await fetchcategoryList(false)
  // 首次进入时自动拉取多页数据，尽量填满左侧列表区域
  await autoFillListHeight()
})

// 弹窗“确定”：校验并提交（mock）
const onDialogConfirm = async ({ close }: { close: () => void }) => {
  if (!ruleFormRef.value) return
  const isEdit = !!hub.categoryData
  const msg = isEdit ? '保存成功' : '新增成功'
  const errorMsg = isEdit ? '保存失败' : '新增失败'
  try {
    await ruleFormRef.value.validate()
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
</script>

<template>
  <div class="flex-col h-full">
    <div class="flex-between items-center mb-24">
      <div class="header-title-class">分组列表</div>
      <el-button type="primary" @click="() => editItem(null)">
        <template #icon>
          <Plus />
        </template>
        新建分组</el-button
      >
    </div>
    <div class="flex-1 flex-col">
      <div class="h-55 flex-y-center title-class pl-16 pr-16">词库名称</div>
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
                <SvgIcon
                  name="rules-file-list-line"
                  style="width: 20px; height: 20px"
                  :color="
                    !item.isDisable && item.id === hub.currentCategory.id ? '#1677ff' : '#4E5969'
                  "
                />
                <div class="category-name flex-y-center overflow-hidden ml-8">
                  <div class="single-line-ellipsis">{{ item.name }}</div>
                  （{{ item.cnt || 0 }}）
                </div>
              </div>
              <div v-if="item.allowDeletion" class="category-action">
                <SvgIcon
                  name="rules-edit"
                  @click.stop="editItem(item)"
                  style="width: 16px; height: 16px; margin: 0 8px"
                  color="#4E5969"
                />
                <SvgIcon
                  name="rules-delete"
                  @click.stop="deleteItem(item)"
                  style="width: 16px; height: 16px; margin: 0 8px"
                  color="#4E5969"
                />
              </div>
            </div>
          </div>
          <div class="list-footer">
            <span v-if="loadingMore">加载中...</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建/编辑分类弹窗 -->
    <AppDialog
      v-model:visible="hub.visible"
      :title="hub.categoryData ? '编辑分组' : '新建分组'"
      :confirm="onDialogConfirm"
    >
      <el-form
        ref="ruleFormRef"
        style="padding-left: 20px"
        :model="hub.ruleForm"
        :rules="hub.rules"
        label-width="auto"
      >
        <el-form-item label="分组名称" prop="name">
          <el-input v-model="hub.ruleForm.name" placeholder="请输入" />
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
    display: block;
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
</style>
