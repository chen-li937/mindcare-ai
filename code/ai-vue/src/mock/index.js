/**
 * 本地 Mock 层
 * 仅在 VITE_USE_MOCK=true 时生效。所有返回结构模拟后端统一响应 { code, message, data }。
 * 后端就绪后，将 .env.development 中 VITE_USE_MOCK 改为 false 即可，页面无需改动。
 */

const delay = (ms = 300) => new Promise((resolve) => setTimeout(resolve, ms))

// 模拟成功响应（api 层只关心 data，故直接返回 data 本体）
export async function mockOk(data, ms = 300) {
  await delay(ms)
  return data
}

// 模拟失败
export async function mockFail(message, ms = 300) {
  await delay(ms)
  throw new Error(message)
}

// 通用分页：从数组中按条件过滤并分页
export function paginate(list, params = {}) {
  const { page = 1, pageSize = 10 } = params
  const start = (page - 1) * pageSize
  return {
    list: list.slice(start, start + pageSize),
    total: list.length,
  }
}

/* ---------------- 认证 ---------------- */

const MOCK_USER = {
  id: 1,
  username: 'admin',
  name: '系统管理员',
  avatar: '',
  roles: ['admin'],
}

export async function mockLogin({ username, password }) {
  if (username === 'admin' && password === '123456') {
    return mockOk({
      token: 'mock-token-' + Date.now(),
    })
  }
  return mockFail('用户名或密码错误')
}

export function mockUserInfo() {
  return mockOk({ ...MOCK_USER })
}

/* ---------------- 数据分析 ---------------- */

const statCards = [
  { title: '今日AI咨询', value: '128', trend: 12.5, icon: 'ChatDotRound', color: '#409eff', bg: '#ecf5ff' },
  { title: '注册用户', value: '3,462', trend: 8.2, icon: 'User', color: '#67c23a', bg: '#f0f9eb' },
  { title: '知识文章', value: '86', trend: 3.1, icon: 'Document', color: '#e6a23c', bg: '#fdf6ec' },
  { title: '高风险预警', value: '7', trend: -15.4, icon: 'Warning', color: '#f56c6c', bg: '#fef0f0' },
]

export function mockDashboard() {
  return mockOk({
    statCards,
    lineChart: {
      dates: ['09-03', '09-04', '09-05', '09-06', '09-07', '09-08', '09-09'],
      ai: [96, 112, 108, 85, 72, 118, 128],
      hotline: [12, 15, 9, 6, 5, 14, 16],
    },
    pieChart: [
      { value: 320, name: '平静', color: '#409eff' },
      { value: 268, name: '愉悦', color: '#67c23a' },
      { value: 196, name: '焦虑', color: '#e6a23c' },
      { value: 124, name: '低落', color: '#909399' },
      { value: 52, name: '愤怒', color: '#f56c6c' },
    ],
    recentList: [
      { user: '林**', channel: 'AI对话', risk: '低', duration: '12分钟', startTime: '2026-09-09 20:31', status: '已结束' },
      { user: '王**', channel: 'AI对话', risk: '中', duration: '25分钟', startTime: '2026-09-09 19:48', status: '已转人工' },
      { user: '陈**', channel: '热线', risk: '高', duration: '41分钟', startTime: '2026-09-09 18:20', status: '已结束' },
      { user: '赵**', channel: 'AI对话', risk: '低', duration: '8分钟', startTime: '2026-09-09 17:05', status: '已结束' },
      { user: '周**', channel: 'AI对话', risk: '低', duration: '15分钟', startTime: '2026-09-09 16:42', status: '进行中' },
    ],
  })
}

/* ---------------- 知识文章 ---------------- */

let articleIdSeq = 6
const articles = [
  { id: 1, title: '如何识别焦虑情绪的早期信号', category: '情绪管理', tags: ['焦虑', '自测'], views: 2341, status: '已发布', publishTime: '2026-09-05', content: '焦虑情绪往往在早期就有迹可循，如持续的紧张感、睡眠变差、注意力下降……' },
  { id: 2, title: '睡前放松的5个实用小技巧', category: '睡眠改善', tags: ['失眠', '放松'], views: 1876, status: '已发布', publishTime: '2026-09-02', content: '良好的睡眠是心理健康的基石。本文介绍5个经实证有效的睡前放松方法……' },
  { id: 3, title: '高压力环境下如何保持心理弹性', category: '压力调节', tags: ['压力', '韧性'], views: 1532, status: '已发布', publishTime: '2026-08-28', content: '心理弹性是指人在面对逆境时能够恢复和成长的能力……' },
  { id: 4, title: '学会非暴力沟通，改善亲密关系', category: '人际交往', tags: ['沟通', '亲密关系'], views: 1209, status: '草稿', publishTime: '—', content: '非暴力沟通包含观察、感受、需要、请求四个要素……' },
  { id: 5, title: '每天10分钟正念冥想入门指南', category: '自我成长', tags: ['正念', '冥想'], views: 986, status: '已发布', publishTime: '2026-08-20', content: '正念冥想不需要特殊装备，只需要一个安静的角落和十分钟时间……' },
  { id: 6, title: '抑郁情绪与抑郁症的区别', category: '情绪管理', tags: ['抑郁', '科普'], views: 3210, status: '已发布', publishTime: '2026-08-15', content: '每个人都会有情绪低落的时候，但抑郁情绪不等同于抑郁症……' },
]

