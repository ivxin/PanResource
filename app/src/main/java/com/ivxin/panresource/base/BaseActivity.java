package com.ivxin.panresource.base;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class BaseActivity extends FragmentActivity {
    public SharedPreferences sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        sp = getSharedPreferences(Constant.SP_FILE, MODE_PRIVATE);
        super.onCreate(savedInstanceState);
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
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
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

    }

    public void toast(CharSequence text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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

    public AlertDialog dialog;

    public void showLoadingDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(false).setTitle(title).setView(new ProgressBar(this));
        dialog = builder.create();
        dialog.show();
    }

    public void hideLoadingDialog() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
        }
    }
}
