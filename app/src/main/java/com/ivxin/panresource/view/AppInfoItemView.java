package com.ivxin.panresource.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivxin.panresource.AppInfo;
import com.ivxin.panresource.R;

public class AppInfoItemView extends MyAdapterItemLayout<AppInfo> {
    private ImageView iv_app_icon;
    private TextView tv_app_label_name;
    private TextView tv_app_package_name;

    public AppInfoItemView(Context context) {
        super(context);
        View.inflate(context, R.layout.layout_app_list_item, this);
        iv_app_icon = findViewById(R.id.iv_app_icon);
        tv_app_label_name = findViewById(R.id.tv_app_label_name);
        tv_app_package_name = findViewById(R.id.tv_app_package_name);
    }

    @Override
    public void setData(AppInfo bean, int position, ViewGroup parent) {
        iv_app_icon.setImageDrawable(bean.getAppIcon());
        tv_app_label_name.setText(bean.getAppName());
        tv_app_package_name.setText(bean.getPackageNmae());
    }
}
