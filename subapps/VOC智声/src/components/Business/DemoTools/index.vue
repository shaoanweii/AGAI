<template>
  <el-button type="primary" plain size="large" @click="visible = true">演示工具</el-button>
  <el-dialog v-model="visible" title="VOC智声演示工具" width="520px">
    <div class="demo-tools">
      <div class="demo-tools__status">
        <span class="demo-tools__dot"></span>
        本地服务运行正常
      </div>
      <div class="demo-tools__content">
        <img v-if="qrCode" :src="qrCode" class="demo-tools__qr" alt="VOC智声 H5 二维码" />
        <div class="demo-tools__info">
          <h4>H5 真机演示</h4>
          <p>手机与电脑连接同一局域网后扫码访问。</p>
          <el-input :model-value="runtime.h5Url" readonly>
            <template #append>
              <el-button @click="copyH5Url">复制</el-button>
            </template>
          </el-input>
          <el-button class="demo-tools__preview" @click="openH5Preview">桌面预览 H5</el-button>
        </div>
      </div>
      <el-divider />
      <div class="demo-tools__reset">
        <div>
          <h4>恢复演示数据</h4>
          <p>清除本地操作并恢复预置业务场景。</p>
        </div>
        <el-button type="danger" plain :loading="resetting" @click="resetDemoData">一键恢复</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QRCode from 'qrcode'

interface DemoRuntime {
  pcUrl: string
  h5Url: string
  resetAt: string
}

const visible = ref(false)
const resetting = ref(false)
const qrCode = ref('')
const runtime = reactive<DemoRuntime>({ pcUrl: '', h5Url: '', resetAt: '' })

/**
 * 获取本地服务地址并生成完全离线的 H5 二维码。
 */
const loadRuntime = async () => {
  const response = await fetch('/api/local/runtime')
  const data = await response.json()
  Object.assign(runtime, data.result || {})
  if (runtime.h5Url) qrCode.value = await QRCode.toDataURL(runtime.h5Url, { width: 180, margin: 1 })
}

/**
 * 复制 H5 真机访问地址。
 */
const copyH5Url = async () => {
  await navigator.clipboard.writeText(runtime.h5Url)
  ElMessage.success('H5 地址已复制')
}

/**
 * 在当前桌面窗口中打开 H5 预览页。
 */
const openH5Preview = () => {
  window.open('/#/h5/home', '_blank')
}

/**
 * 恢复固定种子的合成业务数据并刷新当前页面。
 */
const resetDemoData = async () => {
  await ElMessageBox.confirm('恢复后，本次演示产生的数据将被清除。是否继续？', '恢复演示数据', {
    confirmButtonText: '恢复',
    cancelButtonText: '取消',
    type: 'warning'
  })
  resetting.value = true
  try {
    const response = await fetch('/api/local/admin/reset', { method: 'POST' })
    if (!response.ok) throw new Error('恢复失败')
    ElMessage.success('演示数据已恢复')
    window.location.reload()
  } finally {
    resetting.value = false
  }
}

onMounted(() => {
  loadRuntime().catch(() => ElMessage.error('无法读取本地演示服务状态'))
})
</script>

<style scoped lang="scss">
.demo-tools__status {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #027a48;
  font-weight: 600;
  margin-bottom: 20px;
}

.demo-tools__dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #12b76a;
  box-shadow: 0 0 0 4px #ecfdf3;
}

.demo-tools__content {
  display: flex;
  gap: 24px;
  align-items: center;
}

.demo-tools__qr {
  width: 180px;
  height: 180px;
  border: 1px solid #eaecf0;
  border-radius: 12px;
}

.demo-tools__info {
  flex: 1;

  p {
    color: #667085;
    margin: 8px 0 16px;
    line-height: 1.6;
  }
}

.demo-tools__preview {
  margin-top: 12px;
}

.demo-tools__reset {
  display: flex;
  justify-content: space-between;
  align-items: center;

  p {
    color: #667085;
    margin-top: 6px;
  }
}
</style>
