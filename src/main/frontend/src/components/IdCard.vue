<template>
  <div
    v-if="cardInfo"
    :class="[{'id-card--light-theme': lighttheme}, `id-card--state-${visualCardStates[cardState].toLowerCase()}`]"
    class="id-card">
    <img
      src="~@/assets/images/CardBg.svg"
      class="id-card__background"
      alt="">
    <span
      v-if="cardState === cardStates.REQUESTED || cardState === cardStates.EXPIRED"
      class="label label--sm id-card__label label--primary">{{ $t(`id-card.states.${visualCardStates[cardState].toLowerCase()}`) }}</span>
    <img
      src="~@/assets/images/TecnicoLisboa.svg"
      class="id-card__logo"
      alt="Logo Técnico">
    <figure class="id-card__photo">
      <img
        :src="image"
        class="id-card__photo-img"
        alt="Photo">
      <figcaption class="sr-only">{{ cardInfo.name }} — ID</figcaption>
    </figure>
    <dl class="id-card__info">
      <dt
        v-if="cardNumber"
        class="sr-only">
        {{ $t('id-card.label.number') }}
      </dt>
      <dd
        v-if="cardNumber"
        class="id-card__info-number">
        {{ cardNumber }}
      </dd>
      <dt class="sr-only">
        {{ $t('id-card.label.name') }}
      </dt>
      <dd class="id-card__info-name">
        {{ cardInfo.name }}
      </dd>
      <dt class="sr-only">
        {{ $t('id-card.label.role') }}
      </dt>
      <dd class="id-card__info-role">
        {{ cardInfo.role }}
      </dd>
      <div class="id-card__inline-group">
        <dt class="sr-only">
          istID
        </dt>
        <dd class="id-card__info-ist-id">
          {{ cardInfo.istId }}
        </dd>
        <dt class="id-card__info-due-date-label">
          {{ $t('id-card.label.validThru') }}
        </dt>
        <dd class="id-card__info-due-date">
          {{ cardInfo.expiryDate }}
        </dd>
      </div>
    </dl>

  </div>
  <div
    v-else
    class="id-card id-card--empty-state">
    <slot name="empty-state-message">
      <h1>{{ $t('label.info.noCard') }}</h1>
    </slot>
  </div>
</template>

<script>
import * as cardStates from '@/utils/cards/CardStates'

export default {
  name: 'IdCard',
  props: {
    cardInfo: {
      type: Object,
      required: false,
      default: () => {}
    },
    lighttheme: {
      type: Boolean,
      required: false,
      default: false
    },
    isPreview: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data () {
    return {
      cardStates,
      visualCardStates: {
        [cardStates.REQUESTED]: 'REQUESTED',
        [cardStates.ACTIVE]: 'ACTIVE',
        [cardStates.EXPIRED]: 'EXPIRED'
      }
    }
  },
  computed: {
    image () {
      return this.cardInfo.photo ? `data:image/png;base64,${this.cardInfo.photo}` : `/api/bennu-core/profile/localavatar/${this.cardInfo.istId}`
    },
    cardState () {
      if (this.isPreview) {
        return this.cardStates.ACTIVE
      }

      switch (this.cardInfo.currentState) {
        case this.cardStates.IGNORED:
        case this.cardStates.PENDING:
        case this.cardStates.REQUESTED:
        case this.cardStates.BANK_REQUEST:
        case this.cardStates.IN_PRODUCTION:
          return this.cardStates.REQUESTED
        case this.cardStates.READY_FOR_PICKUP:
        case this.cardStates.DELIVERED:
          return this.cardStates.ACTIVE
        default:
          return this.cardInfo.currentState
      }
    },
    cardNumber () {
      if (this.isPreview || !this.cardInfo.serialNumber) {
        return undefined
      }

      return this.cardInfo.serialNumber.match(/.{1,4}/g).reduce((accumulator, part) => `${accumulator} ${part}`)
    }
  }
}
</script>

<style lang="scss">
    @import "@/assets/scss/_variables.scss";
    .id-card {
        width: 19.875rem;
        height: 11.875rem;
        padding: 1.25rem;
        overflow: hidden;
        background-color: white;
        box-shadow: 0 0.625rem 1.25rem 0 rgba(black,.2);
        border-radius: 0.5rem;
        background-repeat: no-repeat;
        background-size: 100% 100%;
        position: relative;
        color: white;
        display: inline-block;
    }
    .id-card__background {
        position: absolute;
        top: 0;
        left: 0;
        bottom: 0;
        right: 0;
        width: 100%;
        height: 100%;
    }
    .id-card__logo {
        position: absolute;
        top: 1.25rem;
        left: 1.25rem;
    }
    .id-card__photo {
        width: 4.6875rem;
        height: 6.125rem;
        position: absolute;
        top: 0.9375rem;
        right: 0.9375rem;
    }
    .id-card__photo-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 0.1875rem;
    }
    .id-card__info {
        position: absolute;
        bottom: 1.25rem;
        left: 1.25rem;
        display: flex;
        flex-flow: column nowrap;
        justify-content: flex-end;
        font-size: 0.625rem;
        text-transform: uppercase;
        font-family: $title-font;
        text-align: left;
    }
    .id-card__info-name,
    .id-card__info-number,
    .id-card__info-role,
    .id-card__info-ist-id {
        margin: 0;
        margin-top: 0.1875rem;
    }
    .id-card__info-name,
    .id-card__info-number {
        font-size: 0.6875rem;
        max-width: 10.75rem;
        margin-top: 0.5rem;
    }
    .id-card__info-name,
    .id-card__info-number,
    .id-card__info-due-date-label {
        font-weight: 500;
    }
    .id-card--empty-state {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-flow: column nowrap;
        text-align: center;
        p {
            color: $gray;
        }
    }
    .id-card--light-theme {
        color: $dark;
    }
    .id-card--state-requested .id-card__background,
    .id-card--state-requested .id-card__logo,
    .id-card--state-requested .id-card__photo {
        filter: opacity(0.5);
    }
    .id-card--state-expired .id-card__background,
    .id-card--state-expired .id-card__logo,
    .id-card--state-expired .id-card__photo {
        filter: grayscale(1);
        opacity: .4;
    }
    .id-card--state-expired .id-card__label {
        background-color: $magenta;
        border-color: $magenta;
    }
    .id-card--state-active .id-card__label {
        opacity: 0;
        visibility: hidden;
    }
    .id-card__label {
        position: absolute;
        top: 1rem;
        left: 1rem;
        z-index: 1;
    }
    .id-card__inline-group{
        display: flex;
        flex-flow: row nowrap;
        align-items: baseline;
    }
    .id-card__info-due-date-label{
        margin-left: 1.5rem;
    }
    .id-card__info-due-date{
        margin-left: .25rem;
    }
    .modal__panel .id-card {
      margin: 1.8rem 50%;
      transform: translate(-50%);
    }
    @media (min-width: 768px){
      .id-card {
        width: 28.875rem;
        height: 17.25rem;
      }
      .id-card__label {
        top: 2rem;
        left: 2rem;
      }
      .id-card__logo {
        top: 2.25rem;
        left: 2.25rem;
      }
      .id-card__photo{
        top: 2rem;
        right: 2rem;
        width: 6rem;
        height: 8rem;
      }
      .id-card__info {
        bottom: 2.25rem;
        left: 2.25rem;
        font-size: 0.825rem;
      }
      .id-card__info-name, .id-card__info-number {
        font-size: 0.875rem;
      }
    }

</style>
