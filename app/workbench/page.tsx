"use client";

import { useEffect, useRef, useState } from "react";
import styles from "./workbench.module.css";

type Product = {
  id: string;
  name: string;
  image: string;
  area: string;
  kind: "application" | "platform" | "engine";
  action: string;
  description?: string;
  available?: boolean;
};

const products: Product[] = [
  { id: "query", name: "智能问数", image: "/workspace-assets/generated/app-query.png", area: "query", kind: "application", action: "敬请期待", description: "自然语言提问，快速获取业务数据与答案", available: false },
  { id: "voc", name: "VOC智声", image: "/workspace-assets/generated/app-voc.png", area: "voc", kind: "application", action: "进入", description: "聚合全域声音，识别情绪、诉求与风险" },
  { id: "koc", name: "KOC智营", image: "/workspace-assets/generated/app-koc.png", area: "koc", kind: "application", action: "敬请期待", description: "识别关键影响者，驱动口碑传播与内容增长", available: false },
  { id: "lead", name: "线索运营", image: "/workspace-assets/generated/app-lead.png", area: "lead", kind: "application", action: "敬请期待", description: "识别高潜线索，推进转化过程与结果追踪", available: false },
  { id: "consumer", name: "消费者智调", image: "/workspace-assets/generated/app-consumer.png", area: "consumer", kind: "application", action: "进入", description: "快速洞察用户需求，验证产品与市场判断" },
  {
    id: "platform",
    name: "业务运营中台",
    image: "/workspace-assets/generated/platform-core.png",
    area: "platform",
    kind: "platform",
    action: "敬请期待",
    description: "统一客户数据、策略编排、任务协同与结果追踪",
    available: false,
  },
  {
    id: "user",
    name: "用户洞察引擎",
    image: "/workspace-assets/generated/engine-user.png",
    area: "user",
    kind: "engine",
    action: "敬请期待",
    description: "识别人群、关系与机会",
    available: false,
  },
  {
    id: "voice",
    name: "声音洞察引擎",
    image: "/workspace-assets/generated/engine-voice.png",
    area: "voice",
    kind: "engine",
    action: "进入引擎",
    description: "理解声音、情绪与意图",
  },
];

const productById = new Map(products.map((product) => [product.id, product]));

function getProduct(id: string) {
  const product = productById.get(id);
  if (!product) throw new Error(`Unknown product: ${id}`);
  return product;
}

function UserIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="8" r="4" />
      <path d="M4.5 21a7.5 7.5 0 0 1 15 0" />
    </svg>
  );
}

function ChevronIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="m8 10 4 4 4-4" />
    </svg>
  );
}

function ArrowIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M5 12h13M14 7l5 5-5 5" />
    </svg>
  );
}

function ProductCard({ product, active, onActivate, onOpen }: {
  product: Product;
  active: boolean;
  onActivate: (id: string | null) => void;
  onOpen: (product: Product) => void;
}) {
  const cardClass = [
    styles.card,
    styles[`card${product.kind[0].toUpperCase()}${product.kind.slice(1)}`],
    styles[`area${product.area[0].toUpperCase()}${product.area.slice(1)}`],
    product.available === false ? styles.cardUpcoming : "",
    active ? styles.cardActive : "",
  ].join(" ");

  return (
    <button
      className={cardClass}
      type="button"
      aria-label={`${product.name}，${product.action}`}
      onMouseEnter={() => onActivate(product.id)}
      onMouseLeave={() => onActivate(null)}
      onFocus={() => onActivate(product.id)}
      onBlur={() => onActivate(null)}
      onClick={() => onOpen(product)}
    >
      <span className={styles.glassEdge} aria-hidden="true" />
      <span className={styles.cardCopy}>
        <strong>{product.name}</strong>
        {product.description ? <span className={styles.description}>{product.description}</span> : null}
        <span className={`${styles.action} ${product.available === false ? styles.upcoming : ""}`}>
          {product.action}
          {product.available === false ? null : <ArrowIcon />}
        </span>
      </span>
      <span className={styles.visual}>
        <img src={product.image} alt="" draggable="false" />
      </span>
      {product.kind === "platform" ? <span className={styles.circuit} aria-hidden="true" /> : null}
    </button>
  );
}

