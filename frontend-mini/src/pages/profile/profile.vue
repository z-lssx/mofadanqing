<template>
  <view class="profile-container">
    <!-- 1. 头部卡片 -->
    <view class="header-card">
      <view class="user-info" v-if="isLogin">
        <!-- 支持显示头像图片 -->
        <image v-if="userInfo.avatar && userInfo.avatar.startsWith('http')" :src="userInfo.avatar" class="avatar-img" mode="aspectFill"></image>
        <view v-else class="avatar">{{ (userInfo.username || 'U')[0] }}</view>
        
        <view class="text-info">
          <!-- 优先显示昵称 -->
          <text class="username">{{ userInfo.nickname || userInfo.username }}</text>
          <text class="uid">ID: {{ userInfo.id }}</text>
        </view>
        <view class="edit-btn" @click="navigateToEdit">✎</view>
      </view>
      <view class="user-info" v-else @click="goToLogin">
        <view class="avatar">未</view>
        <view class="text-info">
          <text class="username">点击登录</text>
          <text class="uid">登录后享受更多权益</text>
        </view>
      </view>
    </view>
    
    <!-- 2. 功能列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="navigateTo('/pages/orders/orders')">
        <text class="icon">📦</text>
        <text class="label">我的订单</text>
        <text class="arrow">→</text>
      </view>
      <view class="menu-item" @click="navigateTo('/pages/custom/custom')">
        <text class="icon">✨</text>
        <text class="label">开始新定制</text>
        <text class="arrow">→</text>
      </view>
      <view class="menu-item" @click="navigateTo('/pages/address/address')">
        <text class="icon">📍</text>
        <text class="label">收货地址</text>
        <text class="arrow">→</text>
      </view>
      <view class="menu-item">
        <text class="icon">📞</text>
        <text class="label">联系客服</text>
        <text class="arrow">→</text>
      </view>
    </view>
    
    <!-- 3. 退出按钮 -->
    <button class="btn-logout" v-if="isLogin" @click="logout">退出登录</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import request from '../../utils/request'

const isLogin = ref(false)
const userInfo = ref({})

// 每次页面显示都执行
onShow(() => {
  checkLogin()
})

const checkLogin = () => {
  const token = uni.getStorageSync('token')
  if (token) {
    isLogin.value = true
    
    // 策略修改：不再依赖本地缓存，防止显示旧数据
    // 每次 onShow 直接发起网络请求，确保数据绝对新鲜
    
    // 为了用户体验，如果内存中已有 userInfo，先暂时显示（避免白屏）
    // 但不从 Storage 读，防止 Storage 是旧的
    
    // 强制刷新
    updateUserInfo()
  } else {
    isLogin.value = false
    userInfo.value = {}
  }
}

const updateUserInfo = async () => {
  try {
     // 加时间戳，杜绝 GET 请求缓存
     const res = await request.get('/users/me?_t=' + Date.now())
     if (res.code === 200) {
        // 关键：先清空一下（可选），或者直接覆盖
        // 这里使用解构赋值产生新对象引用
        const newUser = { ...res.data }
        
        // 对比一下新旧数据，如果有变化再更新（或者直接更新）
        userInfo.value = newUser
        
        // 更新本地缓存（虽然我们读的时候不用它，但为了其他页面可能需要）
        uni.setStorageSync('userInfo', JSON.stringify(newUser))
     }
  } catch (e) {
     console.error('Refresh user info failed', e)
  }
}

const goToLogin = () => {
  uni.navigateTo({ url: '/pages/login/login' })
}

const navigateTo = (url) => {
  if (url.includes('orders')) {
    uni.switchTab({ url })
  } else {
    uni.navigateTo({ url })
  }
}

const navigateToEdit = () => {
  // 跳转到个人信息编辑页
  uni.navigateTo({ url: '/pages/profile-edit/profile-edit' })
}

const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        checkLogin()
      }
    }
  })
}

onPullDownRefresh(async () => {
  if (isLogin.value) {
    await updateUserInfo()
  } else {
    checkLogin()
  }
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss">
.profile-container {
  min-height: 100vh;
  background: #f8f8f8;
  padding: 30rpx;
}

.header-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  
  .user-info {
    display: flex;
    align-items: center;
    
    .avatar-img {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      margin-right: 30rpx;
    }
    
    .avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      background: #00796b;
      color: #fff;
      font-size: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 30rpx;
    }
    
    .text-info {
      display: flex;
      flex-direction: column;
      
      .username {
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 10rpx;
      }
      
      .uid {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .edit-btn {
      margin-left: auto;
      color: #333;
      font-size: 40rpx;
      padding: 10rpx;
    }
  }
}

.menu-list {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 40rpx;
  
  .menu-item {
    display: flex;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child { border-bottom: none; }
    
    .icon { margin-right: 20rpx; font-size: 32rpx; }
    .label { flex: 1; font-size: 30rpx; color: #333; }
    .arrow { color: #ccc; }
  }
}

.btn-logout {
  background: #fff;
  color: #e74c3c;
  font-size: 30rpx;
  border: none;
  border-radius: 20rpx;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  
  &::after { border: none; }
}
</style>
