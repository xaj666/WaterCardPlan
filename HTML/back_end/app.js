const express = require('express');
const bodyParser = require('body-parser');
const jwt = require('jsonwebtoken');
const cors = require('cors');
const mysql = require('mysql');
const crypto = require('crypto'); // 引入 crypto 模块

const app = express();
const port = process.env.PORT || 4867;
const secretKey = crypto.randomBytes(32).toString('hex');

const dbConfig = {
    host: 'localhost',
    user: 'WaterCardPlan',
    password: '497545',
    database: 'watercardplan'
};

let pool = mysql.createPool(dbConfig);

function handleDisconnect() {
    pool = mysql.createPool(dbConfig); // 创建新的连接池

    pool.getConnection((err, connection) => {
        if (err) {
            console.error('数据库连接失败:', err);
            setTimeout(handleDisconnect, 2000); // 2秒后重试连接
        } else {
            console.log('已连接到数据库');
            connection.release();
        }
    });

    pool.on('error', (err) => {
        console.error('数据库连接错误:', err);
        if (err.code === 'PROTOCOL_CONNECTION_LOST') {
            handleDisconnect();
        } else {
            throw err;
        }
    });
}

handleDisconnect();

app.use(bodyParser.json());
app.use(cors()); // 使用 cors 中间件

//生成key函数
function generateKey() {
    const randomBytes = crypto.randomBytes(24);// 生成一个长度为24的随机字符串
    const key = randomBytes.toString('base64').replace(/[\/\n]/g, '');// 将随机字符串转换为 base64 编码，并替换掉特殊字符
    const onlyDigitsAndLetters = key.replace(/[^a-zA-Z0-9]/g, '');// 只保留数字和字母
    return onlyDigitsAndLetters.substring(0, 24);// 返回前24个字符
}

//获取所有keys的函数
function getAllKeys(callback) {
    const sql = 'SELECT secret_key FROM User';
    pool.query(sql, (err, result) => {
        if (err) {
            console.error('查询数据失败：', err);
            return callback(err, null);
        }
        const keys = result.map(row => row.secret_key);
        callback(null, keys);
    });
}

function addKey(keys, callback) {
    let key = generateKey();
    while (keys.includes(key)) {
        key = generateKey();
    }
    callback(key);
}

app.get('/API/key', (req, res) => {
    getAllKeys((err, keys) => {
        if (err) {
            return res.status(500).json({ error: '无法获取 keys' });
        }
        addKey(keys, (key) => {
            res.send(key); // 以字符串形式返回 key
        });
    });
});

// 查询数据库以获取最新的数据
app.get('/API/refresh', function (req, res) {
    let sql = 'SELECT * FROM User';
    // 执行查询语句
    pool.query(sql, function (err, result) {
        if (err) {
            console.error('查询数据失败：', err);
            res.status(500).json({ error: '查询数据失败' });
        } else {
            // 将查询到的数据发送回前端
            console.log('查询到的数据：', result);
            res.json(result);
        }
    });
});

