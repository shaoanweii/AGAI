<template>
  <div class="main-table">
    <FtCard title="标签管理">
      <el-form inline :model="table.filter" class="clear-form-item-margin">
        <el-row class="w-full" :gutter="24">
          <el-col :span="6">
            <el-form-item label="标签类型" class="w-full">
              <el-select
                :data-testid="`application-10002`"
                v-model="table.filter.tagType"
                placeholder="全部"
              >
                <el-option
                  v-for="(item, index) in conditions.labelType"
                  :key="index"
                  :data-testid="`application-10002-${index}`"
                  :label="item.value"
                  :value="item.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="启用状态" class="w-full">
              <FSelect
                v-model="table.filter.tagStatusList"
                multiple
                clearable
                :options="conditions.enableType"
                placeholder="全部"
                :subLength="10"
                :fieldNames="{ value: 'key', label: 'value' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="标签名称" class="w-full">
              <el-input
                :data-testid="`application-10006`"
                v-model.trim="table.filter.tagName"
                placeholder="请输入"
                :maxlength="20"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <div class="w-full flex justify-end">
              <el-button
                color="#F2F3F5"
                :data-testid="`founding-dataSource-10003`"
                style="margin-right: 8px"
                @click="reset"
                >重置</el-button
              >
              <el-button type="primary" :data-testid="`founding-dataSource-10004`" @click="query(1)"
                >查询</el-button
              >
            </div>
          </el-col>
        </el-row>
      </el-form>
    </FtCard>
    <div v-loading="tableLoading" style="width: 100%; display: flex">
      <div class="flex mt-24 w-full">
        <Classification
          ref="classificationRef"
          :filter="table.filter"
          :setLoading="setTableLoading"
          @CategoryChange="categoryChange"
        />
        <!-- :curCategorize="curCategorize" -->
        <TagList ref="tagListRef" :filter="table.filter" :setLoading="setTableLoading" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import Classification from './components/Classification/index.vue'
import TagList from './components/TagList/index.vue'
import useConditions from '@/hooks/useConditions'
import FtCard from '@/components/FtCard.vue'

const { conditions } = useConditions({ url: '/insights/insTagLibClient/conditions' })
provide('conditions', conditions)

const tableLoading = ref(false)

const table = ref<Record<any, any>>({
  filter: {
    tagType: 'PROD',
    tagStatusList: [],
    tagName: ''
  }
})

const setTableLoading = (loading: boolean) => {
  tableLoading.value = loading
}

const classificationRef = ref()
const tagListRef = ref()
const curCategorize = ref()

const query = async (flag?: number) => {
  await classificationRef.value?.query(flag)
}

const categoryChange = (val: any) => {
  curCategorize.value = val
  tagListRef.value?.refreshTable(val)
}

onMounted(() => {
  classificationRef.value?.query()
})
const reset = () => {
  table.value.filter.tagStatusList = []
  table.value.filter.tagName = ''
  table.value.filter.tagType = 'PROD'

  query(1)
}
</script>
