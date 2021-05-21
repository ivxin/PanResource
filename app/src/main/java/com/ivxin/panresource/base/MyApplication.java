package com.ivxin.panresource.base;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.ivxin.panresource.utils.CrashHandler;
import com.qmuiteam.qmui.QMUIConfig;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.update.BmobUpdateAgent;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        initStaticValues();
        initBmob();
    }

    private void initStaticValues() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                Constant.verisonName = pi.versionName == null ? "null" : pi.versionName;
                Constant.version = pi.versionCode;

            }
        } catch (PackageManager.NameNotFoundException ignore) {
        }
    }

    //find key:https://www.bmob.cn/app/secret/308967
    //doc:http://doc.bmob.cn/data/android/index.html#sdk_1
    private void initBmob() {
        //提供以下两种方式进行初始化操作：
        //第一：默认初始化
        // 注:自v3.5.2开始，数据sdk内部缝合了统计sdk，开发者无需额外集成，传渠道参数即可，不传默认没开启数据统计功能
        Bmob.initialize(this, "7bc0fb75130bd4948f0245013f39ee3e");
        //第二：自v3.4.7版本开始,设置BmobConfig,允许设置请求超时时间、文件分片上传时每片的大小、文件的过期时间(单位为秒)，
        //BmobConfig config =new BmobConfig.Builder(this)
        ////设置appkey
        //.setApplicationId("Your Application ID")
        ////请求超时时间（单位为秒）：默认15s
        //.setConnectTimeout(30)
        ////文件分片上传时每片的大小（单位字节），默认512*1024
        //.setUploadBlockSize(1024*1024)
        ////文件的过期时间(单位为秒)：默认1800s
        //.setFileExpiration(2500)
        //.build();
        //Bmob.initialize(config);
    }
}
