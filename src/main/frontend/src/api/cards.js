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
  const response = await client.post('/idcards', requestReason, {
    headers: {
      'Content-Type': 'text/plain'
    }
  })
  return response.data
}

async function deliverCard (id) {
  const response = await client.put(`/idcards/${id}/deliver`)
  return response.data
}

async function getUserNames () {
  const response = await client.get('/idcards/user-names')
  return response.data
}

async function changeCardName (cardName) {
  const response = await client.post('/idcards/change-card-name', cardName, {
    headers: {
      'Content-Type': 'text/plain'
    }
  })
  return response.data
}

async function getAdminSession () {
  const response = await client.get('/idcards/deliver/admin-session')
  return response.data
}

async function submitUserMifare (request) {
  const response = await client.put('/idcards/deliver/admin-session', request)
  return response.data
}

export default {
  getCards,
  getPreview,
  requestNew,
  deliverCard,
  getUserNames,
  changeCardName,
  getAdminSession,
  submitUserMifare
}
