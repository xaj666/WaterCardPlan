package com.example.watercardplan;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // 找到按钮
        Button button = findViewById(R.id.button);
        TextView ID = findViewById(R.id.AndroidID);

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

        // 获取设备的 Android ID
        String androidId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

        // 打印 Android ID 到控制台
        Log.d("LoginTag", "Android ID: " + androidId);

        // 将 Android ID 复制到剪贴板
        copyToClipboard(androidId);

        // 显示 Toast 提示
        Toast.makeText(getApplicationContext(), "Android ID 已复制到剪贴板", Toast.LENGTH_SHORT).show();

        //显示Android ID标签
        ID.setText("Android ID：" + androidId);
    }

    // 将文本复制到剪贴板的方法
    private void copyToClipboard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Android ID", text);
        clipboardManager.setPrimaryClip(clipData);
    }
}
