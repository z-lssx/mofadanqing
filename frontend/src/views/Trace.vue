<template>
  <div class="trace-page">
    <div class="trace-header-bg">
      <div class="container">
        <div class="header-content">
          <button class="btn-back" @click="goBack">← 返回</button>
          <h2>全流程数字溯源</h2>
          <div class="order-no">订单号：{{ order.orderNo }}</div>
        </div>
        
        <!-- 横向时间轴 -->
        <div class="timeline-horizontal">
          <div 
            class="timeline-node" 
            v-for="(node, index) in statusNodes" 
            :key="index"
            :class="{ 
              active: isCurrentStatus(node.alias), 
              completed: isCompletedStatus(node.alias),
              selected: selectedNodeAlias === node.alias
            }"
            @click="onNodeClick(node)"
            style="cursor: pointer;"
          >
            <div class="node-icon">
              <i v-if="node.icon" :class="node.icon"></i>
              <span v-else>{{ index + 1 }}</span>
            </div>
            <div class="node-label">{{ node.label }}</div>
            <div class="node-time" v-if="getNodeTime(node.alias)">{{ getNodeTime(node.alias) }}</div>
          </div>
          <!-- 进度条背景线 -->
          <div class="progress-line">
             <div class="progress-fill" :style="{ width: progressWidth }"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="container content-container">
      <div v-if="loading" class="loading">
        <p>加载溯源数据中...</p>
      </div>

      <div v-else-if="error" class="error">
        <p>{{ error }}</p>
        <div v-if="error.includes('不存在')" style="margin-top: 10px;">
            <button @click="router.push('/orders')" class="btn btn-primary">返回订单列表</button>
        </div>
      </div>

      <div v-else class="trace-content">
        <!-- 仅展示当前状态的详情卡片 -->
        <div class="status-card" v-if="currentTimelineItem">
          <div class="card-header">
            <h3>当前状态：{{ currentTimelineItem.status }}</h3>
            <span class="time-tag">{{ currentTimelineItem.time }}</span>
          </div>
          
          <div class="card-body">
            <!-- C2M 图片展示区域 -->
            <div class="c2m-preview" v-if="['confirmed', 'wait'].includes(currentTimelineItem.status) || order.items?.[0]?.productImage">
               <div v-if="order.items?.[0]?.productImage && order.items?.[0]?.productImage.startsWith('http')" style="margin-bottom: 20px; text-align: center;">
                  <img :src="order.items[0].productImage" alt="定制设计图" style="max-width: 100%; max-height: 300px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                  <p style="font-size: 12px; color: #666; margin-top: 8px;">定制设计图稿</p>
               </div>
            </div>

            <!-- 溯源媒体展示区域 -->
            <div class="media-gallery" v-if="currentTimelineItem.media && currentTimelineItem.media.length">
              <h4 style="margin-bottom: 10px; color: #333;">现场实况</h4>
              <div class="media-grid">
                <div v-for="(url, idx) in currentTimelineItem.media" :key="idx" class="media-item">
                   <video v-if="url.endsWith('.mp4')" :src="url" controls></video>
                   <img v-else :src="url" alt="现场图片" @click="window.open(url, '_blank')">
                </div>
              </div>
            </div>

            <p class="desc">{{ currentTimelineItem.description || '暂无详细描述' }}</p>
            
            <div class="meta-info">
              <div v-if="currentTimelineItem.operator" class="meta-item">
                <span class="label">操作人：</span>{{ currentTimelineItem.operator }}
              </div>
              <div v-if="currentTimelineItem.remark" class="meta-item">
                <span class="label">备注：</span>{{ currentTimelineItem.remark }}
              </div>
            </div>

            <!-- 操作按钮区域 -->
            <div class="card-actions">
              <!-- 待C2M定制 -> 立即定制 -->
              <button 
                v-if="order.items?.[0]?.currentStatus === 'wait' || !order.items?.[0]?.currentStatus" 
                class="btn btn-primary" 
                @click="goToCustom"
              >
                立即开启定制
              </button>

              <!-- 成品发货 -> 确认收货 -->
              <button 
                v-if="order.items?.[0]?.currentStatus === 'shipped'" 
                class="btn btn-primary" 
                @click="confirmReceipt"
              >
                确认收货
              </button>
            </div>
          </div>
        </div>
        
        <div class="empty-card" v-else>
          <p>暂无当前状态详情</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '../utils/axios'

const route = useRoute()
const router = useRouter()
const orderId = route.params.orderId
const order = ref({})
const timelineItems = ref([])
const loading = ref(true)
const error = ref('')
const selectedNodeAlias = ref('') // 当前选中的时间轴节点

