package com.ivxin.panresource.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.ivxin.panresource.activity.MainActivity;
import com.ivxin.panresource.base.BaseActivity;
import com.ivxin.panresource.base.Constant;
import com.ivxin.panresource.base.OnActivityResultListener;
import com.ivxin.panresource.base.OnPermissionCheckedListener;
import com.ivxin.panresource.databinding.LayoutUpdateDialogBinding;
import com.ivxin.panresource.eneity.ApkFile;
import com.qmuiteam.qmui.widget.QMUIProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.BmobDialogButtonListener;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;

public class UpdateManager {
    private BaseActivity activity;
    private boolean interceptFlag = false;

    public UpdateManager(BaseActivity activity) {
        this.activity = activity;
    }

    public void checkUpdate() {
        BmobUpdateAgent.setUpdateOnlyWifi(false);
        BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
            /**
             * UpdateStatus.TimeOut    =-1：查询出错或超时
             * UpdateStatus.Yes        = 0：有更新
             * UpdateStatus.No         = 1：没有更新
             * UpdateStatus.IGNORED    = 3：该版本已被忽略更新
             * UpdateStatus.EmptyField = 2：字段值为空，请检查以下内容：
             *                             1)、是否已填写target_size目标apk大小（以字节为单位）；
             *                             2)、path或者android_url两者是否必填其中一项（若两者都填写，则默认下载path字段下的apk文件）
             * UpdateStatus.ErrorSizeFormat = 4：请检查target_size填写的格式，请使用file.length()方法获取apk大小
             * UpdateStatus.Update     =6： 代表点击的是“立即更新”
             * UpdateStatus.NotNow     =7： 代表点击的是“以后再说”
             * UpdateStatus.Close      =8： 代表关闭对话框-->只有在强制更新状态下才会在更新对话框的右上方出现close按钮,
             *                              如果用户不点击”立即更新“按钮，这时候开发者可做些操作，比如直接退出应用等
             * @param updateStatus
             * @param updateInfo
             */
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                // TODO Auto-generated method stub
                if (updateStatus == UpdateStatus.Yes) {//版本有更新

                } else if (updateStatus == UpdateStatus.No) {
                    activity.toast("版本无更新");
                } else if (updateStatus == UpdateStatus.EmptyField) {//此提示只是提醒开发者关注那些必填项，测试成功后，无需对用户提示
                    activity.toast("请检查你AppVersion表的必填项，1、target_size（文件大小）是否填写；2、path或者android_url两者必填其中一项。");
                } else if (updateStatus == UpdateStatus.IGNORED) {
                    activity.toast("该版本已被忽略更新");
                } else if (updateStatus == UpdateStatus.ErrorSizeFormat) {
                    activity.toast("请检查target_size填写的格式，请使用file.length()方法获取apk大小。");
                } else if (updateStatus == UpdateStatus.TimeOut) {
                    activity.toast("查询出错或查询超时");
                }
            }
        });
        BmobUpdateAgent.setDialogListener(new BmobDialogButtonListener() {
            @Override
            public void onClick(int status) {
                switch (status) {
                    case UpdateStatus.Update:
                        activity.toast("点击了立即更新按钮");
                        break;
                    case UpdateStatus.NotNow:
                        activity.toast("点击了以后再说按钮");
                        break;
                    case UpdateStatus.Close://只有在强制更新状态下才会在更新对话框的右上方出现close按钮,如果用户不点击”立即更新“按钮，这时候开发者可做些操作，比如直接退出应用等
                        activity.toast("点击了对话框关闭按钮");
                        break;
                }
            }
        });
        BmobUpdateAgent.update(activity);
//        BmobQuery<ApkFile> bmobQuery = new BmobQuery<>();
//        bmobQuery.setLimit(8).order("-createdAt").findObjects(new FindListener<ApkFile>() {
//            @Override
//            public void done(List<ApkFile> list, BmobException e) {
//                if (e == null) {
//                    if (list.size() > 0) {
//                        ApkFile apkFile = list.get(0);
//                        if (apkFile.getVersion_i() > Constant.version) {
//                            showUpdateDialog(apkFile);
//                        } else {
//                            activity.toast("没有新版本");
//                        }
//                    }
//                } else {
//                    Utils.printLog(true, "BmobException", e.toString());
//                }
//            }
//        });
    }

    private void showUpdateDialog(ApkFile apkFile) {
        final LayoutUpdateDialogBinding binding = LayoutUpdateDialogBinding.inflate(LayoutInflater.from(activity));
        binding.pbUpdate.setQMUIProgressBarTextGenerator(new QMUIProgressBar.QMUIProgressBarTextGenerator() {
            @Override
            public String generateText(QMUIProgressBar progressBar, int value, int maxValue) {
                return String.format(Locale.CHINA, "%s％", value);
            }
        });
        binding.tvUpdateLog.setText(String.format(Locale.CHINA, "新版本v%s\n\n%s\n\n下载地址:\n%s  (访问密码：%s)",
                apkFile.getVersion(), apkFile.getUpdate_log(), apkFile.getApk_url(), apkFile.getApk_url_code()));
        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setView(binding.getRoot()).create();
        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        binding.btnCopyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.putTextIntoClipBoard(activity, "", String.format(Locale.CHINA, "%s  (访问密码：%s)", apkFile.getApk_url(), apkFile.getApk_url_code()));
                activity.toast("链接已复制");
                alertDialog.dismiss();
            }
        });
        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.toast("已复制文件访问密码");
                Utils.putTextIntoClipBoard(activity, "", apkFile.getApk_url_code());
                Utils.openUrlWithOtherApp(activity, apkFile.getApk_url(), false);
