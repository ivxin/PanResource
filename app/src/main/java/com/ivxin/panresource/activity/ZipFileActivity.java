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
import com.ivxin.panresource.databinding.ActivityZipFileBinding;
import com.ivxin.panresource.utils.ScanFileUtil;
import com.ivxin.panresource.utils.Utils;
import com.ivxin.panresource.view.FileInfoItemView;
import com.ivxin.panresource.view.MyAdapter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        requestPermission();
        binding.lv7zFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                unZipFile(list.get(position).getPath());
            }
        });
        Constant.zipFilePassword = sp.getString(Constant.SP_KEY_PASSWORD, "123");
        Constant.isOnly7z = sp.getBoolean(Constant.SP_KEY_ONLY7Z, false);
        binding.tvDefaultPassword.setText(String.format("password:%s", Constant.zipFilePassword));
        binding.cbOnly7z.setChecked(Constant.isOnly7z);
        binding.cbOnly7z.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Constant.isOnly7z = isChecked;
                sp.edit().putBoolean(Constant.SP_KEY_ONLY7Z, isChecked).apply();
            }
        });
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
                                    Intent intent = new Intent(ZipFileActivity.this, FolderExploreActivity.class);
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


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                scanFile();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
            }
        } else {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                scanFile();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                scanFile();
            } else {
                toast("存储权限获取失败");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                scanFile();
            } else {
                toast("存储权限获取失败");
            }
        }
    }

    private void scanFile() {
        ScanFileUtil.ScanFileListener scanFileListener = new ScanFileUtil.ScanFileListener() {
            @Override
            public void scanBegin() {
                list.clear();
                showLoadingDialog("scanning...");
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
                        toast("scan finished. cost " + timeConsuming * 1.0 / 1000 + "s");
                    }
                });
            }

            @Override
            public void scanningCallBack(@NotNull final File file) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.add(file);
                        arrayAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        ScanFileUtil scanFileUtil = new ScanFileUtil(ScanFileUtil.Companion.getExternalStorageDirectory(), scanFileListener);
        ScanFileUtil.FileFilterBuilder fileFilter = new ScanFileUtil.FileFilterBuilder();
        fileFilter.notScanHiddenFiles();
        fileFilter.onlyScanFile();
        fileFilter.scanZipFiles();
//        if (Constant.isOnly7z) {
            fileFilter.addCustomFilter(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".7z");
                }
            });
//        }
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
        }).create();
        dialog.show();
        editText.requestFocus();
        editText.setSelection(Constant.zipFilePassword.length());
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                binding.tvDefaultPassword.setText(String.format("password:%s", Constant.zipFilePassword));
            }
        });
    }
}
