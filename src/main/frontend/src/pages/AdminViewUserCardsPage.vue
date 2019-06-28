<template>
  <div
    v-if="profile && profile.admin"
    class="layout-admin-view-user-page">
    <h1 class="h2">{{ $t('title.admin.page') }} - {{ userInView }}</h1>
    <list-cards-page :is-admin-view="true" />
  </div>
  <UnauthorizedPage v-else />
</template>

<script>
import ListCardsPage from '@/pages/ListCardsPage'
import UnauthorizedPage from '@/pages/UnauthorizedPage'
import { mapState } from 'vuex'

export default {
  name: 'AdminViewUserCardsPage',
  components: {
    ListCardsPage,
    UnauthorizedPage
  },
  data () {
    return {
      userInView: ''
    }
  },
  computed: {
    ...mapState([
      'profile',
      'currentUser'
    ])
  },
  mounted () {
    this.userInView = this.currentUser.toLowerCase()
  }
}
</script>

<style lang="scss">
  .layout-admin-view-user-page {
    margin: 5rem 0 0;
    max-width: 71.25rem;
    display: flex;
    flex-flow: column nowrap;
    align-items: center;
    position: relative;
    justify-content: stretch;
    flex-grow: 1;
    overflow-x: hidden;
    & > h1 {
      margin-bottom: -2rem;
    }
  }
</style>