// 定义标准状态流转节点
const statusNodes = [
  { alias: 'wait', label: '待定制', icon: 'icon-clock' },
  { alias: 'confirmed', label: 'C2M定制', icon: 'icon-edit' },
  { alias: 'material', label: '采发包', icon: 'icon-box' },
  { alias: 'workshop', label: '工坊签收', icon: 'icon-home' },
  { alias: 'producing', label: '绣制中', icon: 'icon-needle' },
  { alias: 'shipped', label: '成品发货', icon: 'icon-truck' },
  { alias: 'received', label: '用户签收', icon: 'icon-check-circle' }
]

const goBack = () => router.push('/orders')

const loadTraceData = async () => {
  // 确保 loadTraceData 被重新调用时能强制刷新
  // 实际上 Vue 的响应式已经能处理，但为了保险，可以加一个 timestamp 参数避免缓存
  try {
    const res = await axios.get(`/orders/${orderId}?t=${Date.now()}`)
    if (res.code === 200) {
      order.value = res.data.order
      const firstItem = res.data.items?.[0]
      if (firstItem) {
        order.value.items = [firstItem]
        // 确保 currentStatus 存在，默认 wait
        if (!firstItem.currentStatus) firstItem.currentStatus = 'wait'
        
        if (firstItem.statusTimeline) {
          timelineItems.value = firstItem.statusTimeline.reverse()
        } else {
           timelineItems.value = []
        }
        // 默认选中当前状态
        selectedNodeAlias.value = firstItem.currentStatus
      }
    } else {
      error.value = res.message || '加载失败'
    }
  } catch (e) {
    console.error(e)
    error.value = '网络异常，请重试'
  } finally {
    loading.value = false
  }
}

// 计算当前进度的宽度
const progressWidth = computed(() => {
  const currentAlias = order.value.items?.[0]?.currentStatus || 'wait'
  const index = statusNodes.findIndex(n => n.alias === currentAlias)
  if (index === -1) return '0%'
  const step = 100 / (statusNodes.length - 1)
  return `${index * step}%`
})

// 判断状态是否已完成
const isCompletedStatus = (alias) => {
  const currentAlias = order.value.items?.[0]?.currentStatus || 'wait'
  const currentIndex = statusNodes.findIndex(n => n.alias === currentAlias)
  const targetIndex = statusNodes.findIndex(n => n.alias === alias)
  return targetIndex <= currentIndex
}

// 判断是否是当前状态
const isCurrentStatus = (alias) => {
  return (order.value.items?.[0]?.currentStatus || 'wait') === alias
}

// 获取某个状态节点的发生时间
const getNodeTime = (alias) => {
  // 从 timelineItems 中查找对应状态的时间
  // 注意 timelineItems 是倒序的 (最新的在最前)
  // 我们需要找到匹配该状态描述的 Item
  const node = statusNodes.find(n => n.alias === alias)
  if (!node) return ''
  
  // 这里做一个简单的映射匹配，实际项目中可能需要更严谨的 mapping
  const match = timelineItems.value.find(item => item.status.includes(node.label) || item.status === node.label)
  if (match) {
      // 截取时间 MM-dd HH:mm
      return match.time.substring(5, 16)
  }
  return ''
}

// 点击时间轴节点
const onNodeClick = (node) => {
  // 只能点击已完成或当前的节点
  if (isCompletedStatus(node.alias) || isCurrentStatus(node.alias)) {
    selectedNodeAlias.value = node.alias
  }
}

    // 获取当前选中节点的详情卡片数据
    const currentTimelineItem = computed(() => {
      const targetAlias = selectedNodeAlias.value || order.value.items?.[0]?.currentStatus || 'wait'
      const targetNode = statusNodes.find(n => n.alias === targetAlias)
      if (!targetNode) return null

      // 获取该状态对应的媒体文件
      const getMediaForStatus = (alias) => {
        const item = order.value.items?.[0]
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
        } catch (e) {
          return []
        }
      }

      const media = getMediaForStatus(targetAlias)

      // 从 timelineItems 中查找匹配的记录
      // 注意：timelineItems 记录的是历史操作，status 字段是中文描述
      // 我们需要找到 status 包含 targetNode.label 的那条记录
      
      const match = timelineItems.value.find(item => item.status === targetNode.label)
      
      if (match) {
        return { ...match, media }
      }
      
      // 如果没找到（比如 wait 状态通常没有 timeline 记录），构造默认数据
      if (targetAlias === 'wait') {
          return {
              status: '待C2M定制',
              time: order.value.createTime,
              description: '订单已创建，等待用户开启个性化定制流程。',
              operator: '系统',
              media
          }
      }
      
      return {
        status: targetNode.label,
        time: '',
        description: '暂无详细记录',
        operator: '-',
        media
      }
    })

