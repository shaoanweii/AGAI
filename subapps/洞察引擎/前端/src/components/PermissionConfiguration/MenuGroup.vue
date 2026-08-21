<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    auth: Api.Role.PermissionTree
    testid?: string
  }>(),
  {}
)
const { auth } = toRefs(props)

const isRadioPermission = (node?: Api.Role.PermissionTree | null) => node?.icon === 'radio'

const getCheckboxChildren = (node?: Api.Role.PermissionTree | null) =>
  node?.children?.filter(child => !isRadioPermission(child)) ?? []

const getRadioChildren = (node?: Api.Role.PermissionTree | null) =>
  node?.children?.filter(child => isRadioPermission(child)) ?? []

const getSelectedRadioId = (node: Api.Role.PermissionTree) => {
  const selectedRadio = getRadioChildren(node).find(child => child.checked)
  return selectedRadio?.id
}

/**
 * 初始化 radio 组默认值：无选中时默认选中第一项；多选时仅保留第一项
 * @param node
 */
const initRadioDefaultChecked = (node: Api.Role.PermissionTree) => {
  const radioChildren = getRadioChildren(node)
  if (radioChildren.length > 0) {
    const checkedRadioChildren = radioChildren.filter(child => child.checked)
    if (checkedRadioChildren.length === 0) {
      radioChildren[0].checked = true
    } else if (checkedRadioChildren.length > 1) {
      const firstCheckedId = checkedRadioChildren[0].id
      radioChildren.forEach(child => {
        child.checked = child.id === firstCheckedId
      })
    }
  }

  if (node.children && node.children.length > 0) {
    node.children.forEach(child => initRadioDefaultChecked(child))
  }
}

// const changeExpand = (item: Api.Role.PermissionTree) => {
//   item.expand = !item.expand
// }

/**
 * 计算当前节点的indeterminate状态
 * @param node
 */
function updateIndeterminateStatus(node: Api.Role.PermissionTree) {
  // radio 节点独立控制，不参与父级 checkbox 的全选/半选计算
  const checkboxChildren = getCheckboxChildren(node)
  if (checkboxChildren.length > 0) {
    const allChecked = checkboxChildren.every((child: Api.Role.PermissionTree) => child.checked)
    const anyChecked = checkboxChildren.some((child: Api.Role.PermissionTree) => child.checked)
    node.indeterminate = anyChecked && !allChecked
  } else {
    node.indeterminate = false
  }

  // 当前节点为半选状态并且父节点不是根节点时，更新父节点的状态
  if (node.indeterminate && node.pid !== '0') {
    updateParentStates(auth.value, node)
  }

  // 如果当前节点有子节点，递归更新子节点
  if (node.children && node.children?.length > 0) {
    node.children.forEach((child: Api.Role.PermissionTree) => {
      updateIndeterminateStatus(child)
    })
  }
}

onMounted(() => {
  initRadioDefaultChecked(auth.value)
  updateIndeterminateStatus(auth.value)
})

/**
 * 全选/取消全选， 传入的子级节点
 * @param checked
 * @param tree
 */
const chenckedAllChange = (checked: boolean, tree: Api.Role.PermissionTree) => {
  tree.checked = checked
  tree.indeterminate = false

  const radioChildren = getRadioChildren(tree)
  if (radioChildren.length > 0) {
    if (!checked) {
      radioChildren.forEach(item => {
        item.checked = false
      })
    } else {
      const checkedRadioChildren = radioChildren.filter(item => item.checked)
      if (checkedRadioChildren.length === 0) {
        radioChildren[0].checked = true
      } else if (checkedRadioChildren.length > 1) {
        const firstCheckedId = checkedRadioChildren[0].id
        radioChildren.forEach(item => {
          item.checked = item.id === firstCheckedId
        })
      }
    }
  }

  const checkboxChildren = getCheckboxChildren(tree)
  if (checkboxChildren.length > 0) {
    checkboxChildren.forEach(item => {
      item.checked = checked
      item.indeterminate = false
      if (item.children && item.children.length > 0) {
        chenckedAllChange(checked, item)
      }
    })
  }
}

/**
 * 根据pid获取父级节点
 * @param parentNode
 * @param cur
 */
