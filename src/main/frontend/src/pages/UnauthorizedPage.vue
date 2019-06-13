<template>
  <main class="unauthorized-page layout-centered">
    <h1>{{ $t('title.unauthorized') }}</h1>
    <p>{{ $t('message.error.unauthorized') }}</p>
  </main>
</template>
<style lang="scss">
.unauthorized-page.layout-centered {
  position: absolute;
  left: 50%;
  top: 6rem;
  transform: translate(-50%, 0);
  text-align: center;
  width: 100%;
  max-width: 46rem;
  padding: 1rem;
  @media screen and (min-width: 47.5rem) {
    top: 50%;
    transform: translate(-50%, -50%);
  }
}
</style>