const goToCustom = () => {
  // Use orderId for routing as per new refactor
  router.push(`/custom/${orderId}`)
}

const confirmReceipt = async () => {
  if (!confirm('确认收到商品了吗？')) return
  try {
    const itemId = order.value.items[0].id
    const res = await axios.put(`/orders/items/${itemId}/c2m-status`, { 
      status: 'received', 
      remark: '用户确认收货' 
    })
    if (res.code === 200) {
      alert('收货成功！')
      loadTraceData()
    } else {
      alert(res.message)
    }
  } catch (e) {
    alert('操作失败')
  }
}

onMounted(loadTraceData)
</script>

<style scoped>
.trace-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.trace-header-bg {
  background: linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%); /* 仿山水淡绿风格 */
  padding: 40px 0 80px; /* 底部留白给卡片上浮 */
  position: relative;
  overflow: hidden;
}

/* 装饰背景 (可选) */
.trace-header-bg::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100px;
  background: url('https://via.placeholder.com/1200x200?text=Mountain+Bg') no-repeat bottom center;
  background-size: cover;
  opacity: 0.1;
  pointer-events: none;
}

.header-content {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  z-index: 2;
}

.btn-back {
  position: absolute;
  left: 0;
  top: 0;
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  color: #00695c;
}

.header-content h2 {
  font-size: 28px;
  color: #004d40;
  margin-bottom: 10px;
  font-weight: normal;
  letter-spacing: 2px;
}

.order-no {
  color: #00796b;
  font-size: 14px;
}

/* 横向时间轴 */
.timeline-horizontal {
  display: flex;
  justify-content: space-between;
  position: relative;
  margin: 0 40px;
  z-index: 2;
}

.progress-line {
  position: absolute;
  top: 25px;
  left: 0;
  right: 0;
  height: 2px;
  background: rgba(0, 121, 107, 0.2);
  z-index: -1;
}

.progress-fill {
  height: 100%;
  background: #00796b;
  transition: width 0.5s ease;
}

.timeline-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  width: 80px;
}

.node-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #b2dfdb;
  color: #b2dfdb;
  font-size: 20px;
  margin-bottom: 10px;
  transition: all 0.3s;
}

.timeline-node.completed .node-icon {
  background: #e0f2f1;
  border-color: #00796b;
  color: #00796b;
}

.timeline-node.selected .node-icon {
  border-color: #ff9800;
  color: #ff9800;
  box-shadow: 0 0 0 4px rgba(255, 152, 0, 0.2);
}

.timeline-node.active .node-icon {
  background: #00796b;
  border-color: #004d40;
  color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 10px rgba(0, 121, 107, 0.3);
}

.node-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.timeline-node.active .node-label {
  color: #004d40;
  font-weight: bold;
}

.node-time {
  font-size: 10px;
  color: #999;
}

/* 内容区域 */
.content-container {
  margin-top: -60px; /* 卡片上浮效果 */
  position: relative;
  z-index: 3;
}

.status-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  padding: 40px;
  min-height: 200px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.card-header h3 {
  font-size: 20px;
  color: #333;
}

.time-tag {
  color: #999;
  font-size: 14px;
}

.card-body .desc {
  font-size: 16px;
  color: #555;
  line-height: 1.6;
  margin-bottom: 30px;
}

.meta-info {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.meta-item {
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}
.meta-item:last-child { margin-bottom: 0; }
.meta-item .label { font-weight: bold; color: #333; }

.media-gallery { margin-bottom: 20px; }
.media-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 10px; }
.media-item { width: 100%; height: 100px; border-radius: 4px; overflow: hidden; background: #eee; cursor: pointer; }
.media-item img, .media-item video { width: 100%; height: 100%; object-fit: cover; }

.card-actions {
  display: flex;
  justify-content: flex-end;
}

/* 图标字体模拟 (实际需引入 iconfont 或 svg) */
i { font-style: normal; }
.icon-clock::before { content: '⏱'; }
.icon-edit::before { content: '✏'; }
.icon-box::before { content: '📦'; }
.icon-home::before { content: '🏠'; }
.icon-needle::before { content: '🧵'; }
.icon-truck::before { content: '🚚'; }
.icon-check-circle::before { content: '✨'; }

.container { max-width: 1000px; margin: 0 auto; padding: 0 20px; }
</style>
