<template>
  <div
    v-if="topMessage.active"
    :class="{ 'feedback-top-bar--informative': topMessage.type === 'info', 'feedback-top-bar--warning': topMessage.type === 'warn'}"
    class="feedback-top-bar">
    <div class="feedback-top-bar-container container">
      <div class="feedback-top-bar__message">
        <p>{{ topMessage.msg[$i18n.locale()] }}</p>
      </div>
      <button
        v-if="topMessage.dismiss"
        class="feedback-top-bar__close icon icon-close"
        aria-label="Dismiss this message"
        @click.prevent="dismissMessage()">
        <svg
          class=""
          xmlns="http://www.w3.org/2000/svg"
          width="15"
          height="15">
          <g
            fill="transparent"
            fill-rule="evenodd"
            class="icon-stroke"
            stroke="#FFF"
            stroke-width="1.1666667">
            <circle
              cx="7.2727273"
              cy="7.2727273"
              r="6.6893939"/>
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M5.1336898 5.1336898l4.235078 4.235078M9.3687682 5.1336898l-4.235078 4.235078"/>
          </g>
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
import { mapActions, mapState } from 'vuex'

export default {
  computed: {
    ...mapState({
      'topMessage': state => state.topMessage
    })
  },
  methods: {
    ...mapActions([
      'setTopMessage'
    ]),
    dismissMessage () {
      this.setTopMessage({ active: false, msg: { pt: '', en: '' }, dismiss: false, type: '' })
    }
  }
}
</script>

<style lang="scss">
  @import "@/assets/scss/_variables.scss";

  .feedback-top-bar {
    position: fixed;
    top: $header-height;
    width: 100%;
    min-height: 44px;
    padding: 0.6rem 0 0.5rem;

    // default state
    background: $dark;
    color: $light-blue-700;

    // opacity: 0;
    // visibility: hidden;
    // pointer-events: none;

    transition: opacity 0.2s cubic-bezier(0.77, 0, 0.175, 1), visibility 0.2s cubic-bezier(0.77, 0, 0.175, 1);

    &.is-visible {
      opacity: 1;
      visibility: visible;
      pointer-events: all;
    }

    .icon-close:hover .icon-stroke,
    .icon-close:focus .icon-stroke {
      fill: #FFF;
      stroke: $dark;
    }

  }

  // Other types of feedback: Informative
  .feedback-top-bar--informative {
    background: $blue-600;
    color: #FFF;

    .icon-close:hover .icon-stroke,
    .icon-close:focus .icon-stroke {
      fill: #FFF;
      stroke: $blue-600;
    }
  }
  // Other types of feedback: Warning
  .feedback-top-bar--warning {
    background: $magenta-600;
    color: #FFF;

    .icon-close:hover .icon-stroke,
    .icon-close:focus .icon-stroke {
      fill: #FFF;
      stroke: $magenta-600;
    }
  }

  .feedback-top-bar p {
    color: #FFF;
  }

  .icon-close .icon-stroke {
    transition: all .2s ease-in-out;
  }

  .feedback-top-bar-container {
    display: flex;
    flex-flow: row nowrap;
    align-items: center;
  }

  .feedback-top-bar__message {
    flex-grow: 1;
  }

</style>
