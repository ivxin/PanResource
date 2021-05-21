package com.ivxin.panresource.eneity;

import java.io.File;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class ApkFile extends BmobObject {
    private int version_i;
    private String version;
    private String update_log;
    private String apk_url;
    private String apk_url_code;
    private BmobFile apk_file;

    public BmobFile getApk_file() {
        return apk_file;
    }

    public void setApk_file(BmobFile apk_file) {
        this.apk_file = apk_file;
    }

    public int getVersion_i() {
        return version_i;
    }

    public void setVersion_i(int version_i) {
        this.version_i = version_i;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdate_log() {
        return update_log;
    }

    public void setUpdate_log(String update_log) {
        this.update_log = update_log;
    }

    public String getApk_url() {
        return apk_url;
    }

    public void setApk_url(String apk_url) {
        this.apk_url = apk_url;
    }

    public String getApk_url_code() {
        return apk_url_code;
    }

    public void setApk_url_code(String apk_url_code) {
        this.apk_url_code = apk_url_code;
    }
}
