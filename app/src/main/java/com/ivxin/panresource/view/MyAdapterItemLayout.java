package com.ivxin.panresource.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public abstract class MyAdapterItemLayout<B> extends LinearLayout {

	public MyAdapterItemLayout(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public MyAdapterItemLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public MyAdapterItemLayout(Context context) {
		super(context);
	}

	public abstract void setData(B bean, int position, ViewGroup parent);
}
