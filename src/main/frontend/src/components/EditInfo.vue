<template>
  <div>
    <loading v-if="hasPendingRequest" />
    <div
      v-if="!hasPendingRequest"
      class="container">
      <h1 class="h3--ssp">Is everything right?</h1>
      <div class="section-container">
        <p class="p-default">Photo</p>
        <p class="small">Your photo is managed in Fenix. In case you want
        to change it, please mind that it will be pending approval to figure on your card.
        </p>
      </div>
      <div class="section-container">
        <p class="p-default">Name</p>
        <tag-input
          :tags="userNamesList"
          @remove-tag="removeUserName" />
      </div>
    </div>
  </div>
</template>

<script>
import CardsAPI from '@/api/cards'
import Loading from '@/components/Loading'
import TagInput from '@/components/utils/TagInput'

export default {
  name: 'CardPreviewPage',
  components: {
    TagInput,
    Loading
  },
  data () {
    return {
      mobileMenuBreakpoint: 768,
      isMobile: false,
      windowWidth: 0,
      hasPendingRequest: false,
      userNamesList: [],
      userNames: {}
    }
  },
  computed: {
  },
  watch: {
    windowWidth: {
      immediate: true,
      handler (newWidth, oldWidth) {
        if (newWidth < this.mobileMenuBreakpoint) {
          this.isMobile = true
        } else {
          this.isMobile = false
        }
      }
    }
  },
  mounted () {
    this.$nextTick(function () {
      window.addEventListener('resize', this.getWindowWidth)
      this.getWindowWidth()
    })
    this.fetchUserNames()
  },
  beforeDestroy () {
    window.removeEventListener('resize', this.getWindowWidth)
  },
  methods: {
    getWindowWidth () {
      this.windowWidth = window.innerWidth
    },
    async fetchUserNames () {
      this.hasPendingRequest = true
      const response = await CardsAPI.getUserNames()
      this.userNames = response
      this.userNamesList = [...response.givenNames, ...response.familyNames]
      this.hasPendingRequest = false
    },
    removeUserName (i) {
      this.userNamesList.splice(i, 1)
    }
  }
}
</script>

<style lang="scss">
// import variables

.container {
  text-align: left;

  & h1 {
    text-align: center;
  }
}

.section-container {
  margin: 3rem 0;

  & p:first-child {
    margin-bottom: 1rem;
  }
}

</style>