const findParentNodeById = (
  parentNode: Api.Role.PermissionTree,
  cur: Api.Role.PermissionTree
): Api.Role.PermissionTree | undefined => {
  if (parentNode.id === cur.pid) return parentNode

  if (parentNode.children && parentNode.children.length > 0) {
    for (let child of parentNode.children) {
      let foundParent = findParentNodeById(child, cur)
      if (foundParent) {
        return foundParent
      }
    }
  }
  return undefined
}

/**
 * 更新选中与半选状态
 * @param node
 */
const updatNodeStates = (node: Api.Role.PermissionTree) => {
  const checkboxChildren = getCheckboxChildren(node)
  if (!checkboxChildren.length) {
    node.indeterminate = false
    return
  }

  const checkedResult = checkboxChildren.filter(el => el.checked)
  const indeterminateResult = checkboxChildren.filter(el => el.indeterminate)

  if (checkedResult.length === checkboxChildren.length) {
    node.checked = true
    node.indeterminate = false
  } else if (checkedResult.length === 0) {
    node.checked = false
    node.indeterminate = false
  } else {
    node.checked = false
    node.indeterminate = true
  }
  // 子级没有全选，并且子级有半选状态的时候，更新节点为半选状态
  if (checkedResult.length !== checkboxChildren.length && indeterminateResult.length > 0) {
    node.checked = false
    node.indeterminate = true
  }
}

/**
 * 更新父节点的状态
 * @param parentNode
 * @param node
 */
const updateParentStates = (parentNode: Api.Role.PermissionTree, node: Api.Role.PermissionTree) => {
  const parent = findParentNodeById(parentNode, node)
  if (parent) {
    updatNodeStates(parent)
    updateParentStates(parentNode, parent)
  } else {
    updatNodeStates(node)
  }
}

const firstCheckedChange = (val: boolean) => {
  if (auth.value.children && auth.value.children.length > 0) {
    chenckedAllChange(val, auth.value)
  }
}

const checkedChange = (val: boolean, item: Api.Role.PermissionTree) => {
  if (item.children && item.children.length > 0) {
    chenckedAllChange(val, item)
  }
  updateParentStates(auth.value, item)
}

const radioChange = (val: string | number, item: Api.Role.PermissionTree) => {
  const selectedId = String(val)
  const radioChildren = getRadioChildren(item)
  radioChildren.forEach(subitem => {
    subitem.checked = subitem.id === selectedId
  })
  updateParentStates(auth.value, item)
}

defineExpose({
  chenckedAllChange
})
</script>

