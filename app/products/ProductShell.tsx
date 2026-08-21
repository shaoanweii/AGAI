"use client";

import { useEffect, useState } from "react";
import styles from "./product-shell.module.css";

type ProductShellProps = {
  name: string;
  source: string;
};

export default function ProductShell({ name, source }: ProductShellProps) {
  const [authorized, setAuthorized] = useState(false);
  const [loaded, setLoaded] = useState(false);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    // 子系统统一继承 AGAI 会话，不再暴露各自的登录页。
    if (window.sessionStorage.getItem("agai-authenticated") !== "1") {
      window.location.replace("/login");
      return;
    }
    setAuthorized(true);
  }, []);

  if (!authorized) return <main className={styles.shell} aria-label="正在验证登录状态" />;

  return (
    <main className={styles.shell}>
      <section className={styles.frameWrap} aria-label={name}>
        <div className={`${styles.loading} ${loaded ? styles.loadingHidden : ""}`} aria-hidden={loaded}>正在进入{name}…</div>
        {failed ? (
          <div className={styles.error} role="alert">
            <strong>{name}服务未启动</strong>
            <p>请启动 AGAI 生态服务后重新进入。</p>
          </div>
        ) : null}
        <iframe
          className={styles.frame}
          src={source}
          title={name}
          allow="clipboard-read; clipboard-write; fullscreen"
          onLoad={() => setLoaded(true)}
          onError={() => setFailed(true)}
        />
      </section>
    </main>
  );
}