export async function mockGetArticles(params) {
  let list = [...articles]
  if (params.keyword) list = list.filter((a) => a.title.includes(params.keyword))
  if (params.category) list = list.filter((a) => a.category === params.category)
  if (params.status) list = list.filter((a) => a.status === params.status)
  return mockOk(paginate(list, params))
}

export async function mockSaveArticle(data) {
  if (data.id) {
    const target = articles.find((a) => a.id === data.id)
    Object.assign(target, data)
    return mockOk(target)
  }
  const created = { ...data, id: ++articleIdSeq, views: 0 }
  articles.unshift(created)
  return mockOk(created)
}

export async function mockDeleteArticle(id) {
  const idx = articles.findIndex((a) => a.id === id)
  if (idx > -1) articles.splice(idx, 1)
  return mockOk(null)
}

/* ---------------- 咨询记录 ---------------- */

const consultations = [
  {
    id: 1, user: '林**', channel: 'AI对话', summary: '近期工作压力大，入睡困难', risk: '低',
    duration: '12分钟', messages: 8, startTime: '2026-09-09 20:31', status: '已结束',
    chat: [
      { from: 'user', text: '最近工作压力特别大，晚上总是睡不着，怎么办？', time: '20:31' },
      { from: 'ai', text: '听起来你最近承受了不少压力。入睡困难通常和睡前大脑仍处于兴奋状态有关。我们可以先试试4-7-8呼吸法……', time: '20:32' },
      { from: 'user', text: '好的，我今晚试试。', time: '20:42' },
      { from: 'ai', text: '很棒的决定！如果一周后仍无改善，建议预约专业咨询师进一步评估。', time: '20:43' },
    ],
  },
  {
    id: 2, user: '王**', channel: 'AI对话', summary: '与同事关系紧张，情绪持续低落', risk: '中',
    duration: '25分钟', messages: 16, startTime: '2026-09-09 19:48', status: '已转人工',
    chat: [
      { from: 'user', text: '我和同组同事闹矛盾了，现在每天上班都很压抑，觉得自己什么都做不好。', time: '19:48' },
      { from: 'ai', text: '职场人际冲突确实会让人消耗很大。你说的“什么都做不好”，是一直以来的想法，还是这次冲突后才出现的？', time: '19:50' },
      { from: 'user', text: '好像一直都有点，这次更严重了。', time: '19:55' },
      { from: 'ai', text: '我注意到这种低自我评价可能持续了较长时间，为了给你更专业的支持，我将为你转接人工咨询师……', time: '19:58' },
    ],
  },
  {
    id: 3, user: '陈**', channel: '热线', summary: '表达强烈绝望感，存在自伤念头', risk: '高',
    duration: '41分钟', messages: 24, startTime: '2026-09-09 18:20', status: '已结束',
    chat: [
      { from: 'user', text: '我觉得活着没什么意思了，有时候会想干脆消失算了。', time: '18:20' },
      { from: 'ai', text: '谢谢你愿意告诉我这些。我很在意你的安全——你现在有具体的伤害自己的计划吗？', time: '18:21' },
      { from: 'user', text: '……有想过，但还没准备做。', time: '18:25' },
      { from: 'ai', text: '你现在的安全最重要。我会立即为你接通24小时危机干预热线，请不要挂断……', time: '18:26' },
    ],
  },
  {
    id: 4, user: '赵**', channel: 'AI对话', summary: '咨询考前焦虑调节方法', risk: '低',
    duration: '8分钟', messages: 6, startTime: '2026-09-09 17:05', status: '已结束',
    chat: [
      { from: 'user', text: '下周就要考试了，紧张到看不进去书。', time: '17:05' },
      { from: 'ai', text: '考前焦虑很常见。可以尝试把复习任务拆成25分钟的小块，每块之间休息5分钟……', time: '17:06' },
    ],
  },
  {
    id: 5, user: '周**', channel: 'AI对话', summary: '倾诉失恋后的情绪波动', risk: '低',
    duration: '15分钟', messages: 10, startTime: '2026-09-09 16:42', status: '进行中',
    chat: [
      { from: 'user', text: '分手两周了，还是会突然想哭。', time: '16:42' },
      { from: 'ai', text: '丧失一段亲密关系后，情绪反复是非常正常的哀伤反应……', time: '16:43' },
    ],
  },
]

