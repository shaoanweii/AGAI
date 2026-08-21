import { readFile } from 'node:fs/promises'
import path from 'node:path'

import fg from 'fast-glob'
import type { Plugin } from 'vite'

const virtualModuleId = 'virtual:svg-icons-register'
const resolvedVirtualModuleId = `\0${virtualModuleId}`
const svgDomId = '__svg__icons__dom__'

/** 将版本库内的 SVG 编译为页面 symbol，避开存在已知漏洞的 svg-baker 链路。 */
export function createLocalSvgIconsPlugin(iconDir: string): Plugin {
  return {
    name: 'agai-local-svg-icons',
    resolveId(id) {
      return id === virtualModuleId ? resolvedVirtualModuleId : null
    },
    async load(id) {
      if (id !== resolvedVirtualModuleId) return null

      const files = (await fg('**/*.svg', { cwd: iconDir, absolute: true })).sort()
      const symbols = await Promise.all(
        files.map(async file => {
          this.addWatchFile(file)
          const relativePath = path.relative(iconDir, file).split(path.sep).join('/')
          const symbolId = `icon-${relativePath.replace(/\.svg$/i, '').split('/').join('-')}`
          const source = await readFile(file, 'utf8')
          const match = source.match(/<svg\b([^>]*)>([\s\S]*?)<\/svg>\s*$/i)

          if (!match) throw new Error(`无法解析 SVG 图标：${relativePath}`)

          const attributes = match[1]
            .replace(/\s(?:xmlns(?::xlink)?|width|height)=("[^"]*"|'[^']*')/gi, '')
            .replace(/stroke="[a-zA-Z#0-9]*"/, 'stroke="currentColor"')
          const escapedId = symbolId.replace(/&/g, '&amp;').replace(/"/g, '&quot;')
          return `<symbol id="${escapedId}"${attributes}>${match[2]}</symbol>`
        })
      )

      const sprite = JSON.stringify(symbols.join(''))
      return `
if (typeof document !== 'undefined') {
  const mountSprite = () => {
    let svg = document.getElementById('${svgDomId}')
    if (!svg) {
      svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg')
      svg.id = '${svgDomId}'
      svg.setAttribute('aria-hidden', 'true')
      svg.style.position = 'absolute'
      svg.style.width = '0'
      svg.style.height = '0'
      document.body.insertBefore(svg, document.body.firstChild)
    }
    svg.innerHTML = ${sprite}
  }
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', mountSprite, { once: true })
  } else {
    mountSprite()
  }
}
export default {}
`
    }
  }
}
