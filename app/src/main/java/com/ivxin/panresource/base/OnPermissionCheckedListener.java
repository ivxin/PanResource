package com.ivxin.panresource.base;

/**
 * Created by yaping.wang on 2017/7/21.
 */

public interface OnPermissionCheckedListener {
    void onPermissionGranted(String permission);

    void onPermissionDenied(String permission);
}