// 添加用户
app.post('/api/addUser', (req, res) => {
    // 从请求体中获取用户数据
    const newUser = req.body;
    console.log('收到的新用户数据：', newUser);
    // 将新用户数据插入数据库
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

// 删除用户
app.delete('/api/deleteUser/:id', (req, res) => {
    const id = req.params.id; // 获取要删除的用户名
    const sql = `DELETE FROM User WHERE id = ?`; // SQL 查询语句
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

// 更新用户
app.put('/api/updateUser', (req, res) => {
    const id = req.body.id; // 获取要更新的用户的 id
    const updatedUser = req.body; // 获取要更新的用户数据
    // 构造 SQL 查询语句，使用 id 作为查找条件
    const sql = 'UPDATE User SET ? WHERE id = ?';
    // 执行更新操作
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

// 获取所有Key
app.get('/api/keys', (req, res) => {
    pool.query('SELECT secret_key FROM User', (error, results) => {
        if (error) {
            console.error('Error fetching players from database', error);
            res.status(500).json({ error: '数据库查询失败' });
        } else {
            res.json(results);
        }
    });
});

// 获取User表中共有多少条记录
app.get('/api/count', (req, res) => {
    pool.query('SELECT COUNT(*) AS total FROM User', (error, results) => {
        if (error) {
            console.error('Error fetching record count from User table', error);
            res.status(500).json({ error: '数据库查询失败' });
        } else {
            const totalCount = results[0].total;
            res.json({ totalCount });
        }
    });
});

// 获取 server_info 表中 announcement 字段的信息
app.get('/api/announcement', (req, res) => {
    pool.query('SELECT announcement FROM server_info', (error, results) => {
        if (error) {
            console.error('Error fetching announcement from server_info table', error);
            res.status(500).json({ error: '数据库查询失败' });
        } else {
            res.json(results);
        }
    });
});

// 根据 secret_key 查询对应的 android_id
app.get('/api/Android', (req, res) => {
    const secretKey = req.query.secret_key;

    if (!secretKey) {
        return res.status(400).json({ error: '请提供有效的 secret_key' });
    }

    pool.query('SELECT android_id FROM User WHERE secret_key = ?', [secretKey], (error, results) => {
        if (error) {
            console.error('Error fetching android_id from database', error);
            return res.status(500).json({ error: '数据库查询失败' });
        }

        if (results.length === 0) {
            return res.status(404).json({ error: '未找到匹配的 android_id' });
        }

        const androidIds = results.map(result => result.android_id);
        res.json({ android_id: androidIds });
    });
});

// 根据 secret_key 查询对应的 UserName
app.get('/api/GetName', (req, res) => {
    const secretKey = req.query.secret_key;

    if (!secretKey) {
        return res.status(400).json({ error: '请提供有效的 secret_key' });
    }

    pool.query('SELECT username FROM User WHERE secret_key = ?', [secretKey], (error, results) => {
        if (error) {
            console.error('Error fetching android_id from database', error);
            return res.status(500).json({ error: '数据库查询失败' });
        }

        if (results.length === 0) {
            return res.status(404).json({ error: '未找到匹配的 username' });
        }

        const androidIds = results.map(result => result.username);
        res.json({ username: androidIds });
    });
});

// 根据 secret_key 查询对应的 score
app.get('/api/GetScore', (req, res) => {
    const secretKey = req.query.secret_key;

    if (!secretKey) {
        return res.status(400).json({ error: '请提供有效的 secret_key' });
    }

    pool.query('SELECT score FROM User WHERE secret_key = ?', [secretKey], (error, results) => {
        if (error) {
            console.error('Error fetching android_id from database', error);
            return res.status(500).json({ error: '数据库查询失败' });
        }

        if (results.length === 0) {
            return res.status(404).json({ error: '未找到匹配的 score' });
        }

        const androidIds = results.map(result => result.score);
        res.json({ score: androidIds });
    });
});

//根据secret_key扣积分
app.post('/api/deductScore/:secretKey', (req, res) => {
    const secret_key = req.params.secretKey;
    const scoreToDeduct = req.body.score; // 假设积分是作为请求体中的参数传递

    if (!secret_key || !scoreToDeduct) {
        return res.status(400).json({ error: '缺少 secret_key 或 score 参数' });
    }

    const checkQuery = 'SELECT * FROM User WHERE secret_key = ?';
    pool.query(checkQuery, [secret_key], (checkError, checkResult) => {
        if (checkError) {
            console.error('Error checking for existing secret_key in database', checkError);
            return res.status(500).json({ error: '数据库查询失败' });
        }

        if (checkResult.length === 0) {
            return res.status(404).json({ error: '未找到匹配的 secret_key' });
        }

        const currentScore = checkResult[0].score;

        if (currentScore < scoreToDeduct) {
            return res.status(400).json({ error: '积分不足' });
        }

        const updatedScore = currentScore - scoreToDeduct;

        const updateQuery = 'UPDATE User SET score = ? WHERE secret_key = ?';
        pool.query(updateQuery, [updatedScore, secret_key], (updateError, updateResult) => {
            if (updateError) {
                console.error('Error updating score in database', updateError);
                return res.status(500).json({ error: '数据库更新失败' });
            }

            res.json({ message: '积分扣除成功' });
        });
    });
});

// 查询不重复的 Android_id 数量
app.get('/api/uniqueAndroidIdsCount', (req, res) => {
    const query = 'SELECT COUNT(DISTINCT android_id) AS unique_count FROM User';

    pool.query(query, (error, result) => {
        if (error) {
            console.error('Error querying unique android_id count', error);
            res.status(500).json({ error: '数据库查询失败' });
        } else {
            const uniqueCount = result[0].unique_count;
            res.json({ unique_count: uniqueCount });
        }
    });
});

// 插入 IP 地址
app.post('/api/addIp/:key', (req, res) => {
    const key = req.params.key;
    const ip = req.body.ip;

    if (!key || !ip) {
        return res.status(400).json({ error: '缺少密钥或 IP 地址参数' });
    }

    const updateQuery = 'UPDATE User SET ip = ? WHERE secret_key = ?';
    pool.query(updateQuery, [ip, key], (error, result) => {
        if (error) {
            console.error('Error updating IP address in database', error);
            return res.status(500).json({ error: '数据库更新失败' });
        }

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: '未找到匹配的密钥' });
        }

        res.json({ message: 'IP 地址更新成功' });
    });
});

// 创建 HTTP 服务器
app.listen(port, () => {
    console.log(`HTTP 服务器正在运行，端口: ${port}`);
});