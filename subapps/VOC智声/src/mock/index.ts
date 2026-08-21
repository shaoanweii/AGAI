import type { VocDataItem, CustomerTagExperienceVo } from '@/api/common/index.d'
import type { BrandListResponse } from '@/views/H5/api/brand/types'

export const chartDataMock: VocDataItem[] = [
  {
    date: '2025-06-03',
    experienceValue: 80,
    positiveMentions: 900,
    neutralMentions: 700,
    negativeMentions: 400,
    totalMentions: 2000,
    experienceValueMoM: 2.3
  },
  {
    date: '2025-06-04',
    experienceValue: 65,
    positiveMentions: 800,
    neutralMentions: 600,
    negativeMentions: 350,
    totalMentions: 1750,
    experienceValueMoM: -1.2
  },
  {
    date: '2025-06-05',
    experienceValue: 90,
    positiveMentions: 1000,
    neutralMentions: 800,
    negativeMentions: 500,
    totalMentions: 2300,
    experienceValueMoM: 3.8
  },
  {
    date: '2025-06-06',
    experienceValue: 75,
    positiveMentions: 850,
    neutralMentions: 650,
    negativeMentions: 300,
    totalMentions: 1800,
    experienceValueMoM: -1.7
  },
  {
    date: '2025-06-07',
    experienceValue: 70,
    positiveMentions: 700,
    neutralMentions: 600,
    negativeMentions: 350,
    totalMentions: 1650,
    experienceValueMoM: -0.7
  },
  {
    date: '2025-06-08',
    experienceValue: 85,
    positiveMentions: 950,
    neutralMentions: 700,
    negativeMentions: 400,
    totalMentions: 2050,
    experienceValueMoM: 2.1
  },
  {
    date: '2025-06-09',
    experienceValue: 60,
    positiveMentions: 800,
    neutralMentions: 600,
    negativeMentions: 300,
    totalMentions: 1700,
    experienceValueMoM: -2.9
  },
  {
    date: '2025-06-10',
    experienceValue: 88,
    positiveMentions: 1000,
    neutralMentions: 800,
    negativeMentions: 500,
    totalMentions: 2300,
    experienceValueMoM: 4.7
  }
]

export const basicChartDataMock: CustomerTagExperienceVo[] = [
  {
    customerTagName: '全旅程',
    experienceValue: 75.6,
    totalMentions: 8543,
    positiveMentions: 4512,
    neutralMentions: 2845,
    negativeMentions: 1186
  },
  {
    customerTagName: '外观',
    experienceValue: 82.5,
    totalMentions: 2156,
    positiveMentions: 1456,
    neutralMentions: 543,
    negativeMentions: 157
  },
  {
    customerTagName: '内饰',
    experienceValue: 76.8,
    totalMentions: 1876,
    positiveMentions: 987,
    neutralMentions: 654,
    negativeMentions: 235
  },
  {
    customerTagName: '空间',
    experienceValue: 85.2,
    totalMentions: 2345,
    positiveMentions: 1654,
    neutralMentions: 543,
    negativeMentions: 148
  },
  {
    customerTagName: '配置',
    experienceValue: 79.3,
    totalMentions: 1654,
    positiveMentions: 876,
    neutralMentions: 543,
    negativeMentions: 235
  },
  {
    customerTagName: '动力',
    experienceValue: 73.6,
    totalMentions: 1987,
    positiveMentions: 987,
    neutralMentions: 654,
    negativeMentions: 346
  },
  {
    customerTagName: '操控',
    experienceValue: 77.9,
    totalMentions: 1432,
    positiveMentions: 765,
    neutralMentions: 432,
    negativeMentions: 235
  },
  {
    customerTagName: '舒适性',
    experienceValue: 74.2,
    totalMentions: 1234,
    positiveMentions: 654,
    neutralMentions: 432,
    negativeMentions: 148
  },
  {
    customerTagName: '油耗',
    experienceValue: 68.5,
    totalMentions: 1876,
    positiveMentions: 765,
    neutralMentions: 654,
    negativeMentions: 457
  },
  {
    customerTagName: '质量',
    experienceValue: 71.8,
    totalMentions: 2345,
    positiveMentions: 1234,
    neutralMentions: 765,
    negativeMentions: 346
  },
  {
    customerTagName: '销售服务',
    experienceValue: 81.4,
    totalMentions: 1654,
    positiveMentions: 987,
    neutralMentions: 543,
    negativeMentions: 124
  },
  {
    customerTagName: '售后服务',
    experienceValue: 77.8,
    totalMentions: 1432,
    positiveMentions: 765,
    neutralMentions: 432,
    negativeMentions: 235
  }
]

