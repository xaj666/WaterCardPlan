<!-- Home.vue -->
<template>
  <div id="app">
    <!--网页主体-->
    <el-col>
      <h1>WaterCardPlan后台管理系统</h1>
      <el-row type="flex">
        <el-button type="primary" icon="el-icon-user" @click="addUser">添加</el-button>
        <el-button type="primary" icon="el-icon-loading" @click="refreshData">刷新</el-button>
      </el-row>
      <div style="height: 10px;"></div>
      <el-table :data="tableData" border width="100%">
        <el-table-column fixed prop="number" label="序号" width="50" align="center">
        </el-table-column>
        <el-table-column prop="username" label="用户" width="150" align="center">
        </el-table-column>
        <el-table-column prop="secret_key" label="Key序列号" width="150" align="center">
        </el-table-column>
        <el-table-column prop="android_id" label="设备码" width="150" align="center">
        </el-table-column>
        <el-table-column prop="score" label="积分" width="100" align="center">
        </el-table-column>
        <el-table-column prop="ip" label="登录IP" width="150" align="center">
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150" align="center">
          <template slot-scope="scope">
            <!--修改按钮-->
            <el-button type="primary" icon="el-icon-edit" circle @click="updateUser(scope.row)"></el-button>
            <!--删除按钮-->
            <el-button type="danger" icon="el-icon-delete" circle @click="deleteUser(scope.row)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-col>
    <!-- 用户对话框 -->
    <el-dialog :title="userTitle" :visible.sync="AddUseDialogVisible" width="30%">
      <el-form ref="userForm" :model="userForm" :rules="loginRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username"></el-input>
        </el-form-item>
        <el-form-item label="Key" prop="secret_key">
          <el-input v-model="userForm.secret_key"></el-input>
        </el-form-item>
        <el-form-item label="设备码" prop="android_id">
          <el-input v-model="userForm.android_id"></el-input>
        </el-form-item>
        <el-form-item label="积分" prop="score">
          <el-input v-model="userForm.score"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="AddUseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';// 引入axios
const backendBaseUrl = 'http://localhost:8081'// 设置后端API的基本URL
export default {
  //网页加载
  created() {
    //初始刷新
    this.refreshData();
  },
  // 初始化数据
  data() {
    return {
      // 数据库字段
      // 用户名：username
      // 密钥：secret_key
      // 安卓ID：android_id
      // 积分：score
      // IP：ip
      id: "",
      tableData: [],//表格数据初始化
      AddUseDialogVisible: false,// 添加用户对话框不可见
      userTitle: '',//用户表单初始标题
      // 表单验证规则
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        secret_key: [
          { required: true, message: '请输入密钥', trigger: 'blur' }
        ],
        android_id: [
          { required: true, message: '请输入设备码', trigger: 'blur' }
        ],
        score: [
          { required: true, message: '请输入积分', trigger: 'blur' }
        ],
      },
      // 用户表单数据
      userForm: {},
      newUser: {},//定义封装体
      addOrupdate: true,
    }
  },

  methods: {
    //表单获取数据
    getUserInformation(row) {
      //console.log(row);
      this.userForm = {
        username: row.username,
        secret_key: row.secret_key,
        android_id: row.android_id,
        score: row.score
      }
    },
    // 添加/修改用户逻辑
    submit() {
      this.$refs.userForm.validate(valid => {
        // 表单校验通过
        if (valid) {
          // 创建新用户对象
          this.newUser = {
            username: this.userForm.username,
            secret_key: this.userForm.secret_key,
            android_id: this.userForm.android_id,
            score: this.userForm.score,
          };
          if (this.addOrupdate) {// 发送添加用户请求POST请求到后端API
            this.addUserPost();
          } else {// 发送修改用户请求PUT请求到后端API
            this.updateUserPut();
          }
          this.AddUseDialogVisible = false;// 关闭对话框等其他操作
        } else {
          return false;
        }
      });
    },
    //添加用户请求
    addUserPost() {
      axios.post(backendBaseUrl + '/api/addUser', this.newUser)
        .then(response => {
          console.log(response.data);// 打印响应数据
          this.refreshData();// 添加成功后刷新数据
        })
        .catch(error => {
          console.error('Error adding user:', error);
          // 在这里添加适当的错误处理逻辑，比如显示错误提示
        });
    },
    //修改用户请求
    updateUserPut() {
      this.newUser = {
        id: this.id,
        username: this.userForm.username,
        secret_key: this.userForm.secret_key,
        android_id: this.userForm.android_id,
        score: this.userForm.score,
      };
      console.log(this.newUser); // 检查一下表单数据是否正确
      // 发送修改用户请求PUT请求到后端API
      axios.put(backendBaseUrl + '/api/updateUser', this.newUser)
        .then(response => {
          this.$message({
              type: 'success',
              message: response.data.message // 从后端返回的消息
            });
          console.log(response.data); // 打印响应数据
          this.refreshData(); // 添加成功后刷新数据
        })
        .catch(error => {
          console.error('Error updating user:', error);
          // 在这里添加适当的错误处理逻辑，比如显示错误提示
        });
    },
    //添加用户按钮
    addUser() {
      this.addOrupdate = true;
      this.userForm = {}; // 重置表单数据
      this.userTitle = "添加用户"
      axios.get(backendBaseUrl + '/API/Key')
        .then(response => {// 请求成功，response是响应对象
          this.userForm={
            secret_key:response.data}
        })
        .catch(error => {// 请求失败，error是错误对象
          console.error('请求失败：', error);
          this.$message({
              type: 'error',
              message: '删除用户失败，请稍后重试'
            });
        });
        this.AddUseDialogVisible = true;//显示对话框
    },
    //刷新按钮
    refreshData() {
      axios.get(backendBaseUrl + '/API/Refresh')
        .then(response => {// 请求成功，response是响应对象
          //编号
          response.data.forEach((user, index) => {
            user.number = index + 1;
          });
          console.log(response.data);
          this.tableData = response.data;
        })
        .catch(error => {// 请求失败，error是错误对象
          console.error('请求失败：', error);
        });
    },
    //删除用户按钮
    deleteUser(row) {
      console.log(row.id);
      // 弹出确认对话框
      this.$confirm('此操作将永久删除该用户, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 用户点击了确定按钮
        // 发送删除请求到后端
        axios.delete(backendBaseUrl + `/api/deleteUser/${row.id}`)
          .then(response => {
            // 删除成功，显示成功消息
            this.$message({
              type: 'success',
              message: response.data.message // 从后端返回的消息
            });
            // 刷新页面数据
            this.refreshData();
          })
          .catch(error => {
            // 删除失败，显示错误消息
            console.error('删除用户失败:', error);
            this.$message({
              type: 'error',
              message: '删除用户失败，请稍后重试'
            });
          });
      }).catch(() => {
        // 用户点击了取消按钮
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    //修改用户按钮
    updateUser(row) {
      this.addOrupdate = false;
      this.getUserInformation(row);
      this.userTitle = "修改用户信息"
      this.id = row.id;
      this.AddUseDialogVisible = true;//显示对话框
    }
  },
}
</script>

<!--CSS样式-->
<style>
h1 {
  text-align: center;
}

#app {
  width: 78%;
  margin: 0 auto;
}
</style>