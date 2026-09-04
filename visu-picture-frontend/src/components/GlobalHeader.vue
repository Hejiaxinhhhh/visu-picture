<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="260px">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo-full.svg" alt="visu 视界云图库" />
            <div class="title">视界云图库</div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <div class="nav-wrap">
          <a-menu
            v-model:selectedKeys="current"
            mode="horizontal"
            class="nav-menu"
            :items="items"
            @click="doMenuClick"
          />
        <!-- 我的团队：深色下拉面板（含团队列表与创建入口） -->
        <a-dropdown
          v-if="loginUserStore.loginUser.id"
          trigger="['hover', 'click']"
          @open-change="(v: boolean) => (teamDropdownOpen = v)"
        >
          <div class="team-trigger">
            <TeamOutlined class="team-icon" />
            <span>我的团队</span>
            <DownOutlined class="team-trigger-arrow" :class="{ open: teamDropdownOpen }" />
          </div>
          <template #overlay>
            <div class="team-panel">
              <div
                v-for="spaceUser in teamSpaceList"
                :key="spaceUser.spaceId"
                class="team-panel-item"
                @click="goTeamSpace(spaceUser.spaceId)"
              >
                <div class="item-title">{{ spaceUser.space?.spaceName ?? '未命名团队' }}</div>
                <div class="item-desc">团队空间 · {{ roleText(spaceUser.spaceRole) }}</div>
              </div>
              <div v-if="teamSpaceList.length > 0" class="team-panel-divider"></div>
              <div class="team-panel-item" @click="goCreateTeam">
                <div class="item-title">＋ 创建团队</div>
                <div class="item-desc">发起多人协作</div>
              </div>
            </div>
          </template>
        </a-dropdown>
        </div>
      </a-col>
      <!-- 用户信息展示栏 -->
      <a-col flex="260px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown trigger="['hover', 'click']">
              <a-space class="user-info">
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '视界用户' }}
              </a-space>
              <template #overlay>
                <div class="team-panel user-panel">
                  <!-- 顶部：用户名 + 身份 -->
                  <div class="user-panel-head">
                    <div class="item-title">{{ loginUserStore.loginUser.userName ?? '视界用户' }}</div>
                    <div class="item-desc">
                      {{ loginUserStore.loginUser.userRole === 'admin' ? '管理员' : '普通用户' }}
                    </div>
                  </div>
                  <div class="team-panel-divider"></div>
                  <!-- 菜单项 -->
                  <div class="team-panel-item user-panel-item" @click="router.push('/user/center')">
                    <IdcardOutlined class="item-icon" />
                    <span>用户中心</span>
                  </div>
                  <div class="team-panel-item user-panel-item" @click="router.push('/my_space')">
                    <FolderOutlined class="item-icon" />
                    <span>我的空间</span>
                  </div>
                  <div class="team-panel-divider"></div>
                  <div class="team-panel-item user-panel-item" @click="doLogout">
                    <LogoutOutlined class="item-icon" />
                    <span>退出登录</span>
                  </div>
                </div>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-space>
              <a-button href="/user/login">登录</a-button>
              <a-button type="primary" href="/user/register">注册</a-button>
            </a-space>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref, watchEffect } from 'vue'
import {
  PictureOutlined,
  LogoutOutlined,
  TeamOutlined,
  DownOutlined,
  IdcardOutlined,
  FolderOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { MenuProps } from 'ant-design-vue'
import { useRouter, useRoute } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController.ts'

const loginUserStore = useLoginUserStore()

// 未经过滤的菜单项（原侧边栏菜单已合并到这里）
const originItems = [
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: '公共图库',
    title: '公共图库',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/my_space',
    label: '我的空间',
    title: '我的空间',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
]

// 根据权限过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const key = String(menu?.key ?? '')
    // 管理员才能看到 /admin 开头的菜单
    if (key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    // 登录后才能看到空间相关菜单
    if ((key === '/my_space' || key.startsWith('/add_space')) && !loginUserStore.loginUser.id) {
      return false
    }
    return true
  })
}

// 展示在菜单的路由数组（团队入口已改为独立的下拉面板，不再放入 a-menu）
const items = computed<MenuProps['items']>(() => {
  return filterMenus(originItems) ?? []
})

// ----- 我的团队下拉面板 -----
// 角色文案
const roleText = (role?: string) => {
  if (role === 'admin') return '管理员'
  if (role === 'editor') return '编辑者'
  return '成员'
}
// 进入团队空间
const goTeamSpace = (spaceId?: string | number) => {
  if (!spaceId) return
  router.push(`/space/${spaceId}`)
}
// 创建团队
const goCreateTeam = () => {
  router.push('/add_space?type=' + SPACE_TYPE_ENUM.TEAM)
}

