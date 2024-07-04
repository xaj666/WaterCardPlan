package de.jxvtc.WaterCardTool.Activities;


import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import de.jxvtc.WaterCardTool.R;


public class Login extends Activity {

    public static String SeverKey;
    public static String SeverID;
    public static String SeverInfo;

    public static String SeverScore;
    private static final String TAG = "LoginTag";

    private static String API_KEY = "http://122.51.66.112:4867/api/keys";
    private static String API_COUNT = "http://122.51.66.112:4867/api/count";
    private  static String API_INFO = "http://122.51.66.112:4867/api/announcement";
    private static String API_NAME;
    public static String IP;
    public static String SeverName;

    public static int SeverCount;

    private static String API_Android = "http://122.51.66.112:4867/api/Android?secret_key=xaj";
    public static String API_Score;
    private String[] keysArray;

    private String[] AndroidArray;

    private boolean KeysFlag;

    private boolean AndroidFlag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        Button button = findViewById(R.id.button1);
        TextView ID = findViewById(R.id.AndroidID);
        EditText editText = findViewById(R.id.editTextWriteTagData);

        // 从 SharedPreferences 中获取保存的文本，如果没有保存的值则使用空字符串
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedText = sharedPreferences.getString("savedText", "");
        editText.setText(savedText);

        // 获取设备的 Android ID
        @SuppressLint("HardwareIds") String androidId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        SeverID = androidId;

        //不验证主机名，始终接受连接
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        //获取代理数量
        fetchCount();

        //获取公告
        fetchAnnouncement();

        // 设置点击事件监听器
        button.setOnClickListener(v -> {
            KeysFlag = false;
            AndroidFlag = false;

            if (keysArray != null) {
                for (String key : keysArray) {
                    if (editText.getText().toString().equals(key)) {
                        KeysFlag = true;
                        API_Android = "http://122.51.66.112:4867/api/Android?secret_key=" + key;
                        API_NAME = "http://122.51.66.112:4867/api/GetName?secret_key=" + key;
                        API_Score = "http://122.51.66.112:4867/api/GetScore?secret_key=" + key;
                        SeverKey = key;
                        Log.d(TAG, "Key success:" + API_Android);
                        break;
                    }
                }
            }

            //异步获取服务器端的数据
            fetchAndroidSync();


            if (AndroidArray != null && KeysFlag) {
                for (String Android_id : AndroidArray) {
                    if (androidId.equals(Android_id)) {
                        AndroidFlag = true;
                        Log.d(TAG, "ID success:" + Android_id);
                        break;
                    }
                }
            }

            //获取用户
            fetchNameSync();

            if (KeysFlag == false)
                Toast.makeText(getApplicationContext(), "Key密钥验证失败", Toast.LENGTH_SHORT).show();
            else if (AndroidFlag == false)
                Toast.makeText(getApplicationContext(), "Android ID验证失败", Toast.LENGTH_SHORT).show();

            //获取积分
            fetchScoreSync();


            if (KeysFlag && AndroidFlag) {
                // 保存输入框中的文本到 SharedPreferences
                String inputText = editText.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("savedText", inputText);
                editor.apply();
                IP = GetIP();

                setLoggedIn(true);
                // 在登录成功后返回到 Menu 页面
                Intent intent = new Intent(Login.this, MainMenu.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish(); // 结束当前的 Login 页面

                //提示
                Toast.makeText(getApplicationContext(), "密钥验证成功", Toast.LENGTH_SHORT).show();
                // 关闭当前的活动
                finish();
            }


        });

        // 打印 Android ID 到控制台
        //Log.d(TAG, "Android ID: " + androidId);

        // 将 Android ID 复制到剪贴板
        copyToClipboard(androidId);

        // 显示 Toast 提示
        Toast.makeText(getApplicationContext(), "Android ID 已复制到剪贴板", Toast.LENGTH_SHORT).show();

        //显示Android ID标签
        ID.setText("Android ID：" + androidId);

        // 异步获取服务器端的数据
        fetchData();


    }

