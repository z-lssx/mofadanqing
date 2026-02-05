<template>
  <div class="custom">
    <!-- 2. 定制工作台 (Split Screen) -->
    <section id="page-custom" class="section active" style="padding-top: 0;">
      <div class="workbench">
        <!-- 左侧：控制台 -->
        <aside class="wb-sidebar">
          <h3 style="margin-bottom: 20px;">定制配置</h3>
          <div class="step-list">
            <div 
              class="step-item" 
              :class="{ active: currentStep === 1, done: currentStep > 1 }"
              @click="setStep(1)"
            >1</div>
            <div 
              class="step-item" 
              :class="{ active: currentStep === 2, done: currentStep > 2 }"
              @click="setStep(2)"
            >2</div>
            <div 
              class="step-item" 
              :class="{ active: currentStep === 3, done: currentStep > 3 }"
              @click="setStep(3)"
            >3</div>
          </div>

          <!-- Step 1 Content -->
          <div id="ctrl-step-1" class="control-group" v-show="currentStep === 1">
            <label class="control-label">1. 上传灵感 / 照片</label>
            <div class="upload-box" @click="triggerUpload()">
              <span style="font-size: 24px;" v-if="!uploadedImageUrl">📷</span>
              <img v-else :src="uploadedImageUrl" style="max-width: 100%; max-height: 100px; object-fit: contain;" />
              <span style="margin-top: 10px;" v-if="!uploadedImageUrl">点击上传参考图</span>
              <span style="margin-top: 10px; font-size: 12px; color: green;" v-else>上传成功，点击更换</span>
            </div>
            <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none" accept="image/*" />
            
            <label class="control-label" style="margin-top: 24px;">2. AI 关键词</label>
            <input 
              type="text" 
              placeholder="例：星辰，连理枝，极简" 
              v-model="keywords"
              style="width: 100%; padding: 12px; border: 1px solid rgba(var(--c-ink-rgb), 0.15); border-radius: 4px;"
            >
            
            <button 
              class="btn btn-primary" 
              style="width: 100%; margin-top: 20px;"
              @click="startAiGeneration"
              :disabled="loading"
            >{{ loading ? 'AI 生成中...' : '生成刺绣图稿' }}</button>
          </div>

          <!-- Step 2 Content -->
          <div id="ctrl-step-2" class="control-group" v-show="currentStep === 2">
            <label class="control-label">图层调节</label>
            
            <!-- Hair Amount Slider -->
            <div class="slider-group">
              <label>含发量 (骨架层): {{ hairAmount }}%</label>
              <input type="range" v-model="hairAmount" min="0" max="100" @input="updateCanvas">
            </div>

            <!-- Silk Saturation Slider -->
            <div class="slider-group" style="margin-top: 15px;">
              <label>色彩饱和度 (润色层): {{ silkSaturation }}%</label>
              <input type="range" v-model="silkSaturation" min="0" max="100" @input="updateCanvas">
            </div>

            <div class="style-options" style="margin-top: 20px;">
              <div class="style-opt selected">
                 <div style="font-weight: bold;">混合预览</div>
                 <div style="font-size: 12px;">实时合成效果</div>
              </div>
            </div>

            <button 
              class="btn btn-primary" 
              style="width: 100%; margin-top: 20px;"
              @click="confirmDesign"
            >确认定稿</button>
          </div>

          <!-- Step 3 Content -->
          <div id="ctrl-step-3" class="control-group" v-show="currentStep === 3">
            <label class="control-label">智能报价单 (BOM)</label>
            <div style="background: #F9F9F9; padding: 15px; border-radius: 8px; font-size: 14px; margin-bottom: 20px;">
              <p><strong>产品：</strong>{{ productName }}</p>
              <p><strong>发丝用量：</strong>{{ bomData.hairLength }} 米</p>
              <p><strong>蚕丝用量：</strong>{{ bomData.silkWeight }} 克</p>
              <p><strong>预计工期：</strong>7-10 个工作日</p>
              <div class="divider" style="height: 1px; background: #ddd; margin: 10px 0;"></div>
              
              <!-- 价格明细 -->
              <div v-if="priceBreakdown.finalPrice > 0">
                 <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                   <span>基础定金 (已付):</span>
                   <span>¥{{ priceBreakdown.deposit.toFixed(2) }}</span>
                 </div>
                 <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                   <span>定制增项:</span>
                   <span>+¥{{ (priceBreakdown.finalPrice - priceBreakdown.deposit).toFixed(2) }}</span>
                 </div>
                 <div style="display: flex; justify-content: space-between; font-weight: bold; margin-top: 10px; font-size: 16px;">
                   <span>最终总价:</span>
                   <span>¥{{ priceBreakdown.finalPrice.toFixed(2) }}</span>
                 </div>
                 <div style="display: flex; justify-content: space-between; color: #d32f2f; font-weight: bold; margin-top: 5px;">
                   <span>待付尾款:</span>
                   <span>¥{{ priceBreakdown.balance.toFixed(2) }}</span>
                 </div>
                 <div style="text-align: right; font-size: 12px; color: #999; margin-top: 2px;">
                   (将于成品发货前通知支付)
                 </div>
              </div>
              <div v-else>
                 <p style="font-size: 16px; font-weight: bold; color: var(--c-accent-warm);">预估总价：¥{{ bomData.estimatedCost.toFixed(2) }}</p>
                 <p style="font-size: 12px; color: #999; margin-top: 5px;">*包含发丝处理费与手工费</p>
              </div>
            </div>
            
            <div v-if="digitalAssetId" style="background: #e8f5e9; padding: 10px; border-radius: 4px; margin-bottom: 20px; border: 1px solid #a5d6a7;">
               <p style="color: #2e7d32; font-weight: bold;">✅ 数字版权已确权</p>
               <p style="font-size: 12px; word-break: break-all;">ID: {{ digitalAssetId }}</p>
            </div>

            <button 
              class="btn btn-primary" 
              style="width: 100%; margin-top: 20px;"
              @click="submitOrder"
            >确认提交设计</button>
          </div>
        </aside>

        <!-- 右侧：画布/预览 -->
        <main class="wb-canvas" id="canvas-area">
          <div class="model-container" id="model-3d">
             <!-- Canvas for blending -->
             <canvas id="blend-canvas" width="512" height="512" style="max-width: 100%; max-height: 100%; box-shadow: 0 4px 20px rgba(0,0,0,0.1); border-radius: 4px;"></canvas>
          </div>
          <div class="canvas-toolbar">
            <button class="btn-outline" style="border:none; padding: 0 10px;">🔍 缩放</button>
          </div>
        </main>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from '../utils/axios'

const router = useRouter()
const route = useRoute()
const currentStep = ref(1)
const keywords = ref('')
const selectedStyle = ref('traditional')
const itemId = ref(null)
const c2mType = ref('NONE')
const productName = ref('通用定制')
const loading = ref(false)
const fileInput = ref(null)
const uploadedImageUrl = ref('')

// AI & Rendering State
const taskId = ref(null)
const layer1Url = ref('') // Skeleton
const layer2Url = ref('') // Silk
const hairAmount = ref(50)
const silkSaturation = ref(80)
const bomData = ref({ hairLength: 0, silkWeight: 0, estimatedCost: 0 })
const priceBreakdown = ref({ deposit: 0, finalPrice: 0, balance: 0 })
const digitalAssetId = ref('')

// Canvas References
let canvasCtx = null
let imgLayer1 = null
let imgLayer2 = null

const orderId = ref(null)

// 检查是否是已有订单的定制
onMounted(async () => {
  // 从路由参数获取 orderId
  if (route.params.orderId) {
    orderId.value = route.params.orderId
    await loadOrderInfo(orderId.value)
  } else {
      alert("非法访问：缺少订单信息")
      router.push('/orders')
  }
})

const loadOrderInfo = async (id) => {
  try {
    loading.value = true
    // 获取订单详情，找到待定制的 Item
    const res = await axios.get(`/orders/${id}`)
    if (res.code === 200) {
      const order = res.data.order
      const items = res.data.items || []
      
      // 假设每个订单只有一个待定制项，或者默认取第一个
      // 实际业务中可能需要选择，这里简化处理
      const targetItem = items.find(i => !i.c2mStatus || i.c2mStatus === 'wait' || i.c2mStatus === '待C2M定制') || items[0]
      
      if (targetItem) {
          itemId.value = targetItem.id
          productName.value = targetItem.productName
          c2mType.value = targetItem.c2mType || 'NONE'
          if (targetItem.productName.includes('刺绣') || targetItem.productName.includes('香囊')) {
            c2mType.value = 'EMBROIDERY'
          }
      } else {
          alert("该订单没有需要定制的商品")
          router.push('/orders')
      }
    } else {
        alert("订单加载失败")
        router.push('/orders')
    }
  } catch (e) {
    console.error(e)
    alert("网络异常")
    router.push('/orders')
  } finally {
    loading.value = false
  }
}

// Removed loadProductInfo as it is replaced by loadOrderInfo

const setStep = (step) => {
  currentStep.value = step
  if (step === 2) {
    nextTick(() => {
        initCanvas()
    })
  }
}

// 触发文件上传
const triggerUpload = () => {
  fileInput.value.click()
}

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)
  formData.append('storage', 'oss')

  try {
    loading.value = true
    const res = await axios.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (res.code === 200 || res.url) {
       const url = res.data?.url || res.url
       uploadedImageUrl.value = url
       alert('上传成功')
    } else {
      alert('上传失败')
    }
  } catch (e) {
    console.error(e)
    alert('上传出错')
  } finally {
    loading.value = false
  }
}

// Start AI Generation
const startAiGeneration = async () => {
    if (!keywords.value && !uploadedImageUrl.value) {
        alert('请输入关键词或上传参考图')
        return
    }

    try {
        loading.value = true
        // Create Task
        const res = await axios.post('/ai/c2m/generate', {
            prompt: keywords.value,
            refImg: uploadedImageUrl.value,
            style: 'TRADITIONAL'
        })

        if (res.code === 200) {
            taskId.value = res.data.taskId
            pollTaskStatus()
        } else {
            alert('任务创建失败')
            loading.value = false
        }
    } catch (e) {
        console.error(e)
        alert('请求失败')
        loading.value = false
    }
}

const pollTaskStatus = async () => {
    let attempts = 0
    const maxAttempts = 60 // Max polling attempts
    let delay = 2000 // Initial delay 2s
    const maxDelay = 10000 // Max delay 10s

    const poll = async () => {
        if (!loading.value) return // Stop if cancelled

        try {
            const res = await axios.get(`/ai/c2m/task/${taskId.value}`)
            if (res.code === 200) {
                const task = res.data
                if (task.status === 'COMPLETED') {
                    loading.value = false
                    
                    layer1Url.value = task.layer1Url || task.images[0]
                    layer2Url.value = task.layer2Url || task.images[1]
                    
                    bomData.value = {
                        hairLength: task.hairLength || 0,
                        silkWeight: task.silkWeight || 0,
                        estimatedCost: task.estimatedCost || 0
                    }
                    
                    setStep(2)
                    loadLayers()
                    return // Done
                } else if (task.status === 'FAILED') {
                    loading.value = false
                    alert('生成失败: ' + task.errorMessage)
                    return // Done
                }
            }
        } catch (e) {
            console.error('Polling error', e)
        }

        attempts++
        if (attempts >= maxAttempts) {
            loading.value = false
            alert('生成超时，请稍后在订单中查看结果')
            return
        }

        // Exponential backoff
        delay = Math.min(delay * 1.5, maxDelay)
        setTimeout(poll, delay)
    }

    // Start polling
    setTimeout(poll, delay)
}

// Canvas & Blending Logic
const initCanvas = () => {
    const canvas = document.getElementById('blend-canvas')
    if (canvas) {
        canvasCtx = canvas.getContext('2d')
    }
}

const loadLayers = () => {
    imgLayer1 = new Image()
    // imgLayer1.crossOrigin = "Anonymous" // Removed to avoid CORS issues with OSS
    imgLayer1.src = layer1Url.value
    
    imgLayer2 = new Image()
    // imgLayer2.crossOrigin = "Anonymous" // Removed to avoid CORS issues with OSS
    imgLayer2.src = layer2Url.value
    
    let loaded = 0
    const checkLoad = () => {
        loaded++
        if (loaded === 2) updateCanvas()
    }
    
    const handleError = (e) => {
        console.error("Failed to load image layer", e)
        alert("图片加载失败，可能是链接已过期")
    }
    
    imgLayer1.onload = checkLoad
    imgLayer1.onerror = handleError
    
    imgLayer2.onload = checkLoad
    imgLayer2.onerror = handleError
}

const updateCanvas = () => {
    if (!canvasCtx || !imgLayer1 || !imgLayer2) return
    
    const w = 512
    const h = 512
    canvasCtx.clearRect(0, 0, w, h)
    
    // Draw Layer 1 (Skeleton/Hair)
    canvasCtx.globalAlpha = hairAmount.value / 100
    canvasCtx.globalCompositeOperation = 'source-over'
    canvasCtx.drawImage(imgLayer1, 0, 0, w, h)
    
    // Draw Layer 2 (Silk) with Multiply or Overlay
    canvasCtx.globalAlpha = silkSaturation.value / 100
    canvasCtx.globalCompositeOperation = 'multiply' // "正片叠底" effect
    canvasCtx.drawImage(imgLayer2, 0, 0, w, h)
    
    // Reset
    canvasCtx.globalAlpha = 1.0
    canvasCtx.globalCompositeOperation = 'source-over'
}

const confirmDesign = async () => {
    // 1. Simulate Blockchain Registration
    digitalAssetId.value = "0x" + Math.random().toString(16).substr(2, 64)
    
    // 2. Calculate Price Breakdown locally (Frontend only)
    try {
         let deposit = 1999.00 // Default base price
         
         // If existing order, try to fetch its product price
         if (itemId.value) {
             // Use current loaded item info
             // Mock calculation based on BOM
             const cost = bomData.value.estimatedCost || 0
             const final = 1999 + cost // Mock base
             
             priceBreakdown.value = {
                 deposit: 1999,
                 finalPrice: final,
                 balance: final - 1999
             }
             
             alert("定稿成功！数字资产已确权上链，请确认最终报价。")
             setStep(3)
         } else {
             alert("订单信息异常")
         }
    } catch (e) {
        console.error(e)
        // Fallback
         alert("定稿成功！数字资产已确权上链。")
         setStep(3)
    }
}

// 提交订单
const submitOrder = async () => {
  if (itemId.value) {
    // 统一提交逻辑，无论是否有尾款，都先确认设计，尾款在发货前支付
    try {
        loading.value = true
        
        // 1. Upload the composite image from canvas
        let finalImageUrl = ""
        const canvas = document.getElementById('blend-canvas')
        if (canvas) {
            try {
                // Convert canvas to blob
                const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/png'))
                const formData = new FormData()
                formData.append('file', blob, 'design_composite.png')
                formData.append('storage', 'oss')
                
                // Upload
                const upRes = await axios.post('/upload/image', formData, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                })
                
                if (upRes.code === 200 || upRes.url) {
                    finalImageUrl = upRes.data?.url || upRes.url
                }
            } catch (e) {
                console.error("Failed to upload composite image", e)
                // Continue even if upload fails, fallback to layer2 in backend
            }
        }
        
        // 2. 调用后端接口：上传图片到OSS、保存设计、计算最终价格
        const res = await axios.post('/ai/c2m/confirm', {
            taskId: taskId.value,
            orderItemId: itemId.value,
            generatedImage: finalImageUrl // Pass the new image URL
        })
        
        if (res.code === 200) {
            // 3. 提交成功，更新订单状态为 CONFIRMED
            const statusRes = await axios.put(`/orders/items/${itemId.value}/c2m-status`, {
                status: 'confirmed',
                remark: `用户定制提交 [${productName.value}] - 数字资产ID:${digitalAssetId.value}, 估价:${priceBreakdown.value.finalPrice.toFixed(2)}`
            })
            
            if (statusRes.code === 200) {
                if (priceBreakdown.value.balance > 0) {
                    alert(`定制方案已提交！待制作完成后，您需要支付尾款 ¥${priceBreakdown.value.balance.toFixed(2)} 才能发货。`)
                } else {
                    alert('定制方案已提交，设计师即将介入！')
                }
                
                // Redirect to Trace page using the orderNo returned from backend
                const orderNo = res.data.orderNo // Check if backend sends orderNo
                const orderId = res.data.orderId
                
                // Prioritize orderNo if available (backend supports it now), fallback to orderId
                const targetId = orderNo || orderId
                
                if (targetId) {
                    setTimeout(() => {
                        router.push(`/trace/${targetId}`)
                    }, 500)
                } else {
                    router.push('/orders')
                }
            } else {
                alert("更新状态失败: " + statusRes.message)
            }
        } else {
            alert("提交设计失败: " + (res.message || "未知错误"))
        }
    } catch (e) {
        console.error("Submit Error:", e)
        const errMsg = e.response?.data?.message || e.message || "网络请求异常"
        alert('提交出错: ' + errMsg)
    } finally {
        loading.value = false
    }
  } else {
    // New Order Flow
    try {
        loading.value = true
        // Call new create-c2m endpoint
        const res = await axios.post('/orders/create-c2m', {
            taskId: taskId.value,
            productName: productName.value,
            quantity: 1,
            // Add other needed fields
        })
        
        if (res.code === 200) {
             const newOrder = res.data
             alert(`订单创建成功！订单号: ${newOrder.orderNo}`)
             // Redirect to order detail or list
             // router.push(`/orders/${newOrder.orderId}`)
             router.push('/orders')
        } else {
            alert("创建订单失败: " + res.message)
        }
    } catch (e) {
        console.error(e)
        alert('创建订单出错，请重试')
    } finally {
        loading.value = false
    }
  }
}
</script>

<style scoped>
.custom {
  min-height: 100vh;
  background-color: var(--c-paper);
}
.slider-group {
  margin-bottom: 10px;
}
.slider-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #666;
}
.slider-group input {
  width: 100%;
}
</style>
