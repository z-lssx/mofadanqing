<template>
  <view class="home-container">
    <!-- 1. 顶部轮播图 -->
    <swiper 
      class="hero-swiper" 
      circular 
      autoplay 
      interval="5000" 
      duration="500"
      indicator-dots
      indicator-active-color="#fff"
    >
      <swiper-item v-for="(item, index) in banners" :key="index">
        <image :src="item.image" mode="aspectFill" class="banner-img"></image>
        <view class="hero-text" v-if="index === 0">
          <text class="subtitle">非遗 · 鲁绣 · 数字化</text>
          <text class="title">以发为线 绣藏时光</text>
          <button class="btn-custom" @click="navigateTo('/pages/custom/custom')">开始定制旅程</button>
        </view>
      </swiper-item>
    </swiper>

    <!-- 2. 推荐商品 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">精选商品</text>
        <text class="more-link" @click="switchTab('/pages/products/products')">查看更多 →</text>
      </view>
      
      <view class="product-grid">
        <view 
          v-for="product in products" 
          :key="product.id" 
          class="product-card"
          @click="navigateToProduct(product.id)"
        >
          <image :src="product.coverImg || '/static/placeholder.png'" mode="aspectFill" class="product-img"></image>
          <view class="product-info">
            <text class="product-name">{{ product.name }}</text>
            <text class="product-price">¥{{ product.price }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { onPullDownRefresh } from '@dcloudio/uni-app'

const banners = ref([
  { image: 'https://picsum.photos/800/400?random=1' },
  { image: 'https://picsum.photos/800/400?random=2' }
])

const products = ref([])

const loadProducts = async () => {
  try {
    const res = await request.get('/products', { page: 1, size: 4 })
    if (res.code === 200) {
      products.value = res.data.content || res.data.records || []
    }
  } catch (e) {
    console.error('Load products failed', e)
  }
}

const navigateTo = (url) => {
  uni.navigateTo({ url })
}

const switchTab = (url) => {
  uni.switchTab({ url })
}

const navigateToProduct = (id) => {
  uni.navigateTo({ url: `/pages/products/detail?id=${id}` })
}

onMounted(() => {
  loadProducts()
})

onPullDownRefresh(async () => {
  await loadProducts()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss">
.home-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.hero-swiper {
  height: 400rpx;
  position: relative;
  
  .banner-img {
    width: 100%;
    height: 100%;
  }
  
  .hero-text {
    position: absolute;
    left: 40rpx;
    bottom: 60rpx;
    color: #fff;
    z-index: 10;
    
    .subtitle {
      display: block;
      font-size: 24rpx;
      margin-bottom: 10rpx;
      opacity: 0.9;
      letter-spacing: 2px;
    }
    
    .title {
      display: block;
      font-size: 48rpx;
      font-weight: bold;
      margin-bottom: 30rpx;
      text-shadow: 0 2px 4px rgba(0,0,0,0.3);
    }
    
    .btn-custom {
      display: inline-block;
      background: #e74c3c;
      color: #fff;
      font-size: 28rpx;
      padding: 10rpx 40rpx;
      border-radius: 40rpx;
      border: none;
    }
  }
}

.section {
  padding: 30rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      border-left: 4px solid #00796b;
      padding-left: 10rpx;
    }
    
    .more-link {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.product-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  
  .product-card {
    width: 48%;
    background: #fff;
    border-radius: 12rpx;
    overflow: hidden;
    margin-bottom: 20rpx;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    
    .product-img {
      width: 100%;
      height: 300rpx;
      background: #eee;
    }
    
    .product-info {
      padding: 20rpx;
      
      .product-name {
        display: block;
        font-size: 28rpx;
        color: #333;
        margin-bottom: 10rpx;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .product-price {
        color: #e74c3c;
        font-size: 32rpx;
        font-weight: bold;
      }
    }
  }
}
</style>
