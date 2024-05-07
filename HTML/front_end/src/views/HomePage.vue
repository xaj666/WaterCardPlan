<!-- Home.vue -->
<template>
  <div id="app">
    <!--网页主体-->
    <el-col>
      <h1>WaterCardPlan后台管理系统</h1>
      <el-row type="flex">
        <el-button type="primary" icon="el-icon-user" @click="UserLoginDialog">添加</el-button>
        <el-button type="primary" icon="el-icon-loading">刷新</el-button>
      </el-row>
      <div style="height: 10px;"></div>
      <el-table :data="tableData" border width="100%">
        <el-table-column fixed prop="ID" label="序号" width="50" align="center">
        </el-table-column>
        <el-table-column prop="User" label="用户" width="150" align="center">
        </el-table-column>
        <el-table-column prop="Key" label="Key序列号" width="150" align="center">
        </el-table-column>
        <el-table-column prop="IMEI" label="设备码" width="150" align="center">
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" align="center">
        </el-table-column>
        <el-table-column prop="IP" label="登录IP" width="150" align="center">
        </el-table-column>
        <el-table-column prop="Location" label="登录定位" width="150" align="center">
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150" align="center">
          <template>
            <!--修改按钮-->
            <el-button type="primary" icon="el-icon-edit" circle></el-button>
            <!--删除按钮-->
            <el-button type="danger" icon="el-icon-delete" circle @click="deleteDialog"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-col>
    <!-- 添加用户对话框 -->
    <el-dialog title="登录" :visible.sync="AddUseDialogVisible" width="30%" :before-close="handleClose">
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" label-width="80px">
        <el-form-item label="用户名" prop="user">
          <el-input v-model="loginForm.user"></el-input>
        </el-form-item>
        <el-form-item label="Key" prop="Key">
          <el-input v-model="loginForm.Key"></el-input>
        </el-form-item>
        <el-form-item label="设备码" prop="IMEI">
          <el-input v-model="loginForm.IMEI"></el-input>
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input v-model="loginForm.points"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="AddUseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleLogin">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  // Component definition
  methods: {
    //添加用户
    // 显示添加用户对话框
    UserLoginDialog() {
      this.AddUseDialogVisible = true;
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          // 在这里处理添加用户逻辑
          const newUser = {
            ID: this.tableData.length + 1, // 自增ID
            User: this.loginForm.user,
            Key: this.loginForm.Key,
            IMEI: this.loginForm.IMEI,
            points: this.loginForm.points,
          };
          this.tableData.push(newUser);
          // 关闭对话框
          this.AddUseDialogVisible = false;
        } else {
          return false;
        }
      });
    },
    //删除用户
    deleteDialog() {
      this.$confirm('此操作将永久删除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message({
          type: 'success',
          message: '删除成功!'
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    }
  },
  data() {
    return {
      AddUseDialogVisible: false,// 登录对话框是否可见
      loginForm: {
        user: '',
        Key: '',
        IMEI: '',
        points: ''
      },
      // 表单验证规则
      loginRules: {
        user: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        Key: [
          { required: true, message: '请输入密钥', trigger: 'blur' }
        ],
        IMEI: [
          { required: true, message: '请输入设备码', trigger: 'blur' }
        ],
        points: [
          { required: true, message: '请输入积分', trigger: 'blur' }
        ],
      },
      // 表格数据
      tableData: [{
        ID: '1',
        User: 'qweqweqwe',
        Key: '12312312312',
        IMEI: 'e9f85df86b5c9517',
        points: 100,
        IP: '192.168.233.233',
        Location: '上海',
      },
      ]
    }
  }
}
</script>
<style>
h1 {
  text-align: center;
}

#app {
  width: 85%;
  margin: 0 auto;
}
</style>