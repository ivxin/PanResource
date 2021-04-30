package com.ivxin.panresource.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ivxin.panresource.eneity.AppInfo;
import com.ivxin.panresource.databinding.LayoutAppListItemBinding;

public class AppInfoItemView extends MyAdapterItemLayout<AppInfo> {
    private LayoutAppListItemBinding binding;

    public AppInfoItemView(Context context) {
        super(context);
        binding = LayoutAppListItemBinding.inflate(LayoutInflater.from(context));
        addView(binding.getRoot());
    }

    @Override
    public void setData(AppInfo bean, int position, ViewGroup parent) {
        binding.ivAppIcon.setImageDrawable(bean.getAppIcon());
        binding.tvAppLabelName.setText(bean.getAppName());
        binding.tvAppPackageName.setText(bean.getPackageName());
    }
}
