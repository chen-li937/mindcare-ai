<template>
  <div class="emotional-page">
    <PageHead title="情绪日志" subtitle="跟踪用户心情变化，及时发现需要关怀的对象" />

    <!-- 本周情绪概览 -->
    <el-row :gutter="20" class="overview-row" v-loading="statsLoading">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="overview-card">
          <template #header>
            <div class="card-header">
              <span>本周情绪分布</span>
              <el-tag type="info" size="small">共 {{ stats.total }} 条日志</el-tag>
            </div>
          </template>
          <div v-for="m in moodBarList" :key="m.name" class="mood-bar-row">
            <span class="mood-name">{{ moodMeta(m.name).emoji }} {{ m.name }}</span>
            <el-progress
              :percentage="percent(m.count)"
              :color="moodMeta(m.name).color"
              :stroke-width="14"
              class="mood-progress"
            />
            <span class="mood-count">{{ m.count }} 次</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="overview-card">
          <template #header>
            <div class="card-header"><span>本周平均情绪评分</span></div>
          </template>
          <div class="score-box">
            <el-progress
              type="dashboard"
              :percentage="Math.round((stats.avgScore / 5) * 100)"
              :color="scoreColor"
              :width="140"
            >
              <template #default>
                <span class="score-num">{{ stats.avgScore.toFixed(1) }}</span>
                <span class="score-total">/ 5</span>
              </template>
            </el-progress>
            <p class="score-desc">{{ scoreDesc }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="日志内容关键词" clearable style="width: 180px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="心情">
          <el-select v-model="query.mood" placeholder="全部心情" clearable style="width: 140px">
            <el-option v-for="m in moodOptions" :key="m.name" :label="`${m.emoji} ${m.name}`" :value="m.name" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableList" stripe style="width: 100%">
        <el-table-column prop="user" label="用户" min-width="100" />
        <el-table-column prop="mood" label="心情" min-width="110">
          <template #default="{ row }">
            <el-tag
              :color="moodMeta(row.mood).bg"
              :style="{ color: moodMeta(row.mood).color, border: 'none' }"
            >
              {{ moodMeta(row.mood).emoji }} {{ row.mood }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="情绪评分" min-width="160">
          <template #default="{ row }">
            <el-rate v-model="row.score" disabled :max="5" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="日志内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="记录时间" min-width="160" sortable />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :page-sizes="[10, 20, 50]"
          :total="page.total"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="fetchLogs"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 日志详情弹窗 -->
    <el-dialog v-model="detail.visible" title="情绪日志详情" width="520px">
      <template v-if="detail.row">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="用户">{{ detail.row.user }}</el-descriptions-item>
          <el-descriptions-item label="心情">
            {{ moodMeta(detail.row.mood).emoji }} {{ detail.row.mood }}
          </el-descriptions-item>
          <el-descriptions-item label="情绪评分">
            <el-rate v-model="detail.row.score" disabled :max="5" />
            <span class="detail-score">{{ detail.row.score }} / 5</span>
          </el-descriptions-item>
          <el-descriptions-item label="记录时间">{{ detail.row.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="日志内容">{{ detail.row.content }}</el-descriptions-item>
          <el-descriptions-item label="AI 回应">{{ detail.row.aiReply }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="detail.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import PageHead from '@/components/PageHead.vue'
import { getMoodLogs, getMoodStats } from '@/api/emotional'

const moodOptions = [
  { name: '愉悦', emoji: '😄', color: '#67c23a', bg: '#f0f9eb' },
  { name: '平静', emoji: '🙂', color: '#409eff', bg: '#ecf5ff' },
  { name: '平稳', emoji: '😐', color: '#00b894', bg: '#e8fff8' },
  { name: '焦虑', emoji: '😰', color: '#e6a23c', bg: '#fdf6ec' },
  { name: '低落', emoji: '😔', color: '#909399', bg: '#f4f4f5' },
  { name: '愤怒', emoji: '😠', color: '#f56c6c', bg: '#fef0f0' },
]

const moodMeta = (name) => moodOptions.find((m) => m.name === name) || moodOptions[1]

const statsLoading = ref(false)
const loading = ref(false)
const tableList = ref([])

const stats = reactive({
  total: 0,
  distribution: [],
  avgScore: 0,
})

const moodBarList = computed(() =>
  moodOptions.map((m) => ({
    name: m.name,
    count: stats.distribution.find((d) => d.name === m.name)?.count || 0,
  }))
)

const percent = (count) => (stats.total ? Math.round((count / stats.total) * 100) : 0)

const scoreColor = computed(() => {
  if (stats.avgScore >= 4) return '#67c23a'
  if (stats.avgScore >= 3) return '#e6a23c'
  return '#f56c6c'
})

const scoreDesc = computed(() => {
  if (stats.avgScore >= 4) return '整体情绪状态良好，请继续保持'
  if (stats.avgScore >= 3) return '情绪有波动，建议关注并适当调节'
  return '情绪偏低落，建议主动寻求支持'
})

const fetchStats = async () => {
  statsLoading.value = true
  try {
    const data = await getMoodStats()
    stats.total = data.total
    stats.distribution = data.distribution
    stats.avgScore = data.avgScore
  } finally {
    statsLoading.value = false
  }
}

const query = reactive({ keyword: '', mood: '' })
const page = reactive({ current: 1, size: 10, total: 0 })

const fetchLogs = async () => {
  loading.value = true
  try {
    const data = await getMoodLogs({
      keyword: query.keyword,
      mood: query.mood,
      page: page.current,
      pageSize: page.size,
    })
    tableList.value = data.list
    page.total = data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.current = 1
  fetchLogs()
}
const handleReset = () => {
  Object.assign(query, { keyword: '', mood: '' })
  page.current = 1
  fetchLogs()
}
const handleSizeChange = () => {
  page.current = 1
  fetchLogs()
}

const detail = reactive({ visible: false, row: null })
const openDetail = (row) => {
  detail.row = row
  detail.visible = true
}

onMounted(() => {
  fetchStats()
  fetchLogs()
})
</script>

<style lang="scss" scoped>
.emotional-page {
  .overview-row {
    margin-bottom: 16px;
  }

  .overview-card {
    margin-bottom: 12px;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: bold;
    color: #303133;
  }

  .mood-bar-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 18px;

    .mood-name {
      width: 80px;
      flex-shrink: 0;
      font-size: 14px;
      color: #606266;
    }

    .mood-progress {
      flex: 1;
    }

    .mood-count {
      width: 48px;
      flex-shrink: 0;
      font-size: 13px;
      color: #909399;
      text-align: right;
    }
  }

  .score-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px 0;

    .score-num {
      font-size: 30px;
      font-weight: bold;
      color: #303133;
    }

    .score-total {
      font-size: 14px;
      color: #909399;
    }

    .score-desc {
      margin: 16px 0 0;
      font-size: 14px;
      color: #606266;
    }
  }

  .filter-card {
    margin-bottom: 16px;

    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .detail-score {
    margin-left: 10px;
    font-size: 13px;
    color: #909399;
  }
}
</style>
