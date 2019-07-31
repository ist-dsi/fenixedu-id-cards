import client from '@/api/client'

async function getCards (username) {
  const response = await client.get(`/idcards/${username}`)
  return response.data
}

async function getPreview () {
  const response = await client.get('/idcards/preview')
  return response.data
}

async function requestNew ({ requestReason }) {
  await client.post('/idcards', requestReason, { headers: { 'X-Requested-With': 'fenixedu-id-cards-frontend', 'Content-Type': 'text/plain' } })
}

async function deliverCard (id) {
  await client.put(`/idcards/${id}/deliver`, null, { headers: { 'X-Requested-With': 'fenixedu-id-cards-frontend' } })
}

async function getUserNames () {
  const response = await client.get('/idcards/user-names')
  return response.data
}

async function changeCardName (cardName) {
  await client.post('/idcards/change-card-name', cardName, { headers: { 'X-Requested-With': 'fenixedu-id-cards-frontend', 'Content-Type': 'text/plain' } })
}

export default {
  getCards,
  getPreview,
  requestNew,
  deliverCard,
  getUserNames,
  changeCardName
}
