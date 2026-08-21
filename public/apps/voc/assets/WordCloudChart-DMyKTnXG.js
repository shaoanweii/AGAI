import{aR as h,aS as m,_ as y}from"./drill-down-C0C1v3aG.js";import{u as z,i as w,m as b,n as v}from"./echarts-CVUDWY3Y.js";import{x as C,r as R,o as _,k,d as E,D as O,I as S}from"./vue-runtime-sUTo5VqX.js";const W=C({__name:"WordCloudChart",props:{data:{}},emits:["word-click"],setup(p,{emit:f}){z([b,v]);const s=p,u=f,r=R(null);let e=null,n=null;const g=t=>t>=80?{sizeRange:[10,24],gridSize:6}:t>=40?{sizeRange:[12,28],gridSize:8}:{sizeRange:[14,32],gridSize:12},l=t=>{if(t==null)return"-";const o=Number(t);return isNaN(o)?"-":`${o>0?"+":""}${o.toFixed(1)}%`},c=()=>{if(!r.value)return;e||(e=w(r.value),e.on("click",i=>{u("word-click",i.data)}));const t=Array.isArray(s.data)?s.data:[],o=g(t.length),x=h(t,{palette:m,highlightTopCount:10,dimOpacity:.8});e.setOption({tooltip:{show:!0,confine:!0,backgroundColor:"white",borderColor:"#E5E6EB",borderWidth:1,padding:12,textStyle:{color:"#333",fontSize:14},formatter:i=>{const a=i.data;return`
          <div style="background: white; border-radius: 4px; padding: 0; font-size: 12px; min-width: 200px;">
            <div class="mb-12 fs-14 fw-500" style="color: #333; margin-bottom: 12px;">
              ${a.name||"-"}
            </div>
            <table style="width: 100%; border-collapse: collapse; margin: 0;">
              <thead>
                <tr style="background: #f0f8ff;">
                  <th style="padding: 8px 12px; text-align: left; color: #26292E; font-weight: 400; font-size: 14px;">名称</th>
                  <th style="padding: 8px 12px; text-align: center; color: #26292E; font-weight: 400; font-size: 14px;">数值</th>
                  <th style="padding: 8px 12px; text-align: center; color: #26292E; font-weight: 400; font-size: 14px;">环比</th>
                  <th style="padding: 8px 12px; text-align: center; color: #26292E; font-weight: 400; font-size: 14px;">同比</th>
                </tr>
              </thead>
              <tbody>
                <tr style="background: white;">
                  <td style="padding: 8px 12px; color: #333; font-size: 14px;">提及量</td>
                  <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px;">${a.value||0}</td>
                  <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px;">${l(a.mentionsMoM)}</td>
                  <td style="padding: 8px 12px; text-align: center; color: #333; font-size: 14px;">${l(a.mentionsYoY)}</td>
                </tr>
              </tbody>
            </table>
          </div>
        `}},series:[{type:"wordCloud",shape:"circle",left:"center",top:"center",width:"96%",height:"62%",keepAspect:!1,sizeRange:o.sizeRange,rotationRange:[0,0],gridSize:o.gridSize,layoutAnimation:!0,drawOutOfBound:!1,textStyle:{fontFamily:"sans-serif",fontWeight:"500",color:function(i){return x(i)}},data:t}]})},d=()=>{e==null||e.resize()};return _(()=>{c(),window.addEventListener("resize",d),r.value&&typeof ResizeObserver<"u"&&(n=new ResizeObserver(d),n.observe(r.value))}),k(()=>{window.removeEventListener("resize",d),n==null||n.disconnect(),e==null||e.dispose(),e=null,n=null}),E(()=>s.data,c,{deep:!0}),(t,o)=>(O(),S("div",{ref_key:"chartRef",ref:r,class:"word-cloud-chart"},null,512))}}),B=y(W,[["__scopeId","data-v-53cae49f"]]);export{B as W};
