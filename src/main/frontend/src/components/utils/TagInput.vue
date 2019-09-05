<template>
  <div
    class="f-tag-field">
    <p
      v-if="value !== ''"
      class="f-tag-field__label">{{ value }}</p>
    <div
      v-for="(tag, i) in tags"
      :key="i"
      class="f-tag-field__tag">
      {{ tag.value }}
      <button
        v-if="!tag.disableRemoveAction"
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
      <div
        v-else
        :style="{ width: '14px', height: '14px' }" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'CardPreviewPage',
  components: {},
  props: {
    value: {
      type: String,
      required: false,
      default: ''
    },
    tags: {
      type: Array,
      required: true
    }
  },
  methods: {
    removeTag (item, index) {
      this.$emit('remove-tag', item, index)
    }
  }
}
</script>