export const topQuestionDataMock: any[] = [
  {
    keyword: '发动机异响',
    sentiment: 'negative',
    mentions: 1234,
    mentionsChange: 192,
    mentionsMoM: 15.6,
    mentionRate: 9.8
  },
  {
    keyword: '变速箱顿挫',
    sentiment: 'negative',
    mentions: 987,
    mentionsChange: -830,
    mentionsMoM: -8.3,
    mentionRate: 7.9
  },
  {
    keyword: '车机卡顿',
    sentiment: 'negative',
    mentions: 765,
    mentionsChange: 97,
    mentionsMoM: 12.7,
    mentionRate: 6.1
  }
]

/**
 * 人群特征mock数据，符合PopulationCharacteristicsResponse类型
 */
export const populationDataMock: any = {
  totalUsers: 10000,
  genderDistributions: [
    { gender: '男', proportion: 0.6 },
    { gender: '女', proportion: 0.4 }
  ],
  ageDistributions: [
    { ageRange: '26-35', proportion: 0.5 },
    { ageRange: '18-25', proportion: 0.2 },
    { ageRange: '36-45', proportion: 0.2 },
    { ageRange: '46及以上', proportion: 0.1 }
  ],
  customerTypeDistributions: [
    { customerType: '老客户', proportion: 0.7 },
    { customerType: '新客户', proportion: 0.3 }
  ],
  carAgeDistributions: [
    { carAge: '1年以内', proportion: 0.4 },
    { carAge: '1-3年', proportion: 0.4 },
    { carAge: '3年以上', proportion: 0.2 }
  ],
  provinceDistributions: [
    { provinceName: '其他', proportion: 0.5 },
    { provinceName: '广东', proportion: 0.1 },
    { provinceName: '北京', proportion: 0.1 },
    { provinceName: '上海', proportion: 0.1 },
    { provinceName: '四川', proportion: 0.1 }
  ],
  educationDistributions: [
    { education: '本科', proportion: 0.5 },
    { education: '硕士', proportion: 0.2 },
    { education: '大专及以下', proportion: 0.2 },
    { education: '博士', proportion: 0.1 }
  ]
}

// 数据源分析Mock数据
export const dataSourceListMock = [
  {
    dataSource: '汽车之家',
    totalMentionValue: 1100,
    positiveMentionValue: 500,
    neutralMentionValue: 400,
    negativeMentionValue: 200
  },
  {
    dataSource: '懂车帝',
    totalMentionValue: 800,
    positiveMentionValue: 350,
    neutralMentionValue: 300,
    negativeMentionValue: 150
  },
  {
    dataSource: '易车网',
    totalMentionValue: 600,
    positiveMentionValue: 250,
    neutralMentionValue: 200,
    negativeMentionValue: 150
  },
  {
    dataSource: '微博',
    totalMentionValue: 500,
    positiveMentionValue: 200,
    neutralMentionValue: 180,
    negativeMentionValue: 120
  },
  {
    dataSource: '小红书',
    totalMentionValue: 400,
    positiveMentionValue: 150,
    neutralMentionValue: 150,
    negativeMentionValue: 100
  }
]

export const dataSourceRemarkMock = {
  trend: [
    {
      keyWord: '2025-06-27',
      positiveMentionValue: 120,
      neutralMentionValue: 80,
      negativeMentionValue: 60
    },
    {
      keyWord: '2025-06-28',
      positiveMentionValue: 130,
      neutralMentionValue: 90,
      negativeMentionValue: 70
    },
    {
      keyWord: '2025-06-29',
      positiveMentionValue: 140,
      neutralMentionValue: 100,
      negativeMentionValue: 80
    },
    {
      keyWord: '2025-06-30',
      positiveMentionValue: 150,
      neutralMentionValue: 110,
      negativeMentionValue: 90
    },
    {
      keyWord: '2025-07-01',
      positiveMentionValue: 160,
      neutralMentionValue: 120,
      negativeMentionValue: 100
    },
    {
      keyWord: '2025-07-02',
      positiveMentionValue: 170,
      neutralMentionValue: 130,
      negativeMentionValue: 110
    },
    {
      keyWord: '2025-07-03',
      positiveMentionValue: 180,
      neutralMentionValue: 140,
      negativeMentionValue: 120
    }
  ],
  wordCloud: [
    { name: '空间大', value: 500 },
    { name: '油耗高', value: 400 },
    { name: '噪音大', value: 350 },
    { name: '外观漂亮', value: 300 },
    { name: '动力不足', value: 250 },
    { name: '性价比高', value: 200 },
    { name: '内饰精致', value: 180 },
    { name: '操控好', value: 160 },
    { name: '配置丰富', value: 140 },
    { name: '售后服务好', value: 120 }
  ]
}

