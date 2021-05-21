package com.ivxin.panresource.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ivxin.panresource.R;
import com.ivxin.panresource.base.BaseActivity;
import com.ivxin.panresource.base.Constant;
import com.ivxin.panresource.base.OnActivityResultListener;
import com.ivxin.panresource.databinding.ActivityZipFileBinding;
import com.ivxin.panresource.utils.FileUtils;
import com.ivxin.panresource.utils.ScanFileUtil;
import com.ivxin.panresource.utils.Utils;
import com.ivxin.panresource.view.FileInfoItemView;
import com.ivxin.panresource.view.MyAdapter;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ZipFileActivity extends BaseActivity {
    private ActivityZipFileBinding binding;
    private final List<File> list = new ArrayList<>();
    private MyAdapter<FileInfoItemView, File> arrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityZipFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        arrayAdapter = new MyAdapter<>(this, list, FileInfoItemView.class, File.class);
        binding.lv7zFile.setAdapter(arrayAdapter);
        binding.lv7zFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                unZipFile(list.get(position).getPath());
            }
        });
        Constant.zipFilePassword = sp.getString(Constant.SP_KEY_PASSWORD, "123");
        Constant.isOnly7z = sp.getBoolean(Constant.SP_KEY_ONLY7Z, false);
        binding.tvDefaultPassword.setText(String.format("设置解压密码:%s", Constant.zipFilePassword));
        binding.cbOnly7z.setChecked(Constant.isOnly7z);
        binding.cbOnly7z.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Constant.isOnly7z = isChecked;
                sp.edit().putBoolean(Constant.SP_KEY_ONLY7Z, isChecked).apply();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        HashSet<String> outPutPathSet = (HashSet<String>) sp.getStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, new HashSet<>());
        binding.tvClearFolder.setText(String.format(Locale.CHINA, "清理解压出的文件(%s)", outPutPathSet.size()));
    }

    private void unZipFile(final String zipFilePath) {
        final File zipFile = new File(zipFilePath);
        if (!zipFile.exists() || zipFile.isDirectory() || !zipFile.canRead()) {
            toast("文件不存在");
            return;
        }
        showLoadingDialog("解压缩中...");
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
                                    toast("已解压到：\n" + outPutPath);
                                    HashSet<String> outPutPathSet = (HashSet<String>) sp.getStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, new HashSet<>());
                                    outPutPathSet.add(outPutPath);
                                    sp.edit().putStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, outPutPathSet).apply();
                                    Intent intent = new Intent(ZipFileActivity.this, FolderExploreActivity.class);
                                    intent.putExtra(FolderExploreActivity.KEY_PATH, outPutPath);
                                    gotoOther(intent);
                                    break;
                                case 2:
                                    toast("解压缩异常,可能密码错误,也可能解压完成,直接检查目录");
                                    break;
                                default:
                                    toast("解压缩失败");
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

    private void scanFile() {
        ScanFileUtil.ScanFileListener scanFileListener = new ScanFileUtil.ScanFileListener() {
            @Override
            public void scanBegin() {
                list.clear();
                showLoadingDialog("扫描中...");
            }

            @Override
            public void scanComplete(final long timeConsuming) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Collections.sort(list, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                if (o1.lastModified() - o2.lastModified() == 0) return 0;
                                else
                                    return o2.lastModified() - o1.lastModified() > 0 ? 1 : -1;
                            }
                        });
                        arrayAdapter.notifyDataSetChanged();
                        hideLoadingDialog();
                        toast("扫描结束 耗时: " + timeConsuming * 1.0 / 1000 + "秒");
                    }
                });
            }

            @Override
            public void scanningCallBack(@NotNull final File file) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!file.getName().toLowerCase().endsWith(".7z")) {
                            return;
                        }
                        list.add(file);
                        arrayAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        ScanFileUtil scanFileUtil = new ScanFileUtil(Environment.getExternalStorageDirectory().getAbsolutePath(), scanFileListener);
        ScanFileUtil.FileFilterBuilder fileFilter = new ScanFileUtil.FileFilterBuilder();
        fileFilter.notScanHiddenFiles();
        fileFilter.onlyScanFile();
        fileFilter.scanZipFiles();
        if (Constant.isOnly7z) {
            fileFilter.addCustomFilter(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".7z");
                }
            });
        }
        scanFileUtil.setCallBackFilter(fileFilter.build());
        scanFileUtil.startAsyncScan();
    }

    public void reScanZipFile(View view) {
        scanFile();
    }

    public void changeDefaultPassword(View view) {
        final EditText editText = new EditText(this);
        editText.setPadding(50, 30, 30, 50);
        editText.setBackgroundResource(R.drawable.bg_content_box);
        editText.setText(Constant.zipFilePassword);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(editText).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Constant.zipFilePassword = editText.getText().toString();
                sp.edit().putString(Constant.SP_KEY_PASSWORD, Constant.zipFilePassword).apply();
            }
        }).setTitle("设置解压密码").create();
        dialog.show();
        editText.requestFocus();
        editText.setSelection(Constant.zipFilePassword.length());
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                binding.tvDefaultPassword.setText(String.format("设置解压密码:%s", Constant.zipFilePassword));
            }
        });
        QMUIKeyboardHelper.showKeyboard(editText, true);
    }

    public void exploreFolder(View view) {
        File baiduFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "BaiduNetdisk");
        String folder = baiduFolder.exists() ? baiduFolder.getAbsolutePath() : Environment.getExternalStorageDirectory().getAbsolutePath();
        Intent intent = new Intent(this, FolderExploreActivity.class);
        intent.putExtra(FolderExploreActivity.KEY_PATH, folder);
        gotoOther(intent);
    }

    public void clearUnzippedFiles(View view) {
        showLoadingDialog("正在清理");
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashSet<String> outPutPathSet = (HashSet<String>) sp.getStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, new HashSet<>());
                int outPutPathCount = outPutPathSet.size();
                int deleteCount = 0;
                for (String outPutPath : outPutPathSet) {
                    deleteCount += FileUtils.deleteDir(outPutPath) ? 1 : 0;
                }
                outPutPathSet.clear();
                sp.edit().putStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, outPutPathSet).apply();
                int finalDeleteCount = deleteCount;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoadingDialog();
                        toast(String.format(Locale.CHINA, "已清理解压的文件:%s/%s", finalDeleteCount, outPutPathCount));
                        binding.tvClearFolder.setText(String.format(Locale.CHINA, "清理解压出的文件(%s)", outPutPathSet.size()));
                    }
                });
            }
        }).start();
    }
}
