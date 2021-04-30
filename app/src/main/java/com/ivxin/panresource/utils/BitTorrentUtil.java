package com.ivxin.panresource.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BitTorrentUtil {
    public static String serverUrl = "http://127.0.0.1:6800";
    public static String username = "admin";
    public static String password = "password";
    public static String cookie = "";//

    public static String qBitTorrentLogin() {
        String loginUrl = serverUrl + "/api/v2/auth/login";
        String params = String.format("username=%s&password=%s", username, password);
        try {
            // 构建URL
            URL uri = new URL(loginUrl);
            // 构建连接
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestMethod("POST");

            conn.setConnectTimeout(10 * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // 构建参数
            byte[] param = params.getBytes();
            // 设置请求头
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Charset", "utf-8");
            conn.setRequestProperty("Content-Length", String.valueOf(param.length));

            // 获取输出流,发送参数数据
            OutputStream out = conn.getOutputStream();
            out.write(param);
            out.flush();

            // 获取输入流,处理返回的数据
            if (conn.getResponseCode() == 200) {
                cookie = conn.getHeaderField("Set-Cookie");
            } else {
                cookie = null;
            }
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("qBitTorrentLogin", "cookie:" + cookie);
        return cookie;
    }

    public static String postTorrentFile(File torrentFile) {
        if (TextUtils.isEmpty(cookie)) {
            qBitTorrentLogin();
        }
        String uploadUrl = serverUrl + "/api/v2/torrents/add";
        StringBuilder result = new StringBuilder();
        try {
            // 构建URL
            URL uri = new URL(uploadUrl);
            // 构建连接
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10 * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // 设置请求头
            conn.setRequestProperty("Content-Type", "multipart/form-data");
            conn.setRequestProperty("User-Agent", "Fiddler");
            conn.setRequestProperty("Host", serverUrl);
            conn.setRequestProperty("Cookie", cookie.substring(0, cookie.indexOf(';')));//SID=uswt9TaJ5XPs0h0K2R9ek3nNL83NxTf3; HttpOnly; path=/; SameSite=Strict
            conn.setRequestProperty("Charset", "uft-8");
//            conn.setRequestProperty("Content-Length", String.valueOf(torrentFile.length()));
            conn.setRequestProperty("Content-Disposition", String.format("form-data; name='torrents'; filename='%s'", torrentFile.getName()));
            conn.setRequestProperty("Content-Type", "application/x-bittorrent");

            // 获取输出流,发送参数数据
            OutputStream os = conn.getOutputStream();
            //获取输入流
            InputStream is = new FileInputStream(torrentFile);
            byte[] bts = new byte[1024 * 10];
            //一个一个字节的读取并写入
            while (is.read(bts) != -1) {
                os.write(bts);
            }
            os.flush();
            os.close();
            is.close();
            // 获取输入流,处理返回的数据
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } else {
                result = new StringBuilder(conn.getResponseCode() + ":" + conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            result = new StringBuilder("MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            result = new StringBuilder("IOException");
            e.printStackTrace();
        }
        Log.d("postTorrentFile", "result:" + result);
        return result.toString();
    }
}
