<template>
  <div>
    <div v-if="isInitialLoading">
      <loading />
    </div>
    <router-view v-else />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import Loading from '@/components/Loading'

export default {
  components: {
    Loading
  },
  computed: {
    ...mapState([
      'isInitialLoading'
    ])
  }
}
</script>

<style lang="scss">
:root {
  --dark: #2e3242;
  --slate: #45555f;
  --gray: #717782;
  --gray400: #8f95a1;
  --blue: #009de0;
  --light-blue: #eef2f5;
  --magenta: #e9374f;
  --orange: #f9690e;
  --green: #199d5b;
}
@import "@/assets/scss/_resets.scss";
@import "@/assets/scss/_fonts.scss";
@import "@/assets/scss/_helpers.scss";
@import "@/assets/scss/_utilities.scss";
@import "@/assets/scss/_icons.scss";
@import "@/assets/scss/_typography.scss";
@import "@/assets/scss/_lists.scss";
@import "@/assets/scss/_cards.scss";
@import "@/assets/scss/_buttons.scss";
@import "@/assets/scss/_labels.scss";
@import "@/assets/scss/_loading.scss";
@import "@/assets/scss/_avatars.scss";
@import "@/assets/scss/_empty-states.scss";
//global component styles
@import "@/assets/scss/_tabs.scss";
@import "@/assets/scss/_forms.scss";
//global layout styles
@import "@/assets/scss/_grid.scss";
@import "@/assets/scss/layouts/_layout-sidebar.scss";
@import "@/assets/scss/layouts/_layout-search-results.scss";
@import "@/assets/scss/layouts/_layout-wizard.scss";
</style>
