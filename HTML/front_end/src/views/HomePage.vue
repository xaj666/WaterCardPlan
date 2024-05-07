<!-- Home.vue -->
<template>
  <div id="app">
    <!--网页主体-->
    <el-col>
      <div>
        <button @click="q">Fetch Data</button>
      </div>
      <h1>WaterCardPlan后台管理系统</h1>
      <el-row type="flex">
        <el-button type="primary" icon="el-icon-user" @click="UserLoginDialog">添加</el-button>
        <el-button type="primary" icon="el-icon-loading" @click="refreshData">刷新</el-button>
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
    <el-dialog title="添加用户" :visible.sync="AddUseDialogVisible" width="30%">
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" label-width="80px">
        <el-form-item label="用户名" prop="User">
          <el-input v-model="loginForm.User"></el-input>
        </el-form-item>
        <el-form-item label="Key" prop="Key">
          <el-input v-model="loginForm.Key"></el-input>
        </el-form-item>
        <el-form-item label="设备码" prop="IMEI">
          <el-input v-model="loginForm.IMEI"></el-input>
        </el-form-item>
        <el-form-item label="积分" prop="Points">
          <el-input v-model="loginForm.Points"></el-input>
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
import axios from 'axios';// 引入axios
const backendBaseUrl = 'http://localhost:8081'// 设置后端API的基本URL
export default {
  created() {
    this.refreshData();
  },
  methods: {
    //示例请求
    q() {
      axios.get(backendBaseUrl + '/API/q')
        .then(response => {
          console.log(response.data);
        })
        .catch(error => {
          console.error('请求失败：', error);
        });
    },
    // 显示添加用户对话框
    UserLoginDialog() {
      this.AddUseDialogVisible = true;
    },
    //添加用户逻辑
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
    //刷新
    refreshData() {
      axios.get(backendBaseUrl + '/API/get')
        .then(response => {
          //编号
          response.data.forEach((user, index) => {
            user.ID = index + 1;
          });
          console.log(response.data);
          this.tableData = response.data;
        })
        .catch(error => {
          console.error('请求失败：', error);
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
  // 初始化数据
  data() {
    return {
      AddUseDialogVisible: false,// 添加用户对话框不可见
      loginForm: {
        user: '',
        Key: '',
        IMEI: '',
        points: ''
      },
      // 表单验证规则
      loginRules: {
        User: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        Key: [
          { required: true, message: '请输入密钥', trigger: 'blur' }
        ],
        IMEI: [
          { required: true, message: '请输入设备码', trigger: 'blur' }
        ],
        Points: [
          { required: true, message: '请输入积分', trigger: 'blur' }
        ],
      },
      // 表格数据
      tableData: [{
      },
      ]
    }
  }
}
</script>

<!--CSS样式-->
<style>
h1 {
  text-align: center;
}

#app {
  width: 85%;
  margin: 0 auto;
}
</style>