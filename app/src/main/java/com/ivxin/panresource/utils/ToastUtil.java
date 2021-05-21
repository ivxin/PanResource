/**
 *
 */
package com.ivxin.panresource.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ivxin.panresource.R;


public class ToastUtil {

    public static void show(Context context, CharSequence info) {
        View layout = LayoutInflater.from(context).inflate(R.layout.common_toast, null);
        TextView text = layout.findViewById(R.id.toast_message_tv);
        if (TextUtils.isEmpty(info)) {
            info = "";
        }
        text.setText(info);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static void show(Context context, int info) {
        show(context, context.getString(info));
    }

}
