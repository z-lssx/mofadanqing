import { reactive } from 'vue'
import axios from './axios'

export const messageState = reactive({
  count: 0,
  
  async fetchCount() {
    try {
      // 仅请求未读数量，减少数据传输
      // 后端 /messages 接口返回分页数据，其中 total 即为总数（当 unread=true 时）
      const res = await axios.get('/messages', { params: { page: 1, size: 1, unread: true } })
      if (res.code === 200) {
        const data = res.data || {}
        // 兼容不同的返回结构
        this.count = data.total !== undefined ? data.total : (data.records ? data.records.length : 0)
      }
    } catch (e) {
      console.error('Failed to fetch message count', e)
    }
  },

  reset() {
    this.count = 0
  }
})
