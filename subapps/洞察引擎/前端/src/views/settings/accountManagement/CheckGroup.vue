<template>
  <div class="check-box-group">
    <div class="check-main">
      <div class="check-top">
        <el-checkbox
          v-model="mockData.checked"
          :indeterminate="mockData.indeterminate"
          @change="handleChangeAll"
          >{{ mockData.title }}
        </el-checkbox>
        <icon-down @click="changeExpand" style="cursor: pointer; margin-left: 8px" />
      </div>
      <div class="check-item-wrapper" v-show="expand">
        <div v-for="(item, index) in mockData.children" :key="index">
          <div class="check-item" v-if="!item.children">
            <div class="check-item-left">
              <el-checkbox
                v-model="item.checked"
                :indeterminate="item.indeterminate"
                @change="handleChangeSub(item)"
                >{{ item.title }}</el-checkbox
              >
            </div>
            <div class="check-item-right">
              <el-checkbox-group v-model="item.checkedList" @change="handleChange(item, mockData)">
                <el-checkbox
                  :value="subitem.title"
                  v-for="(subitem, index) in item.qxChildren"
                  :key="index"
                  >{{ subitem.title }}</el-checkbox
                >
              </el-checkbox-group>
            </div>
          </div>
          <div v-else>
            <div>
              <div class="check-child-top">
                <el-checkbox
                  v-model="item.checked"
                  :indeterminate="item.indeterminate"
                  @change="handleChangeChildAll(item)"
                  >{{ item.title }}
                </el-checkbox>
                <icon-down @click="changeChildExpand" style="cursor: pointer; margin-left: 8px" />
              </div>
              <div class="check-item-wrapper" v-show="childExpand">
                <div class="check-item" v-for="(subItem, index) in item.children" :key="index">
                  <div class="check-item-left indent">
                    <el-checkbox
                      v-model="subItem.checked"
                      :indeterminate="subItem.indeterminate"
                      @change="handleChildChangeSub(subItem, item)"
                      >{{ subItem.title }}</el-checkbox
                    >
                  </div>
                  <div class="check-item-right">
                    <el-checkbox-group
                      v-model="subItem.checkedList"
                      @change="handleChange(subItem, item)"
                    >
                      <el-checkbox
                        :value="childItem.title"
                        v-for="(childItem, index) in subItem.qxChildren"
                        :key="index"
                        >{{ childItem.title }}</el-checkbox
                      >
                    </el-checkbox-group>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const indeterminate = ref(false)
const expand = ref(true)
const childExpand = ref(true)
let qxChildrenTotalCount = ref(0)

const emits = defineEmits(['updateTotalChecked'])

const changeExpand = () => {
  expand.value = !expand.value
}
const changeChildExpand = () => {
  childExpand.value = !childExpand.value
}

const propData = defineProps({
  mockData: {
    type: Object,
    default: () => {
      return reactive({
        title: '主控台',
        checked: true,
        indeterminate: false,
        checkedList: [],
        children: [
          {
            title: '主控台',
            checked: true,
            indeterminate: false,
            checkedList: [],
            qxChildren: [
              {
                title: '查看',
                checked: true
              },
              {
                title: '编辑',
                checked: true
              }
            ]
          },
          {
            title: '样板间',
            checked: true,
            indeterminate: false,
            checkedList: [],
            qxChildren: [
              {
                title: '查看',
                checked: true
              },
              {
                title: '编辑',
                checked: true
              }
            ]
          }
          // {
          //   title: '数据资产',
          //   checked: true,
          //   indeterminate: false,
          //   checkedList: [],
          //   children: [
          //     {
          //       title: '数据源',
          //       checked: true,
          //       indeterminate: false,

          //       children: [
          //         {
          //           title: '查看',
          //           checked: true,
          //         },
          //         {
          //           title: '编辑',
          //           checked: true,
          //         },
          //       ]
          //     },
          //     {
          //       title: '语料库',
          //       checked: true,
          //       indeterminate: false,

          //       children: [
          //         {
          //           title: '查看',
          //           checked: true,
          //         },
          //         {
          //           title: '编辑',
          //           checked: true,
          //         },
          //       ]
          //     }
          //   ]
          // }
        ]
      })
    }
  }
})