export default function WorkbenchPage() {
  const [authorized, setAuthorized] = useState(false);
  const [activeId, setActiveId] = useState<string | null>(null);
  const [toast, setToast] = useState("");
  const [accountOpen, setAccountOpen] = useState(false);
  const accountRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (window.sessionStorage.getItem("agai-authenticated") !== "1") {
      window.location.replace("/login");
      return;
    }
    setAuthorized(true);
  }, []);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(""), 2200);
    return () => window.clearTimeout(timer);
  }, [toast]);

  useEffect(() => {
    function closeAccount(event: PointerEvent) {
      if (!accountRef.current?.contains(event.target as Node)) setAccountOpen(false);
    }
    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === "Escape") setAccountOpen(false);
    }
    document.addEventListener("pointerdown", closeAccount);
    document.addEventListener("keydown", closeOnEscape);
    return () => {
      document.removeEventListener("pointerdown", closeAccount);
      document.removeEventListener("keydown", closeOnEscape);
    };
  }, []);

  function openProduct(product: Product) {
    if (product.id === "voc") {
      window.localStorage.setItem("version", "1.0.0");
      window.localStorage.setItem("report_token", "voc-voice-local-demo-token");
      window.localStorage.setItem("report_user_name", "演示管理员");
      window.localStorage.setItem("report_user_id", "demo-admin");
      window.location.assign("/apps/voc/index.html#/overview");
      return;
    }
    if (product.id === "voice") {
      window.location.assign("/products/insight");
      return;
    }
    if (product.id === "consumer") {
      window.location.assign("/products/consumer");
      return;
    }
    setToast(product.available === false ? `${product.name}暂未开放，敬请期待` : `${product.name}入口待配置`);
  }

  function logout() {
    window.sessionStorage.removeItem("agai-authenticated");
    window.location.assign("/");
  }

  if (!authorized) return <main className={styles.shell} aria-label="正在验证登录状态" />;

  return (
    <main className={styles.shell}>
      <header className={styles.header}>
        <button className={styles.brand} type="button" onClick={() => window.location.assign("/")} aria-label="返回 AGAI 官网">AGAI</button>
        <span className={styles.divider} aria-hidden="true" />
        <strong className={styles.title}>智能工作台</strong>

        <div className={styles.accountActions} ref={accountRef}>
          <button className={styles.profile} type="button" onClick={() => setAccountOpen((value) => !value)} aria-label="Alex 账号中心" aria-haspopup="menu" aria-expanded={accountOpen}>
            <i><UserIcon /></i>
            <span>Alex</span>
            <ChevronIcon />
          </button>
          <div className={`${styles.accountMenu} ${accountOpen ? styles.accountMenuOpen : ""}`} role="menu" aria-hidden={!accountOpen}>
            <button type="button" role="menuitem" tabIndex={accountOpen ? 0 : -1} onClick={logout}>退出登录</button>
          </div>
        </div>
      </header>

      <section className={styles.workspace} aria-labelledby="workbench-heading">
        <div className={styles.welcome}>
          <h1 id="workbench-heading">你好， Alex</h1>
          <p>欢迎回到 AGAI 智能工作台</p>
        </div>

        <div className={styles.cardGrid}>
          <div className={styles.upperMatrix}>
            <div className={styles.upperLeft}>
              <div className={styles.topRow}>
                {["voc", "consumer", "koc"].map((id) => {
                  const product = getProduct(id);
                  return <ProductCard key={id} product={product} active={activeId === id} onActivate={setActiveId} onOpen={openProduct} />;
                })}
              </div>
              <div className={styles.middleRow}>
                {["platform", "query"].map((id) => {
                  const product = getProduct(id);
                  return <ProductCard key={id} product={product} active={activeId === id} onActivate={setActiveId} onOpen={openProduct} />;
                })}
              </div>
            </div>
            <ProductCard product={getProduct("lead")} active={activeId === "lead"} onActivate={setActiveId} onOpen={openProduct} />
          </div>
          <div className={styles.bottomRow}>
            {["voice", "user"].map((id) => {
              const product = getProduct(id);
              return <ProductCard key={id} product={product} active={activeId === id} onActivate={setActiveId} onOpen={openProduct} />;
            })}
          </div>
        </div>
      </section>

      <div className={`${styles.toast} ${toast ? styles.toastVisible : ""}`} role="status" aria-live="polite">
        {toast}
      </div>
    </main>
  );
}
