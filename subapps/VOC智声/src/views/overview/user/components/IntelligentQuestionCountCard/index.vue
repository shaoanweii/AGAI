<script setup lang="ts">
import { useAppStore } from '@/store'
import { useUserStore } from '@/store/modules/user'
import { hasCanswerMenuPermission } from '@/utils/permission'
import { debounce } from 'lodash-es'
import { computed } from 'vue'

defineOptions({
  name: 'IntelligentQuestionCountCard'
})

const appStore = useAppStore()
const userStore = useUserStore()

/**
 * 判断当前用户是否具备智能问数菜单权限。
 */
const hasCanswerMenu = computed(() => {
  return hasCanswerMenuPermission(userStore.menus || [])
})

const handleLink = debounce(() => {
  if (!hasCanswerMenu.value) return
  appStore.handleCanswerAuth()
}, 300)
</script>

<template>
  <div v-if="hasCanswerMenu" class="intelligent-question-count-card" @click="handleLink">
    <img src="@/assets/images/iqcc_bgv2.png" class="iqcc-logo" alt="" />
    <!-- <div class="iqcc-title">你好，欢迎使用智能问数～</div>
    <div class="iqcc-input-container flex-y-center pl-16 pr-4">
      <img src="@/assets/images/voiceprint-fill.png" alt="" class="w-24 h-24" />
      <input
        type="text"
        class="flex-1 h-24 mr-8 ml-8"
        style="border: none; outline: none"
        placeholder="输入想要分析的内容关键词"
      />
      <div class="icon-bg">
        <img src="@/assets/images/arrow-right-line.png" alt="" class="w-20 h-20" />
      </div>
    </div>

    <div class="flex flex-wrap mt-16">
      <div class="lh-26 mr-10 text-body text-primary">快捷搜索</div>

      <div class="flex">
        <div class="tag-item" @click="handleClick">竞品对比</div>
        <div class="tag-item" @click="handleClick">新车上市</div>
        <div class="tag-item" @click="handleClick">智能化研究</div>
      </div>
    </div> -->
  </div>
</template>

<style lang="scss" scoped>
.intelligent-question-count-card {
  width: 100%;
  height: 206px;
  // background: linear-gradient(180deg, #8accff 0%, #c3eefd 100%);
  // border-radius: 20px 20px 20px 20px;
  // border: 1px solid;
  // border-image: linear-gradient(139deg, rgba(242, 249, 254, 1), rgba(242, 249, 254, 0)) 1 1;
  position: relative;
  cursor: pointer;
  // padding: 24px;
  // background-image: url(@/assets/images/svg/iqcc_bg.svg);
  // background-size: 100% 100%;
  // &::after {
  //   content: '';
  //   position: absolute;
  //   left: 0;
  //   right: 0;
  //   bottom: 0;
  //   top: 0;
  //   cursor: pointer;
  // }
  .iqcc-logo {
    width: 100%;
    height: 100%;
  }

  .iqcc-title {
    font-weight: 500;
    font-size: 24px;
    color: #0b457f;
    text-align: left;
    margin-left: 91px;
    line-height: 34px;
  }

  .iqcc-input-container {
    width: 100%;
    height: 48px;
    background: #ffffff;
    border-radius: 40px 40px 40px 40px;
    margin-top: 32px;

    .icon-bg {
      width: 40px;
      height: 40px;
      background: #e6f4fe;
      border-radius: 20px 20px 20px 20px;
      border: 1px solid rgba(255, 255, 255, 0.5);
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }

  .tag-item {
    min-width: 80px;
    height: 26px;
    padding: 4px 12px;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 24px 24px 24px 24px;
    border: 1px solid #f0f0f0;
    text-align: center;
    font-weight: 400;
    font-size: 14px;
    color: #1f2733;
    line-height: 18px;

    & + .tag-item {
      margin-left: 10px;
    }
  }
}
</style>
