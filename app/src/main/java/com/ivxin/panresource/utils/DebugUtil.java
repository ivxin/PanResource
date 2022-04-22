package com.ivxin.panresource.utils;

import android.util.Log;

public class DebugUtil {
    public static final int MAX_LENGTH = 1800;

    /**
     * 打印log
     * @param showLog
     * @param tag
     * @param msg
     * @author yaping.wang
     */
    public static void LogLog(boolean showLog, String tag, String msg) {
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
}
