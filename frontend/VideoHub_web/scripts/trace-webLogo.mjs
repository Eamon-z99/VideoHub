import { createRequire } from 'node:module'
import { writeFile } from 'node:fs/promises'
import path from 'node:path'

const require = createRequire(import.meta.url)
const potrace = require('potrace')

const input = path.resolve('public/assets/webLogo.png')
const output = path.resolve('public/assets/webLogo.svg')

const trace = (src) =>
  new Promise((resolve, reject) => {
    potrace.trace(
      src,
      {
        // 只矢量化黑色笔画，背景不写入（保持透明）
        color: '#000',
        background: 'transparent',
        threshold: 210,
        turdSize: 10,
        optCurve: true,
        optTolerance: 0.4,
      },
      (err, svg) => {
        if (err) reject(err)
        else resolve(svg)
      },
    )
  })

const svg = await trace(input)
await writeFile(output, svg, 'utf8')
console.log(`Wrote ${output}`)

