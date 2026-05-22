<template>
  <view class="edit-container">
    <view class="form-group">
      <view class="form-item">
        <text class="label">收货人</text>
        <input class="input" placeholder="请填写收货人姓名" v-model="form.userName" />
      </view>
      <view class="form-item">
        <text class="label">手机号码</text>
        <input class="input" type="number" placeholder="请填写手机号码" v-model="form.telNumber" />
      </view>
      <view class="form-item">
        <text class="label">所在地区</text>
        <!-- 简化处理，使用 input 代替 picker -->
        <input class="input" placeholder="省市区信息" v-model="form.region" />
      </view>
      <view class="form-item">
        <text class="label">详细地址</text>
        <input class="input" placeholder="街道、楼牌号等" v-model="form.detailInfo" />
      </view>
      <view class="form-item switch-item">
        <text class="label">设为默认地址</text>
        <switch :checked="form.isDefault" color="#00796b" @change="onSwitchChange" />
      </view>
    </view>
    
    <button class="btn-save" @click="save">保存</button>
    <button class="btn-del" v-if="isEdit" @click="del">删除</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const form = ref({
  userName: '',
  telNumber: '',
  region: '',
  detailInfo: '',
  isDefault: false
})
const isEdit = ref(false)
const editIndex = ref(-1)

onLoad((option) => {
  if (option.index !== undefined) {
    isEdit.value = true
    editIndex.value = parseInt(option.index)
    const list = uni.getStorageSync('addressList') || []
    const item = list[editIndex.value]
    if (item) {
      form.value = { ...item, region: item.provinceName + item.cityName + item.countyName }
    }
  }
})

const onSwitchChange = (e) => {
  form.value.isDefault = e.detail.value
}

const save = () => {
  if (!form.value.userName || !form.value.telNumber || !form.value.detailInfo) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  
  const list = uni.getStorageSync('addressList') || []
  
  // 简单解析 region
  const newItem = {
    ...form.value,
    provinceName: form.value.region.substr(0, 3) || '', // Mock parsing
    cityName: '',
    countyName: ''
  }
  
  if (newItem.isDefault) {
    // 如果设为默认，取消其他默认
    list.forEach(i => i.isDefault = false)
  }
  
  if (isEdit.value) {
    list[editIndex.value] = newItem
  } else {
    list.unshift(newItem)
  }
  
  uni.setStorageSync('addressList', list)
  uni.navigateBack()
}

const del = () => {
  uni.showModal({
    title: '提示',
    content: '确定删除该地址吗？',
    success: (res) => {
      if (res.confirm) {
        const list = uni.getStorageSync('addressList') || []
        list.splice(editIndex.value, 1)
        uni.setStorageSync('addressList', list)
        uni.navigateBack()
      }
    }
  })
}
</script>

<style lang="scss">
.edit-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
}

.form-group {
  background: #fff;
  border-radius: 12rpx;
  padding: 0 30rpx;
  margin-bottom: 40rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #eee;
  
  &:last-child { border-bottom: none; }
  
  .label {
    width: 160rpx;
    font-size: 30rpx;
    color: #333;
  }
  
  .input {
    flex: 1;
    font-size: 30rpx;
    color: #333;
  }
  
  &.switch-item {
    justify-content: space-between;
  }
}

.btn-save {
  background: #00796b;
  color: #fff;
  border-radius: 40rpx;
  margin-bottom: 30rpx;
}

.btn-del {
  background: #fff;
  color: #e74c3c;
  border-radius: 40rpx;
}
</style>