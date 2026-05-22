<template>
  <view class="payment-container">
    <!-- 1. 地址选择 -->
    <view class="address-card" @click="chooseAddress">
      <view class="info" v-if="address">
        <view class="row1">
          <text class="name">{{ address.userName }}</text>
          <text class="tel">{{ address.telNumber }}</text>
        </view>
        <view class="row2">{{ address.provinceName }}{{ address.cityName }}{{ address.countyName }} {{ address.detailInfo }}</view>
      </view>
      <view class="empty" v-else>
        <text class="icon">+</text>
        <text>请选择收货地址</text>
      </view>
      <text class="arrow">></text>
    </view>
    
    <!-- 2. 商品清单 -->
    <view class="goods-card">
      <view class="goods-item" v-for="(item, index) in items" :key="index">
        <image :src="item.productImage || item.coverImg || '/static/logo.png'" mode="aspectFill" class="thumb"></image>
        <view class="info">
          <text class="title">{{ item.name || item.productName }}</text>
          <text class="sku" v-if="item.sku">{{ item.sku }}</text>
          <view class="price-row">
            <text class="price">¥{{ item.price }}</text>
            <text class="qty">x{{ item.quantity }}</text>
          </view>
        </view>
      </view>
      
      <view class="cell-row">
        <text class="label">配送方式</text>
        <text class="value">普通快递 免邮</text>
      </view>
      <view class="cell-row">
        <text class="label">买家留言</text>
        <input class="input" placeholder="无" v-model="remark" />
      </view>
      <view class="total-row">
        <text>共 {{ totalCount }} 件，小计：</text>
        <text class="total-price">¥{{ totalPrice.toFixed(2) }}</text>
      </view>
    </view>
    
    <!-- 3. 底部支付栏 -->
    <view class="footer-bar">
      <view class="total-info">
        <text>合计:</text>
        <text class="final-price">¥{{ totalPrice.toFixed(2) }}</text>
      </view>
      <button class="btn-pay" @click="pay" :disabled="loading">立即支付</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'
import request from '../../utils/request'

const address = ref(null)
const items = ref([])
const remark = ref('')
const loading = ref(false)

onLoad(() => {
  // 获取待支付商品
  const checkoutItems = uni.getStorageSync('checkoutItems')
  if (checkoutItems) {
    items.value = checkoutItems
  }
})

onShow(() => {
  // 获取选中的地址
  const selected = uni.getStorageSync('selectedAddress')
  if (selected) {
    address.value = selected
    // 清除选中状态，避免下次进来还是它（可选）
    // uni.removeStorageSync('selectedAddress')
  } else {
    // 如果没有选中，尝试获取默认地址
    const list = uni.getStorageSync('addressList') || []
    const def = list.find(i => i.isDefault) || list[0]
    if (def) address.value = def
  }
})

const totalCount = computed(() => {
  return items.value.reduce((sum, i) => sum + i.quantity, 0)
})

const totalPrice = computed(() => {
  return items.value.reduce((sum, i) => sum + i.price * i.quantity, 0)
})

const chooseAddress = () => {
  uni.navigateTo({ url: '/pages/address/address?type=select' })
}

const pay = async () => {
  if (!address.value) {
    uni.showToast({ title: '请选择收货地址', icon: 'none' })
    return
  }
  
  loading.value = true
  uni.showLoading({ title: '支付中...' })
  
  try {
    const payload = {
      items: items.value.map(i => ({ 
        productId: i.productId || i.id, // 兼容 CartItem 和 Product
        quantity: i.quantity 
      })),
      shippingAddress: `${address.value.provinceName}${address.value.cityName}${address.value.countyName} ${address.value.detailInfo} ${address.value.userName} ${address.value.telNumber}`,
      paymentMethod: 'WECHAT',
      remark: remark.value
    }
    
    const res = await request.post('/orders/create', payload)
    
    if (res.code === 200) {
      uni.showToast({ title: '支付成功' })
      
      // 清空购物车对应项（如果是从购物车来的）
      // 这里简单清空所有 checked 的，实际上应该根据 id 匹配
      // 为简化，假设 checkoutItems 来自购物车，则全部清空购物车（演示逻辑）
      if (items.value[0].cartId) { // 假设有 cartId 字段标记
         // 调用清空接口
      }
      // 或者直接通知购物车刷新
      
      setTimeout(() => {
        uni.switchTab({ url: '/pages/orders/orders' })
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '支付失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络异常', icon: 'none' })
  } finally {
    loading.value = false
    uni.hideLoading()
  }
}
</script>

<style lang="scss">
.payment-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 120rpx;
}

.address-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .info {
    flex: 1;
    .row1 {
      font-size: 30rpx;
      font-weight: bold;
      margin-bottom: 10rpx;
      .tel { margin-left: 20rpx; font-weight: normal; font-size: 28rpx; }
    }
    .row2 {
      font-size: 26rpx;
      color: #666;
      line-height: 1.4;
    }
  }
  
  .empty {
    display: flex;
    align-items: center;
    color: #333;
    font-size: 30rpx;
    .icon {
      width: 40rpx;
      height: 40rpx;
      background: #00796b;
      color: #fff;
      border-radius: 4rpx;
      text-align: center;
      line-height: 40rpx;
      margin-right: 20rpx;
    }
  }
  
  .arrow { color: #ccc; margin-left: 20rpx; }
}

.goods-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  
  .goods-item {
    display: flex;
    margin-bottom: 20rpx;
    background: #fafafa;
    padding: 10rpx;
    border-radius: 8rpx;
    
    .thumb {
      width: 120rpx;
      height: 120rpx;
      background: #eee;
      margin-right: 20rpx;
      border-radius: 8rpx;
    }
    
    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      .title { font-size: 28rpx; color: #333; }
      .sku { font-size: 24rpx; color: #999; }
      
      .price-row {
        display: flex;
        justify-content: space-between;
        .price { color: #333; font-weight: bold; }
        .qty { color: #999; }
      }
    }
  }
  
  .cell-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    font-size: 28rpx;
    
    .label { color: #333; }
    .value { color: #666; }
    .input { text-align: right; color: #333; flex: 1; margin-left: 20rpx; }
  }
  
  .total-row {
    padding-top: 20rpx;
    text-align: right;
    font-size: 28rpx;
    .total-price { color: #e74c3c; font-weight: bold; font-size: 32rpx; }
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
  
  .total-info {
    font-size: 28rpx;
    color: #333;
    .final-price { color: #e74c3c; font-weight: bold; font-size: 36rpx; margin-left: 10rpx; }
  }
  
  .btn-pay {
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