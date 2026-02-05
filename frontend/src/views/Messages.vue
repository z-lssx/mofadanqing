<template>
  <div class="messages">
    <h1>消息中心</h1>
    <div class="toolbar" style="display: flex; gap: 10px;">
      <button class="btn" @click="load">刷新</button>
      <button class="btn" @click="batchRead" :disabled="selectedIds.length===0">批量已读</button>
      <button class="btn btn-outline" @click="batchDelete" :disabled="selectedIds.length===0">批量删除</button>
    </div>
    <table>
      <thead>
        <tr>
          <th style="width: 40px"><input type="checkbox" :checked="allSelected" @change="toggleAll"></th>
          <th>ID</th><th>标题</th><th>内容</th><th>时间</th><th>状态</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="m in messages" :key="m.id">
          <td><input type="checkbox" :checked="selectedIds.includes(m.id)" @change="toggleSelect(m.id)"></td>
          <td>{{ m.id }}</td>
          <td>{{ m.title }}</td>
          <td>{{ m.content }}</td>
          <td>{{ format(m.createdAt) }}</td>
          <td>{{ m.isRead ? '已读' : '未读' }}</td>
          <td>
            <button v-if="!m.isRead" @click="read(m)" style="margin-right: 5px">已读</button>
            <button @click="del(m)" style="color: red">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="pager">
      <button @click="prev" :disabled="page<=1">上一页</button>
      <span>{{ page }} / {{ pages }} (Total: {{ total }})</span>
      <button @click="next" :disabled="page>=pages">下一页</button>
    </div>
  </div>
</template>
<script>
import axios from '../utils/axios'
import { messageState } from '../utils/messageState'

export default {
  name: 'Messages',
  data() {
    return { page: 1, size: 20, messages: [], total: 0, pages: 0, selectedIds: [] }
  },
  computed: {
    allSelected() {
      return this.messages.length > 0 && this.selectedIds.length === this.messages.length
    }
  },
  mounted() { this.load() },
  methods: {
    async load() {
      this.selectedIds = [] // 翻页或刷新清空选中
      try {
        const res = await axios.get('/messages', { params: { page: this.page, size: this.size } })
        if (res && res.code === 200) {
          const p = res.data || {}
          this.messages = p.records || p.content || []
          this.total = p.total || 0
          this.pages = p.pages || 0
        } else {
          this.messages = []
          this.total = 0
          this.pages = 0
        }
      } catch (e) {
        console.error('Failed to load messages', e)
      }
    },
    prev() { if (this.page>1) { this.page--; this.load() } },
    next() { if (this.page<this.pages) { this.page++; this.load() } },
    
    async read(m) { 
      try {
        await axios.put(`/messages/${m.id}/read`)
        await this.load() 
        messageState.fetchCount() // 实时更新红点
      } catch(e) { console.error(e) }
    },
    
    async del(m) { 
      if (!confirm('删除?')) return
      try {
        await axios.delete(`/messages/${m.id}`)
        await this.load() 
        messageState.fetchCount() // 实时更新红点
      } catch(e) { console.error(e) }
    },
    
    toggleSelect(id) {
      if (this.selectedIds.includes(id)) {
        this.selectedIds = this.selectedIds.filter(i => i !== id)
      } else {
        this.selectedIds.push(id)
      }
    },
    toggleAll() {
      if (this.allSelected) {
        this.selectedIds = []
      } else {
        this.selectedIds = this.messages.map(m => m.id)
      }
    },
    
    async batchRead() {
      if (this.selectedIds.length === 0) return
      try {
        await axios.post('/messages/batch/read', this.selectedIds)
        await this.load()
        messageState.fetchCount() // 实时更新红点
      } catch(e) { console.error(e) }
    },
    
    async batchDelete() {
      if (this.selectedIds.length === 0) return
      if (!confirm('确定删除选中消息？')) return
      try {
        await axios.post('/messages/batch/delete', this.selectedIds)
        await this.load()
        messageState.fetchCount() // 实时更新红点
      } catch(e) { console.error(e) }
    },
    
    format(s) { if (!s) return ''; return new Date(s).toLocaleString() }
  }
}
</script>
<style scoped>
.messages { max-width: 900px; margin: 20px auto }
.toolbar { margin-bottom: 10px }
table { width: 100%; border-collapse: collapse }
th, td { border: 1px solid #eee; padding: 8px }
.pager { margin-top: 10px; display: flex; gap: 10px; align-items: center }
</style>
