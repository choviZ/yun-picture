<template>
  <div id="home-page">
    <!-- 搜索框 -->
    <div class="search-bar">
      <a-input-search
        placeholder="从海量图片中搜索"
        v-model:value="searchParams.searchText"
        enter-button="搜索"
        size="large"
        @search="doSearch"
      />
    </div>
    <!-- 分类及标签筛选 -->
    <a-tabs v-model:activeKey="selectedCategory" @change="doSearch">
      <a-tab-pane key="all" tab="全部分类" />
      <a-tab-pane :tab="category" v-for="category in categoryList" :key="category" />
    </a-tabs>
    <div class="tag-bar">
      <span style="margin-right: 8px">标签：</span>
      <a-space :size="[0, 8]" wrap>
        <a-checkable-tag
          v-for="(tag, index) in tagList"
          :key="tag"
          v-model:checked="selectedTag[index]"
          @change="doSearch"
        >
          {{ tag }}
        </a-checkable-tag>
      </a-space>
    </div>

    <!-- 图片展示 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
      :pagination="pagination"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <router-link target="_blank" :to="`/picture/${picture.id}`">
            <a-card hoverable style="width: 240px">
              <template #cover>
                <img
                  :alt="picture.name"
                  :src="picture.thumbnailUrl ?? picture.url"
                  style="height: 180px; object-fit: cover"
                />
              </template>
              <a-card-meta :title="picture.name">
                <template #description>
                  <a-flex>
                    <a-tag color="green">{{ picture.category ?? '默认' }}</a-tag>
                    <a-tag v-for="tag in picture.tags" :key="tag">
                      {{ tag }}
                    </a-tag>
                  </a-flex>
                </template>
              </a-card-meta>
            </a-card>
          </router-link>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>
<script lang="ts" setup>
import {
  getPictureVoListUsingPost,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'

// 定义数据
const dataList = ref<API.PictureVo[]>([])
const total = ref(0)
const loading = ref(true)

/**
 * 搜索条件
 */
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'editTime',
  sortOrder: 'descend',
})
/**
 * 分页参数
 */
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 12,
    total: total.value,
    // 切换页号
    onChange: (page: number, pageSize: number) => {
      searchParams.current = page
      searchParams.pageSize = pageSize
      fetchData()
    },
  }
})
/**
 * 获取数据
 */
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params: API.PictureQueryRequest = {
    ...searchParams,
    tags: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  // [true, false, false] => ['java']
  selectedTag.value.forEach((useTag, index) => {
    if (useTag) {
      params.tags?.push(tagList.value[index])
    }
  })
  const res = await getPictureVoListUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data?.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}
/**
 * 首次加载时获取数据
 */
onMounted(() => {
  fetchData()
  getCategoryTagOptions()
})
/**
 * 搜索
 */
const doSearch = () => {
  // 重置搜索条件
  searchParams.current = 1
  fetchData()
}

// 标签和分类数据
const categoryList = ref<string[]>([])
const tagList = ref<string[]>([])
const selectedTag = ref<boolean[]>([])
const selectedCategory = ref<string>('all')

/**
 * 获取标签和分类选项
 */
const getCategoryTagOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接收的格式
    tagList.value = res.data.data.tagList ?? []
    categoryList.value = res.data.data.categoryList ?? []
  } else {
    message.error('加载选项失败' + res.data.msg)
  }
}
</script>

<style scoped lang="scss">
#home-page {
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
