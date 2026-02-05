<template>
  <div class="admin-products">
    <h1>管理员 · 商品管理</h1>
    <div class="actions">
      <button class="btn" @click="openCreate">添加商品</button>
    </div>
    <div class="product-table">
      <table>
        <thead>
          <tr>
            <th>ID</th><th>名称</th><th>价格</th><th>库存</th><th>状态</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in products" :key="p.id">
            <td>{{ p.id }}</td>
            <td>{{ p.name }}</td>
            <td>¥{{ p.price }}</td>
            <td>{{ p.stock }}</td>
            <td>{{ p.status }}</td>
            <td>
              <button @click="edit(p)">编辑</button>
              <button @click="toggleStatus(p)">{{ p.status === 'ACTIVE' ? '下架' : '上架' }}</button>
              <button @click="remove(p)" style="color:#e74c3c">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showDialog" class="dialog">
      <div class="dialog-body">
        <h3>{{ isEdit ? '编辑商品' : '添加商品' }}</h3>
        <div class="form-row"><label>名称</label><input v-model="form.name" /></div>
        <div class="form-row"><label>价格</label><input type="number" v-model.number="form.price" /></div>
        <div class="form-row"><label>库存</label><input type="number" v-model.number="form.stock" /></div>
        <div class="form-row"><label>封面图URL</label><input v-model="form.coverImg" /></div>
        <div class="form-row"><label>上传封面</label>
          <select v-model="uploadStorage">
            <option value="oss">阿里云OSS</option>
            <option value="local">本地</option>
            <option value="mock">示例</option>
          </select>
          <input type="file" @change="onFileChange" accept="image/*" />
        </div>
        <div class="form-row"><label>描述</label><textarea v-model="form.description" /></div>
        <div class="dialog-actions">
          <button @click="save">保存</button>
          <button @click="close">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '../utils/axios'
export default {
  name: 'AdminProducts',
  data() {
    return {
      products: [],
      showDialog: false,
      isEdit: false,
      form: { id: null, name: '', price: 0, stock: 0, coverImg: '', description: '' },
      uploadStorage: 'oss'
    }
  },
  mounted() { this.load() },
  methods: {
    async load() {
      try {
        const res = await axios.get('/products', { params: { page: 1, size: 100, includeInactive: true } })
        if (res.code === 200) {
          const pageData = res.data || {}
          this.products = pageData.content || pageData.records || []
        }
      } catch (e) {
        console.error('加载商品失败', e)
      }
    },
    openCreate() { this.isEdit = false; this.form = { id: null, name: '', price: 0, stock: 0, coverImg: '', description: '' }; this.showDialog = true },
    edit(p) { this.isEdit = true; this.form = { id: p.id, name: p.name, price: p.price, stock: p.stock, coverImg: p.coverImg, description: p.description }; this.showDialog = true },
    close() { this.showDialog = false },
    async save() {
      try {
        if (this.isEdit) {
          await axios.put(`/products/${this.form.id}`, this.form)
        } else {
          await axios.post('/products', this.form)
        }
        this.showDialog = false
        await this.load()
        alert('保存成功')
      } catch (e) { alert('保存失败') }
    },
    async remove(p) {
      if (!confirm(`确认删除商品 ${p.name} ?`)) return
      try { await axios.delete(`/products/${p.id}`); await this.load(); alert('删除成功') } catch (e) { alert('删除失败') }
    },
    async toggleStatus(p) {
      const next = p.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
      try { await axios.put(`/products/${p.id}/status`, null, { params: { status: next } }); await this.load() } catch (e) { alert('更新失败') }
    },
    async onFileChange(e) {
      const file = e.target.files && e.target.files[0]
      if (!file) return
      const fd = new FormData()
      fd.append('file', file)
      fd.append('storage', this.uploadStorage)
      try {
        const r = await axios.post('/upload/image', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
        const data = r.data || r
        const url = data.url || ''
        if (url) this.form.coverImg = url
        alert('上传成功')
      } catch (err) { alert('上传失败') }
    }
  }
}
</script>

<style scoped>
.admin-products { max-width: 1000px; margin: 0 auto; padding: 20px }
.product-table table { width: 100%; border-collapse: collapse }
.product-table th, .product-table td { border: 1px solid #eee; padding: 8px }
.actions { margin: 10px 0 }
.dialog { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center }
.dialog-body { background: #fff; padding: 20px; width: 500px; border-radius: 8px }
.form-row { display: flex; gap: 10px; margin: 10px 0 }
.form-row label { width: 80px; color: #666 }
.dialog-actions { display: flex; gap: 10px; justify-content: flex-end }
</style>
