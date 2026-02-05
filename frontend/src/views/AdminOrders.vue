<template>
  <div class="admin-orders">
    <div class="header">
      <h2>订单状态管理</h2>
      <div class="search-bar">
        <input v-model="searchQuery" placeholder="搜索订单号 / SKU / 手机号" class="search-input" />
        <button class="btn btn-primary" @click="search">搜索</button>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>商品名称</th>
            <th>收货人</th>
            <th>C2M 状态</th>
            <th>最近更新</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="order in orders">
            <!-- 遍历订单项，因为状态是 Item 维度的 -->
            <tr v-for="item in order.items" :key="item.id">
              <td>{{ order.orderNo }}</td>
              <td>
                <div class="product-info">
                  <img :src="item.productImage || 'https://via.placeholder.com/40'" class="thumb" />
                  <span>{{ item.productName }}</span>
                </div>
              </td>
              <td>{{ order.userId }} ({{ order.shippingAddress }})</td>
              <td>
                <span class="status-tag" :class="getStatusClass(item.c2mStatus)">
                  {{ getStatusText(item.c2mStatus) }}
                </span>
              </td>
              <td>{{ getLastUpdateTime(item) }}</td>
              <td>
                <div class="actions">
                  <button 
                    v-if="getNextAction(item.c2mStatus)"
                    class="btn btn-sm btn-primary"
                    @click="openActionModal(item, getNextAction(item.c2mStatus))"
                  >
                    {{ getNextAction(item.c2mStatus).label }}
                  </button>
                  <button class="btn btn-sm btn-outline" @click="viewDetail(item)">详情</button>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <!-- 操作确认弹窗 -->
    <div v-if="showActionModal" class="modal-overlay" @click.self="showActionModal = false">
      <div class="modal">
        <h3>确认操作：{{ currentAction.label }}</h3>
        <p>即将变更订单项状态，请确认信息。</p>
        
        <div class="form-group">
          <label>操作备注</label>
          <textarea v-model="actionRemark" placeholder="请输入操作备注（如物流单号、工坊名称等）"></textarea>
        </div>

        <div class="modal-actions">
          <button class="btn" @click="showActionModal = false">取消</button>
          <button class="btn btn-primary" @click="submitAction">确认变更</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../utils/axios'

const orders = ref([])
const searchQuery = ref('')
const showActionModal = ref(false)
const currentItem = ref(null)
const currentAction = ref(null)
const actionRemark = ref('')

// C2M 状态流转定义 (管理员可操作的步骤)
const adminActions = {
  'confirmed': { label: '采发包寄出', nextStatus: 'material', alias: 'material' },
  'material': { label: '工坊签收', nextStatus: 'workshop', alias: 'workshop' },
  'workshop': { label: '开始绣制', nextStatus: 'producing', alias: 'producing' },
  'producing': { label: '成品发货', nextStatus: 'shipped', alias: 'shipped' }
}

const getStatusText = (alias) => {
  const map = {
    'wait': '待开启',
    'confirmed': '已确认',
    'material': '已发包',
    'workshop': '工坊已签收',
    'producing': '绣制中',
    'shipped': '已发货',
    'received': '已完成'
  }
  return map[alias] || alias || '待处理'
}

const getStatusClass = (alias) => {
  if (['received'].includes(alias)) return 'success'
  if (['producing'].includes(alias)) return 'warning'
  if (['shipped'].includes(alias)) return 'info'
  return 'default'
}

const getLastUpdateTime = (item) => {
    // 简化处理，实际应从 timeline 取
    return item.updatedAt || '刚刚'
}

const getNextAction = (currentAlias) => {
  return adminActions[currentAlias]
}

const openActionModal = (item, action) => {
  currentItem.value = item
  currentAction.value = action
  actionRemark.value = ''
  showActionModal.value = true
}

const submitAction = async () => {
  if (!currentItem.value || !currentAction.value) return
  
  try {
    await axios.put(`/orders/items/${currentItem.value.id}/status`, {
      status: currentAction.value.alias,
      operator: '管理员', // 实际应从 Token 解析
      remark: actionRemark.value
    })
    
    alert('操作成功')
    showActionModal.value = false
    loadOrders()
  } catch (e) {
    console.error(e)
    alert('操作失败: ' + (e.message || '未知错误'))
  }
}

const search = () => {
    loadOrders()
}

const loadOrders = async () => {
  try {
    // 暂时复用普通订单接口，实际应有管理员专用的搜索接口
    const res = await axios.get('/orders', { params: { includeInactive: true, keyword: searchQuery.value } })
    if (res.code === 200) {
      orders.value = res.data.records || []
    }
  } catch (e) {
    console.error('加载订单失败', e)
  }
}

const viewDetail = (item) => {
    alert(`SKU: ${item.id}\n商品: ${item.productName}\n当前状态: ${getStatusText(item.c2mStatus)}`)
}

onMounted(loadOrders)
</script>

<style scoped>
.admin-orders { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.search-bar { display: flex; gap: 10px; }
.search-input { padding: 8px; border: 1px solid #ddd; border-radius: 4px; width: 250px; }

.table-container { background: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #eee; }
.data-table th { background: #f9f9f9; font-weight: 600; color: #555; }

.product-info { display: flex; align-items: center; gap: 10px; }
.thumb { width: 40px; height: 40px; border-radius: 4px; object-fit: cover; }

.status-tag { padding: 4px 8px; border-radius: 4px; font-size: 12px; background: #eee; color: #666; }
.status-tag.success { background: #e6fffa; color: #00b894; }
.status-tag.warning { background: #fff8e1; color: #ffa502; }
.status-tag.info { background: #e3f2fd; color: #0984e3; }

.actions { display: flex; gap: 8px; }
.btn-sm { padding: 4px 10px; font-size: 12px; }

.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 1000;
  display: flex; align-items: center; justify-content: center;
}
.modal {
  background: white; padding: 30px; border-radius: 12px; width: 90%; max-width: 400px;
}
.form-group { margin: 20px 0; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; }
.form-group textarea { width: 100%; height: 80px; padding: 8px; border: 1px solid #ddd; border-radius: 4px; resize: none; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; }
</style>
