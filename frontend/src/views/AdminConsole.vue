<template>
  <div class="admin-console">
    <h1>管理后台</h1>
    <p>请选择需要管理的模块</p>
    <div class="grid">
      <div class="card" @click="go('/admin/products')">
        <h3>商品管理</h3>
        <p>管理商品信息、库存与上下架</p>
      </div>
      <div class="card" @click="go('/admin/logistics/pack')">
        <h3>采发包管理</h3>
        <p>下发采发包并维护相关信息</p>
      </div>
      <div class="card" @click="go('/admin/logistics/workshop')">
        <h3>工坊签收管理</h3>
        <p>确认工坊签收，进入绣制环节</p>
      </div>
      <div class="card" @click="go('/admin/logistics/production')">
        <h3>绣制管理</h3>
        <p>管理绣制流程与完成时间</p>
      </div>
      <div class="card" @click="go('/admin/logistics/shipment')">
        <h3>成品发货管理</h3>
        <p>维护发货信息与预计送达</p>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: 'AdminConsole',
  methods: {
    go(path) { this.$router.push(path) }
  }
}
</script>
<style scoped>
.admin-console { max-width: 1000px; margin: 20px auto; }
.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.card { border: 1px solid #eee; border-radius: 8px; padding: 16px; cursor: pointer; transition: box-shadow .2s }
.card:hover { box-shadow: 0 6px 16px rgba(0,0,0,0.1) }
.card h3 { margin: 0 0 8px 0 }
.card p { color: #666; margin: 0 }
</style>
