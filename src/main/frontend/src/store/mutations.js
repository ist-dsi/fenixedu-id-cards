import Vue from 'vue'
import * as types from './mutation-types'
import * as cardStates from '@/utils/cards/CardStates'

export default {
  [types.SET_TOP_MESSAGE] (state, { active, msg, dismiss, type }) {
    state.topMessage = { active, msg, dismiss, type }
  },
  [types.RECEIVE_PROFILE] (state, { profile }) {
    Vue.set(state, 'profile', profile)
  },
  [types.RECEIVE_CARDS] (state, cards) {
    Vue.set(state.cardsPage, 'cards', cards.filter(card => card.currentState !== cardStates.IGNORED))
  },
  [types.RECEIVE_PREVIEW] (state, { cardPreview }) {
    Vue.set(state, 'cardPreview', cardPreview)
  }
}
