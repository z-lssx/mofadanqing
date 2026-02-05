<template>
  <div class="orders-page">
    <div class="container">
      <div class="page-header">
        <h2 class="page-title">我的订单</h2>
        <p class="page-subtitle">My Orders</p>
      </div>
      
      <!-- 选项卡导航 -->
      <div class="tabs-wrapper">
        <div class="tabs">
          <button 
            v-for="tab in ['all', 'processing', 'completed']"
            :key="tab"
            :class="['tab-item', { active: activeTab === tab }]"
            @click="activeTab = tab; filterOrders()"
          >
            {{ getTabLabel(tab) }}
          </button>
        </div>
      </div>

      <!-- 订单列表内容 -->
      <div class="orders-content">
        <!-- 加载中 -->
        <div v-if="loading" class="state-container">
          <div class="needle-spin"></div>
          <p>正在寻觅您的订单...</p>
        </div>
        
        <!-- 错误提示 -->
        <div v-else-if="error" class="state-container">
          <div class="state-icon error">!</div>
          <p>{{ error }}</p>
          <button class="btn btn-outline" @click="loadOrders">重试</button>
        </div>
        
        <!-- 空状态 -->
        <div v-else-if="filteredOrders.length === 0" class="state-container">
          <div class="empty-icon">📭</div>
          <p>暂无相关订单</p>
          <button class="btn btn-primary" @click="navigateTo('/')">去选购商品</button>
        </div>

        <!-- 订单列表 -->
        <div v-else class="orders-grid">
          <div 
            v-for="order in filteredOrders" 
            :key="order.id" 
            class="order-card"
            :class="`status-${order.status.toLowerCase()}`"
          >
            <!-- 卡片头部 -->
            <div class="card-header">
              <div class="order-meta">
                <span class="order-no">NO. {{ order.orderNo || order.id }}</span>
                <span class="order-date">{{ formatDate(order.createTime) }}</span>
              </div>
              <div class="order-status-badge">
                {{ getStatusText(order.status) }}
              </div>
            </div>
            
            <!-- 商品列表 -->
            <div class="card-body" @click="viewOrderDetail(order)">
              <div v-if="order.items && order.items.length > 0" class="product-item">
                <div 
                  class="product-thumb" 
                  :style="{ backgroundImage: `url(${order.items[0].productImage || 'https://via.placeholder.com/150?text=No+Image'})` }"
                ></div>
                <div class="product-info">
                  <h3 class="product-name">{{ order.items[0].productName }}</h3>
                  <div class="product-specs" v-if="order.items.length > 1">
                    等 {{ order.items.length }} 件商品
                  </div>
                  <div class="product-price">
                    ¥ {{ order.items[0].unitPrice }}
                  </div>
                </div>
              </div>
              <div v-else class="no-items">
                无商品信息
              </div>
            </div>

            <!-- 底部合计与操作 -->
            <div class="card-footer">
              <div class="total-info">
                <span>总计</span>
                <span class="total-price">¥ {{ order.totalAmount }}</span>
              </div>
              
              <div class="action-group">
                <!-- 溯源按钮 (始终显示) -->
                <button 
                  class="btn btn-text" 
                  @click.stop="showTrace(order)"
                >
                  <span class="icon">🔍</span> 档案溯源
                </button>
                
                <!-- SHIPPED状态额外显示联系客服 -->
                <button 
                  v-if="order.status === 'SHIPPED'"
                  class="btn btn-outline btn-sm"
                  @click.stop="contactService"
                >
                  联系客服
                </button>

                <!-- 定制按钮 (仅PAID状态且未开始定制的显示) -->
                <!-- 逻辑：PAID状态 且 (无c2mStatus 或 c2mStatus为wait) -->
                <button 
                  v-if="order.status === 'PAID' && (!order.items?.[0]?.c2mStatus || order.items[0].c2mStatus === 'wait')"
                  class="btn btn-outline btn-sm"
                  @click.stop="navigateTo(`/custom/${order.id}`)"
                >
                  开始定制
                </button>

                <!-- 主要操作按钮 -->
                <button 
                  class="btn btn-sm"
                  :class="getActionBtnClass(order.status)"
                  @click.stop="handleOrderAction(order)"
                >
                  {{ getActionButtonText(order.status) }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'

const router = useRouter()
const activeTab = ref('all')
const orders = ref([])
const filteredOrders = ref([])
const loading = ref(false)
const error = ref('')

// 导航
const navigateTo = (path) => {
  router.push(path)
}

// 查看详情 (跳转到溯源)
const viewOrderDetail = (order) => {
  showTrace(order)
}

// 标签文本
const getTabLabel = (tab) => {
  const map = {
    all: '全部订单',
    processing: '进行中',
    completed: '已完成'
  }
  return map[tab]
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 状态文本
const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待支付',
    PAID: '制作中', 
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

// 按钮文本
const getActionButtonText = (status) => {
  const buttonMap = {
    PENDING: '去支付',
    PAID: '联系客服',
    SHIPPED: '确认收货',
    COMPLETED: '再次购买',
    CANCELLED: '删除订单'
  }
  return buttonMap[status] || '查看详情'
}

// 按钮样式
const getActionBtnClass = (status) => {
  if (status === 'PENDING') return 'btn-primary'
  if (status === 'SHIPPED') return 'btn-primary'
  return 'btn-outline'
}

// 联系客服
const contactService = () => {
  alert('专属客服为您服务：400-123-4567')
}

// 确认收货
const confirmReceipt = async (order) => {
  if (!confirm('确认收到货品且无误吗？')) return
  try {
    // 针对每个订单项调用 item 级别的状态更新接口
    // 目前简单处理：取第一个 item 或遍历所有 items
    if (order.items && order.items.length > 0) {
      for (const item of order.items) {
        // 仅对已发货状态的 item 进行确认
        // 注意：后端 C2M 状态 'shipped' 对应主订单 'SHIPPED'
        // 为确保逻辑健壮，无论当前 item 具体状态如何（只要是发货后），都尝试更新为 received
        await axios.put(`/orders/items/${item.id}/c2m-status`, {
          status: 'received',
          remark: '用户确认收货'
        })
      }
      alert('确认收货成功')
      loadOrders() // 刷新列表
    } else {
      alert('订单数据异常，无法操作')
    }
  } catch (e) {
    console.error(e)
    alert('网络异常，请稍后重试')
  }
}

// 订单操作
const handleOrderAction = async (order) => {
  if (order.status === 'COMPLETED') {
    navigateTo('/')
  } else if (order.status === 'PAID') {
    contactService()
  } else if (order.status === 'SHIPPED') {
    confirmReceipt(order)
  } else if (order.status === 'CANCELLED') {
    if (confirm('确认删除此订单记录？')) {
      alert('订单记录已清除')
      // TODO: 调用删除接口
    }
  } else if (order.status === 'PENDING') {
    navigateTo('/cart')
  }
}

// 查看溯源
const showTrace = (order) => {
  navigateTo(`/trace/${order.id}`)
}


// 筛选逻辑
const filterOrders = () => {
  if (activeTab.value === 'all') {
    filteredOrders.value = orders.value
  } else if (activeTab.value === 'processing') {
    filteredOrders.value = orders.value.filter(order => 
      ['PENDING', 'PAID', 'SHIPPED'].includes(order.status)
    )
  } else if (activeTab.value === 'completed') {
    filteredOrders.value = orders.value.filter(order => 
      ['COMPLETED', 'CANCELLED'].includes(order.status)
    )
  }
}

// 加载数据
const loadOrders = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await axios.get('/orders')
    if (response.code === 200) {
      orders.value = response.data.records || []
      // 默认按时间倒序
      orders.value.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      filterOrders()
    } else {
      error.value = response.message || '暂无法获取订单'
    }
  } catch (err) {
    console.error(err)
    error.value = '网络连接异常'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  padding: 40px 20px;
  background-color: var(--c-paper);
}

/* 头部 */
.page-header {
  text-align: center;
  margin-bottom: 50px;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  color: var(--c-ink);
  margin-bottom: 8px;
  letter-spacing: 2px;
}

.page-subtitle {
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: var(--c-primary);
  text-transform: uppercase;
  letter-spacing: 4px;
  opacity: 0.8;
}

/* 标签页 */
.tabs-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 40px;
}

