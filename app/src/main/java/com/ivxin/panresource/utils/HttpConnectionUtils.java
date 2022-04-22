package com.ivxin.panresource.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class HttpConnectionUtils {
    /**
     * 处理Map参数为 key=value&key=value...
     *
     * @param params 参数
     * @return
     */
    private static String buildPrams(Map<String, String> params) {
        StringBuilder pramsString = new StringBuilder();
        if (params != null) {
            Set<String> set = params.keySet();
            for (String key : set) {
                pramsString.append(key).append("=").append(params.get(key)).append("&");
            }
        }
        pramsString = new StringBuilder(pramsString.substring(0, pramsString.length() - 1));
        return pramsString.toString();
    }

    /**
     * 处理输入流的结果
     *
     * @param inputStream
     * @return
     */
    private static String buildResult(InputStream inputStream) {
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
        } catch (IOException e) {
            result = new StringBuilder(e.getMessage());
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * POST方式连接返回结果
     *
     * @param url    URL
     * @param params 参数
     * @return String 结果
     */
    public static String httpURLconnectionPOST(String url, Map<String, String> params) {
        String result;
        try {
            // 构建URL
            URL uri = new URL(url);
            // 构建连接
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10 * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // 构建参数
            byte[] param = buildPrams(params).getBytes();
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
                result = buildResult(conn.getInputStream());
            } else {
                result = conn.getResponseCode() + ":" + conn.getResponseMessage();
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            result = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            result = e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

    /**
     * POST方式连接返回结果
     *
     * @param url    URL
     * @param params 参数
     * @return String 结果
     */
    public static String httpURLconnectionPOST(String url, String params) {
        String result;
        try {
            // 构建URL
            URL uri = new URL(url);
            // 构建连接
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(10 * 1000);
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
                result = buildResult(conn.getInputStream());
            } else {
                result = conn.getResponseCode() + ":" + conn.getResponseMessage();
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            result = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            result = e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

    /**
     * GET方式连接
     *
     * @param url   URL
     * @param prams 参数
     * @return String 结果
     */
    public static String httpURLconnectionGET(String url, Map<String, String> prams) {
        String result = "";
        try {
            // 构建URL
            URL uri = new URL(url + "?" + buildPrams(prams));
            // 构建连接
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(10 * 1000);
            conn.setRequestMethod("GET");
            // 获取输入流,处理返回的数据
            if (conn.getResponseCode() == 200) {
                result = buildResult(conn.getInputStream());
            } else {
                result = conn.getResponseCode() + ":" + conn.getResponseMessage();
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            result = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            result = e.getMessage();
            e.printStackTrace();
        }

        return result;
    }
}
