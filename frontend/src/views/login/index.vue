<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- Logo -->
      <div class="login-header">
        <div class="logo">
          <div class="logo-icon">
            <el-icon :size="32"><DataAnalysis /></el-icon>
          </div>
        </div>
        <h1 class="login-title">企业报表系统</h1>
        <p class="login-subtitle">Enterprise Report System</p>
      </div>
      
      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
            class="login-input"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            class="login-input"
          />
        </el-form-item>
        
        <div class="login-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码？</el-link>
        </div>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 其他登录方式 -->
      <div class="other-login">
        <div class="divider">
          <span>其他登录方式</span>
        </div>
        <div class="social-login">
          <el-button circle class="social-btn">
            <el-icon><ChatDotRound /></el-icon>
          </el-button>
          <el-button circle class="social-btn">
            <el-icon><Message /></el-icon>
          </el-button>
          <el-button circle class="social-btn">
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
      
      <!-- 底部信息 -->
      <div class="login-footer">
        <p>© 2024 Enterprise Report System. All rights reserved.</p>
      </div>
    </div>
    
    <!-- 右侧装饰 -->
    <div class="login-decoration">
      <div class="decoration-content">
        <div class="decoration-icon">
          <el-icon :size="120"><TrendCharts /></el-icon>
        </div>
        <h2 class="decoration-title">数据驱动决策</h2>
        <p class="decoration-desc">
          一站式企业报表解决方案<br>
          在线设计 · 参数化生成 · 权限控制 · 定时任务
        </p>
        <div class="decoration-features">
          <div class="feature-item">
            <el-icon><Document /></el-icon>
            <span>模板管理</span>
          </div>
          <div class="feature-item">
            <el-icon><DataLine /></el-icon>
            <span>数据分析</span>
          </div>
          <div class="feature-item">
            <el-icon><Timer /></el-icon>
            <span>定时生成</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        ElMessage.success('登录成功')
        const redirect = route.query.redirect || '/'
        router.push(redirect)
      } catch (error) {
        console.error('Login error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

// 背景装饰
.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  animation: float 20s ease-in-out infinite;
  
  &-1 {
    width: 600px;
    height: 600px;
    top: -200px;
    left: -200px;
  }
  
  &-2 {
    width: 400px;
    height: 400px;
    bottom: -100px;
    right: 20%;
    animation-delay: -5s;
  }
  
  &-3 {
    width: 300px;
    height: 300px;
    top: 30%;
    right: -100px;
    animation-delay: -10s;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-30px) rotate(5deg);
  }
}

// 登录卡片
.login-card {
  width: 480px;
  min-height: 100%;
  background: $bg-primary;
  padding: 60px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 10;
  box-shadow: 20px 0 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.logo-icon {
  width: 72px;
  height: 72px;
  border-radius: $radius-xl;
  background: linear-gradient(135deg, $primary-color, #5856D6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 8px 24px rgba($primary-color, 0.3);
}

.login-title {
  font-size: 28px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.login-subtitle {
  font-size: $font-size-md;
  color: $text-secondary;
  letter-spacing: 1px;
}

// 登录表单
.login-form {
  margin-bottom: 24px;
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-input__wrapper) {
    padding: 8px 16px;
    border-radius: $radius-lg;
    box-shadow: 0 0 0 1px $border-color inset;
    transition: all $transition-normal;
    
    &:hover {
      box-shadow: 0 0 0 1px $gray-300 inset;
    }
    
    &.is-focus {
      box-shadow: 0 0 0 2px rgba($primary-color, 0.2), 0 0 0 1px $primary-color inset !important;
    }
  }
  
  :deep(.el-input__inner) {
    height: 24px;
    font-size: 15px;
  }
  
  :deep(.el-input__prefix-inner) {
    font-size: 18px;
    color: $text-secondary;
  }
}

.login-input {
  height: 52px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  
  :deep(.el-checkbox__label) {
    color: $text-secondary;
  }
  
  :deep(.el-link) {
    font-size: $font-size-md;
  }
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: $font-weight-semibold;
  border-radius: $radius-lg !important;
  background: linear-gradient(135deg, $primary-color, #5856D6) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba($primary-color, 0.3);
  transition: all $transition-normal;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba($primary-color, 0.4);
  }
  
  &:active {
    transform: translateY(0);
  }
}

// 其他登录方式
.other-login {
  margin-top: 24px;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  
  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: $border-color;
  }
  
  span {
    padding: 0 16px;
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.social-btn {
  width: 48px;
  height: 48px;
  border: 1px solid $border-color !important;
  background: transparent !important;
  
  &:hover {
    border-color: $primary-color !important;
    color: $primary-color;
    background: rgba($primary-color, 0.04) !important;
  }
  
  .el-icon {
    font-size: 20px;
  }
}

// 底部信息
.login-footer {
  margin-top: auto;
  padding-top: 24px;
  text-align: center;
  
  p {
    font-size: $font-size-sm;
    color: $text-tertiary;
  }
}

// 右侧装饰
.login-decoration {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  position: relative;
}

.decoration-content {
  text-align: center;
  color: #fff;
  max-width: 480px;
}

.decoration-icon {
  margin-bottom: 32px;
  opacity: 0.9;
  
  .el-icon {
    filter: drop-shadow(0 8px 24px rgba(0, 0, 0, 0.2));
  }
}

.decoration-title {
  font-size: 36px;
  font-weight: $font-weight-bold;
  margin-bottom: 16px;
  letter-spacing: -1px;
}

.decoration-desc {
  font-size: 18px;
  line-height: 1.8;
  opacity: 0.9;
  margin-bottom: 48px;
}

.decoration-features {
  display: flex;
  justify-content: center;
  gap: 32px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: $radius-xl;
  backdrop-filter: blur(10px);
  
  .el-icon {
    font-size: 32px;
  }
  
  span {
    font-size: 14px;
    font-weight: $font-weight-medium;
  }
}

// 响应式
@media (max-width: 1024px) {
  .login-decoration {
    display: none;
  }
  
  .login-card {
    width: 100%;
    max-width: 480px;
    margin: auto;
    border-radius: $radius-2xl;
    min-height: auto;
    box-shadow: $shadow-xl;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 40px 24px;
    margin: 16px;
    border-radius: $radius-xl;
  }
  
  .login-title {
    font-size: 24px;
  }
}
</style>
