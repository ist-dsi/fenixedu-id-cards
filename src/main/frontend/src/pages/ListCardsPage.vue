<template>
  <div
    v-if="profile && currentUser"
    class="layout-list-cards">
    <div
      v-if="cardsPage.cards.length > 0"
      class="layout-list-cards__slideshow">
      <div class="slideshow__btns">
        <button
          v-if="hasPrevious"
          @click.prevent="selectPreviousCard">
          <img
            src="~@/assets/images/arrow-left.svg"
            alt="Arrow left icon">
        </button>
      </div>
      <div class="slideshow__cards">
        <div class="slideshow__carousel-wrapper">
          <div
            v-touch:start="dragStart"
            v-touch:moving="dragAction"
            v-touch:end="dragEnd"
            v-if="isMobile"
            ref="cardsContainer"
            class="slideshow__cards-wrapper">
            <template v-if="selectedCard">
              <template v-for="card in availableCards">
                <id-card
                  :key="card.cardId"
                  :card-info="card"
                  :cardtype="'idtecnico'"
                  class="slideshow__card"
                />
              </template>
            </template>
          </div>
          <div
            v-else
            ref="cardsContainer"
            class="slideshow__cards-wrapper">
            <template v-if="selectedCard">
              <id-card
                :card-info="selectedCard"
                :cardtype="'idtecnico'"
                class="slideshow__card"
              />
            </template>
          </div>
        </div>
        <ol class="slideshow__carousel-status">
          <li
            v-for="card in availableCards"
            :key="card.cardId"
            :class="{ 'slideshow__indicator--active': card.cardId === selectedCard.cardId }"
            class="slideshow__indicator"/>
        </ol>
      </div>
      <div class="slideshow__btns">
        <button
          v-if="hasNext"
          @click.prevent="selectNextCard">
          <img
            src="~@/assets/images/arrow-right.svg"
            alt="Arrow right icon">
        </button>
      </div>
    </div>
    <div
      v-else
      class="layout-list-cards__slideshow">
      <template>
        <id-card>
          <template slot="empty-state-message">
            <h1 class="h5 h5--ssp">No card yet</h1>
            <p
              v-if="!isAdminView"
              class="">Looks like you don't have any Técnico Lisboa card.</p>
            <p
              v-else
              class="">Looks like this user does not have any Técnico Lisboa card.</p>
          </template>
        </id-card>
      </template>
    </div>
    <div
      v-if="selectedCard"
      class="layout-list-cards__actions">
      <button
        v-if="profile.canRequestCard && !isAdminView"
        class="btn btn--primary btn--outline"
        @click.prevent="openRequestNewCardWithReasonModal">
        Request new
      </button>
      <button
        v-else-if="!isSelectedCardDelivered && isAdminView"
        class="btn btn--primary btn--outline"
        @click.prevent="openConfirmDeliverCardModal">
        Deliver card
      </button>
      <button
        class="p--default timeline__toggle"
        @click.prevent="toggleTimeline">
        View card history
      </button>
    </div>
    <div
      v-else-if="!selectedCard && !isAdminView"
      class="layout-list-cards__actions">
      <button
        class="btn btn--primary"
        @click.prevent="openRequestNewCardModal">
        Request new
      </button>
    </div>
    <div class="layout-list-cards__timeline">
      <template v-if="selectedCard && showTimeline">
        <div class="timeline">
          <ol class="timeline__list">
            <template v-for="(transition, index) in stateTransitions">
              <li
                :key="transition"
                class="timeline__item"><!-- Aria phrase -->
                <i
                  :class="{'timeline__item-status--filled' : isTransitionComplete(index)}"
                  class="timeline__item-status"/>
                <div class="timeline__item-text">
                  <h2 class="h5--ssp timeline__item-title">
                    {{ stateMessages[transition] }}
                    <img
                      v-if="transition === cardStates.READY_FOR_PICKUP && isTransitionComplete(index)"
                      src="~@/assets/images/icon-info.svg"
                      class="icon timeline__item-icon"
                      @click.prevent="readyForPickupModal = true" >
                  </h2>
                  <time
                    v-if="getStateTransitionDate(transition) && isTransitionComplete(index)"
                    :datetime="getTransitionDateTime(transition)"
                    class="timeline__item-time p--default">{{ getStateTransitionDate(transition) }}</time>
                  <p
                    v-else-if="!getStateTransitionDate(transition) && isTransitionComplete(index)"
                    class="timeline__item-time p--default">N/A</p>
                </div>
              </li>
            </template>
          </ol>
        </div>
      </template>
    </div>
    <div class="layout-list-cards__other-features">
      <ul class="list-features">
        <li class="list-features__item">
          <a
            :href="cardFeaturesUrl"
            class="card card--no-shadow">
            <div class="card-row">
              <div class="card-row__figure list-features__figure">
                <figure class="figure figure--icon list-features__icons">
                  <img
                    src="~@/assets/images/icon-identity.svg"
                    alt="Identity icon">
                </figure>
              </div>
              <div class="card-row__text">
                <h1 class="card-row__title h4--ssp">
                  Card features
                </h1>
                <p>Get to know the power you have in your hands</p>
              </div>
              <div class="card-row__meta">
                <figure class="figure figure--icon list-features__meta-icons">
                  <img
                    src="~@/assets/images/arrow-right.svg"
                    alt="Arrow right icon">
                </figure>
              </div>
            </div>
          </a>
        </li>
        <li class="list-features__item">
          <a
            :href="discountsAndPromotionsUrl"
            class="card card--no-shadow">
            <div class="card-row">
              <div class="card-row__figure list-features__figure">
                <figure class="figure figure--icon list-features__icons">
                  <img
                    src="~@/assets/images/icon-ticket.svg"
                    alt="Ticket icon">
                </figure>
              </div>
              <div class="card-row__text">
                <h1 class="card-row__title h4--ssp">
                  Discounts and promotions
                </h1>
                <p>Provides access to protocols and agreements with a broad set of partners</p>
              </div>
              <div class="card-row__meta">
                <figure class="figure figure--icon list-features__meta-icons">
                  <img
                    src="~@/assets/images/arrow-right.svg"
                    alt="Arrow right icon">
                </figure>
              </div>
            </div>
          </a>
        </li>
      </ul>
    </div>

    <modal
      v-scroll-lock="requestNewCardModal"
      :withfooter="true"
      v-model="requestNewCardModal">
      <template slot="modal-panel">
        <template v-if="!hasPendingRequest">
          <h1 class="h2">Is everything right?</h1>
          <p>This data is obtained from Fenix.</p>
          <id-card
            v-if="cardPreview"
            :key="cardPreview.cardId"
            :card-info="cardPreview"
            :cardtype="'idtecnico'"
            :is-preview="true"
          />
          <p>Please confirm if it is updated so your card has the accurate info about you.</p>
        </template>
        <loading v-if="hasPendingRequest" />
      </template>
      <template
        v-if="!hasPendingRequest"
        slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <button
            class="btn btn--slate btn--outline"
            @click.prevent="openEditInfoModal">
            Edit
          </button>
          <button
            class="btn btn--primary"
            @click.prevent="confirmRequestNewCard">
            Confirm
          </button>
        </div>
      </template>
    </modal>
    <modal
      v-scroll-lock="successModal"
      v-model="successModal">
      <template slot="modal-panel">
        <figure class="figure figure--icon modal-panel__icons">
          <img
            src="~@/assets/images/icon-check.svg"
            alt="Check icon">
        </figure>
        <h1 class="h2">Card requested</h1>
        <p>Your card request was successfull, you'll be notified for its pickup.</p>
      </template>
    </modal>
    <modal
      v-scroll-lock="editInfoModal"
      :withfooter="true"
      v-model="editInfoModal">
      <template slot="modal-panel">
        <figure class="figure figure--icon modal-panel__icons">
          <img
            src="~@/assets/images/icon-warning.svg"
            alt="Warning icon">
        </figure>
        <h1 class="h2">Edit your info</h1>
        <p>
          Your personal data is managed in Fenix.
          Please click
          <span
            class="layout-list-cards__modal-paragraph-link"
            @click.prevent="editData">
            "<u>edit</u>"
          </span>
          to open a new window where you'll change your info.
          When you're done please come back to this screen and select
          <span v-if="!editClicked">
            "<u>next</u>".
          </span>
          <span
            v-else
            class="layout-list-cards__modal-paragraph-link"
            @click.prevent="openConfirmDataModal">
            "<u>next</u>".
          </span>
        </p>
      </template>
      <template slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <button
            class="btn btn--slate btn--outline"
            @click.prevent="editInfoModal = false">
            Cancel
          </button>
          <button
            v-if="!editClicked"
            class="btn btn--primary"
            @click.prevent="editData">
            Edit
          </button>
          <button
            v-else
            class="btn btn--primary"
            @click.prevent="openConfirmDataModal">
            Next
          </button>
        </div>
      </template>
    </modal>
    <modal
      v-scroll-lock="confirmDataModal"
      :withfooter="true"
      v-model="confirmDataModal">
      <template slot="modal-panel">
        <template v-if="!hasPendingRequest">
          <h1 class="h2">Your next card</h1>
          <p>This data is obtained from Fenix.<br>Please confirm if it is updated so your card has the accurate info about you.</p>
          <id-card
            v-if="cardPreview"
            :key="cardPreview.cardId"
            :card-info="cardPreview"
            :cardtype="'idtecnico'"
            :is-preview="true"
          />
        </template>
        <loading v-if="hasPendingRequest" />
      </template>
      <template
        v-if="!hasPendingRequest"
        slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <button
            class="btn btn--slate btn--outline"
            @click.prevent="openEditInfoModal">
            Edit
          </button>
          <button
            class="btn btn--primary"
            @click.prevent="confirmRequestNewCard">
            Confirm
          </button>
        </div>
      </template>
    </modal>
    <modal
      v-scroll-lock="requestNewCardWithReasonModal"
      :withfooter="true"
      v-model="requestNewCardWithReasonModal">
      <template slot="modal-panel">
        <h1 class="h2">Why do you need a new card?</h1>
        <form action="">
          <div
            v-for="reason in requestReasons"
            :key="reason"
            class="f-field f-field--radio">
            <input
              :id="reason.toLowerCase().split(' ')[0]"
              :value="reason.toLowerCase()"
              type="radio"
              name="cardRequestReason"
              class="f-field__radio"
              @click="changeCurrentRequestReason(reason)">
            <label
              :for="reason.toLowerCase().split(' ')[0]"
              class="f-field__label f-field__label--radio">{{ `${reason.charAt(0)}${reason.slice(1).toLowerCase()}` }}</label>
          </div>
          <div
            v-if="openOtherReasonInput"
            class="f-field">
            <input
              id="cardRequestReason-other-text"
              v-model="otherRequestReasonText"
              type="text"
              class="f-field__input">
          </div>
        </form>
      </template>
      <template slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <button
            class="btn btn--slate btn--outline"
            @click.prevent="closeRequestNewCardWithReasonModal">
            Cancel
          </button>
          <button
            :disabled="isConfirmRequestWithReasonDisabled"
            :class="{ 'btn--disabled': isConfirmRequestWithReasonDisabled }"
            class="btn btn--primary"
            @click.prevent="confirmRequestNewCardWithReason">
            Confirm
          </button>
        </div>
      </template>
    </modal>
    <modal
      v-scroll-lock="cardResponsabilitiesModal"
      :withfooter="true"
      v-model="cardResponsabilitiesModal">
      <template slot="modal-panel">
        <figure class="figure figure--icon modal-panel__icons">
          <img
            src="~@/assets/images/icon-warning.svg"
            alt="Warning icon">
        </figure>
        <h1 class="h2">With a great card,<br>great responsabilities</h1>
        <p>If you have access to secure doors, please report the accident to the campus security.</p>
      </template>
      <template slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <a
            :href="`tel: ${securityPhoneNumber}`"
            target="_blank"
            class="btn btn--slate btn--outline">
            Call
          </a>
          <button
            class="btn btn--primary"
            @click.prevent="confirmResponsabilities">
            Next
          </button>
        </div>
      </template>
    </modal>
    <modal
      v-scroll-lock="readyForPickupModal"
      :withfooter="true"
      v-model="readyForPickupModal">
      <template slot="modal-panel">
        <figure class="figure figure--icon modal-panel__icons">
          <img
            src="~@/assets/images/icon-check.svg"
            alt="Check icon">
        </figure>
        <h1 class="h2">Ready for pickup</h1>
        <p>Your card is ready to be delivered to you at Técnico Santander agency (Central Pavillion).</p>
      </template>
      <template slot="modal-footer">
        <a
          :href="tecnicoSantanderMapsUrl"
          target="_blank"
          class="btn btn--primary btn--outline layout-list-cards__modal-footer">
          Get directions
        </a>
      </template>
    </modal>
    <modal
      v-scroll-lock="displayErrorModal"
      v-model="displayErrorModal"
      class="error-modal">
      <template slot="modal-panel">
        <figure class="figure figure--icon modal-panel__icons">
          <img
            src="~@/assets/images/icon-error.svg"
            alt="Error icon">
        </figure>
        <h1 class="h2">{{ currentError.title }}</h1>
        <p>{{ currentError.message }}</p>
      </template>
    </modal>
    <modal
      v-scroll-lock="confirmDeliverCardModal"
      :withfooter="true"
      v-model="confirmDeliverCardModal">
      <template slot="modal-panel">
        <figure class="figure figure--icon modal-panel__icons">
          <img
            src="~@/assets/images/icon-warning.svg"
            alt="Warning icon">
        </figure>
        <h1 class="h2">Confirm deliver card</h1>
        <p>This action cannot be reversed. Are you sure you want to deliver this card?</p>
      </template>
      <template slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <button
            class="btn btn--slate btn--outline"
            @click.prevent="closeConfirmDeliverCardModal">
            Cancel
          </button>
          <button
            class="btn btn--primary"
            @click.prevent="deliverSelectedCard">
            Confirm
          </button>
        </div>
      </template>
    </modal>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import Modal from '@/components/utils/Modal'
