import { spawn } from 'node:child_process'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const node = process.execPath
const viteBin = path.join(root, 'node_modules', 'vite', 'bin', 'vite.js')

const server = spawn(node, [path.join(root, 'local-demo', 'server.mjs'), '4174'], {
  cwd: root,
  stdio: 'inherit',
  env: { ...process.env, VOC_DEMO_PUBLIC_PORT: '5173' }
})
const vite = spawn(node, [viteBin, '--mode', 'local-demo'], { cwd: root, stdio: 'inherit' })

const shutdown = signal => {
  server.kill(signal)
  vite.kill(signal)
}

process.on('SIGINT', () => shutdown('SIGINT'))
process.on('SIGTERM', () => shutdown('SIGTERM'))
vite.on('exit', code => {
  server.kill('SIGTERM')
  process.exit(code || 0)
})
