<template>
  <div id="create-picture">
    <h2 style="margin-bottom: 16px">批量创建</h2>
    <!-- 编辑图片信息 -->
    <a-form layout="vertical" :model="uploadData" @finish="handlerSubmit">
      <a-form-item label="关键词" name="searchText">
        <a-input v-model:value="uploadData.searchText" placeholder="请输入关键词" />
      </a-form-item>
      <a-form-item label="数量" name="count">
        <a-input-number v-model:value="uploadData.count" />
      </a-form-item>
      <a-form-item label="名称前缀" name="prefixName">
        <a-input v-model:value="uploadData.prefixName" placeholder="请输入名称前缀，会自动补充序号" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading"> 创建 </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { uploadPictureByBatchUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

// 定义数据
const uploadData = reactive<API.PictureUploadByBatchRequest>({count:10})
const loading = ref(false)
/**
 * 提交表单
 */
const handlerSubmit = async () => {
  loading.value = true
  const res = await uploadPictureByBatchUsingPost({
    ...uploadData,
  })
  if (res.data.code === 0) {
    message.success(`创建成功共${res.data.data}条`)
  } else {
    message.error('上传失败')
  }
  loading.value = false
}
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
