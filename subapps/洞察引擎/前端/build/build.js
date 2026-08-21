console.log('build > 文件开始执行！')
// import fs from 'fs'
// import path from 'path'
const fs = require('fs')
const path = require('path')

function getRootPath(...dir) {
  return path.resolve(process.cwd(), ...dir)
}
const runBuild = async () => {
  try {
    const OUTPUT_DIR = 'dist'
    const VERSION = 'version.json'
    const versionJson = {
      version: 'V_' + Math.floor(Math.random() * 10000) + Date.now()
    }
    fs.writeFileSync(getRootPath(`${OUTPUT_DIR}/${VERSION}`), JSON.stringify(versionJson))
    console.log(`version file is build successfully!`)
  } catch (error) {
    console.error('version build error:\n' + error)
    process.exit(1)
  }
}
runBuild()
console.log('build > 文件执行结束！')