import IdCard from '@/components/IdCard'
import Loading from '@/components/Loading'
import * as cardStates from '@/utils/cards/CardStates'
import stateMessages from '@/utils/cards/CardStateMessages'
import * as requestReasons from '@/utils/reasons/RequestReasons'

export default {
  name: 'ListCardsPage',
  components: {
    IdCard,
    Modal,
    Loading
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
      showTimeline: true,
      validateDataModal: false,
      selectedCardIndex: 0,
      cardStates,
      requestReasons,
      stateTransitions: [
        cardStates.REQUESTED,
        cardStates.BANK_REQUEST,
        cardStates.IN_PRODUCTION,
        cardStates.READY_FOR_PICKUP,
        cardStates.DELIVERED
      ],
      stateMessages,
      requestNewCardModal: false,
      successModal: false,
      editInfoModal: false,
      editClicked: false,
      confirmDataModal: false,
      requestNewCardWithReasonModal: false,
      cardResponsabilitiesModal: false,
      readyForPickupModal: false,
      displayErrorModal: false,
      confirmDeliverCardModal: false,
      openOtherReasonInput: false,
      isConfirmRequestWithReasonDisabled: true,
      hasPendingRequest: false,
      currentRequestReason: undefined,
      otherRequestReasonText: '',
      changeDataUrl: 'https://fenix.tecnico.ulisboa.pt/personal',
      tecnicoSantanderMapsUrl: 'https://goo.gl/maps/dC4k68TZ9xuy6zAVA',
      securityPhoneNumber: '+351218419162',
      cardFeaturesUrl: 'https://tecnico.ulisboa.pt/pt/viver/servicos/cartao-de-identificacao/',
      discountsAndPromotionsUrl: 'https://drh.tecnico.ulisboa.pt/protocolos-e-acordos/',
      swipePosX1: 0,
      swipePosX2: 0,
      swipePosInitial: undefined,
      cardWidth: 19.875,
      cardMargin: 0.5,
      cardPadding: 2,
      isMobile: false,
      mobileMenuBreakpoint: 768,
      windowWidth: 0,
      currentError: {
        title: '',
        message: ''
      }
    }
  },
  computed: {
    ...mapState([
      'cardsPage',
      'cardPreview',
      'profile',
      'currentUser'
    ]),
    selectedCard () {
      const { cards } = this.cardsPage
      return cards && cards.length > 0 ? cards[this.selectedCardIndex] : undefined
    },
    previousCard () {
      const { cards } = this.cardsPage

      if (cards && this.selectedCardIndex < cards.length - 1) {
        return cards[this.selectedCardIndex + 1]
      }

      return false
    },
    nextCard () {
      if (this.selectedCardIndex > 0) {
        return this.cardsPage.cards[this.selectedCardIndex - 1]
      }
      return false
    },
    isSelectedCardRequested () {
      return !this.isTransitionComplete(this.stateTransitions.indexOf(this.cardStates.READY_FOR_PICKUP))
    },
    availableCards () {
      return this.cardsPage.cards.slice().reverse()
    },
    filteredHistory () {
      const { history } = this.selectedCard

      history.forEach(transition => {
        if (transition.state === this.cardStates.PENDING || transition.state === this.cardStates.IGNORED) {
          transition.state = this.cardStates.REQUESTED
        }
      })

      return history.filter(t => t.state !== this.cardStates.EXPIRED)
    },
    hasPrevious () {
      const { cards } = this.cardsPage
      return cards && this.selectedCardIndex < cards.length - 1
    },
    hasNext () {
      return this.availableCards.length > 1 && this.selectedCardIndex > 0
    },
    isSelectedCardDelivered () {
      const { history } = this.selectedCard

      return history.findIndex(t => t.state === this.cardStates.DELIVERED) !== -1
    }
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
    ...mapActions([
      'fetchPreview',
      'requestNewCard',
      'fetchCards',
      'deliverCard',
      'fetchProfile'
    ]),
    getWindowWidth () {
      this.windowWidth = window.innerWidth
    },
    selectPreviousCard () {
      if (this.previousCard) {
        this.shiftCards(1)
      }
    },
    selectNextCard () {
      if (this.nextCard) {
        this.shiftCards(-1)
      }

      if (this.isSelectedCardRequested) {
        this.showTimeline = true
      }
    },
    toggleTimeline () {
      this.showTimeline = !this.showTimeline
    },
    isTransitionComplete (transitionIndex) {
      const history = this.filteredHistory
      const lastState = history[history.length - 1].state

      return transitionIndex <= this.stateTransitions.indexOf(lastState)
    },
    getStateTransitionDate (transitionState) {
      const history = this.filteredHistory
      const transition = history.find(t => t.state === transitionState)
      return transition ? transition.when : undefined
    },
    getTransitionDateTime (transitionState) {
      const date = this.getStateTransitionDate(transitionState)
      return date ? date.split('/').reverse().join('-') : undefined
    },
    async openRequestNewCardModal () {
      try {
        this.hasPendingRequest = true
        await this.fetchPreview()
        this.hasPendingRequest = false
        this.requestNewCardModal = true

        if (this.displayErrorModal) {
          this.displayErrorModal = false
          this.resetCurrentError()
        }
      } catch (err) {
        this.hasPendingRequest = false
        this.currentError = {
          title: 'Error while previewing card',
          message: err.response.data.error
        }
        this.displayErrorModal = true
      }
    },
    resetCurrentError () {
      this.currentError = { title: '', message: '' }
    },
    async confirmRequestNewCard () {
      try {
        this.hasPendingRequest = true
        await this.requestNewCard()
        this.hasPendingRequest = false
      } catch (err) {
        this.hasPendingRequest = false
        console.error(err)
      }

      await this.fetchCards()
      await this.fetchProfile()
      this.openSuccessModal()
    },
    openSuccessModal () {
      this.closeCardDataModals()
      this.successModal = true
    },
    openEditInfoModal () {
      this.closeCardDataModals()
      this.editClicked = false
      this.editInfoModal = true
    },
    editData () {
      window.open(this.changeDataUrl, '_blank')
      this.editClicked = true
    },
    async openConfirmDataModal () {
      this.hasPendingRequest = true
      await this.fetchPreview()
      this.hasPendingRequest = false
      this.editInfoModal = false
      this.confirmDataModal = true
      this.editClicked = false
    },
    closeCardDataModals () {
      this.requestNewCardModal = false
      this.confirmDataModal = false
    },
    openRequestNewCardWithReasonModal () {
      this.openOtherReasonInput = false
      this.currentRequestReason = undefined
      this.isConfirmRequestWithReasonDisabled = true
      this.otherRequestReasonText = ''
      this.requestNewCardWithReasonModal = true
    },
    closeRequestNewCardWithReasonModal () {
      this.requestNewCardWithReasonModal = false
    },
    async confirmRequestNewCardWithReason () {
      if (this.currentRequestReason) {
        switch (this.currentRequestReason) {
          case this.requestReasons.LOST:
          case this.requestReasons.STOLEN:
            this.cardResponsabilitiesModal = true
            break
          case this.requestReasons.OUTDATED:
            this.openEditInfoModal()
            break
          default:
            await this.openRequestNewCardModal()
        }

        this.closeRequestNewCardWithReasonModal()
      }
    },
    changeCurrentRequestReason (reason) {
      const isOther = reason === this.requestReasons.OTHER
      this.currentRequestReason = reason
      this.openOtherReasonInput = isOther
      this.isConfirmRequestWithReasonDisabled = isOther
    },
    async confirmResponsabilities () {
      await this.openRequestNewCardModal()
      this.cardResponsabilitiesModal = false
    },
    async deliverSelectedCard () {
      try {
        const id = this.selectedCard.cardId
        await this.deliverCard({ id })
        await this.fetchCards()
        this.closeConfirmDeliverCardModal()
      } catch (err) {
        console.error(err)
      }
    },
    openConfirmDeliverCardModal () {
      this.confirmDeliverCardModal = true
    },
    closeConfirmDeliverCardModal () {
      this.confirmDeliverCardModal = false
    },
    dragStart (event) {
      const transform = this.$refs.cardsContainer.style.transform
      const currentPosition = transform.replace(/[a-zA-Z()]/g, '')
      if (event.type === 'touchstart') {
        this.swipePosInitial = currentPosition
        this.swipePosX1 = event.touches[0].clientX
      }
    },
    dragAction (event) {
      const transform = this.$refs.cardsContainer.style.transform
      let currentPosition = transform.replace(/[a-zA-Z()]/g, '')
      if (event.type === 'touchmove') {
        this.swipePosX2 = this.swipePosX1 - event.touches[0].clientX
        this.swipePosX1 = event.touches[0].clientX
      }
      this.$refs.cardsContainer.style.transform = `translate(${currentPosition - this.swipePosX2}px)`
    },
    dragEnd (event) {
      const transform = this.$refs.cardsContainer.style.transform
      const finalPosition = transform.replace(/[a-zA-Z()]/g, '')
      if (finalPosition - this.swipePosInitial < -20 && this.nextCard) {
        this.selectNextCard()
      } else if (finalPosition - this.swipePosInitial > 20 && this.previousCard) {
        this.selectPreviousCard()
      } else {
        this.shiftCards(0)
      }
    },
    shiftCards (direction) {
      this.$refs.cardsContainer.style.transition = 'all 300ms ease-out'
      this.selectedCardIndex += direction

      this.$refs.cardsContainer.style.transform = `translate(${this.convertRemToPixels(this.cardPadding + this.cardMargin - (this.cardWidth + this.cardMargin * 2) * (this.availableCards.length - this.selectedCardIndex - 1))}px)`

      setTimeout(() => {
        this.finishTransition()
      }, 310)
    },
    finishTransition () {
      this.$refs.cardsContainer.style.transition = 'none'
    },
    convertRemToPixels (rem) {
      return rem * parseFloat(getComputedStyle(document.documentElement).fontSize)
    }
  }
}
</script>

