<template>
  <div id="userRegister">
    <div class="title-bar">
      <h2>小黑子的云图库</h2>
      <p>欢迎登录</p>
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

      <div class="toLogin">
        没有账号？
        <RouterLink to="/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" class="button">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import type { AxiosResponse } from 'axios'

const loginUserStore = useLoginUserStore()

// 表单信息
const loginUserForm = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
/**
 * 提交表单
 * @param userLoginRequest
 */
const handlerSubmit = async (userLoginRequest: API.UserLoginRequest) => {
  const res: AxiosResponse<API.ResponseLoginUserVo_> = await userLoginUsingPost(userLoginRequest)
  if (res.data.code === 0 && res.data.data) {
    // 更新状态
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败 : ' + res.data.msg)
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
