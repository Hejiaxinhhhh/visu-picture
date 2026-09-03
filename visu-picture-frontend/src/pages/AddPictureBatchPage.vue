<template>
  <div id="addPictureBatchPage">
    <h2 style="margin-bottom: 16px">批量创建</h2>
    <!-- 图片信息表单 -->
    <a-form name="formData" layout="vertical" :model="formData" @finish="handleSubmit">
      <a-form-item name="searchText" label="关键词">
        <a-input v-model:value="formData.searchText" placeholder="请输入关键词" allow-clear />
      </a-form-item>
      <a-form-item name="count" label="抓取数量">
        <a-input-number
          v-model:value="formData.count"
          placeholder="请输入数量"
          style="min-width: 180px"
          :min="1"
          :max="30"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="namePrefix" label="名称前缀">
        <a-input
          v-model:value="formData.namePrefix"
          placeholder="请输入名称前缀，会自动补充序号"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
          执行任务
        </a-button>
      </a-form-item>
      <!-- 任务进度 -->
      <a-form-item v-if="loading && progress">
        <div class="progress-wrapper">
          <div class="progress-text">
            正在抓取并上传图片：已完成 {{ progress.done }} / {{ progress.total }} 张
          </div>
          <a-progress
            :percent="Math.round((progress.done / progress.total) * 100)"
            :status="progress.done >= progress.total ? 'success' : 'active'"
          />
        </div>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  getBatchUploadProgressUsingGet,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
  uploadPictureByBatchUsingPost,
} from '@/api/pictureController.ts'
import { useRoute, useRouter } from 'vue-router'

const formData = reactive<API.PictureUploadByBatchRequest>({
  count: 10,
})
// 提交任务状态
const loading = ref(false)
// 任务进度
const progress = ref<{ done: number; total: number } | null>(null)
// 轮询定时器
let progressTimer: number | null = null

const router = useRouter()

/**
 * 开始轮询任务进度
 */
const startProgressPolling = () => {
  stopProgressPolling()
  progressTimer = window.setInterval(async () => {
    try {
      const res = await getBatchUploadProgressUsingGet()
      if (res.data.code === 0 && res.data.data) {
        const [done, total] = res.data.data.split('/').map(Number)
        if (!Number.isNaN(done) && !Number.isNaN(total) && total > 0) {
          progress.value = { done, total }
        }
      }
    } catch (error) {
      // 轮询失败暂时忽略，下一次重试
    }
  }, 800)
}

/**
 * 停止轮询任务进度
 */
const stopProgressPolling = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

onUnmounted(() => {
  stopProgressPolling()
})

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  loading.value = true
  progress.value = null
  // 开始轮询进度
  startProgressPolling()
  let res
  try {
    res = await uploadPictureByBatchUsingPost({
      ...formData,
    })
  } finally {
    stopProgressPolling()
    loading.value = false
  }
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success(`创建成功，共 ${res.data.data} 条`)
    // 跳转到主页
    router.push({
      path: `/`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
  }
}
</script>

<style scoped>
#addPictureBatchPage {
  max-width: 720px;
  margin: 0 auto;
}

.progress-wrapper {
  padding: 12px 16px;
  border: 1px solid #e7ebf6;
  border-radius: 10px;
  background: rgba(61, 90, 245, 0.04);
}

.progress-text {
  margin-bottom: 8px;
  color: rgba(35, 44, 86, 0.7);
  font-size: 14px;
}
</style>
