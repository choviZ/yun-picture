<template>
  <div id="userRegister">
    <div class="title-bar">
      <h2>小黑子云图库</h2>
      <p>欢迎注册</p>
    </div>
    <a-form
      :model="loginUserForm"
      name="basic"
      autocomplete="off"
      @finish="handlerSubmit"
      class="form"
    >
      <a-form-item
        name="userAccount"
        :rules="[{ required: true, message: '请输入账号!', trigger: 'blur' }]"
      >
        <a-input v-model:value="loginUserForm.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item name="userPassword" :rules="[{ required: true, message: '请输入密码!' }]">
        <a-input-password v-model:value="loginUserForm.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <a-form-item name="checkPassword" :rules="[{ required: true, message: '请输入密码!' }]">
        <a-input-password
          v-model:value="loginUserForm.checkPassword"
          placeholder="请二次确认密码"
        />
      </a-form-item>

      <div class="toLogin">
        已有账号？
        <RouterLink to="/register">去登录</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" class="button">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { userRegisterUsingPost } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import type { AxiosResponse } from 'axios'

// 表单信息
const loginUserForm = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})
/**
 * 提交表单
 * @param userRegisterRequest
 */
const handlerSubmit = async (userRegisterRequest: API.UserRegisterRequest) => {
  const res: AxiosResponse<API.ResponseLong_> = await userRegisterUsingPost(userRegisterRequest)
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    router.push({
      path: '/login',
      replace: true,
    })
  } else {
    message.error('注册失败 : ' + res.data.msg)
  }
}
</script>

<style scoped lang="scss">
#userRegister {
  height: 100vh;

  .title-bar {
    text-align: center;
    margin-top: 16px;

    p {
      color: #bbb;
      margin-bottom: 32px;
    }
  }

  .toLogin {
    color: #bbb;
    text-align: right;
    margin-bottom: 14px;
  }
}

#userRegister .form {
  margin: 0 auto;
  width: 480px;
}

#userRegister .button {
  width: 100%;
}
</style>
