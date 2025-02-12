<template>
  <div class="picture-upload">
    <a-upload
      list-type="picture-card"
      :show-upload-list="false"
      :before-upload="beforeUpload"
      :custom-request="handleChange"
    >
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
      <div v-else>
        <loading-outlined v-if="loading"></loading-outlined>
        <plus-outlined v-else></plus-outlined>
        <div class="ant-upload-text">点击或拖拽上传图片</div>
      </div>
    </a-upload>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'
import type { AxiosResponse } from 'axios'

// 加载
const loading = ref<boolean>(false)

/**
 * 父组件传递的参数
 * picture 图片信息
 * onSuccess 上传成功后更新图片信息
 */
interface Props {
  picture?: API.PictureVo
  onSuccess?: (newPicture: API.PictureVo) => void
}
const props = defineProps<Props>()
/**
 * 上传
 * @param file 文件
 */
const handleChange = async ({ file }: any) => {
  loading.value = true
  const params = props.picture ? { id: props.picture.id } : {};
  const res: AxiosResponse<API.ResponsePictureVo_> = await uploadPictureUsingPost(params, {}, file)
  if (res.data.code === 0 && res.data.data) {
    message.success('上传成功')
    // 上传成功的图片信息传递给父组件
    props.onSuccess?.(res.data.data)
    loading.value = false
  } else {
    message.error('上传失败：' + res.data.msg)
    loading.value = false
  }
}
/**
 * 上传前的校验
 * @param file
 */
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  const isJpgOrPng =
    file.type === 'image/jpeg' ||
    file.type === 'image/png' ||
    file.type === 'image/webp' ||
    file.type === 'image/jpg'
  if (!isJpgOrPng) {
    message.error('图片格式错误!')
  }
  const isLt3M = file.size / 1024 / 1024 < 3
  if (!isLt3M) {
    message.error('图片不能大于3MB!')
  }
  return isJpgOrPng && isLt3M
}
</script>

<style scoped>
.picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: 100% !important;
  min-height: 152px;
  min-width: 152px;

  img {
    max-width: 100%;
    max-height: 480px;
  }
}

.avatar-uploader > .ant-upload {
  width: 128px;
  height: 128px;
}

.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}
</style>
