<template>
  <div class="knowledge-page">
    <PageHead title="知识文章" subtitle="管理面向用户推送的心理健康科普内容">
      <template #buttons>
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>新增文章
        </el-button>
      </template>
    </PageHead>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="文章标题">
          <el-input v-model="query.keyword" placeholder="请输入标题关键词" clearable style="width: 220px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部分类" clearable style="width: 160px">
            <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="已发布" value="已发布" />
            <el-option label="草稿" value="草稿" />
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

    <!-- 文章列表 -->
    <el-card shadow="never" v-loading="loading">
      <el-table :data="tableList" stripe style="width: 100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="title" label="文章标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" min-width="110">
          <template #default="{ row }">
            <el-tag effect="light">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="t in row.tags" :key="t" size="small" class="tag-item">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="阅读量" min-width="90" sortable />
        <el-table-column prop="status" label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '已发布' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" min-width="110" sortable />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :page-sizes="[5, 10, 20]"
          :total="page.total"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="fetchList"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑文章' : '新增文章'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tagsText" placeholder="多个标签用逗号分隔，如：焦虑,失眠" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="已发布">已发布</el-radio>
            <el-radio value="草稿">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文章内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入文章正文内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHead from '@/components/PageHead.vue'
import { getArticles, getCategoryList, createArticle, updateArticle, deleteArticle } from '@/api/knowledge'

const categoryOptions = ref([])

const loading = ref(false)
const submitting = ref(false)
const tableList = ref([])

const query = reactive({ keyword: '', category: '', status: '' })
const page = reactive({ current: 1, size: 5, total: 0 })

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getArticles({
      keyword: query.keyword,
      category: query.category,
      status: query.status,
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
  Object.assign(query, { keyword: '', category: '', status: '' })
  page.current = 1
  fetchList()
}
const handleSizeChange = () => {
  page.current = 1
  fetchList()
}

// 新增 / 编辑
const formRef = ref(null)
const dialog = reactive({ visible: false, isEdit: false, editId: null })
const defaultForm = () => ({ title: '', category: '', tagsText: '', status: '草稿', content: '' })
const form = reactive(defaultForm())

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }],
}

const openCreate = () => {
  Object.assign(form, defaultForm())
  dialog.isEdit = false
  dialog.editId = null
  dialog.visible = true
}

const openEdit = (row) => {
  Object.assign(form, {
    title: row.title,
    category: row.category,
    status: row.status === '已下架' ? '草稿' : row.status,
    content: row.content,
    tagsText: (row.tags || []).join(','),
  })
  dialog.isEdit = true
  dialog.editId = row.id
  dialog.visible = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const tags = form.tagsText.split(/[,，]/).map((t) => t.trim()).filter(Boolean)
      const payload = {
        title: form.title,
        category: form.category,
        tags,
        status: form.status,
        content: form.content,
      }
      if (dialog.isEdit) {
        await updateArticle({ id: dialog.editId, ...payload })
        ElMessage.success('文章已更新')
      } else {
        await createArticle(payload)
        ElMessage.success('文章已创建')
      }
      dialog.visible = false
      fetchList()
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除文章「${row.title}」吗？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await deleteArticle(row.id)
      ElMessage.success('删除成功')
      // 删的是当前页最后一条时回退一页
      if (tableList.value.length === 1 && page.current > 1) page.current -= 1
      fetchList()
    })
    .catch(() => {})
}

onMounted(() => {
  fetchList()
  getCategoryList()
    .then((list) => {
      categoryOptions.value = (list || []).map((c) => c.name)
    })
    .catch(() => {})
})
</script>

<style lang="scss" scoped>
.knowledge-page {
  .filter-card {
    margin-bottom: 16px;

    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }

  .tag-item {
    margin-right: 6px;
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
