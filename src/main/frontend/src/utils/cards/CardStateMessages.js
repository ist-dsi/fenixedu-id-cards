import * as cardStates from './CardStates'

export default {
  [cardStates.REQUESTED]: 'Requested',
  [cardStates.BANK_REQUEST]: 'Bank requested card for production',
  [cardStates.IN_PRODUCTION]: 'In production',
  [cardStates.READY_FOR_PICKUP]: 'Ready for pickup',
  [cardStates.DELIVERED]: 'Delivered'
}
