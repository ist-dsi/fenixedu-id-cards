<template>
  <main class="verification-page">
    <div class="layout-centered">
      <div class="verification-page__icon">
        <img
          src="~@/assets/images/icon-error.svg"
          alt="Error icon">
      </div>
      <h1 class="h3 verification-page__title">{{ $t('title.404.page') }}</h1>
      <p>{{ $t('message.error.404') }}</p>
    </div>
  </main>
</template>

<style lang="scss">
  .layout-centered{
    position: absolute;
    left: 50%;
    top: 6rem;
    transform: translate(-50%, 0);
    text-align: center;
    width: 100%;
    max-width: 46rem;
    padding: 1rem;

    @media screen and (min-width: 47.5rem){
      top: 50%;
      transform: translate(-50%, -50%);
    }
  }
</style>
