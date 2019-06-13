import Vue from 'vue'
import App from './App'
import store from './store'
import router from './router'
import client from '@/api/client'
import moment from 'moment'
import Vue2TouchEvents from 'vue2-touch-events'
import VScrollLock from 'v-scroll-lock'

Vue.use(VScrollLock)

Vue.use(Vue2TouchEvents, {
  touchClass: '',
  tapTolerance: 10,
  swipeTolerance: 30,
  longTapTimeInterval: 400
})

Vue.use({
  router,
  store,
  axios: client,
  errorHandler: () => {
    store.dispatch('setTopMessage', { active: true, msg: { pt: Vue.i18n.translateIn('pt', 'message.error.internalError'), en: Vue.i18n.translateIn('en', 'message.error.internalError') }, dismiss: true, type: 'warn' })
  },
  notAuthorizedHandler: () => {
    alert('Not authorized')
  },
  notAuthorizedHandlerRoute: (auth, user) => {
    alert('Not authorized route')
  }
})

Vue.mixin({
  methods: {
    setLocale (locale) {
      this.$i18n.set(locale)
      moment.locale(locale)
    }
  }
})

window.addEventListener('offline', () => {
  store.dispatch('setTopMessage', { active: true, msg: { pt: Vue.i18n.translateIn('pt', 'message.error.noNetwork'), en: Vue.i18n.translateIn('en', 'message.error.noNetwork') }, dismiss: false, type: 'warn' })
}, false)

window.addEventListener('online', () => {
  if (store.state.topMessage.active && store.state.topMessage.msg[Vue.i18n.locale()] === Vue.i18n.translate('message.error.noNetwork')) {
    store.dispatch('setTopMessage', { active: false, msg: { pt: '', en: '' }, dismiss: false, type: '' })
  }
}, false)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  render (createElement) {
    return createElement(App, {})
  }
})
