import { reactive, computed } from 'vue'
import axios from './axios'

// 内部响应式状态
const state = reactive({
  items: [],
  loading: false
})

export const cartState = reactive({
  items: computed(() => state.items),
  
  count: computed(() => state.items.reduce((sum, item) => sum + item.quantity, 0)),
  
  totalPrice: computed(() => state.items.reduce((sum, item) => sum + (item.price || 0) * item.quantity, 0)),

  // 初始化加载
  async fetchCart() {
    state.loading = true
    try {
      const res = await axios.get('/cart')
      if (res.code === 200) {
        state.items = res.data.map(item => ({
          id: item.id, // cart item id
          productId: item.productId,
          name: item.productName,
          price: item.price,
          coverImg: item.productImage,
          quantity: item.quantity
        }))
      }
    } catch (e) {
      console.error(e)
    } finally {
      state.loading = false
    }
  },

  async addItem(product) {
    try {
      await axios.post('/cart', {
        productId: product.id,
        quantity: 1
      })
      await this.fetchCart()
      return true
    } catch (e) {
      console.error(e)
      alert('添加失败，请先登录')
      return false
    }
  },

  async updateQuantity(cartItemId, quantity) {
    if (quantity <= 0) {
      await this.removeItem(cartItemId)
      return
    }
    try {
      await axios.put(`/cart/${cartItemId}`, { quantity })
      await this.fetchCart()
    } catch (e) {
      console.error(e)
    }
  },

  async removeItem(cartItemId) {
    try {
      await axios.delete(`/cart/${cartItemId}`)
      await this.fetchCart()
    } catch (e) {
      console.error(e)
    }
  },

  async clear() {
    try {
      await axios.delete('/cart')
      state.items = []
    } catch (e) {
      console.error(e)
    }
  },
  
  // 退出登录时清除本地状态
  reset() {
    state.items = []
  }
})
