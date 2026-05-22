<template>
  <view class="edit-container">
    <view class="avatar-wrapper">
      <button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
        <image v-if="avatarUrl" :src="avatarUrl" class="avatar-img" mode="aspectFill"></image>
        <view v-else class="avatar-placeholder">点击设置头像</view>
      </button>
    </view>
    
    <view class="form-group">
      <view class="label">昵称</view>
      <input type="nickname" class="input" v-model="nickname" placeholder="请输入昵称" @blur="onNicknameBlur" />
    </view>
    
    <button class="btn-save" @click="saveProfile" :loading="loading">保存</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import request from '../../utils/request'

const avatarUrl = ref('')
const nickname = ref('')
const loading = ref(false)

// 初始化数据
const init = () => {
  const user = uni.getStorageSync('userInfo')
  if (user) {
    const userInfo = JSON.parse(user)
    // 优先使用 nickname，没有则使用 username
    nickname.value = userInfo.nickname || userInfo.username || ''
    avatarUrl.value = userInfo.avatar || ''
  }
}
init()

const onChooseAvatar = (e) => {
  const { avatarUrl: tempUrl } = e.detail
  avatarUrl.value = tempUrl
  
  // 延迟上传，解决 Windows 开发工具文件写入延迟问题
  setTimeout(() => {
    uploadAvatar(tempUrl)
  }, 500)
}

const onNicknameBlur = (e) => {
  if (e.detail.value) {
    nickname.value = e.detail.value
  }
}

const uploadAvatar = (filePath, retryCount = 0) => {
  uni.showLoading({ title: '上传中...' })
  
  request.upload('/upload/image', filePath)
    .then(data => {
      if (data && data.url) {
        avatarUrl.value = data.url
      } else {
        uni.showToast({ title: '上传失败: ' + (data.message || '未知错误'), icon: 'none' })
      }
    })
    .catch(err => {
      // 针对 Windows 开发工具的 ENOENT 错误进行重试
      const errMsg = JSON.stringify(err)
      if (errMsg.includes('ENOENT') && retryCount < 3) {
        console.warn(`File not found, retrying... (${retryCount + 1}/3)`)
        uni.hideLoading()
        setTimeout(() => {
          uploadAvatar(filePath, retryCount + 1)
        }, 1000)
        return
      }
      
      console.error('Upload failed', err)
      uni.showToast({ title: '上传请求失败', icon: 'none' })
    })
    .finally(() => {
      // 只有在没有触发重试的情况下才隐藏 loading
      // 如果触发了重试，上面的 catch 块已经处理了隐藏（并在重试前重新显示会由下一次调用处理）
      // 这里为了简化，我们统一在非重试路径隐藏
      // 但由于 finally 无法判断是否重试，我们可以依赖 catch 中的逻辑
      // 实际上 request.upload 的 Promise 链在 retry 时已经断开
      // 简单处理：如果 err 触发了重试，我们不需要在这里 hideLoading，因为 catch 里已经 hide 了
      // 但 Promise 的 finally 无论如何都会执行。
      // 所以最好的方式是：如果重试，不要 resolve/reject 到这里，或者在这里判断。
      // 简化版：每次都 hide，重试时 show。
      uni.hideLoading()
    })
}

const saveProfile = async () => {
  if (!nickname.value) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  
  loading.value = true
  try {
    const res = await request.post('/users/update', {
      nickname: nickname.value,
      avatar: avatarUrl.value
    })
    
    if (res.code === 200) {
      uni.showToast({ title: '保存成功', icon: 'success' })
      // 更新本地存储
      const user = uni.getStorageSync('userInfo')
      if (user) {
        const userInfo = JSON.parse(user)
        userInfo.nickname = nickname.value
        userInfo.avatar = avatarUrl.value
        uni.setStorageSync('userInfo', JSON.stringify(userInfo))
      }
      
      uni.setStorageSync('needRefresh', true)
      
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
       uni.showToast({ title: '保存失败: ' + res.message, icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss">
.edit-container {
  padding: 40rpx;
}

.avatar-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 60rpx;
  
  .avatar-btn {
    padding: 0;
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    overflow: hidden;
    background: #f0f0f0;
    border: none;
    
    &::after { border: none; }
  }
  
  .avatar-img {
    width: 100%;
    height: 100%;
  }
  
  .avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 24rpx;
  }
}

.form-group {
  margin-bottom: 40rpx;
  border-bottom: 1rpx solid #eee;
  padding-bottom: 20rpx;
  
  .label {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 20rpx;
  }
  
  .input {
    font-size: 32rpx;
    color: #333;
    height: 80rpx;
  }
}

.btn-save {
  background: #00796b;
  color: #fff;
  border-radius: 40rpx;
  margin-top: 60rpx;
}
</style>
