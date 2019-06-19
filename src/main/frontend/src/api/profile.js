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

async function changeLocale (language) {
  let localeTag = language === 'pt' ? 'pt-PT' : 'en-GB'

  await client.post(`api/bennu-core/profile/locale/${localeTag}`, null, {
    headers: { 'X-Requested-With': 'fenixedu-id-cards-frontend' }
  })
}

export default {
  get,
  changeLocale
}
