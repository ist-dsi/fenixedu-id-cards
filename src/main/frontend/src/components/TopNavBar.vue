<template>
  <header
    v-click-outside="closeMobileMenu"
    class="main-header">
    <div class="container container--header">
      <div class="main-header__logo">
        <router-link
          :to="{ name: 'ListCardsPage' }"
          :aria-label="$t('al.tecnicoCard.homepage')"
          class="logo">
          <svg>
            <g
              fill="none"
              fill-rule="evenodd">
              <path
                d="M23.4116667 11.8098782h-1.272381v9.5279744h-2.7538095v-9.5279744h-1.2695238V9.8989231h5.2957143v1.9109551zm-6.7819048-4.9444808l-.0011905 17.5114359c0 2.2698077-1.8461904 3.4450321-4.1230952 3.4450321-2.2766667 0-4.1390476-.9918846-4.1390476-2.918968h.002381c0-.7563653.6159523-1.3701859 1.3747618-1.3701859.7588096 0 1.3742857.6138206 1.3742857 1.3701859h.0011905c0 1.3995962.1121429 2.3492629 1.3864286 2.3492629 1.37 0 1.37-.9539359 1.37-2.875327l.0011905-17.5114359c0-2.2679102 1.8454762-3.4440833 4.1228571-3.4440833 2.2766667 0 4.1390476.9906987 4.1390476 2.9187308h-.0021428c0 .7563654-.6154762 1.3697115-1.3754762 1.3697115-.7585714 0-1.3738095-.6133461-1.3738095-1.3697115h-.0016667c0-1.399359-.1114286-2.3490257-1.3859524-2.3490257-1.3704762 0-1.3697619.9522757-1.3697619 2.8743782zM8.3709524 9.8989231h2.7452381l.0040476 11.4320513H8.3680952l.0028572-11.4320513zM0 .1861859v15.450109C0 29.5478205 15.2492857 37 15.2492857 37s15.3064286-7.4521795 15.3064286-21.3637051V.1861859H0z"
                fill="#009DE0"/>
              <text
                font-family="Klavika"
                font-size="17"
                font-weight="500"
                fill="#45555F">
                <tspan
                  x="41"
                  y="21">CARD</tspan>
              </text>
            </g>
          </svg>
        </router-link>
      </div>
      <ul
        class="mobile-nav mobile-only"
        hidden>
        <li>
          <a
            ref="burguerMenu"
            :aria-expanded="showMobileMenu"
            class="mobile-nav-trigger"
            aria-controls="main-nav"
            aria-label="Menu"
            href
            @click.prevent="toggleMobileMenu">
            <span class="icon icon-burguer">
              <svg
                width="24"
                height="24"
                xmlns="http://www.w3.org/2000/svg">
                <g
                  transform="translate(0 4)"
                  class="icon--fill"
                  fill="#2E3242"
                  fill-rule="evenodd">
                  <g class="icon-burguer__top-bar"><rect
                    width="24"
                    height="2"
                    rx="1"/></g>
                  <g class="icon-burguer__center-bar"><rect
                    width="24"
                    height="2"
                    rx="1"
                    y="7"/></g>
                  <g class="icon-burguer__bottom-bar"><rect
                    width="24"
                    height="2"
                    rx="1"
                    y="14"/></g>
                </g>
              </svg>
            </span>
          </a>
        </li>
      </ul>

      <nav
        id="main-nav"
        ref="nav"
        class="main-nav menu menu-active">
        <ul class="primary-nav">
          <li
            v-if="profile"
            hidden
            class="has-children mobile-only">
            <a
              href
              aria-controls="profile-menu"
              @click.prevent="openSubMenu">
              <div class="link-icon">
                <img
                  :src="`${profile.photo ? 'data:image/png;base64,' + profile.photo.data : undefined }`"
                  :alt="profile.name"
                  class="user-avatar">
              </div>
              <div class="link-text">{{ profile.name }}</div>
            </a>
            <ul
              id="profile-menu"
              class="secondary-nav is-hidden">
              <li
                class="go-back mobile-only">
                <a
                  aria-controls="profile-menu"
                  href
                  @click.prevent="closeSubMenu">
                  <span class="link-text">{{ $t('btn.back') }}</span>
                </a>
              </li>
              <li
                v-if="profile && profile.admin"
                hidden
                class="mobile-only">
                <button @click.prevent="openAdminPage">
                  <span class="link-text">{{ $t('btn.admin') }}</span>
                </button>
              </li>
              <li class="mobile-only">
                <button
                  v-if="profile"
                  @click.stop="logout">
                  <span class="link-text">{{ $t('btn.logout') }}</span>
                </button>
              </li>
            </ul>
          </li>
          <li
            v-if="isMobile"
            class="has-children mobile-only">
            <a
              href
              @click.prevent="openSubMenu">
              <span class="link-text">{{ $t('label.language') }}</span>
            </a>
            <ul class="secondary-nav is-hidden">
              <li
                class="go-back mobile-only">
                <a
                  href
                  @click.prevent="closeSubMenu">
                  <span class="link-text">{{ $t('btn.back') }}</span>
                </a>
              </li>
              <li>
                <a
                  v-if="$i18n.locale() === 'en'"
                  href
                  @click.prevent="closeMobileMenu() + setLocale('pt')">Português</a>
              </li>
              <li>
                <a
                  v-if="$i18n.locale() === 'pt'"
                  href
                  @click.prevent="closeMobileMenu() + setLocale('en')">English</a>
              </li>
            </ul>
          </li>
        </ul>

        <ul class="utility-nav">
          <li
            v-if="!isMobile"
            class="languages">
            <dropdown :size="'md'">
              <a
                slot="dropdown-trigger"
                :aria-label="$t('label.language')"
                href
                class="lang-trigger"
                aria-haspopup="true">
                <span class="icon icon-lang">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="20"
                    height="16">
                    <path
                      fill="#8F95A1"
                      class="icon--fill"
                      fill-rule="nonzero"
                      d="M9.399 9.399c-1.202-1.231-2.404-2.35-2.95-4.923h4.48V2.573h-4.48V0H4.48v2.573H0v1.903h4.48c0 .67.11.335 0 .67-.655 2.462-1.42 4.14-4.48 5.707l.656 1.902c2.95-1.566 4.48-3.468 5.136-5.706.656 1.678 1.749 3.02 2.951 4.252L9.4 9.399zM15.3 3.133h-2.514L8.306 16h1.967l1.312-3.804h5.136L18.033 16H20L15.3 3.133zm-3.17 7.049l1.967-5.147 1.968 5.147H12.13z"/>
                  </svg>
                </span>
                <span class="icon-label">{{ $t('label.language') }}</span>
              </a>
              <div slot="dropdown-panel">
                <ul class="dropdown-menu">
                  <li
                    v-if="$i18n.locale() === 'pt'"
                    class="dropdown-menu__item">
                    <a
                      href
                      class="dropdown-menu__link"
                      @click.prevent="setLocale('en')">
                      <span class="link-text">English</span>
                    </a>
                  </li>
                  <li
                    v-if="$i18n.locale() === 'en'"
                    class="dropdown-menu__item">
                    <a
                      href
                      class="dropdown-menu__link"
                      @click.prevent="setLocale('pt')">
                      <span class="link-text">Português</span>
                    </a>
                  </li>
                </ul>
              </div>
            </dropdown>
          </li>
          <li
            v-if="profile && !isMobile"
            class="user">
            <dropdown :size="'lg'">
              <a
                slot="dropdown-trigger"
                :aria-label="$t('al.account')"
                href
                class="user-trigger"
                aria-haspopup="true">
                <avatar
                  :name="profile.name"
                  :src="`${profile.photo ? 'data:image/png;base64,' + profile.photo.data : undefined }`"
                  class="figure--36" />
              </a>
              <div slot="dropdown-panel">
                <div class="dropdown-user__details">
                  <avatar
                    :name="profile.name"
                    :src="`${profile.photo ? 'data:image/png;base64,' + profile.photo.data : undefined }`"
                    class="figure--48" />
                  <div class="dropdown-user__text">
                    <div
                      class="name">{{ profile.name }}</div>
                    <div class="user-name">{{ profile.username }}</div>
                  </div>
                </div>
                <ul class="dropdown-menu">
                  <li
                    v-if="profile && profile.admin"
                    class="dropdown-menu__item">
                    <button
                      class="dropdown-menu__link"
                      @click.prevent="$router.push({ name: 'AdminUserSearchPage' })">
                      <span class="link-icon">
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          width="22"
                          height="22"
                        >
                          <g
                            fill="none"
                            fill-rule="evenodd"
                          >
                            <path d="M-1-1h24v24H-1z" />
                            <path
                              stroke="#8F95A1"
                              stroke-width="1.6"
                              d="M4.7734498 2.9929783c-.2273648.1161469-.567943.3765366-.985939.7945325-.417996.417996-.6783856.7585742-.7945325.985939L4.469358 6.9625645l-.2277796.4251855c-.236851.4421188-.4297518.9067712-.5754995 1.388053L3.52635 9.23721l-2.5910052.5038065C.8567018 9.983916.8 10.4088645.8 11c0 .5911356.0567019 1.0160841.1353448 1.2589835L3.52635 12.76279l.139729.461407c.1457476.4812818.3386484.9459342.5754994 1.388053l.2277796.4251855-1.4763797 2.1891147c.1161469.2273647.3765366.567943.7945325.985939.417996.4179959.7585742.6783856.985939.7945325l2.1891147-1.4763797.4251855.2277796c.4421188.236851.9067712.4297518 1.388053.5754994l.461407.139729.5038065 2.5910052C9.983916 21.1432981 10.4088645 21.2 11 21.2c.5911356 0 1.0160841-.0567019 1.2589835-.1353448L12.76279 18.47365l.461407-.139729c.4812818-.1457476.9459342-.3386484 1.388053-.5754994l.4251855-.2277796 2.1891147 1.4763797c.2273647-.1161469.567943-.3765366.985939-.7945325.4179959-.417996.6783856-.7585743.7945325-.985939l-1.4763797-2.1891147.2277796-.4251855c.236851-.4421188.4297518-.9067712.5754994-1.388053l.139729-.461407 2.5910052-.5038065C21.1432981 12.0160841 21.2 11.5911356 21.2 11s-.0567019-1.016084-.1353448-1.2589835L18.47365 9.23721l-.139729-.461407c-.1457476-.4812818-.3386484-.9459342-.5754994-1.388053l-.2277796-.4251855 1.4763797-2.1891147c-.1161469-.2273648-.3765366-.567943-.7945325-.985939-.417996-.417996-.7585743-.6783856-.985939-.7945325L15.0374355 4.469358l-.4251855-.2277796c-.4421188-.236851-.9067712-.4297518-1.388053-.5754995L12.76279 3.52635 12.2589835.9353448C12.0160841.8567018 11.5911356.8 11 .8S9.983916.8567019 9.7410165.9353448L9.23721 3.52635l-.461407.139729c-.4812818.1457476-.9459342.3386484-1.388053.5754994l-.4251855.2277796-2.1891147-1.4763797zM11 15.6076923c-2.5447582 0-4.6076923-2.0629341-4.6076923-4.6076923S8.4552418 6.3923077 11 6.3923077 15.6076923 8.4552418 15.6076923 11 13.5447582 15.6076923 11 15.6076923z"
                            />
                          </g>
                        </svg>
                      </span>
                      <span class="link-text">{{ $t('btn.admin') }}</span>
                    </button>
                  </li>
                  <li
                    v-if="profile"
                    class="dropdown-menu__item">
                    <button
                      class="dropdown-menu__link"
                      @click.stop="logout">
                      <span class="link-icon">
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          width="18"
                          height="19">
                          <g
                            fill="none"
                            fill-rule="evenodd">
                            <path d="M-6-3h24v24H-6z"/>
                            <g
                              stroke="#8F95A1"
                              stroke-linecap="round">
                              <path
                                stroke-linejoin="round"
                                stroke-width="2"
                                d="M9.076923 1.5h-8V18h7.5750767"/>
                              <path
                                stroke-width="1.8"
                                d="M12 6l4 4.0122369L12.0256659 14M6 10h9"/>
                            </g>
                          </g>
                        </svg>
                      </span>
                      <span class="link-text">{{ $t('btn.logout') }}</span>
                    </button>
                  </li>
                </ul>
              </div>
            </dropdown>
          </li>
        </ul>
        <ul/>
      </nav>
    </div>
    <feedback-top-bar />
  </header>

