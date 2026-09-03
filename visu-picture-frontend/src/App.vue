<template>
  <div id="app">
    <a-config-provider :locale="zhCN" :theme="themeConfig">
      <BasicLayout />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import zhCN from 'ant-design-vue/es/locale/zh_CN';

// 全局主题：参考 Vexor 风格（蓝色主色 + 浅色圆角）
const themeConfig = {
  token: {
    colorPrimary: '#3d5af5',
    colorInfo: '#3d5af5',
    colorLink: '#3d5af5',
    borderRadius: 8,
    borderRadiusLG: 14,
    colorBgLayout: 'transparent',
    colorTextHeading: '#171a2b',
    fontFamily:
      "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif",
  },
  components: {
    Menu: {
      itemBorderRadius: 10,
      itemMarginInline: 8,
      itemSelectedBg: 'rgba(61, 90, 245, 0.08)',
      itemHoverBg: 'rgba(61, 90, 245, 0.06)',
    },
    Card: {
      borderRadiusLG: 14,
    },
  },
}
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
dayjs.locale('zh-cn');

import BasicLayout from '@/layouts/BasicLayout.vue'
// import { healthUsingGet } from '@/api/mainController.ts'
// import {useLoginUserStore} from "@/stores/useLoginUserStore.ts";

// 已经改为在权限校验文件中获取
// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser()

// healthUsingGet().then((res) => {
//   console.log(res)
// })
</script>

<style scoped></style>
