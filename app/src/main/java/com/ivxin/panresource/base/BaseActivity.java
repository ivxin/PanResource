package com.ivxin.panresource.base;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ivxin.panresource.utils.ToastUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import us.feras.mdv.MarkdownView;

public class BaseActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_PERMISSION_ANY = 0;
    public static final int REQUEST_CODE_PERMISSION_FILE_ACCESS = 1;
    public static final int REQUEST_CODE_ACTIVITY_FILE_ACCESS = 2;
    public static final int REQUEST_CODE_ACTIVITY_ANY = 3;
    public SharedPreferences sp;
    private OnActivityResultListener onActivityResultListener;
    private static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private static final Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        sp = getSharedPreferences(Constant.SP_FILE, MODE_PRIVATE);
        super.onCreate(savedInstanceState);
    }

    public void runOnThread(Runnable runnable) {
        cachedThreadPool.execute(runnable);
    }

    public <T> Future<T> submitThread(Runnable runnable, T result) {
        return cachedThreadPool.submit(runnable, result);
    }

    public void runOnUiThreadDelay(Runnable runnable, long delay) {
        handler.postDelayed(runnable, delay);
    }


    public void showTipDialog(String content) {
        FrameLayout layout = new FrameLayout(this);
        int padding = 50;
        layout.setPadding(padding, padding, padding, padding);
        MarkdownView markdownView = new MarkdownView(this);
        markdownView.setPadding(50, 30, 30, 50);
        markdownView.loadMarkdown(content);


//        final QMUILinkTextView textView = new QMUILinkTextView(this);
//        textView.setPadding(50, 30, 30, 50);
//        textView.setBackgroundResource(R.drawable.bg_content_box);
//        textView.setOnLinkClickListener(new QMUILinkTextView.OnLinkClickListener() {
//            @Override
//            public void onTelLinkClick(String phoneNumber) {
//                Intent intent = new Intent(Intent.ACTION_CALL);
//                intent.setData(Uri.parse("tel:" + phoneNumber));
//                startActivity(intent);
//            }
//
//            @Override
//            public void onMailLinkClick(String mailAddress) {
//                Intent intent = new Intent(Intent.ACTION_SEND);
//                String[] receiver;
//                receiver = new String[]{mailAddress};
//                intent.putExtra(Intent.EXTRA_EMAIL, receiver);
//                intent.putExtra("subject", "About " + getString(R.string.app_name));
//                intent.putExtra(Intent.EXTRA_TEXT, "/*Thanks advance for any tips.*/");
//                intent.setType("text/plain");
//                startActivity(Intent.createChooser(intent, "Choose Email Client")); //调用系统的mail客户端进行发送
//            }
//
//            @Override
//            public void onWebUrlLinkClick(String url) {
//                Utils.openUrlWithOtherApp(BaseActivity.this, url, false);
//            }
//        });
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            textView.setText(Html.fromHtml(content, 0));
//        }else{
//            textView.setText(Html.fromHtml(content));
//        }
        layout.addView(markdownView);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(layout).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        dialog.show();
    }

    private OnPermissionCheckedListener onPermissionCheckedListener;

    public void checkPermissions(OnPermissionCheckedListener listener, String... permissions) {
        onPermissionCheckedListener = listener;
        for (String permission : permissions) {
            // 检查权限
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                if (onPermissionCheckedListener != null)
                    onPermissionCheckedListener.onPermissionGranted(permission);
            } else {
                // 进入到这里代表没有权限.
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION_ANY);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_ANY) {
            if (onPermissionCheckedListener != null)
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        // 用户同意授权
                        onPermissionCheckedListener.onPermissionGranted(permission);
                    } else {
                        // 用户拒绝授权
                        onPermissionCheckedListener.onPermissionDenied(permission);
                    }
                }
        }
        if (requestCode == REQUEST_CODE_PERMISSION_FILE_ACCESS) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                onFileAccessGrantedListener.onFileAccessGranted();
                onFileAccessGrantedListener = null;
            } else {
                Intent appIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                appIntent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(appIntent, REQUEST_CODE_ACTIVITY_FILE_ACCESS, new OnActivityResultListener() {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                        if (requestCode == REQUEST_CODE_ACTIVITY_FILE_ACCESS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {//Build.VERSION_CODES.R==30
                            if (Environment.isExternalStorageManager()) {
                                onFileAccessGrantedListener.onFileAccessGranted();
                                onFileAccessGrantedListener = null;
                            } else {
                                toast("存储权限获取失败");
                            }
                        }
                    }
                });
                toast("存储权限获取失败");
            }
        }
    }

    public void startActivityForResult(Intent intent, int requestCode, OnActivityResultListener onActivityResultListener) {
        startActivityForResult(intent, requestCode, null, onActivityResultListener);
    }

    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options, OnActivityResultListener onActivityResultListener) {
        super.startActivityForResult(intent, requestCode, options);
        this.onActivityResultListener = onActivityResultListener;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ACTIVITY_FILE_ACCESS) {
            if (onFileAccessGrantedListener != null) {
                if (resultCode == RESULT_OK) {
                    onFileAccessGrantedListener.onFileAccessGranted();
                } else {
                    toast("存储权限获取失败");
                }
                onFileAccessGrantedListener = null;
            }
        }
        if (requestCode == REQUEST_CODE_ACTIVITY_ANY) {
            if (onActivityResultListener != null) {
                onActivityResultListener.onActivityResult(requestCode, resultCode, data);
                onActivityResultListener = null;
            }
        }
    }


    public interface OnFileAccessGrantedListener {
        void onFileAccessGranted();
    }

    private OnFileAccessGrantedListener onFileAccessGrantedListener;

    public void requestFileAccessPermission(@NonNull OnFileAccessGrantedListener onFileAccessGrantedListener) {
        this.onFileAccessGrantedListener = onFileAccessGrantedListener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {//Build.VERSION_CODES.R==30
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                this.onFileAccessGrantedListener.onFileAccessGranted();
                this.onFileAccessGrantedListener = null;
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_ACTIVITY_FILE_ACCESS);
            }
        } else {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                this.onFileAccessGrantedListener.onFileAccessGranted();
                this.onFileAccessGrantedListener = null;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_FILE_ACCESS);
            }
        }

    }

    public void toast(CharSequence text) {
        ToastUtil.show(this, text);
    }

    public void gotoOtherForResult(Class<?> cls, int requestCode) {
        Intent intent = new Intent(this, cls);
        startActivityForResult(intent, requestCode);
    }

    public void gotoOtherForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    public void gotoOther(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    public void gotoOther(Intent intent) {
        startActivity(intent);
    }

    private AlertDialog loadingDialog;

    public void showLoadingDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(title)
                .setView(new ProgressBar(this));
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    public void hideLoadingDialog() {
        if (loadingDialog != null) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        }
    }
}
