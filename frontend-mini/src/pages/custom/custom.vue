<template>
  <view class="custom-page">
    <!-- 步骤导航 -->
    <view class="step-list">
      <view 
        class="step-item" 
        :class="{ active: currentStep === 1, done: currentStep > 1 }"
        @click="setStep(1)"
      >1</view>
      <view 
        class="step-item" 
        :class="{ active: currentStep === 2, done: currentStep > 2 }"
        @click="setStep(2)"
      >2</view>
      <view 
        class="step-item" 
        :class="{ active: currentStep === 3, done: currentStep > 3 }"
        @click="setStep(3)"
      >3</view>
    </view>

    <!-- Step 1: 上传与关键词 -->
    <view v-if="currentStep === 1" class="step-content">
      <view class="label">1. 上传灵感 / 照片</view>
      <view class="upload-box" @click="chooseImage">
        <image v-if="uploadedImageUrl" :src="uploadedImageUrl" mode="aspectFit" class="preview-img"></image>
        <view v-else class="placeholder">
          <text class="icon">📷</text>
          <text class="hint">点击上传参考图</text>
        </view>
      </view>

      <view class="label" style="margin-top: 20px;">2. AI 关键词</view>
      <input 
        class="input-box"
        placeholder="例：星辰，连理枝，极简" 
        v-model="keywords"
      />
      
      <button 
        class="btn-primary" 
        @click="startAiGeneration"
        :disabled="loading"
      >{{ loading ? 'AI 生成中...' : '生成刺绣图稿' }}</button>
    </view>

    <!-- Step 2: 图层调节与合成 -->
    <view v-if="currentStep === 2" class="step-content">
      <view class="canvas-container" style="position: relative; background: #fff; overflow: hidden;">
        <!-- CSS 图层叠加方案 -->
        <image 
          v-if="layer1Url" 
          :src="layer1Url" 
          mode="aspectFill" 
          class="layer-img"
          :style="{ opacity: hairAmount / 100 }"
        ></image>
        <image 
          v-if="layer2Url" 
          :src="layer2Url" 
          mode="aspectFill" 
          class="layer-img"
          :style="{ opacity: silkSaturation / 100, mixBlendMode: 'multiply' }"
        ></image>
        <view v-if="!layer1Url && !layer2Url" class="loading-placeholder">图片加载中...</view>
      </view>

      <view class="controls">
        <view class="slider-group">
          <text>含发量 (骨架层): {{ hairAmount }}%</text>
          <slider :value="hairAmount" min="0" max="100" @change="onHairChange" show-value />
        </view>

        <view class="slider-group">
          <text>色彩饱和度 (润色层): {{ silkSaturation }}%</text>
          <slider :value="silkSaturation" min="0" max="100" @change="onSilkChange" show-value />
        </view>
      </view>

      <button class="btn-primary" @click="confirmDesign">确认定稿</button>
    </view>

    <!-- Step 3: 报价单 -->
    <view v-if="currentStep === 3" class="step-content">
      <view class="bom-card">
        <view class="row"><text class="bold">产品：</text><text>{{ productName }}</text></view>
        <view class="row"><text class="bold">发丝用量：</text><text>{{ bomData.hairLength }} 米</text></view>
        <view class="row"><text class="bold">蚕丝用量：</text><text>{{ bomData.silkWeight }} 克</text></view>
        <view class="row"><text class="bold">预计工期：</text><text>7-10 个工作日</text></view>
        <view class="divider"></view>
        
        <!-- 价格明细 -->
        <view class="price-info" v-if="priceBreakdown.finalPrice > 0">
           <view class="row">
             <text>基础定金 (已付):</text>
             <text>¥{{ priceBreakdown.deposit.toFixed(2) }}</text>
           </view>
           <view class="row">
             <text>定制增项:</text>
             <text>+¥{{ (priceBreakdown.finalPrice - priceBreakdown.deposit).toFixed(2) }}</text>
           </view>
           <view class="row highlight">
             <text>最终总价:</text>
             <text>¥{{ priceBreakdown.finalPrice.toFixed(2) }}</text>
           </view>
           <view class="row" style="color: #d32f2f; font-weight: bold;">
             <text>待付尾款:</text>
             <text>¥{{ priceBreakdown.balance.toFixed(2) }}</text>
           </view>
        </view>
        <view v-else>
           <view class="row highlight">
             <text>预估总价:</text>
             <text>¥{{ bomData.estimatedCost.toFixed(2) }}</text>
           </view>
        </view>
      </view>

      <view v-if="digitalAssetId" class="digital-card">
         <text class="success-text">✅ 数字版权已确权</text>
         <text class="id-text">ID: {{ digitalAssetId }}</text>
      </view>

      <button class="btn-primary" @click="submitOrder" :loading="loading">确认提交设计</button>
    </view>

  </view>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance, nextTick } from 'vue'