/**
 * 地域分析Mock数据，符合ProvinceAnalysis接口
 */
export const regionDataMock: any[] = [
  {
    provinceName: '广东',
    experienceValue: 78.5,
    experienceValueMoM: 2.3,
    mentions: 15432,
    mentionsMoM: 1.5,
    hotWordName: '空间大'
  },
  {
    provinceName: '北京',
    experienceValue: 76.2,
    experienceValueMoM: 1.8,
    mentions: 12345,
    mentionsMoM: 0.8,
    hotWordName: '外观漂亮'
  },
  {
    provinceName: '上海',
    experienceValue: 79.1,
    experienceValueMoM: 3.2,
    mentions: 11876,
    mentionsMoM: 2.1,
    hotWordName: '配置丰富'
  },
  {
    provinceName: '江苏',
    experienceValue: 75.8,
    experienceValueMoM: 1.5,
    mentions: 9876,
    mentionsMoM: 0.9,
    hotWordName: '性价比高'
  },
  {
    provinceName: '浙江',
    experienceValue: 77.3,
    experienceValueMoM: 2.1,
    mentions: 8765,
    mentionsMoM: 1.2,
    hotWordName: '操控好'
  },
  {
    provinceName: '四川',
    experienceValue: 74.6,
    experienceValueMoM: 0.8,
    mentions: 7654,
    mentionsMoM: 0.5,
    hotWordName: '内饰精致'
  },
  {
    provinceName: '湖北',
    experienceValue: 76.9,
    experienceValueMoM: 1.9,
    mentions: 6543,
    mentionsMoM: 1.1,
    hotWordName: '舒适性好'
  },
  {
    provinceName: '山东',
    experienceValue: 75.2,
    experienceValueMoM: 1.2,
    mentions: 5432,
    mentionsMoM: 0.7,
    hotWordName: '动力强'
  },
  {
    provinceName: '河南',
    experienceValue: 73.8,
    experienceValueMoM: 0.6,
    mentions: 4321,
    mentionsMoM: 0.3,
    hotWordName: '油耗低'
  },
  {
    provinceName: '福建',
    experienceValue: 77.1,
    experienceValueMoM: 2.0,
    mentions: 3987,
    mentionsMoM: 1.3,
    hotWordName: '质量好'
  }
]

// 车系分析相关 Mock 数据
export interface CarSeriesItem {
  name: string
  logo: string
  negativeRate: number
  mentions: number
}

export interface RateDataItem {
  name: string
  percent: number
  highlight?: boolean
}

export interface CarSeriesTableRow {
  brand: string
  brandLogo?: string
  carSeries: string
  mentions: number
  proportion: number
  mentionsMoM: number
  mentionsTrendData: number[]
  negativeRate: number
  negativeMoM: number
  negativeTrendData: number[]
  positiveRate: number
  positiveMoM: number
  positiveTrendData: number[]
}

// 车系基础数据 Mock
export const carSeriesItemsMock: CarSeriesItem[] = [
  {
    name: '深蓝汽车',
    logo: '/src/assets/images/drilldown/car-logo1.png',
    negativeRate: 0.25,
    mentions: 8234
  },
  {
    name: '智行',
    logo: '/src/assets/images/drilldown/car-logo2.png',
    negativeRate: 0.15,
    mentions: 7456
  },
  {
    name: '远途',
    logo: '/src/assets/images/drilldown/car-logo3.png',
    negativeRate: 0.28,
    mentions: 6892
  },
  {
    name: '凌峰',
    logo: '/src/assets/images/drilldown/car-logo4.png',
    negativeRate: 0.18,
    mentions: 5674
  },
  {
    name: '阿维塔',
    logo: '/src/assets/images/drilldown/car-logo5.png',
    negativeRate: 0.22,
    mentions: 4321
  }
]

// 负面率排名数据 Mock
export const rateDataMock: RateDataItem[] = [
  { name: '深蓝S09', percent: 92.35, highlight: true },
  { name: '阿维塔011', percent: 88.35, highlight: true },
  { name: '启源Q07', percent: 71.65 },
  { name: '引力E05', percent: 68.22 },
  { name: '凯程F70', percent: 65.78 },
  { name: '深蓝S7', percent: 62.45 },
  { name: '阿维塔12', percent: 59.88 }
]

