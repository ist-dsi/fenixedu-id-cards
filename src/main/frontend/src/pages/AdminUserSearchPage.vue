<template>
  <div
    v-if="profile && profile.admin"
    class="layout-admin-user-search-page">
    <h1 class="h2">{{ $t('title.admin.page') }}</h1>
    <div class="user-search">
      <input
        v-model="username"
        :placeholder="$t('placeholder.searchUser')"
        @keyup.enter="goToUserPage" >
      <button
        class="btn btn--primary btn--outline"
        @click.prevent="goToUserPage">
        {{ $t('btn.search') }}
      </button>
    </div>
  </div>
  <UnauthorizedPage v-else />
</template>

<script>
import UnauthorizedPage from '@/pages/UnauthorizedPage'
import { mapState } from 'vuex'

export default {
  name: 'AdminUserSearchPage',
  components: {
    UnauthorizedPage
  },
  data () {
    return {
      username: ''
    }
  },
  computed: {
    ...mapState([
      'profile'
    ])
  },
  methods: {
    goToUserPage () {
      const lowerUsername = this.username.toLowerCase()

      if (lowerUsername && lowerUsername !== this.profile.username) {
        this.$router.push({ name: 'AdminViewUserCardsPage', params: { username: lowerUsername } })
      } else {
        this.$router.push({ name: 'ListCardsPage' })
      }
    }
  }
}
</script>

<style lang="scss">
  .layout-admin-user-search-page {
    margin: 5rem 0 0;
    max-width: 71.25rem;
    display: flex;
    flex-flow: column nowrap;
    align-items: center;
    position: relative;
    justify-content: stretch;
    flex-grow: 1;
    overflow-x: hidden;
  }

  .user-search {
    display: flex;
    & > input {
      margin-right: 10px;
      padding-left: 10px;
    }
  }
</style>
