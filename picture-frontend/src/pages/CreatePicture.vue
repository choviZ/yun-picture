<template>
  <div id="create-picture">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="fill" tab="本地图片上传">
        <!-- 上传图片 -->
        <UploadPicture :picture="picture" :onSuccess="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="url上传" force-render>
        <UrlPictureUpload :picture="picture" :onSuccess="onSuccess"/>
      </a-tab-pane>
    </a-tabs>
    <!-- 编辑图片信息 -->
    <a-form layout="vertical" :model="pictureInfo" @finish="handlerSubmit" v-if="picture">
      <a-form-item label="图片名称" name="name">
        <a-input v-model:value="pictureInfo.name" placeholder="图片名称" />
      </a-form-item>
      <a-form-item label="简介" name="introduction">
        <a-textarea
          v-model:value="pictureInfo.introduction"
          :rows="4"
          placeholder="请输入简介"
          allowClear
        />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete
          v-model:value="pictureInfo.category"
          placeholder="请输入分类"
          :options="categoryOptions"
          allowClear
        />
      </a-form-item>

      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="pictureInfo.tags"
          mode="tags"
          placeholder="请输入标签"
          :options="tagOptions"
          allowClear
        ></a-select>
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">
          {{ route.query?.id ? '修改' : '创建' }}
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import UploadPicture from '@/components/UploadPicture.vue'
import { onMounted, reactive, ref } from 'vue'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useRoute } from 'vue-router'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'

const route = useRoute()
// 定义数据
const picture = ref<API.PictureVo>()
const onSuccess = (newPicture: API.PictureVo) => {
  picture.value = newPicture
  pictureInfo.name = newPicture.name
}
const pictureInfo = reactive<API.PictureEditRequest>({})
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

const uploadType = ref<'file' | 'url'>()

/**
 * 提交表单
 */
const handlerSubmit = async (valus: any) => {
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }
  const res = await editPictureUsingPost({
    id: pictureId,
    ...valus,
  })
  if (res.data.code === 0) {
    if (route.query?.id) {
      message.success('修改成功')
      await router.push({ path: '/' })
    } else {
      message.success('上传成功')
      await router.push(`/add_picture?id=${pictureId}`)
    }
  } else {
    message.error('上传失败')
  }
}

// 获取标签和分类选项
const getCategoryTagOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接收的格式
    tagOptions.value = (res.data.data.tagList ?? []).map((tag: string) => {
      return {
        value: tag,
        label: tag,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((category: string) => {
      return {
        value: category,
        label: category,
      }
    })
  } else {
    message.error('加载选项失败' + res.data.msg)
  }
}

/**
 * 获取老数据
 */
const getOldPic = async () => {
  const id = route.query?.id
  if (id) {
    try {
      const res = await getPictureVoByIdUsingGet({ id: id })
      if (res.data.code === 0 && res.data.data) {
        const data = res.data.data
        picture.value = data
        pictureInfo.name = data.name
        pictureInfo.introduction = data.introduction
        pictureInfo.tags = Array.isArray(data.tags) ? data.tags : []
        pictureInfo.category = data.category
      }
    } catch (error) {
      console.log(error)
    }
  }
}
/**
 * 首次加载时获取
 */
onMounted(() => {
  getOldPic()
  getCategoryTagOptions()
})

</script>

<style scoped lang="scss">
#create-picture {
  max-width: 702px;
  margin: 0 auto;

  h2 {
    text-align: center;
  }
}
</style>
