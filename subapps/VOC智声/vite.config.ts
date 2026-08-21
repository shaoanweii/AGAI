import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import vueJsx from '@vitejs/plugin-vue-jsx'
import postcssPxToRem from 'postcss-pxtorem'
import { createLocalSvgIconsPlugin } from './build/svg-icons-plugin'

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')

  // 判断是否开启调试模式
  const isDebug = env.VITE_DEBUG === 'true'

  console.log(`🔧 构建模式: ${mode}`)
  console.log(`🐛 调试模式-isDebug: ${isDebug ? '开启' : '关闭'}`)
  return {
    // 不同环境的基础路径配置
    base: './',
    // base: '/report/',
    plugins: [
      createLocalSvgIconsPlugin(path.resolve(__dirname, 'src/assets/svg')),
      vue(),
      vueJsx({
        // 启用 JSX 和 TSX 支持
        include: [/\.[jt]sx$/]
      })
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
        '@assets': path.resolve(__dirname, './src/assets'),
        '@components': path.resolve(__dirname, './src/components'),
        '@views': path.resolve(__dirname, './src/views'),
        '@store': path.resolve(__dirname, './src/store'),
        '@api': path.resolve(__dirname, './src/api'),
        '@hooks': path.resolve(__dirname, './src/hooks'),
        '@utils': path.resolve(__dirname, './src/utils'),
        '@types': path.resolve(__dirname, './src/types'),
        '@constants': path.resolve(__dirname, './src/constants'),
        '@styles': path.resolve(__dirname, './src/styles'),
        '@layout': path.resolve(__dirname, './src/layout'),
        '@h5': path.resolve(__dirname, './src/views/H5')
      }
    },
    css: {
      preprocessorOptions: {
        scss: {
          // 全局引入scss变量
          additionalData: `@import "@/styles/_variables.scss";`
          // javascriptEnabled: true
        }
      },
      postcss: {
        plugins: [
          postcssPxToRem({
            rootValue: 37.5, // 设计稿宽度 / 10
            propList: ['*'],
            selectorBlackList: [
              // Element UI相关
              /^\.el-/,
              // Vant UI相关
              /^\.van-/,
              // 不转换根元素
              'html'
            ], // 忽略转换的类名
            minPixelValue: 2,
            mediaQuery: false,
            exclude: function (file: any) {
              // 排除node_modules目录
              if (/node_modules/.test(file)) {
                return true
              }
              // 排除非H5目录的所有文件
              if (!/src[/\\]views[/\\]H5/.test(file)) {
                return true
              }
              return false
            }
          })
        ]
      }
    },
    // 开发服务器配置
    server: {
      host: '0.0.0.0',
      port: 5173,
      proxy: {
        '^/api/review': {
          target: 'http://127.0.0.1:4174',
          changeOrigin: true
          // rewrite: path => path.replace(/^\/api/, '')
        },
        '/api': {
          target: 'http://127.0.0.1:4174/',
          changeOrigin: true
          // rewrite: path => path.replace(/^\/api/, '')
        }
      }
    },
    // 构建配置 - 根据调试模式动态配置
    build: {
      // sourcemap: true,
      // cssMinify: false
      sourcemap: isDebug,
      // 打包文件超过2M 警告提示（现代应用可接受的大小）
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          // 入口文件名（内容哈希，内容不变则 URL 不变，浏览器可复用缓存）
          entryFileNames: `assets/[name]-[hash].js`,
          // 块文件名
          chunkFileNames: `assets/[name]-[hash].js`,
          // 资源文件名 css 图片等等
          assetFileNames: `assets/[name]-[hash].[ext]`,
          // 超过 chunkSizeWarningLimit值 分包
          manualChunks(id) {
            // 第三方库分包
            if (id.includes('node_modules')) {
              const parts = id.split('node_modules/')[1].split('/')
              // 正确处理 @scope/pkg 格式的包名
              const packageName = parts[0].startsWith('@') ? `${parts[0]}/${parts[1]}` : parts[0]
              const scope = parts[0]

              // Vue 核心运行时（修复：vue 包本身也归入 vue-runtime）
              if (packageName === 'vue' || scope === '@vue') return 'vue-runtime'
              // Element Plus 全家桶（修复：@element-plus/icons-vue 归入 element-plus）
              if (scope === 'element-plus' || scope === '@element-plus') return 'element-plus'
              // Vant（含 @vant 下的依赖）
              if (packageName === 'vant' || scope === '@vant') return 'vant'
              // ECharts（含底层渲染库 zrender）
              if (packageName === 'echarts' || packageName === 'zrender') return 'echarts'
              if (packageName === 'echarts-wordcloud') return 'echarts-addons'
              // 路由
              if (packageName === 'vue-router') return 'vue-router'
              // 状态管理
              if (packageName === 'pinia' || packageName === 'pinia-plugin-persistedstate')
                return 'pinia'
              // HTTP
              if (packageName === 'axios') return 'axios'
              // 日期
              if (packageName === 'dayjs') return 'dayjs'
              // 加密
              if (packageName === 'crypto-js') return 'crypto'
              // 工具库
              if (packageName === 'lodash-es') return 'lodash'
              // 其余小型第三方库
              return 'vendor'
            }

            // 应用代码仅保留天然异步边界，避免按目录强拆放大循环依赖并触发 TDZ
            if (
              id.includes('/src/components/Business/DrillDownDialog/') ||
              id.includes('/src/components/Business/DrillDownPage/')
            ) {
              return 'drill-down'
            }
          }
        }
      }
    }
  }
})
