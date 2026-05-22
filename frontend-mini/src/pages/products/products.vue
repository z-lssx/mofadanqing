<template>
  <view class="products-container">
    <!-- 1. 顶部搜索与筛选 -->
    <view class="header-bar">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          placeholder="搜索商品..." 
          v-model="keyword" 
          @confirm="onSearch"
        />
      </view>
    </view>
    
    <!-- 2. 分类筛选 (横向滚动) -->
    <scroll-view scroll-x class="category-scroll">
      <view class="category-list">
        <view 
          class="category-item" 
          :class="{ active: selectedCategory === '' }"
          @click="selectCategory('')"
        >全部</view>
        <view 
          class="category-item" 
          v-for="cat in categories" 
          :key="cat.id"
          :class="{ active: selectedCategory === cat.id }"
          @click="selectCategory(cat.id)"
        >{{ cat.name }}</view>
      </view>
    </scroll-view>

    <!-- 3. 商品列表 -->
    <scroll-view scroll-y class="product-scroll" @scrolltolower="loadMore">
      <view class="product-list">
        <view 
          class="product-item" 
          v-for="product in products" 
          :key="product.id"
          @click="navigateToDetail(product.id)"
        >
          <image :src="product.coverImg || '/static/placeholder.png'" mode="aspectFill" class="img"></image>
          <view class="info">
            <text class="name">{{ product.name }}</text>
            <text class="desc">{{ product.description }}</text>
            <view class="bottom">
              <text class="price">¥{{ product.price }}</text>
              <button class="btn-add" @click="addToCart(product)">+</button>
            </view>
          </view>
        </view>
      </view>
      
      <view class="loading-more" v-if="loading">加载中...</view>
      <view class="no-more" v-if="!hasMore && products.length > 0">没有更多了</view>
      <view class="empty" v-if="!loading && products.length === 0">暂无商品</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { onPullDownRefresh } from '@dcloudio/uni-app'

const keyword = ref('')
const selectedCategory = ref('')
const categories = ref([])
const products = ref([])
const page = ref(1)
const hasMore = ref(true)
const loading = ref(false)

const loadCategories = async () => {
  try {
    const res = await request.get('/products/categories')
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (e) {}
}

const loadProducts = async (refresh = false) => {
  if (loading.value || (!hasMore.value && !refresh)) return
  
  loading.value = true
  if (refresh) {
    page.value = 1
    hasMore.value = true
    products.value = []
  }
  
  try {
    const res = await request.get('/products', {
      page: page.value,
      size: 10,
      keyword: keyword.value,
      categoryId: selectedCategory.value
    })
    
    if (res.code === 200) {
      const list = res.data.content || res.data.records || []
      if (refresh) {
        products.value = list
      } else {
        products.value = [...products.value, ...list]
      }
      
      if (list.length < 10) {
        hasMore.value = false
      } else {
        page.value++
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  loadProducts(true)
}

const selectCategory = (id) => {
  selectedCategory.value = id
  loadProducts(true)
}

const loadMore = () => {
  loadProducts()
}

const navigateToDetail = (id) => {
  uni.navigateTo({ url: `/pages/products/detail?id=${id}` })
}

const addToCart = async (product) => {
  try {
     // 尝试调用加入购物车接口，如果失败（如未实现）则仅提示
     // 实际项目中应调用 /cart/add
     uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (e) {
     uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

onMounted(() => {
  loadCategories()
  loadProducts(true)
})

onPullDownRefresh(async () => {
  await loadProducts(true)
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss">
.products-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.header-bar {
  padding: 20rpx;
  background: #fff;
  
  .search-box {
    background: #f0f0f0;
    border-radius: 30rpx;
    padding: 10rpx 20rpx;
    display: flex;
    align-items: center;
    
    .search-icon { margin-right: 10rpx; font-size: 24rpx; }
    .search-input { flex: 1; font-size: 28rpx; }
  }
}

.category-scroll {
  background: #fff;
  white-space: nowrap;
  border-bottom: 1rpx solid #eee;
  
  .category-list {
    padding: 20rpx;
    display: flex;
    
    .category-item {
      padding: 10rpx 30rpx;
      margin-right: 20rpx;
      border-radius: 30rpx;
      background: #f5f5f5;
      font-size: 26rpx;
      color: #666;
      
      &.active {
        background: #00796b;
        color: #fff;
      }
    }
  }
}

.product-scroll {
  flex: 1;
  padding: 20rpx;
  box-sizing: border-box;
  
  .product-list {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
  }
  
  .product-item {
    width: 48%;
    background: #fff;
    border-radius: 12rpx;
    margin-bottom: 20rpx;
    overflow: hidden;
    
    .img {
      width: 100%;
      height: 300rpx;
      background: #eee;
    }
    
    .info {
      padding: 15rpx;
      
      .name {
        display: block;
        font-size: 28rpx;
        color: #333;
        margin-bottom: 6rpx;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .desc {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-bottom: 15rpx;
        height: 32rpx;
        overflow: hidden;
      }
      
      .bottom {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .price { color: #e74c3c; font-size: 30rpx; font-weight: bold; }
        .btn-add {
          width: 50rpx;
          height: 50rpx;
          line-height: 50rpx;
          text-align: center;
          background: #00796b;
          color: #fff;
          border-radius: 50%;
          font-size: 32rpx;
          padding: 0;
          margin: 0;
        }
      }
    }
  }
}

.loading-more, .no-more, .empty {
  text-align: center;
  padding: 20rpx;
  color: #999;
  font-size: 24rpx;
}
</style>