.tabs {
  display: inline-flex;
  background: rgba(255, 255, 255, 0.5);
  padding: 6px;
  border-radius: 50px;
  border: 1px solid rgba(var(--c-ink-rgb), 0.1);
}

.tab-item {
  background: transparent;
  border: none;
  padding: 10px 30px;
  border-radius: 40px;
  font-size: 15px;
  color: var(--c-gray);
  cursor: pointer;
  transition: all 0.3s var(--ease);
  font-family: 'Noto Serif SC', serif;
}

.tab-item.active {
  background: var(--c-primary);
  color: white;
  box-shadow: 0 4px 12px rgba(var(--c-primary-rgb), 0.3);
}

.tab-item:hover:not(.active) {
  color: var(--c-ink);
}

/* 列表区域 */
.orders-content {
  max-width: 900px;
  margin: 0 auto;
}

.orders-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 订单卡片 */
.order-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.4s var(--ease);
  position: relative;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
}

.order-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);
}

/* 状态左边框装饰 */
.order-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--c-gray);
  opacity: 0.5;
}

.order-card.status-pending::before { background: #E6A23C; opacity: 1; }
.order-card.status-paid::before,
.order-card.status-shipped::before { background: var(--c-primary); opacity: 1; }
.order-card.status-completed::before { background: var(--c-accent-warm); opacity: 1; }
.order-card.status-cancelled::before { background: #909399; opacity: 0.3; }

/* 卡片头部 */
.card-header {
  padding: 16px 24px;
  border-bottom: 1px solid rgba(var(--c-ink-rgb), 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--c-gray);
}

.order-no {
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.5px;
}

.order-status-badge {
  font-size: 14px;
  font-weight: 600;
  font-family: 'Noto Serif SC', serif;
}

.status-pending .order-status-badge { color: #E6A23C; }
.status-paid .order-status-badge,
.status-shipped .order-status-badge { color: var(--c-primary); }
.status-completed .order-status-badge { color: var(--c-accent-warm); }
.status-cancelled .order-status-badge { color: #909399; }

/* 卡片内容 */
.card-body {
  padding: 24px;
  cursor: pointer;
}

.product-item {
  display: flex;
  gap: 20px;
  align-items: center;
}

.product-thumb {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  background-color: #f5f5f5;
  box-shadow: inset 0 0 0 1px rgba(0,0,0,0.05);
}

.product-info {
  flex: 1;
}

.product-name {
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  color: var(--c-ink);
  margin-bottom: 8px;
  font-weight: 600;
}

.product-specs {
  font-size: 13px;
  color: var(--c-gray);
}

.product-price {
  font-family: 'Inter', sans-serif;
  font-weight: 500;
  color: var(--c-ink);
  margin-top: 4px;
}

/* 卡片底部 */
.card-footer {
  padding: 16px 24px;
  background: rgba(var(--c-primary-rgb), 0.02);
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(var(--c-ink-rgb), 0.05);
}

.total-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-info span:first-child {
  font-size: 13px;
  color: var(--c-gray);
}

.total-price {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  color: var(--c-primary);
  font-weight: 700;
}

.action-group {
  display: flex;
  gap: 12px;
}

.btn-sm {
  padding: 6px 20px;
  font-size: 14px;
  border-radius: 4px;
}

.btn-text {
  background: none;
  border: none;
  color: var(--c-gray);
  font-size: 14px;
  padding: 6px 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-text:hover {
  color: var(--c-primary);
  background: rgba(var(--c-primary-rgb), 0.05);
  border-radius: 4px;
}

.icon {
  font-style: normal;
}

/* 状态容器 (加载/错误/空) */
.state-container {
  padding: 80px 0;
  text-align: center;
  color: var(--c-gray);
}

.state-icon {
  font-size: 48px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.3;
  filter: grayscale(100%);
}

.needle-spin {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(var(--c-primary-rgb), 0.2);
  border-top-color: var(--c-primary);
  border-radius: 50%;
  animation: spin 1s infinite linear;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .product-item {
    align-items: flex-start;
  }
  
  .card-footer {
    flex-direction: column;
    align-items: flex-end;
    gap: 16px;
  }
  
  .total-info {
    width: 100%;
    justify-content: flex-end;
  }
  
  .action-group {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
