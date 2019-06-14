import * as types from './mutation-types'
import ProfileAPI from '@/api/profile'
import CardsAPI from '@/api/cards'

export const setTopMessage = ({ commit }, { active, msg, dismiss, type }) => {
  commit(types.SET_TOP_MESSAGE, { active, msg, dismiss, type })
}

export const fetchProfile = async ({ commit }) => {
  return ProfileAPI.get()
    .then(profile => commit(types.RECEIVE_PROFILE, profile))
    .catch(err => console.error(err))
}

export const fetchCards = async ({ commit }) => {
  return CardsAPI.getCards()
    .then(cards => commit(types.RECEIVE_CARDS, cards))
    .catch(err => console.error(err))
}

export const fetchUserCards = async ({ commit }, { username }) => {
  return CardsAPI.getUserCards(username)
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
