package de.jxvtc.WaterCardTool.Activities;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderManager {
    private static final String TAG = "OrderManager";
    private static final String BASE_URL = "http://122.51.66.112:4866/api/orders";
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void createOrder(String username, String androidId, int amount, String ip, OrderCallback callback) {
        executor.execute(() -> {
            String result = createOrderInBackground(username, androidId, amount, ip);
            mainHandler.post(() -> handleCreateOrderResult(result, callback));
        });
    }

    public static void updateOrderStatus(String orderId, int status, OrderCallback callback) {
        executor.execute(() -> {
            String result = updateOrderStatusInBackground(orderId, status);
            mainHandler.post(() -> handleUpdateOrderResult(result, callback));
        });
    }

    public interface OrderCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    private static String createOrderInBackground(String username, String androidId, int amount, String ip) {
        try {
            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("username", username);
            jsonParam.put("android_id", androidId);
            jsonParam.put("amount", amount);
            jsonParam.put("ip", ip);

            OutputStream os = connection.getOutputStream();
            os.write(jsonParam.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                return response.toString();
            } else {
                return "ERROR:创建订单失败: " + responseCode;
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "创建订单错误: " + e.getMessage());
            return "ERROR:创建订单错误: " + e.getMessage();
        }
    }

    private static String updateOrderStatusInBackground(String orderId, int status) {
        try {
            URL url = new URL(BASE_URL + "/" + orderId + "/status");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("status", status);

            OutputStream os = connection.getOutputStream();
            os.write(jsonParam.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                return response.toString();
            } else {
                return "ERROR:更新订单状态失败: " + responseCode;
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "更新订单状态错误: " + e.getMessage());
            return "ERROR:更新订单状态错误: " + e.getMessage();
        }
    }

    private static void handleCreateOrderResult(String result, OrderCallback callback) {
        if (result != null && !result.startsWith("ERROR:")) {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                callback.onSuccess(jsonResponse.toString());
            } catch (JSONException e) {
                callback.onError("解析响应失败: " + e.getMessage());
            }
        } else {
            callback.onError(result.replace("ERROR:", ""));
        }
    }

    private static void handleUpdateOrderResult(String result, OrderCallback callback) {
        if (result != null && !result.startsWith("ERROR:")) {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                callback.onSuccess(jsonResponse.getString("message"));
            } catch (JSONException e) {
                callback.onError("解析响应失败: " + e.getMessage());
            }
        } else {
            callback.onError(result.replace("ERROR:", ""));
        }
    }
}
