<template>
  <div
    class="f-tag-field">
    <p
      v-if="label !== ''"
      class="f-tag-field__label">{{ label }}</p>
    <div
      v-for="(tag, i) in tags"
      :key="i"
      class="f-tag-field__tag">
      {{ tag.label }}
      <button
        type="button"
        class="f-tag-field__tag-remove"
        aria-label="Remove"
        @click.prevent="removeTag(tag, i)">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="14"
          height="14"
          class="icon icon-close">
          <g
            fill="none"
            fill-rule="evenodd"
            stroke-width="1.16667"
            transform="translate(.33333 .33333)">
            <circle
              cx="6.66667"
              cy="6.66667"
              r="6.08333"
              class="circle--stroke"
              stroke="#C9CDD4"
              fill="transparent"/>
            <path
              class="cross--stroke"
              stroke="#8F95A1"
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M4.70588 4.70588l3.88216 3.88216m0-3.88216L4.70588 8.58804"/>
          </g>
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CardPreviewPage',
  components: {},
  props: {
    label: {
      type: String,
      required: false,
      default: ''
    },
    tags: {
      type: Array,
      required: true
    }
  },
  data () {
    return {
      mobileMenuBreakpoint: 768,
      isMobile: false,
      windowWidth: 0
    }
  },
  computed: {
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
    removeTag (item, index) {
      this.$emit('remove-tag', item, index)
    }
  }
}
</script>

<style lang="scss">
// import variables

.container {
  text-align: left;

  & h1 {
    text-align: center;
  }
}

.section-container {
  margin: 3rem 0;

  & p:first-child {
    margin-bottom: 1rem;
  }
}

</style>
