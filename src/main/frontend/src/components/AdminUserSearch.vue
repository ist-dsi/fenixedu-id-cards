<template>
  <div class="user-search">
    <input
      v-model="bindUsername"
      :placeholder="$t('placeholder.search.user')"
      @keyup.enter="goToUserPage" >
    <button
      class="btn btn--primary btn--outline"
      @click.prevent="goToUserPage">
      {{ $t('btn.search') }}
    </button>
  </div>
</template>

<script>
export default {
  name: 'AdminUserSearchPage',
  props: {
    username: {
      type: String,
      required: false,
      default: ''
    }
  },
  data () {
    return {
      bindUsername: ''
    }
  },
  mounted () {
    this.bindUsername = this.username
  },
  methods: {
    goToUserPage () {
      if (this.bindUsername) {
        this.$router.push({ name: 'AdminViewUserCardsPage', params: { username: this.bindUsername } })
      } else {
        this.$router.push({ name: 'ListCardsPage' })
      }
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