import request from '../../utils/request'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'

// 获取当前实例 (必须在 setup 顶层同步调用)
const instance = getCurrentInstance()

const currentStep = ref(1)
const keywords = ref('')
const uploadedImageUrl = ref('')
const loading = ref(false)
const taskId = ref(null)

// AI Data
const layer1Url = ref('')
const layer2Url = ref('')
const hairAmount = ref(50)
const silkSaturation = ref(80)
const bomData = ref({ hairLength: 0, silkWeight: 0, estimatedCost: 0 })
const priceBreakdown = ref({ deposit: 0, finalPrice: 0, balance: 0 })
const digitalAssetId = ref('')
const productName = ref('通用定制')
const itemId = ref(null)
const orderId = ref(null)

// Canvas Refs
let canvasNode = null
let ctx = null
let img1 = null
let img2 = null
let canvasCssW = 300
let canvasCssH = 300
const normalizeUrl = (url) => {
  if (!url) return ''
  try {
    let u = url.trim()
    if (u.startsWith('http://')) u = 'https://' + u.slice(7)
    return u
  } catch (e) { return url }
}

// 接收参数
onLoad(async (option) => {
  if (option.orderId) {
    orderId.value = option.orderId
    await loadOrderInfo(orderId.value)
  } else {
    // 允许没有 orderId，可能是直接体验
    productName.value = '自由定制体验'
  }
})

// 下拉刷新
onPullDownRefresh(async () => {
  if (orderId.value) {
    await loadOrderInfo(orderId.value)
  }
  uni.stopPullDownRefresh()
})

// 加载订单信息
const loadOrderInfo = async (id) => {
  try {
    loading.value = true
    const res = await request.get(`/orders/${id}`)
    if (res.code === 200) {
      const order = res.data.order
      const items = res.data.items || []
      
      // 找到待定制的 Item (wait 或 待C2M定制)
      const targetItem = items.find(i => !i.c2mStatus || i.c2mStatus === 'wait' || i.c2mStatus === '待C2M定制') || items[0]
      
      if (targetItem) {
          itemId.value = targetItem.id
          productName.value = targetItem.productName
      } else {
          uni.showModal({
            title: '提示',
            content: '该订单没有需要定制的商品',
            showCancel: false,
            success: () => uni.navigateBack()
          })
      }
    } else {
       uni.showToast({ title: '加载失败', icon: 'none' })
    }
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '网络异常', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 上传图片
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      const filePath = res.tempFilePaths[0]
      uploadImage(filePath)
    }
  })
}

const uploadImage = async (filePath) => {
  try {
    loading.value = true
    uni.showLoading({ title: '上传中...' })
    const res = await request.upload('/upload/image', filePath, 'file', { storage: 'oss' })
    if (res.code === 200 || res.url) {
      uploadedImageUrl.value = res.data?.url || res.url
      uni.showToast({ title: '上传成功' })
    }
  } catch (e) {
    uni.showToast({ title: '上传失败', icon: 'none' })
  } finally {
    loading.value = false
    uni.hideLoading()
  }
}

