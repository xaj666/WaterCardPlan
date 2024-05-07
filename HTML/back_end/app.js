const express = require('express'); // 引入 express 模块
const cors = require('cors'); // 引入 cors 模块
const db = require('./db'); // 引入数据库模块


const app = express(); // 创建 express 应用
app.use(cors()); // 使用 cors 中间件

app.get('/API/q', (req, res) => {
    //在这里处理你的逻辑，例如从数据库获取数据
    const data = { message: 'Hello from the backend!' };
    // 发送响应给前端
    res.json(data);
  });

// 查询数据库以获取最新的数据
app.get('/API/get', function(req, res) {
    let sql = 'SELECT * FROM user';
    // 执行查询语句
    db.query(sql, function(err, result) {
        if (err) {
            console.error('查询数据失败：', err);
            res.status(500).json({ error: '查询数据失败' });
        }
        // 将查询到的数据发送回前端
        res.json(result);
    });
});

// 使 express 监听 8080 端口号发起的 http 请求
const server = app.listen(8081, function() {
    console.log("服务器已启动，监听8081端口");
});
