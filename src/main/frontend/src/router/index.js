import Vue from 'vue'
import Router from 'vue-router'
import store from '@/store'
import PageWithNavBarAndFooterLayout from '@/layouts/PageWithNavBarAndFooterLayout'
import ListCardsPage from '@/pages/ListCardsPage'
import PageNotFoundPage from '@/pages/PageNotFoundPage'
import AdminUserSearchPage from '@/pages/AdminUserSearchPage'
import AdminViewUserCardsPage from '@/pages/AdminViewUserCardsPage'
import UnauthorizedPage from '@/pages/UnauthorizedPage'
import CardReviewPage from '@/pages/CardReviewPage'

Vue.use(Router)

const router = new Router({
  mode: 'history',
  base: '/tecnico-card',
  routes: [
    {
      path: '',
      async beforeEnter (to, from, next) {
        store.dispatch('setInitialLoading', { isInitialLoading: true })
        await store.dispatch('fetchProfile')
        store.dispatch('setInitialLoading', { isInitialLoading: false })
        next()
      },
      component: PageWithNavBarAndFooterLayout,
      children: [
        {
          path: '/unauthorized',
          name: 'UnauthorizedPage',
          component: UnauthorizedPage
        },
        {
          path: '/review',
          name: 'CardReviewPage',
          component: CardReviewPage,
          async beforeEnter (to, from, next) {
            store.dispatch('setInitialLoading', { isInitialLoading: true })
            try {
              await store.dispatch('fetchPreview')
            } catch (err) {}

            store.dispatch('setInitialLoading', { isInitialLoading: false })
            next()
          }
        },
        {
          path: '/',
          name: 'ListCardsPage',
          component: ListCardsPage,
          async beforeEnter (to, from, next) {
            store.dispatch('setInitialLoading', { isInitialLoading: true })
            await store.dispatch('changeCurrentUser', { username: store.state.profile.username })
            await store.dispatch('fetchCards')
            store.dispatch('setInitialLoading', { isInitialLoading: false })
            next()
          }
        },
        {
          path: '/admin',
          name: 'AdminUserSearchPage',
          component: AdminUserSearchPage,
          async beforeEnter (to, from, next) {
            if (!store.state.profile.admin) {
              next('/unauthorized')
            }
            next()
          }
        },
        {
          path: '/admin/:username',
          name: 'AdminViewUserCardsPage',
          component: AdminViewUserCardsPage,
          async beforeEnter (to, from, next) {
            if (!store.state.profile.admin) {
              next('/unauthorized')
            }
            store.dispatch('setInitialLoading', { isInitialLoading: true })
            await store.dispatch('changeCurrentUser', { username: to.params.username.toLowerCase() })
            await store.dispatch('fetchCards')
            store.dispatch('setInitialLoading', { isInitialLoading: false })
            next()
          }
        },
        {
          path: '*',
          name: 'PageNotFound',
          component: PageNotFoundPage
        }
      ]
    }
  ],
  scrollBehavior (to, from, savedPosition) {
    return { x: 0, y: 0 }
  }
})

export default router
