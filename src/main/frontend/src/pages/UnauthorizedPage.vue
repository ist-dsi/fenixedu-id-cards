<template>
  <main class="layout-centered">
    <h1>{{ $t('header') }}</h1>
    <p>{{ $t('description') }}</p>
  </main>
</template>

<script>
export default {
  name: 'UnauthorizedPage',
  i18n: {
    messages: {
      pt: {
        header: 'Utilizador não autorizado',
        description: 'De momento, não tem acesso a este serviço.'
      },
      en: {
        header: 'Unauthorized user',
        description: 'You do not have access to this service.'
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
</style>
