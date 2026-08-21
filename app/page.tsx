"use client";

import { useEffect, useRef, useState, type CSSProperties } from "react";

const productApps = [
  ["智能问数", "自然语言提问，快速获取业务数据与答案"],
  ["VOC智声", "聚合全域声音，识别情绪、诉求与风险"],
  ["KOC智营", "识别关键影响者，驱动口碑传播与内容增长"],
  ["线索运营", "识别高潜线索，推进转化过程与结果追踪"],
  ["消费者智调", "快速洞察用户需求，验证产品与市场判断"],
] as const;

const capabilityMetrics = [
  { value: 8, suffix: "年", label: "汽车行业深耕" },
  { value: 15, suffix: "+", label: "汽车品牌实践" },
  { value: 3000, suffix: "+", label: "产品标签" },
  { value: 100, suffix: "万+", label: "行业观点库" },
  { value: 6000, suffix: "+", label: "品牌车系图谱" },
] as const;

function goTo(id: string) {
  document.getElementById(id)?.scrollIntoView({ behavior: "smooth", block: "start" });
}

function Header({ onWorkbench }: { onWorkbench: () => void }) {
  return (
    <header className="site-header" aria-label="主导航">
      <button className="brand" type="button" onClick={() => goTo("hero")} aria-label="返回首页">AGAI</button>
      <nav>
        <button type="button" onClick={() => goTo("products")}>产品生态</button>
        <button type="button" onClick={() => goTo("solutions")}>解决方案</button>
        <button type="button" onClick={() => goTo("capability")}>行业能力</button>
        <button type="button" onClick={() => goTo("customers")}>客户实践</button>
        <button type="button" onClick={() => goTo("about")}>关于 AGAI</button>
      </nav>
      <button className="workbench" type="button" onClick={onWorkbench}>进入工作台</button>
    </header>
  );
}

function BoardCopy({
  kicker,
  title,
  accent,
  summary,
  className = "",
}: {
  kicker?: string;
  title: string;
  accent: string;
  summary: string;
  className?: string;
}) {
  return (
    <div className={`board-copy ${className}`}>
      {kicker && <p className="board-kicker">{kicker}</p>}
      <h2>{title}<em>{accent}</em></h2>
      <p className="board-summary">{summary}</p>
    </div>
  );
}

