<script setup lang="ts">

import { ref, computed, onMounted, watch } from 'vue'
import { useQueryStore } from '@/store/modules/query'
import { debounce } from 'lodash-es'

defineOptions({
  name: 'SummaryCopy'
})


interface Props {
  type: string 
}

const props = withDefaults(defineProps<Props>(), {
  type:'1'
})


const queryStore = useQueryStore()
const storePms = queryStore.currentQueryParams

const loading = ref(true)
const theData = ref<any>('')

// 模拟接口 - TODO: 换成真实接口
const fetchData = () => {
  loading.value = true
  theData.value = ''
  setTimeout(() => {
    theData.value = '当前整体负面率为66.82%，表现不佳。重点问题集中于“日常维护/维修成本”和“高合密钥紊乱”两大高负面话题，数据主要来源于汽车之家和懂车帝。阿维塔011负面率最高，产品维度占比最大，口碑维度提及较低。整体呈现需重点优化产品质量与售后服务。'
    loading.value = false
  }, 1000)
}

const fetchDataDelay = debounce(fetchData, 300)

onMounted(() => {
  fetchData()
})

watch( () => ({
    startDate: storePms.startDate,
    endDate: storePms.endDate,
  }), () => {
    fetchDataDelay()
})

watch(() => storePms.brandCode, () => {
  if(  props.type === '1') fetchDataDelay()
})
watch(() => storePms.tempCode, () => {
  if(  props.type === '2') fetchDataDelay()
})

</script>



<template>
  <div class="report-summary" >
    <div class="logo">
      <img src="@/assets/images/ai-head.svg" width="100" height="100" alt="" />
    </div>
    <div class="content" >
      <div class="title-container">
        <!-- <div class="fh">“</div> -->
        <SvgIcon name="ld" width="28px" height="24px"></SvgIcon>
        <SvgIcon name="bgjd" width="80px" height="28px" class="ml-10"></SvgIcon>
        <div class="subtitle">(内容为模型生成，仅作为参考!)</div>
      </div>

      <div class="report-str-container"  v-loading="loading" >
        <div class="report-str" >
          {{ theData }}
        </div>
      </div>
    </div>
      
    <div class="cc-icon">
      <SvgIcon name="rd" width="28px" height="24px"></SvgIcon>
    </div>
  </div>
  
</template>

<style lang="scss" scoped>
.report-summary {

  width: calc(100% + 6px);
  // height: 146px;
  left:-3px;
  padding: 16px;
  margin:24px 0;
  position: relative;
  border-radius: 8px;
  display: flex;
  // align-items: center;
  background: linear-gradient(180deg, rgba(238, 248, 255, 0.8) 0%, rgba(238, 248, 255, 0) 100%);
  overflow: hidden;
  border: 3px solid transparent;
  text-align: justify;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border: 2px solid transparent;
    border-image: linear-gradient(180deg, rgba(138, 204, 255, 1), rgba(195, 238, 253, 1)) 2 2;
    border-radius: 12px; /* 稍微大于元素本身的圆角 */
    pointer-events: none; /* 确保点击能穿透伪元素 */
  }
  .logo {
    // width: 100px;
    // height: 100px;
    border-radius: 60px 60px 60px 60px;
    // background: #000;
  }
  .content {
    flex: 1;
    margin-left: 20px;
    .title-container {
      display: flex;
      .title {
        // font-weight: 500;
        // font-size: 20px;
        // color: #1f2733;
        // line-height: 28px;
      }
      .subtitle {
        font-weight: 500;
        font-size: 14px;
        color: #60b8eb;
        line-height: 28px;
        margin-left: 10px;
      }
    }
    .report-str-container {
      display: flex;
    }
    .report-str {
      padding-right:42px;
      margin-top: 8px;
      font-weight: 400;
      color: #1f2733;
      flex: 1;
      // max-height: 64px;
      overflow-y: auto;
      
      // UI调整
      font-size: 16px;
      line-height: 24px;
      min-height: 48px;
      max-height:72px;
    }

  }
  .cc-icon {
      position: absolute;
      right:20px;
      bottom: 32px;
      width: 28px;
      height: 29px;
    }
}
</style>
