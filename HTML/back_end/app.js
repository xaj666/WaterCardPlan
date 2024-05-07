const express = require('express')// 引入 express 模块
const db=require('./db')// 引入数据库模块

const app = express()// 创建 express 应用



// 使 express 监听 5000 端口号发起的 http 请求
const server = app.listen(5000, function() { 
   
	console.log("服务器已启动，监听5000端口");
})