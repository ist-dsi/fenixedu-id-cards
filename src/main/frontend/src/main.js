import Vue from 'vue'
import App from './App'
import store from './store'
import router from './router'
import i18n, { setLocale } from '@/i18n'

import Vue2TouchEvents from 'vue2-touch-events'
import VScrollLock from 'v-scroll-lock'

Vue.use(VScrollLock)

Vue.use(Vue2TouchEvents, {
  touchClass: '',
  tapTolerance: 10,
  swipeTolerance: 30,
  longTapTimeInterval: 400
})

Vue.mixin({
  methods: { setLocale }
})

window.addEventListener('offline', () => {
  store.dispatch('setTopMessage', { active: true, msg: { pt: i18n.t('message.error.noNetwork', 'pt'), en: i18n.t('message.error.noNetwork', 'en') }, dismiss: false, type: 'warn' })
}, false)

window.addEventListener('online', () => {
  const { active, msg } = store.state.topMessage
  if (active && msg[i18n.locale] === i18n.t('message.error.noNetwork')) {
    store.dispatch('setTopMessage', { active: false, msg: { pt: '', en: '' }, dismiss: false, type: '' })
  }
}, false)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  i18n,
  render (createElement) {
    return createElement(App, {})
  }
})
