import client from '@/api/client'

async function getCards () {
  const response = await client.get('/idcards/getUserCards')
  return response.data
}

async function getUserCards (username) {
  const response = await client.get(`/idcards/getUserCards/${username}`)
  return response.data
}

async function getPreview () {
  const response = await client.get('/idcards/previewCard')
  return response.data
}

async function requestNew () {
  await client.post('/idcards/requestCard')
}

export default {
  getCards,
  getUserCards,
  getPreview,
  requestNew
}
