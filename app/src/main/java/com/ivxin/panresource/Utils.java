package com.ivxin.panresource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class Utils {
    /**
     * 判断是否安装目标应用
     *
     * @param packageName 目标应用安装后的包名
     * @return 是否已安装目标应用
     */
    public static boolean isInstalledByPackageName(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }

    public static boolean startAppByPackageName(Activity activity, String packageName) {
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = new Intent();
        intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        } else {
            activity.startActivity(intent);
            return true;
        }
    }

    /**
     * 作者：MrTrying 链接：https://www.jianshu.com/p/b78e6697e15f 读取剪切板文本
     *
     * @param context context
     */
    public static String getTextFromClipBoard(Context context) {
        String text = "";
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 判断剪切版时候有内容
        if (clipboardManager != null) {
            if (!clipboardManager.hasPrimaryClip())
                return text;
            ClipData clipData = clipboardManager.getPrimaryClip();
            // //获取 ClipDescription
            // ClipDescription clipDescription =
            // clipboardManager.getPrimaryClipDescription();
            // //获取 label
            // String label = clipDescription.getLabel().toString();
            // 获取 text
            // text = clipData.getItemAt(0).getText().toString();
            text = clipData.getItemAt(0).coerceToText(context).toString();
            /*
             * 顺带说一下之前遇到的问题，我boss直接从网易新闻复制了内容， 粘帖到我们自己的app中，之后文本的样式都不对，
             * 这是因为复制的内容是包含HTML标签的字符串，导致内容显示有问题， String text =
             * clipData.getItemAt(0).coerceToText(context).toString();
             * 最后使用coerceToText()将剪贴板数据强制转换为文本解决问题。
             */
        }

        return text;
    }

    /**
     * 作者：MrTrying 链接：https://www.jianshu.com/p/b78e6697e15f 储存文本到剪切板
     *
     * @param context context
     */
    public static void putTextIntoClipBoard(Context context, String label, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建ClipData对象
        ClipData clipData = ClipData.newPlainText(label, text);
        // 添加ClipData对象到剪切板中
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
        }
    }

    public static String getAppName(Context context, int pID) {
        String processName = "";
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = am.getRunningAppProcesses();
        Iterator<ActivityManager.RunningAppProcessInfo> i = l.iterator();
        // PackageManager pm = context.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    // CharSequence c = pm
                    // .getApplicationLabel(pm.getApplicationInfo(info.processName,
                    // PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +" Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }

    public static int getAppPid(Context context, String packageName) {
        int pid = -1;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = am.getRunningAppProcesses();
        Iterator<ActivityManager.RunningAppProcessInfo> i = l.iterator();
        // PackageManager pm = context.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.processName == packageName) {
                    // CharSequence c = pm
                    // .getApplicationLabel(pm.getApplicationInfo(info.processName,
                    // PackageManager.GET_META_DATA));
                    // // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +" Label: "+c.toString());
                    // processName = c.toString();
                    pid = info.pid;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return pid;
    }

    /**
     * GET方式连接
     *
     * @param url    URL
     * @param params 参数
     * @return String 结果
     */
    public static String httpURLConnectionGET(String url, String params) {
        String result = "";
        try {
            // 构建URL
            URL uri = new URL(url + "?" + params);
            // 构建连接
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setRequestMethod("GET");
            // 获取输入流,处理返回的数据
            if (conn.getResponseCode() == 200) {
                result = buildResult(conn.getInputStream());
            } else {
                result = conn.getResponseCode() + ":" + conn.getResponseMessage();
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            result = "MalformedURLException:" + e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            result = "IOException:" + e.getMessage();
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
    public static String httpURLConnectionPOST(String url, String params) {
        String result = "";
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
            result = "MalformedURLException:" + e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            result = "IOException:" + e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 处理输入流的结果
     *
     * @param inputStream
     * @return
     */
    private static String buildResult(InputStream inputStream) {
        String result = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            reader.close();
        } catch (IOException e) {
            result = "IOException:" + e.getMessage();
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 删除Html标签
     * 将utf-8的文档手动改成gbk的，然后进行筛选输出，用于多数据
     *
     * @param inputString
     * @return
     */
    public static String removeHtmlTag(String inputString) {
        if (inputString == null)
            return null;
        String htmlStr = inputString; // 含html标签的字符串
        String textStr = "";
        java.util.regex.Pattern p_script;
        java.util.regex.Matcher m_script;
        java.util.regex.Pattern p_style;
        java.util.regex.Matcher m_style;
        java.util.regex.Pattern p_html;
        java.util.regex.Matcher m_html;
        java.util.regex.Pattern p_special;
        java.util.regex.Matcher m_special;
        try {
// 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
// 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
            String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
// 定义HTML标签的正则表达式
            String regEx_html = "<[^>]+>";
// 定义一些特殊字符的正则表达式 如：     
            String regEx_special = "\\&[a-zA-Z]{1,10};";


            p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll(""); // 过滤script标签
            p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
            m_style = p_style.matcher(htmlStr);
            htmlStr = m_style.replaceAll(""); // 过滤style标签
            p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll(""); // 过滤html标签
            p_special = Pattern
                    .compile(regEx_special, Pattern.CASE_INSENSITIVE);
            m_special = p_special.matcher(htmlStr);
            htmlStr = m_special.replaceAll(""); // 过滤特殊标签
            textStr = htmlStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return textStr;// 返回文本字符串
    }

    public static final int MAX_LENGTH = 1800;

    /**
     * 打印log
     * @param showLog
     * @param tag
     * @param msg
     * @author yaping.wang
     */
    public static void printLog(boolean showLog, String tag, String msg) {
        String format_msg = formatJson(msg);
        if (showLog) {
            int strLength = format_msg.length();
            int start = 0;
            int page = 0;
            while (start + MAX_LENGTH < strLength) {
                Log.d(tag + "-" + page, format_msg.substring(start, start + MAX_LENGTH));
                start += MAX_LENGTH;
                page++;
            }
            Log.d(tag + "-" + page, format_msg.substring(start, strLength));
        }
    }
    /**
     * 格式化
     *
     * @param jsonStr
     * @return
     * @author lizhgb
     * @Date 2015-10-14 下午1:17:35
     */
    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr))
            return "";
        StringBuilder sb = new StringBuilder();
        String last = "\0";
        String current = "\0";
        int indent = 0;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = String.valueOf(jsonStr.charAt(i));
            switch (current) {
                case "{":
                case "[":
                    sb.append(current);
                    sb.append("\n");
                    indent++;
                    addIndentBlank(sb, indent);
                    break;
                case "}":
                case "]":
                    sb.append("\n");
                    indent--;
                    addIndentBlank(sb, indent);
                    sb.append(current);
                    break;
                case ",":
                    sb.append(current);
                    if (last != "\\") {
                        sb.append("\n");
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }
        return sb.toString();
    }

    /**
     * 添加space
     *
     * @param sb
     * @param indent
     * @author lizhgb
     * @Date 2015-10-14 上午10:38:04
     */
    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("\t");
        }
    }
    public static void killAppByPID(Context context, int pid) {
        android.os.Process.killProcess(pid);
    }

}
