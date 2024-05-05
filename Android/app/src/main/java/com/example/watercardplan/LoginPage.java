package com.example.watercardplan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // 找到按钮
        Button button = findViewById(R.id.button);

        // 设置点击事件监听器
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个Intent对象，指定要启动的Activity
                Intent intent = new Intent(LoginPage.this, TestPage.class);
                // 启动第二个Activity
                startActivity(intent);
                // 关闭当前的活动
                finish();
            }
        });
    }
}
