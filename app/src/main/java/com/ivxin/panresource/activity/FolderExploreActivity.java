package com.ivxin.panresource.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.Nullable;

import com.draggable.library.extension.ImageViewerHelper;
import com.ivxin.panresource.base.BaseActivity;
import com.ivxin.panresource.base.Constant;
import com.ivxin.panresource.databinding.ActivityFolderExploreBinding;
import com.ivxin.panresource.utils.FileUtils;
import com.ivxin.panresource.utils.Utils;
import com.ivxin.panresource.view.FileInfoItemView;
import com.ivxin.panresource.view.MyAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FolderExploreActivity extends BaseActivity {
    public static final String KEY_PATH = "KEY_PATH";
    public static String path = "";
    private ActivityFolderExploreBinding binding;
    private List<File> fileList = new ArrayList<>();
    private MyAdapter<FileInfoItemView, File> arrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFolderExploreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getIntent() != null) {
            path = getIntent().getStringExtra(KEY_PATH);
        }

        if (TextUtils.isEmpty(path) || !new File(path).isDirectory()) {
            toast("dir error");
            finish();
        }
        binding.tvDir.setText(path);
        arrayAdapter = new MyAdapter<>(this, fileList, FileInfoItemView.class, File.class);
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
            Collections.addAll(fileList, files);
        }
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isFile() && o2.isDirectory()) {
                    return 1;
                } else if (o1.isDirectory() && o2.isFile()) {
                    return -1;
                } else if (o1.isFile() && o2.isFile()) {
                    return o2.getName().compareTo(o1.getName());
                } else if (o1.isDirectory() && o2.isDirectory()) {
                    return o2.getName().compareTo(o1.getName());
                } else
                    return 0;
            }
        });
        binding.lvFiles.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
        binding.lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final File file = fileList.get(position);
                String path = file.getPath().toLowerCase();
                if (file.isFile()) {
                    String extend = FileUtils.getFileExtension(path);
                    switch (extend) {
                        case "jpg":
                        case "jpeg":
                        case "png":
                        case "bmp":
                        case "webp":
                            showImageViewDialog(file);
                            break;
                        case "7z":
                        case "zip":
                        case "rar":
                            unZipFile(file.getPath());
                            break;
                        case "torrent":
                            //upload this file to bittorrent client :aria2 or qbittorrent...
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    final String result = BitTorrentUtil.postTorrentFile(file);
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            toast(result);
//                                        }
//                                    });
//                                }
//                            }).start();
//                            break;
                        default:
                            FileUtils.openFile(getApplicationContext(), file);
                            break;
                    }
                } else if (file.isDirectory()) {
                    Intent intent = new Intent(FolderExploreActivity.this, FolderExploreActivity.class);
                    intent.putExtra(FolderExploreActivity.KEY_PATH, file.getPath());
                    gotoOther(intent);
                } else {
                    toast("It's a file.");
                }

            }
        });
    }

    private void showImageViewDialog(File file) {
        List<String> imags = new ArrayList<>();
        for (File ifImageFile : fileList) {
            String[] extensions = new String[]{"jpg", "jpeg", "png", "webp", "bmp"};
            for (String ext : extensions) {
                if (FileUtils.getFileExtension(file).toLowerCase().endsWith(ext)) {
                    imags.add(ifImageFile.getPath());
                    break;
                }
            }
        }
        int index = 0;
        for (int i = 0; i < imags.size(); i++) {
            if (imags.get(i).equals(file.getPath())) {
                index = i;
                break;
            }
        }
//        ImageViewerHelper.ImageInfo imageInfo = new ImageViewerHelper.ImageInfo(file.getPath(), file.getPath(), file.length());
        ImageViewerHelper.INSTANCE.showImages(this, imags, index, false);
    }

    private void unZipFile(final String zipFilePath) {
        final File zipFile = new File(zipFilePath);
        if (!zipFile.exists() || zipFile.isDirectory() || !zipFile.canRead()) {
            toast("文件不存在");
            return;
        }
        showLoadingDialog("unzipping...");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String outPutPath = zipFile.getParent() + "/" + zipFile.getName().substring(0, zipFile.getName().lastIndexOf("."));
                    final int exitCode = Utils.unZipFile(zipFilePath, outPutPath, Constant.zipFilePassword);
                    Log.d("unZipFile", "run: " + outPutPath);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (exitCode) {
                                case 0:
                                    toast("unzip to " + outPutPath);
                                    Intent intent = new Intent(FolderExploreActivity.this, FolderExploreActivity.class);
                                    intent.putExtra(FolderExploreActivity.KEY_PATH, outPutPath);
                                    gotoOther(intent);
                                    break;
                                case 2:
                                    toast("wrong password");
                                    break;
                                default:
                                    toast("unzip fail");
                                    break;
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    hideLoadingDialog();
                }
            }
        });
        thread.start();
    }
}