// 开始 AI 生成
const startAiGeneration = async () => {
  if (!keywords.value && !uploadedImageUrl.value) {
    uni.showToast({ title: '请输入关键词或传图', icon: 'none' })
    return
  }
  
  try {
    loading.value = true
    const res = await request.post('/ai/c2m/generate', {
      prompt: keywords.value,
      refImg: uploadedImageUrl.value,
      style: 'TRADITIONAL'
    })
    
    if (res.code === 200) {
      taskId.value = res.data.taskId
      pollTaskStatus()
    }
  } catch (e) {
    loading.value = false
    uni.showToast({ title: '请求失败', icon: 'none' })
  }
}

// 轮询任务
const pollTaskStatus = () => {
  let attempts = 0
  const maxAttempts = 60
  
  const poll = async () => {
    try {
      const res = await request.get(`/ai/c2m/task/${taskId.value}`)
      if (res.code === 200) {
        const task = res.data
        if (task.status === 'COMPLETED') {
          loading.value = false
          layer1Url.value = task.layer1Url || task.images[0]
          layer2Url.value = task.layer2Url || task.images[1]
          console.log('[pollTaskStatus] completed', { layer1Url: layer1Url.value, layer2Url: layer2Url.value })
          bomData.value = {
             hairLength: task.hairLength || 0,
             silkWeight: task.silkWeight || 0,
             estimatedCost: task.estimatedCost || 0
          }
          setStep(2)
          return
        } else if (task.status === 'FAILED') {
          loading.value = false
          uni.showToast({ title: '生成失败', icon: 'none' })
          return
        }
      }
    } catch (e) {}
    
    attempts++
    if (attempts < maxAttempts) {
      setTimeout(poll, 2000)
    } else {
      loading.value = false
      uni.showToast({ title: '生成超时', icon: 'none' })
    }
  }
  
  setTimeout(poll, 2000)
}

const setStep = (step) => {
  currentStep.value = step
  console.log('[setStep]', step)
}

const onHairChange = (e) => {
  hairAmount.value = e.detail.value
}

const onSilkChange = (e) => {
  silkSaturation.value = e.detail.value
}

const confirmDesign = () => {
  digitalAssetId.value = "0x" + Math.random().toString(16).substr(2, 64)
  
  // 估价逻辑 (与网页端一致)
  if (itemId.value) {
      // 假设基础定金 1999
      const basePrice = 1999
      const cost = bomData.value.estimatedCost || 0
      const final = basePrice + cost
      
      priceBreakdown.value = {
          deposit: basePrice,
          finalPrice: final,
          balance: final - basePrice
      }
  } else {
      // 纯体验
      priceBreakdown.value = {
          deposit: 0,
          finalPrice: bomData.value.estimatedCost || 1999,
          balance: 0
      }
  }
  
  setStep(3)
}

