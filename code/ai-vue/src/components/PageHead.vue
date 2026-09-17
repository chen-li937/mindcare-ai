<template>
  <div class="page-head">
    <div class="head-text">
      <p class="page-title">{{ title }}</p>
      <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
    </div>
    <div class="action-btn">
      <slot name="buttons"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  subtitle: {
    type: String,
    default: '',
  },
})
</script>

<style lang="scss" scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: bold;
    color: #2d3748;
  }

  .page-subtitle {
    margin: 4px 0 0;
    font-size: 13px;
    color: #909399;
  }

  .action-btn {
    display: flex;
    align-items: center;
    gap: 10px;
  }
}
</style>
