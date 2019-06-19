import * as types from './mutation-types'
import ProfileAPI from '@/api/profile'
import CardsAPI from '@/api/cards'
import store from '@/store'

export const setTopMessage = ({ commit }, { active, msg, dismiss, type }) => {
  commit(types.SET_TOP_MESSAGE, { active, msg, dismiss, type })
}

export const fetchProfile = async ({ commit }) => {
  return ProfileAPI.get()
    .then(profile => commit(types.RECEIVE_PROFILE, profile))
    .catch(err => console.error(err))
}

export const fetchCards = async ({ commit }) => {
  const username = store.state.currentUser || store.state.profile.username

  return CardsAPI.getCards(username)
    .then(cards => commit(types.RECEIVE_CARDS, cards))
    .catch(err => console.error(err))
}

export const fetchPreview = async ({ commit }) => {
  const cardPreview = await CardsAPI.getPreview()

  commit(types.RECEIVE_PREVIEW, { cardPreview })
}

export const requestNewCard = async ({ commit }) => {
  await CardsAPI.requestNew()
}

export const changeCurrentUser = async ({ commit }, { username }) => {
  commit(types.CHANGE_CURRENT_USER, { username })
}
