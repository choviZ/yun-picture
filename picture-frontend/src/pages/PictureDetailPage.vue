<template>
  <div id="picture-detail">
    <a-row :gutter="[16, 16]">
      <!-- 图片展示 -->
      <a-col :sm="24" :md="16" :xl="18">
        <a-card title="图片">
          <a-image :src="picture.url" style="max-height: 600px; object-fit: contain" />
        </a-card>
      </a-col>
      <!-- 图片信息 -->
      <a-col :sm="24" :md="8" :xl="6">
        <a-card title="图片信息">
          <a-descriptions :column="1">
            <a-descriptions-item label="名称">{{ picture.name ?? '无名' }}</a-descriptions-item>
            <a-descriptions-item label="简介"
              >{{ picture.introduction ?? '什么都没说~' }}
            </a-descriptions-item>
            <a-descriptions-item label="分类">{{ picture.category ?? '默认' }}</a-descriptions-item>
            <a-descriptions-item label="标签">
              <a-tag v-for="tag in picture.tags" :key="tag">
                {{ tag }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="格式">{{ picture.picFormat ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="高度">{{ picture.picHeight ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="宽度">{{ picture.picWidth ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="宽高比">{{ picture.picScale ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="大小"
              >{{ formatSize(picture.picSize) }}
            </a-descriptions-item>
          </a-descriptions>

          <a-space>
            <a-button type="primary" @click="doDownload">
              免费下载
              <template #icon>
                <DownloadOutlined />
              </template>
            </a-button>
            <a-button
              :href="`/add_picture/?id=${props.id}`"
              type="default"
              style="margin-right: 12px"
              :icon="h(EditOutlined)"
              v-if="canEdit">
              编辑
            </a-button>
            <a-popconfirm title="确定要删除吗？" @confirm="doDelete" v-if="canEdit">
              <a-button danger :icon="h(DeleteOutlined)">删除</a-button>
            </a-popconfirm>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { deletePictureUsingPost, getPictureVoByIdUsingGet } from '@/api/pictureController.ts'
import { computed, h, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { DeleteOutlined, DownloadOutlined, EditOutlined } from '@ant-design/icons-vue'
import { downloadImage, formatSize } from '@/util/index.ts'
import router from '@/router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

interface Props {
  id: number
}

const props = defineProps<Props>()
const picture = ref<API.PictureVo>({})
/**
 * 获取数据
 */
const fetchData = async () => {
  const res = await getPictureVoByIdUsingGet({ id: props.id })
  if (res.data.code === 0 && res.data.data) {
    picture.value = res.data.data
  } else {
    message.error('获取图片详情失败')
  }
}
/**
 * 首次加载时获取数据
 */
onMounted(() => {
  fetchData()
})

/**
 * 删除数据
 */
const doDelete = async () => {
  const res = await deletePictureUsingPost({ id: props.id })
  if (res.data.code === 0) {
    await router.push('/')
    message.success('删除成功')
  } else {
    message.error('删除失败 : ' + res.data.msg)
  }
}

const loginUserStore = useLoginUserStore()
/**
 * 判断用户是否有编辑权限
 */
const canEdit = computed(() => {
  const user = loginUserStore.loginUser
  if (!user) return false
  return user.userRole === 'admin' || picture.value.userId === user.id
})

/**
 * 下载图片
 */
// 处理下载
const doDownload = () => {
  downloadImage(picture.value.url)
}
</script>

<style scoped lang="scss">
#picture-detail {
  margin-bottom: 16px;

  .tag-bar {
    margin-bottom: 16px;
  }

  .search-bar {
    max-width: 480px;
    margin: 0 auto 4px;
  }
}
</style>
