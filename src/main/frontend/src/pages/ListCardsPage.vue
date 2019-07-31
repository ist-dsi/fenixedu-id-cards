<template>
  <div
    v-if="profile && currentUser && !isInitialLoading"
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
            :style="{ transform: `translate(${convertRemToPixels(cardPadding + cardMargin - (cardWidth + cardMargin * 2) * (cardsPage.cards.length - 1))}px)` }"
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
        <ol
          v-if="availableCards.length > 1"
          class="slideshow__carousel-status">
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
            <h1 class="h5 h5--ssp">{{ $t('label.card.emptyState.title') }}</h1>
            <p
              v-if="!isAdminView"
              class="">{{ $t('label.card.emptyState.message') }}</p>
            <p
              v-else
              class="">{{ $t('label.card.emptyState.message.admin') }}</p>
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
        @click.prevent="hasAllCardsExpired ? openRequestNewCardModal() : openRequestNewCardWithReasonModal()">
        {{ $t('btn.card.requestNew') }}
      </button>
      <button
        v-else-if="!isSelectedCardDelivered && isAdminView"
        class="btn btn--primary btn--outline"
        @click.prevent="openConfirmDeliverCardModal">
        {{ $t('btn.card.deliver') }}
      </button>
      <button
        class="p--default timeline__toggle"
        @click.prevent="toggleTimeline">
        {{ $t('btn.card.history') }}
      </button>
    </div>
    <div
      v-else-if="!selectedCard && !isAdminView"
      class="layout-list-cards__actions">
      <button
        class="btn btn--primary"
        @click.prevent="openRequestNewCardModal">
        {{ $t('btn.card.requestNew') }}
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
                    {{ $t(`message.cardStates.${stateTransitionLabels[transition]}`) }}
                    <img
                      v-if="transition === cardStates.READY_FOR_PICKUP && isTransitionComplete(index)
                      && !isTransitionComplete(stateTransitions.indexOf(cardStates.DELIVERED)) && selectedCard.currentState !== cardStates.EXPIRED"
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
                  {{ $t('label.featuredContent.features.title') }}
                </h1>
                <p>{{ $t('label.featuredContent.features.message') }}</p>
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
                  {{ $t('label.featuredContent.discounts.title') }}
                </h1>
                <p>{{ $t('label.featuredContent.discounts.message') }}</p>
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
          <h1 class="h2">{{ $t('modal.title.requestNew') }}</h1>
          <p>{{ $t('modal.message.first.requestNew') }}</p>
          <id-card
            v-if="cardPreview"
            :key="cardPreview.cardId"
            :card-info="cardPreview"
            :cardtype="'idtecnico'"
            :is-preview="true"
          />
          <p>{{ $t('modal.message.second.requestNew') }}</p>
        </template>
        <loading v-if="hasPendingRequest" />
      </template>
      <template
        v-if="!hasPendingRequest"
        slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <button
            class="btn btn--light"
            @click.prevent="openEditInfoModal">
            {{ $t('btn.edit') }}
          </button>
          <button
            class="btn btn--primary"
            @click.prevent="confirmRequestNewCard">
            {{ $t('btn.confirm') }}
          </button>
        </div>
      </template>
    </modal>
    <modal
      v-scroll-lock="successModal"
      :withfooter="true"
      v-model="successModal">
      <template slot="modal-panel">
        <figure class="figure figure--icon modal-panel__icons">
          <img
            src="~@/assets/images/icon-check.svg"
            alt="Check icon">
        </figure>
        <h1 class="h2">{{ $t('modal.title.success') }}</h1>
        <p>{{ $t('modal.message.success') }}</p>
      </template>
      <template slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <button
            class="btn btn--primary"
            @click.prevent="successModal = false">
            {{ $t('btn.finish') }}
          </button>
        </div>
      </template>
    </modal>
    <edit-info
      :open="editInfoModal"
      @close="closeEditModal"/>
    <modal
      v-scroll-lock="confirmDataModal"
      :withfooter="true"
      v-model="confirmDataModal">
      <template slot="modal-panel">
        <template v-if="!hasPendingRequest">
          <h1 class="h2">{{ $t('modal.title.confirmData') }}</h1>
          <p>{{ $t('modal.message.first.confirmData') }}<br>{{ $t('modal.message.second.confirmData') }}</p>
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
            class="btn btn--light"
            @click.prevent="openEditInfoModal">
            {{ $t('btn.edit') }}
          </button>
          <button
            class="btn btn--primary"
            @click.prevent="confirmRequestNewCard">
            {{ $t('btn.confirm') }}
          </button>
        </div>
      </template>
    </modal>
    <modal
      v-scroll-lock="requestNewCardWithReasonModal"
      :withfooter="true"
      v-model="requestNewCardWithReasonModal">
      <template slot="modal-panel">
        <h1 class="h2">{{ $t('modal.title.requestNewWithReason') }}</h1>
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
              class="f-field__label f-field__label--radio">{{ $t(`label.requestReason.${reason.toLowerCase().split(' ')[0]}`) }}</label>
          </div>
          <div
            v-if="openOtherReasonInput"
            class="f-field">
            <textarea
              id="cardRequestReason-other-text"
              v-model="otherRequestReasonText"
              name="cardRequestReason-other-text"
              cols="30"
              rows="6"
              class="f-field__textarea"
              @input="changedOtherRequestReasonText"/>
          </div>
        </form>
      </template>
      <template slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <button
            class="btn btn--light"
            @click.prevent="closeRequestNewCardWithReasonModal">
            {{ $t('btn.cancel') }}
          </button>
          <button
            :disabled="isConfirmRequestWithReasonDisabled"
            :class="{ 'btn--disabled': isConfirmRequestWithReasonDisabled }"
            class="btn btn--primary"
            @click.prevent="confirmRequestNewCardWithReason">
            {{ $t('btn.confirm') }}
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
        <h1 class="h2">{{ $t('modal.title.parts.first.cardResponsabilities') }}<br>{{ $t('modal.title.parts.second.cardResponsabilities') }}</h1>
        <p>{{ $t('modal.message.cardResponsabilities') }}</p>
      </template>
      <template slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <a
            :href="`tel: ${securityPhoneNumber}`"
            target="_blank"
            class="btn btn--light">
            {{ $t('btn.call') }}
          </a>
          <button
            class="btn btn--primary"
            @click.prevent="confirmResponsabilities">
            {{ $t('btn.next') }}
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
        <h1 class="h2">{{ $t('modal.title.readyForPickup') }}</h1>
        <p>{{ $t('modal.message.first.readyForPickup') }} {{ $t(getSelectedCardDisplayPickupLocation()) }}.</p>
        <p v-if="getSelectedCardPickupLocation() === pickupLocations.ALAMEDA_SANTANDER">{{ $t('modal.message.second.readyForPickup') }}</p>
      </template>
      <template slot="modal-footer">
        <a
          :href="pickupLocationsUrls[getSelectedCardPickupLocation()]"
          target="_blank"
          class="btn btn--primary btn--outline layout-list-cards__modal-footer">
          {{ $t('btn.getDirections') }}
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
        <template v-if="!hasPendingRequest">
          <figure class="figure figure--icon modal-panel__icons">
            <img
              src="~@/assets/images/icon-warning.svg"
              alt="Warning icon">
          </figure>
          <h1 class="h2">{{ $t('modal.title.confirmDeliver') }}</h1>
          <p>{{ $t('modal.message.confirmDeliver') }}</p>
        </template>
        <loading v-if="hasPendingRequest" />
      </template>
      <template
        v-if="!hasPendingRequest"
        slot="modal-footer">
        <div class="btn--group layout-list-cards__modal-footer">
          <button
            class="btn btn--light"
            @click.prevent="closeConfirmDeliverCardModal">
            {{ $t('btn.cancel') }}
          </button>
          <button
            class="btn btn--primary"
            @click.prevent="deliverSelectedCard">
            {{ $t('btn.confirm') }}
          </button>
        </div>
      </template>
    </modal>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import Vue from 'vue'
