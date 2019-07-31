<template>
  <div
    v-if="cardPreview && !isInitialLoading"
    class="layout-list-cards layout-list-cards-form">
    <h1 class="h2">{{ $t('modal.title.confirmData') }}</h1>
    <p
      v-if="isMobile"
      class="p--default">
      {{ $t('text.review.parts.first.page') }} <br>{{ $t('text.review.parts.second.page') }}
    </p>
    <id-card
      :card-info="cardPreview"
      :cardtype="'idtecnico'"
      :is-preview="true"/>
    <p
      v-if="!isMobile"
      class="p--default">
      {{ $t('text.review.parts.first.page') }} <br>{{ $t('text.review.parts.second.page') }}
    </p>
    <div
      class="layout-list-cards__actions btn--group">
      <button
        class="btn btn--light"
        @click.prevent="openEditModal">
        {{ $t('btn.edit') }}
      </button>
      <button
        v-if="$route.query.redirect"
        class="btn btn--primary"
        @click.prevent="redirect">
        {{ $t('btn.finish') }}
      </button>
    </div>
    <edit-info
      :open="editModal"
      @close="closeEditModal"/>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import IdCard from '@/components/IdCard'
import EditInfo from '@/components/EditInfo'

export default {
  name: 'CardPreviewPage',
  components: {
    IdCard,
    EditInfo
  },
  data () {
    return {
      mobileMenuBreakpoint: 768,
      isMobile: false,
      windowWidth: 0,
      editModal: false,
      redirectUrl: 'https://google.com'
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
    },
    openEditModal () {
      this.editModal = true
    },
    closeEditModal () {
      this.editModal = false
    },
    redirect () {
      if (this.$route.query.redirect) {
        window.location.replace(this.redirectUrl)
      }
    }
  }
}
</script>

<style lang="scss">
// import variables
@import "@/assets/scss/_variables.scss";

.layout-list-cards.layout-list-cards-form {

  > .h2 {
    margin: 15vh auto 1rem;
  }

  .p--default {
    text-align: center;
  }

  .id-card {
    margin: 1rem auto 2rem;
  }

  .layout-list-cards__actions {
    max-width: 100%;
    margin-top: auto;
    padding: 1rem 1rem;

    .btn {
      width: 100%;
    }

    @media (min-width: 768px) {
      max-width: 19rem;
    }

    @media (max-width: 768px) {
      box-shadow: 0 -.0625rem 0 0 rgba($dark, 0.1);
    }
  }
}

</style>
