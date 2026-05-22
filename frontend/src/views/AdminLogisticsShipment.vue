<template>
  <div class="admin-logistics">
    <h1>成品发货管理</h1>
    <div class="filters">
      <input v-model="q.username" placeholder="用户名" />
      <input v-model="q.userId" placeholder="用户ID" />
      <input v-model="q.orderNo" placeholder="订单号" />
      <select v-model="q.status">
        <option value="">全部</option>
        <option value="shipped">成品发货</option>
      </select>
      <button class="btn" @click="load">查询</button>
    </div>
    <table>
      <thead>
        <tr>
          <th>ID</th><th>订单号</th><th>用户ID</th><th>昵称</th><th>SKU</th><th>数量</th><th>状态</th><th>备注</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in records" :key="r.id">
          <td>{{ r.id }}</td>
          <td>{{ r.orderNo }}</td>
          <td>{{ r.userId }}</td>
          <td>{{ r.userNickname }}</td>
          <td>{{ r.sku }}</td>
          <td>{{ r.quantity }}</td>
          <td>{{ r.status }}</td>
          <td>{{ r.remark }}</td>
          <td>
            <button @click="openEdit(r)">编辑</button>
            <button @click="openEdit(r)" style="margin-left: 5px;">上传素材</button>
            <button @click="remove(r)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="pager">
      <button @click="prev" :disabled="page<=1">上一页</button>
      <span>{{ page }} / {{ pages }} (Total: {{ total }})</span>
      <button @click="next" :disabled="page>=pages">下一页</button>
    </div>
    <div v-if="show" class="drawer">
      <div class="drawer-body">
        <h3>编辑</h3>
        <div class="row"><label>物流单号</label><input v-model="form.logisticsNo" /></div>
        <div class="row"><label>预计送达</label><input v-model="form.expectedFinishTime" type="datetime-local" /></div>
        <div class="row"><label>备注</label><input v-model="form.remark" /></div>

        <div class="row" style="display: block;">
          <label style="margin-bottom: 8px; display: block;">上传多媒体</label>
          <MediaUpload v-model="form.mediaUrls" :limit="5" />
        </div>

        <div class="actions">
          <button @click="save">保存</button>
          <button @click="show=false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import axios from '../utils/axios'
import MediaUpload from '../components/MediaUpload.vue'

export default {
  name: 'AdminLogisticsShipment',
  components: { MediaUpload },
  data() {
    return {
      q: { username: '', userId: '', orderNo: '', status: 'shipped' },
      page: 1, size: 20, records: [], total: 0, pages: 0,
      show: false, form: {}
    }
  },
  mounted() { this.load() },
  methods: {
    async load() {
      const params = { page: this.page, size: this.size, username: this.q.username || undefined, userId: this.q.userId || undefined, orderNo: this.q.orderNo || undefined, status: this.q.status || undefined }
      const res = await axios.get('/admin/logistics/shipment/list', { params })
      if (res && res.code === 200) {
        const p = res.data || {}
        this.records = p.records || p.content || []
        this.total = p.total || 0
        this.pages = p.pages || 0
      } else {
        this.records = []
        this.total = 0
        this.pages = 0
      }
    },
    prev() { if (this.page>1) { this.page--; this.load() } },
    next() { if (this.page < this.pages) { this.page++; this.load() } },
    openEdit(r) { 
      this.form = { 
        id: r.id, 
        logisticsNo: r.logisticsNo || '', 
        expectedFinishTime: r.expectedFinishTime || '', 
        remark: r.remark || '',
        mediaUrls: r.mediaUrls || ''
      }
      this.show = true 
    },
    async save() { await axios.put(`/admin/logistics/shipment/${this.form.id}`, this.form); this.show=false; this.load() },
    async remove(r) { await axios.delete(`/admin/logistics/shipment/${r.id}`); this.load() }
  }
}
</script>
<style scoped>
.admin-logistics { max-width: 1100px; margin: 20px auto; }
.filters { display: flex; gap: 8px; margin-bottom: 10px; }
table { width: 100%; border-collapse: collapse }
th, td { border: 1px solid #eee; padding: 8px }
.pager { margin-top: 10px; display: flex; gap: 10px; align-items: center }
.drawer { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; justify-content: flex-end; z-index: 1000 }
.drawer-body { width: 360px; background: #fff; padding: 16px }
.row { display: flex; gap: 10px; align-items: center; margin: 10px 0 }
.row label { width: 100px }
.actions { display: flex; gap: 8px; justify-content: flex-end }
</style>
