const express = require('express')// 引入 express 模块
const db = require('./db')// 引入数据库模块
const app = express()// 创建 express 应用

//json解析器
const bodyParser = require('body-parser')
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({ extended: false }))

// 处理 GET 请求
app.get('/get', (req, res) => {
  let sql = 'SELECT * FROM user'
  db.query(sql, (err, result) => {
    if (err) {
      res.send(err)
    } else {
      res.send({
        code: 200,data:result,meg: '查询成功'})
    }
    console.log("get请求成功");
  })
})

//
app.post('/post', (req, res) => {
  console.log(req.body)
})

// 启动服务器
app.listen(8080, () => {
  console.log('服务器已启动，监听8080端口')
})