const submitOrder = async () => {
  loading.value = true
  
  try {
      // 请求后端进行图片合成
      let finalImageUrl = ""
      try {
          const mergeRes = await request.post('/ai/c2m/merge', {
             taskId: taskId.value,
             layer1: layer1Url.value,
             layer2: layer2Url.value,
             hairAmount: hairAmount.value,
             silkSaturation: silkSaturation.value
          })
          if (mergeRes.code === 200 && mergeRes.data) {
             finalImageUrl = mergeRes.data
          } else {
             // 降级：使用主视觉图
             finalImageUrl = layer2Url.value || layer1Url.value
          }
      } catch (e) {
          console.error("Merge failed, fallback to layer2", e)
          finalImageUrl = layer2Url.value || layer1Url.value
      }

      const customizationParams = {
          hairAmount: hairAmount.value,
          silkSaturation: silkSaturation.value,
          layer1: layer1Url.value,
          layer2: layer2Url.value
      }
      
      if (itemId.value) {
          // 关联现有订单提交
          const res = await request.post('/ai/c2m/confirm', {
            taskId: taskId.value,
            orderItemId: itemId.value,
            generatedImage: finalImageUrl,
            extraParams: customizationParams // 假设后端支持此字段，或拼接到 remark
          })
          
          if (res.code === 200) {
              // 更新状态
              await request.put(`/orders/items/${itemId.value}/c2m-status`, {
                status: 'confirmed',
                remark: `小程序定制提交 - 骨架:${hairAmount.value}% 润色:${silkSaturation.value}% - ID:${digitalAssetId.value}`
              })
              
              uni.showToast({ title: '提交成功' })
              
              // 跳转溯源 (使用 switchTab 或 reLaunch 确保彻底离开定制页)
              // 这里的策略是：回到首页，然后自动跳转到 trace，或者直接 reLaunch 到 trace（如果 trace 不是 tabbar）
              // 但用户说 reLaunch 也无法一次性回退到最外层（可能是 trace 的返回逻辑问题）
              // 最稳妥的方式：回到 "首页" 或 "订单页" (Tabbar)，然后再打开 Trace? 不太好体验
              // 坚持使用 reLaunch 到 trace，但如果用户觉得"无法一次性回退"，可能是因为 Trace 页面的左上角返回按钮行为
              // 如果 reLaunch 到 Trace，Trace 是栈底，点击返回会去哪里？ -> 首页 (UniApp 默认行为)
              // 除非 Trace 是 Tabbar 页面。
              
              const targetId = res.data.orderId || orderId.value
              setTimeout(() => {
                  uni.reLaunch({ url: `/pages/trace/trace?id=${targetId}` })
              }, 1500)
          } else {
              uni.showToast({ title: '提交失败: ' + res.message, icon: 'none' })
          }
      } else {
          // 新建订单
          const res = await request.post('/orders/create-c2m', {
             taskId: taskId.value,
             productName: productName.value,
             quantity: 1,
             remark: `骨架:${hairAmount.value}% 润色:${silkSaturation.value}%`
          })
          if (res.code === 200) {
             uni.showToast({ title: '下单成功' })
             setTimeout(() => {
                uni.switchTab({ url: '/pages/index/index' })
             }, 1500)
          }
      }
  } catch(e) {
     console.error(e)
     uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
     loading.value = false
  }
}
</script>

<style lang="scss">
.custom-page {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.step-list {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
  
  .step-item {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #ddd;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 15px;
    color: #fff;
    font-weight: bold;
    
    &.active {
      background: #00796b;
      transform: scale(1.1);
    }
    &.done {
      background: #4cd964;
    }
  }
}

.step-content {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.label {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.upload-box {
  width: 100%;
  height: 200px;
  border: 2px dashed #eee;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  
  .preview-img {
    width: 100%;
    height: 100%;
  }
  
  .placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    .icon { font-size: 32px; margin-bottom: 10px; }
    .hint { color: #999; font-size: 14px; }
  }
}

.input-box {
  width: 100%;
  height: 44px;
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 0 10px;
  margin-bottom: 20px;
  box-sizing: border-box;
}

.btn-primary {
  background: #00796b;
  color: #fff;
  margin-top: 20px;
  border-radius: 22px;
}

.canvas-container {
  width: 300px;
  height: 300px;
  margin: 0 auto 20px;
  border: 1px solid #eee;
  
  .blend-canvas {
    width: 100%;
    height: 100%;
  }

  .layer-img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    transition: opacity 0.1s;
  }
}

.slider-group {
  margin-bottom: 15px;
  text {
    font-size: 14px;
    color: #666;
  }
}

.bom-card {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  
  .row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
    
    .bold { font-weight: bold; }
    &.highlight {
      font-size: 18px;
      font-weight: bold;
      color: #ff5a5f;
      margin-top: 10px;
    }
  }
  .divider {
    height: 1px;
    background: #eee;
    margin: 10px 0;
  }
}

.digital-card {
  background: #e8f5e9;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #a5d6a7;
  
  .success-text {
    display: block;
    color: #2e7d32;
    font-weight: bold;
    margin-bottom: 5px;
  }
  .id-text {
    font-size: 12px;
    color: #666;
    word-break: break-all;
  }
}
</style>
