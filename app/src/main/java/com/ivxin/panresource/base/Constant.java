package com.ivxin.panresource.base;

import com.google.gson.Gson;

public class Constant {
    public static final Gson GSON = new Gson();
    public static String versionName = "";//本app的
    public static int version = 0;

    public static final String SP_FILE = "SP_FILE";
    public static final String SP_KEY_TEMPLATE = "SP_KEY_TEMPLATE";
    public static final String SP_KEY_REX = "SP_KEY_REX";
    public static final String SP_KEY_PASSWORD = "SP_KEY_PASSWORD";
    public static final String SP_KEY_ONLY7Z = "SP_KEY_ONLY7Z";
    public static final String SP_KEY_PACKAGE_NAME = "SP_KEY_PACKAGE_NAME";
    public static final String SP_KEY_AUTOMATIC = "SP_KEY_AUTOMATIC";
    public static final String SP_KEY_OUTPUT_PATH_SET = "SP_KEY_OUTPUT_PATH_SET";
    public static final String BMOB_AccessKey = "";


    public static final String SP_KEY_DOWNLOAD_SERVER_CONFIG = "SP_KEY_DOWNLOAD_SERVER_CONFIG";
    public static String codeRex = "";
    public static String template = "";
    public static String zipFilePassword = "";
    public static String appPackageName = "";//要打开的app的包名
    public static boolean isAutomatic;
    public static boolean isOnly7z;
}
