<template>
  <div class="login-wrapper">
    <div class="top-logo">
      <img class="logo-mark" :src="voiceLogo" alt="声音洞察引擎" />
      <div class="brand-copy">
        <h1 class="tl-title">声音洞察引擎</h1>
        <span>AGAI Voice Insight</span>
      </div>
    </div>

    <el-card class="login-card" :body-style="{ padding: '48px 48px 77px' }">
      <h3>欢迎登录</h3>
      <h2>声音洞察引擎</h2>
      <el-form class="form" :model="form" label-width="0px">
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
    </el-card>
    <div class="copy-right">
      <!-- <p>©️2021 北京富通东方科技有限公司 京ICP备18028478号-1</p> -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { getRandomImage } from '@/api/main'
import useUserStore from '@/stores/modules/user'
import { ElMessage } from 'element-plus'

const voiceLogo = '/workspace-assets/logos/voice-insight-logo.png'

const state = reactive({
  loading: false,
  codeSrc: '',
  timeKey: ''
})
onMounted(() => {
  getCode()
})

// 本地 standalone 调试链路默认账号，避免影响其他构建模式。
const defaultLoginForm =
  import.meta.env.MODE === 'standalone'
    ? {
        username: 'admin',
        password: 'Passw0rd@!',
        captcha: '2587'
      }
    : {
        username: '',
        password: '',
        captcha: ''
      }

const form = reactive({
  username: defaultLoginForm.username,
  password: defaultLoginForm.password,
  // "appId": "insights",
  // "type": "base",
  checkKey: '',
  captcha: defaultLoginForm.captcha
})

let router = useRouter()
const doLogin = async () => {
  form.checkKey = state.timeKey
  useUserStore()
    .login(form)
    .then(() => {
      // router.push('/dataCenter/processing');
      router.push('/')
    })
    .catch((err: any) => {
      state.loading = false
      ElMessage.error(err.message || '登录失败，请重试！')
      console.log(err)
      getCode()
    })
}

//获取验证码
const getCode = () => {
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
  background: url(../assets/bg/login.png);
  background-size: 100% auto;
  position: relative;
  .top-logo {
    position: absolute;
    top: 59px;
    left: 90px;
    display: flex;
    align-items: center;
    .logo-mark {
      width: 40px;
      height: 40px;
      object-fit: contain;
    }
    .brand-copy {
      display: flex;
      flex-direction: column;
      justify-content: center;
      width: 114px;
      margin-left: 16px;
    }
    .tl-title {
      font-size: 24px;
      font-weight: bold;
      line-height: 28px;
      margin: 0;
      white-space: nowrap;
    }
    span {
      margin-top: 3px;
      font-size: 12px;
      font-weight: 600;
      color: #5f7fac;
      line-height: 14px;
      letter-spacing: 3px;
      white-space: nowrap;
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
