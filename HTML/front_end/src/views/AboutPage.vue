<!-- About.vue -->
<template>
  <div>
    <h2>About</h2>
    <p>This is the about page.</p>
    <el-button type="primary" @click="showLoginDialog">登录</el-button>


    <!-- 登录对话框 -->
    <el-dialog title="登录" :visible.sync="loginDialogVisible" width="30%" :before-close="handleClose">
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="loginForm.password"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="loginDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleLogin">确定</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
export default {
  data() {
    return {
      loginDialogVisible: false,
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      }
    };
  },
  methods: {
    showLoginDialog() {
      this.loginDialogVisible = true;
    },

    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          // 在这里处理登录逻辑
          console.log('用户名:', this.loginForm.username);
          console.log('密码:', this.loginForm.password);
          // 关闭对话框
          this.loginDialogVisible = false;
        } else {
          return false;
        }
      });
    }
  }
};
</script>