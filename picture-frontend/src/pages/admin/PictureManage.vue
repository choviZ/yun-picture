<template>
  <div id="user-manage">
    <a-space>
      <a-button type="primary" href="/add_picture" target="_blank">+创建图片</a-button>
      <a-button type="primary" href="/add_picture/batch" target="_blank">+批量创建</a-button>
    </a-space>
    <div class="search-form">
      <a-form layout="inline" :model="searchParams">
        <a-form-item name="searchText" label="关键词">
          <a-input v-model:value="searchParams.searchText" placeholder="从名称和简介搜索" />
        </a-form-item>
        <a-form-item name="category" label="分类">
          <a-input v-model:value="searchParams.category" placeholder="类型" />
        </a-form-item>
        <a-form-item name="tags" label="标签">
          <a-input v-model:value="searchParams.tags" placeholder="标签" />
        </a-form-item>
        <a-form-item name="reviewStatus" label="审核状态">
          <a-select
            v-model:value="searchParams.reviewStatus"
            placeholder="请输入标签"
            :options="PIC_REVIEW_STATUS_OPTIONS"
            allowClear
            style="min-width: 100%"
          ></a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" @click="fetchData()"> 搜索</a-button>
        </a-form-item>
      </a-form>
    </div>
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'url'">
          <a-image :src="record.url" :width="120" />
        </template>
        <template v-if="column.dataIndex === 'tags'">
          <a-space wrap>
            <a-tag v-for="tag in JSON.parse(record.tags || '[]')" :key="tag">{{ tag }}</a-tag>
          </a-space>
        </template>
        <!-- 图片信息 -->
        <template v-if="column.dataIndex === 'picInfo'">
          <div>格式:{{ record.picFormat }}</div>
          <div>高度:{{ record.picHeight }}</div>
          <div>宽度:{{ record.picWidth }}</div>
          <div>宽高比:{{ record.picScale }}</div>
          <div>大小:{{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <!-- 审核信息 -->
        <template v-if="column.dataIndex === 'reviewMessage'">
          <div>状态: {{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}</div>
          <div>信息: {{ record.reviewMessage }}</div>
          <div>审核人: {{ record.reviewerId }}</div>
          <div v-if="record.reviewTime !== null">
            时间:
            {{ dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm:ss') }}
          </div>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button
              type="link"
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.PASS"
              @click="doReview(record, PIC_REVIEW_STATUS_ENUM.PASS)"
            >
              通过
            </a-button>
            <a-button
              type="link"
              danger
              v-if="record.reviewStatus !== PIC_REVIEW_STATUS_ENUM.REJECT"
              @click="doReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
            >
              拒绝
            </a-button>
            <a-button
              type="link"
              :href="`/add_picture?id=${record.id}`"
              target="_blank"
              style="margin-right: 14px"
              >编辑
            </a-button>
            <a-popconfirm title="确定要删除吗？" @confirm="doDelete(record.id)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import {
  deletePictureUsingPost,
  doPictureReviewUsingPost,
  getPictureListUsingPost,
} from '@/api/pictureController.ts'
import {
  PIC_REVIEW_STATUS_ENUM,
  PIC_REVIEW_STATUS_MAP,
  PIC_REVIEW_STATUS_OPTIONS,
} from '@/contstants/picture.ts'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'url',
  },
  {
    title: '名称',
    dataIndex: 'name',
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'category',
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '审核信息',
    dataIndex: 'reviewMessage',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 展示的数据
const dataList = ref([])
const total = ref<number>()

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 获取数据
const fetchData = async () => {
  const res = await getPictureListUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data?.records ?? []
    total.value = Number(res.data.data.total) ?? 0
  }
}

// 页面加载时请求一次
onMounted(async () => fetchData())

// 表格变化处理
interface PaginationChangeParams {
  current: number
  pageSize: number
}

const doTableChange = (page: PaginationChangeParams) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 删除数据
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id: id })
  if (res.data.code === 0) {
    await fetchData()
    message.success('删除成功')
  } else {
    message.error('删除失败 : ' + res.data.msg)
  }
}

/**
 * 审核
 */
const doReview = async (record: API.Picture, reviewStatus: number) => {
  const reviewMessage = '管理员审核通过'
  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus: reviewStatus,
    reviewMessage: reviewMessage,
  })
  if (res.data.code === 0) {
    message.success('审核处理成功')
    // 更新数据
    await fetchData()
  } else {
    message.error('审核处理失败')
  }
}
</script>

<style scoped>
#user-manage {
  background: linear-gradient(to right, #fefefe, #fff);

  .search-form {
    float: right;
    margin-bottom: 16px;
  }
}
</style>
