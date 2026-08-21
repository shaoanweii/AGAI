import React from 'react'
import { createRoot } from 'react-dom/client'
import './styles.css'

const sections = [
  {
    id: 'capability',
    nav: '行业能力',
    src: '/boards/02-capability.png',
    alt: 'AGAI 汽车行业数智能力：全域数据接入、行业语义理解与业务闭环',
  },
  {
    id: 'products',
    nav: '产品生态',
    src: '/boards/03-product-ecosystem.png',
    alt: 'AGAI 产品生态：业务应用、业务运营中台、用户洞察引擎与声音洞察引擎',
  },
  {
    id: 'solutions',
    nav: '解决方案',
    src: '/boards/04-automotive-scenarios.png',
    alt: 'AGAI 贯穿 5A 用户旅程的汽车行业解决方案',
  },
  {
    id: 'customers',
    nav: '客户实践',
    src: '/boards/05-customer-practice.png',
    alt: 'AGAI 汽车行业客户实践与合作品牌',
  },
  {
    id: 'about',
    nav: '关于 AGAI',
    src: '/boards/06-simple-footer.png',
    alt: '让客户之声直达决策与增长',
  },
]

function goTo(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function Header() {
  return (
    <header className="site-header" aria-label="主导航">
      <button className="brand" type="button" onClick={() => goTo('hero')} aria-label="返回首页">AGAI</button>
      <nav>
        <button type="button" onClick={() => goTo('products')}>产品生态</button>
        <button type="button" onClick={() => goTo('solutions')}>解决方案</button>
        <button type="button" onClick={() => goTo('capability')}>行业能力</button>
        <button type="button" onClick={() => goTo('customers')}>客户实践</button>
        <button type="button" onClick={() => goTo('about')}>关于 AGAI</button>
      </nav>
      <button className="workbench" type="button" onClick={() => window.dispatchEvent(new CustomEvent('agai:workbench'))}>
        进入工作台
      </button>
    </header>
  )
}

function App() {
  const [notice, setNotice] = React.useState(false)

  React.useEffect(() => {
    const open = () => {
      setNotice(true)
      window.setTimeout(() => setNotice(false), 2200)
    }
    window.addEventListener('agai:workbench', open)
    return () => window.removeEventListener('agai:workbench', open)
  }, [])

  return (
    <>
      <main>
        <section className="hero-board" id="hero">
          <Header />
          <img src="/boards/01-hero-body.png" width="1672" height="847" alt="让汽车增长，因 AI 而发生" fetchPriority="high" />
        </section>
        {sections.map(section => (
          <section className="design-board" id={section.id} key={section.id} aria-label={section.nav}>
            <img src={section.src} width="1672" height="941" alt={section.alt} loading="lazy" />
          </section>
        ))}
      </main>
      <div className={`notice ${notice ? 'is-visible' : ''}`} role="status" aria-live="polite">
        工作台入口即将开放
      </div>
    </>
  )
}

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