//                BmobFile bmobFile = apkFile.getApk_file();
//                binding.pbUpdate.setVisibility(View.VISIBLE);
//                new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        try {
//                            String savePath;
//                            URL url = new URL(apkFile.getApk_url());
//                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                            conn.setRequestProperty("Accept-Encoding", "identity");
//                            conn.connect();
//                            int length = conn.getContentLength();
//                            InputStream is = conn.getInputStream();
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                savePath = activity.getFilesDir() + "/Download/";//7.0以上储存到app内部
//                            } else {//7.0以下储存到外部
//                                savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Download/";
//                            }
//                            File file = new File(savePath);
//                            if (!file.exists()) {
//                                file.mkdirs();
//                            }
//
//                            final String filePath = savePath + "update.apk";
//                            File apkFile = new File(filePath);
//                            if (apkFile.exists() && apkFile.length() == length) {
//                                activity.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        binding.pbUpdate.setProgress(100, true);
//                                        binding.pbUpdate.setVisibility(View.INVISIBLE);
//                                        alertDialog.dismiss();
//                                        checkOreoBeforeInstall(filePath);
//                                    }
//                                });
//                                is.close();
//                                conn.disconnect();
//                                return;
//                            }
//                            int progress = 0;
//                            FileOutputStream fos = new FileOutputStream(apkFile);
//                            int count = 0;
//                            byte buf[] = new byte[1024];
//                            do {
//                                int numread = is.read(buf);
//                                count += numread;
//                                int lastProgress = progress;
//                                progress = (int) (((float) count / length) * 100);
//                                if (numread <= 0)
//                                    progress = 100;
//                                // 更新进度
//                                if (lastProgress != progress)//减少发送次数,通知否则会卡界面
//                                {
//                                    final int finalProgress = progress;
//                                    activity.runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            binding.pbUpdate.setProgress(finalProgress, true);
//                                        }
//                                    });
//                                }
//                                if (numread <= 0) {
//                                    // 下载完成通知安装
//                                    activity.runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            binding.pbUpdate.setVisibility(View.INVISIBLE);
//                                            alertDialog.dismiss();
//                                            checkOreoBeforeInstall(filePath);
//                                        }
//                                    });
//                                    break;
//                                }
//                                fos.write(buf, 0, numread);
//                            } while (!interceptFlag);// 点击取消就停止下载.
//                            fos.close();
//                            is.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();

//                if (bmobFile.getLocalFile() == null) {
//                    bmobFile.download(new DownloadFileListener() {
//                        @Override
//                        public void onStart() {
//                            binding.pbUpdate.setVisibility(View.VISIBLE);
//                            super.onStart();
//                        }
//
//                        @Override
//                        public void done(String path, BmobException e) {
//                            if (e == null) {
//                                activity.toast("下载完成:\n" + path);
//                                checkOreoBeforeInstall(path);
//                            } else {
//                                activity.toast("下载失败:\n" + e.getMessage());
//                            }
//                        }
//
//                        @Override
//                        public void onProgress(Integer integer, long l) {
//                            binding.pbUpdate.setProgress(integer);
//                        }
//
//                        @Override
//                        public void onFinish() {
//                            super.onFinish();
//                            binding.pbUpdate.setVisibility(View.INVISIBLE);
//                            alertDialog.dismiss();
//                        }
//                    });
//                } else {
//                    checkOreoBeforeInstall(bmobFile.getLocalFile().getAbsolutePath());
//                }
            }
        });
        alertDialog.show();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                interceptFlag = true;
            }
        });
    }

    /**
     * 检测版本8.0
     */
    public void checkOreoBeforeInstall(String apkFile) {
        if (Build.VERSION.SDK_INT >= 26) {//8.0
            //判断是否可以直接安装
            boolean canInstall = activity.getPackageManager().canRequestPackageInstalls();
            if (canInstall) {
                activity.checkPermissions(new OnPermissionCheckedListener() {
                    @Override
                    public void onPermissionGranted(String permission) {
                        UpdateManager.installApk(activity, apkFile);
                    }

                    @TargetApi(Build.VERSION_CODES.O)
                    @Override
                    public void onPermissionDenied(String permission) {
                        if (activity.shouldShowRequestPermissionRationale(Manifest.permission.REQUEST_INSTALL_PACKAGES)) {
                            //引导用户去打开权限
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                            activity.startActivityForResult(intent, 1, new OnActivityResultListener() {
                                @Override
                                public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                                    if (resultCode == Activity.RESULT_OK) {
                                        UpdateManager.installApk(activity, apkFile);
                                    } else {
                                        activity.toast("安装失败:没有权限");
                                    }
                                }
                            });
                        } else {
                            UpdateManager.installApk(activity, apkFile);
                        }
                    }
                }, Manifest.permission.REQUEST_INSTALL_PACKAGES);
            } else {
                UpdateManager.installApk(activity, apkFile);
            }
        } else {
            UpdateManager.installApk(activity, apkFile);
        }
    }

    /**
     * 安装apk
     */
    public static void installApk(BaseActivity activity, String apkFile) {
        File apkfile = new File(apkFile);
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        Uri data;
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//			 "com.ivxin.panresource.FileProvider"即是在清单文件中配置的authorities
            data = FileProvider.getUriForFile(activity, "com.ivxin.panresource.FileProvider", apkfile);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            data = Uri.fromFile(apkfile.getAbsoluteFile());
        }

        intent.setDataAndType(data, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
