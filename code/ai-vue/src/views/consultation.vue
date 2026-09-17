<template>
  <div class="consultation-page">
    <PageHead title="咨询记录" subtitle="查看 AI 对话与热线咨询记录，跟进高风险会话">
      <template #buttons>
        <el-button :icon="Download" @click="handleExport">导出记录</el-button>
      </template>
    </PageHead>

    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="用户/摘要关键词" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="query.risk" placeholder="全部等级" clearable style="width: 140px">
            <el-option label="低风险" value="低" />
            <el-option label="中风险" value="中" />
            <el-option label="高风险" value="高" />
          </el-select>
        </el-form-item>
        <el-form-item label="咨询方式">
          <el-select v-model="query.channel" placeholder="全部方式" clearable style="width: 140px">
            <el-option label="AI对话" value="AI对话" />
            <el-option label="热线" value="热线" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
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

    <!-- 记录列表 -->
    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableList" stripe style="width: 100%">
        <el-table-column prop="user" label="用户" min-width="100" />
        <el-table-column prop="channel" label="咨询方式" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.channel === 'AI对话' ? 'primary' : 'warning'" effect="light">
              {{ row.channel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="咨询摘要" min-width="220" show-overflow-tooltip />
        <el-table-column prop="risk" label="风险等级" min-width="100">
          <template #default="{ row }">
            <el-tag :type="riskTagType(row.risk)" effect="dark">{{ row.risk }}风险</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" min-width="90" />
        <el-table-column prop="messages" label="消息数" min-width="80" />
        <el-table-column prop="startTime" label="开始时间" min-width="160" sortable />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">查看详情</el-button>
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
          @current-change="fetchList"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 对话详情弹窗 -->
    <el-dialog v-model="detail.visible" title="咨询详情" width="640px">
      <template v-if="detail.row">
        <el-descriptions :column="2" border size="small" class="detail-desc">
          <el-descriptions-item label="用户">{{ detail.row.user }}</el-descriptions-item>
          <el-descriptions-item label="咨询方式">{{ detail.row.channel }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="riskTagType(detail.row.risk)" effect="dark" size="small">{{ detail.row.risk }}风险</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.row.status }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ detail.row.startTime }}</el-descriptions-item>
          <el-descriptions-item label="时长">{{ detail.row.duration }}</el-descriptions-item>
        </el-descriptions>

        <div class="chat-box" v-loading="detail.loading">
          <el-empty v-if="!detail.loading && detail.chat.length === 0" description="暂无聊天记录" :image-size="60" />
          <div v-for="(msg, i) in detail.chat" :key="i" class="chat-item" :class="msg.from">
            <el-avatar :size="32" class="chat-avatar">
              {{ msg.from === 'ai' ? 'AI' : 'U' }}
            </el-avatar>
            <div class="chat-bubble">
              <p class="chat-text">{{ msg.text }}</p>
              <p class="chat-time">{{ msg.time }}</p>
            </div>
          </div>
        </div>

        <div v-if="detail.row.risk === '高'" class="risk-tip">
          <el-alert
            title="该会话被识别为高风险（可能涉及自伤/自杀倾向），已自动转介人工干预"
            type="error"
            :closable="false"
            show-icon
          />
        </div>
      </template>
      <template #footer>
        <el-button @click="detail.visible = false">关闭</el-button>
        <el-button type="warning" :loading="escalating" @click="handleEscalate">转人工跟进</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import PageHead from '@/components/PageHead.vue'
import { getConsultations, getConsultationMessages, escalateConsultation } from '@/api/consultation'

const loading = ref(false)
const escalating = ref(false)
const tableList = ref([])

const query = reactive({ keyword: '', risk: '', channel: '', dateRange: null })
const page = reactive({ current: 1, size: 10, total: 0 })

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getConsultations({
      keyword: query.keyword,
      risk: query.risk,
      channel: query.channel,
      dateRange: query.dateRange,
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
  fetchList()
}
const handleReset = () => {
  Object.assign(query, { keyword: '', risk: '', channel: '', dateRange: null })
  page.current = 1
  fetchList()
}
const handleSizeChange = () => {
  page.current = 1
  fetchList()
}

const riskTagType = (risk) => ({ 低: 'success', 中: 'warning', 高: 'danger' }[risk] || 'info')
const statusTagType = (status) =>
  ({ 已结束: 'info', 进行中: 'success', 已转人工: 'warning' }[status] || 'info')

// 详情弹窗
const detail = reactive({ visible: false, row: null, chat: [], loading: false })
const openDetail = async (row) => {
  detail.row = row
  detail.chat = []
  detail.visible = true
  detail.loading = true
  try {
    detail.chat = (await getConsultationMessages(row.id)) || []
  } catch (e) {
    detail.chat = []
  } finally {
    detail.loading = false
  }
}

const handleEscalate = async () => {
  escalating.value = true
  try {
    await escalateConsultation(detail.row.id)
    detail.row.status = '已转人工'
    ElMessage.success('已创建人工跟进工单')
    detail.visible = false
    fetchList()
  } finally {
    escalating.value = false
  }
}

const handleExport = () => {
  ElMessage.info('导出功能待接入后端（演示）')
}

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.consultation-page {
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

  .detail-desc {
    margin-bottom: 16px;
  }

  .chat-box {
    max-height: 320px;
    overflow-y: auto;
    background-color: #f5f7fa;
    border-radius: 8px;
    padding: 16px;
  }

  .chat-item {
    display: flex;
    gap: 10px;
    margin-bottom: 16px;

    &.ai {
      flex-direction: row;
    }

    &.user {
      flex-direction: row-reverse;

      .chat-bubble {
        background-color: #409eff;
        color: #fff;

        .chat-time {
          color: rgba(255, 255, 255, 0.7);
        }
      }
    }

    .chat-avatar {
      flex-shrink: 0;
      background-color: #67c23a;
      color: #fff;
      font-size: 12px;
    }

    &.user .chat-avatar {
      background-color: #909399;
    }

    .chat-bubble {
      max-width: 75%;
      background-color: #fff;
      border-radius: 8px;
      padding: 10px 12px;

      .chat-text {
        margin: 0;
        font-size: 14px;
        line-height: 1.6;
      }

      .chat-time {
        margin: 4px 0 0;
        font-size: 12px;
        color: #909399;
        text-align: right;
      }
    }
  }

  .risk-tip {
    margin-top: 12px;
  }
}
</style>
