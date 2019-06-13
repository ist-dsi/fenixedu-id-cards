import translationsPt from './pt.json'
import translationsEn from './en.json'

export default function (Vue, store) {
  Vue.i18n.add('en', translationsEn)
  Vue.i18n.add('pt', translationsPt)
  Vue.i18n.set('pt')
}
