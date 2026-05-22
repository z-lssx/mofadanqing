<template>
  <view class="orders-container">
    <!-- 1. 顶部 Tab -->
    <view class="tabs">
      <view 
        v-for="tab in tabs" 
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </view>
    </view>
    
    <!-- 2. 订单列表 -->
    <scroll-view scroll-y class="order-list" @scrolltolower="loadMore">
      <view v-if="loading && orders.length === 0" class="loading-state">加载中...</view>
      
      <view v-else-if="orders.length === 0" class="empty-state">
        <text class="empty-icon">📭</text>
        <text>暂无订单</text>
        <button class="btn-go" @click="goShopping">去逛逛</button>
      </view>
      
      <view 
        v-else 
        class="order-card" 
        v-for="order in orders" 
        :key="order.id"
        @click="goToTrace(order.id)"
      >
        <view class="card-header">
          <text class="order-no">NO. {{ order.orderNo }}</text>
          <text class="status">{{ getStatusText(order.status) }}</text>
        </view>
        
        <view class="card-body">
          <image :src="order.items?.[0]?.productImage || '/static/placeholder.png'" mode="aspectFill" class="thumb"></image>
          <view class="info">
            <text class="name">{{ order.items?.[0]?.productName }}</text>
            <text class="specs" v-if="order.items?.length > 1">等 {{ order.items.length }} 件商品</text>
            <text class="price">¥{{ order.totalAmount }}</text>
          </view>
        </view>
        
        <view class="card-footer">
           <button class="btn-outline" @click.stop="goToTrace(order.id)">查看溯源</button>
           <button 
             class="btn-primary" 
             v-if="order.status === 'PAID' && (!order.items?.[0]?.c2mStatus || order.items[0].c2mStatus === 'wait')"
             @click.stop="startCustom(order.id)"
           >开始定制</button>
        </view>
      </view>
      
      <view class="no-more" v-if="!hasMore && orders.length > 0">没有更多了</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import request from '../../utils/request'

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'processing', label: '进行中' },
  { key: 'completed', label: '已完成' }
]
const activeTab = ref('all')
const orders = ref([])
const loading = ref(false)
const hasMore = ref(false) // 暂不分页，简单全量

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/orders')
    if (res.code === 200) {
      let allOrders = res.data.records || []
      // 客户端筛选
      if (activeTab.value === 'processing') {
        allOrders = allOrders.filter(o => ['PENDING', 'PAID', 'SHIPPED'].includes(o.status))
      } else if (activeTab.value === 'completed') {
        allOrders = allOrders.filter(o => ['COMPLETED', 'CANCELLED'].includes(o.status))
      }
      orders.value = allOrders
    }
  } catch (e) {
    console.error(e)
    // 如果未登录 (request.js 会自动跳转，但这里最好也处理一下)
    if (e.message === 'Unauthorized') {
      // 已经在 request.js 处理跳转
    }
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

onPullDownRefresh(() => {
  loadOrders()
})

const switchTab = (key) => {
  activeTab.value = key
  loadOrders()
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待支付',
    PAID: '制作中',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const goShopping = () => {
  uni.switchTab({ url: '/pages/products/products' })
}

const goToTrace = (id) => {
  uni.navigateTo({ url: `/pages/trace/trace?id=${id}` })
}

const startCustom = (id) => {
  uni.navigateTo({ url: `/pages/custom/custom?orderId=${id}` })
}

onShow(() => {
  loadOrders()
})
</script>

<style lang="scss">
.orders-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.tabs {
  display: flex;
  background: #fff;
  padding: 20rpx;
  justify-content: space-around;
  border-bottom: 1rpx solid #eee;
  
  .tab-item {
    font-size: 28rpx;
    color: #666;
    padding-bottom: 10rpx;
    border-bottom: 2px solid transparent;
    
    &.active {
      color: #00796b;
      border-color: #00796b;
      font-weight: bold;
    }
  }
}

.order-list {
  flex: 1;
  padding: 20rpx;
  box-sizing: border-box;
}

.order-card {
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  padding: 20rpx;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    margin-bottom: 20rpx;
    border-bottom: 1rpx solid #f5f5f5;
    padding-bottom: 10rpx;
    
    .order-no { color: #999; }
    .status { color: #00796b; font-weight: bold; }
  }
  
  .card-body {
    display: flex;
    margin-bottom: 20rpx;
    
    .thumb {
      width: 120rpx;
      height: 120rpx;
      border-radius: 8rpx;
      background: #eee;
      margin-right: 20rpx;
    }
    
    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      .name { font-size: 28rpx; color: #333; }
      .specs { font-size: 22rpx; color: #999; }
      .price { font-size: 30rpx; color: #333; font-weight: bold; }
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 20rpx;
    
    button {
      margin: 0;
      font-size: 24rpx;
      padding: 0 30rpx;
      height: 60rpx;
      line-height: 60rpx;
      border-radius: 30rpx;
    }
    
    .btn-outline {
      background: #fff;
      border: 1rpx solid #ccc;
      color: #666;
    }
    
    .btn-primary {
      background: #00796b;
      color: #fff;
      border: none;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 100rpx;
  color: #999;
  
  .empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
  .btn-go { margin-top: 40rpx; font-size: 28rpx; }
}

.loading-state, .no-more {
  text-align: center;
  padding: 20rpx;
  color: #999;
  font-size: 24rpx;
}
</style>
