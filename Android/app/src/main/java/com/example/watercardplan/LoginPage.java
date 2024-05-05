package com.example.watercardplan;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginPage extends AppCompatActivity {

    private static final String TAG = "LoginTag";
    private static final String API_URL = "http://150.158.85.14:4867/api/keys";
    private String[] keysArray;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        Button button = findViewById(R.id.button);
        TextView ID = findViewById(R.id.AndroidID);

        // 设置点击事件监听器
        button.setOnClickListener(v -> {
            // 创建一个Intent对象，指定要启动的Activity
            Intent intent = new Intent(LoginPage.this, TestPage.class);
            // 启动第二个Activity
            startActivity(intent);
            // 关闭当前的活动
            finish();
        });

        // 获取设备的 Android ID
        @SuppressLint("HardwareIds") String androidId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

        // 打印 Android ID 到控制台
        Log.d(TAG, "Android ID: " + androidId);

        // 将 Android ID 复制到剪贴板
        copyToClipboard(androidId);

        // 显示 Toast 提示
        Toast.makeText(getApplicationContext(), "Android ID 已复制到剪贴板", Toast.LENGTH_SHORT).show();

        //显示Android ID标签
        ID.setText("Android ID：" + androidId);

        // 异步获取服务器端的Keys数据
        fetchData();


    }

    // 将文本复制到剪贴板的方法
    private void copyToClipboard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Android ID", text);
        clipboardManager.setPrimaryClip(clipData);
    }

    //获取服务器Keys密钥数据
    @SuppressLint("StaticFieldLeak")
    private void fetchData() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String responseData = null;

                try {
                    URL url = new URL(API_URL);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty. No point in parsing.
                        return null;
                    }
                    responseData = buffer.toString();
                } catch (IOException e) {
                    Log.e(TAG, "出错：", e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(TAG, "Error closing stream", e);
                        }
                    }
                }

                if (responseData != null) {
                    try {
                        JSONArray keysJSONArray = new JSONArray(responseData);
                        keysArray = new String[keysJSONArray.length()];
                        for (int i = 0; i < keysJSONArray.length(); i++) {
                            JSONObject keyJSONObject = keysJSONArray.getJSONObject(i);
                            String secretKey = keyJSONObject.getString("secret_key");
                            keysArray[i] = secretKey;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON", e);
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // 在这里处理你得到的keys数据
                if (keysArray != null && keysArray.length > 0) {
                    StringBuilder keysBuilder = new StringBuilder();
                    for (String key : keysArray) {
                        keysBuilder.append(key).append("\n");
                    }
                    String keys = keysBuilder.toString().trim(); // 去除多余的空格
                    Log.d(TAG, "Keys密钥: \n" + keys);
                } else {
                    Toast.makeText(LoginPage.this, "未获取到Keys数据", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

}
