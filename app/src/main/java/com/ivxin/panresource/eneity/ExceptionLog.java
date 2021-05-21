package com.ivxin.panresource.eneity;

import java.io.File;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class ExceptionLog extends BmobObject {
    BmobFile logFile;
    String log;

    public void setLogFile(BmobFile logFile) {
        this.logFile = logFile;
    }

    public BmobFile getLogFile() {
        return logFile;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getLog() {
        return log;
    }
}
