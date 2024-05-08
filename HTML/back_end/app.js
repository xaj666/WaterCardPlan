const express = require('express'); // 引入 express 模块
const cors = require('cors'); // 引入 cors 模块
const db = require('./db'); // 引入数据库模块


const app = express(); // 创建 express 应用
app.use(cors()); // 使用 cors 中间件
app.use(express.json());// 解析 application/json 请求体


// 查询数据库以获取最新的数据
app.get('/API/refresh', function (req, res) {
    let sql = 'SELECT * FROM User';
    // 执行查询语句
    db.query(sql, function (err, result) {
        if (err) {
            console.error('查询数据失败：', err);
            res.status(500).json({ error: '查询数据失败' });
        }
        // 将查询到的数据发送回前端
        console.log('查询到的数据：', result);
        res.json(result);
    });
});
// 添加用户
app.post('/api/addUser', (req, res) => {
    // 从请求体中获取用户数据
    const newUser = req.body;
    console.log('收到的新用户数据：', newUser);
    // 将新用户数据插入数据库
    const sql = 'INSERT INTO User SET ?';
    db.query(sql, newUser, (err, result) => {
        if (err) {
            console.error('添加用户失败：', err);
            return res.status(500).json({ error: '添加用户失败' });
        }
        console.log('添加用户成功：', result);
        res.json(result);
    });
});
// 删除用户
app.delete('/api/deleteUser/:username', (req, res) => {
    const username = req.params.username; // 获取要删除的用户名
    const sql = `DELETE FROM User WHERE username = ?`; // SQL 查询语句
    db.query(sql, [username], (err, result) => {
        if (err) {
            console.error('删除用户失败:', err);
            res.status(500).json({ message: '删除用户失败' });
            return;
        }
        if (result.affectedRows > 0) {
            res.status(200).json({ message: '用户删除成功' });
        } else {
            res.status(404).json({ message: '未找到要删除的用户' });
        }
    });
});

// 使 express 监听 8080 端口号发起的 http 请求
const server = app.listen(8081, function () {
    console.log("服务器已启动，监听8081端口");
});
