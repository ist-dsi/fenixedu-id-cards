<template>
  <div
    v-if="profile && profile.admin"
    class="layout-admin-user-search-page"
  >
    <h1 class="h2">
      {{ $t('title.admin.page') }}
    </h1>
    <div class="user-search">
      <h2 class="h3">
        {{ $t('label.searchUser.admin') }}
      </h2>
      <div>
        <input
          v-model="username"
          :placeholder="$t('placeholder.searchUser')"
          @keyup.enter="goToUserPage"
        >
        <button
          class="btn btn--primary btn--outline"
          @click.prevent="goToUserPage"
        >
          {{ $t('btn.search') }}
        </button>
      </div>
    </div>
    <div class="session-container">
      <div
        v-if="!session"
        class="not-found-container"
      >
        <div>
          <img
            src="~@/assets/images/icon-error.svg"
            alt="Error icon"
          >
        </div>
        <h1 class="h3">
          {{ $t('label.info.session.admin') }}
        </h1>
      </div>
      <div v-else>
        <div class="session-info-container">
          <h2 class="h3">
            {{ $t('label.info.session.open.admin') }}
          </h2>
          <div>
            <p class="small">
              {{ $t('label.info.session.created.at.admin') }}: {{ session.createdAt }}
            </p>
            <p class="small">
              {{ $t('label.info.session.host.admin') }}: {{ session.ipAddress }}
            </p>
          </div>
        </div>
        <div
          v-if="!session.userMifare"
          class="loading-bar"
        >
          <div class="blue-bar" />
        </div>
        <div
          v-else
          class="session-user-info-container"
        >
          <div
            v-if="session.userIstId"
            class="session-user-container"
          >
            <h5 class="h5">
              {{ $t('label.user.data.admin') }}
            </h5>
            <div class="user-info-container">
              <div class="photo-container">
                <img
                  :src="userPhotoUrl"
                  alt="User Photo"
                >
              </div>
              <div>
                <p>Username: {{ session.userIstId }}</p>
                <p>Mifare: {{ session.userMifare }}</p>
              </div>
            </div>
            <div class="check-icon-container">
              <figure class="figure--56 figure--icon">
                <img
                  src="~@/assets/images/icon-check.svg"
                  alt="Check icon"
                >
              </figure>
              <p>{{ $t('label.info.card.delivered.admin') }}</p>
            </div>
          </div>
          <div
            v-else
            class="user-not-found-container"
          >
            <h5 class="h5">
              {{ $t('label.info.mifare.not.found.admin') }}
            </h5>
            <p>{{ $t('label.info.mifare.not.found.insert.data.admin') }}</p>
            <div class="user-not-found">
              <div class="field">
                <p>Mifare</p>
                <p>{{ session.userMifare }}</p>
              </div>
              <div class="field">
                <p>Username</p>
                <input
                  v-model="deliverUsername"
                  placeholder=""
                  @keyup.enter="submitUserMifare"
                >
              </div>
              <button
                class="btn btn--primary btn--outline"
                @click.prevent="submitUserMifare"
              >
                {{ $t('btn.submit') }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <unauthorized-page v-else />
</template>

<script>
import CardsAPI from '@/api/cards'
import UnauthorizedPage from '@/pages/UnauthorizedPage'
import { mapState } from 'vuex'

export default {
  name: 'AdminUserSearchPage',
  components: {
    UnauthorizedPage
  },
  data () {
    return {
      username: '',
      deliverUsername: '',
      getAdminSessionInterval: undefined,
      session: undefined
    }
  },
  computed: {
    ...mapState([
      'profile'
    ]),
    userPhotoUrl () {
      return this.session && `data:image/png;base64,${this.session.userPhoto}`
    }
  },
  created () {
    this.getAdminSession()
    this.getAdminSessionInterval = setInterval(() => this.getAdminSession(), 1000)
  },
  destroyed () {
    clearInterval(this.getAdminSessionInterval)
  },
  methods: {
    goToUserPage () {
      const lowerUsername = this.username.toLowerCase().trim()

      if (lowerUsername && lowerUsername !== this.profile.username) {
        this.$router.push({ name: 'AdminViewUserCardsPage', params: { username: lowerUsername } })
      } else {
        this.$router.push({ name: 'ListCardsPage' })
      }
    },
    async getAdminSession () {
      try {
        this.session = await CardsAPI.getAdminSession()
      } catch (err) {
        this.session = undefined
      }
    },
    async submitUserMifare () {
      try {
        await CardsAPI.submitUserMifare({ mifare: this.session.userMifare, istId: this.deliverUsername.trim() })
        this.deliverUsername = ''
      } catch (err) {
      }
    }
  }
}
</script>

<style lang="scss">
  .layout-admin-user-search-page {
    margin: 5rem 0 0;
    max-width: 71.25rem;
    overflow-x: hidden;

    & h1 {
      text-align: center;
    }
  }

  .user-search {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 1rem 0;

    & > div {
      display: flex;
    }

    & input {
      margin-right: 10px;
      padding-left: 10px;
    }
  }

  .not-found-container {
    margin-top: 4rem;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .session-container {
    margin-top: 2rem;

    .loading-bar {
      margin-top: 6rem;
    }

    .session-info-container {
      display: flex;
      flex-direction: column;
      align-items: center;

      h2 {
        margin-bottom: 0;
      }
    }

    .session-user-info-container {
      margin-top: 1rem;

      .session-user-container {
        display: flex;
        align-items: center;
        flex-direction: column;
        margin-top: 2rem;

        .user-info-container {
          display: flex;
          margin: 0.5rem 0;

          .photo-container {
            margin-right: 0.5rem;
            & img {
              width: 100px;
            }
          }
        }
      }

      .check-icon-container {
        display: flex;
        flex-direction: column;
        align-items: center;
      }

      .user-not-found-container {
        margin-top: 2rem;

        h5 {
          text-align: center;
          margin-bottom: 0;
        }

        & > p {
          text-align: center;
          margin-bottom: 2rem;
        }

        .user-not-found {
          display: flex;
          align-items: flex-end;
          justify-content: center;

          .field {
            display: flex;
            flex-direction: column;
            margin-right: 2rem;

            label {
              margin-bottom: 0.5rem;
            }
          }
        }
      }
    }
  }
</style>
