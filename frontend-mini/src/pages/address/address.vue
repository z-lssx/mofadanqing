<template>
  <view class="address-container">
    <view class="address-list">
      <view 
        class="address-item" 
        v-for="(item, index) in addressList" 
        :key="index"
        @click="selectAddress(item)"
      >
        <view class="info">
          <view class="row1">
            <text class="name">{{ item.userName }}</text>
            <text class="tel">{{ item.telNumber }}</text>
            <text class="tag" v-if="item.isDefault">默认</text>
          </view>
          <view class="row2">{{ item.provinceName }}{{ item.cityName }}{{ item.countyName }} {{ item.detailInfo }}</view>
        </view>
        <view class="edit-icon" @click.stop="editAddress(item, index)">✎</view>
      </view>
      
      <view class="empty" v-if="addressList.length === 0">暂无收货地址</view>
    </view>
    
    <view class="footer-btn">
      <button class="btn-add" @click="addAddress">+ 新增收货地址</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'

const addressList = ref([])
const isSelectMode = ref(false)

onShow(() => {
  // 从本地存储加载地址列表
  const list = uni.getStorageSync('addressList') || []
  addressList.value = list
  
  // 检查是否是选择模式
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  // 简单判断：如果上一页是 payment，则是选择模式
  // 但这里其实不需要特别判断，点击行为由业务决定
  // 我们可以约定：如果 URL 参数 type=select，则点击是选择
  // 这里简化处理：总是点击选择并返回，如果是管理入口进入，返回也没关系（或者不返回）
  
  // 更好的方式：检查 options
  // 由于 onShow拿不到 options，需在 onLoad 拿。这里简化，默认点击就是选择。
})

const selectAddress = (item) => {
  // 存储选中的地址
  uni.setStorageSync('selectedAddress', item)
  uni.navigateBack()
}

const addAddress = () => {
  uni.navigateTo({ url: '/pages/address/address-edit' })
}

const editAddress = (item, index) => {
  // 传递索引或ID以便编辑
  uni.navigateTo({ url: `/pages/address/address-edit?index=${index}` })
}
</script>

<style lang="scss">
.address-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}

.address-list {
  padding: 20rpx;
}

.address-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  
  .info {
    flex: 1;
    margin-right: 20rpx;
    
    .row1 {
      display: flex;
      align-items: center;
      margin-bottom: 10rpx;
      
      .name { font-size: 32rpx; font-weight: bold; color: #333; margin-right: 20rpx; }
      .tel { font-size: 28rpx; color: #666; margin-right: 20rpx; }
      .tag {
        font-size: 20rpx;
        color: #fff;
        background: #e74c3c;
        padding: 2rpx 8rpx;
        border-radius: 4rpx;
      }
    }
    
    .row2 {
      font-size: 26rpx;
      color: #666;
      line-height: 1.4;
    }
  }
  
  .edit-icon {
    padding: 20rpx;
    color: #999;
    font-size: 40rpx;
  }
}

.empty {
  text-align: center;
  padding: 100rpx;
  color: #999;
}

.footer-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  
  .btn-add {
    background: #00796b;
    color: #fff;
    border-radius: 40rpx;
    font-size: 30rpx;
  }
}
</style>