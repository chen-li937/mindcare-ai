import request from '@/utils/request'
import { mockGetArticles, mockSaveArticle, mockDeleteArticle } from '@/mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

/**
 * 知识文章分页列表
 * 真实接口：GET /api/article/page?keyword=&category=&status=&page=&pageSize=
 * 返回：{ list, total }，list 项：{ id, title, category, tags, summary, content, author, readMinutes, views, status, publishTime, ... }
 */
export function getArticles(params) {
  if (USE_MOCK) return mockGetArticles(params)
  return request.get('/article/page', { params }).then((res) => res.data)
}

/**
 * 分类列表（表单下拉框）
 * 真实接口：GET /api/article/category/list
 */
export function getCategoryList() {
  return request.get('/article/category/list').then((res) => res.data)
}

/**
 * 新增文章
 * 真实接口：POST /api/article  body: { category, title, summary, content, tags, author, readMinutes, status, isTop }
 */
export function createArticle(data) {
  if (USE_MOCK) return mockSaveArticle(data)
  return request.post('/article', data).then((res) => res.data)
}

/**
 * 编辑文章
 * 真实接口：PUT /api/article/{id}
 */
export function updateArticle(data) {
  if (USE_MOCK) return mockSaveArticle(data)
  return request.put(`/article/${data.id}`, data).then((res) => res.data)
}

/**
 * 删除文章
 * 真实接口：DELETE /api/article/{id}
 */
export function deleteArticle(id) {
  if (USE_MOCK) return mockDeleteArticle(id)
  return request.delete(`/article/${id}`).then((res) => res.data)
}
