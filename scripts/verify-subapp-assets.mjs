import { access, readFile } from "node:fs/promises";
import { dirname, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";

const projectRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const publicRoot = resolve(projectRoot, "public");
const appNames = ["consumer", "insight", "voc"];
const assetReferencePattern = /(?:src|href)=["']([^"']+)["']/g;

/**
 * 校验子应用入口引用的同域静态资源，防止只发布 index.html 而漏掉 JS/CSS。
 */
async function verifyAppAssets(appName) {
  const indexPath = resolve(publicRoot, "apps", appName, "index.html");
  const html = await readFile(indexPath, "utf8");
  const references = [...html.matchAll(assetReferencePattern)]
    .map(match => match[1])
    .filter(reference => reference.startsWith(`/apps/${appName}/`));

  if (references.length === 0) {
    throw new Error(`${appName}: index.html 未发现同域静态资源引用`);
  }

  const missing = [];
  for (const reference of new Set(references)) {
    const pathname = decodeURIComponent(new URL(reference, "https://agai.local").pathname);
    const assetPath = resolve(publicRoot, `.${pathname}`);

    // 入口内容属于构建产物，但仍需阻止异常路径逃逸 public 目录。
    if (!assetPath.startsWith(`${publicRoot}${sep}`)) {
      missing.push(`${reference}（路径越界）`);
      continue;
    }

    try {
      await access(assetPath);
    } catch {
      missing.push(reference);
    }
  }

  if (missing.length > 0) {
    throw new Error(`${appName}: 缺少 ${missing.length} 个静态资源\n${missing.join("\n")}`);
  }

  return { appName, references: new Set(references).size };
}

const results = await Promise.all(appNames.map(verifyAppAssets));
for (const { appName, references } of results) {
  console.log(`✓ ${appName}: ${references} 个入口资源均存在`);
}
