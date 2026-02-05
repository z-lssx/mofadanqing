<template>
  <div class="login">
    <div class="login-container">
      <h2>登录</h2>
      <div class="login-tabs">
        <button 
          :class="['tab-btn', { active: loginType === 'password' }]"
          @click="loginType = 'password'"
        >
          账号密码登录
        </button>
        <button 
          :class="['tab-btn', { active: loginType === 'sms' }]"
          @click="loginType = 'sms'"
        >
          手机号验证码登录
        </button>
      </div>

      <form @submit.prevent="handleLogin">
        <!-- 账号密码登录 -->
        <div v-if="loginType === 'password'" class="form-group">
          <div class="input-group">
            <label for="username">用户名</label>
            <input 
              type="text" 
              id="username" 
              v-model="loginForm.username"
              required
              placeholder="请输入用户名"
            />
          </div>
          <div class="input-group">
            <label for="password">密码</label>
            <input 
              type="password" 
              id="password" 
              v-model="loginForm.password"
              required
              placeholder="请输入密码"
            />
          </div>
        </div>

        <!-- 手机号验证码登录 -->
        <div v-else class="form-group">
          <div class="input-group">
            <label for="phone">手机号</label>
            <input 
              type="tel" 
              id="phone" 
              v-model="loginForm.phone"
              required
              placeholder="请输入手机号"
              pattern="1[3-9]\d{9}"
            />
          </div>
          <div class="input-group sms-group">
            <label for="smsCode">验证码</label>
            <div class="sms-input">
              <input 
                type="text" 
                id="smsCode" 
                v-model="loginForm.smsCode"
                required
                placeholder="请输入验证码"
              />
              <button 
                type="button" 
                class="sms-btn"
                :disabled="countdown > 0"
                @click="sendSmsCode"
              >
                {{ countdown > 0 ? `${countdown}s后重发` : '发送验证码' }}
              </button>
            </div>
          </div>
        </div>

        <button type="submit" class="btn btn-primary login-btn">登录</button>
        <div class="login-footer">
          <a href="#">忘记密码？</a>
          <span>·</span>
          <a href="/register">立即注册</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'

const router = useRouter()
const loginType = ref('password')
const countdown = ref(0)
const loginForm = reactive({
  username: '',
  password: '',
  phone: '',
  smsCode: ''
})

// 发送验证码
const sendSmsCode = async () => {
  if (!loginForm.phone) {
    alert('请输入手机号')
    return
  }
  
  try {
    // 调用后端发送验证码的接口
    await axios.post('/auth/send-sms', { phone: loginForm.phone })
    console.log('发送验证码到:', loginForm.phone)
    
    // 模拟倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    
    alert('验证码已发送')
  } catch (error) {
    console.error('发送验证码失败:', error)
    alert('发送验证码失败，请稍后重试')
  }
}

// 处理登录
const handleLogin = async () => {
  try {
    let response
    if (loginType.value === 'password') {
      // 账号密码登录
      response = await axios.post('/auth/login', {
        username: loginForm.username,
        password: loginForm.password
      })
    } else {
      // 手机号验证码登录
      response = await axios.post('/auth/login/sms', {
        phone: loginForm.phone,
        smsCode: loginForm.smsCode
      })
    }
    
    // 保存 token 到 sessionStorage
    sessionStorage.setItem('token', response.token)
    sessionStorage.setItem('userInfo', JSON.stringify(response.user))
    
    // 跳转到首页
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    if (error.message) {
      alert('登录失败: ' + error.message)
    } else if (error.response && error.response.data && error.response.data.message) {
      alert('登录失败: ' + error.response.data.message)
    } else {
      alert('登录失败，请检查账号密码或验证码')
    }
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--c-paper);
  background-image: url('../assets/网页背景图.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.login-container {
  background: var(--glass);
  backdrop-filter: blur(20px);
  border: var(--glass-border);
  border-radius: 12px;
  box-shadow: var(--shadow-hover);
  padding: 2.5rem;
  width: 100%;
  max-width: 400px;
}

.login-container h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--c-ink);
  font-size: 1.8rem;
  font-family: 'Noto Serif SC', serif;
}

.login-tabs {
  display: flex;
  margin-bottom: 2rem;
  border-bottom: 1px solid rgba(var(--c-ink-rgb), 0.15);
}

.tab-btn {
  flex: 1;
  padding: 0.8rem;
  background: none;
  border: none;
  font-size: 1rem;
  color: var(--c-gray);
  cursor: pointer;
  transition: all 0.3s var(--ease);
}

.tab-btn.active {
  color: var(--c-primary);
  border-bottom: 2px solid var(--c-primary);
  font-weight: 600;
}

.form-group {
  margin-bottom: 1.5rem;
}

.input-group {
  margin-bottom: 1rem;
}

.input-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: var(--c-gray);
}

.input-group input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid rgba(var(--c-ink-rgb), 0.15);
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s var(--ease);
  background: rgba(var(--c-paper-rgb), 0.8);
}

.input-group input:focus {
  outline: none;
  border-color: var(--c-primary);
  box-shadow: 0 0 0 2px rgba(var(--c-primary-rgb), 0.2);
}

.sms-group {
  margin-bottom: 1.5rem;
}

.sms-input {
  display: flex;
  gap: 1rem;
}

.sms-input input {
  flex: 1;
}

.sms-btn {
  padding: 0 1.5rem;
  background: var(--c-accent-warm);
  border: 1px solid var(--c-accent-warm);
  border-radius: 4px;
  font-size: 0.9rem;
  color: var(--c-ink);
  cursor: pointer;
  transition: all 0.3s var(--ease);
}

.sms-btn:hover:not(:disabled) {
  background: #c9bb9f;
  transform: translateY(-1px);
}

.sms-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.login-btn {
  width: 100%;
  margin-bottom: 1.5rem;
}

.login-footer {
  text-align: center;
  font-size: 0.9rem;
}

.login-footer a {
  color: var(--c-gray);
  text-decoration: none;
  transition: color 0.3s var(--ease);
}

.login-footer a:hover {
  color: var(--c-primary);
}

.login-footer span {
  margin: 0 0.5rem;
  color: var(--c-gray);
}

@media (max-width: 480px) {
  .login-container {
    padding: 1.5rem;
    margin: 0 1rem;
  }
  
  .login-container h2 {
    font-size: 1.5rem;
  }
}
</style>