// 团队空间列表（原侧边栏的“我的团队”菜单合并到这里）
const teamSpaceList = ref<API.SpaceUserVO[]>([])
const route = useRoute()

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    const next = res.data.data ?? []
    // 内容没变化时不更新引用，避免菜单 items 频繁重建导致悬停弹出层失效
    if (JSON.stringify(next) !== JSON.stringify(teamSpaceList.value)) {
      teamSpaceList.value = next
    }
  } else {
    message.error('加载我的团队空间失败，' + res.data.message)
  }
}

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  // 登录才加载；同时依赖路由路径，创建/加入团队后跳转会自动刷新列表
  if (loginUserStore.loginUser.id) {
    route.path
    fetchTeamSpaceList()
  }
})

const router = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>([])
// 团队下拉面板展开状态（控制箭头旋转）
const teamDropdownOpen = ref(false)
// 监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 路由跳转事件
const doMenuClick = ({ key }: { key: any }) => {
  // 必须用字符串形式跳转，保留 key 中的 query 参数（如 /add_space?type=1）
  router.push(String(key))
}

// 用户注销
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#globalHeader .nav-wrap {
  display: flex;
  align-items: center;
}

#globalHeader .nav-menu {
  flex: 1;
  min-width: 0;
}

/* 我的团队触发器：与导航菜单文字风格一致 */
#globalHeader .team-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  height: 40px;
  cursor: pointer;
  color: #26283a;
  font-size: 15px;
  white-space: nowrap;
  border-radius: 8px;
  transition: color 0.2s ease;
}

#globalHeader .team-trigger:hover {
  color: #1890ff;
}

.team-trigger-arrow {
  font-size: 10px;
  opacity: 0.55;
  transition: transform 0.25s ease;
}

.team-trigger-arrow.open {
  transform: rotate(180deg);
}

/* hover 时图标掉落弹跳动画（仿悦目菜单物品掉落效果） */
@keyframes icon-drop-bounce {
  0% {
    transform: translateY(-16px) rotate(-8deg);
    opacity: 0;
  }
  55% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  70% {
    transform: translateY(-5px);
  }
  85% {
    transform: translateY(0);
  }
  92% {
    transform: translateY(-2px);
  }
  100% {
    transform: translateY(0);
  }
}

#globalHeader .team-trigger:hover .team-icon {
  animation: icon-drop-bounce 0.55s cubic-bezier(0.3, 0.6, 0.4, 1);
}

#globalHeader .title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: #171a2b;
  font-size: 17px;
  font-weight: 700;
  margin-left: 12px;
  padding-left: 12px;
  border-left: 1px solid #e4e8f2;
  line-height: 26px;
  white-space: nowrap;
}

.logo {
  height: 42px;
}

/* 顶部导航居中，贴近参考样式 */
#globalHeader :deep(.ant-menu-horizontal) {
  justify-content: center;
  border-bottom: none;
  background: transparent;
}

#globalHeader .user-login-status {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

#globalHeader .user-info {
  cursor: pointer;
  color: #26283a;
}
</style>

<style>
/* 团队下拉面板：渲染在 body 下，需全局样式（浅色主题面板） */
.team-panel {
  min-width: 216px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  border: 1px solid #e4e8f2;
  box-shadow: 0 12px 32px rgba(37, 55, 120, 0.12);
  backdrop-filter: blur(8px);
}

.team-panel .team-panel-item {
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.team-panel .team-panel-item:hover {
  background: #eef2ff;
}

.team-panel .item-title {
  color: #26283a;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.team-panel .item-desc {
  color: rgba(35, 44, 86, 0.55);
  font-size: 12px;
  line-height: 18px;
  margin-top: 1px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.team-panel .team-panel-item:hover .item-desc {
  color: #4f6bff;
}

.team-panel .team-panel-divider {
  height: 1px;
  margin: 6px 8px;
  background: #e4e8f2;
}

/* 头像用户面板：顶部用户名区 + 图标菜单项 */
.user-panel {
  min-width: 200px;
}

.user-panel .user-panel-head {
  padding: 8px 12px 10px;
}

.user-panel .user-panel-head .item-title {
  font-size: 15px;
}

.user-panel .user-panel-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #26283a;
  font-size: 14px;
  line-height: 22px;
}

.user-panel .item-icon {
  font-size: 15px;
  color: rgba(35, 44, 86, 0.6);
  transition: color 0.15s ease;
}

.user-panel .team-panel-item:hover .item-icon {
  color: #4f6bff;
}
</style>
