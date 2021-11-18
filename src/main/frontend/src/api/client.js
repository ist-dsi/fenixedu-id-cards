import axios from 'axios'

const client = axios.create({
  baseURL: `${process.env.VUE_APP_CTX ?? '/'}`
})

client.interceptors.request.use(function (config) {
  if (!['get', 'head'].includes(config.method.toLowerCase())) {
    config.headers['X-Requested-With'] = 'fenixedu-id-cards-frontend'
  }
  return config
})

export default client
