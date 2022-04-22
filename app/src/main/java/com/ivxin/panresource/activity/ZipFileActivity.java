package com.ivxin.panresource.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.ivxin.panresource.R;
import com.ivxin.panresource.base.BaseActivity;
import com.ivxin.panresource.base.Constant;
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
        binding.lv7zFile.setOnItemClickListener((parent, view, position, id) -> unZipFile(list.get(position).getPath()));
        Constant.zipFilePassword = sp.getString(Constant.SP_KEY_PASSWORD, "123");
        Constant.isOnly7z = sp.getBoolean(Constant.SP_KEY_ONLY7Z, false);
        binding.tvDefaultPassword.setText(String.format("设置解压密码:%s", Constant.zipFilePassword));
        binding.cbOnly7z.setChecked(Constant.isOnly7z);
        binding.cbOnly7z.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Constant.isOnly7z = isChecked;
            sp.edit().putBoolean(Constant.SP_KEY_ONLY7Z, isChecked).apply();
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
        runOnThread(() -> {
            try {
                final String outPutPath = zipFile.getParent() + "/" + zipFile.getName().substring(0, zipFile.getName().lastIndexOf("."));
                final int exitCode = Utils.unZipFile(zipFilePath, outPutPath, Constant.zipFilePassword);
                runOnUiThread(() -> {
                    switch (exitCode) {
                        case 0:
                        case 2:
                            toast("已解压到：\n" + outPutPath);
                            HashSet<String> outPutPathSet = (HashSet<String>) sp.getStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, new HashSet<>());
                            HashSet<String> pathSet = new HashSet<>(outPutPathSet);
                            pathSet.add(outPutPath);
                            sp.edit().putStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, pathSet).commit();
                            Intent intent = new Intent(ZipFileActivity.this, FolderExploreActivity.class);
                            intent.putExtra(FolderExploreActivity.KEY_PATH, outPutPath);
                            gotoOther(intent);
                            break;
                        default:
                            toast("解压缩失败");
                            break;
                    }

                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                hideLoadingDialog();
            }
        });
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
                runOnUiThread(() -> {
                    Collections.sort(list, (o1, o2) -> {
                        if (o1.lastModified() - o2.lastModified() == 0) return 0;
                        else
                            return o2.lastModified() - o1.lastModified() > 0 ? 1 : -1;
                    });
                    arrayAdapter.notifyDataSetChanged();
                    hideLoadingDialog();
                    toast("扫描结束 耗时: " + timeConsuming * 1.0 / 1000 + "秒");
                });
            }

            @Override
            public void scanningCallBack(@NotNull final File file) {
                runOnUiThread(() -> {
                    String[] ends=getResources().getStringArray(R.array.zip_extends);
                    for (String end : ends) {
                        if (file.getName().toLowerCase().endsWith(end)) {
                            list.add(file);
                            arrayAdapter.notifyDataSetChanged();
                            return;
                        }
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
            fileFilter.addCustomFilter((dir, name) -> name.endsWith(".7z"));
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        } else {
            editText.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        @SuppressLint("RestrictedApi") final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(editText, 50, 20, 50, 0)
                .setTitle("设置解压密码")
                .setPositiveButton("确定", (dialog1, which) -> {
                    Constant.zipFilePassword = editText.getText().toString();
                    sp.edit().putString(Constant.SP_KEY_PASSWORD, Constant.zipFilePassword).apply();
                }).create();
        dialog.show();
        editText.requestFocus();
        editText.setSelection(Constant.zipFilePassword.length());
        dialog.setOnDismissListener(dialog12 ->
                binding.tvDefaultPassword.setText(String.format("设置解压密码:%s", Constant.zipFilePassword)));
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
        runOnThread(() -> {
            HashSet<String> outPutPathSet = (HashSet<String>) sp.getStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, new HashSet<>());
            HashSet<String> clearFailSet = new HashSet<>();
            int outPutPathCount = outPutPathSet.size();
            int deleteCount = 0;
            for (String outPutPath : outPutPathSet) {
                boolean isSuccess = FileUtils.deleteDir(outPutPath);
                deleteCount += isSuccess ? 1 : 0;
                if (!isSuccess) {
                    clearFailSet.add(outPutPath);
                }
            }
            sp.edit().putStringSet(Constant.SP_KEY_OUTPUT_PATH_SET, clearFailSet).commit();
            int finalDeleteCount = deleteCount;
            runOnUiThread(() -> {
                hideLoadingDialog();
                toast(String.format(Locale.CHINA, "已清理解压的文件:%s/%s", finalDeleteCount, outPutPathCount));
                binding.tvClearFolder.setText(String.format(Locale.CHINA, "清理解压出的文件(%s)", outPutPathSet.size()));
                if (arrayAdapter.getCount() > 0) {
                    scanFile();
                }
            });
        });
    }
}
