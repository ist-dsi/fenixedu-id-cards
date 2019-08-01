<template>
  <div
    v-if="cardPreview && !isInitialLoading"
    class="page-container">
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
        @click.prevent="openEditModal"
      >
        {{ $t('btn.edit') }}
      </button>
    </div>
    <modal
      v-scroll-lock="editModal"
      :withfooter="true"
      v-model="editModal">
      <template slot="modal-panel">
        <edit-info />
      </template>
      <template
        slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <button
            class="btn btn--light"
            @click.prevent="">
            {{ $t('btn.cancel') }}
          </button>
          <button
            class="btn btn--primary"
            @click.prevent="">
            {{ $t('btn.confirm') }}
          </button>
        </div>
      </template>
    </modal>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import IdCard from '@/components/IdCard'
import Modal from '@/components/utils/Modal'
import EditInfo from '@/components/EditInfo'

export default {
  name: 'CardPreviewPage',
  components: {
    IdCard,
    Modal,
    EditInfo
  },
  data () {
    return {
      mobileMenuBreakpoint: 768,
      isMobile: false,
      windowWidth: 0,
      editModal: false
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
    }
  }
}
</script>

<style lang="scss">
// import variables
@import "@/assets/scss/_variables.scss";

.page-container {
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
  margin-top: auto;
  padding: 1rem 1rem;
  display: flex;
  justify-content: center;

  @media (min-width: 768px) {
    max-width: 16rem;
  }

  @media (max-width: 768px) {
    box-shadow: 0 -0.0625rem 0 0 rgba($dark, 0.1);
  }

  & .btn {
    width: 100%;
  }
}

</style>
