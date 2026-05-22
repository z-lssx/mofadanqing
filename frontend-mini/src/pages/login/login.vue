<template>
  <view class="login-container">
    <view class="logo-box">
      <view class="logo">墨</view>
      <text class="app-name">墨发丹青</text>
      <text class="slogan">非遗 · 鲁绣 · 数字化定制</text>
    </view>

    <view class="btn-group">
      <!-- 微信一键登录 -->
      <button 
        class="btn-wechat" 
        @click="handleWechatLogin" 
        :loading="loading"
      >
        <text class="icon">🟢</text> 微信一键登录
      </button>
      
      <!-- 暂不登录 -->
      <view class="link-guest" @click="goHome">暂不登录，随便逛逛</view>
    </view>
    
    <view class="footer">
      <text>登录即代表同意《用户协议》与《隐私政策》</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import request from '../../utils/request'

const loading = ref(false)

const handleWechatLogin = () => {
  loading.value = true
  
  // 1. 获取微信 Code
  uni.login({
    provider: 'weixin',
    success: async (loginRes) => {
      if (loginRes.code) {
        try {
          // 2. 调用后端登录接口
          const res = await request.post('/auth/wechat/login', {
            code: loginRes.code
          })
          
          if (res.code === 200 || res.token) {
             const token = res.data?.token || res.token
             const user = res.data?.user || res.user
             
             // 3. 存储 Token
             uni.setStorageSync('token', token)
             uni.setStorageSync('userInfo', JSON.stringify(user))
             
             uni.showToast({ title: '登录成功', icon: 'success' })
             
             // 4. 返回上一页或首页
             setTimeout(() => {
                const pages = getCurrentPages()
                if (pages.length > 1) {
                  uni.navigateBack()
                } else {
                  uni.switchTab({ url: '/pages/index/index' })
                }
             }, 1500)
          } else {
             uni.showToast({ title: '登录失败', icon: 'none' })
          }
        } catch (e) {
          console.error(e)
          uni.showToast({ title: '服务异常', icon: 'none' })
        } finally {
          loading.value = false
        }
      } else {
        loading.value = false
        uni.showToast({ title: '获取Code失败', icon: 'none' })
      }
    },
    fail: (err) => {
      loading.value = false
      uni.showToast({ title: '微信登录调用失败', icon: 'none' })
    }
  })
}

const goHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}
</script>

<style lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 40rpx;
}

.logo-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 100rpx;
  
  .logo {
    width: 160rpx;
    height: 160rpx;
    background: #004d40;
    color: #fff;
    font-size: 80rpx;
    font-family: 'Courier New', Courier, monospace;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 20rpx;
    margin-bottom: 30rpx;
    box-shadow: 0 8px 20px rgba(0,77,64,0.3);
  }
  
  .app-name {
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 10rpx;
  }
  
  .slogan {
    font-size: 24rpx;
    color: #999;
    letter-spacing: 2px;
  }
}

.btn-group {
  width: 100%;
  
  .btn-wechat {
    background: #07c160;
    color: #fff;
    border-radius: 50rpx;
    font-size: 32rpx;
    height: 90rpx;
    line-height: 90rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 30rpx;
    
    .icon { margin-right: 10rpx; font-size: 36rpx; }
    
    &::after { border: none; }
  }
  
  .link-guest {
    text-align: center;
    color: #666;
    font-size: 28rpx;
    padding: 20rpx;
  }
}

.footer {
  position: absolute;
  bottom: 60rpx;
  font-size: 22rpx;
  color: #ccc;
}
</style>
