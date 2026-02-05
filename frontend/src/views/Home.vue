<template>
  <div class="home">
    <!-- 1. 首页 -->
    <section id="page-home" class="section active">
      <div class="container">
        <!-- Hero -->
        <div class="hero-wrapper">
          <div class="hero-text">
            <span style="color: var(--c-primary); font-weight: 600; letter-spacing: 2px; display: block; margin-bottom: 10px;">非遗 · 鲁绣 · 数字化</span>
            <h1>以发为线<br>绣藏时光</h1>
            <p>连接城市情感与乡村匠心。通过 AI 赋能与 3D 渲染，将您的珍贵记忆转化为独一无二的非遗艺术品。</p>
            <div style="display: flex; gap: 20px;">
              <button class="btn btn-red" @click="navigateTo('/custom')">开始定制旅程</button>
              <button class="btn btn-outline">观看纪录片 ▶</button>
            </div>
          </div>
          <div class="hero-visual">
            <img src="https://images.unsplash.com/photo-1627921319349-36655c65b214?q=80&w=1200" class="hero-img" alt="鲁绣展示">
            <div class="float-card">
              <img src="https://images.unsplash.com/photo-1544212727-4a11f2a33333?q=80&w=100" style="width: 50px; height: 50px; border-radius: 50%; object-fit: cover;">
              <div>
                <div style="font-weight: 700;">大师手作</div>
                <div style="font-size: 12px; color: var(--c-gray);">传承人李绣娘 · 监制</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 动态商品列表 -->
        <div class="product-grid">
          <div v-for="product in products" :key="product.id" class="product-card">
            <img :src="product.coverImg || 'https://via.placeholder.com/300x200'" :alt="product.name" />
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="price">¥{{ product.price }}</p>
              <p class="description">{{ product.description }}</p>
              <div class="actions">
                <button @click="viewProduct(product.id)" class="btn-view">查看详情</button>
                <button @click="addToCart(product)" class="btn-cart">加入购物车</button>
              </div>
            </div>
          </div>
        </div>
        <div class="home-actions">
          <button class="btn" @click="navigateTo('/products')">查看更多商品</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import axios from '../utils/axios'
import { cartState } from '../utils/cartState'

const router = useRouter()
const products = ref([])

const navigateTo = (path) => {
  router.push(path)
}

const loadProducts = async () => {
  try {
    const res = await axios.get('/products', { params: { page: 1, size: 6 } })
    if (res.code === 200) {
      const pageData = res.data || {}
      products.value = pageData.content || pageData.records || []
    }
  } catch (e) {
    console.error('加载商品失败', e)
  }
}

const viewProduct = (id) => {
  router.push(`/product/${id}`)
}

const addToCart = (product) => {
  cartState.addItem(product)
}

onMounted(loadProducts)
</script>

<style scoped>
.home {
  min-height: 100vh;
  background-image: url('/image/网页背景图.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  background: white;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.product-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info { padding: 15px; }
.price { font-size: 18px; font-weight: bold; color: #e74c3c; }
.description { font-size: 14px; color: #666; margin: 10px 0; height: 40px; overflow: hidden; }
.actions { display: flex; gap: 10px; margin-top: 10px; }
.btn-view { background-color: #3498db; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; transition: all 0.3s; }
.btn-view:hover { background-color: #2980b9; transform: translateY(-2px); }
.btn-cart { background-color: #2ecc71; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; transition: all 0.3s; }
.btn-cart:hover { background-color: #27ae60; transform: translateY(-2px); }
.home-actions { display: flex; justify-content: center; margin-top: 20px; }
</style>