    private void setLoggedIn(boolean loggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", loggedIn);
        editor.apply();
    }

    // 将文本复制到剪贴板的方法
    private void copyToClipboard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Android ID", text);
        clipboardManager.setPrimaryClip(clipData);
    }

    //获取服务器Key密钥数据
    @SuppressLint("StaticFieldLeak")
    private void fetchData() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String responseData = null;

                try {
                    URL url = new URL(API_KEY);
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
                    //Log.d(TAG, "Keys密钥: \n" + keys);
                } else {
                    Toast.makeText(Login.this, "服务器错误：未获取到Key数据", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }


    //获取服务器Android_id密钥数据
    @SuppressLint("StaticFieldLeak")
    private void fetchAndroidSync() {
        // 在新线程中执行网络请求
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String responseData = null;

                try {
                    URL url = new URL(API_Android);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) {
                        // Nothing to do.
                        return;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty. No point in parsing.
                        return;
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
                        JSONObject keysJSONObject = new JSONObject(responseData);
                        JSONArray keysJSONArray = keysJSONObject.getJSONArray("android_id");
                        AndroidArray = new String[keysJSONArray.length()];
                        for (int i = 0; i < keysJSONArray.length(); i++) {
                            String secretKey = keysJSONArray.getString(i);
                            AndroidArray[i] = secretKey;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON", e);
                    }
                }

                // 处理获取到的数据
                if (AndroidArray != null && AndroidArray.length > 0) {
                    StringBuilder keysBuilder = new StringBuilder();
                    for (String key : AndroidArray) {
                        keysBuilder.append(key).append("\n");
                    }
                    String keys = keysBuilder.toString().trim(); // 去除多余的空格
                    //Log.d(TAG, "Android ID: \n" + keys);
                } else {
                    // 处理未获取到数据的情况
                    Toast.makeText(Login.this, "服务器错误：未获取到ID数据", Toast.LENGTH_LONG).show();
                }
            }
        });

        thread.start();
        try {
            thread.join(); // 等待网络请求线程执行完毺
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //获取服务器UserName数据
    @SuppressLint("StaticFieldLeak")
    private void fetchNameSync() {
        // 在新线程中执行网络请求
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String responseData = null;

                try {
                    URL url = new URL(API_NAME);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) {
                        // Nothing to do.
                        return;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty. No point in parsing.
                        return;
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
                        JSONObject keysJSONObject = new JSONObject(responseData);
                        JSONArray keysJSONArray = keysJSONObject.getJSONArray("username");
                        AndroidArray = new String[keysJSONArray.length()];
                        for (int i = 0; i < keysJSONArray.length(); i++) {
                            String secretKey = keysJSONArray.getString(i);
                            AndroidArray[i] = secretKey;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON", e);
                    }
                }

                // 处理获取到的数据
                if (AndroidArray != null && AndroidArray.length > 0) {
                    StringBuilder keysBuilder = new StringBuilder();
                    for (String key : AndroidArray) {
                        keysBuilder.append(key).append("\n");
                    }
                    String keys = keysBuilder.toString().trim(); // 去除多余的空格
                    //Log.d(TAG, "UserName: \n" + keys);
                    SeverName = keys;
                } else {
                    // 处理未获取到数据的情况
                    Toast.makeText(Login.this, "服务器错误：未获取到用户数据", Toast.LENGTH_LONG).show();
                }
            }
        });

        thread.start();
        try {
            thread.join(); // 等待网络请求线程执行完毺
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //获取服务器Score数据
    @SuppressLint("StaticFieldLeak")
    private void fetchScoreSync() {
        // 在新线程中执行网络请求
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String responseData = null;

                try {
                    URL url = new URL(API_Score);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) {
                        // Nothing to do.
                        return;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty. No point in parsing.
                        return;
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
                        JSONObject keysJSONObject = new JSONObject(responseData);
                        JSONArray keysJSONArray = keysJSONObject.getJSONArray("score");
                        AndroidArray = new String[keysJSONArray.length()];
                        for (int i = 0; i < keysJSONArray.length(); i++) {
                            String secretKey = keysJSONArray.getString(i);
                            AndroidArray[i] = secretKey;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON", e);
                    }
                }

                // 处理获取到的数据
                if (AndroidArray != null && AndroidArray.length > 0) {
                    StringBuilder keysBuilder = new StringBuilder();
                    for (String key : AndroidArray) {
                        keysBuilder.append(key).append("\n");
                    }
                    String keys = keysBuilder.toString().trim(); // 去除多余的空格
                    //Log.d(TAG, "score: \n" + keys);
                    SeverScore = keys;
                } else {
                    // 处理未获取到数据的情况
                    Toast.makeText(Login.this, "服务器错误：未获取到积分数据", Toast.LENGTH_LONG).show();
                }
            }
        });

        thread.start();
        try {
            thread.join(); // 等待网络请求线程执行完毺
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    //ApiPost
    public static class ApiManager {

        public static void deductScore(String key, int scoreToDeduct) {
            new DeductScoreTask().execute(key, String.valueOf(scoreToDeduct));
        }

        private static class DeductScoreTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String key = params[0];
                int scoreToDeduct = Integer.parseInt(params[1]);
                String apiUrl = "http://122.51.66.112:4867/api/deductScore/" + key;

                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("score", scoreToDeduct);

                    OutputStream os = connection.getOutputStream();
                    os.write(jsonParam.toString().getBytes());
                    os.flush();
                    os.close();

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }

                    bufferedReader.close();
                    return response.toString();

                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Error: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Log.d(TAG, "Response: " + result);
                    WriteTag.MoneyFlag = true;
                    // 处理来自服务器的响应
                } else {
                    Log.e(TAG, "余额不足");
                    WriteTag.MoneyFlag = false;
                    // 显示余额不足的提示
                }
            }
        }
    }



    // 在 LoginPage 类中添加一个名为 GetIP 的方法
    private String GetIP() {
        String ipAddress = getIPAddress();
        Log.d(TAG, "IP Address: " + ipAddress);
        return ipAddress;
    }


    //获取IP
    public static String getIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchCount() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                int totalCount = 0;

                try {
                    URL url = new URL(API_COUNT);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // 读取服务器响应
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    reader = new BufferedReader(inputStreamReader);
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // 解析JSON响应
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    totalCount = jsonResponse.getInt("totalCount");

                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Error fetching record count", e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error closing stream", e);
                        }
                    }
                }

                return totalCount;
            }

            @Override
            protected void onPostExecute(Integer totalCount) {
                if (totalCount > 0) {
                    // 处理从服务器获取到的记录总数
                    Log.d(TAG, "User表中的记录总数为: " + totalCount);
                    SeverCount = totalCount;
                } else {
                    // 处理未获取到记录总数的情况
                    Log.e(TAG, "未能从服务器获取到User表的记录总数");
                }
            }
        }.execute();
    }


    //获取公告
    @SuppressLint("StaticFieldLeak")
    private void fetchAnnouncement() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String announcement = null;

                try {
                    URL url = new URL(API_INFO);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // 读取服务器响应
                    InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                    reader = new BufferedReader(inputStreamReader);
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // 解析JSON响应
                    JSONArray jsonArray = new JSONArray(response.toString());
                    JSONObject jsonResponse = jsonArray.getJSONObject(0);
                    announcement = jsonResponse.getString("announcement");

                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Error fetching announcement", e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error closing stream", e);
                        }
                    }
                }

                return announcement;
            }

            @Override
            protected void onPostExecute(String announcement) {
                if (announcement != null && !announcement.isEmpty()) {
                    // 处理从服务器获取到的公告信息
                    Log.d(TAG, "服务器公告信息: " + announcement);
                    SeverInfo = announcement;
                } else {
                    // 处理未获取到公告信息的情况
                    Log.e(TAG, "未能从服务器获取到公告信息");
                }
            }
        }.execute();
    }

}