<style lang="scss">
// import variables
@import "@/assets/scss/_variables.scss";

.main-header {
  z-index: 100;
}
.layout-list-cards{
  max-width: 71.25rem;
  display: flex;
  flex-flow: column nowrap;
  align-items: center;
  position: relative;
  justify-content: stretch;
  flex-grow: 1;
  overflow-x: hidden;
}
.layout-list-cards__slideshow{
  display: flex;
  flex-flow: row nowrap;
  align-items: center;
  margin: 2rem 0 0;
}
.slideshow__cards {
  position: relative;
  height: 13.75rem;
  max-width: 25.875rem;
  margin: 0 auto;
}
.slideshow__carousel-wrapper {
  position: relative;
  z-index: 1;
  height: 100%;
  overflow: hidden;
}
.slideshow__cards-wrapper {
  display: flex;
  width: 1000rem;
  position: relative;
}
.slideshow__card {
  margin: .5rem;
}
.slideshow__btns{
  display: none;
  z-index: 99;
  width: 40px;
}
.slideshow__carousel-status{
  display: inline;
  position: absolute;
  left: 50%;
  transform: translate(-50%, -50%);
  margin: 0 0;
}
.slideshow__indicator {
  display: inline-block;
  margin: 0 .25rem;
  width: .5rem;
  height: .5rem;
  border-radius: 50%;
  background-color: $gray-300;
}
.slideshow__indicator--active {
  background-color: $slate;
}
.layout-list-cards__actions{
  width: 100%;
  max-width: 19.875rem;
  display: flex;
  justify-content: space-between;
  padding: 2.5rem 0 1rem;
  align-items: center;
}
.layout-list-cards__timeline{
  width: 100%;
  max-width: 19.875rem;
}
.timeline__toggle:after {
  content:'';
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 15 9' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M.78699116 1.042324l5.65685425 5.65685424L12.10069967 1.042324' stroke='%238F95A1' stroke-width='1.5' fill='none' fill-rule='evenodd' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  width: 1em;
  height: 1em;
  margin: 0 0 0 .5em;
  display: inline-block;
  cursor: pointer;
  background-color: transparent;
  background-size: 100% 100%;
  background-position: 50% 50%;
  background-repeat: no-repeat;
  transform: translateY(.25rem);
}
  .timeline {
    position: relative;
    display: flex;
    margin: 2rem 0 0;
    //justify-content: center;
  }
  .timeline__item {
    position: relative;
    padding-bottom: 2rem;
    display: flex;
    flex-flow: row nowrap;
  }
  .timeline__item:not(:last-of-type):before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    top: .75rem;
    margin: 1rem;
    margin-top: 1rem;
    margin-bottom: 0;
    width: 2px;
    height: calc(100% - 1.75rem);
    background-color: $blue;
  }
  .timeline__item-status {
    position: absolute;
    top: 0;
    left: 0;
    display: flex;
    justify-content: flex-end;
    align-items: center;

    &:before{
      content: '';
      display: block;
      width: 1.75rem;
      height: 1.75rem;
      margin: 0 .25rem;
      border-radius: 50%;
      border: 2px solid $blue;
    }
  }
  .timeline__item-status--filled {
    &:before {
      background: $blue;
    }
    &:after {
      content: '';
      position: absolute;
      left: 0;
      display: block;
      width: 1.75rem;
      height: 1.75rem;
      margin: 0 .25rem;
      background-image: url("data:image/svg+xml;utf8,%3Csvg width='15' height='13' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cpath fill='none' d='M-5-6h24v24H-5z'/%3E%3Cpath stroke='%23FFF' stroke-width='3' stroke-linecap='round' stroke-linejoin='round' d='M2 7.25L5.753 11 13.5 1.5'/%3E%3C/g%3E%3C/svg%3E");
      background-repeat: no-repeat;
      background-size: 12px 10px;
      background-position: center;
    }
  }
  .timeline__item-text {
    margin-left: 3rem;
    min-height: 2.6875rem;
  }
  .timeline__item-title {
    margin: 0;
  }
  .timeline__item-icon {
    cursor: pointer;
    float: right;
    margin: .1rem .45rem;
  }
  .layout-list-cards__other-features{
    margin: 1rem auto 0;
  }
  .list-features{
    display: flex;
    flex-flow: column nowrap;
  }
  .list-features__item{
    flex-grow: 1;
    cursor: pointer;
    border-top: 1px solid $light-gray;
  }
  .list-features__figure{
    align-self: center;
  }
  .list-features__icons{
    min-width: 4.5rem;
  }
  .card--no-shadow {
    background: none;
    box-shadow: none;
    width: 100%;
  }
  .card-row__meta {
    display: none;
  }
  .modal__panel {
    text-align: center;
  }
  .modal-panel__icons {
    width: unset;
    height: unset;
  }
  .layout-list-cards__modal-paragraph-link{
    cursor: pointer;
  }
  .layout-list-cards__modal-footer{
    align-items: stretch;
    justify-content: center;
    max-width: 19rem;
    width: 100%;
    .btn{
    flex-grow: 1;
    }
  }

@media (min-width: 768px){
  .layout-list-cards__slideshow {
    margin: 5rem 0 2rem;
  }
  .slideshow__btns {
    display: block;
    margin: 0 4rem;
  }
  .card--no-shadow .card-row {
    padding: 3rem 1.5rem;
  }
  .card-row__title {
    margin-bottom: .375rem;
  }
  .card-row__meta {
    display: block;
  }
  .slideshow__cards {
    max-width: 32rem;
    height: 19rem;
  }
  .slideshow__cards-wrapper {
    display: unset;
    width: unset;
  }
  .slideshow__carousel-status{
    margin: 0.5rem 0;
  }
  .slideshow__card{
    margin: .5rem 1rem 2rem;
  }
  .layout-list-cards__other-features{
    margin: auto auto 0;
  }
  .layout-list-cards__actions {
    padding: 1rem 0;
  }
  .layout-list-cards__timeline, .layout-list-cards__actions{
  max-width: 30rem;
  }
  .list-features{
    display: flex;
    flex-flow: row nowrap;
  }
  .list-features__item{
    flex-basis: 50%;
    min-width: 50%;
    max-width: 50%;
  }
}
@media (min-width: 1200px){
}
</style>
