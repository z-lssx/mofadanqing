<template>
  <div class="register">
    <div class="register-container">
      <h2>注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <div class="input-group">
            <label for="username">用户名</label>
            <input 
              type="text" 
              id="username" 
              v-model="registerForm.username"
              required
              placeholder="请输入用户名"
            />
          </div>
          <div class="input-group">
            <label for="password">密码</label>
            <input 
              type="password" 
              id="password"
              v-model="registerForm.password"
              required
              placeholder="请输入密码"
              minlength="6"
            />
          </div>
          <div class="input-group">
            <label for="phone">手机号</label>
            <input 
              type="tel" 
              id="phone"
              v-model="registerForm.phone"
              required
              placeholder="请输入手机号"
              pattern="1[3-9]\d{9}"
            />
          </div>
          <div class="input-group">
            <label for="email">邮箱</label>
            <input 
              type="email" 
              id="email"
              v-model="registerForm.email"
              required
              placeholder="请输入邮箱"
            />
          </div>
        </div>
        <button type="submit" class="btn btn-primary register-btn">注册</button>
        <div class="register-footer">
          <span>已有账号？</span>
          <a href="/login">立即登录</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'

const router = useRouter()
const registerForm = reactive({
  username: '',
  password: '',
  phone: '',
  email: ''
})

// 处理注册
const handleRegister = async () => {
  try {
    const response = await axios.post('/auth/register', registerForm)
    
    // 保存 token 到 localStorage
    localStorage.setItem('token', response.token)
    localStorage.setItem('user', JSON.stringify(response.user))
    
    // 跳转到首页
    router.push('/')
  } catch (error) {
    console.error('注册失败:', error)
    if (error.response && error.response.data && error.response.data.message) {
      alert('注册失败: ' + error.response.data.message)
    } else if (error.message) {
      alert('注册失败: ' + error.message)
    } else {
      alert('注册失败，请检查输入信息')
    }
  }
}
</script>

<style scoped>
.register {
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

.register-container {
  background: var(--glass);
  backdrop-filter: blur(20px);
  border: var(--glass-border);
  border-radius: 12px;
  box-shadow: var(--shadow-hover);
  padding: 2.5rem;
  width: 100%;
  max-width: 400px;
}

.register-container h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--c-ink);
  font-size: 1.8rem;
  font-family: 'Noto Serif SC', serif;
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

.register-btn {
  width: 100%;
  margin-bottom: 1.5rem;
}

.register-footer {
  text-align: center;
  font-size: 0.9rem;
  color: var(--c-gray);
}

.register-footer a {
  color: var(--c-primary);
  text-decoration: none;
  margin-left: 0.5rem;
  transition: color 0.3s var(--ease);
}

.register-footer a:hover {
  color: var(--c-primary-dark);
}

@media (max-width: 480px) {
  .register-container {
    padding: 1.5rem;
    margin: 0 1rem;
  }
  
  .register-container h2 {
    font-size: 1.5rem;
  }
}
</style>