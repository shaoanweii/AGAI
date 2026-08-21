<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { fmtPer, fmtFix, fmtNum } from '@/utils'
import type { Placement } from 'element-plus'

defineOptions({
  name: 'ToolPop'
})

interface Props {
  sceneData?: any
  dataType?: string
  queryType?: string
  toolName: string
  rowData: any
  disabled?: boolean
  placement?: Placement
  offset?:number
}

const props = withDefaults(defineProps<Props>(), {
  toolName: '',
  disabled: false,
  placement: 'top',
  offset:0,
  rowData: () => {}
})

const emit = defineEmits<{
  'reference-click': []
}>()

const title = ref<string>('')
const tableData = ref<any[]>([])

const translateData = () => {

  let row = props.rowData
  let toolName = props.toolName
  let o 
  
  // 获取标题 
  title.value = row.name

  // 获取表格数据

  // 共用： 提及量/占比 /  单独模块：观点TOP（卡片）
  if (['mentionCount', 'mentionRatio', 'opinionName'].includes(toolName)) {
    
    o  = {
      name: '提及量',
      value: fmtNum(row.mentionCount),
      mom: fmtFix(row.mentionRingRatio),
      yoy: fmtFix(row.mentionYearOnYearRatio)
    }

    if (toolName === 'mentionRatio') {
      o  = {
        name: '占比',
        value: fmtPer(row.mentionRatio),
        mom: fmtFix(row.mentionRingRatio),
        yoy: fmtFix(row.mentionYearOnYearRatio)
      }
    }

    if (toolName === 'opinionName') {

      title.value = row.opinionName
      o.name = '提及量'
    }


  }

  // 负面率
  else if (toolName === 'negativeRate') {
    o = {
      name: '负面率',
      value: fmtPer(row.negativeRate),
      mom: fmtFix(row.negativeRingRatio),
      yoy: fmtFix(row.negativeYearOnYearRatio)
    }
  }

  // 场景top
  else if (toolName === 'focusSceneTop3') {
    let sceneData = props.sceneData
    title.value += '-' + sceneData.sceneName
    
    o = {
      name: props.dataType === 'mention' ? '提及量' : '负面率',
      value: props.dataType === 'mention' ? fmtNum(sceneData.value) : fmtPer(sceneData.value),
      mom: fmtFix(sceneData.ringRatio),
      yoy: fmtFix(sceneData.yearOnYearRatio)
    }
  }

  if (o) tableData.value = [o]
  
}


onMounted(() => {
  translateData()
})

// 监听 props 变化，重新转换数据
watch(
  () => [props.rowData, props.toolName, props.sceneData, props.dataType],
  () => {
    translateData()
  },
  { deep: true }
)

/**
 * 转发 reference 区域点击事件。
 * `el-popover` 会接管 reference 插槽根节点，业务侧直接在插槽内容上绑定 click
 * 时可能失效，因此统一由 ToolPop 对外抛出组件事件供上层联动使用。
 */
const handleReferenceClick = () => {
  emit('reference-click')
}

</script>


<template>
  <el-popover
    :placement="placement"
    :offset="offset"
    :show-after="200"
    :disabled="props.disabled"
    :width="410"
    trigger="hover"
    popper-class="tool-pop"
    >
    <template #reference>
      <div class="tool-pop-reference" @click="handleReferenceClick">
        <slot name="popBtn" ></slot>
      </div>
    </template>
    <template #default>

      <div class="fs-14 fw-500 mb-12" style="color: #333">{{ title }}</div>
      
      <el-table :data="tableData" class="pop-table">
        <el-table-column prop="name" label="名称" width="70" />
        <el-table-column prop="value" label=数值 />
        <el-table-column prop="mom" label="环比" class-name="c666" />
        <el-table-column prop="yoy" label="同比" class-name="c666" />
      </el-table>

    </template>
  </el-popover>
</template>

<style lang="scss" scoped>
.tool-pop-reference {
  width: 100%;
}

.el-table {
 .flex {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;

    :deep(.mod){
      height: 30px;
      line-height: 30px;
      margin:0 10px;
      width:calc(33.3% - 20px);
      text-align: center;
      background-color: #f5f5f5;
    }
  }
}
</style>
