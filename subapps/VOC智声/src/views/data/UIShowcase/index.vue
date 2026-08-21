<template>
  <div class="ui-showcase">
    <div class="page-header">
      <h1>UI组件展示</h1>
      <p>这里展示了项目中所有的自定义UI组件及其使用方法</p>
    </div>

    <!-- ButtonGroup 组件展示 -->
    <div class="component-section">
      <h2>ButtonGroup 按钮组</h2>
      <div class="component-demo">
        <ButtonGroup
          v-model="buttonGroupValue"
          :options="buttonGroupOptions"
          @change="onButtonGroupChange"
        />
      </div>
      <div class="component-props">
        <h3>Props 参数</h3>
        <ul>
          <li><code>options</code>: Option[] - 按钮选项数组，每个选项包含 label 和 value</li>
          <li><code>modelValue</code>: string - 当前选中的值</li>
        </ul>
        <h3>Events 事件</h3>
        <ul>
          <li><code>update:modelValue</code>: (value: string) => void - 值变化时触发</li>
          <li><code>change</code>: (value: string) => void - 选择变化时触发</li>
        </ul>
      </div>
    </div>

    <!-- FAnalyseWrap 组件展示 -->
    <div class="component-section">
      <h2>FAnalyseWrap 分析页面布局</h2>
      <div class="component-demo">
        <el-button @click="showAnalyseWrap = !showAnalyseWrap">
          {{ showAnalyseWrap ? '隐藏' : '显示' }} FAnalyseWrap
        </el-button>
        <FAnalyseWrap v-model="showAnalyseWrap" header-height="80px">
          <template #header>
            <div style="padding: 20px; background: #f0f0f0">这是头部内容区域</div>
          </template>
          <div style="padding: 20px">
            这是主要内容区域，支持滚动
            <el-button @click="showAnalyseWrap = false">关闭</el-button>
          </div>
        </FAnalyseWrap>
      </div>
      <div class="component-props">
        <h3>Props 参数</h3>
        <ul>
          <li><code>v-model</code>: boolean - 控制显示/隐藏</li>
          <li><code>headerHeight</code>: string - 头部高度，默认 '72px'</li>
        </ul>
        <h3>Slots 插槽</h3>
        <ul>
          <li><code>header</code>: 头部内容插槽</li>
          <li><code>default</code>: 主要内容插槽</li>
        </ul>
      </div>
    </div>

    <!-- FCard 组件展示 -->
    <div class="component-section">
      <h2>FCard 卡片组件</h2>
      <div class="component-demo">
        <FCard
          title="示例卡片"
          width="400px"
          height="300px"
          :is-show-more="true"
          tooltip="这是一个提示信息"
        >
          <template #leftExtra>
            <el-tag size="small">标签</el-tag>
          </template>
          <template #more>
            <span style="color: #409eff; cursor: pointer">自定义更多</span>
          </template>
          <div>
            <p>这是卡片的主要内容区域</p>
            <p>可以放置任何内容</p>
          </div>
        </FCard>
      </div>
      <div class="component-props">
        <h3>Props 参数</h3>
        <ul>
          <li><code>title</code>: string - 卡片标题，默认 '标题'</li>
          <li><code>width</code>: string - 卡片宽度，默认 '100%'</li>
          <li><code>height</code>: string - 卡片高度，默认 '100%'</li>
          <li><code>isShowMore</code>: boolean - 是否显示更多按钮，默认 false</li>
          <li><code>tooltip</code>: string - 提示信息</li>
        </ul>
        <h3>Slots 插槽</h3>
        <ul>
          <li><code>title</code>: 自定义标题内容</li>
          <li><code>leftExtra</code>: 标题左侧额外内容</li>
          <li><code>more</code>: 自定义更多按钮内容</li>
          <li><code>default</code>: 卡片主要内容</li>
        </ul>
      </div>
    </div>

    <!-- FSelect 组件展示 -->
    <div class="component-section">
      <h2>FSelect 选择器</h2>
      <div class="component-demo">
        <FSelect v-model="selectValue" :options="selectOptions" />
      </div>
      <div class="component-props">
        <h3>Props 参数</h3>
        <ul>
          <li><code>options</code>: any[] - 选项数组</li>
          <li>
            <code>fields</code>: object - 字段映射，默认 { label: 'itemText', value: 'itemValue' }
          </li>
          <li><code>v-model</code>: any - 选中的值</li>
        </ul>
      </div>
    </div>

    <!-- FTable 组件展示 -->
    <div class="component-section">
      <h2>FTable 表格组件</h2>
      <div class="component-demo">
        <FTable
          :columns="tableColumns"
          :data="tableData"
          :loading="tableLoading"
          height="300"
          @sort-change="onTableSortChange"
        />
      </div>
      <div class="component-props">
        <h3>Props 参数</h3>
        <ul>
          <li><code>columns</code>: TableColumn[] - 列配置数组</li>
          <li><code>data</code>: any[] - 表格数据</li>
          <li><code>loading</code>: boolean - 加载状态</li>
          <li><code>height</code>: string | number - 表格高度</li>
          <li><code>pagination</code>: boolean | object - 分页配置</li>
          <li><code>size</code>: 'large' | 'default' | 'small' - 表格尺寸</li>
        </ul>
        <h3>Events 事件</h3>
        <ul>
          <li><code>sort-change</code>: 排序变化事件</li>
        </ul>
      </div>
    </div>

    <!-- SortNum 组件展示 -->
    <div class="component-section">
      <h2>SortNum 排序数字</h2>
      <div class="component-demo">
        <div style="display: flex; gap: 10px; align-items: center">
          <SortNum :rank="1" />
          <SortNum :rank="2" />
          <SortNum :rank="3" />
          <SortNum :rank="4" />
          <SortNum :rank="5" />
        </div>
      </div>
      <div class="component-props">
        <h3>Props 参数</h3>
        <ul>
          <li><code>rank</code>: number - 排序数字，前3名显示热门图标</li>
        </ul>
      </div>
    </div>

    <!-- SvgIcon 组件展示 -->
    <div class="component-section">
      <h2>SvgIcon 图标组件</h2>
      <div class="component-demo">
        <div style="display: flex; gap: 15px; align-items: center">
          <SvgIcon name="hot_rank" width="24px" height="24px" color="#e5484d" />
          <SvgIcon name="o_rank" width="24px" height="24px" color="#5c7092" />
        </div>
      </div>
      <div class="component-props">
        <h3>Props 参数</h3>
        <ul>
          <li><code>name</code>: string - 图标名称</li>
          <li><code>prefix</code>: string - 图标前缀，默认 '#icon-'</li>
          <li><code>color</code>: string - 图标颜色，默认 '#ffffff'</li>
          <li><code>width</code>: string - 图标宽度，默认 '16px'</li>
          <li><code>height</code>: string - 图标高度，默认 '16px'</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ButtonGroup from '@/components/UI/ButtonGroup/index.vue'
