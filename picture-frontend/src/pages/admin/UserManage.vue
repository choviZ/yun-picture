<template>
  <div id="user-manage">
    <div class="search-form">
      <a-form layout="inline" :model="searchParams">
        <a-form-item>
          <a-input v-model:value="searchParams.userAccount" placeholder="账号" />
        </a-form-item>
        <a-form-item>
          <a-input v-model:value="searchParams.userName" placeholder="用户名" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" @click="fetchData()"> 搜索</a-button>
        </a-form-item>
      </a-form>
    </div>
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="60" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole == 'admin'">
            <a-tag color="blue">{{ record.userRole }}</a-tag>
          </div>
          <div v-else>
            <a-tag color="green">{{ record.userRole }}</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button type="primary" style="margin-right: 14px">编辑</a-button>
          <a-popconfirm title="确定要删除吗？" @confirm="doDelete(record.id)">
            <a-button type="primary" danger>删除</a-button>
          </a-popconfirm>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { getUserListUsingPost, userDeleteUsingPost } from '@/api/userController.ts'
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '昵称',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '角色',
    dataIndex: 'userRole',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
// 展示的数据
const data = ref<API.LoginUserVo[]>()
const total = ref<number>()

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  const res = await getUserListUsingPost(searchParams)
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data?.records
    total.value = Number(res.data.data.total)
  }
}

// 页面加载时获取数据
onMounted(async () => fetchData())

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

// 表格变化处理
interface PaginationChangeParams{
  current: number,
  pageSize: number
}
const doTableChange = (page:PaginationChangeParams) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 删除数据
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await userDeleteUsingPost({ id: id })
  if (res.data.code === 0) {
    await fetchData()
    message.success('删除成功')
  } else {
    message.error('删除失败 : ' + res.data.msg)
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
