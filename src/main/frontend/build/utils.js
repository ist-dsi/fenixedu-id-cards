
const path = require('path')
const fs = require('fs')

const isPathIn = function (srcPath, debug) {
  return function (modulePath) {
    if (srcPath instanceof Array) {
      const srcArray = srcPath.map((s) => path.resolve(__dirname, s))
      for (let src of srcArray) {
        if (modulePath.includes(src)) {
          if (debug) {
            fs.appendFileSync('./webpack.include.txt', `PATH: ${modulePath}\n`)
          }
          return true
        }
      }
      return false
    } else {
      const src = path.resolve(__dirname, srcPath)
      if (modulePath.includes(src)) {
        if (debug) {
          fs.appendFileSync('./webpack.include.txt', `PATH: ${modulePath}\n`)
        }
        return true
      } else {
        return false
      }
    }
  }
}

module.exports = {
  isPathIn: isPathIn
}
