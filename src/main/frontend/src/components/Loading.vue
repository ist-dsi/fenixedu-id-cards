<template>
  <div class="loading-animation">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink"
      width="225"
      height="225"
    >
      <defs>
        <circle
          id="a"
          cx="113"
          cy="113"
          r="103"
        />
      </defs>
      <g
        fill="none"
        fill-rule="evenodd"
      >
        <mask
          id="b"
          fill="#fff"
        >
          <use xlink:href="#a" />
        </mask>
        <use
          fill="#0076DC"
          xlink:href="#a"
        />
        <g
          fill="#04080D"
          mask="url(#b)"
          opacity=".16"
        >
          <path d="M214.504672 130.5857928c-7.085416 41.1874254-38.629414 74.0109132-79.141212 82.9796822L66 142h18.5230769L38 94h147l29.504672 36.5857928z" />
        </g>
        <g mask="url(#b)">
          <path
            fill="#004B9E"
            d="M66 84h89v58c-11.845703 7.7480469-26.6790365 11.6220703-44.5 11.6220703S77.8457031 149.7480469 66 142V84z"
          />
          <path
            fill="#00428E"
            d="M111 69l74 24.5-74 28.5-74-28.5L111 69z"
          />
          <path
            fill="#008DE0"
            d="M111 69v49L37 93.5z"
          />
          <path
            fill="#0056B6"
            d="M111 69v49l74-24.5z"
          />
          <path
            fill="#DDE4E9"
            d="M148.492476 153.4612221h-.173862V99.89727L111 89.8977775V87l39.807268 10.4612221h.511346v.1343802l.060802.0159785-.060802.2269153v55.622726h-.21109L158.3 167h-17l7.192476-13.5387779z"
          />
        </g>
      </g>
    </svg>

    <div class="loading-bar">
      <div class="blue-bar" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'Loading'
}
</script>

<style lang="scss">
  div.loading-animation {
    display: flex;
    height: 100vh;
    justify-content: center;
    flex-direction: column;
    align-items: center;
    img {
      margin-top: -7.5rem;
      width: 15rem;
      height: 15rem;
    }
  }
</style>
