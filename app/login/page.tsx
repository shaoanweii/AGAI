"use client";

import { FormEvent, useEffect, useState } from "react";
import styles from "./login.module.css";

function Icon({ name }: { name: "arrow" | "user" | "lock" | "eye" | "building" }) {
  const paths = {
    arrow: <><path d="M19 12H5M11 6l-6 6 6 6"/></>,
    user: <><circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/></>,
    lock: <><rect x="5" y="10" width="14" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v3"/></>,
    eye: <><path d="M2.5 12s3.3-5.5 9.5-5.5 9.5 5.5 9.5 5.5-3.3 5.5-9.5 5.5S2.5 12 2.5 12Z"/><circle cx="12" cy="12" r="2.5"/></>,
    building: <><path d="M4 21V5l8-2v18M12 8h7v13M2 21h20M7 8h2M7 12h2M7 16h2M15 11h2M15 15h2"/></>,
  };
  return <svg viewBox="0 0 24 24" aria-hidden="true">{paths[name]}</svg>;
}

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [toast, setToast] = useState("");
  const [account, setAccount] = useState("admin");
  const [password, setPassword] = useState("FT2026");
  const [error, setError] = useState("");

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(""), 2200);
    return () => window.clearTimeout(timer);
  }, [toast]);

  function enterWorkbench(event?: FormEvent) {
    event?.preventDefault();
    if (account !== "admin" || password !== "FT2026") {
      setError("账号或密码错误，请重新输入");
      return;
    }
    window.sessionStorage.setItem("agai-authenticated", "1");
    setError("");
    window.location.assign("/workbench");
  }

  return (
    <main className={styles.shell}>
      <header className={styles.header}>
        <button className={styles.logo} type="button" onClick={() => window.location.assign("/")} aria-label="返回 AGAI 官网">AGAI</button>
        <button className={styles.back} type="button" onClick={() => window.location.assign("/")}><Icon name="arrow" />AGAI 官网</button>
      </header>

      <div className={styles.ambient} aria-hidden="true" />
      <section className={styles.content}>
        <div className={styles.ecosystem} aria-label="AGAI 产品生态插画">
          <img src="/workspace-assets/login-cover-ecosystem-v3.png" width="1672" height="941" alt="由业务运营中台、五个业务应用与双洞察引擎组成的 AGAI 登录页生态背景" />
        </div>

        <div className={styles.loginFrame}>
          <div className={styles.corner} aria-hidden="true" />
          <div className={styles.loginPanel}>
            <div className={styles.heading}>
              <h1>欢迎登录 <span>AGAI</span></h1>
              <p>一个账号，连接全域数智产品</p>
            </div>

            <form onSubmit={enterWorkbench}>
              <label htmlFor="account">账号</label>
              <div className={styles.field}>
                <Icon name="user" />
                <input id="account" name="account" autoComplete="username" value={account} onChange={(event) => { setAccount(event.target.value); setError(""); }} placeholder="请输入账号" />
              </div>

              <label htmlFor="password">密码</label>
              <div className={styles.field}>
                <Icon name="lock" />
                <input id="password" name="password" type={showPassword ? "text" : "password"} autoComplete="current-password" value={password} onChange={(event) => { setPassword(event.target.value); setError(""); }} placeholder="请输入密码" />
                <button className={styles.eye} type="button" aria-label={showPassword ? "隐藏密码" : "显示密码"} onClick={() => setShowPassword((value) => !value)}><Icon name="eye" /></button>
              </div>

              <div className={styles.options}>
                <label className={styles.remember}><input type="checkbox" defaultChecked /><span aria-hidden="true">✓</span>记住账号</label>
                <div>
                  <button type="button" onClick={() => setToast("请联系管理员重置密码")}>忘记密码</button>
                  <button type="button" onClick={() => setToast("管理员联系方式待配置")}>联系管理员</button>
                </div>
              </div>

              <p className={`${styles.error} ${error ? styles.errorVisible : ""}`} role="alert">{error || "登录提示"}</p>

              <button className={styles.primary} type="submit">登录</button>
              <button className={styles.sso} type="button" onClick={() => setToast("企业统一登录待配置")}><Icon name="building" />企业统一登录</button>
            </form>
          </div>
        </div>
      </section>

      <div className={`${styles.toast} ${toast ? styles.visible : ""}`} role="status" aria-live="polite">{toast}</div>
    </main>
  );
}