import Modal from '@/components/utils/Modal'
import IdCard from '@/components/IdCard'
import Loading from '@/components/Loading'
import EditInfo from '@/components/EditInfo'
import * as cardStates from '@/utils/cards/CardStates'
import * as requestReasons from '@/utils/reasons/RequestReasons'
import * as pickupLocations from '@/utils/pickup/PickupLocations'
import pickupLocationsUrls from '@/utils/pickup/PickupLocationsURLs'

export default {
  name: 'ListCardsPage',
  components: {
    IdCard,
    Modal,
    Loading,
    EditInfo
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
      stateTransitionLabels: {
        [cardStates.REQUESTED]: 'requested',
        [cardStates.BANK_REQUEST]: 'bankRequest',
        [cardStates.IN_PRODUCTION]: 'inProduction',
        [cardStates.READY_FOR_PICKUP]: 'readyForPickup',
        [cardStates.DELIVERED]: 'delivered'
      },
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
      securityPhoneNumber: '+351218419162',
      cardFeaturesUrl: 'https://tecnico.ulisboa.pt/pt/viver/servicos/cartao-de-identificacao/',
      discountsAndPromotionsUrl: 'https://drh.tecnico.ulisboa.pt/protocolos-e-acordos/',
      pickupLocationsUrls,
      pickupLocations,
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
      'currentUser',
      'isInitialLoading'
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
        return this.availableCards[this.selectedCardIndex - 1]
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
    },
    hasAllCardsExpired () {
      const { cards } = this.cardsPage
      return cards && this.cardsPage.cards.every(c => c.currentState === this.cardStates.EXPIRED)
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
      this.hasPendingRequest = true
      this.requestNewCardModal = true
      await this.getCardPreview({ errorCallback: this.closeCardDataModals })
      this.hasPendingRequest = false
    },
    resetCurrentError () {
      this.currentError = { title: '', message: '' }
    },
    async confirmRequestNewCard () {
      try {
        this.hasPendingRequest = true
        if (this.currentRequestReason) {
          await this.requestNewCard({
            requestReason: this.currentRequestReason === this.requestReasons.OTHER ? `OTHER: ${this.otherRequestReasonText}` : this.currentRequestReason
          })
          this.currentRequestReason = undefined
        } else {
          await this.requestNewCard({ requestReason: '' })
        }
      } catch (err) {
        console.error(err)
      }

      await this.fetchCards()
      await this.fetchProfile()
      this.hasPendingRequest = false
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
    closeEditModal () {
      this.editInfoModal = false
      this.openRequestNewCardModal()
    },
    async openConfirmDataModal () {
      this.editInfoModal = false
      this.editClicked = false
      this.hasPendingRequest = true
      this.confirmDataModal = true
      await this.getCardPreview({ errorCallback: this.closeCardDataModals })
      this.hasPendingRequest = false
    },
    closeCardDataModals () {
      this.requestNewCardModal = false
      this.confirmDataModal = false
    },
    async getCardPreview ({ errorCallback }) {
      this.resetCurrentError()
      try {
        await this.fetchPreview()
      } catch (err) {
        errorCallback()
        this.currentError = {
          title: Vue.i18n.translate('error.card.preview.title'),
          message: err.response.data.error
        }
        this.displayErrorModal = true
      }
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
    changedOtherRequestReasonText () {
      this.isConfirmRequestWithReasonDisabled = this.otherRequestReasonText.length === 0
    },
    async confirmResponsabilities () {
      await this.openRequestNewCardModal()
      this.cardResponsabilitiesModal = false
    },
    async deliverSelectedCard () {
      try {
        const id = this.selectedCard.cardId
        this.hasPendingRequest = true
        await this.deliverCard({ id })
        await this.fetchCards()
        this.closeConfirmDeliverCardModal()
        this.hasPendingRequest = false
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

      const deltaPos = currentPosition - this.swipePosX2
      const maxDrag = this.convertRemToPixels(this.cardPadding + this.cardMargin - (this.cardWidth + this.cardMargin * 2) * (this.cardsPage.cards.length - 1))
      const minDrag = this.convertRemToPixels(this.cardPadding + this.cardMargin)
      if (deltaPos >= maxDrag && deltaPos <= minDrag) {
        this.$refs.cardsContainer.style.transform = `translate(${currentPosition - this.swipePosX2}px)`
      }
    },
    dragEnd (event) {
      const transform = this.$refs.cardsContainer.style.transform
      const finalPosition = transform.replace(/[a-zA-Z()]/g, '')
      if (finalPosition - this.swipePosInitial < -40 && this.nextCard) {
        this.selectNextCard()
      } else if (finalPosition - this.swipePosInitial > 40 && this.previousCard) {
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
    },
    getSelectedCardPickupLocation () {
      if (this.availableCards.length > 0) {
        const { pickupAddress } = this.selectedCard

        return `${pickupAddress.address2.trim()}, ${pickupAddress.zipCode.trim()}`
      }

      return false
    },
    getSelectedCardDisplayPickupLocation () {
      const pickupLocation = this.getSelectedCardPickupLocation()

      switch (pickupLocation) {
        case this.pickupLocations.ALAMEDA_SANTANDER:
          return 'text.pickupLocations.alameda.santander'
        case this.pickupLocations.ALAMEDA_DRH:
          return 'text.pickupLocations.alameda.drh'
        case this.pickupLocations.TAGUS_NAGT:
          return 'text.pickupLocations.tagus.nagt'
        case this.pickupLocations.TAGUS_DRH:
          return 'text.pickupLocations.tagus.drh'
        case this.pickupLocations.CTN_RH:
          return 'text.pickupLocations.ctn.rh'
        default:
          return false
      }
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
  height: 14.5rem;
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
    height: 20rem;
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
