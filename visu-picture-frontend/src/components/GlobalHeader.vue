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
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <!-- 用户信息展示栏 -->
      <a-col flex="260px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space class="user-info">
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '视界用户' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <router-link to="/my_space">
                      <UserOutlined />
                      我的空间
                    </router-link>
                  </a-menu-item>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
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
import { PictureOutlined, LogoutOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { MenuProps } from 'ant-design-vue'
import { useRouter } from 'vue-router'
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
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    label: '创建团队',
    title: '创建团队',
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

// 展示在菜单的路由数组（有团队空间时追加“我的团队”子菜单）
const items = computed<MenuProps['items']>(() => {
  const menus = filterMenus(originItems) ?? []
  if (teamSpaceList.value.length > 0) {
    menus.push({
      key: 'teamSpace',
      icon: () => h(TeamOutlined),
      label: '我的团队',
      children: teamSpaceList.value.map((spaceUser) => ({
        key: '/space/' + spaceUser.spaceId,
        label: spaceUser.space?.spaceName,
      })),
    })
  }
  return menus
})

// 团队空间列表（原侧边栏的“我的团队”菜单合并到这里）
const teamSpaceList = ref<API.SpaceUserVO[]>([])

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    teamSpaceList.value = res.data.data
  } else {
    message.error('加载我的团队空间失败，' + res.data.message)
  }
}

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  // 登录才加载
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})

const router = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>([])
// 监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 路由跳转事件
const doMenuClick = ({ key }: { key: any }) => {
  router.push({
    path: key,
  })
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