import FAnalyseWrap from '@/components/UI/FAnalyseWrap/index.vue'
import FCard from '@/components/UI/FCard/index.vue'
import FSelect from '@/components/UI/FSelect/index.vue'
import FTable from '@/components/UI/FTable/index.vue'
import SortNum from '@/components/UI/SortNum/index.vue'
import SvgIcon from '@/components/UI/SvgIcon/index.vue'

defineOptions({
  name: 'UIShowcase'
})

// ButtonGroup 数据
const buttonGroupValue = ref('option1')
const buttonGroupOptions = ref([
  { label: '选项一', value: 'option1' },
  { label: '选项二', value: 'option2' },
  { label: '选项三', value: 'option3' }
])

const onButtonGroupChange = (value: string) => {
  console.log('ButtonGroup 选择变化:', value)
}

// FAnalyseWrap 数据
const showAnalyseWrap = ref(false)

// FSelect 数据
const selectValue = ref('')
const selectOptions = ref([
  { itemText: '北京', itemValue: 'beijing' },
  { itemText: '上海', itemValue: 'shanghai' },
  { itemText: '广州', itemValue: 'guangzhou' },
  { itemText: '深圳', itemValue: 'shenzhen' }
])

// FTable 数据
const tableLoading = ref(false)
const tableColumns = ref([
  { title: '姓名', dataIndex: 'name', width: 120 },
  { title: '年龄', dataIndex: 'age', width: 80, sortable: true },
  { title: '城市', dataIndex: 'city', width: 120 },
  { title: '职业', dataIndex: 'job', showOverflowTooltip: { popperClass: 'text-tooltip-light' } }
])

const tableData = ref([
  { name: '张三', age: 25, city: '北京', job: '前端开发工程师' },
  { name: '李四', age: 30, city: '上海', job: '后端开发工程师' },
  { name: '王五', age: 28, city: '广州', job: '产品经理' },
  { name: '赵六', age: 32, city: '深圳', job: 'UI设计师' }
])

const onTableSortChange = (sortInfo: any) => {
  console.log('表格排序变化:', sortInfo)
}
</script>

<style lang="scss" scoped>
.ui-showcase {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .page-header {
    margin-bottom: 40px;
    text-align: center;

    h1 {
      font-size: 32px;
      color: #2c3e50;
      margin-bottom: 10px;
    }

    p {
      font-size: 16px;
      color: #7f8c8d;
      margin: 0;
    }
  }

  .component-section {
    margin-bottom: 50px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    overflow: hidden;

    h2 {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      margin: 0;
      padding: 20px 24px;
      font-size: 20px;
      font-weight: 600;
    }

    .component-demo {
      padding: 30px 24px;
      border-bottom: 1px solid #ebeef5;
      background: #fafbfc;

      > * {
        margin-bottom: 0;
      }
    }

    .component-props {
      padding: 24px;

      h3 {
        color: #409eff;
        font-size: 16px;
        margin: 0 0 15px 0;
        font-weight: 600;
      }

      ul {
        margin: 0 0 20px 0;
        padding-left: 20px;

        li {
          margin-bottom: 8px;
          line-height: 1.6;
          color: #606266;

          code {
            background: #f1f2f6;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
            font-size: 13px;
            color: #e83e8c;
          }
        }
      }

      ul:last-child {
        margin-bottom: 0;
      }
    }
  }
}
</style>
