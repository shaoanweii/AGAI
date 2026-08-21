import{d as i,c as o,f as d}from"./drill-down-C0C1v3aG.js";function u(t,e){const l=parseInt(t.slice(1,3),16),c=parseInt(t.slice(3,5),16),s=parseInt(t.slice(5,7),16);return`rgba(${l}, ${c}, ${s}, ${e})`}function v(t,e,l=!1){if(!t||t.length===0)return"";if(!Array.isArray(t)){console.log("请设置tooltip.trigger为axis或单独写方法");return}const c=t[0].axisValue;let s="";return t.forEach(r=>{const a=r.data;e==="negativeRate"&&(s+=`
          <tr>    
            <td class="chartTd" >${r.seriesName}</td>
            <td class="chartTd">${i(a.value)} </td>
            <td class="chartTd c666">${d(a.valueMoM||a.negativeRateMoM)}</td>
            <td class="chartTd c666">${d(a.valueYoY||a.negativeRateYoY)}</td>
          </tr>
        `),e.includes("mention")&&(s+=`
        <tr>    
          <td class="chartTd" >${r.seriesName}</td>
          <td class="chartTd">${o(a.value)}</td>
          <td class="chartTd c666">${d(a.valueMoM||a.mentionsMoM)}</td>
          <td class="chartTd c666">${d(a.valueYoY||a.mentionsMoM)}</td>
        </tr>
      `)}),`
        <div class="chartPop">
            <!-- 标题 -->
            <div class="mb-12 fs-14 fw-500 c333" >${c}</div>
            <!-- 表格 -->
            <table style="width: 100%; border-collapse: collapse; margin: 0;">
              <thead>
                <tr class="chartTr">
                  <th class="chartTh">名称</th>
                  <th class="chartTh">${l?"数值":e==="negativeRate"?"负面率":"提及量"}</th>
                  <th class="chartTh">环比</th>
                  <th class="chartTh">同比</th>
                </tr>
              </thead>
              <tbody>
                ${s}
              </tbody>
            </table>
        </div>
      `}function $(t,e,l=!1){if(!t||t.length===0)return"";if(!Array.isArray(t)){console.log("请设置tooltip.trigger为axis或单独写方法");return}const c=t[0].axisValue;let s="";return t.forEach(r=>{const a=r.data,h=r.seriesName.includes("率")?i(a.value):o(a.value);s+=`
        <tr>    
          <td class="chartTd" >${r.seriesName}</td>
          <td class="chartTd">${h} </td>
        </tr>
      `}),`
        <div class="chartPop chartPop_trend">
            <!-- 标题 -->
            <div class="mb-12 fs-14 fw-500 c333" >${c}</div>
            <!-- 表格 -->
            <table style="width: 100%; border-collapse: collapse; margin: 0;">
              <thead>
                <tr class="chartTr">
                  <th class="chartTh">名称</th>
                  <th class="chartTh">${e==="negativeRate"?"负面率":"提及量"}</th>
                </tr>
              </thead>
              <tbody>
                ${s}
              </tbody>
            </table>
        </div>
      `}export{$ as a,v as f,u as h};
