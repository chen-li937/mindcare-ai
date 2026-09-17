<template>
  <div class="dashboard" v-loading="loading">
    <PageHead title="数据分析" subtitle="平台运营数据与用户情绪概览" />

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ backgroundColor: card.bg, color: card.color }">
              <el-icon :size="26"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-title">{{ card.title }}</p>
              <p class="stat-value">{{ card.value }}</p>
              <p v-if="card.trend !== 0" class="stat-trend" :class="card.trend > 0 ? 'up' : 'down'">
                <el-icon><CaretTop v-if="card.trend > 0" /><CaretBottom v-else /></el-icon>
                {{ Math.abs(card.trend) }}% 较昨日
              </p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20">
      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近7日活跃趋势</span>
              <el-tag type="success" size="small">AI对话 / 情绪日记</el-tag>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户情绪分布</span>
              <el-tag type="info" size="small">本周</el-tag>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近咨询 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>最近咨询记录</span>
          <el-button type="primary" link @click="goConsultation">
            查看全部<el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
      <el-table :data="recentList" stripe style="width: 100%">
        <el-table-column prop="user" label="用户" min-width="120" />
        <el-table-column prop="channel" label="咨询方式" min-width="110">
          <template #default="{ row }">
            <el-tag :type="row.channel === 'AI对话' ? 'primary' : 'warning'" effect="light">
              {{ row.channel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="risk" label="风险等级" min-width="100">
          <template #default="{ row }">
            <el-tag :type="riskTagType(row.risk)" effect="dark">{{ row.risk }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" min-width="90" />
        <el-table-column prop="startTime" label="开始时间" min-width="170" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import PageHead from '@/components/PageHead.vue'
import { getDashboard } from '@/api/dashboard'

const router = useRouter()

const loading = ref(false)
const statCards = ref([])
const recentList = ref([])

const riskTagType = (risk) => ({ 低: 'success', 中: 'warning', 高: 'danger' }[risk] || 'info')
const statusTagType = (status) =>
  ({ 已结束: 'info', 进行中: 'success', 已转人工: 'warning' }[status] || 'info')

const goConsultation = () => router.push('/back/consultation')

// ECharts 图表
const lineChartRef = ref(null)
const pieChartRef = ref(null)
let lineChart = null
let pieChart = null

const renderLineChart = (line) => {
  if (!lineChart) return
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: line.dates,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f2f5' } },
    },
    series: [
      {
        name: 'AI对话',
        type: 'line',
        smooth: true,
        data: line.ai,
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.25)' },
            { offset: 1, color: 'rgba(64,158,255,0.02)' },
          ]),
        },
      },
      {
        name: '情绪日记',
        type: 'line',
        smooth: true,
        data: line.diary,
        itemStyle: { color: '#e6a23c' },
      },
    ],
    legend: { data: ['AI对话', '情绪日记'], right: 0, top: 0 },
  })
}

const renderPieChart = (pie) => {
  if (!pieChart) return
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, left: 'center' },
    series: [
      {
        name: '情绪分布',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: pie.map((item) => ({
          value: item.value,
          name: item.name,
          itemStyle: { color: item.color },
        })),
      },
    ],
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getDashboard()
    statCards.value = data.statCards
    recentList.value = data.recentList
    await nextTick()
    if (!lineChart) lineChart = echarts.init(lineChartRef.value)
    if (!pieChart) pieChart = echarts.init(pieChartRef.value)
    renderLineChart(data.lineChart)
    renderPieChart(data.pieChart)
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  lineChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  pieChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-row {
    margin-bottom: 20px;
  }

  .stat-card {
    margin-bottom: 12px;

    .stat-inner {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .stat-info {
      .stat-title {
        margin: 0;
        font-size: 13px;
        color: #909399;
      }

      .stat-value {
        margin: 4px 0;
        font-size: 26px;
        font-weight: bold;
        color: #303133;
      }

      .stat-trend {
        margin: 0;
        font-size: 12px;
        display: flex;
        align-items: center;
        gap: 2px;

        &.up {
          color: #67c23a;
        }

        &.down {
          color: #f56c6c;
        }
      }
    }
  }

  .chart-card {
    margin-bottom: 20px;
  }

  .chart-box {
    width: 100%;
    height: 320px;
  }

  .table-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: bold;
    color: #303133;
  }
}
</style>
