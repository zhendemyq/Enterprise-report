<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 初始化时检查登录状态
onMounted(() => {
  userStore.initUser()
})
</script>

<style lang="scss">
#app {
  width: 100%;
  height: 100%;
}
</style>
