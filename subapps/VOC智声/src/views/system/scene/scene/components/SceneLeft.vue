<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import fileTypePng from '@/assets/images/system/file-type.png'
import fileTypeDisablePng from '@/assets/images/system/file-type-disable.png'
import ClassifyDialog from '@views/system/scene/scene/components/ClassifyDialog.vue'
import SortDialog from '@views/system/scene/scene/components/SortDialog.vue'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'
import { appDialogConfirm } from '@/components/appDialog'
import { deleteSpecialType, getSpecialTypeList } from '@/api/system/scene'

interface Emits {
  (e: 'classifyItemClick', item: any): void
}

const emit = defineEmits<Emits>()

const loading = ref(false)

const hub = reactive({
  currentClassify: {
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
  total: 0, // 总数
  classificationList: [] as any[],
  visible: false,
  classifyData: null as any,
  sortVisible: false
})

// 获取分类列表
const fetchClassificationList = async () => {
  loading.value = true
  hub.classificationList = []
  try {
    const params: any = {
      type: 1,
      pageNum: hub.queryParams.pageNum,
      pageSize: hub.queryParams.pageSize
    }
    const response = await getSpecialTypeList(params)
    if (response.success && response.result) {
      hub.total = response.result.total || 0
      hub.classificationList =
        response.result.list?.map((item: any) => ({
          ...item,
          isActive: false,
          isDisable: false
        })) || []

      // 默认选中第一个
      if (hub.classificationList.length > 0) {
        const firstItem = hub.classificationList[0]
        firstItem.isActive = true
        hub.currentClassify = {
          ...firstItem
        }
        emit('classifyItemClick', firstItem)
      }

      console.log('分类列表获取成功:', hub.classificationList)
    } else {
      ElMessage.error(response.message || '获取分类列表失败')
      hub.classificationList = []
      hub.total = 0
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败，请稍后重试')
    hub.classificationList = []
    hub.total = 0
  } finally {
    loading.value = false
  }
}

const canSort = () => !loading.value && hub.classificationList.length > 0

// 打开分类排序弹窗
const openSortDialog = () => {
  if (!canSort()) return
  hub.sortVisible = true
}

// 点击分类
const itemClick = (item: any) => {
  if (item.isDisable) return

  hub.classificationList.forEach(classify => {
    classify.isActive = false
  })
  item.isActive = true
  hub.currentClassify = {
    ...item
  }
  emit('classifyItemClick', item)
}

// 编辑分类
const editItem = (item: any | null) => {
  hub.classifyData = item
  hub.visible = true
}

// 删除分类
const deleteItem = async (item: any) => {
  try {
    await appDialogConfirm(
      () =>
        h('div', { class: 'flex items-center' }, [
          h(SvgIcon, { name: 'info-circle-filled', width: '20px', height: '20px' }),
          h('span', { class: 'ml-8' }, `确定要删除分类 "${item.name}" 吗？`)
        ]),
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    const response = await deleteSpecialType({ id: item.id })
    if (response.success) {
      ElMessage.success('删除分类成功')
      fetchClassificationList()
    } else {
      ElMessage.error(response.message || '删除分类失败')
    }
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('删除分类失败:', error)
  }
}

// 分页
const handleSizeChange = (val: number) => {
  hub.queryParams.pageSize = val
  hub.queryParams.pageNum = 1
  fetchClassificationList()
}

// 分页
const handleCurrentChange = (val: number) => {
  hub.queryParams.pageNum = val
  fetchClassificationList()
}

// 刷新列表
const refreshList = () => {
  fetchClassificationList()
}

onMounted(async () => {
  await fetchClassificationList()
})
</script>

<template>
  <div class="flex-col h-full">
    <div class="flex-between items-center mb-24">
      <div class="text-h3" style="font-weight: 600">分类列表</div>
      <div class="flex-y-center">
        <el-button class="sort-btn" :disabled="!canSort()" @click="openSortDialog">
          <SvgIcon name="direction_swap" width="16px" height="16px" color="currentColor" class="mr-6" />
          分类排序
        </el-button>
        <el-button class="ml-16" type="primary" @click="() => editItem(null)">新建分类</el-button>
      </div>
    </div>
    <div class="flex-1 flex-col">
      <div class="h-55 flex-y-center title-class pl-16 pr-16">分类名称</div>
      <div class="classification-list flex-auto overflow-auto">
        <el-scrollbar v-loading="loading" max-height="calc(100vh - 330px)">
          <div
            v-for="(item, index) in hub.classificationList"
            :key="`${item.name}-${index}`"
            @click="itemClick(item)"
          >
            <div
              class="classification-item flex-y-center flex-between h-55 pl-16 pr-16"
              :class="{ active: !item.isDisable && item.id === hub.currentClassify.id }"
            >
              <div class="flex-y-center">
                <el-image v-show="!item.isDisable" :src="fileTypePng" style="width: 20px; height: 20px" />
                <el-image v-show="item.isDisable" :src="fileTypeDisablePng" style="width: 20px; height: 20px" />
                <div class="classification-name ml-8">{{ item.name }}</div>
              </div>
              <div class="classification-action">
                <Edit
                  @click="editItem(item)"
                  style="width: 16px; height: 16px; margin-right: 8px"
                  color="#929AA6"
                />
                <Delete
                  @click="deleteItem(item)"
                  style="width: 16px; height: 16px; margin-right: 8px"
                  color="#929AA6"
                />
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
      <el-pagination
        v-model:current-page="hub.queryParams.pageNum"
        v-model:page-size="hub.queryParams.pageSize"
        :total="hub.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="->,total, prev, pager, next, sizes"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    <ClassifyDialog v-model:visible="hub.visible" :classify-data="hub.classifyData" @success="refreshList()" />
    <SortDialog
      v-model:visible="hub.sortVisible"
      title="分类排序"
      :type="1"
      :total="hub.total"
      @success="refreshList()"
    />
  </div>
</template>

<style lang="scss" scoped>
.title-class {
  background: #f2f4f7;
}

.h-55 {
  height: 55px;
}

.sort-btn {
  --el-button-text-color: #1d2129;
}

.classification-item {
  border-top: 1px solid #e5e6eb;

  .classification-action {
    display: none;
  }
}

.active {
  background: #eaf3ff;

  .classification-action {
    display: block;
  }
}
</style>
