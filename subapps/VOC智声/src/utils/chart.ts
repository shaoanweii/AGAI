import { fmtFix, fmtNum, fmtPer } from './index'
/**
 * 图表工具函数
 * 只保留项目中实际使用的函数
 */

/**
 * 十六进制颜色转换为 rgba 格式
 * @param hex 十六进制颜色值 (如: #1677FF)
 * @param alpha 透明度 (0-1)
 * @returns rgba 格式的颜色字符串
 */
export function hexToRgba(hex: string, alpha: number): string {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}



/*  负面率/提及量 二选一

    isVal ：可选，传false时 表头为—— 名称(如品牌名)   + 负面率/提及量 + 环比 + 同比
                  传ture 时 表头为—— 名称(负面率/提及量) +  数值      + 环比 + 同比
*/
export function formatChartPop(params: any, dataType: string, isVal:boolean = false): any {

  // 若 tooltip.trigger= 'axis'  则params为数组，且length为 指标(或系列serie)的数量； 否则params为对象
  if (!params || params.length === 0) return ''

  // 对象另外写方法
  if (!Array.isArray(params))   {
    console.log('请设置tooltip.trigger为axis或单独写方法')
    return
  } 

  // 标题取第0项即可，各项一致，如2025-07
  const title = params[0].axisValue

  let trStr = ''

  params.forEach((param: any) => {

    const data = param.data

    // console.log('data', data)

    // 负面率
    if (dataType === 'negativeRate') {
      trStr += `
          <tr>    
            <td class="chartTd" >${param.seriesName}</td>
            <td class="chartTd">${fmtPer(data.value)} </td>
            <td class="chartTd c666">${fmtFix(data.valueMoM || data.negativeRateMoM)}</td>
            <td class="chartTd c666">${fmtFix(data.valueYoY || data.negativeRateYoY)}</td>
          </tr>
        `
    }

    // 提及量
    if (dataType.includes('mention')) {
      trStr += `
        <tr>    
          <td class="chartTd" >${param.seriesName}</td>
          <td class="chartTd">${fmtNum(data.value)}</td>
          <td class="chartTd c666">${fmtFix(data.valueMoM || data.mentionsMoM)}</td>
          <td class="chartTd c666">${fmtFix(data.valueYoY || data.mentionsMoM)}</td>
        </tr>
      `
    }


  })

  return `
        <div class="chartPop">
            <!-- 标题 -->
            <div class="mb-12 fs-14 fw-500 c333" >${title}</div>
            <!-- 表格 -->
            <table style="width: 100%; border-collapse: collapse; margin: 0;">
              <thead>
                <tr class="chartTr">
                  <th class="chartTh">名称</th>
                  <th class="chartTh">${ isVal ? '数值' :(dataType === 'negativeRate' ? '负面率' : '提及量')}</th>
                  <th class="chartTh">环比</th>
                  <th class="chartTh">同比</th>
                </tr>
              </thead>
              <tbody>
                ${trStr}
              </tbody>
            </table>
        </div>
      `
}

// 同时 含负面率+提及量用此
export function formatChartPop2(params: any): any {

   // 若 tooltip.trigger= 'axis'  则params为数组， 其length为 系列(指标)的数量； 否则params为对象 。  
   if (!params || params.length === 0) return ''

   // 对象另外写方法
   if (!Array.isArray(params))   {
     console.log('请设置tooltip.trigger为axis或单独写方法')
     return
   } 

  // 标题取第0项即可，各项一致，如2025-07
  const title = params[0].axisValue
  let data = params[0].data

  // console.log('data',data)

  // 仅一个系列（指标）时，不用遍历   
  // params.forEach

  return `
        <div class="chartPop">
            <!-- 标题 -->
            <div class="mb-12 fs-14 fw-500 c333" >${title}</div>
            <!-- 表格 -->
            <table style="width: 100%; border-collapse: collapse; margin: 0;">
              <thead>
                <tr class="chartTr">
                  <th class="chartTh">名称</th>
                  <th class="chartTh">数值</th>
                  <th class="chartTh">环比</th>
                  <th class="chartTh">同比</th>
                </tr>
              </thead>
              <tbody>
                <tr>    
                  <td class="chartTd" >负面率</td>
                  <td class="chartTd">${fmtPer(data.negativeRate)} </td>
                  <td class="chartTd c666">${fmtFix( data.negativeRateMoM)}</td>
                  <td class="chartTd c666">${fmtFix( data.negativeRateYoY)}</td>
                </tr>
                <tr>    
                  <td class="chartTd" >提及量</td>
                  <td class="chartTd">${fmtNum(data.mentions)}</td>
                  <td class="chartTd c666">${fmtFix( data.mentionsMoM)}</td>
                  <td class="chartTd c666">${fmtFix( data.mentionsMoM)}</td>
                </tr>
              </tbody>
            </table>
        </div>
      `
}

// 用于趋势图：  无同比环比  ，表头为 —— 名称(品牌) + 负面率/提及量 
export function formatChartPop_trend(params: any, dataType: string, isVal:boolean = false): any {

  // 若 tooltip.trigger= 'axis'  则params为数组，且length为 指标(或系列serie)的数量； 否则params为对象
  if (!params || params.length === 0) return ''

  // 对象另外写方法
  if (!Array.isArray(params))   {
    console.log('请设置tooltip.trigger为axis或单独写方法')
    return
  } 

  // 标题取第0项即可，各项一致，如2025-07
  const title = params[0].axisValue

  let trStr = ''

  params.forEach((param: any) => {

    const data = param.data
    // 根据系列名称判断是否需要添加%号
    // 提及量不需要%号，率需要%号
    const isRate = param.seriesName.includes('率')
    const displayValue = isRate ? fmtPer(data.value) : fmtNum(data.value)

    trStr += `
        <tr>    
          <td class="chartTd" >${param.seriesName}</td>
          <td class="chartTd">${displayValue} </td>
        </tr>
      `
  })

  return `
        <div class="chartPop chartPop_trend">
            <!-- 标题 -->
            <div class="mb-12 fs-14 fw-500 c333" >${title}</div>
            <!-- 表格 -->
            <table style="width: 100%; border-collapse: collapse; margin: 0;">
              <thead>
                <tr class="chartTr">
                  <th class="chartTh">名称</th>
                  <th class="chartTh">${ dataType === 'negativeRate' ? '负面率' : '提及量' }</th>
                </tr>
              </thead>
              <tbody>
                ${trStr}
              </tbody>
            </table>
        </div>
      `
}
