<template>
  <modal
    v-scroll-lock="open"
    :withfooter="true"
    :value="open"
    class="modal--lg"
    @input="$emit('close')">
    <template slot="modal-panel">
      <div>
        <loading v-if="hasPendingRequest" />
        <div
          v-if="!hasPendingRequest"
          class="form">
          <h1 class="h2">Is everything right?</h1>
          <div class="f-group">
            <p>Photo</p>
            <p class="small f-field--readonly">Your photo is managed in your <a
              class="u-active-link"
              href="https://fenix.tecnico.ulisboa.pt/">Fenix personal area</a>. In case you want
              to <a
                class="u-active-link"
                href="https://fenix.tecnico.ulisboa.pt/">change it</a>, please mind that it will be pending approval to figure on your card.
            </p>
          </div>
          <div class="f-group">
            <div>
              <p>Name</p>
              <a
                class="u-active-link small"
                @click.prevent="resetNames">Reset</a>
            </div>
            <tag-input
              :tags="userNamesList"
              class="f-field--danger"
              @remove-tag="removeUserName"/>
            <p
              :class="{ danger: selectedFamilyNames < 1 || selectedGivenNames < 1}"
              class="small f-field__validation">If you want to shorten your displayed name,
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
      const givenNamesList = this.userNames.givenNames.split(' ')
      const familyNamesList = this.userNames.familyNames.split(' ')
      this.userNamesList = [...givenNamesList.map(n => ({ label: n, isGivenName: true })),
        ...familyNamesList.map(n => ({ label: n, isGivenName: false }))]
      this.selectedGivenNames = givenNamesList.length
      this.selectedFamilyNames = familyNamesList.length
    }
  }
}
</script>

<style lang="scss">
// import variables
@import "@/assets/scss/_variables.scss";

.layout-list-cards.layout-list-cards-form .f-group{
  text-align: left;

  .f-field--readonly {
    margin: 1rem 0;
  }

  &:last-child {
    p:first-child {
      float: left;
    }
    .u-active-link {
      float: right;
      margin-top: 5px;
      font-weight: 400;
    }
  }

  .f-tag-field {
    margin-top: 2.5rem;
  }

  .f-field--danger {
    border-color: $magenta;
    + .f-field__validation{
      color: $magenta;
    }
  }
}

</style>