// 生成趋势数据的辅助函数
const generateTrendData = (
  length: number = 7,
  baseValue: number = 50,
  volatility: number = 20
): number[] => {
  const data: number[] = []
  let currentValue = baseValue + (Math.random() - 0.5) * volatility

  for (let i = 0; i < length; i++) {
    const change = (Math.random() - 0.5) * volatility * 0.3
    currentValue = Math.max(0, Math.min(100, currentValue + change))
    data.push(Math.round(currentValue * 100) / 100)
  }

  return data
}

// 车系表格数据 Mock
export const carSeriesTableDataMock: CarSeriesTableRow[] = carSeriesItemsMock.map((item, index) => {
  const mentions = 8000 + Math.floor(Math.random() * 2000)
  const proportion = 15 + Math.floor(Math.random() * 5)
  const mentionsMoM = (Math.random() - 0.5) * 10
  const negativeRate = 50 + Math.floor(Math.random() * 30)
  const negativeMoM = (Math.random() - 0.5) * 10
  const positiveRate = 10 + Math.floor(Math.random() * 10)
  const positiveMoM = (Math.random() - 0.5) * 10

  return {
    brand: item.name,
    carSeries: `${item.name}${String.fromCharCode(65 + index)}${String(index + 1).padStart(2, '0')}`,
    mentions,
    proportion: proportion / 100,
    mentionsMoM: mentionsMoM / 100,
    mentionsTrendData: generateTrendData(7, 70, 25),
    negativeRate: negativeRate / 100,
    negativeMoM: negativeMoM / 100,
    negativeTrendData: generateTrendData(7, 30, 20),
    positiveRate: positiveRate / 100,
    positiveMoM: positiveMoM / 100,
    positiveTrendData: generateTrendData(7, 55, 18)
  }
})

// 数据来源数据 Mock
export const dataSourceMock: any[] = [
  { name: '抖音', value: 40.45, color: '#398BFF' },
  { name: '小红书', value: 20.16, color: '#48CFCF' },
  { name: '快手', value: 17.8, color: '#FBBC2C' },
  { name: '新浪微博', value: 13.2, color: '#87A7D7' },
  { name: '汽车之家', value: 8.39, color: '#7D8AFF' }
]

/**
 * 根据数据源生成不同的mock词云数据
 */
export function generateMockWordCloudData(dataSource?: string) {
  const baseWords = [
    { name: '空间大', value: 0 },
    { name: '油耗高', value: 0 },
    { name: '噪音大', value: 0 },
    { name: '外观漂亮', value: 0 },
    { name: '动力不足', value: 0 },
    { name: '性价比高', value: 0 },
    { name: '内饰精致', value: 0 },
    { name: '操控好', value: 0 },
    { name: '配置丰富', value: 0 },
    { name: '售后服务好', value: 0 }
  ]

  // 根据数据源生成不同的词云权重
  const baseValues = [500, 400, 350, 300, 250, 200, 180, 160, 140, 120]

  return baseWords.map((item, index) => ({
    ...item,
    value: Math.floor(baseValues[index] * 1.2)
  }))
}

// 舆情时间线 Mock 集合（按日期渲染）
export const timelineItemsMock = [
  {
    id: 101,
    date: '2025年8月18日',
    platform: '汽车之家',
    title: 'UNI-K配置丰富但油耗高',
    content: '智行S7整车做工上乘，配置也丰富，驾驶辅助功能基本都有，但用了几个月后能耗略高。',
    time: '09:12',
    userName: '赵*来',
    role: '汽车之家',
    tags: [{ text: '外观大气' }, { text: '配置高' }, { text: '油耗高' }],
    badgeText: '疑似水军'
  },
  {
    id: 102,
    date: '2025年8月18日',
    platform: '微博',
    title: 'UNI-K刹车手感偏软',
    content: '城市通勤刹车脚感偏软，需要适应。',
    time: '14:05',
    userName: '张*',
    role: '新浪微博',
    tags: [{ text: '刹车偏软' }],
    badgeText: '高价值内容'
  },
  {
    id: 103,
    date: '2025年8月16日',
    platform: '懂车帝',
    title: 'UNI-K车机系统体验一般',
    content: '语音识别偶尔不灵敏，导航易发热卡顿，希望优化。',
    time: '11:20',
    userName: '王*利',
    role: '懂车帝',
    tags: [{ text: '车机' }, { text: '语音' }]
  },
  {
    id: 104,
    date: '2025年7月12日',
    platform: '小红书',
    title: 'UNI-K外观高级，气场强',
    content: '线条硬朗，灯组精致，停在路边很有辨识度。',
    time: '18:33',
    userName: '朱*峰',
    role: '小红书',
    tags: [{ text: '灯组设计' }, { text: '侧围线条' }, { text: '尾部设计' }]
  }
]
