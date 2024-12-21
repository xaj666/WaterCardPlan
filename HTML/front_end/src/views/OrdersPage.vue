<template>
  <div class="orders-container">
    <div class="search-box">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择">
            <el-option label="全部" value=""></el-option>
            <el-option label="充值成功" value="success"></el-option>
            <el-option label="充值失败" value="failed"></el-option>
            <el-option label="处理中" value="processing"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="充值时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="orderData" border style="width: 100%">
      <el-table-column type="index" label="序号" width="80" align="center"></el-table-column>
      <el-table-column prop="order_id" label="订单号" width="180" align="center"></el-table-column>
      <el-table-column prop="username" label="用户名" width="120" align="center"></el-table-column>
      <el-table-column prop="android_id" label="设备码" min-width="180" align="center"></el-table-column>
      <el-table-column prop="amount" label="充值金额" width="150" align="center">
        <template slot-scope="scope">
          <span style="color: #f56c6c">￥{{ scope.row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="ip" label="IP地址" width="140" align="center"></el-table-column>
      <el-table-column prop="statusText" label="状态" width="120" align="center">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.statusText)">{{scope.row.statusText}}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="创建时间" width="180" align="center">
        <template slot-scope="scope">
          {{ formatDateTime(scope.row.create_time) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleDetail(scope.row)">详情</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 30, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
const backendBaseUrl = 'http://122.51.66.112:4866'

export default {
  data() {
    return {
      searchForm: {
        username: '',
        status: '',
        dateRange: []
      },
      originalData: [],
      orderData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  async created() {
    await this.loadOrders();
  },
  methods: {
    handleSearch() {
      let filteredData = [...this.originalData]
      
      // 按用户名筛选
      if (this.searchForm.username) {
        filteredData = filteredData.filter(item => 
          item.username.toLowerCase().includes(this.searchForm.username.toLowerCase())
        )
      }
      
      // 按状态筛选
      if (this.searchForm.status) {
        filteredData = filteredData.filter(item => 
          item.status === this.getStatusLabel(this.searchForm.status)
        )
      }
      
      // 按时间范围筛选
      if (this.searchForm.dateRange && this.searchForm.dateRange.length === 2) {
        const startDate = this.searchForm.dateRange[0]
        const endDate = this.searchForm.dateRange[1]
        filteredData = filteredData.filter(item => {
          const itemDate = new Date(item.createTime)
          return itemDate >= startDate && itemDate <= endDate
        })
      }
      
      this.orderData = filteredData
      this.total = filteredData.length
      this.currentPage = 1 // 重置到第一页
    },
    
    resetSearch() {
      this.searchForm = {
        username: '',
        status: '',
        dateRange: []
      }
      this.orderData = [...this.originalData]
      this.total = this.orderData.length
      this.currentPage = 1
    },

    getStatusLabel(value) {
      const statusMap = {
        'success': '充值成功',
        'failed': '充��失败',
        'processing': '处理中'
      }
      return statusMap[value] || ''
    },

    // 分页相关方法
    handleSizeChange(val) {
      this.pageSize = val
      this.handleSearch() // 重新搜索以更新数据
    },

    handleCurrentChange(val) {
      this.currentPage = val
      this.handleSearch() // 重新搜索以更新数据
    },

    getStatusType(status) {
      const statusMap = {
        '处理中': 'warning',
        '充值成功': 'success',
        '充值失败': 'danger'
      }
      return statusMap[status] || 'info'
    },

    async loadOrders() {
      try {
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize,
          username: this.searchForm.username,
          status: this.searchForm.status,
          startDate: this.searchForm.dateRange?.[0],
          endDate: this.searchForm.dateRange?.[1]
        };
        
        const response = await axios.get(backendBaseUrl + '/api/orders', { params });
        this.orderData = response.data.data;
        this.total = response.data.total;
      } catch (error) {
        console.error('加载订单失败:', error);
        this.$message.error('加载订单失败');
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确认删除该订单?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        await axios.delete(backendBaseUrl + `/api/orders/${row.order_id}`);
        this.$message.success('删除成功');
        await this.loadOrders();
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除订单失败:', error);
          this.$message.error('删除订单失败');
        }
      }
    },
    async handleDetail(row) {
      try {
        const response = await axios.get(backendBaseUrl + `/api/orders/${row.order_id}`);
        // 这里可以显示详情弹窗
        console.log('订单详情:', response.data);
      } catch (error) {
        console.error('获取订单详情失败:', error);
        this.$message.error('获取订单详情失败');
      }
    },
    // 添加日期格式化方法
    formatDateTime(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }
  }
}
</script>

<style scoped>
.orders-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}
.search-box {
  margin-bottom: 20px;
  background-color: #f8f8f8;
  padding: 20px;
  border-radius: 4px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 