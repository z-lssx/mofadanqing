<template>
  <view class="detail-container">
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else-if="!product" class="error">商品不存在</view>
    
    <block v-else>
      <!-- 1. 商品大图 -->
      <image 
        :src="product.coverImg || '/static/logo.png'" 
        mode="aspectFill" 
        class="cover-img"
        @click="previewImage(product.coverImg)"
      ></image>
      
      <!-- 2. 基本信息 -->
      <view class="info-card">
        <view class="price-row">
          <text class="price">¥{{ product.price }}</text>
          <text class="sales">已售 {{ product.sales || 0 }}</text>
        </view>
        <view class="title">{{ product.name }}</view>
        <view class="desc">{{ product.description }}</view>
      </view>
      
      <!-- 3. 详情内容 (模拟富文本) -->
      <view class="detail-content">
        <view class="section-title">商品详情</view>
        <view class="text-content">
          这是一件精美的非遗工艺品，采用传统手工技艺制作...
          (这里应展示 product.detail 富文本，小程序需用 rich-text)
        </view>
        <rich-text :nodes="product.detail"></rich-text>
      </view>
      
      <!-- 4. 底部操作栏 -->
      <view class="bottom-bar">
        <view class="icon-btn" @click="goHome">
          <text>🏠</text>
          <text>首页</text>
        </view>
        <view class="icon-btn">
          <text>📞</text>
          <text>客服</text>
        </view>
        
        <view class="btn-group">
          <button class="btn-cart" @click="addToCart">加入购物车</button>
          <button class="btn-buy" @click="buyNow">立即购买</button>
        </view>
      </view>
    </block>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '../../utils/request'

const product = ref(null)
const loading = ref(true)
const id = ref(null)

onLoad((option) => {
  if (option.id) {
    id.value = option.id
    loadDetail(option.id)
  }
})

const loadDetail = async (productId) => {
  loading.value = true
  try {
    const res = await request.get(`/products/${productId}`)
    if (res.code === 200) {
      product.value = res.data
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const previewImage = (url) => {
  if (url) uni.previewImage({ urls: [url] })
}

const goHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

const addToCart = async () => {
  if (!product.value) return
  
  try {
    const res = await request.post('/cart', {
      productId: product.value.id,
      quantity: 1
    })
    
    if (res.code === 200) {
       uni.showToast({ title: '已加入购物车', icon: 'success' })
    } else {
       uni.showToast({ title: '加入失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络异常', icon: 'none' })
  }
}

const buyNow = () => {
  if (!product.value) return
  
  // 1. 构造单个商品作为 checkoutItems
  const item = {
    ...product.value,
    quantity: 1,
    productId: product.value.id
  }
  uni.setStorageSync('checkoutItems', [item])
  
  // 2. 跳转到支付确认页
  uni.navigateTo({ url: '/pages/payment/payment' })
}
</script>

<style lang="scss">
.detail-container {
  padding-bottom: 120rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.cover-img {
  width: 100%;
  height: 600rpx;
  background: #fff;
}

.info-card {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .price-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 15rpx;
    
    .price { color: #e74c3c; font-size: 40rpx; font-weight: bold; }
    .sales { color: #999; font-size: 24rpx; }
  }
  
  .title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 10rpx;
    line-height: 1.4;
  }
  
  .desc {
    font-size: 26rpx;
    color: #666;
    line-height: 1.5;
  }
}

.detail-content {
  background: #fff;
  padding: 30rpx;
  min-height: 300rpx;
  
  .section-title {
    font-size: 30rpx;
    font-weight: bold;
    margin-bottom: 20rpx;
    padding-left: 10rpx;
    border-left: 3px solid #00796b;
  }
  
  .text-content {
    font-size: 28rpx;
    color: #666;
    line-height: 1.6;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 100;
  
  .icon-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100rpx;
    font-size: 20rpx;
    color: #666;
    
    text:first-child { font-size: 36rpx; margin-bottom: 4rpx; }
  }
  
  .btn-group {
    flex: 1;
    display: flex;
    margin-left: 20rpx;
    
    button {
      flex: 1;
      height: 70rpx;
      line-height: 70rpx;
      font-size: 28rpx;
      border-radius: 35rpx;
      margin: 0 10rpx;
      color: #fff;
      
      &.btn-cart { background: #ff9800; }
      &.btn-buy { background: #e74c3c; }
    }
  }
}

.loading, .error {
  text-align: center;
  padding: 100rpx;
  color: #999;
}
</style>
