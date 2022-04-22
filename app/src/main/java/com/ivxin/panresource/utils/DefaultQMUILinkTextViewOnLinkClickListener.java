package com.ivxin.panresource.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.qmuiteam.qmui.widget.textview.QMUILinkTextView;

public class DefaultQMUILinkTextViewOnLinkClickListener implements QMUILinkTextView.OnLinkClickListener {
    private Context context;

    public DefaultQMUILinkTextViewOnLinkClickListener(Context context) {
        this.context = context;
    }

    @Override
    public void onTelLinkClick(String phoneNumber) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse(String.format("tel:%s", phoneNumber)));
        context.startActivity(intent);
    }

    @Override
    public void onMailLinkClick(String mailAddress) {
        Uri uri = Uri.parse(String.format("mailto:%s", mailAddress));
        String[] email = {mailAddress};
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
        intent.putExtra(Intent.EXTRA_SUBJECT, "subject/主题"); // 主题
        intent.putExtra(Intent.EXTRA_TEXT, "text/正文"); // 正文
        context.startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
    }

    @Override
    public void onWebUrlLinkClick(String url) {
        Utils.openUrlWithOtherApp(context, url, false);
    }
}
