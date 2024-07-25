const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const mysql = require('mysql');
const crypto = require('crypto');

const app = express();
const port = process.env.PORT || 4867;
const secretKey = crypto.randomBytes(32).toString('hex');

const pool = mysql.createPool({
    connectionLimit: 10,
    host: 'localhost',
    user: 'WaterCardPlan',
    password: '497545',
    database: 'watercardplan'
});

app.use(bodyParser.json());
app.use(cors());

function generateKey() {
    const randomBytes = crypto.randomBytes(24);
    const key = randomBytes.toString('base64').replace(/[\/\n]/g, '');
    return key.replace(/[^a-zA-Z0-9]/g, '').substring(0, 24);
}

function checkKey(key) {
    return new Promise((resolve, reject) => {
        const sql = 'SELECT * FROM User WHERE secret_key = ?';
        pool.query(sql, key, (err, result) => {
            if (err) {
                console.error('查询数据失败：', err);
                return resolve(true);
            }
            if (result.length > 0) {
                console.log('Key已存在：', key);
                return resolve(true);
            }
            console.log('Key可用：', key);
            resolve(false);
        });
    });
}

async function addKey() {
    let key = generateKey();
    while (await checkKey(key)) {
        key = generateKey();
    }
    return key;
}

app.get('/API/key', async (req, res) => {
    const key = await addKey();
    res.json({ key });
});

app.get('/API/refresh', (req, res) => {
    const sql = 'SELECT * FROM User';
    pool.query(sql, (err, result) => {
        if (err) {
            console.error('查询数据失败：', err);
            return res.status(500).json({ error: '查询数据失败' });
        }
        console.log('查询到的数据：', result);
        res.json(result);
    });
});

app.post('/api/addUser', (req, res) => {
    const newUser = req.body;
    console.log('收到的新用户数据：', newUser);
    const sql = 'INSERT INTO User SET ?';
    pool.query(sql, newUser, (err, result) => {
        if (err) {
            console.error('添加用户失败：', err);
            return res.status(500).json({ error: '添加用户失败' });
        }
        console.log('添加用户成功：', result);
        res.json(result);
    });
});

app.delete('/api/deleteUser/:id', (req, res) => {
    const id = req.params.id;
    const sql = 'DELETE FROM User WHERE id = ?';
    pool.query(sql, [id], (err, result) => {
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

app.put('/api/updateUser', (req, res) => {
    const id = req.body.id;
    const updatedUser = req.body;
    const sql = 'UPDATE User SET ? WHERE id = ?';
    pool.query(sql, [updatedUser, id], (err, result) => {
        if (err) {
            console.error('更新用户失败:', err);
            return res.status(500).json({ message: '更新用户失败' });
        }
        if (result.affectedRows > 0) {            
            return res.status(200).json({ message: '用户更新成功' });
        } else {
            return res.status(404).json({ message: '未找到要更新的用户' });
        }
    });
});

// 其他路由和数据库操作...

app.listen(port, () => {
    console.log(`HTTP 服务器正在运行，端口: ${port}`);
});

process.on('uncaughtException', (err) => {
    console.error('未捕获的异常:', err);
});

process.on('unhandledRejection', (reason, promise) => {
    console.error('未处理的拒绝:', promise, '原因:', reason);
});
