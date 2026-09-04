<template>
  <div class="picture-list">
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <img
                :alt="picture.name"
                :src="picture.thumbnailUrl ?? picture.url"
                style="height: 180px; object-fit: cover"
                loading="lazy"
                decoding="async"
              />
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
            <template v-if="showOp" #actions>
              <ShareAltOutlined @click="(e) => doShare(picture, e)" />
              <SearchOutlined @click="(e) => doSearch(picture, e)" />
              <EditOutlined v-if="canEdit" @click="(e) => doEdit(picture, e)" />
              <DeleteOutlined v-if="canDelete" @click="(e) => doDelete(picture, e)" />
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <!-- 滚动加载哨兵：进入视口时触发 loadMore -->
    <div ref="sentinelRef" class="load-more-sentinel">
      <a-spin v-if="loading" size="small" />
      <span v-else-if="finished" class="no-more-text">没有更多了</span>
    </div>
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  DeleteOutlined,
  EditOutlined,
  SearchOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import ShareModal from '@/components/ShareModal.vue'
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'

interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  canEdit?: boolean
  canDelete?: boolean
  /** 是否已加载全部数据 */
  finished?: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
  finished: false,
})

const emit = defineEmits<{
  (e: 'loadMore'): void
}>()

// ----- 滚动加载（IntersectionObserver） -----
const sentinelRef = ref<HTMLDivElement>()
let observer: IntersectionObserver | null = null
// 上次触发时间，防止加载失败时短时间内无限重试
let lastLoadTime = 0

const tryLoadMore = () => {
  if (props.loading || props.finished) return
  const now = Date.now()
  if (now - lastLoadTime < 1000) return
  lastLoadTime = now
  emit('loadMore')
}

// 判断哨兵当前是否在视口内（含预加载余量）
const isSentinelVisible = () => {
  const el = sentinelRef.value
  if (!el) return false
  const rect = el.getBoundingClientRect()
  return rect.top < window.innerHeight + 200
}

onMounted(() => {
  observer = new IntersectionObserver(
    (entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        tryLoadMore()
      }
    },
    { rootMargin: '200px 0px' },
  )
  if (sentinelRef.value) {
    observer.observe(sentinelRef.value)
  }
})

onUnmounted(() => {
  observer?.disconnect()
  observer = null
})

// 数据加载结束后，若哨兵仍在视口内（如新图片懒加载高度未撑开），自动续载下一页；
// 绕过节流（节流仅用于 IntersectionObserver 高频回调），但限制失败重试次数防死循环
let lastListLength = 0
let autoRetryCount = 0
watch(
  () => [props.dataList.length, props.loading],
  () => {
    nextTick(() => {
      if (props.loading || props.finished) return
      // 数据长度未变化视为本次加载失败，最多连续重试 3 次后停止
      if (props.dataList.length === lastListLength) {
        autoRetryCount++
        if (autoRetryCount >= 3) return
      } else {
        autoRetryCount = 0
      }
      lastListLength = props.dataList.length
      if (isSentinelVisible()) {
        emit('loadMore')
      }
    })
  },
)

const router = useRouter()
// 跳转至图片详情页
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

// 搜索（跳转站内以图搜图结果页）
const doSearch = (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  // 打开新的页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}

// 编辑
const doEdit = (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  // 跳转时一定要携带 spaceId
  router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

// 删除数据
const doDelete = async (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    props.onReload?.()
  } else {
    message.error('删除失败')
  }
}

// ----- 分享操作 ----
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>()
// 分享
const doShare = (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>

<style scoped>
/* 图片卡片：圆角 + 柔和阴影 + 悬浮上浮 */
.picture-list :deep(.ant-card) {
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #eceff7;
  box-shadow: 0 2px 10px rgba(37, 55, 120, 0.05);
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.picture-list :deep(.ant-card:hover) {
  transform: translateY(-4px);
  border-color: rgba(61, 90, 245, 0.35);
  box-shadow: 0 14px 30px rgba(37, 55, 120, 0.14);
}

.picture-list :deep(.ant-card .ant-card-body) {
  padding: 12px 14px;
}

.load-more-sentinel {
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 0;
}

.no-more-text {
  color: rgba(35, 44, 86, 0.45);
  font-size: 13px;
}
</style>
