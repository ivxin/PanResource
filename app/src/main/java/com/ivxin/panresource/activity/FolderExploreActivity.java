package com.ivxin.panresource.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.draggable.library.extension.ImageViewerHelper;
import com.google.gson.reflect.TypeToken;
import com.ivxin.panresource.R;
import com.ivxin.panresource.base.BaseActivity;
import com.ivxin.panresource.base.Constant;
import com.ivxin.panresource.databinding.ActivityFolderExploreBinding;
import com.ivxin.panresource.databinding.LayoutDownloadConfigDialogBinding;
import com.ivxin.panresource.databinding.LayoutDownloadToolSelectDialogBinding;
import com.ivxin.panresource.eneity.DownloadConfigVO;
import com.ivxin.panresource.utils.Aria2DownloadUtil;
import com.ivxin.panresource.utils.DefaultQMUILinkTextViewOnLinkClickListener;
import com.ivxin.panresource.utils.QBitTorrentUtil;
import com.ivxin.panresource.utils.FileUtils;
import com.ivxin.panresource.utils.TorrentFile2MagnetLink;
import com.ivxin.panresource.utils.Utils;
import com.ivxin.panresource.view.FileInfoItemView;
import com.ivxin.panresource.view.MyAdapter;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.widget.textview.QMUILinkTextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class FolderExploreActivity extends BaseActivity {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    public static String rootPath;
    public static final String KEY_PATH = "KEY_PATH";
    public String path = "";
    private ActivityFolderExploreBinding binding;
    private final List<File> fileList = new ArrayList<>();
    private MyAdapter<FileInfoItemView, File> arrayAdapter;
    private boolean isStillLoading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFolderExploreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        arrayAdapter = new MyAdapter<>(this, fileList, FileInfoItemView.class, File.class);
        binding.lvFiles.setAdapter(arrayAdapter);
        binding.lvFiles.setOnItemLongClickListener((parent, view, position, id) -> {
            String filePath = fileList.get(position).getPath();
            showFileOperDialog(filePath);
            return true;
        });
        binding.lvFiles.setOnItemClickListener((parent, view, position, id) -> {
            final File file = fileList.get(position);
            openFile(file);
        });
        rootPath = readPath(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        readPath(intent);
    }

    private String readPath(Intent intent) {
        if (intent != null) {
            path = intent.getStringExtra(KEY_PATH);
            if (TextUtils.isEmpty(path) || !new File(path).isDirectory()) {
                toast("路径不正确");
                loadParentPath();
            } else {
                readFolder(null);
            }
        } else {
            toast("路径不正确");
            finish();
        }
        return path;
    }

    private void loadParentPath() {
        String parentPath = new File(path).getParent();
        Intent intent = new Intent(FolderExploreActivity.this, FolderExploreActivity.class);
        intent.putExtra(FolderExploreActivity.KEY_PATH, parentPath);
        gotoOther(intent);
    }

    private void openFile(File file) {
        String newPath = file.getPath().toLowerCase();
        if (file.isFile()) {
            String extend = FileUtils.getFileExtension(newPath);
            boolean canOpen = false;

            String[] imageExtents = getResources().getStringArray(R.array.image_extends);
            for (String imageExtent : imageExtents) {
                if (imageExtent.endsWith(extend)) {
                    showImageViewDialog(file);
                    canOpen = true;
                    break;
                }
            }

            String[] zipExtents = getResources().getStringArray(R.array.zip_extends);
            for (String zipExtent : zipExtents) {
                if (zipExtent.endsWith(extend)) {
                    unZipFile(file.getPath());
                    canOpen = true;
                    break;
                }
            }

            String[] textExtents = getResources().getStringArray(R.array.text_extends);
            for (String textExtent : textExtents) {
                if (textExtent.endsWith(extend)) {
                    Intent intent = new Intent(this, TextReaderActivity.class);
                    intent.putExtra(TextReaderActivity.FILE_NAME, file.getAbsolutePath());
                    gotoOther(intent);
                    canOpen = true;
                    break;
                }
            }
            if ("torrent".equals(extend)) {
                showDownloadToolSelectDialog(file);
                canOpen = true;
            }
            if (!canOpen) {
                FileUtils.openFile(getApplicationContext(), file);
            }
        } else if (file.isDirectory()) {
            Intent intent = new Intent(FolderExploreActivity.this, FolderExploreActivity.class);
            intent.putExtra(FolderExploreActivity.KEY_PATH, file.getPath());
            gotoOther(intent);
        } else {
            toast("It's not a file or folder.");
        }
    }

    private void showDownloadToolSelectDialog(File file) {
        LayoutDownloadToolSelectDialogBinding binding = LayoutDownloadToolSelectDialogBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setTitle("选择下载工具").setView(binding.getRoot()).create();
        dialog.show();
        binding.btnDefault.setOnClickListener(v -> {
            dialog.dismiss();
            FileUtils.openFile(getApplicationContext(), file);
        });
        binding.btnOpenMagnet.setOnClickListener(v -> {
            dialog.dismiss();
            String magnetLink = TorrentFile2MagnetLink.convert(file);
            if (TextUtils.isEmpty(magnetLink)) {
                toast("获取磁链失败");
            } else {
                Utils.openUrlWithOtherApp(FolderExploreActivity.this, magnetLink, false);
            }
        });
        binding.btnCopyMagnet.setOnClickListener(v -> {
            dialog.dismiss();
            String magnetLink = TorrentFile2MagnetLink.convert(file);
            if (TextUtils.isEmpty(magnetLink)) {
                toast("获取磁链失败");
            } else {
                Utils.putTextIntoClipBoard(getApplicationContext(), "", magnetLink);
                toast("复制了磁链\n" + magnetLink);
            }
        });
        binding.btnQBittorrent.setOnClickListener(v -> {
            dialog.dismiss();
            uploadToQBittorrent(file);
        });
        binding.btnAria2.setOnClickListener(v -> {
            dialog.dismiss();
            uploadToAria2(file);
        });
        binding.btnMiuTorrent.setOnClickListener(v -> {
            dialog.dismiss();
            uploadToMiuTorrent(file);
        });
    }

    private void uploadToMiuTorrent(File file) {
        toast("未实现");
    }

    private void uploadToAria2(File file) {
        showLoadingDialog("上传中...");
        runOnThread(() -> {
//            String magnetLink = TorrentFile2MagnetLink.convert(file);
//                DebugUtil.LogLog(true, "file:", file.getAbsolutePath());
//                DebugUtil.LogLog(true, "magnetLink:", magnetLink);
            final String result = new Aria2DownloadUtil(FolderExploreActivity.this).postTorrentFiles(file);
            runOnUiThread(() -> {
                hideLoadingDialog();
                toast(result);
            });

        });
    }

    private void uploadToQBittorrent(File file) {
        showLoadingDialog("上传中...");
        runOnThread(() -> {
            final String result = new QBitTorrentUtil(FolderExploreActivity.this).postTorrentFile(file);
            runOnUiThread(() -> {
                hideLoadingDialog();
                toast(result);
            });
        });
    }

    private void showFileOperDialog(String filePath) {
        String[] options = new String[]{"打开...", "分享", "重命名", "删除", "复制路径", "属性"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(FolderExploreActivity.this, android.R.layout.simple_list_item_1, options);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ListView listView = new ListView(this);
        listView.setAdapter(arrayAdapter);
        AlertDialog alertDialog = builder.setView(listView).create();
        alertDialog.show();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            alertDialog.dismiss();
            File operationFile = new File(filePath);
            String operation = options[position];
            switch (operation) {
                case "打开...":
                    if (operationFile.isFile()) {
                        FileUtils.openFile(getApplicationContext(), operationFile);
                    } else {
                        openFile(operationFile);
                    }
                    break;
                case "分享":
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    //Android7.0版本以上使用FileProvider
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        shareIntent.putExtra(Intent.EXTRA_STREAM,
                                FileProvider.getUriForFile(FolderExploreActivity.this,
                                        getPackageName() + ".FileProvider", operationFile));
                    } else {
                        shareIntent.putExtra(Intent.EXTRA_STREAM, new File(filePath));
                    }
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.setType("*/*");//此处可发送多种文件
                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//这一句一定得写
                    startActivity(Intent.createChooser(shareIntent, "分享文件"));
                    break;
                case "重命名":
                    showFileRenameDialog(operationFile);
                    break;
                case "删除":
                    if (operationFile.isFile()) {
                        FileUtils.deleteFile(operationFile);
                    } else {
                        FileUtils.deleteDir(operationFile);
                    }
                    readFolder(null);
                    break;
                case "复制路径":
                    Utils.putTextIntoClipBoard(getApplicationContext(), "", filePath);
                    toast("复制了文件地址\n" + filePath);
                    break;
                case "属性":
                    showFileDetailDialog(operationFile);
                    break;
                default:
                    break;
            }
        });
    }

    private void showFileDetailDialog(File operationFile) {
        runOnUiThreadDelay(() -> {
            if (isStillLoading) {
                showLoadingDialog("加载中...");
            }
        }, 300);

        runOnThread(() -> {
            isStillLoading = true;
            String type = "";
            if (operationFile.isFile()) {
                type = FileUtils.getFileExtension(operationFile);
            } else {
                type = "文件夹";
            }
            final String content = String.format(Locale.CHINA, getString(R.string.content_template),
                    FileUtils.getFileNameNoExtension(operationFile),
                    type,
                    operationFile.getParent(),
                    operationFile.canRead() + "",
                    operationFile.canWrite() + "",
                    operationFile.canExecute() + "",
                    FileUtils.getFileMD5ToString(operationFile),
                    FileUtils.getFileSize(operationFile),
                    simpleDateFormat.format(operationFile.lastModified())
            );
            isStillLoading = false;
            runOnUiThread(() -> {
                hideLoadingDialog();
                showTipDialog(content);
            });
        });
    }

    private void showFileRenameDialog(File operationFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        FrameLayout frameLayout = new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
        layoutParams.bottomMargin = layoutParams.topMargin = layoutParams.leftMargin = layoutParams.rightMargin = 10;
        EditText editText = new EditText(this);
        editText.setLayoutParams(layoutParams);
        editText.setPadding(50, 30, 30, 50);
        editText.setBackgroundResource(R.drawable.bg_content_box);
        editText.setText(operationFile.getName());
        editText.setSelection(operationFile.getName().length());
        frameLayout.addView(editText);
        AlertDialog alertDialog = builder.setView(frameLayout).setTitle("文件重命名").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (operationFile.renameTo(new File(operationFile.getParent(), editText.getText().toString().trim()))) {
                        toast("重命名完成");
                    } else {
                        toast("重命名失败");
                    }
                } catch (Exception e) {
                    toast("重命名失败");
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        }).create();
        alertDialog.show();
        QMUIKeyboardHelper.showKeyboard(editText, true);
        alertDialog.setOnDismissListener(dialog -> readFolder(null));
    }

    public void readFolder(View view) {
        binding.tvDir.setText(path);
        fileList.clear();
        arrayAdapter.notifyDataSetChanged();
        runOnUiThreadDelay(() -> {
            if (isStillLoading) {
                showLoadingDialog("加载中..");
            }
        }, 300);
        runOnThread(() -> {
            isStillLoading = true;
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
            isStillLoading = false;
            runOnUiThread(() -> {
                arrayAdapter.notifyDataSetChanged();
                hideLoadingDialog();
            });
        });

    }

    @Override
    public void onBackPressed() {
        if (path.equals(rootPath)) {
            super.onBackPressed();
        } else {
            loadParentPath();
        }
    }

    private void showImageViewDialog(File file) {
        List<String> imags = new ArrayList<>();
        for (File ifImageFile : fileList) {
            String[] extensions = new String[]{"jpg", "jpeg", "png", "webp", "bmp", "heic"};
            for (String ext : extensions) {
                if (FileUtils.getFileExtension(ifImageFile).toLowerCase().endsWith(ext)) {
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
        runOnThread(() -> {
            try {
                final String outPutPath = zipFile.getParent() + "/" + zipFile.getName().substring(0, zipFile.getName().lastIndexOf("."));
                final int exitCode = Utils.unZipFile(zipFilePath, outPutPath, Constant.zipFilePassword);
                Log.d("unZipFile", "run: " + outPutPath);
                runOnUiThread(new Runnable() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void run() {
                        switch (exitCode) {
                            case 0:
                                toast("已解压到：" + outPutPath);
                                HashSet<String> outPutPathSet = (HashSet<String>) sp.getStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, new HashSet<>());
                                HashSet<String> pathSet = new HashSet<>(outPutPathSet);
                                pathSet.add(outPutPath);
                                sp.edit().putStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, pathSet).commit();
                                Intent intent = new Intent(FolderExploreActivity.this, FolderExploreActivity.class);
                                intent.putExtra(FolderExploreActivity.KEY_PATH, outPutPath);
                                gotoOther(intent);
                                break;
                            case 2:
                                toast("解压缩异常,可能密码错误,也可能解压完成,直接检查目录");
                                readFolder(null);
                                break;
                            default:
                                toast("解压缩失败");
                                readFolder(null);
                                break;
                        }

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                hideLoadingDialog();
            }
        });
    }

    public void downloadConfig(View view) {
        LayoutDownloadConfigDialogBinding binding = LayoutDownloadConfigDialogBinding.inflate(getLayoutInflater());
        QMUILinkTextView textView = binding.tvQbittorrent;
        textView.setOnLinkClickListener(new DefaultQMUILinkTextViewOnLinkClickListener(this));
        String config = sp.getString(Constant.SP_KEY_DOWNLOAD_SERVER_CONFIG, "");
        if (!TextUtils.isEmpty(config)) {
            DownloadConfigVO downloadConfigVO = Constant.GSON.fromJson(config, new TypeToken<DownloadConfigVO>() {
            }.getType());
            binding.etQbtServer.setText(downloadConfigVO.getQbtServer());
            binding.etQbtUser.setText(downloadConfigVO.getQbtUser());
            binding.etQbtPass.setText(downloadConfigVO.getQbtPass());
            binding.etAriaServer.setText(downloadConfigVO.getAriaServer());
            binding.etAriaKey.setText(downloadConfigVO.getAriaKey());
            binding.etMiuServer.setText(downloadConfigVO.getMiuServer());
            binding.etMiuUser.setText(downloadConfigVO.getMiuUser());
            binding.etMiuPass.setText(downloadConfigVO.getMiuPass());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder
                .setTitle("远程下载配置")
                .setView(binding.getRoot())
                .setPositiveButton("保存", (dialog1, which) -> {
                    dialog1.dismiss();
                    DownloadConfigVO downloadConfigVO = new DownloadConfigVO();
                    downloadConfigVO.setQbtServer(binding.etQbtServer.getText().toString().trim());
                    downloadConfigVO.setQbtUser(binding.etQbtUser.getText().toString().trim());
                    downloadConfigVO.setQbtPass(binding.etQbtPass.getText().toString().trim());
                    downloadConfigVO.setAriaServer(binding.etAriaServer.getText().toString().trim());
                    downloadConfigVO.setAriaKey(binding.etAriaKey.getText().toString().trim());
                    downloadConfigVO.setMiuServer(binding.etMiuServer.getText().toString().trim());
                    downloadConfigVO.setMiuUser(binding.etMiuUser.getText().toString().trim());
                    downloadConfigVO.setMiuPass(binding.etMiuPass.getText().toString().trim());
                    sp.edit().putString(Constant.SP_KEY_DOWNLOAD_SERVER_CONFIG, Constant.GSON.toJson(downloadConfigVO)).apply();
                }).create();
        dialog.show();
    }
}
