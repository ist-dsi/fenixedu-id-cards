import client from '@/api/client'

async function getCards (username) {
  const response = await client.get(`/idcards/${username}`)
  return response.data
}

async function getPreview () {
  const response = await client.get('/idcards/preview')
  return response.data
}

async function requestNew () {
  await client.post('/idcards', null, { headers: { 'X-Requested-With': 'fenixedu-id-cards-frontend' } })
}

export default {
  getCards,
  getPreview,
  requestNew
}
