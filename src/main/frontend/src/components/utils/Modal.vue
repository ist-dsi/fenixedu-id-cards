<template>
  <transition name="modal-slide-down">
    <div
      v-if="value"
      class="modal"
    >
      <button
        class="modal__close"
        @click.prevent="close"
      >
        <svg
          viewBox="0 0 40 40"
          xmlns="http://www.w3.org/2000/svg"
        >
          <g
            fill="none"
            fill-rule="evenodd"
          >
            <circle
              stroke="#DDE4E9"
              stroke-width="2.5"
              fill="#DDE4E9"
              cx="20"
              cy="20"
              r="18.75"
            />
            <g
              opacity=".5"
              stroke="#1C172F"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
            >
              <path d="M14.12 14.12l11.64 11.64M25.76 14.12L14.12 25.76" />
            </g>
          </g>
        </svg>
      </button>
      <div
        class="modal__panel"
      >
        <slot name="modal-panel" />
      </div>
      <footer
        v-if="withfooter"
        class="modal__footer"
      >
        <slot name="modal-footer" />
      </footer>
    </div>
  </transition>
</template>
<script>
export default {
  name: 'Dropdown',
  props: {
    value: {
      type: Boolean,
      required: true
    },
    withfooter: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  methods: {
    close () {
      this.$emit('input', false)
    }
  }
}
</script>
<style lang="scss">
.modal {
  position: fixed;
  z-index: 1000000;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  overflow-x: hidden;
  overflow-y: auto;
  width: 100vw;
  display: flex;
  flex-flow: column nowrap;
  align-items: center;
  justify-content: center;
  background-color: white;
  box-shadow: 0 0.125rem 2.5rem 0 rgba(black, 0.4);
}

.modal__close {
  position: absolute;
  z-index: 1000001;
  top: 24px;
  right: 32px;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 1.25rem;
  box-shadow: 0 0.125rem 1.25rem 0 rgba(black, 0);
  transition: box-shadow  0.3s   ease;

  svg {
    width: 100%;
    height: 100%;
  }

  circle {
    stroke: $light-blue-600;
    fill: $light-blue-600;
    transition:
      stroke  0.3s   ease,
      fill    0.3s   ease;
  }

  &:hover,
  &:focus {
    circle {
      stroke: $light-blue-700;
      fill: $light-blue-700;
    }
  }

  &:focus {
    outline: none;
    box-shadow: 0 0.125rem 1.25rem 0 rgba(black, 0.4);
  }
}

.modal__panel {
  position: relative;
  width: 100%;
  max-width: 22rem;
  display: flex;
  flex-flow: column nowrap;
  padding: 24px;
  opacity: 1;
  transform: scale(1);
}

.modal__panel__content {
  padding: 24px;
}
.modal__footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 1rem;
  border-top: 1px solid $light-blue;
  display: flex;
  background-color: white;
  justify-content: center;
}
.modal--lg .modal__panel {
  max-width: 32rem;
}

.modal--align-top {
  justify-content: flex-start;

  .modal__panel {
    padding: 8rem 0.5rem 18rem;
    overflow-y: auto;
    max-height: 100vh;
  }
}
.modal-slide-down-enter-active,
.modal-slide-down-leave-active {
  transition:
    opacity   0.3s  ease,
    transform 0.3s  ease;
}
.modal-slide-down-enter,
.modal-slide-down-leave-to /* .fade-leave-active below version 2.1.8 */ {
  opacity: 0;
  transform: scale(0.98);
}

@media (min-width: 1200px) {
  .modal__panel {
    padding: 0;
  }
}
</style>
