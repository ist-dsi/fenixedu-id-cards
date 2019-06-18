<template>
  <div
    v-if="profile && profile.admin"
    class="user-search">
    <input
      v-model="username"
      :placeholder="$t('placeholder.search.user')"
      @keyup.enter="goToUserPage" >
    <button
      class="btn btn--primary btn--outline"
      @click.prevent="goToUserPage">
      {{ $t('btn.search') }}
    </button>
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
      this.$router.push({ name: 'AdminViewUserCardsPage', params: { username: this.username } })
    }
  }
}
</script>

<style lang="scss">
  .user-search {
    display: flex;
    & > input {
      margin-right: 10px;
    }
  }
</style>
