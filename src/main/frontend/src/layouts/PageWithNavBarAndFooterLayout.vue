<template>
  <div
    id="app"
    :class="{ 'nav-is-visible': showSideMenu }"
    class="page-width-nav-bar-and-footer-bar-layout"
  >
    <top-nav-bar
      @toggle-mobile-menu="toggleMobileMenu"
    />
    <div class="page-width-nav-bar-and-footer-bar-layout__main-content main-content">
      <router-view :key="$route.fullPath" />
    </div>
    <bottom-bar />
    <div
      class="page-width-nav-bar-and-footer-bar-layout__overlay"
      aria-hidden="true"
    />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import TopNavBar from '@/components/TopNavBar.vue'
import BottomBar from '@/components/BottomBar.vue'

export default {
  components: {
    TopNavBar,
    BottomBar
  },
  data () {
    return {
      showSideMenu: false
    }
  },
  computed: {
    ...mapState([
      'profile'
    ])
  },
  methods: {
    toggleMobileMenu (show) {
      this.showSideMenu = show
    }
  }
}
</script>
<style lang="scss">
.main-header,
.main-content,
.app-footer {
  transition: transform 600ms cubic-bezier(0.77, 0, 0.175, 1);

  .page-width-nav-bar-and-footer-bar-layout.nav-is-visible & {
    transform: translateX(-#{$menu-mobile--width});
  }
}
.page-width-nav-bar-and-footer-bar-layout {
  display: flex;
  flex-flow: column nowrap;
  min-height: 50rem;
  justify-content: stretch;
  @media screen and (min-height: 50rem), (min-width: 37.5rem) {
    min-height: 100vh;
  }
}
.page-width-nav-bar-and-footer-bar-layout__main-content {
  flex-grow: 1;
  background-color: white;
  display: flex;
  flex-flow: column nowrap;
}
.page-width-nav-bar-and-footer-bar-layout.nav-is-visible .page-width-nav-bar-and-footer-bar-layout__overlay {
  opacity: 1;
  visibility: visible;
}
.page-width-nav-bar-and-footer-bar-layout__overlay {
  position: fixed;
  z-index: 1;
  height: 100%;
  width: 100%;
  top: 0;
  left: 0;
  cursor: pointer;
  background-color: rgba(46, 50, 66, 0.3);
  visibility: hidden;
  opacity: 0;
  -webkit-backface-visibility: hidden;
  backface-visibility: hidden;
  transition:
    opacity 600ms cubic-bezier(0.77, 0, 0.175, 1),
    visibility 600ms cubic-bezier(0.77, 0, 0.175, 1);
}
.main-content {
  padding-top: $header-height;
  max-width: 71.25rem;
  width: 100%;
  margin-left: auto;
  margin-right: auto;
}
</style>
