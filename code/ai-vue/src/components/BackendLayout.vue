<template>
  <div class="backend-layout">
    <el-container class="main-container">
      <SideBar :collapse="isCollapse" />
      <el-container>
        <el-header>
          <NavBar :collapsed="isCollapse" @toggle="isCollapse = !isCollapse" />
        </el-header>
        <el-main>
          <router-view class="content-container"></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import SideBar from './SideBar.vue'
import NavBar from './NavBar.vue'

const isCollapse = ref(false)
</script>

<style lang="scss" scoped>
.backend-layout {
  height: 100vh;

  .main-container {
    height: 100%;
  }

  :deep(.el-header) {
    padding: 0;
    height: 64px;
  }

  :deep(.el-main) {
    padding: 20px;
    background-color: #f5f7fa;

    .content-container {
      display: block;
    }
  }
}
</style>