const { mockData } = toRefs(propData)

onMounted(() => {
  // console.log(mockData.value);
  qxChildrenTotalCount.value = countQxFn(mockData.value)
})

// 当前表头的总选项
const handleChangeAll = (value: any) => {
  console.log(value)
  indeterminate.value = false
  if (value) {
    mockData.value.checkedList = mockData.value.children.map((item: any) => item.title)
    mockData.value.children.forEach((item: any) => {
      item.checked = true
      if (item.qxChildren) {
        handleChangeSub(item)
      }
      if (item.children) {
        item.children.forEach((subItem: any) => {
          subItem.checked = true
          handleChangeSub(subItem)
        })
        item.checkedList = item.children.map((subItem: any) => subItem.title)
      }
      // handleChangeSub(item)
    })
    // console.log(countCheckedChild(mockData.value), mockData.value)
  } else {
    mockData.value.children.forEach((item: any) => {
      item.checked = false
      if (item.qxChildren) {
        mockData.value.checkedList = []
      }
      handleChangeSub(item)
      if (item.children) {
        // console.log(2222222);

        item.children.forEach((subItem: any) => {
          subItem.checked = false
          subItem.checkedList = []
          handleChangeSub(subItem)
        })
      }
    })
  }
}
// 当前children表头的总选项
const handleChangeChildAll = (obj: any) => {
  console.log(obj)
  indeterminate.value = false
  if (obj.checked) {
    // mockData.value.value.checked = value
    obj.checkedList = obj.children.map((item: any) => item.title)
    obj.children.forEach((item: any) => {
      item.checked = true
      if (item.qxChildren) {
        handleChangeSub(item)
      }
      if (item.children) {
        item.children.forEach((subItem: any) => {
          subItem.checked = true
          handleChangeSub(subItem)
        })
        item.checkedList = item.children.map((subItem: any) => subItem.title)
      }
      // handleChangeSub(item)
    })
    // console.log(countCheckedChild(obj), obj)
  } else {
    obj.children.forEach((item: any) => {
      item.checked = false
      if (item.qxChildren) {
        obj.checkedList = []
      }
      handleChangeSub(item)
      if (item.children) {
        item.children.forEach((subItem: any) => {
          subItem.checked = false
          subItem.checkedList = []
        })
      }
    })
    // obj.value.checked = value
  }
}
// 第一列项
const handleChangeSub = (itemObj: any) => {
  console.log(itemObj)
  if (itemObj.checked) {
    itemObj.checkedList = itemObj.qxChildren.map(
      (item: { title: string; checked: boolean }) => item.title
    )
  } else {
    itemObj.checkedList = []
  }
  if (itemObj.children) {
    itemObj.children.map((subItem: any) => {
      handleChangeSub(subItem)
    })
  }
  console.log(mockData.value)

  countCheckedChild()
}
// children第一列项
const handleChildChangeSub = (itemObj: any, pObj: any) => {
  console.log(itemObj, pObj)
  if (itemObj.checked) {
    itemObj.checkedList = itemObj.qxChildren.map(
      (item: { title: string; checked: boolean }) => item.title
    )
  } else {
    itemObj.checkedList = []
  }
  if (itemObj.children) {
    itemObj.children.map((subItem: any) => {
      handleChildChangeSub(subItem, pObj)
    })
  }
  // console.log(countCheckedChildFn(pObj), countQxFn(pObj));
  checkCurChildStatus(pObj)
  countCheckedChild()
}
// 传入object的qx子项数量
const countQxFn = (obj: any, acc = 0): any => {
  let temp = acc
  if (obj.qxChildren?.length) {
    temp += obj.qxChildren.length
  }
  if (obj.children?.length) {
    return (temp = obj.children.reduce((acc: number, cur: object) => {
      acc += countQxFn(cur, temp)
      return acc
    }, temp))
  }
  return temp
}
// 所有子项数量
// const qxChildrenTotalCountFn = (obj: any): any => {
//   if (obj.children?.length) {
//     obj.children.map((it: any) => qxChildrenTotalCountFn(it))
//   }
//   if (obj.qxChildren?.length) {
//     qxChildrenTotalCount.value += obj.qxChildren.length
//   }
//   // console.log(obj, qxChildrenTotalCount.value);
// }
// 选择子项数量
const countCheckedChildFn = (obj: any, acc = 0): any => {
  let temp = acc
  if (obj.qxChildren?.length) {
    temp += obj.checkedList.length
    console.log(obj, obj.checkedList, obj.checkedList.length, temp)
  }
  if (obj.children?.length) {
    return (temp = obj.children.reduce((acc: number, cur: object) => {
      acc += countCheckedChildFn(cur, temp)
      console.log(acc)
      return acc
    }, temp))
  }
  return temp
}
// 总选项状态
const countCheckedChild = (): any => {
  var totalCheckStatus = 'all'
  const qxChildrenCheckListCount = countCheckedChildFn(mockData.value)
  console.log(qxChildrenCheckListCount, qxChildrenTotalCount.value)

  if (qxChildrenCheckListCount === qxChildrenTotalCount.value) {
    totalCheckStatus = 'all'
  } else if (qxChildrenCheckListCount === 0) {
    totalCheckStatus = 'empty'
  } else {
    totalCheckStatus = 'some'
  }
  if (totalCheckStatus === 'all') {
    mockData.value.indeterminate = false
    mockData.value.checked = true
    mockData.value.children.forEach((item: any) => (item.checked = true))
  } else if (totalCheckStatus === 'empty') {
    mockData.value.indeterminate = false
    mockData.value.checked = false
  } else {
    mockData.value.checked = false
    mockData.value.indeterminate = true
  }
  emits('updateTotalChecked', totalCheckStatus)
  return totalCheckStatus
}
// 查看编辑点击
const handleChange = (curObj: any, pObj: any) => {
  // console.log(curObj, pObj);
  if (curObj.qxChildren.length === curObj.checkedList.length) {
    curObj.checked = true
    curObj.indeterminate = false
  } else if (curObj.checkedList.length === 0) {
    curObj.checked = false
    curObj.indeterminate = false
  } else {
    curObj.checked = false
    curObj.indeterminate = true
  }

  checkCurChildStatus(pObj)

  countCheckedChild()
}