<template>
  <div class="group" :data-testid="`${testid}-permission-group`">
    <div class="g-header">
      <div class="g-left" :data-testid="`${testid}-permission-g-l-h`">
        <el-checkbox
          v-model="auth.checked"
          :indeterminate="auth.indeterminate"
          :data-testid="`${testid}-permission-g-l-h-check`"
          @change="(val: any) => firstCheckedChange(val)"
        >
          {{ auth.name }}
        </el-checkbox>
        <!--箭头，先注释如有需要打开注释-->
        <!--<template v-if="auth.children">-->
        <!--  <icon-down v-if="!auth.expand" @click="changeExpand(auth)" style="cursor: pointer; margin-left: 8px"/>-->
        <!--  <icon-right v-if="auth.expand" @click="changeExpand(auth)" style="cursor: pointer; margin-left: 8px"/>-->
        <!--</template>-->
      </div>
      <div class="g-right">
        <template v-if="auth.children?.[0].checkButton">
          <el-checkbox
            v-for="(item, index) in auth?.children"
            :data-testid="`${testid}-permission-g-r-${index}`"
            v-model="item.checked"
            :key="index"
            class="mr-16"
            @change="(val: any) => checkedChange(val, item)"
          >
            {{ item.name }}
          </el-checkbox>
        </template>
      </div>
    </div>
    <template v-if="!auth.children?.[0].checkButton">
      <div class="g-body" v-show="!auth.expand" :data-testid="`${testid}-permission-body`">
        <template v-for="(item, index) in auth.children" :key="index">
          <div
            class="gr g-border-top"
            v-if="item.children?.[0].checkButton"
            :data-testid="`${testid}-permission-g-b-${index}`"
          >
            <div class="g-left" :data-testid="`${testid}-permission-g-b-l-${index}`">
              <el-checkbox
                v-model="item.checked"
                :indeterminate="item.indeterminate"
                :data-testid="`${testid}-permission-g-b-l-check-${index}`"
                @change="(val: any) => checkedChange(val, item)"
              >
                {{ item.name }}
              </el-checkbox>
            </div>
            <div class="g-right">
              <template v-for="(subitem, iindex) in item?.children" :key="iindex">
                <el-checkbox
                  v-if="!isRadioPermission(subitem)"
                  :data-testid="`${testid}-permission-g-b-r-${iindex}-${index}`"
                  v-model="subitem.checked"
                  class="mr-16"
                  @change="(val: any) => checkedChange(val, subitem)"
                >
                  {{ subitem.name }}
                </el-checkbox>
              </template>
              <el-radio-group
                v-if="getRadioChildren(item).length > 0"
                :model-value="getSelectedRadioId(item)"
                @change="(val: any) => radioChange(val, item)"
                class="permission-radio-group ml-30"
              >
                <el-radio
                  v-for="(subitem, iindex) in getRadioChildren(item)"
                  :data-testid="`${testid}-permission-g-b-r-radio-${iindex}-${index}`"
                  :value="subitem.id"
                  :key="subitem.id"
                  class="mr-16"
                >
                  {{ subitem.name }}
                </el-radio>
              </el-radio-group>
            </div>
          </div>
          <template v-else>
            <div class="gr g-border-top">
              <div class="g-left">
                <el-checkbox
                  v-model="item.checked"
                  :indeterminate="item.indeterminate"
                  :data-testid="`${testid}-permission-g-b-t-l`"
                  @change="(val: any) => checkedChange(val, item)"
                >
                  {{ item.name }}
                </el-checkbox>
                <!--箭头，先注释如有需要打开注释-->
                <!--<template v-if="item.children">-->
                <!--  <icon-down v-if="!item.expand"  @click="changeExpand(item)" style="cursor: pointer; margin-left: 8px"/>-->
                <!--  <icon-right v-if="item.expand" @click="changeExpand(item)" style="cursor: pointer; margin-left: 8px"/>-->
                <!--</template>-->
              </div>
              <div class="g-right"></div>
            </div>
            <div
              v-for="(subItem, subItemIndex) in item.children"
              :key="subItemIndex"
              v-show="!item.expand"
              :data-testid="`${testid}-permission-g-b-t-b-${subItemIndex}`"
            >
              <div class="gr g-border-top">
                <div class="g-left pl-38">
                  <el-checkbox
                    v-model="subItem.checked"
                    :indeterminate="subItem.indeterminate"
                    :data-testid="`${testid}-permission-g-b-t-b-check1-${subItemIndex}-${index}`"
                    @change="(val: any) => checkedChange(val, subItem)"
                  >
                    {{ subItem.name }}
                  </el-checkbox>
                </div>
                <div class="g-right">
                  <el-checkbox
                    v-model="childItem.checked"
                    v-for="(childItem, iiindex) in subItem.children"
                    :data-testid="`${testid}-permission-g-b-t-b-check2-${iiindex}-${index}`"
                    @change="(val: any) => checkedChange(val, childItem)"
                    :value="childItem.name"
                    :key="iiindex"
                    class="mr-16"
                  >
                    {{ childItem.name }}
                  </el-checkbox>
                </div>
              </div>
            </div>
          </template>
        </template>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
$left: 147px;
$padding: 14px 0 14px 24px;
.pl-38 {
  padding-left: 38px !important;
}

.group {
  width: 100%;
  display: block;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  transition: all 1s ease;
  box-sizing: border-box;
  overflow: hidden;

  &::v-deep(.el-checkbox) {
    padding-left: 0;
    line-height: 20px;
  }

  .g-header {
    display: flex;
    background-color: #f2f3f5;
  }

  .gr {
    display: flex;
  }

  .g-left {
    width: $left;
    border-right: 1px solid #e5e6eb;
    padding: $padding;
    box-sizing: border-box;
  }

  .g-right {
    flex: 1;
    width: 0;
    padding: $padding;
  }

  .g-border-top {
    border-top: 1px solid #e5e6eb;
  }
}
</style>
