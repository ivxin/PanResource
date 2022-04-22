package com.ivxin.panresource.utils;

import org.eclipse.ecf.protocol.bittorrent.TorrentFile;

import java.io.File;
import java.io.IOException;

public class TorrentFile2MagnetLink {
    public static String convert(File file) {
        try {
            TorrentFile torrentFile = new TorrentFile(file);
            return String.format("magnet:?xt=urn:btih:%s&dn=%s", torrentFile.getHexHash(), torrentFile.getName());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