// 检查当前children项的选择状态
const checkCurChildStatus = (pObj: any) => {
  var checkedCount = countCheckedChildFn(pObj)
  var qxTotalCount = countQxFn(pObj)
  if (checkedCount === qxTotalCount) {
    pObj.checked = true
    pObj.indeterminate = false
  } else if (checkedCount === 0) {
    pObj.checked = false
    pObj.indeterminate = false
  } else {
    pObj.checked = false
    pObj.indeterminate = true
  }
}

defineExpose({
  handleChangeAll
})
</script>

<style lang="scss">
.check-box-group {
  width: 100%;
  display: block;

  .check-main {
    border: 1px solid #e5e6eb;
    border-radius: 4px;
    transition: all 1s ease;

    .check-child-top {
      padding: 14px 24px;
      position: relative;
      border-top: 1px solid #e5e6eb;

      &::after {
        content: '';
        width: 1px;
        height: 52px;
        background-color: #e5e6eb;
        display: inline-block;
        position: absolute;
        top: 0;
        left: 146px;
      }
    }

    .check-top {
      background-color: #f2f3f5;
      padding: 14px 24px;
      position: relative;

      &::after {
        content: '';
        width: 1px;
        height: 52px;
        background-color: #e5e6eb;
        display: inline-block;
        position: absolute;
        top: 0;
        left: 146px;
      }
    }

    .check-item-wrapper {
      transition: all 1s ease;

      .check-item {
        display: flex;

        &-left {
          // text-align: center;
          width: 146px;
          padding: 14px 11px 14px 25px;
          // box-sizing: border-box;
          border-right: 1px solid #e5e6eb;
          border-top: 1px solid #e5e6eb;
        }

        .indent {
          text-align: center;
        }

        &-right {
          padding: 14px 6px 14px 24px;
          box-sizing: border-box;
          border-top: 1px solid #e5e6eb;
          width: 100%;
          // &:last-child{
          //   border-bottom: none;
          // }
        }
      }
    }
  }
}
</style>
