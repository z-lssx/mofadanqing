<template>
  <div class="not-found">
    <div class="container">
      <h2>404</h2>
      <h3>页面未找到</h3>
      <p>抱歉，您访问的页面不存在或已被移除</p>
      <button class="btn btn-primary" @click="goHome">返回首页</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--c-paper);
  background-image: url('../assets/网页背景图.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.container {
  text-align: center;
  background: var(--glass);
  backdrop-filter: blur(20px);
  border: var(--glass-border);
  border-radius: 12px;
  padding: 4rem 2rem;
  box-shadow: var(--shadow-hover);
  max-width: 500px;
}

h2 {
  font-size: 4rem;
  margin-bottom: 1rem;
  color: var(--c-primary);
  font-family: 'Noto Serif SC', serif;
}

h3 {
  font-size: 1.8rem;
  margin-bottom: 1rem;
  color: var(--c-ink);
  font-family: 'Noto Serif SC', serif;
}

p {
  color: var(--c-gray);
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

@media (max-width: 480px) {
  .container {
    padding: 3rem 1.5rem;
    margin: 0 1rem;
  }
  
  h2 {
    font-size: 3rem;
  }
  
  h3 {
    font-size: 1.5rem;
  }
}
</style>
