<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2>个人主页</h2>
      <p>欢迎回来，{{ userInfo.username }}</p>
    </div>
    
    <div class="profile-content">
      <!-- 用户信息卡片 -->
      <div class="profile-card">
        <h3>基本信息</h3>
        <div class="info-item">
          <span class="label">用户名：</span>
          <span class="value">{{ userInfo.username }}</span>
        </div>
        <div class="info-item">
          <span class="label">邮箱：</span>
          <span class="value">{{ userInfo.email || '未绑定' }}</span>
        </div>
        <div class="info-item">
          <span class="label">手机号：</span>
          <span class="value">{{ userInfo.phone || '未绑定' }}</span>
        </div>
        <div class="info-item">
          <span class="label">注册时间：</span>
          <span class="value">{{ formatDate(userInfo.createdAt) }}</span>
        </div>
      </div>
      
      <!-- 快捷操作 -->
      <div class="profile-card">
        <h3>快捷操作</h3>
        <div class="action-buttons">
          <button class="btn btn-primary" @click="navigateTo('/orders')">
            查看我的订单
          </button>
          <button class="btn btn-outline" @click="navigateTo('/custom')">
            开始新定制
          </button>
          <button class="btn btn-outline" @click="logout">
            退出登录
          </button>
        </div>
      </div>
      
      <!-- 账户安全 -->
      <div class="profile-card">
        <h3>账户安全</h3>
        <div class="security-item">
          <span>密码修改</span>
          <button class="btn btn-sm btn-outline" @click="showPasswordModal = true">修改</button>
        </div>
        <div class="security-item">
          <span>绑定邮箱</span>
          <button class="btn btn-sm btn-outline" @click="showEmailModal = true">管理</button>
        </div>
        <div class="security-item">
          <span>绑定手机</span>
          <button class="btn btn-sm btn-outline" @click="showPhoneModal = true">管理</button>
        </div>
      </div>
    </div>
    
    <!-- 密码修改模态框 -->
    <div v-if="showPasswordModal" class="modal-overlay" @click="closePasswordModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>修改密码</h3>
          <button class="close-btn" @click="closePasswordModal">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="updatePassword">
            <div class="form-group">
              <label>当前密码</label>
              <input type="password" v-model="passwordForm.oldPassword" required placeholder="请输入当前密码" class="form-input">
            </div>
            <div class="form-group">
              <label>新密码</label>
              <input type="password" v-model="passwordForm.newPassword" required placeholder="请输入新密码" class="form-input">
              <p v-if="passwordError" class="error-message">{{ passwordError }}</p>
            </div>
            <div class="form-group">
              <label>确认新密码</label>
              <input type="password" v-model="passwordForm.confirmPassword" required placeholder="请确认新密码" class="form-input">
              <p v-if="confirmPasswordError" class="error-message">{{ confirmPasswordError }}</p>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-outline" @click="closePasswordModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="passwordLoading">
                {{ passwordLoading ? '提交中...' : '确认修改' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- 邮箱管理模态框 -->
    <div v-if="showEmailModal" class="modal-overlay" @click="closeEmailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>管理邮箱</h3>
          <button class="close-btn" @click="closeEmailModal">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="updateEmail">
            <div class="form-group">
              <label>当前邮箱</label>
              <input type="email" :value="userInfo.email || '未绑定'" disabled class="form-input">
            </div>
            <div class="form-group">
              <label>新邮箱</label>
              <input type="email" v-model="emailForm.newEmail" required placeholder="请输入新邮箱" class="form-input">
            </div>
            <div class="form-group">
              <label>验证码</label>
              <div class="code-input-group">
                <input type="text" v-model="emailForm.code" required placeholder="请输入验证码" class="form-input">
                <button type="button" class="btn btn-outline" :disabled="emailCountdown > 0" @click="sendEmailCode">
                  {{ emailCountdown > 0 ? `${emailCountdown}s后重发` : '发送验证码' }}
                </button>
              </div>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-outline" @click="closeEmailModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="emailLoading">
                {{ emailLoading ? '提交中...' : '确认修改' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- 手机管理模态框 -->
    <div v-if="showPhoneModal" class="modal-overlay" @click="closePhoneModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>管理手机</h3>
          <button class="close-btn" @click="closePhoneModal">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="updatePhone">
            <div class="form-group">
              <label>当前手机号</label>
              <input type="tel" :value="userInfo.phone || '未绑定'" disabled class="form-input">
            </div>
            <div class="form-group">
              <label>新手机号</label>
              <input type="tel" v-model="phoneForm.newPhone" required placeholder="请输入新手机号" class="form-input">
            </div>
            <div class="form-group">
              <label>验证码</label>
              <div class="code-input-group">
                <input type="text" v-model="phoneForm.code" required placeholder="请输入验证码" class="form-input">
                <button type="button" class="btn btn-outline" :disabled="phoneCountdown > 0" @click="sendPhoneCode">
                  {{ phoneCountdown > 0 ? `${phoneCountdown}s后重发` : '发送验证码' }}
                </button>
              </div>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-outline" @click="closePhoneModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="phoneLoading">
                {{ phoneLoading ? '提交中...' : '确认修改' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'

const router = useRouter()
const userInfo = ref({})

// 模态框状态
const showPasswordModal = ref(false)
const showEmailModal = ref(false)
const showPhoneModal = ref(false)

// 表单数据
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const emailForm = ref({
  newEmail: '',
  code: ''
})

const phoneForm = ref({
  newPhone: '',
  code: ''
})

// 错误提示
const passwordError = ref('')
const confirmPasswordError = ref('')

// 加载状态
const passwordLoading = ref(false)
const emailLoading = ref(false)
const phoneLoading = ref(false)

// 倒计时
const emailCountdown = ref(0)
const phoneCountdown = ref(0)

// 获取用户信息
const getUserInfo = () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 导航方法
const navigateTo = (path) => {
  router.push(path)
}

// 登出方法
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/')
}

// 关闭密码模态框
const closePasswordModal = () => {
  showPasswordModal.value = false
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordError.value = ''
  confirmPasswordError.value = ''
  passwordLoading.value = false
}

// 关闭邮箱模态框
const closeEmailModal = () => {
  showEmailModal.value = false
  emailForm.value = {
    newEmail: '',
    code: ''
  }
  emailLoading.value = false
  emailCountdown.value = 0
}

// 关闭手机模态框
const closePhoneModal = () => {
  showPhoneModal.value = false
  phoneForm.value = {
    newPhone: '',
    code: ''
  }
  phoneLoading.value = false
  phoneCountdown.value = 0
}

// 更新密码
const updatePassword = async () => {
  // 重置错误信息
  passwordError.value = ''
  confirmPasswordError.value = ''
  
  // 验证新密码长度
  if (passwordForm.value.newPassword.length < 3) {
    passwordError.value = '新密码长度至少为3位'
    return
  }
  
  // 验证密码一致性
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    confirmPasswordError.value = '两次输入的新密码不一致'
    return
  }

  passwordLoading.value = true
  
  try {
    await axios.post('/auth/update-password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    alert('密码修改成功')
    closePasswordModal()
  } catch (error) {
    console.error('密码修改失败:', error)
    if (error.response && error.response.data) {
      alert(`密码修改失败: ${error.response.data.message || '请检查当前密码是否正确'}`)
    } else {
      alert('密码修改失败，请检查当前密码是否正确')
    }
  } finally {
    passwordLoading.value = false
  }
}

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!emailForm.value.newEmail) {
    alert('请输入新邮箱')
    return
  }

  try {
    await axios.post('/auth/send-email-code', {
      email: emailForm.value.newEmail
    })
    
    alert('验证码已发送')
    startEmailCountdown()
  } catch (error) {
    console.error('发送验证码失败:', error)
    alert('发送验证码失败，请稍后重试')
  }
}

// 开始邮箱倒计时
const startEmailCountdown = () => {
  emailCountdown.value = 60
  const timer = setInterval(() => {
    emailCountdown.value--
    if (emailCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 更新邮箱
const updateEmail = async () => {
  emailLoading.value = true
  
  try {
    await axios.post('/auth/update-email', {
      newEmail: emailForm.value.newEmail,
      code: emailForm.value.code
    })
    
    alert('邮箱修改成功')
    // 更新本地用户信息
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      const userData = JSON.parse(storedUserInfo)
      userData.email = emailForm.value.newEmail
      localStorage.setItem('userInfo', JSON.stringify(userData))
      userInfo.value = userData
    }
    closeEmailModal()
  } catch (error) {
    console.error('邮箱修改失败:', error)
    alert('邮箱修改失败，请检查验证码是否正确')
  } finally {
    emailLoading.value = false
  }
}

// 发送手机验证码
const sendPhoneCode = async () => {
  if (!phoneForm.value.newPhone) {
    alert('请输入新手机号')
    return
  }

  try {
    await axios.post('/auth/send-sms', {
      phone: phoneForm.value.newPhone
    })
    
    alert('验证码已发送')
    startPhoneCountdown()
  } catch (error) {
    console.error('发送验证码失败:', error)
    alert('发送验证码失败，请稍后重试')
  }
}

// 开始手机倒计时
const startPhoneCountdown = () => {
  phoneCountdown.value = 60
  const timer = setInterval(() => {
    phoneCountdown.value--
    if (phoneCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 更新手机
const updatePhone = async () => {
  phoneLoading.value = true
  
  try {
    await axios.post('/auth/update-phone', {
      newPhone: phoneForm.value.newPhone,
      code: phoneForm.value.code
    })
    
    alert('手机号修改成功')
    // 更新本地用户信息
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      const userData = JSON.parse(storedUserInfo)
      userData.phone = phoneForm.value.newPhone
      localStorage.setItem('userInfo', JSON.stringify(userData))
      userInfo.value = userData
    }
    closePhoneModal()
  } catch (error) {
    console.error('手机号修改失败:', error)
    alert('手机号修改失败，请检查验证码是否正确')
  } finally {
    phoneLoading.value = false
  }
}

// 初始化
onMounted(() => {
  getUserInfo()
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: 100vh;
  background: var(--c-paper);
}

.profile-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 30px;
  background: var(--glass);
  backdrop-filter: blur(20px);
  border: var(--glass-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow);
}

.profile-header h2 {
  font-size: 2.5rem;
  color: var(--c-ink);
  margin-bottom: 10px;
  font-family: 'Noto Serif SC', serif;
}

.profile-header p {
  font-size: 1.2rem;
  color: var(--c-gray);
}

.profile-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.profile-card {
  background: var(--glass);
  backdrop-filter: blur(20px);
  border: var(--glass-border);
  border-radius: var(--radius-lg);
  padding: 30px;
  box-shadow: var(--shadow);
  transition: all 0.3s var(--ease);
}

.profile-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.profile-card h3 {
  font-size: 1.4rem;
  color: var(--c-ink);
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--c-primary);
  font-family: 'Noto Serif SC', serif;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(var(--c-ink-rgb), 0.15);
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-weight: 600;
  color: var(--c-gray-700);
}

.value {
  color: var(--c-ink);
  font-family: 'Noto Serif SC', serif;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(var(--c-ink-rgb), 0.15);
}

.security-item:last-child {
  border-bottom: none;
}

.btn-sm {
  padding: 6px 16px;
  font-size: 14px;
  border-radius: var(--radius-md);
  transition: all 0.3s var(--ease);
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-content {
  background: var(--glass);
  backdrop-filter: blur(20px);
  border: var(--glass-border);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 500px;
  box-shadow: var(--shadow-lg);
  transition: all 0.3s var(--ease);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid rgba(var(--c-ink-rgb), 0.15);
}

.modal-header h3 {
  font-size: 1.3rem;
  color: var(--c-ink);
  margin: 0;
  font-family: 'Noto Serif SC', serif;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--c-gray);
  cursor: pointer;
  padding: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s var(--ease);
}

.close-btn:hover {
  background: rgba(var(--c-ink-rgb), 0.1);
  color: var(--c-ink);
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--c-gray-700);
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(var(--c-ink-rgb), 0.2);
  border-radius: var(--radius-md);
  font-size: 1rem;
  transition: all 0.3s var(--ease);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.form-input:focus {
  outline: none;
  border-color: var(--c-primary);
  box-shadow: 0 0 0 2px rgba(var(--c-primary-rgb), 0.2);
  background: rgba(255, 255, 255, 0.95);
}

.form-input:disabled {
  background: rgba(var(--c-ink-rgb), 0.05);
  color: var(--c-gray);
  cursor: not-allowed;
}

.error-message {
  color: var(--c-error);
  font-size: 14px;
  margin-top: 6px;
  margin-bottom: 0;
}

.code-input-group {
  display: flex;
  gap: 10px;
}

.code-input-group input {
  flex: 1;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
  
  .profile-container {
    padding: 20px 15px;
  }
  
  .profile-header h2 {
    font-size: 2rem;
  }
  
  .modal-content {
    margin: 0 20px;
    max-width: none;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions button {
    width: 100%;
  }
  
  .code-input-group {
    flex-direction: column;
  }
}
</style>