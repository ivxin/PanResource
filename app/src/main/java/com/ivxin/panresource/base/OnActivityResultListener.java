package com.ivxin.panresource.base;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public interface OnActivityResultListener {
    void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data);
}
