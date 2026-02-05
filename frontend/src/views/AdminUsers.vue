<template>
  <div class="admin-users">
    <h1>管理员 · 用户管理</h1>
    <table>
      <thead><tr><th>ID</th><th>用户名</th><th>角色</th><th>邮箱</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td><td>{{ u.username }}</td><td>{{ u.role }}</td><td>{{ u.email }}</td>
          <td>
            <button @click="makeAdmin(u)" :disabled="u.role==='ADMIN'">设为管理员</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import axios from '../utils/axios'
export default {
  name: 'AdminUsers',
  data() { return { users: [] } },
  mounted() { this.load() },
  methods: {
    async load() {
      const res = await axios.get('/users', { params: { page: 1, size: 100 } })
      const data = res.data || res
      const pageData = data.data || {}
      this.users = pageData.content || pageData.records || []
    },
    async makeAdmin(u) {
      try { await axios.put(`/user/${u.id}/role`, null, { params: { role: 'ADMIN' } }); await this.load() } catch (e) { alert('更新失败') }
    }
  }
}
</script>

<style scoped>
.admin-users { max-width: 900px; margin: 20px auto }
table { width: 100%; border-collapse: collapse }
th, td { border: 1px solid #eee; padding: 8px }
</style>
