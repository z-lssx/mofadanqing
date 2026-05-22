<template>
  <div class="product-detail" v-if="product">
    <div class="header">
      <h1>{{ product.name }}</h1>
      <div class="meta">
        <span class="price">¥{{ product.price }}</span>
        <span class="stock">库存：{{ product.stock }}</span>
      </div>
    </div>
    <div class="content">
      <img :src="product.coverImg || 'https://via.placeholder.com/600x400'" :alt="product.name" />
      <p class="desc">{{ product.description }}</p>
      <div class="actions">
        <button class="btn btn-primary" @click="showOrderModal = true">立即定制</button>
        <button class="btn" @click="goBack">返回</button>
      </div>
    </div>

    <!-- 简易下单弹窗 -->
    <div v-if="showOrderModal" class="modal-overlay" @click.self="showOrderModal = false">
      <div class="modal">
        <h3>确认订单</h3>
        <div class="form-group">
          <label>商品名称</label>
          <input :value="product.name" disabled />
        </div>
        <div class="form-group">
          <label>单价</label>
          <input :value="'¥' + product.price" disabled />
        </div>
        <div class="form-group">
          <label>数量</label>
          <input type="number" v-model.number="orderForm.quantity" min="1" :max="product.stock" />
        </div>
        <div class="form-group">
          <label>收货地址</label>
          <textarea v-model="orderForm.address" placeholder="请输入您的收货地址"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn" @click="showOrderModal = false">取消</button>
          <button class="btn btn-primary" @click="submitOrder" :disabled="submitting">
            {{ submitting ? '提交中...' : '确认支付' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '../utils/axios'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const showOrderModal = ref(false)
const submitting = ref(false)

const orderForm = reactive({
  quantity: 1,
  address: ''
})

const loadProduct = async () => {
  try {
    const id = route.params.id
    const res = await axios.get(`/products/${id}`)
    if (res.code === 200) {
      product.value = res.data
    }
  } catch (e) {
    console.error('加载商品详情失败', e)
  }
}

const submitOrder = async () => {
  if (!orderForm.address) {
    alert('请填写收货地址')
    return
  }
  
  submitting.value = true
  try {
    const payload = {
      items: [
        {
          productId: product.value.id,
          quantity: orderForm.quantity
        }
      ],
      shippingAddress: orderForm.address,
      paymentMethod: 'WECHAT' // 默认支付方式
    }
    
    const res = await axios.post('/orders/create', payload)
    if (res.code === 200) {
      alert('下单成功！')
      showOrderModal.value = false
      router.push('/orders')
    } else {
      alert(res.message || '下单失败')
    }
  } catch (e) {
    console.error('下单异常', e)
    alert('下单失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const goBack = () => router.back()

onMounted(loadProduct)
</script>

<style scoped>
.product-detail { max-width: 960px; margin: 0 auto; padding: 20px; position: relative; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom: 20px; }
.meta { display: flex; gap: 16px; color: #666; }
.price { font-size: 20px; color: #e74c3c; font-weight: 600; }
.content img { width: 100%; max-height: 420px; object-fit: cover; border-radius: 8px; }
.desc { margin-top: 16px; color: #555; line-height: 1.6; }
.actions { margin-top: 20px; display: flex; gap: 12px; }

/* Modal Styles */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal {
  background: white; padding: 30px; border-radius: 12px;
  width: 90%; max-width: 400px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}
.modal h3 { margin-bottom: 20px; text-align: center; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; }
.form-group input, .form-group textarea {
  width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;
}
.form-group textarea { height: 80px; resize: none; }
.modal-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
</style>
