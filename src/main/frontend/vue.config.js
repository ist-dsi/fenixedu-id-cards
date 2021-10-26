const StyleLintPlugin = require('stylelint-webpack-plugin')

module.exports = {
  transpileDependencies: [
    // can be string or regex
    // 'my-dep',
    // /other-dep/
  ],
  outputDir: 'dist',
  publicPath: '/',
  indexPath: 'index.html', // generated index file
  filenameHashing: true,
  runtimeCompiler: false,
  productionSourceMap: false,
  pages: {
    index: {
      // entry for the page
      entry: 'src/main.js',
      // the source template
      template: 'index.html',
      // output as dist/index.html
      filename: 'index.html'
    }
  },
  pluginOptions: {
    webpackBundleAnalyzer: {
      openAnalyzer: false,
      reportFilename: '../webpack-bundle-report.html'
    }
  },
  // Vue CLI sets mode to development when `npm run serve` is run and to production whith `npm run build`
  // Because of this mode should not be set here, devTool is set to source-map in production
  // Development uses a more performant option (speeds up rebuilds) that despite that correctlly maps to the source code https://webpack.js.org/configuration/devtool/
  // Docs: https://cli.vuejs.org/config/#configurewebpackc
  configureWebpack: {
    // Docs: https://webpack.js.org/configuration/optimization/
    optimization: {
      // Docs: https://webpack.js.org/plugins/split-chunks-plugin/
      splitChunks: {
        chunks: 'async',
        minSize: 20480,
        maxSize: 0,
        minChunks: 1,
        maxAsyncRequests: 30,
        maxInitialRequests: 30,
        automaticNameDelimiter: '~',
        enforceSizeThreshold: 249856
      }
    },
    resolve: {
      extensions: ['.js', '.vue', '.json'],
      alias: {
        '@/': './src'
      }
    },
    // Docs: https://webpack.js.org/configuration/dev-server/
    devServer: {
      host: 'localhost',
      port: 8081,
      https: true,
      open: true,
      overlay: {
        warnings: true,
        errors: true
      },
      progress: true,
      proxy: {
        '/api': {
          secure: false,
          target: 'http://localhost:8080'
        },
        '/idcards': {
          secure: false,
          target: 'http://localhost:8080'
        }
      },
      disableHostCheck: true,
      historyApiFallback: true
    }
  },
  css: {
    // globally import _variables.scss as a resource file
    loaderOptions: {
      sass: {
        prependData: '@import "@/assets/scss/_variables.scss";'
      }
    }
  },
  chainWebpack: (config) => {
    config
      .plugin('stylelint')
      .before('vue-loader')
      .use(StyleLintPlugin, [{ files: ['src/**/*.{vue,scss}'] }])
  }
}