</template>

<script>
import { mapState } from 'vuex'
import ClickOutside from 'vue-click-outside'
import Dropdown from '@/components/utils/Dropdown'
import Avatar from '@/components/utils/Avatar'
import FeedbackTopBar from '@/components/utils/FeedbackTopBar'

export default {
  components: {
    Dropdown,
    Avatar,
    FeedbackTopBar
  },
  directives: {
    ClickOutside
  },
  data () {
    return {
      mobileMenuBreakpoint: 1200,
      showMobileMenu: false,
      windowWidth: 0,
      isMobile: false
    }
  },
  computed: {
    ...mapState([
      'profile'
    ])
  },
  watch: {
    windowWidth: {
      immediate: true,
      handler (newWidth, oldWidth) {
        if (newWidth < this.mobileMenuBreakpoint) {
          this.isMobile = true
        } else {
          this.closeMobileMenu()
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
    openSubMenu (event) {
      const selectedElement = event.target.parentNode
      const subMenuElement = selectedElement.querySelector('ul')
      const parentNav = selectedElement.parentNode
      const utilityNav = parentNav.parentNode.querySelector('ul.utility-nav')
      if (subMenuElement.classList.contains('is-hidden')) {
        selectedElement.classList.add('selected')
        subMenuElement.classList.remove('is-hidden')
        parentNav.classList.add('moves-out')
        if (utilityNav) {
          utilityNav.classList.add('moves-out')
        }
      } else {
        selectedElement.classList.remove('selected')
        parentNav.classList.add('moves-out')
      }
    },
    closeSubMenu (event) {
      const selectedElement = event.target.parentNode
      const currentNav = selectedElement.parentNode
      const previousNav = currentNav.parentNode.parentNode
      const utilityNav = previousNav.parentNode.querySelector('ul.utility-nav')
      currentNav.classList.add('is-hidden')
      previousNav.classList.remove('moves-out')
      if (utilityNav) {
        utilityNav.classList.remove('moves-out')
      }
    },
    closeMobileMenu () {
      if (this.$refs.nav) {
        const navigations = this.$refs.nav.querySelectorAll('.has-children ul')
        const selectedNavigations = this.$refs.nav.querySelectorAll('.has-children a')
        const elementsMovedOut = this.$refs.nav.querySelectorAll('.moves-out')
        for (const navigation of navigations) {
          navigation.classList.add('is-hidden')
        }
        for (const selectedNavigation of selectedNavigations) {
          selectedNavigation.classList.remove('selected')
        }
        for (const elementMovedOut of elementsMovedOut) {
          elementMovedOut.classList.remove('moves-out')
        }
      }
      document.body.classList.remove('scroll-lock')
      this.showMobileMenu = false
      this.$emit('toggle-mobile-menu', this.showMobileMenu)
    },
    openMobileMenu () {
      document.body.classList.add('scroll-lock')
      this.showMobileMenu = true
      this.$emit('toggle-mobile-menu', this.showMobileMenu)
    },
    toggleMobileMenu () {
      if (this.showMobileMenu) {
        this.closeMobileMenu()
      } else {
        this.openMobileMenu()
      }
    },
    openAdminPage () {
      this.closeMobileMenu()
      this.$router.push({ name: 'AdminUserSearchPage' })
    },
    logout () {
      window.location.href = `/logout`
    }
  }
}

</script>
<style lang="scss">
// import variables
@import "@/assets/scss/_variables.scss";
@import "@/assets/scss/_buttons.scss";
@import "@/assets/scss/layouts/_header.scss";

// bootstrap container or not?
.container {
  width: 100%;
  padding-right: 0.9375rem;
  padding-left: 0.9375rem;
  margin-right: auto;
  margin-left: auto;
  max-width: 71.25rem;
}

// ================== Main Header Styles
.main-header,
.container--header {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  justify-content: space-between;
}

.main-header {
  background: rgba(#fff, 0.97);
  width: 100%;
  height: $header-height;
  box-shadow: 0 0.0625rem 0 0 rgba($dark, 0.1);
  position: fixed;
  z-index: 2;
  // padding: 0.25rem 0;
}

.main-header__logo {
  display: flex;
  flex-direction: row;
  align-items: center;

  padding: 0.5rem 0;
}

.logo {
  display: flex;
  flex-direction: row;
  align-items: center;

  max-width: 12.5rem;
  height: 100%;
  height: 2.3rem;
  max-height: 100%;
}

.logo img,
.logo svg {
  width: auto;
  height: 100%;
  object-fit: contain;
}
.main-header__logo .logo svg.ul{
    width: 100%;
    height: auto;
}
.mobile-nav {
  display: flex;
  align-items: center;
}

.mobile-nav li ~ li {
  margin-left: 1.5rem;
}

// ================== Global Menu Styles
.menu .moves-out {
  transform: translateX(-100%);
}

.menu > ul ul {
  position: absolute;
  top: 0;
  left: 100%;
}

.menu .has-children > a:before,
.menu .go-back > a:before {
    position: absolute;
    content: "";
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}

.menu .has-children > a:after,
.menu .go-back > a:after {
  position: absolute;
  content: "";
  top: 50%;
  height: 0.625rem;
  width: 0.625rem;
  border: 0.125rem solid $slate;
  border-left: 0;
  border-bottom: 0;
  transform: rotate(45deg) translateY(-50%);
}

.menu .has-children > a:after {
  right: 1rem;
}

.menu .go-back > a {
  padding-left: 2.5rem;
}

.menu .go-back > a:after {
  left: 1rem;
  transform: rotate(-135deg) translateY(50%);
}

// a editar
.menu .is-hidden {
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
}

// IF CSS is disabled, visibility of .mobile-only elements is turned off with attribute "hidden" to simplify document outline,
// but if CSS is enabled they are needed
.mobile-only[hidden] {
  display: flex;

  @media screen and (min-width: 1200px) {
    display: none;
  }
}

.user-side-menu {
  display: flex;
  flex-direction: row;
  img {
    margin-top: 10px;
    margin-right: 10px;
  }
}
// ================== end Global Menu Styles

// ================== Main Nav on mobile
.main-nav {
  position: fixed;
  top: 0;
  bottom: 0;
  right: -#{$menu-mobile--width};
  width: $menu-mobile--width;
  height: 100vh;
  background-color: $dark-600;
  overflow-x: hidden;
  overflow-y: auto;
  // transition: all 600ms cubic-bezier(0.77, 0, 0.175, 1); // INFO, removed due to animation on window resize
}

.main-nav ul {
  display: flex;
  flex-direction: column;
  width: 100%;
  transition: all 600ms cubic-bezier(0.77, 0, 0.175, 1);
}

.main-nav a {
  position: relative;
  color: #fff;
  display: flex;
  padding: 0 16px;
  width: 100%;
  height: $header-height;
  line-height: $header-height;
  border-bottom: 0.0625rem solid $dark;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

//mobile active state
  .primary-nav li .active{
    color: $blue;
    font-weight: 600;
  }
// ================== end of Main Nav on mobile

// ================== Main Nav on desktop
@media screen and (min-width: 1200px) {
  // ================== Global Menu Styles
  .menu > ul ul {
    position: static;
  }

  .menu a {
    color: $dark;
    display: block;
    font-size: 1rem;
    transition: color 0.2s ease-in-out;

    &:hover,
    &:focus {
      color: $blue-600;
    }

    &:active {
      color: $blue-700;
    }
  }
  // ================== end Global Menu Styles

  .main-nav {
    position: static;
    width: calc(100% - 23rem);
    height: auto;
    background-color: #fff;
    display: flex;
    align-items: stretch;
    justify-content: space-between;
    margin-left: auto;
    transition: none;
    overflow: visible;
  }

  // .main-nav ul,
  .main-nav > ul {
    flex-direction: row;
    width: auto;
  }

  .main-nav > ul > li {
    display: flex;
  }

  .main-nav > ul > li > a {
    display: flex;
    align-items: center;
  }

  .main-nav a {
    height: auto;
    line-height: normal;
    white-space: nowrap;
    white-space: normal;
    padding: 0;
    border-bottom: none;
  }

  .main-nav .has-children > a:after,
  .main-nav .go-back > a:after {
    content: initial;
  }

  .main-nav .has-children.has-focus > a,
  .main-nav .has-children:focus-within > a {
    color: $blue-600;
  }

  .main-nav .secondary-nav {
    background-color: $light-blue;
    padding: 2.5rem calc((100vw - 1110px) / 2);
    position: absolute;
    left: 0;
    right: 0;
    top: $header-height;
    width: 100%;
    height: auto;

    display: flex;
    flex-direction: row;
    justify-content: flex-start;
    align-items: flex-start;

    transition: opacity 0.2s ease-in-out 0.18s,
      visibility 0.2s ease-in-out 0.18s;
  }

  .primary-nav > li ~ li {
    margin-left: 2rem;
  }

  .primary-nav > li.has-children:focus-within .is-hidden {
    opacity: 1;
    visibility: visible;
    pointer-events: all;
  }

  // focus-within not widely supported yet
  .primary-nav > li.has-children:focus-within > a {
    color: $blue-600;
    box-shadow: inset 0 -0.125rem 0 $blue-600;
  }

  // alternative to focus-within -> add class .has-focus via js
  .primary-nav > li.has-children:hover .is-hidden,
  .primary-nav > li.has-children.has-focus .is-hidden {
    opacity: 1;
    visibility: visible;
    pointer-events: all;
  }

  .primary-nav > li.has-children > a {
    position: relative;
    box-shadow: inset 0 -0.125rem 0 transparent;
    transition: color 0.2s ease-in-out, box-shadow 0.2s ease-in-out;

    &:hover,
    &:focus {
      box-shadow: inset 0 -0.125rem 0 $blue-600;
    }

    &:active {
      box-shadow: inset 0 -0.125rem 0 $blue-700;
    }
  }

  .primary-nav > li.has-children.has-focus > a {
    color: $blue-600;
    box-shadow: inset 0 -0.125rem 0 $blue-600;

    &:active {
      color: $blue-700;
      box-shadow: inset 0 -0.125rem 0 $blue-700;
    }
  }

  p.has-image {
    font-weight: 400;
    margin-top: 1rem;
  }

  p.has-image img {
    margin: 0.5rem 16px 0 0;
  }

  .secondary-nav ul {
    flex-direction: column;
  }

  .secondary-nav .intro-message {
    display: block;
    opacity: 0.65;
    font-weight: 400;
    line-height: 1.5;
    text-align: left;
    margin-top: 1rem;
    transition: opacity 0.2s ease-in-out;
  }

  .secondary-nav > li {
    flex-basis: 23%;
    margin-right: 2%;
    overflow: hidden;
  }

  .secondary-nav > li > a {
    font-weight: 600;
    margin-bottom: 0.5rem;
  }

  .secondary-nav > li ul li {
    margin-top: 0.625rem;
  }

  .secondary-nav .intro a:hover p,
  .secondary-nav .intro a:focus p,
  .secondary-nav .intro a:hover span,
  .secondary-nav .intro a:focus span {
    opacity: 1;
  }

  .utility-nav {
    align-items: center;
    margin-left: auto;
  }

  .utility-nav > li ~ li {
    margin-left: 1rem;
  }

  .utility-nav > li > a {
    font-size: 0.875rem;
  }
}

// ================== Main Header icons
.main-header .utility-nav .icon {
  display: none;

  @media screen and (min-width: 1200px) {
    display: flex;

    & + .icon-label {
      display: none;
    }
  }
}

.notifications {
  position: relative;
}

.notifications__count {
  position: absolute;
  top: 0;
  right: -2px;
  content: "";
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: $blue-600;

  // to animate (?)
  opacity: 0;
  transform: scale(0.1);
  transition: transform 0.2s cubic-bezier(0.77, 0, 0.175, 1);
}

.notifications__count.is-visible {
  opacity: 1;
  transform: scale(1);
}

.search-trigger,
.notifications-trigger {
  &:hover,
  &:focus {
    .icon--stroke {
      stroke: $blue-600;
    }
  }

  &:active {
    .icon--stroke {
      stroke: $blue-700;
    }
  }
}

.apps-trigger,
.lang-trigger,
.mobile-nav-trigger {
  &:hover,
  &:focus {
    .icon--fill {
      fill: $blue-600;
    }
  }

  &:active {
    .icon--fill {
      fill: $blue-700;
    }
  }
}

.icon-burguer {
  .icon-burguer__top-bar,
  .icon-burguer__center-bar,
  .icon-burguer__bottom-bar {
    transition: transform 0.2s ease-in-out, transform-origin 0.2s ease-in-out,
      opacity 0.2s ease-in-out;
  }

  .nav-is-visible & {
    .icon-burguer__top-bar {
      transform-origin: top left;
      transform: rotate(45deg) translate(3px, -4px);
    }
    .icon-burguer__center-bar {
      opacity: 0;
    }
    .icon-burguer__bottom-bar {
      transform-origin: bottom left;
      transform: rotate(-45deg) translate(8px, 7px);
    }
  }
}

.icon-lang,
.icon-apps,
.icon-burguer {
  .icon--fill {
    transition: fill 0.2s ease-in-out;
  }
}

.icon-search,
.icon-notifications {
  .icon--stroke {
    transition: stroke 0.2s ease-in-out;
  }
}

.main-header .icon {
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

// Avatars

// user avatar on main-header
.user-container {
  width: 2.25rem;
  height: 2.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

// user avatar on main-header
.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid rgba(0, 0, 0, 0.12);
  transition: border 0.2s ease-in-out;

  .user-trigger.has-focus &,
  .user-trigger:hover &,
  .user-trigger:focus & {
    border: 2px solid rgba($blue-600, 1);
  }

  .user-trigger:active & {
    border: 2px solid rgba($blue-700, 1);
  }
}

.user-avatar--lg {
  width: 48px;
  height: 48px;
}
</style>
