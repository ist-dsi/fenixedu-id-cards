import Vue from 'vue'
import Vuex from 'vuex'
import * as actions from './actions'
import mutations from './mutations'

Vue.use(Vuex)

const state = {
  topMessage: { active: false, msg: { pt: '', en: '' }, dismiss: false, type: '' },
  isInitialLoading: false,
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

export default store
