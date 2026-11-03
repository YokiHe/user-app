<template>
  <div class="app-wrapper">
    <!-- ========== 登录页 ========== -->
    <div v-if="!user" class="login-page">
      <div class="login-card">
        <div class="login-header">
          <h2>👤 用户管理系统</h2>
          <p class="subtitle">第 10 章热身项目</p>
        </div>

        <el-alert
          v-if="loginMsg"
          :title="loginMsg"
          type="error"
          show-icon
          class="mb-16"
          :closable="false"
        />

        <el-form @keyup.enter="login">
          <el-form-item>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="logging"
            @click="login"
          >
            登 录
          </el-button>
        </el-form>

        <p class="tip">测试账号：kylin / Kylin@123</p>
      </div>
    </div>

    <!-- ========== 管理页 ========== -->
    <div v-else class="manage-page">
      <!-- 顶部导航 -->
      <el-header class="app-header">
        <div class="header-left">
          <h3>👤 用户管理系统</h3>
        </div>
        <div class="header-right">
          <el-tag type="success" effect="dark" class="mr-12">
            {{ user.nickname }}
          </el-tag>
          <el-button type="danger" size="small" @click="logout">
            退出登录
          </el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <div class="app-body" v-if="view === 'list'">
        <el-card class="content-card" shadow="hover">
          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="toolbar-left">
              <el-input
                v-model="keyword"
                placeholder="输入用户名搜索"
                class="search-input"
                clearable
                @clear="searchUsers"
                @keyup.enter="searchUsers"
              >
                <template #append>
                  <el-button @click="searchUsers">搜索</el-button>
                </template>
              </el-input>

              <el-button @click="loadUsers">查看全部</el-button>
            </div>

            <el-button type="success" @click="dialogVisible = true">
              <el-icon class="mr-4"><Plus /></el-icon>新增用户
            </el-button>
          </div>

          <!-- 表格 -->
          <el-table
            :data="list"
            border
            stripe
            class="user-table"
            v-loading="tableLoading"
            empty-text="暂无数据"
          >
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="nickname" label="昵称" min-width="120" />
            <el-table-column label="操作" width="140" align="center">
              <template #default="scope">
                <el-popconfirm
                  title="确定删除该用户吗？"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  @confirm="del(scope.row.id)"
                >
                  <template #reference>
                    <el-button size="small" type="danger" plain>
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="page"
              :page-size="size"
              :total="total"
              layout="prev, pager, next, total, jumper"
              background
            />
          </div>
        </el-card>
      </div>

      <!-- 新增用户弹窗 -->
      <el-dialog
        v-model="dialogVisible"
        title="新增用户"
        width="420px"
        :close-on-click-modal="false"
        class="add-dialog"
      >
        <el-form label-width="70px" class="add-form">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addUser" :loading="submitting">
            提交
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// ===== 接口前缀 =====
// 本地开发回退 localhost；生产环境由 .env.production 提供同源相对路径
const API = import.meta.env.VITE_API_BASE || 'http://localhost:8080/userapi/api/v1'

// ===== axios 封装 =====
function apiGet(url, params) {
  return axios.get(url, { params, withCredentials: true })
}
function apiPost(url, data) {
  return axios.post(url, data, { withCredentials: true })
}
function apiDelete(url) {
  return axios.delete(url, { withCredentials: true })
}

// ===== 登录 =====
const user = ref(null)
const view = ref('login')
const loginForm = ref({ username: '', password: '' })
const loginMsg = ref('')
const logging = ref(false)

async function login() {
  if (!loginForm.value.username || !loginForm.value.password) {
    loginMsg.value = '请输入用户名和密码'
    return
  }

  logging.value = true
  loginMsg.value = ''

  try {
    const res = await apiPost(API + '/auth/login', loginForm.value)
    if (res.data.code === 200) {
      user.value = res.data.data
      view.value = 'list'
      loginMsg.value = ''
      loadUsers()
      ElMessage.success('登录成功 🎉')
    } else {
      loginMsg.value = res.data.message
    }
  } catch (e) {
    loginMsg.value = '网络错误：后端未启动？'
  } finally {
    logging.value = false
  }
}

async function logout() {
  await apiPost(API + '/auth/logout')
  user.value = null
  view.value = 'login'
  loginForm.value = { username: '', password: '' }
  ElMessage.success('已退出登录')
}

// ===== 用户列表 =====
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 5
const keyword = ref('')
const tableLoading = ref(false)

async function loadUsers() {
  tableLoading.value = true
  try {
    const res = await apiGet(API + '/users', {
      page: page.value,
      size,
      username: keyword.value
    })

    const data = res.data
    if (data.code === 401) {
      user.value = null
      return
    }

    list.value = data.data.list
    total.value = data.data.total
  } finally {
    tableLoading.value = false
  }
}

function searchUsers() {
  page.value = 1
  loadUsers()
}

watch(page, loadUsers)

// ===== 新增用户 =====
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({ username: '', password: '', nickname: '' })

async function addUser() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('用户名和密码不能为空')
    return
  }

  submitting.value = true
  try {
    const res = await apiPost(API + '/users', form.value)
    if (res.data.code === 201) {
      ElMessage.success('新增成功 🎉')
      dialogVisible.value = false
      form.value = { username: '', password: '', nickname: '' }
      loadUsers()
    } else {
      ElMessage.error(res.data.message)
    }
  } finally {
    submitting.value = false
  }
}

// ===== 删除用户 =====
async function del(id) {
  const res = await apiDelete(`${API}/users/${id}`)
  if (res.data.code === 200) {
    ElMessage.success('删除成功')
    loadUsers()
  } else {
    ElMessage.error(res.data.message)
  }
}

// ===== 页面加载校验登录态 =====
onMounted(async () => {
  try {
    const res = await apiGet(API + '/auth/me')
    if (res.data.code === 200) {
      user.value = res.data.data
      view.value = 'list'
      loadUsers()
    }
  } catch (e) {
    // 未登录
  }
})
</script>

<style scoped>
/* ========== 全局 ========== */
.app-wrapper {
  min-height: 100vh;
  background: #f0f2f5;
}

/* ========== 登录页 ========== */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 380px;
  padding: 40px 32px 32px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: fadeInUp 0.5s ease-out;
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  letter-spacing: 4px;
}

.tip {
  margin-top: 16px;
  text-align: center;
  color: #c0c4cc;
  font-size: 12px;
}

/* ========== 管理页 ========== */
.manage-page {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 56px;
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.app-body {
  padding: 24px;
  animation: fadeIn 0.3s ease-out;
}

.content-card {
  border-radius: 12px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 240px;
}

.user-table {
  border-radius: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* ========== 弹窗 ========== */
.add-form {
  padding: 8px 0;
}

/* ========== 动画 ========== */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* ========== 工具类 ========== */
.mb-16 {
  margin-bottom: 16px;
}

.mr-4 {
  margin-right: 4px;
}

.mr-12 {
  margin-right: 12px;
}
</style>