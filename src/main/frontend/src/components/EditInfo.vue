<template>
  <modal
    v-scroll-lock="open"
    :withfooter="true"
    :value="open"
    @input="$emit('close')">
    <template slot="modal-panel">
      <div>
        <loading v-if="hasPendingRequest" />
        <div
          v-if="!hasPendingRequest"
          class="container">
          <h1 class="h2">Is everything right?</h1>
          <div class="section-container">
            <p class="p-default">Photo</p>
            <p class="small">Your photo is managed in Fenix. In case you want
            to change it, please mind that it will be pending approval to figure on your card.
            </p>
          </div>
          <div class="section-container">
            <div>
              <p class="p-default">Name</p>
              <a @click.prevent="resetNames">Reset</a>
            </div>
            <tag-input
              :tags="userNamesList"
              @remove-tag="removeUserName" />
            <p
              :class="{ danger: selectedFamilyNames < 1 || selectedGivenNames < 1}"
              class="small">If you want to shorten your displayed name,
              choose at least one of your first and last names.
            </p>
          </div>
        </div>
      </div>
    </template>
    <template
      slot="modal-footer">
      <div class="btn--group layout-list-cards__modal-footer">
        <button
          class="btn btn--light"
          @click.prevent="$emit('close')">
          {{ $t('btn.cancel') }}
        </button>
        <button
          :class="{ 'btn--disabled': selectedFamilyNames < 1 || selectedGivenNames < 1}"
          class="btn btn--primary"
          @click.prevent="">
          {{ $t('btn.confirm') }}
        </button>
      </div>
    </template>
  </modal>
</template>

<script>
import CardsAPI from '@/api/cards'
import Loading from '@/components/Loading'
import Modal from '@/components/utils/Modal'
import TagInput from '@/components/utils/TagInput'

export default {
  name: 'EditInfo',
  components: {
    TagInput,
    Loading,
    Modal
  },
  props: {
    open: {
      type: Boolean,
      required: true
    }
  },
  data () {
    return {
      mobileMenuBreakpoint: 768,
      isMobile: false,
      windowWidth: 0,
      hasPendingRequest: false,
      userNamesList: [],
      userNames: {},
      selectedGivenNames: 0,
      selectedFamilyNames: 0
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
      this.resetNames()
      this.hasPendingRequest = false
    },
    removeUserName (item, index) {
      if (item.isGivenName) {
        this.userNamesList.splice(index, 1)
        this.selectedGivenNames--
      }

      if (!item.isGivenName) {
        this.userNamesList.splice(index, 1)
        this.selectedFamilyNames--
      }
    },
    resetNames () {
      this.userNamesList = [...this.userNames.givenNames.map(n => ({ label: n, isGivenName: true })),
        ...this.userNames.familyNames.map(n => ({ label: n, isGivenName: false }))]
      this.selectedGivenNames = this.userNames.givenNames.length
      this.selectedFamilyNames = this.userNames.familyNames.length
    }
  }
}
</script>

<style lang="scss">
// import variables
@import "@/assets/scss/_variables.scss";

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

.danger {
  color: $magenta;
}

</style>
