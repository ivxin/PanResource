package com.ivxin.panresource.eneity;

import java.io.Serializable;

public class DownloadConfigVO implements Serializable {
    public String qbtServer;
    public String qbtUser;
    public String qbtPass;
    public String ariaServer;
    public String ariaKey;
    public String miuServer;
    public String miuUser;
    public String miuPass;

    public String getQbtServer() {
        return qbtServer;
    }

    public void setQbtServer(String qbtServer) {
        this.qbtServer = qbtServer;
    }

    public String getQbtUser() {
        return qbtUser;
    }

    public void setQbtUser(String qbtUser) {
        this.qbtUser = qbtUser;
    }

    public String getQbtPass() {
        return qbtPass;
    }

    public void setQbtPass(String qbtPass) {
        this.qbtPass = qbtPass;
    }

    public String getAriaServer() {
        return ariaServer;
    }

    public void setAriaServer(String ariaServer) {
        this.ariaServer = ariaServer;
    }

    public String getAriaKey() {
        return ariaKey;
    }

    public void setAriaKey(String ariaKey) {
        this.ariaKey = ariaKey;
    }

    public String getMiuServer() {
        return miuServer;
    }

    public void setMiuServer(String miuServer) {
        this.miuServer = miuServer;
    }

    public String getMiuUser() {
        return miuUser;
    }

    public void setMiuUser(String miuUser) {
        this.miuUser = miuUser;
    }

    public String getMiuPass() {
        return miuPass;
    }

    public void setMiuPass(String miuPass) {
        this.miuPass = miuPass;
    }
}
