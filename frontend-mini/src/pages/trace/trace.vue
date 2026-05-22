<template>
  <view class="trace-container">
    <view class="trace-header">
      <view class="title">全流程数字溯源</view>
      <view class="order-no">订单号：{{ order.orderNo }}</view>
      
      <!-- 可滑动横向时间轴 -->
      <scroll-view scroll-x class="timeline-scroll" :scroll-into-view="'step-' + currentStepIdx" scroll-with-animation>
        <view class="timeline-wrapper">
          <view 
            class="step" 
            v-for="(step, idx) in steps" 
            :key="idx"
            :id="'step-' + idx"
            :class="{ 
              completed: idx <= currentStepIdx,
              active: idx === currentStepIdx,
              selected: selectedNodeAlias === step.alias
            }"
            @click="onStepClick(step, idx)"
          >
            <view class="circle">
              <text v-if="idx < currentStepIdx" class="icon">✓</text>
              <text v-else>{{ idx + 1 }}</text>
            </view>
            <text class="label">{{ step.label }}</text>
            <view class="line" v-if="idx < steps.length - 1"></view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <view class="trace-content">
      <view v-if="loading" class="loading">加载中...</view>
      
      <view v-else class="status-card">
        <view class="card-header">
          <view class="status-info">
            <text class="status-title">当前状态：{{ currentItem.status }}</text>
            <text class="time" v-if="currentItem.time">{{ currentItem.time }}</text>
          </view>
        </view>
        
        <view class="card-body">
          <!-- C2M 定制图展示 -->
          <view class="c2m-preview" v-if="['confirmed', 'wait'].includes(currentItem.alias) || (order.items && order.items[0] && order.items[0].productImage)">
             <view v-if="order.items && order.items[0] && order.items[0].productImage && order.items[0].productImage.startsWith('http')" class="preview-box">
                <image :src="order.items[0].productImage" mode="aspectFit" class="design-img" @click="previewImage(order.items[0].productImage)"></image>
                <text class="preview-label">定制设计图稿</text>
             </view>
          </view>

          <view class="desc">{{ currentItem.description || '暂无详细描述' }}</view>
          
          <!-- 媒体展示 -->
          <view class="media-section" v-if="currentItem.media && currentItem.media.length">
            <text class="section-title">现场实况</text>
            <view class="media-grid">
              <view v-for="(url, idx) in currentItem.media" :key="idx" class="media-item-wrap">
                <video 
                  v-if="url.endsWith('.mp4')" 
                  :src="url" 
                  class="media-item"
                  object-fit="cover"
                ></video>
                <image 
                  v-else 
                  :src="url" 
                  mode="aspectFill" 
                  class="media-item"
                  @click="previewImage(url)"
                ></image>
              </view>
            </view>
          </view>
          
          <view class="meta">
            <view class="meta-row">
              <text class="label">操作人：</text>
              <text class="value">{{ currentItem.operator || '-' }}</text>
            </view>
            <view class="meta-row">
              <text class="label">备注：</text>
              <text class="value">{{ currentItem.remark || '-' }}</text>
            </view>
          </view>

          <!-- 操作按钮区域 -->
          <view class="card-actions">
            <!-- 待C2M定制 -> 立即定制 -->
            <button 
              v-if="showCustomBtn" 
              class="btn btn-primary" 
              @click="goToCustom"
            >
              立即开启定制
            </button>

            <!-- 成品发货 -> 确认收货 -->
            <button 
              v-if="showConfirmBtn" 
              class="btn btn-primary" 
              @click="confirmReceipt"
            >
              确认收货
            </button>
          </view>

        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'

const orderId = ref('')
const order = ref({})
const timelineItems = ref([])
const loading = ref(true)
const selectedNodeAlias = ref('')

const steps = [
  { alias: 'wait', label: '待定制' },
  { alias: 'confirmed', label: 'C2M定制' },
  { alias: 'material', label: '采发包' },
  { alias: 'workshop', label: '工坊' },
  { alias: 'producing', label: '绣制中' },
  { alias: 'shipped', label: '发货' },
  { alias: 'received', label: '签收' }
]

onLoad((option) => {
  if (option.id) {
    orderId.value = option.id
    loadTraceData()
  }
})

onPullDownRefresh(async () => {
  if (orderId.value) {
    await loadTraceData()
  }
  uni.stopPullDownRefresh()
})

