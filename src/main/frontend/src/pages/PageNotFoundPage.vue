<template>
  <main class="layout-centered">
    <div class="page-not-found__icon">
      <img
        src="~@/assets/images/icon-error.svg"
        alt="Error icon"
      >
    </div>
    <div class="page-not-found__content">
      <h1 class="h3 page-not-found__title">
        {{ $t('header') }}
      </h1>
      <p>{{ $t('description') }}</p>
    </div>
  </main>
</template>

<script>
export default {
  name: 'PageNotFoundPage',
  i18n: {
    messages: {
      pt: {
        header: 'Erro 404',
        description: 'A página que procura não foi encontrada.'
      },
      en: {
        header: 'Error 404',
        description: "The page you're looking for could not be found."
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-centered {
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

.page-not-found__icon {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
}
</style>
