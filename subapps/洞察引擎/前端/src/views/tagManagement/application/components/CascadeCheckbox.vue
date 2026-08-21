<template>
  <div>
    <!--一级全部-->
    <div style="width: 840px; display: inline-flex; flex-wrap: wrap">
      <el-checkbox
        v-model="firstAll"
        :indeterminate="firstIndeterminate"
        style="margin-bottom: 14px; margin-right: 16px"
        >一级全部
      </el-checkbox>
      <div
        v-for="(item, fIndex) of firstList"
        :key="item.key"
        style="margin-bottom: 14px; margin-right: 16px"
        class="flex align-center"
      >
        <el-checkbox
          v-model="item.checked"
          :indeterminate="item.indeterminate"
          :value="item.key"
          @change="(val) => handleFirstChange(val as boolean, fIndex)"
        >
        </el-checkbox>
        <span style="margin-left: 8px; cursor: pointer" @click="handleItem(1, fIndex)">{{
          item.value
        }}</span>
      </div>
    </div>

    <!--二级全部-->
    <div v-if="getSecondList?.length" style="width: 800px; display: inline-flex; flex-wrap: wrap">
      <el-checkbox
        v-model="secondAll"
        :indeterminate="secondIndeterminate"
        style="margin-bottom: 14px; margin-right: 16px"
        >二级全部
      </el-checkbox>
      <div
        v-for="(item, sIndex) of getSecondList"
        :key="item.key"
        style="margin-bottom: 14px; margin-right: 16px"
        class="flex align-center"
      >
        <el-checkbox
          v-model="item.checked"
          :value="item.key"
          :indeterminate="item.indeterminate"
          @change="(val) => handleSecondChange(val as boolean, sIndex)"
        >
        </el-checkbox>
        <span style="margin-left: 8px; cursor: pointer" @click="handleItem(2, sIndex)">{{
          item.value
        }}</span>
      </div>
    </div>

    <!--三级全部-->
    <div v-if="getThirdList?.length" style="width: 800px; display: inline-flex; flex-wrap: wrap">
      <el-checkbox
        v-model="thirdAll"
        :indeterminate="thirdIndeterminate"
        style="margin-bottom: 14px; margin-right: 16px"
        >三级全部
      </el-checkbox>
      <div
        v-for="item of getThirdList"
        :key="item.key"
        style="margin-bottom: 14px; margin-right: 16px"
        class="flex align-center"
      >
        <el-checkbox
          v-model="item.checked"
          :value="item.key"
          :indeterminate="item.indeterminate"
          @change="() => handleThirdChange()"
        >
        </el-checkbox>
        <span style="margin-left: 8px; cursor: pointer">{{ item.value }}</span>
      </div>
    </div>

    <!--<el-button @click="test">测试</el-button>-->
  </div>
</template>

<script setup lang="ts">
import type { ConditionsDetailItem } from '@/types'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

interface ListItem extends ConditionsDetailItem {
  checked?: boolean
  indeterminate?: boolean
}

const firstList = reactive<ListItem[]>([])

// 记录一级部分当前选择的标签
const firstIndex = ref(-1)
const secondIndex = ref(-1)

// 递归处理数据(添加选中状态和半选状态)
const recursiveForEach = (list: ListItem[] | undefined, checked = true) => {
  if (!list) return []
  Array.isArray(list) &&
    list.forEach(item => {
      item.checked = checked
      item.indeterminate = false
      if (item.children) {
        recursiveForEach(item.children, checked)
      }
    })
  return list
}

watch(
  () => props.data,
  (nval, oval) => {
    if (JSON.stringify(nval) === JSON.stringify(oval)) {
      return
    }
    firstIndex.value = -1
    secondIndex.value = -1
    firstList.length = 0
    // 获取一二三级全部标签
    Object.assign(firstList, recursiveForEach(nval as ListItem[], true))
  },
  {
    deep: true,
    immediate: true
  }
)

// 获取一级 or 二级 or 三级 当前展示的list和已选中的list
const getListAndCheckedListByList = (array: ListItem[] | undefined) => {
  if (!array) {
    return {
      list: [],
      checkedList: [],
      indeterminateList: []
    }
  }
  const list = (Array.isArray(array) && array) || []
  const checkedList = list.filter((el: ListItem) => el.checked)
  const indeterminateList = list.filter((el: ListItem) => el.indeterminate)
  return {
    list,
    checkedList,
    indeterminateList
  }
}

// 二级标签
const getSecondList = computed(() => {
  if (firstIndex.value === -1) return []
  return firstList[firstIndex.value]?.children
})
// 三级标签
const getThirdList = computed(() => {
  if (!getSecondList.value?.length || secondIndex.value === -1) return []
  return getSecondList.value[secondIndex.value]?.children
})

// 一级部分是否半选
const firstIndeterminate = computed(() => {
  const checkedList = firstList?.filter(el => el.checked) || []
  if (checkedList?.length === firstList?.length || checkedList?.length === 0) {
    return false
  } else if (checkedList?.length > 0) {
    return true
  }
})