const loadTraceData = async () => {
  loading.value = true
  try {
    const res = await request.get(`/orders/${orderId.value}?t=${Date.now()}`)
    if (res.code === 200) {
      order.value = res.data.order
      // 确保 items 数据存在
      if (res.data.items) {
        order.value.items = res.data.items
      }
      
      const firstItem = res.data.items?.[0]
      if (firstItem) {
         if (firstItem.statusTimeline) {
           // 处理可能为 JSON 字符串的情况
           if (typeof firstItem.statusTimeline === 'string') {
             try {
               timelineItems.value = JSON.parse(firstItem.statusTimeline).reverse()
             } catch(e) { timelineItems.value = [] }
           } else {
             timelineItems.value = firstItem.statusTimeline.reverse()
           }
         }
         // 存储当前状态
         order.value.currentStatus = firstItem.currentStatus || 'wait'
         order.value.item = firstItem // 保存 item 引用方便后续使用
         
         // 默认选中当前状态
         selectedNodeAlias.value = order.value.currentStatus
      }
    }
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '加载失败: ' + (e.message || '未知错误'), icon: 'none' })
  } finally {
    loading.value = false
  }
}

const currentStepIdx = computed(() => {
  const status = order.value.currentStatus || 'wait'
  return steps.findIndex(s => s.alias === status)
})

const onStepClick = (step, idx) => {
  // 允许点击当前或之前的节点
  if (idx <= currentStepIdx.value) {
    selectedNodeAlias.value = step.alias
  }
}

// 是否显示“立即定制”按钮
const showCustomBtn = computed(() => {
  // 只有在最新状态是 'wait' 且当前选中的也是 'wait' 时显示
  const currentStatus = order.value.currentStatus || 'wait'
  return currentStatus === 'wait' && selectedNodeAlias.value === 'wait'
})

// 是否显示“确认收货”按钮
const showConfirmBtn = computed(() => {
  const currentStatus = order.value.currentStatus
  // 状态为已发货(shipped) 且 选中了该节点
  return currentStatus === 'shipped' && selectedNodeAlias.value === 'shipped'
})

const currentItem = computed(() => {
  const targetAlias = selectedNodeAlias.value || order.value.currentStatus || 'wait'
  const targetNode = steps.find(s => s.alias === targetAlias)
  
  // 获取对应的媒体资源
  const getMedia = (alias) => {
    const item = order.value.item
    if (!item) return []
    let jsonStr = ''
    switch (alias) {
      case 'material': jsonStr = item.mediaPack; break;
      case 'workshop': jsonStr = item.mediaWorkshop; break;
      case 'producing': jsonStr = item.mediaProduction; break;
      case 'shipped': jsonStr = item.mediaShipment; break;
    }
    if (!jsonStr) return []
    try {
      return JSON.parse(jsonStr)
    } catch (e) { return [] }
  }
  
  const media = getMedia(targetAlias)
  
  // 尝试从时间轴记录中匹配
  if (targetNode && timelineItems.value.length > 0) {
    // 模糊匹配 label (后端记录的 status 是中文)
    const match = timelineItems.value.find(item => item.status.includes(targetNode.label) || item.status === targetNode.label)
    if (match) {
      return { ...match, media, alias: targetAlias }
    }
  }
  
  // 默认构造
  if (targetAlias === 'wait') {
    return {
      status: '待定制',
      alias: 'wait',
      time: order.value.createTime ? order.value.createTime.replace('T', ' ') : '',
      description: '订单已创建，等待开启个性化定制流程。',
      operator: '系统',
      media
    }
  }
  
  return {
    status: targetNode ? targetNode.label : targetAlias,
    alias: targetAlias,
    time: '',
    description: '暂无详细记录',
    operator: '-',
    media
  }
})

const previewImage = (url) => {
  uni.previewImage({ urls: [url] })
}

const goToCustom = () => {
  uni.navigateTo({ url: `/pages/custom/custom?orderId=${orderId.value}` })
}

