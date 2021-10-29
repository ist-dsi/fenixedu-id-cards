<template>
  <figure
    :class="{ 'figure--icon': icon }"
    class="figure figure--avatar"
  >
    <img
      v-if="src"
      :src="src"
      :alt="name"
    >
    <div v-if="!icon">
      {{ name.charAt(0) }}
    </div>
  </figure>
</template>

<script>
export default {
  props: {
    src: {
      type: String,
      required: false,
      default: undefined
    },
    icon: {
      type: Boolean,
      default: false
    },
    name: {
      type: String,
      required: true
    }
  }
}
</script>

<style lang="scss">
.profile--left {
  display: flex;
  flex-direction: row;
}

.profile__avatar {
  display: inline-block;
  width: 5rem;
  height: 5rem;

  img.avatar {
    border-radius: 50%;
    width: inherit;
    height: inherit;
  }
}
</style>
