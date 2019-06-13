import Vue from 'vue'
import client from '@/api/client'
import moment from 'moment'

async function get () {
  try {
    const response = await client.get('/api/fenix/v1/person')
    const adminResponse = await client.get('/idcards/isCardsAdmin')

    Vue.i18n.set(response.data.locale || 'pt')
    moment.locale(response.data.locale || 'pt')

    return {
      profile: {
        ...response.data,
        isAdmin: adminResponse.data
      }
    }
  } catch (err) {
    if (err.response.status === 401) {
      window.location.href = 'https://id.tecnico.ulisboa.pt'
    } else {
      throw err
    }
  }
}

export default {
  get
}