export async function mockGetConsultations(params) {
  let list = [...consultations]
  if (params.keyword) {
    list = list.filter(
      (c) => c.user.includes(params.keyword) || c.summary.includes(params.keyword)
    )
  }
  if (params.risk) list = list.filter((c) => c.risk === params.risk)
  if (params.channel) list = list.filter((c) => c.channel === params.channel)
  if (params.dateRange?.length === 2) {
    list = list.filter((c) => {
      const day = c.startTime.slice(0, 10)
      return day >= params.dateRange[0] && day <= params.dateRange[1]
    })
  }
  return mockOk(paginate(list, params))
}

export async function mockEscalate(id) {
  const target = consultations.find((c) => c.id === id)
  if (target) target.status = '已转人工'
  return mockOk(null)
}

/* ---------------- 情绪日志 ---------------- */

const moodLogs = [
  { id: 1, user: '林**', mood: '焦虑', score: 4, content: '明天要做项目汇报，今晚一直反复想幻灯片，手心出汗。', aiReply: '你的身体反应是焦虑的典型表现。试试5-4-3-2-1着陆法，把注意力拉回当下……', createdAt: '2026-09-09 21:10' },
  { id: 2, user: '王**', mood: '低落', score: 3, content: '周末一个人待着，觉得没人在乎我，什么都不想做。', aiReply: '孤独感袭来时尤其难熬。哪怕一件小事——给朋友发条消息，都可能让今天不同……', createdAt: '2026-09-09 19:22' },
  { id: 3, user: '赵**', mood: '愉悦', score: 9, content: '今天跑完了5公里，还和朋友吃了火锅，心情特别好！', aiReply: '运动和社交都是情绪的“充电宝”，为你开心！记得把这种状态记录下来……', createdAt: '2026-09-09 18:05' },
  { id: 4, user: '周**', mood: '平静', score: 7, content: '今天按计划完成了工作，晚上看了会儿书，挺安稳的一天。', aiReply: '安稳本身就是一种很好的状态。规律的生活节奏是情绪稳定的基石。', createdAt: '2026-09-09 16:40' },
  { id: 5, user: '吴**', mood: '愤怒', score: 2, content: '辛苦做的方案被同事抢了功劳，气得手抖，又不敢当场翻脸。', aiReply: '被夺走成果的愤怒完全合理。压抑不会让愤怒消失，我们可以一起想想如何表达边界……', createdAt: '2026-09-09 14:18' },
  { id: 6, user: '郑**', mood: '焦虑', score: 5, content: '考研报名开始了，还没确定学校，越想越慌。', aiReply: '不确定性最容易引发焦虑。我们可以把“选学校”拆成几个可执行的小步骤……', createdAt: '2026-09-08 22:30' },
  { id: 7, user: '孙**', mood: '平静', score: 6, content: '普通的一天，没什么特别的情绪波动。', aiReply: '能平静地度过一天，也是心理健康的表现。', createdAt: '2026-09-08 20:12' },
  { id: 8, user: '钱**', mood: '愉悦', score: 8, content: '养的多肉开花了，小小的惊喜，开心了好久。', aiReply: '能为生活中的小确幸感到快乐，是很珍贵的能力。', createdAt: '2026-09-08 12:08' },
]

export async function mockGetMoodLogs(params) {
  let list = [...moodLogs]
  if (params.keyword) list = list.filter((l) => l.user.includes(params.keyword))
  if (params.mood) list = list.filter((l) => l.mood === params.mood)
  return mockOk(paginate(list, params))
}

export async function mockGetMoodStats() {
  const distribution = ['愉悦', '平静', '焦虑', '低落', '愤怒'].map((name) => ({
    name,
    count: moodLogs.filter((l) => l.mood === name).length,
  }))
  const avgScore =
    moodLogs.reduce((sum, l) => sum + l.score, 0) / (moodLogs.length || 1)
  return mockOk({ total: moodLogs.length, distribution, avgScore })
}
