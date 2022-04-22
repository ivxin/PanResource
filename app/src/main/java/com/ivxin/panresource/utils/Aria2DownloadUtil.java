package com.ivxin.panresource.utils;

import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.reflect.TypeToken;
import com.ivxin.panresource.base.BaseActivity;
import com.ivxin.panresource.base.Constant;
import com.ivxin.panresource.eneity.DownloadConfigVO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Aria2DownloadUtil {
    public static final String ARIA2_ADD_TORRENT = "aria2.addTorrent";
    public static final String ARIA2_ADD_METALINK = "aria2.addMetalink";
    public static final String ARIA2_ADD_URI = "aria2.addUri";

    private final String aria2_url;
    private final String token;

    public Aria2DownloadUtil(BaseActivity activity) {
        String config = activity.sp.getString(Constant.SP_KEY_DOWNLOAD_SERVER_CONFIG, "");
        DownloadConfigVO downloadConfigVO = Constant.GSON.fromJson(config, new TypeToken<DownloadConfigVO>() {
        }.getType());
        aria2_url = downloadConfigVO.getAriaServer();
        token = downloadConfigVO.getAriaKey();
    }

    public static class Aria2Params {
        private String id;
        private String jsonrpc;
        private String method;
        private List<Object> params;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getJsonrpc() {
            return jsonrpc;
        }

        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<Object> getParams() {
            return params;
        }

        public void setParams(List<Object> params) {
            this.params = params;
        }

    }

    public String postTorrentFiles(File... torrentFiles) {
        if (TextUtils.isEmpty(aria2_url)) {
            return "请先配置服务器";
        }
        String[] encodedTorrents = new String[torrentFiles.length];
        for (int i = 0; i < torrentFiles.length; i++) {
            File file = torrentFiles[i];
            if (file != null && file.exists() && file.isFile()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] bytes = new byte[0];
                    byte[] tempbytes = new byte[0xff];
                    int byteread = 0;
                    // 读入多个字节到字节数组中，byteread为一次读入的字节数
                    while ((byteread = fileInputStream.read(tempbytes)) != -1) {
                        byte[] combine = new byte[bytes.length + byteread];
                        System.arraycopy(bytes, 0, combine, 0, bytes.length);
                        System.arraycopy(tempbytes, 0, combine, bytes.length, byteread);
                        bytes = combine;
                    }
                    byte[] encodedTorrentFile = Base64.encode(bytes, Base64.URL_SAFE);
                    encodedTorrents[i] = new String(encodedTorrentFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Aria2Params aria2Params = new Aria2Params();
        aria2Params.setId("requestCode");
        aria2Params.setJsonrpc("2.0");
        aria2Params.setMethod(ARIA2_ADD_TORRENT);
        List<Object> params = new ArrayList<>();
        params.add(token);
        params.add(encodedTorrents);
        aria2Params.setParams(params);
        return post(Constant.GSON.toJson(aria2Params));
    }

    public String postMetalinks(String... metalinks) {
        if (TextUtils.isEmpty(aria2_url)) {
            return "请先配置服务器";
        }
//        for (int i = 0; i < metalinks.length; i++) {
//            metalinks[i] = new String(Base64.encode(metalinks[i].getBytes(), Base64.URL_SAFE));
//        }
        Aria2Params aria2Params = new Aria2Params();
        aria2Params.setId("requestCode");
        aria2Params.setJsonrpc("2.0");
        aria2Params.setMethod(ARIA2_ADD_METALINK);
        List<Object> params = new ArrayList<>();
        params.add(token);
        params.add(metalinks);
        aria2Params.setParams(params);
        return post(Constant.GSON.toJson(aria2Params));
    }

    public String postUri(String... uris) {
        if (TextUtils.isEmpty(aria2_url)) {
            return "请先配置服务器";
        }
//        for (int i = 0; i < uris.length; i++) {
//            uris[i] = new String(Base64.encode(uris[i].getBytes(), Base64.URL_SAFE));
//        }
        Aria2Params aria2Params = new Aria2Params();
        aria2Params.setId("requestCode");
        aria2Params.setJsonrpc("2.0");
        aria2Params.setMethod(ARIA2_ADD_URI);
        List<Object> params = new ArrayList<>();
        params.add(token);
        params.add(uris);
        aria2Params.setParams(params);
        return post(Constant.GSON.toJson(aria2Params));
    }

    private String post(String json) {
        json = json.replace("\"" + token + "\",", "\"token\":\"" + token + "\",");
        DebugUtil.LogLog(true, "post", json);
        return HttpConnectionUtils.httpURLconnectionPOST(aria2_url, json);
    }

}
