<template>
  <div
    v-if="cardPreview && !isInitialLoading"
    class="layout-list-cards">
    <div class="title-container">
      <h1 class="h3--ssp">Your next card</h1>
      <p v-if="isMobile">Here's a preview of your new card with your information reviewed.</p>
    </div>
    <id-card
      :card-info="cardPreview"
      :cardtype="'idtecnico'"
      :is-preview="true"
    />
    <p v-if="!isMobile">Here's a preview of your new card with your information reviewed.</p>
    <div class="button-container">
      <button
        class="btn btn--light"
        @click.prevent="() => console.log('ola')">
        {{ $t('btn.edit') }}
      </button>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import IdCard from '@/components/IdCard'

export default {
  name: 'CardPreviewPage',
  components: {
    IdCard
  },
  props: {
    isAdminView: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data () {
    return {
      mobileMenuBreakpoint: 768,
      isMobile: false,
      windowWidth: 0
    }
  },
  computed: {
    ...mapState([
      'cardPreview',
      'isInitialLoading'
    ])
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
  },
  beforeDestroy () {
    window.removeEventListener('resize', this.getWindowWidth)
  },
  methods: {
    getWindowWidth () {
      this.windowWidth = window.innerWidth
    }
  }
}
</script>

<style lang="scss">
// import variables
@import "@/assets/scss/_variables.scss";

.layout-list-cards {
  max-width: 71.25rem;
  display: flex;
  flex-flow: column nowrap;
  align-items: center;
  position: relative;
  justify-content: stretch;
  flex-grow: 1;
  overflow-x: hidden;
  margin: 5rem 0 0;
  text-align: center;

  & > p {
    max-width: 16rem;
    margin-top: 2rem;
  }
}

.title-container {
  max-width: 17rem;
  margin-bottom: 1rem;

  @media (min-width: 768px) {
    margin-bottom: 2rem;
  }
}

.button-container {
  width: 100%;
  box-shadow: 0 -0.0625rem 0 0 rgba($dark, 0.1);
  margin-top: auto;
  padding: 1rem 1rem;

  & .btn {
    width: 100%;
  }
}

</style>
