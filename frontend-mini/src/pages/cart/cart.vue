<template>
  <view class="cart-container">
    <view v-if="items.length === 0" class="empty-state">
      <text class="icon">🛒</text>
      <text class="text">购物车还是空的</text>
      <button class="btn-go" @click="goShopping">去逛逛</button>
    </view>
    
    <view v-else class="cart-content">
      <scroll-view scroll-y class="item-list">
        <view class="cart-item" v-for="(item, index) in items" :key="item.id">
          <!-- 选中框 (暂简化为全部选中) -->
          <view class="checkbox checked">✓</view>
          
          <!-- 修正字段名: item.productImage，并使用存在的 logo.png 作为兜底 -->
          <image :src="item.productImage || '/static/logo.png'" mode="aspectFill" class="thumb"></image>
          
          <view class="info">
            <text class="title">{{ item.name }}</text>
            <view class="price-row">
              <text class="price">¥{{ item.price }}</text>
              <view class="qty-control">
                <view class="btn" @click="updateQty(item, -1)">-</view>
                <text class="num">{{ item.quantity }}</text>
                <view class="btn" @click="updateQty(item, 1)">+</view>
              </view>
            </view>
          </view>
          
          <view class="delete" @click="removeItem(item)">×</view>
        </view>
      </scroll-view>
      
      <!-- 底部结算栏 -->
      <view class="footer-bar">
        <view class="total-info">
          <text>合计:</text>
          <text class="total-price">¥{{ totalPrice.toFixed(2) }}</text>
        </view>
        <button class="btn-checkout" @click="startCheckout">去结算 ({{ totalCount }})</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import request from '../../utils/request'

const items = ref([])
const loading = ref(false)

const loadCart = async () => {
  loading.value = true
  try {
    // 强制不缓存
    const res = await request.get('/cart?_t=' + Date.now())
    if (res.code === 200) {
      items.value = res.data
      
      // 更新 TabBar 徽标（如果有的话）
      if (items.value.length > 0) {
         uni.setTabBarBadge({ index: 2, text: String(totalCount.value) }).catch(() => {})
      } else {
         uni.removeTabBarBadge({ index: 2 }).catch(() => {})
      }
    }
  } catch (e) {
    console.error('Load cart failed', e)
  } finally {
    loading.value = false
  }
}

const updateQty = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  
  try {
    const res = await request.put(`/cart/${item.id}`, { quantity: newQty })
    if (res.code === 200) {
      item.quantity = newQty
    }
  } catch (e) {
    uni.showToast({ title: '更新失败', icon: 'none' })
  }
}

const removeItem = (item) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
           const delRes = await request.delete(`/cart/${item.id}`)
           if (delRes.code === 200) {
             items.value = items.value.filter(i => i.id !== item.id)
             // 更新 TabBar
             if (items.value.length === 0) {
                 uni.removeTabBarBadge({ index: 2 }).catch(() => {})
             } else {
                 uni.setTabBarBadge({ index: 2, text: String(totalCount.value) }).catch(() => {})
             }
           }
        } catch (e) {
           uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 移除本地模拟的 saveCart
// const saveCart = () => { ... }

const totalPrice = computed(() => {
  return items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const totalCount = computed(() => {
  return items.value.reduce((sum, item) => sum + item.quantity, 0)
})

const goShopping = () => {
  uni.switchTab({ url: '/pages/products/products' })
}

const startCheckout = () => {
  if (items.value.length === 0) return
  
  // 1. 将选中的商品（这里简化为全部）存入本地缓存，供 Payment 页面读取
  uni.setStorageSync('checkoutItems', items.value)
  
  // 2. 跳转到支付确认页
  uni.navigateTo({ url: '/pages/payment/payment' })
}

// 移除 submitOrder，因为逻辑移到了 Payment 页面
// const submitOrder = async (address) => { ... }

onShow(() => {
  loadCart()
})

onPullDownRefresh(async () => {
  await loadCart()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss">
.cart-container {
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  
  .icon { font-size: 80rpx; margin-bottom: 20rpx; }
  .btn-go {
    margin-top: 40rpx;
    background: #00796b;
    color: #fff;
    font-size: 28rpx;
    padding: 10rpx 40rpx;
    border-radius: 40rpx;
  }
}

.cart-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.item-list {
  flex: 1;
  padding: 20rpx;
  padding-bottom: 120rpx; // Space for footer
}

.cart-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  position: relative;
  
  .checkbox {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    border: 1px solid #ddd;
    margin-right: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    color: transparent;
    
    &.checked {
      background: #00796b;
      border-color: #00796b;
      color: #fff;
      font-size: 24rpx;
    }
  }
  
  .thumb {
    width: 140rpx;
    height: 140rpx;
    border-radius: 8rpx;
    background: #eee;
    margin-right: 20rpx;
  }
  
  .info {
    flex: 1;
    height: 140rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .title { font-size: 28rpx; color: #333; }
    
    .price-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .price { color: #e74c3c; font-weight: bold; }
      
      .qty-control {
        display: flex;
        align-items: center;
        border: 1px solid #eee;
        border-radius: 4rpx;
        
        .btn {
          width: 50rpx;
          height: 50rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #f9f9f9;
          font-size: 32rpx;
        }
        
        .num {
          width: 60rpx;
          text-align: center;
          font-size: 26rpx;
          border-left: 1px solid #eee;
          border-right: 1px solid #eee;
        }
      }
    }
  }
  
  .delete {
    position: absolute;
    top: 10rpx;
    right: 10rpx;
    padding: 10rpx;
    color: #ccc;
    font-size: 32rpx;
  }
}

.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 100;
  
  .total-info {
    font-size: 28rpx;
    color: #333;
    
    .total-price {
      color: #e74c3c;
      font-weight: bold;
      font-size: 36rpx;
      margin-left: 10rpx;
    }
  }
  
  .btn-checkout {
    background: #e74c3c;
    color: #fff;
    font-size: 30rpx;
    padding: 0 60rpx;
    height: 70rpx;
    line-height: 70rpx;
    border-radius: 35rpx;
    margin: 0;
  }
}
</style>
