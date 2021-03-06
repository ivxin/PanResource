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
             * UpdateStatus.TimeOut    =-1????????????????????????
             * UpdateStatus.Yes        = 0????????????
             * UpdateStatus.No         = 1???????????????
             * UpdateStatus.IGNORED    = 3??????????????????????????????
             * UpdateStatus.EmptyField = 2?????????????????????????????????????????????
             *                             1)??????????????????target_size??????apk?????????????????????????????????
             *                             2)???path??????android_url?????????????????????????????????????????????????????????????????????path????????????apk?????????
             * UpdateStatus.ErrorSizeFormat = 4????????????target_size???????????????????????????file.length()????????????apk??????
             * UpdateStatus.Update     =6??? ????????????????????????????????????
             * UpdateStatus.NotNow     =7??? ????????????????????????????????????
             * UpdateStatus.Close      =8??? ?????????????????????-->????????????????????????????????????????????????????????????????????????close??????,
             *                              ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
             * @param updateStatus
             * @param updateInfo
             */
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                // TODO Auto-generated method stub
                if (updateStatus == UpdateStatus.Yes) {//???????????????

                } else if (updateStatus == UpdateStatus.No) {
                    activity.toast("???????????????");
                } else if (updateStatus == UpdateStatus.EmptyField) {//?????????????????????????????????????????????????????????????????????????????????????????????
                    activity.toast("????????????AppVersion??????????????????1???target_size?????????????????????????????????2???path??????android_url???????????????????????????");
                } else if (updateStatus == UpdateStatus.IGNORED) {
                    activity.toast("???????????????????????????");
                } else if (updateStatus == UpdateStatus.ErrorSizeFormat) {
                    activity.toast("?????????target_size???????????????????????????file.length()????????????apk?????????");
                } else if (updateStatus == UpdateStatus.TimeOut) {
                    activity.toast("???????????????????????????");
                }
            }
        });
        BmobUpdateAgent.setDialogListener(new BmobDialogButtonListener() {
            @Override
            public void onClick(int status) {
                switch (status) {
                    case UpdateStatus.Update:
                        activity.toast("???????????????????????????");
                        break;
                    case UpdateStatus.NotNow:
                        activity.toast("???????????????????????????");
                        break;
                    case UpdateStatus.Close://????????????????????????????????????????????????????????????????????????close??????,???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        activity.toast("??????????????????????????????");
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
//                            activity.toast("???????????????");
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
                return String.format(Locale.CHINA, "%s???", value);
            }
        });
        binding.tvUpdateLog.setText(String.format(Locale.CHINA, "?????????v%s\n\n%s\n\n????????????:\n%s  (???????????????%s)",
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
                Utils.putTextIntoClipBoard(activity, "", String.format(Locale.CHINA, "%s  (???????????????%s)", apkFile.getApk_url(), apkFile.getApk_url_code()));
                activity.toast("???????????????");
                alertDialog.dismiss();
            }
        });
        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.toast("???????????????????????????");
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
//                                savePath = activity.getFilesDir() + "/Download/";//7.0???????????????app??????
//                            } else {//7.0?????????????????????
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
//                                // ????????????
//                                if (lastProgress != progress)//??????????????????,????????????????????????
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
//                                    // ????????????????????????
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
//                            } while (!interceptFlag);// ???????????????????????????.
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
//                                activity.toast("????????????:\n" + path);
//                                checkOreoBeforeInstall(path);
//                            } else {
//                                activity.toast("????????????:\n" + e.getMessage());
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
     * ????????????8.0
     */
    public void checkOreoBeforeInstall(String apkFile) {
        if (Build.VERSION.SDK_INT >= 26) {//8.0
            //??????????????????????????????
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
                            //???????????????????????????
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                            activity.startActivityForResult(intent, 1, new OnActivityResultListener() {
                                @Override
                                public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                                    if (resultCode == Activity.RESULT_OK) {
                                        UpdateManager.installApk(activity, apkFile);
                                    } else {
                                        activity.toast("????????????:????????????");
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
     * ??????apk
     */
    public static void installApk(BaseActivity activity, String apkFile) {
        File apkfile = new File(apkFile);
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        Uri data;
        // ????????????????????????7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//			 "com.ivxin.panresource.FileProvider"?????????????????????????????????authorities
            data = FileProvider.getUriForFile(activity, "com.ivxin.panresource.FileProvider", apkfile);
            // ?????????????????????????????????
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
