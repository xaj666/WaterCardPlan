const mysql=require('mysql');

// 创建连接池
const pool=mysql.createPool({
    host:'62.234.77.248',// 数据库地址
    port:3306,// 数据库端口
    user:'root',// 数据库用户名
    password:'123456',// 数据库密码
    database:'watercardplan',// 数据库名称
})

module.exports = pool;