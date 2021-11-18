import client from '@/api/client'
import { setLocale } from '@/i18n'

async function get () {
  try {
    const personResponse = await client.get('/api/fenix/v1/person')
    const userResponse = await client.get('/idcards/user-info')

    await setLocale(userResponse.data.language || 'pt')

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

export async function changeLocale (language) {
  const localeTag = language === 'pt' ? 'pt-PT' : 'en-GB'
  await client.post(`api/bennu-core/profile/locale/${localeTag}`)
}

export default {
  get,
  changeLocale
}
