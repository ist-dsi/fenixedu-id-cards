import Vue from 'vue'
import * as types from './mutation-types'

export default {
  [types.SET_TOP_MESSAGE] (state, { active, msg, dismiss, type }) {
    state.topMessage = { active, msg, dismiss, type }
  },
  [types.RECEIVE_PROFILE] (state, { profile }) {
    Vue.set(state, 'profile', profile)
  },
  [types.RECEIVE_CARDS] (state, cards) {
    Vue.set(state.cardsPage, 'cards', cards)
  },
  [types.RECEIVE_PREVIEW] (state, { cardPreview }) {
    Vue.set(state, 'cardPreview', cardPreview)
  },
  [types.CHANGE_CURRENT_USER] (state, { username }) {
    Vue.set(state, 'currentUser', username)
  }
}
