<template>
  <div class="login-wrapper">
    <div class="top-logo">
      <img class="logo" src="@/assets/images/brand/voc-voice-mark-v2.png" alt="VOC智声" />
      <h1 class="tl-title">VOC智声</h1>
    </div>

    <el-card class="login-card" :body-style="{ padding: '48px 48px 77px' }">
      <h3>欢迎登录</h3>
      <h2>VOC智声</h2>
      <p v-if="isLocalDemo()" class="demo-description">全链路客户体验洞察与事件闭环演示平台</p>
      <el-form v-if="!isLocalDemo()" class="form" :model="form" label-width="0px">
        <!-- <el-form class="form" :model="form" size="large" v-loading="state.loading"> -->
        <el-form-item>
          <el-input
            :data-testid="`login-1001`"
            placeholder="账号"
            v-model.trim="form.username"
            autocomplete="username"
            clearable
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            :data-testid="`login-1002`"
            type="password"
            placeholder="密码"
            v-model.trim="form.password"
            autocomplete="current-password"
            clearable
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <div class="code-box flex">
            <el-input
              :data-testid="`login-1003`"
              placeholder="请输入验证码"
              v-model.trim="form.captcha"
              clearable
              style="flex: 1"
              @keyup.enter="doLogin"
              size="large"
            />
            <div class="img-box" :title="'点击刷新'" :data-testid="`login-1005`" @click="getCode">
              <img :src="state.codeSrc" alt="" />
            </div>
          </div>
          <!-- <el-checkbox v-model="form.keep" label="在浏览器中保持登录" size="large" /> -->
        </el-form-item>
        <el-form-item>
          <div :data-testid="`login-1004`" class="btn" style="width: 100%" @click="doLogin">
            登 录
          </div>
        </el-form-item>
      </el-form>
      <div v-else class="demo-entry">
        <div class="demo-account">
          <span class="demo-account__avatar">演</span>
          <div>
            <strong>演示管理员</strong>
            <p>拥有 PC 与 H5 全部演示权限</p>
          </div>
        </div>
        <el-button type="primary" size="large" class="demo-entry__button" :loading="state.loading" @click="doLogin">
          一键进入 VOC智声
        </el-button>
      </div>
    </el-card>
    <div class="copy-right">VOC智声 · 离线产品演示</div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { getRandomImage } from '@/api/main/index'
import useUserStore from '@/store/modules/user'
import { ElMessage } from 'element-plus'
import { onMounted, reactive } from 'vue'
import { removeWatermark } from '@/utils/index.ts'
import { isLocalDemo } from '@/utils/env'

const state = reactive({
  loading: false,
  codeSrc: '',
  timeKey: ''
})
onMounted(() => {
  removeWatermark()
  getCode()
})

const form = reactive({
  username: '',
  password: '',
  // "appId": "insights",
  // "type": "base",
  checkKey: '',
  captcha: ''
})

let router = useRouter()
const userStore = useUserStore()

const doLogin = async () => {
  state.loading = true
  form.checkKey = state.timeKey
  userStore
    .login(form)
    .then(() => {
      // router.push('/dataCenter/processing');
      // router.push('/')
      setTimeout(() => {
        router.push(userStore.homePath)
      }, 500)
    })
    .catch(() => {
      state.loading = false
      if (!isLocalDemo()) getCode()
    })
}

//获取验证码
const getCode = () => {
  if (isLocalDemo()) return
  let timeKey = new Date().getTime()
  state.timeKey = timeKey.toString()

  getRandomImage(timeKey).then(res => {
    state.codeSrc = res.result
  })
}
</script>

<style lang="scss" scoped>
.login-wrapper {
  height: 100vh;
  background: url(../../assets/images/login_bg.png);
  background-size: 100% auto;
  position: relative;
  .top-logo {
    position: absolute;
    top: 59px;
    left: 90px;
    display: flex;
    align-items: center;
    .logo {
      /* position: absolute;
    top: 54px;
    left: 90px; */
      height: 40px;
      width: 40px;
      border-radius: 9px;
    }
    .tl-title {
      font-size: 24px;
      font-weight: bold;
      margin-left: 16px;
    }
  }

  .login-card {
    position: absolute;
    right: 10vw;
    bottom: 20vh;
    border-radius: 4px;

    h3 {
      font-size: 20px;
      color: var(--color-high);
    }

    h2 {
      font-size: 28px;
      color: var(--color-high);
      margin-top: 6px;
      height: 48px;
      line-height: 48px;
      // font-weight: bold;
    }

    .code-box {
      width: 100%;
      justify-content: space-between;
    }

    .img-box {
      width: 144px;
      // border: var(--border);
      margin-left: 12px;
      // border-radius: 4px;
      background-color: #f2f3f7;
      display: flex;
      align-items: center;
      cursor: pointer;

      img {
        width: 100%;
        height: 40px;
      }
    }

    .form {
      margin-top: 23px;
      width: 330px;

      :deep(.el-input-wrapper) {
        .el-input {
          height: 46px;
          box-sizing: border-box;

          &:-webkit-autofill {
            -webkit-text-fill-color: #000 !important; /*记住密码的字的颜色*/
            transition: background-color 5000s ease-in-out 0s; /*延时渲染背景色来去除背景色*/
            //caret-color: #acfff2;/*光标颜色*/
          }
        }
      }

      :deep(.el-form-item) {
        margin-bottom: 12px;
      }
    }

    .demo-description {
      color: #667085;
      margin-top: 8px;
    }

    .demo-entry {
      margin-top: 28px;
      width: 330px;
    }

    .demo-account {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 16px;
      border: 1px solid #dbe4f0;
      border-radius: 10px;
      background: #f7f9fc;

      p {
        color: #667085;
        font-size: 13px;
        margin-top: 4px;
      }
    }

    .demo-account__avatar {
      width: 42px;
      height: 42px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      background: #165dff;
      font-weight: 600;
    }

    .demo-entry__button {
      width: 100%;
      margin-top: 24px;
    }

    .btn {
      background-color: #165dff;
      border-radius: 4px;
      padding: 14px 148px;
      color: #fff;
      margin-top: 36px;
      line-height: 1;
    }
  }

  .copy-right {
    width: 100%;
    position: fixed;
    bottom: 20px;
    text-align: center;
    opacity: 0.8;
  }
}
</style>
