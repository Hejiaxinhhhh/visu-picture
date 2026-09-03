<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout>
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>
      <a-layout-footer class="footer">
        <a href="https://gitee.com/whirlwind-nanny" target="_blank"> 视界图库 by 何佳鑫(旋风) </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from "@/components/GlobalSider.vue";
</script>

<style scoped>
#basicLayout .header {
  padding-inline: 20px;
  background: #ffffff;
  color: unset;
  border-bottom: 1px solid #edf0f7;
  position: sticky;
  top: 0;
  z-index: 100;
}

#basicLayout .sider {
  background: transparent;
  padding-top: 20px;
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
}

#basicLayout .content {
  padding: 28px;
  background: transparent;
  margin-bottom: 28px;
}

#basicLayout .footer {
  background: transparent;
  padding: 16px;
  text-align: center;
  color: rgba(35, 44, 86, 0.45);
  font-size: 13px;
}

#basicLayout .footer a {
  color: rgba(35, 44, 86, 0.55);
}

#basicLayout .footer a:hover {
  color: #3d5af5;
}
</style>
