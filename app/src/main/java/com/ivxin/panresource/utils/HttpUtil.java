package com.ivxin.panresource.utils;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.ivxin.panresource.eneity.ResponseVO;

import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class HttpUtil {
    public static final String TAG = "HttpUtil";
    private static HttpUtil util;

    private HttpUtil() {
    }

    public static HttpUtil getInstance() {
        if (util == null) {
            util = new HttpUtil();
        }
        return util;
    }

    private static String baseUrl;
    private static ThreadPoolExecutor executor;

    public void init(String baseUrl) {
        executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        setBaseUrl(baseUrl);
    }

    public static void setBaseUrl(String baseUrl) {
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        if (!baseUrl.startsWith("http")) {
            baseUrl = "http://" + baseUrl;
        }
        HttpUtil.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public <T> void request(String sub, HashMap<String, String> param, Class<T> clazz, HttpCallBack<T> httpCallBack) {
        if (executor == null) {
            Log.d(TAG, "invoke HttpUtil.init() first");
            return;
        }
        if (sub.startsWith("/")) {
            sub = sub.substring(1);
        }
        final String url = baseUrl + sub;
        MyHandler<T> handler = new MyHandler<>();
        handler.setHttpCallBack(httpCallBack);
        handler.setClazz(clazz);
        executor.execute(() -> {
            Message msg = handler.obtainMessage();
            msg.obj = HttpConnectionUtils.httpURLconnectionPOST(url, param);
            handler.sendMessage(msg);
        });

    }

    private static class MyHandler<T> extends Handler {
        private Gson gson = new Gson();
        private HttpCallBack<T> httpCallBack;
        private Class<T> clazz;

        public void setHttpCallBack(HttpCallBack<T> httpCallBack) {
            this.httpCallBack = httpCallBack;
        }

        public void setClazz(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public void handleMessage(Message msg) {
            if (httpCallBack != null) {
                String jsonResult = (String) msg.obj;
                Log.d(TAG, "jsonResult:" + jsonResult);
                try {
                    if (TextUtils.isEmpty(jsonResult)) {
                        httpCallBack.done(-1, "获取数据失败", null, null);
                        return;
                    }
                    ResponseVO responseVO = gson.fromJson(jsonResult, ResponseVO.class);
                    T t = gson.fromJson(responseVO.getData(), clazz);
                    httpCallBack.done(responseVO.getCode(), responseVO.getMessage(), t, null);
                } catch (Exception e) {
                    httpCallBack.done(-1, "jsonResult:" + jsonResult, null, e);
                }
            }
        }
    }


    public interface HttpCallBack<T> {
        void done(int code, String message, T t, Exception e);
    }

}