function CapabilitySection() {
  const ref = useRef<HTMLElement>(null);
  const [visible, setVisible] = useState(false);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const node = ref.current;
    if (!node) return;
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        setVisible(true);
        observer.disconnect();
      }
    }, { threshold: 0.32 });
    observer.observe(node);
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!visible) return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      setProgress(1);
      return;
    }
    const start = performance.now();
    let frame = 0;
    const tick = (now: number) => {
      const raw = Math.min((now - start) / 1100, 1);
      setProgress(1 - Math.pow(1 - raw, 3));
      if (raw < 1) frame = requestAnimationFrame(tick);
    };
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [visible]);

  return (
    <section className="layered-board capability-board" id="capability" ref={ref} aria-label="汽车行业数智能力">
      <img className="board-background" src="/boards/v4/02-capability-background.png" width="1672" height="941" alt="全域数据接入、行业语义理解与业务闭环的三维数智链路" loading="lazy" />
      <BoardCopy
        kicker="汽车行业数智能力"
        title="从全域声音，到可执行的"
        accent="增长信号"
        summary="贯通公域与私域数据，以汽车行业知识增强语义理解，让每一条客户之声进入业务决策。"
        className="capability-copy"
      />
      <div className="capability-pillars">
        <div><strong>会听<i />全域数据统一接入</strong><span>多源采集 · 多模态转译</span></div>
        <div><strong>能懂<i />行业语义深度理解</strong><span>标签识别 · 情感意图 · 风险分级</span></div>
        <div><strong>会行动<i />洞察直达业务闭环</strong><span>策略建议 · 任务协同 · 结果追踪</span></div>
      </div>
      <div className={`metrics-panel ${visible ? "is-visible" : ""}`}>
        {capabilityMetrics.map((metric, index) => (
          <div className="metric" key={metric.label} style={{ "--delay": `${index * 90}ms` } as CSSProperties}>
            <span className={`metric-icon metric-icon--${index + 1}`} aria-hidden="true" />
            <span className="metric-copy">
              <strong>{Math.round(metric.value * progress).toLocaleString("zh-CN")}<em>{metric.suffix}</em></strong>
              <small>{metric.label}</small>
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}

function ProductSection() {
  return (
    <section className="layered-board product-board" id="products" aria-label="产品生态">
      <img className="board-background" src="/boards/v5/03-product-ecosystem-background.png" width="1672" height="941" alt="五大业务应用、运营中台与双洞察引擎组成的产品生态" loading="lazy" />
      <BoardCopy kicker="AGAI 产品生态" title="一套产品生态，让洞察持续" accent="进入业务" summary="双引擎沉淀理解力，运营中台编排业务流，五大应用将洞察转化为可执行的汽车增长行动。" className="product-copy" />
      <div className="product-apps">
        {productApps.map(([name, description]) => <div key={name}><strong>{name}</strong><span>{description}</span></div>)}
      </div>
      <div className="engine-label engine-label--left"><strong>用户洞察引擎</strong><span>识别人群、关系与机会</span></div>
      <div className="engine-label engine-label--right"><strong>声音洞察引擎</strong><span>理解声音、情绪与意图</span></div>
    </section>
  );
}

function SolutionsSection() {
  return (
    <section className="layered-board solutions-board" id="solutions" aria-label="汽车行业解决方案">
      <img className="board-background" src="/boards/v4/04-automotive-scenarios-v7.png" width="1672" height="941" alt="贯穿 5A 用户旅程的汽车行业解决方案链路" loading="lazy" />
      <BoardCopy kicker="AGAI 汽车行业解决方案" title="贯穿 5A 用户旅程，让每一次洞察产生" accent="业务结果" summary="从品牌认知到用户拥护，AGAI 将声音、用户、内容与线索连接成可持续的汽车增长飞轮。" className="solutions-copy" />
    </section>
  );
}

function CustomerSection() {
  return (
    <section className="layered-board customer-board" id="customers" aria-label="客户实践">
      <img className="board-background" src="/boards/v5/05-customer-practice-background.png" width="1672" height="941" alt="十五家汽车合作品牌与持续运营实践" loading="lazy" />
      <BoardCopy kicker="客户实践" title="与汽车品牌共同，把洞察变成" accent="增长" summary="8年深耕汽车行业，服务15+汽车品牌，以产品、数据与持续运营能力支撑业务长期演进。" className="customer-copy" />
    </section>
  );
}

function ClosingSection() {
  return (
    <section className="layered-board closing-board" id="about" aria-label="关于 AGAI">
      <div className="closing-scene" aria-hidden="true">
        <span className="closing-orbit closing-orbit--1" />
        <span className="closing-orbit closing-orbit--2" />
        <span className="closing-orbit closing-orbit--3" />
        <span className="closing-orbit closing-orbit--4" />
        <span className="closing-core" />
        <span className="closing-node closing-node--1" />
        <span className="closing-node closing-node--2" />
        <span className="closing-node closing-node--3" />
        <span className="closing-node closing-node--4" />
      </div>
      <BoardCopy title="让客户之声，直达" accent="决策与增长" summary="用 AGAI 建立面向品牌、线索、服务与产品的全域数智能力。" className="closing-copy" />
      <div className="closing-actions">
        <button type="button" onClick={() => window.location.assign("/login")}>进入工作台</button>
        <button type="button" onClick={() => goTo("site-footer")}>联系我们</button>
      </div>
      <footer className="site-footer" id="site-footer">
        <div className="site-footer-top">
          <div className="footer-brand-line"><strong>AGAI</strong><span>让汽车增长，因 AI 而发生</span></div>
          <nav aria-label="页脚导航">
            <button type="button" onClick={() => goTo("products")}>产品生态</button>
            <button type="button" onClick={() => goTo("solutions")}>解决方案</button>
            <button type="button" onClick={() => goTo("customers")}>客户实践</button>
            <button type="button" onClick={() => goTo("about")}>关于 AGAI</button>
          </nav>
        </div>
        <div className="site-footer-bottom">
          <span>北京富通东方科技有限公司</span>
          <span>© 2026 AGAI. All rights reserved.</span>
          <div className="footer-legal"><span>隐私政策</span><i aria-hidden="true" /><span>服务条款</span></div>
        </div>
      </footer>
    </section>
  );
}

export default function Home() {
  return (
    <main>
      <section className="hero-board" id="hero">
        <Header onWorkbench={() => window.location.assign("/login")} />
        <div className="hero-canvas">
          <img className="hero-background" src="/boards/v3/01-hero-background.png" width="1761" height="893" alt="汽车全域数智解决方案的 3D 产品生态场景" fetchPriority="high" />
          <div className="hero-copy">
            <p className="hero-kicker"><strong>AutoGrowth AI</strong><span aria-hidden="true">·</span>汽车全域数智解决方案</p>
            <h1>让汽车增长，<br />因 <em>AI</em> 而发生</h1>
            <p className="hero-summary">面向全球汽车行业，以 AI 驱动数据洞察与智能运营，<br />让客户之声从被听见、被理解，到进入决策与业务增长。</p>
            <div className="hero-actions"><button type="button" onClick={() => goTo("products")}>探索产品生态</button><button type="button" onClick={() => goTo("solutions")}>了解解决方案</button></div>
          </div>
          <img className="hero-values" src="/illustrations/hero-value-strip.png" width="2105" height="450" alt="听见全域声音，理解真实需求，驱动业务增长" />
        </div>
      </section>
      <CapabilitySection />
      <ProductSection />
      <SolutionsSection />
      <CustomerSection />
      <ClosingSection />
    </main>
  );
}
