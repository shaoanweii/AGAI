import fs from 'node:fs'
import path from 'node:path'

const root = process.cwd()
const targets = process.argv.slice(2).length ? process.argv.slice(2) : ['dist']
const forbidden = [
  /富通/gi,
  /长安/gi,
  /changan\.com/gi,
  /@ichangan/gi,
  /\bcqca\b/gi,
  /172\.16\./g,
  /10\.63\./g,
  /192\.168\./g,
  /eyJhbGciOi/gi
]

const findings = []
const textExtensions = new Set([
  '.css',
  '.csv',
  '.html',
  '.js',
  '.json',
  '.map',
  '.md',
  '.mjs',
  '.svg',
  '.txt',
  '.xml'
])

const scanText = (content, location) => {
  forbidden.forEach(pattern => {
    pattern.lastIndex = 0
    if (pattern.test(content)) findings.push(`${path.relative(root, location)}: ${pattern}`)
  })
}

const visit = location => {
  if (!fs.existsSync(location)) return
  const stat = fs.statSync(location)
  if (stat.isDirectory()) {
    fs.readdirSync(location)
      .filter(name => name !== 'node_modules')
      .forEach(name => visit(path.join(location, name)))
    return
  }
  if (stat.size > 8 * 1024 * 1024) return
  scanText(path.relative(root, location), location)
  if (!textExtensions.has(path.extname(location).toLowerCase())) return
  scanText(fs.readFileSync(location, 'utf8'), location)
}

targets.forEach(target => visit(path.resolve(root, target)))
if (findings.length) {
  console.error('VOC智声发布物仍包含企业专属痕迹：')
  findings.forEach(item => console.error(`- ${item}`))
  process.exit(1)
}
console.log(`VOC智声清洁门禁通过：${targets.join(', ')}`)
