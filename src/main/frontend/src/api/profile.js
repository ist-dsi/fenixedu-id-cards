import Vue from 'vue'
import client from '@/api/client'

async function get () {
  try {
    const personResponse = await client.get('/api/fenix/v1/person')
    const userResponse = await client.get('/idcards/user-info')

    Vue.i18n.set(userResponse.data.language || 'pt')

    return {
      profile: {
        ...personResponse.data,
        ...userResponse.data
      }
    }
  } catch (err) {
    if (err.response.status === 401) {
      window.location.href = `/login?callback=${window.location}`
    } else {
      throw err
    }
  }
}

export default {
  get
}
