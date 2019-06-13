<template>
  <div
    :class="{'dropdown--dropup': dropup}"
    class="dropdown"
    @click.prevent="toggle"
  >
    <slot name="dropdown-trigger"/>
    <transition name="dropdown-slide-down">
      <div
        v-click-outside="clickOutsideToggle"
        v-show="opened"
        :class="`dropdown--${size}`"
        class="dropdown__panel">
        <slot name="dropdown-panel"/>
      </div>
    </transition>
  </div>
</template>
<script>
import ClickOutside from 'vue-click-outside'
export default {
  name: 'Dropdown',
  directives: {
    ClickOutside
  },
  props: {
    size: {
      type: String,
      required: false,
      default: 'md'
    },
    dropup: {
      type: Boolean,
      required: false,
      default: false
    },
    closesOnClick: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data () {
    return {
      opened: false
    }
  },
  mounted () {
    // prevent click outside event with popupItem.
    this.popupItem = this.$el
  },
  methods: {
    toggle () {
      if (this.closesOnClick) {
        this.opened = !this.opened
      } else {
        this.opened = true
      }
    },
    clickOutsideToggle () {
      this.opened = false
    },
    hide () {
      if (this.closesOnClick) {
        this.opened = false
      }
    }
  }
}
</script>
<style lang="scss">

.dropdown {
  position: relative;
}

.dropdown--xs {
  min-width: 6.25rem;
}

.dropdown--md {
  min-width: 11.5rem;
}

.dropdown--lg {
  min-width: 18.125rem;
}

.dropdown__panel {
  position: absolute;
  top: 1.5em;
  top: calc(100% + 7px);
  right: -.5rem;
  word-wrap: none;
  white-space: nowrap;
  z-index: 10000;
  opacity: 1;
  transform: translateY(0);

  border-radius: 2px;
  background: #FFF;
  // box-shadow: 0 0.1875rem 0.4375rem 0 rgba(0, 0, 0, 0.1);
  box-shadow: 0 0 3px 0 rgba(0,0,0,0.10), 0 5px 10px 0 rgba(0,0,0,0.15);
}

.dropdown__panel:before {
  position: absolute;
    content: "";
    top: -0.5rem;
    // right: 11%;
    right: 1rem;
    // box-shadow: 0 -0.1875rem 0.4375rem 0 rgba(0, 0, 0, 0.1);
    filter: drop-shadow(0px -2px 1px rgba(0, 0, 0, 0.05));
    width: 0;
    height: 0;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
    border-bottom: 8px solid #FFF;
}
.dropdown--dropup {
  .dropdown__panel {
    top: unset;
    bottom: calc(100% + 14px);
    &:before{
      top: unset;
      bottom: -0.5rem;
      filter: drop-shadow(0px 2px 1px rgba(0, 0, 0, 0.05));
      border-bottom: none;
      border-top: 8px solid #FFF;
    }
  }
}
.dropdown__panel--open {

}
.dropdown-slide-down-enter-active, .dropdown-slide-down-leave-active {
  transition: transform .3s ease, opacity .3s ease;
}
.dropdown-slide-down-enter, .dropdown-slide-down-leave-to /* .fade-leave-active below version 2.1.8 */ {
  opacity: 0;
  transform: translateY(-.5rem);
}
.dropdown--up {

}

// Global dropdown menu styles

.dropdown-user__details {
  padding: 16px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #EEF2F5;
}

.dropdown-user__text {
  margin-left: 16px;
}

.dropdown-user__text .name {
  font-weight: 600;
  max-width: 12rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-user__text .user-name {
  font-size: 16px;
  color: #717782;
}

.dropdown-menu {
  display: flex;
  flex-direction: column;

  position: relative;
}

.dropdown-menu__item ~ .dropdown-menu__item a,
.dropdown-menu__item ~ .dropdown-menu__item button {
    border-top: 1px solid #EEF2F5;
}

.dropdown-menu .link-icon {
  // same with as user-avatar
  width: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dropdown-menu .link-text {
  margin-left: 16px;
}

// review main-nav list item and anchor styles
.main-nav .dropdown-menu__link,
.dropdown-menu__link {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 1rem;
  color: #45555F;

  transition: background-color .2s ease-in-out;

  &:hover,
  &:focus {
    background-color: #EEF2F5;
    color: #45555F;
  }

  &:active {
    // background-color: #DDE4E9;
    background-color: darken(#EEF2F5, 2%);
  }
}

</style>