// 一级部分是否全选
const firstAll = computed({
  get() {
    return firstList?.every(el => el.checked)
  },
  set(val) {
    Object.assign(firstList, recursiveForEach(firstList || [], val))
  }
})

// 二级部分是否半选
const secondIndeterminate = computed(() => {
  const { list, checkedList, indeterminateList } = getListAndCheckedListByList(getSecondList.value)
  if (indeterminateList?.length) {
    return true
  }
  if (checkedList?.length === list?.length || checkedList?.length === 0) {
    return false
  } else if (checkedList?.length > 0) {
    return true
  }
})

// 二级部分是否全选
const secondAll = computed({
  get() {
    return !!getSecondList.value?.length && getSecondList.value?.every(el => el.checked)
  },
  set(val) {
    firstList[firstIndex.value].children = reactive(recursiveForEach(getSecondList.value, val))
    firstList[firstIndex.value].checked = val
    firstList[firstIndex.value].indeterminate = false
  }
})

// 三级部分是否半选
const thirdIndeterminate = computed(() => {
  const { list, checkedList, indeterminateList } = getListAndCheckedListByList(getThirdList.value)
  if (indeterminateList?.length) {
    return true
  }
  if (checkedList?.length === list?.length || checkedList?.length === 0) {
    return false
  } else if (checkedList?.length > 0) {
    return true
  }
})

// 三级部分是否全选
const thirdAll = computed({
  get() {
    return !!getThirdList.value?.length && getThirdList.value?.every(el => el.checked)
  },
  set(val) {
    firstList[firstIndex.value].children![secondIndex.value].children = reactive(
      recursiveForEach(getThirdList.value, val)
    )
    firstList[firstIndex.value].children![secondIndex.value].checked = val
    firstList[firstIndex.value].children![secondIndex.value].indeterminate = false
  }
})

// 点击标签文案
const handleItem = (level: number, index: number) => {
  if (level === 1) {
    firstIndex.value = index
    secondIndex.value = -1
  } else if (level === 2) {
    secondIndex.value = index
  }
}

// 一级标签change
const handleFirstChange = (val: boolean, findex: number) => {
  firstList[findex].children = recursiveForEach(firstList[findex]?.children || [], val)
  const { list, checkedList } = getListAndCheckedListByList(firstList[findex]?.children || [])
  if (list.length === checkedList.length) {
    firstList[findex].checked = true
    firstList[findex].indeterminate = false
  } else if (checkedList.length === 0) {
    firstList[findex].checked = false
    firstList[findex].indeterminate = false
  } else {
    firstList[findex].checked = false
    firstList[findex].indeterminate = true
  }
}
const setFirstStatus = () => {
  const { list, checkedList, indeterminateList } = getListAndCheckedListByList(getSecondList.value)
  if (checkedList.length === list.length) {
    firstList[firstIndex.value].checked = true
    firstList[firstIndex.value].indeterminate = false
  } else if (checkedList.length === 0) {
    firstList[firstIndex.value].checked = false
    firstList[firstIndex.value].indeterminate = false
  } else {
    firstList[firstIndex.value].checked = false
    firstList[firstIndex.value].indeterminate = true
  }

  if (indeterminateList.length) {
    firstList[firstIndex.value].indeterminate = true
  }
}

// 二级标签change
const handleSecondChange = (val: boolean, sIndex: number) => {
  if (getSecondList.value?.length) {
    firstList[firstIndex.value].children![sIndex].children = recursiveForEach(
      firstList[firstIndex.value]?.children![sIndex].children || [],
      val
    )
  }

  const { list, checkedList } = getListAndCheckedListByList(
    firstList[firstIndex.value]?.children![sIndex].children
  )
  if (checkedList.length === list.length) {
    firstList[firstIndex.value].children![sIndex].checked = true
    firstList[firstIndex.value].children![sIndex].indeterminate = false
  } else if (checkedList.length === 0) {
    firstList[firstIndex.value].children![sIndex].checked = false
    firstList[firstIndex.value].children![sIndex].indeterminate = false
  } else {
    firstList[firstIndex.value].children![sIndex].checked = false
    firstList[firstIndex.value].children![sIndex].indeterminate = true
  }

  setFirstStatus()
}

// 三级标签change
const handleThirdChange = () => {
  const { list, checkedList } = getListAndCheckedListByList(getThirdList.value)
  if (checkedList.length === list.length) {
    firstList[firstIndex.value].children![secondIndex.value].checked = true
    firstList[firstIndex.value].children![secondIndex.value].indeterminate = false
  } else if (checkedList.length === 0) {
    firstList[firstIndex.value].children![secondIndex.value].checked = false
    firstList[firstIndex.value].children![secondIndex.value].indeterminate = false
  } else {
    firstList[firstIndex.value].children![secondIndex.value].checked = false
    firstList[firstIndex.value].children![secondIndex.value].indeterminate = true
  }
  setFirstStatus()
}
</script>

<style scoped lang="scss"></style>
