import { fmtFix, fmtNum, fmtPer } from '@/utils'

const dict: any = {
  提及量: 'mentionCount',
  正面率: 'positiveRate',
  中性率: 'neutralRate',
  负面率: 'negativeRate',
  环比: 'ringRatio',
  同比: 'yearOnYearRatio'
  // '正面提及量': 'positiveMentionCount',
  // '中性提及量': 'neutralMentionCount',
  // '负面提及量': 'negativeMentionCount'
}

const colors = ['#7298D0', '#82E3C7', '#60B8EB', '#FF8A8B', '#9a60b4', '#ea7ccc']
// const colors = [ '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc' ]

const lableList = Object.keys(dict)
const fieldList = Object.values(dict)
const legendData = ['提及量', '正面率', '中性率', '负面率']
// const legendData = lableList
// console.log('legendData', legendData)
// console.log('fieldList', fieldList)

const opts: any = {
  color: colors,
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross',
      crossStyle: {
        color: '#999'
      }
    },
    confine: true,
    formatter: function (params: any) {
      if (!params || params.length === 0) return ''

      // 获取第一个数据点的完整数据
      const data = params[0].data
      if (!data) return ''

      // 构建表格HTML
      let tableHtml = `
        <div style="background: white; border-radius: 4px; padding: 0; font-size: 12px; min-width: 200px;">
          <div class="mb-12 fs-14 fw-500" style="color: #333">
            ${data.tagName}
          </div>
          <table style="width: 100%; border-collapse: collapse; margin: 0;">
            <thead>
              <tr style="background: #f0f8ff; padding: 8px 12px; color: #26292E; border-radius: 4px 4px 0 0;">
                <th style="padding: 8px 12px; text-align: left; color: #26292E;" class="fw-400 fs-14">名称</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">数值</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">环比</th>
                <th style="padding: 8px 12px; text-align: center; color: #26292E;" class="fw-400 fs-14">同比</th>
              </tr>
            </thead>
            <tbody>
      `

      // 定义要显示的数据行
      const dataRows = [
        {
          name: '提及量',
          value: data.mentionCount || 0,
          ringRatio: data.ringRatio,
          yearOnYearRatio: data.yearOnYearRatio
        },
        {
          name: '正面率',
          value: data.positiveRate,
          ringRatio: data.positiveRingRatio,
          yearOnYearRatio: data.positiveYearOnYearRatio
        },
        {
          name: '中性率',
          value: data.neutralRate,
          ringRatio: data.neutralRingRatio,
          yearOnYearRatio: data.neutralYearOnYearRatio
        },
        {
          name: '负面率',
          value: data.negativeRate,
          ringRatio: data.negativeRingRatio,
          yearOnYearRatio: data.negativeYearOnYearRatio
        }
      ]

      // 添加数据行
      dataRows.forEach((row, index) => {
        // const isLast = index === dataRows.length - 1
        const noBorder = true
        tableHtml += `
          <tr style="background: ${index % 2 === 0 ? 'white' : '#fafafa'};">
            <td style="padding: 8px 12px; color: #333; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${row.name}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${row.name === '提及量' ? fmtNum(row.value) : fmtPer(row.value)}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${fmtFix(row.ringRatio)}
            </td>
            <td style="padding: 8px 12px; text-align: center; color: #666; font-size: 14px; border-bottom: ${noBorder ? 'none' : '1px solid #e9ecef'};">
              ${fmtFix(row.yearOnYearRatio)}
            </td>
          </tr>
        `
      })

      tableHtml += `
            </tbody>
          </table>
        </div>
      `

      return tableHtml
    }
  },
  // color: ['#4285F4', '#FBBC05', '#34A853', '#EA4335'],

  // seriesName === '提及量' ? item.value[dict[seriesName]] : fmtRatio(item.value[dict[seriesName]]) + '<br/>';

  grid: {
    left: 24,
    right: 24,
    top: 40,
    bottom: 50,
    containLabel: true
  },
  legend: {
    bottom: 15,
    left: 'center',
    data: legendData
    // data: ['负面率', '提及量']
  },
  xAxis: [
    {
      type: 'category',
      axisPointer: {
        type: 'shadow'
      },
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        interval: 0,
        rotate: 45,
        color: '#666',
        fontSize: 12
      }
    }
  ],
  yAxis: [
    {
      // '百分比',
      type: 'value',
      min: 0,
      max: 100,
      interval: 25,
      splitLine: {
        lineStyle: {
          color: '#DDE3EE',
          type: 'dashed'
        }
      },
      axisLabel: {
        color: '#999',
        formatter: '{value} %'
      }
    },

    {
      //'数字',
      type: 'value',
      min: 0,
      // max: 250,
      // interval: 1000,
      splitLine: {
        show: false
      },

      axisLabel: {
        color: '#999'
      }
    }
  ],
  series: []
}

lableList.forEach((name, index) => {
  const s: any = {
    name,
    type: 'bar',
    stack: 'value',
    barWidth: 24
  }
  if (name === '提及量') {
    s.type = 'line'
    s.yAxisIndex = 1 // 两个y轴，y0 是百分比(左侧) ，
    s.symbol = 'none'
    s.stack = 'none'
  }
  // 隐藏部分系列
  else if (['环比', '同比'].includes(name)) {
    s.itemStyle = {
      color: 'rgba(0,0,0,0)'
    }
  }
  opts.series.push(s)
})

// console.log('opts', opts)

export const chartOpts = opts
export const fields = fieldList
