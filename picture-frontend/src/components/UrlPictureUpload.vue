<template>
  <div class="url-picture-upload">
    <a-input-search
      v-model:value="url"
      placeholder="请输入url"
      enter-button="上传"
      @search="handlerUpload"
      :loading="loading"
    />
    <div class="img-wrapper">
      <a-image v-if="picture?.url" :src="picture?.url"></a-image>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { uploadPictureByUrlUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

const url = ref<string>()
// 加载
const loading = ref<boolean>(false)

interface Props {
  picture?: API.PictureVo
  onSuccess?: (newPicture: API.PictureVo) => void
}

const props = defineProps<Props>()
/**
 * 根据url上传
 */
const handlerUpload = async () => {
  loading.value = true
  const params: API.PictureUploadRequest = { fileUrl: url.value }
  if (props.picture) {
    params.id = props.picture.id
  }
  const res = await uploadPictureByUrlUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    // 上传成功的图片信息传递给父组件
    props.onSuccess?.(res.data.data)
    loading.value = false
  } else {
    message.error('上传失败：' + res.data.msg)
    loading.value = false
  }
}
</script>
<style scoped lang="scss">
.url-picture-upload {
  margin-bottom: 16px;
  img{
    max-width: 100%;
    max-height: 480px;
  }
  .img-wrapper{
    text-align: center;
    margin-top: 16px;

  }
}
</style>
