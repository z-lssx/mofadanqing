<template>
  <div class="cart-page">
    <div class="container">
      <h1>我的购物车</h1>
      
      <div v-if="cartState.items.length === 0" class="empty-cart">
        <p>购物车还是空的，去挑选心仪的商品吧！</p>
        <button class="btn btn-primary" @click="$router.push('/products')">去逛逛</button>
      </div>

      <div v-else class="cart-content">
        <!-- 商品列表 -->
        <div class="cart-list">
          <div v-for="item in cartState.items" :key="item.id" class="cart-item">
            <div class="item-img">
              <img :src="item.coverImg || 'https://via.placeholder.com/100'" :alt="item.name">
            </div>
            <div class="item-info">
              <h3>{{ item.name }}</h3>
              <p class="price">¥{{ item.price }}</p>
            </div>
            <div class="item-quantity">
              <button class="btn-qty" @click="cartState.updateQuantity(item.id, item.quantity - 1)">-</button>
              <input type="number" :value="item.quantity" @change="e => cartState.updateQuantity(item.id, parseInt(e.target.value))" min="1">
              <button class="btn-qty" @click="cartState.updateQuantity(item.id, item.quantity + 1)">+</button>
            </div>
            <div class="item-subtotal">
              ¥{{ (item.price * item.quantity).toFixed(2) }}
            </div>
            <div class="item-actions">
              <button class="btn-remove" @click="cartState.removeItem(item.id)">删除</button>
            </div>
          </div>
        </div>

        <!-- 底部结算栏 -->
        <div class="cart-footer">
          <div class="total-info">
            <span>共 {{ cartState.count }} 件商品</span>
            <span class="total-price">合计：¥{{ cartState.totalPrice.toFixed(2) }}</span>
          </div>
          <button class="btn btn-primary btn-checkout" @click="showCheckoutModal = true">去结算</button>
        </div>
      </div>
    </div>

    <!-- 结算弹窗 -->
    <div v-if="showCheckoutModal" class="modal-overlay" @click.self="showCheckoutModal = false">
      <div class="modal">
        <!-- 步骤1: 填写信息 -->
        <template v-if="step === 1">
          <h3>填写订单信息</h3>
          <div class="form-group">
            <label>收货地址</label>
            <textarea v-model="checkoutForm.address" placeholder="请输入详细收货地址"></textarea>
          </div>
          <div class="form-group">
            <label>支付方式</label>
            <select v-model="checkoutForm.paymentMethod">
              <option value="WECHAT">微信支付</option>
              <option value="ALIPAY">支付宝</option>
            </select>
          </div>
          
          <div class="modal-actions">
            <button class="btn" @click="showCheckoutModal = false">取消</button>
            <button class="btn btn-primary" @click="goToPayment">确认支付</button>
          </div>
        </template>

        <!-- 步骤2: 扫码支付 -->
        <template v-else>
          <h3>请扫描二维码支付</h3>
          <div class="qrcode-container">
            <img src="https://via.placeholder.com/200?text=QRCode" alt="收款二维码" />
            <p style="margin-top: 10px; color: #666;">支付金额：<span style="color: #e74c3c; font-weight: bold;">¥{{ cartState.totalPrice.toFixed(2) }}</span></p>
          </div>
          
          <div class="modal-actions">
            <button class="btn" @click="step = 1">返回修改</button>
            <button class="btn btn-primary" @click="submitOrder" :disabled="submitting">
              {{ submitting ? '支付确认中...' : '我已完成支付' }}
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { cartState } from '../utils/cartState'
import axios from '../utils/axios'

const router = useRouter()
const showCheckoutModal = ref(false)
const submitting = ref(false)
const step = ref(1)

const checkoutForm = reactive({
  address: '',
  paymentMethod: 'WECHAT'
})

const goToPayment = () => {
  if (!checkoutForm.address) {
    alert('请填写收货地址')
    return
  }
  step.value = 2
}

const submitOrder = async () => {
  submitting.value = true
  try {
    const payload = {
      items: cartState.items.map(item => ({
        productId: item.productId, // 注意这里字段名变化
        quantity: item.quantity
      })),
      shippingAddress: checkoutForm.address,
      paymentMethod: checkoutForm.paymentMethod
    }

    const res = await axios.post('/orders', payload)
    
    if (res.code === 200) {
      alert('支付成功！订单已创建')
      await cartState.clear() // 调用后端清空接口
      showCheckoutModal.value = false
      router.push('/orders')
    } else {
      alert(res.message || '下单失败')
    }
  } catch (e) {
    console.error(e)
    alert('下单失败，请稍后重试')
  } finally {
    submitting.value = false
    // 重置步骤
    setTimeout(() => { step.value = 1 }, 500)
  }
}
</script>

<style scoped>
.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 20px 0;
}
.cart-page {
  padding: 40px 0;
  min-height: 80vh;
  background-color: #f9f9f9;
}
.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}
h1 { margin-bottom: 30px; text-align: center; color: #333; }

.empty-cart {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 8px;
}
.empty-cart p { margin-bottom: 20px; color: #666; font-size: 18px; }

.cart-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  overflow: hidden;
  margin-bottom: 30px;
}
.cart-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}
.cart-item:last-child { border-bottom: none; }

.item-img img {
  width: 80px; height: 80px; object-fit: cover; border-radius: 4px;
}
.item-info { flex: 1; padding: 0 20px; }
.item-info h3 { margin: 0 0 5px 0; font-size: 16px; color: #333; }
.price { color: #e74c3c; font-weight: bold; }

.item-quantity { display: flex; align-items: center; gap: 5px; }
.btn-qty {
  width: 28px; height: 28px; border: 1px solid #ddd; background: white; cursor: pointer;
}
.item-quantity input {
  width: 40px; text-align: center; border: 1px solid #ddd; padding: 4px;
}

.item-subtotal { width: 100px; text-align: right; font-weight: bold; color: #e74c3c; margin: 0 20px; }

.btn-remove {
  color: #999; border: none; background: none; cursor: pointer;
}
.btn-remove:hover { color: #e74c3c; text-decoration: underline; }

.cart-footer {
  background: white;
  padding: 20px 30px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  position: sticky;
  bottom: 0;
}
.total-info { font-size: 16px; }
.total-price { font-size: 24px; color: #e74c3c; font-weight: bold; margin-left: 20px; }
.btn-checkout { padding: 12px 40px; font-size: 18px; }

/* Modal */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal {
  background: white; padding: 30px; border-radius: 12px; width: 90%; max-width: 400px;
}
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; }
.form-group textarea, .form-group select {
  width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px;
}
.form-group textarea { height: 80px; resize: none; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; }
</style>