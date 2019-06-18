import client from '@/api/client'

async function getCards () {
  const response = await client.get('/idcards/')
  return response.data
}

async function getUserCards (username) {
  const response = await client.get(`/idcards/${username}`)
  return response.data
}

async function getPreview () {
  const response = await client.get('/idcards/preview')
  return response.data
}

async function requestNew () {
  await client.post('/idcards')
}

export default {
  getCards,
  getUserCards,
  getPreview,
  requestNew
}
