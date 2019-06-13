<template>
  <div v-if="profile && profile.isAdmin">
    {{ $route.params.username }}
    <list-cards-page />
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
  computed: {
    ...mapState([
      'profile'
    ])
  }
}
</script>
