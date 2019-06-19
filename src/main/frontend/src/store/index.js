import Vue from 'vue'
import Vuex from 'vuex'
import VuexI18N from 'vuex-i18n'

import * as actions from './actions'
import mutations from './mutations'
import setupI18N from '@/i18n'

Vue.use(Vuex)

const state = {
  topMessage: { active: false, msg: { pt: ' ', en: '' }, dismiss: false, type: '' },
  profile: {},
  cardsPage: {},
  cardPreview: {},
  currentUser: {}
}

const store = new Vuex.Store({
  state,
  actions,
  mutations
})

Vue.use(VuexI18N.plugin, store)
setupI18N(Vue, store)

export default store