const confirmReceipt = () => {
  uni.showModal({
    title: '提示',
    content: '确认收到商品了吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const itemId = order.value.item.id
          const res = await request.put(`/orders/items/${itemId}/c2m-status`, { 
            status: 'received', 
            remark: '用户确认收货' 
          })
          if (res.code === 200) {
            uni.showToast({ title: '收货成功' })
            loadTraceData() // 刷新数据
          } else {
            uni.showToast({ title: res.message || '操作失败', icon: 'none' })
          }
        } catch (e) {
          uni.showToast({ title: '网络异常', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style lang="scss">
.trace-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}

.trace-header {
  background: linear-gradient(135deg, #00796b 0%, #004d40 100%);
  color: #fff;
  padding: 40rpx 0;
  padding-bottom: 80rpx;
  border-bottom-left-radius: 40rpx;
  border-bottom-right-radius: 40rpx;
  
  .title { font-size: 36rpx; font-weight: bold; margin-bottom: 10rpx; text-align: center; }
  .order-no { font-size: 24rpx; opacity: 0.8; text-align: center; margin-bottom: 40rpx; }
  
  .timeline-scroll {
    width: 100%;
    white-space: nowrap;
    padding: 20rpx 0;
  }
  
  .timeline-wrapper {
    display: inline-flex;
    padding: 0 40rpx;
    align-items: flex-start;
  }
  
  .step {
    display: inline-flex;
    flex-direction: column;
    align-items: center;
    margin-right: 60rpx;
    position: relative;
    opacity: 0.6;
    transition: all 0.3s;
    
    &:last-child { margin-right: 0; }
    
    &.completed, &.active { opacity: 1; }
    
    .circle {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      background: rgba(255,255,255,0.2);
      border: 2px solid rgba(255,255,255,0.5);
      color: #fff;
      font-size: 32rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16rpx;
      transition: all 0.3s;
      position: relative;
      z-index: 2;
      
      .icon { font-size: 36rpx; font-weight: bold; }
    }
    
    .label { font-size: 24rpx; color: #fff; }
    
    .line {
      position: absolute;
      top: 40rpx;
      left: 80rpx; /* circle width */
      width: 60rpx; /* margin-right */
      height: 4rpx;
      background: rgba(255,255,255,0.3);
      z-index: 1;
    }
    
    &.completed .line { background: rgba(255,255,255,0.8); }
    &.completed .circle { background: #fff; color: #00796b; border-color: #fff; }
    
    &.active .circle { 
      transform: scale(1.15); 
      background: #fff; 
      color: #004d40; 
      border-color: #fff;
      box-shadow: 0 0 20rpx rgba(255,255,255,0.4);
    }
    &.active .label { font-weight: bold; }
    
    &.selected .circle {
      border-color: #ffca28; /* Amber accent */
      box-shadow: 0 0 0 6rpx rgba(255, 202, 40, 0.4);
    }
    &.selected .label { color: #ffca28; }
  }
}

.trace-content {
  padding: 30rpx;
  margin-top: -60rpx;
}

.status-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.08);
  min-height: 400rpx;
  
  .card-header {
    margin-bottom: 30rpx;
    padding-bottom: 20rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    .status-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .status-title { font-size: 34rpx; font-weight: bold; color: #333; }
    .time { font-size: 24rpx; color: #999; }
  }
  
  .preview-box {
    text-align: center;
    margin-bottom: 30rpx;
    background: #f9f9f9;
    padding: 20rpx;
    border-radius: 12rpx;
    
    .design-img {
      width: 100%;
      height: 300rpx;
      border-radius: 8rpx;
      margin-bottom: 10rpx;
    }
    .preview-label { font-size: 24rpx; color: #666; }
  }
  
  .desc { font-size: 30rpx; color: #555; line-height: 1.6; margin-bottom: 40rpx; }
  
  .section-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
    border-left: 6rpx solid #00796b;
    padding-left: 16rpx;
  }
  
  .media-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    margin-bottom: 40rpx;
    
    .media-item-wrap {
      width: 31%; /* 3 columns */
      height: 160rpx;
      border-radius: 8rpx;
      overflow: hidden;
      background: #000;
    }
    
    .media-item {
      width: 100%;
      height: 100%;
    }
  }
  
  .meta {
    background: #f5f7fa;
    padding: 24rpx;
    border-radius: 12rpx;
    margin-bottom: 40rpx;
    
    .meta-row {
      display: flex;
      margin-bottom: 12rpx;
      font-size: 26rpx;
      &:last-child { margin-bottom: 0; }
      
      .label { color: #999; width: 120rpx; }
      .value { color: #333; flex: 1; }
    }
  }
  
  .card-actions {
    margin-top: 40rpx;
    border-top: 1rpx solid #f0f0f0;
    padding-top: 30rpx;
    display: flex;
    justify-content: flex-end;
    
    .btn {
      margin: 0;
      font-size: 28rpx;
      padding: 0 40rpx;
      height: 72rpx;
      line-height: 72rpx;
      border-radius: 36rpx;
      background: #00796b;
      color: #fff;
      
      &::after { border: none; }
      &:active { opacity: 0.9; }
    }
  }
}

.loading {
  text-align: center;
  padding: 60rpx;
  color: #fff;
}
</style>
