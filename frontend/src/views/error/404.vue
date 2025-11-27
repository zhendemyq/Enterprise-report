<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-illustration">
        <div class="number">4</div>
        <div class="circle">
          <el-icon class="face-icon"><Warning /></el-icon>
        </div>
        <div class="number">4</div>
      </div>
      
      <h1 class="error-title">页面不存在</h1>
      <p class="error-desc">
        抱歉，您访问的页面已不存在或已被移除
      </p>
      
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          <el-icon><Back /></el-icon>
          返回上页
        </el-button>
      </div>
      
      <div class="quick-links">
        <span class="link-label">快速访问：</span>
        <router-link to="/dashboard">工作台</router-link>
        <router-link to="/report/template">模板管理</router-link>
        <router-link to="/report/generate">报表生成</router-link>
      </div>
    </div>
    
    <!-- 装饰元素 -->
    <div class="decoration">
      <div class="circle-1"></div>
      <div class="circle-2"></div>
      <div class="circle-3"></div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled, Back, Warning } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.error-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
  position: relative;
  overflow: hidden;
}

.error-content {
  text-align: center;
  z-index: 1;
}

.error-illustration {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 32px;
}

.number {
  font-size: 120px;
  font-weight: 800;
  color: $primary-color;
  line-height: 1;
  text-shadow: 4px 4px 0 rgba($primary-color, 0.1);
}

.circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary-color 0%, #5856D6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba($primary-color, 0.3);
  animation: float 3s ease-in-out infinite;
}

.face-icon {
  font-size: 48px;
  color: #fff;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.error-title {
  font-size: 32px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 12px;
}

.error-desc {
  font-size: 16px;
  color: $text-secondary;
  margin-bottom: 32px;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 40px;
  
  .el-button {
    min-width: 140px;
  }
}

.quick-links {
  font-size: 14px;
  
  .link-label {
    color: $text-tertiary;
    margin-right: 8px;
  }
  
  a {
    color: $primary-color;
    text-decoration: none;
    margin: 0 8px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.circle-1,
.circle-2,
.circle-3 {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, rgba($primary-color, 0.1) 0%, transparent 100%);
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, rgba(#5856D6, 0.1) 0%, transparent 100%);
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, rgba($success-color, 0.1) 0%, transparent 100%);
  top: 50%;
  left: 10%;
}
</style>
