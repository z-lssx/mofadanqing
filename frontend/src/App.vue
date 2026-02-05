<template>
  <div id="app">
    <!-- 导航栏 -->
    <header>
      <div class="header-inner">
        <div class="logo">
          <div class="logo-icon">绣</div>
          <span>墨发丹青</span>
        </div>
        <nav class="nav-links">
          <span 
            class="nav-link" 
            :class="{ active: currentRoute === '/' }"
            @click="navigateTo('/')"
          >首页</span>
          <!-- C2M Custom link removed from global nav -->

          <!-- 购物车 -->
          <span 
            class="nav-link" 
            :class="{ active: currentRoute === '/cart' }"
            @click="navigateTo('/cart')"
          >
            购物车
            <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
          </span>

          <!-- 登录后显示我的订单 -->
          <span 
            v-if="isLoggedIn"
            class="nav-link"
            :class="{ active: currentRoute === '/orders' }"
            @click="navigateTo('/orders')"
          >我的订单</span>
          <!-- 登录后显示个人主页 -->
          <span 
            v-if="isLoggedIn"
            class="nav-link"
            :class="{ active: currentRoute === '/profile' }"
            @click="navigateTo('/profile')"
          >个人主页</span>
        </nav>
        <div style="display: flex; gap: 15px; align-items: center;">
          <!-- 未登录状态 -->
          <template v-if="!isLoggedIn">
            <button 
              class="btn btn-outline" 
              style="padding: 8px 20px;"
              @click="navigateTo('/login')"
            >登录</button>
          </template>
          <!-- 登录状态 -->
          <template v-else>
            <div class="user-dropdown" ref="dropdownRef">
              <div class="username-trigger" @click="toggleDropdown">
                {{ userInfo.username }}
                <span v-if="userInfo.role === 'ADMIN'" class="role-tag">管理员</span>
                <!-- 用户名旁的红点：仅当有未读消息且菜单关闭时显示 -->
                <span v-if="unreadCount > 0 && !isDropdownOpen" class="red-dot"></span>
              </div>
              <div class="dropdown-menu" v-show="isDropdownOpen">
                <div class="menu-item" @click="navigateTo('/messages')">
                  消息中心
                  <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
                </div>
                <div 
                  v-if="userInfo.role === 'ADMIN'"
                  class="menu-item" 
                  @click="navigateTo('/admin/console')"
                >管理后台</div>
                <div class="menu-item logout" @click="logout">登出</div>
              </div>
            </div>
          </template>
          <!-- Global Custom Button removed -->
        </div>
      </div>
    </header>

    <!-- 页面内容 -->
    <main>
      <router-view />
    </main>

    <!-- Loading -->
    <div class="loader" id="ai-loader">
      <div class="needle-spin"></div>
      <h3 style="margin-top: 20px; color: var(--c-ink);">AI 正在织梦...</h3>
      <p style="color: var(--c-gray);">正在融合鲁绣针法库</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { cartState } from './utils/cartState'
import { messageState } from './utils/messageState'

const route = useRoute()
const router = useRouter()
const currentRoute = ref('/')
const isLoggedIn = ref(false)
const userInfo = ref({})
const isDropdownOpen = ref(false)
const dropdownRef = ref(null)

const cartCount = computed(() => cartState.count)
const unreadCount = computed(() => messageState.count)

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

// 点击外部关闭下拉菜单
const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isDropdownOpen.value = false
  }
}

// 计算当前路由
const updateCurrentRoute = () => {
  currentRoute.value = route.path
  isDropdownOpen.value = false // 路由跳转时关闭下拉菜单
}

// 导航方法
const navigateTo = (path) => {
  router.push(path)
}

// 检查登录状态
const checkLoginStatus = () => {
  const token = sessionStorage.getItem('token')
  const userData = sessionStorage.getItem('userInfo')
  
  if (token && userData) {
    isLoggedIn.value = true
    userInfo.value = JSON.parse(userData)
    // 登录后拉取购物车和消息
    cartState.fetchCart()
    messageState.fetchCount()
    startMessagePolling()
  } else {
    isLoggedIn.value = false
    userInfo.value = {}
    // 未登录重置
    cartState.reset()
    messageState.reset()
    stopMessagePolling()
  }
}

// 登出方法
const logout = () => {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('userInfo')
  isLoggedIn.value = false
  userInfo.value = {}
  cartState.reset()
  messageState.reset()
  isDropdownOpen.value = false
  router.push('/')
}

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    currentRoute.value = newPath
    updateCurrentRoute() // 确保关闭菜单
    checkLoginStatus()
  }
)

// 初始化
onMounted(() => {
  updateCurrentRoute()
  checkLoginStatus()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  stopMessagePolling()
})

let timer = null
const startMessagePolling = () => {
  if (timer) return
  const tick = () => messageState.fetchCount()
  tick()
  timer = setInterval(tick, 10000)
}
const stopMessagePolling = () => {
  if (timer) { clearInterval(timer); timer = null; }
}
</script>

<style>
@import './assets/global.css';

#app {
  min-height: 100vh;
  background-color: var(--c-paper);
}

main {
  min-height: calc(100vh - 80px);
  padding-top: 80px;
}

.cart-badge {
  background: #e74c3c;
  color: white;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 12px;
  margin-left: 4px;
  vertical-align: middle;
}

/* User Dropdown Styles */
.user-dropdown {
  position: relative;
  display: inline-block;
  cursor: pointer;
  padding: 10px;
}

.username-trigger {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: var(--c-ink);
}

.dropdown-menu {
  display: none;
  position: absolute;
  right: 0;
  top: 100%;
  background-color: white;
  min-width: 160px;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
  border-radius: 4px;
  z-index: 1000;
  padding: 8px 0;
  border: 1px solid #eee;
}

.user-dropdown:hover .dropdown-menu {
  display: block;
}

.menu-item {
  padding: 10px 16px;
  color: var(--c-ink);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.2s;
  font-size: 14px;
}

.menu-item:hover {
  background-color: #f5f5f5;
}

.menu-item.logout {
  color: #e74c3c;
  border-top: 1px solid #eee;
}

.red-dot {
  width: 8px;
  height: 8px;
  background-color: #e74c3c;
  border-radius: 50%;
  display: inline-block;
  margin-left: 6px;
}

.badge {
  background-color: #e74c3c;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
}

.role-tag {
  font-size: 12px;
  color: #e67e22;
  margin-left: 6px;
  background: rgba(230, 126, 34, 0.1);
  padding: 2px 4px;
  border-radius: 4px;
}
</style>
