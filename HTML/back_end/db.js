const mysql=require('mysql');

// 创建数据库连接
const connection=mysql.createConnection({
    host:'150.158.85.14',// 数据库主机地址
    port:'3306',// 数据库端口号
    user:'WaterCardPlan',// 数据库用户名
    password:'497545',// 数据库密码
    database:'watercardplan'// 数据库名
})
    
// 连接到数据库
connection.connect((err)=>{
    if(err){
        console.error('数据库连接失败：',err);
        return;
    }
    console.log('成功连接到数据库');
})
// 导出数据库连接
module.exports = connection;