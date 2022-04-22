package com.ivxin.panresource.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.BuildConfig;

import com.ivxin.panresource.R;
import com.ivxin.panresource.base.BaseActivity;
import com.ivxin.panresource.base.Constant;
import com.ivxin.panresource.databinding.ActivityMainBinding;
import com.ivxin.panresource.databinding.LayoutAppListDialogBinding;
import com.ivxin.panresource.databinding.LayoutWebLoadDialogBinding;
import com.ivxin.panresource.eneity.AppInfo;
import com.ivxin.panresource.utils.UpdateManager;
import com.ivxin.panresource.utils.Utils;
import com.ivxin.panresource.view.AppInfoItemView;
import com.ivxin.panresource.view.MyAdapter;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends BaseActivity {

    private final List<AppInfo> appInfoList = new ArrayList<>();

    private ActivityMainBinding binding;
    private String url;
    private boolean isOpenFromOther = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String messUrl = uri.toString();
                if (!TextUtils.isEmpty(messUrl) && messUrl.startsWith("http")) {
                    url = messUrl;
                    isOpenFromOther = true;
                }
            }
        }
        if (!sp.contains(Constant.SP_KEY_PACKAGE_NAME)) {
            showTipDialog(getString(R.string.tip));
            sp.edit()
                    .putString(Constant.SP_KEY_REX, getString(R.string.code_rex))
                    .putString(Constant.SP_KEY_TEMPLATE, getString(R.string.default_template))
                    .putString(Constant.SP_KEY_PACKAGE_NAME, getString(R.string.default_app_package_name))
                    .apply();
        }
        initStatic();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.cbAutomaticOpen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean(Constant.SP_KEY_AUTOMATIC, isChecked).apply();
            Constant.isAutomatic = isChecked;
        });
        binding.cbAutomaticOpen.setChecked(Constant.isAutomatic);
        binding.tvCodeRex.setText(Constant.codeRex);
        readDeviceApps();
    }

    private void initStatic() {
        Constant.codeRex = sp.getString(Constant.SP_KEY_REX, getString(R.string.code_rex));
        Constant.template = sp.getString(Constant.SP_KEY_TEMPLATE, getString(R.string.default_template));
        Constant.appPackageName = sp.getString(Constant.SP_KEY_PACKAGE_NAME, getString(R.string.default_app_package_name));
        Constant.isAutomatic = sp.getBoolean(Constant.SP_KEY_AUTOMATIC, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spannableString = new SpannableString(item.getTitle());
            spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString.length(), 0);
            item.setTitle(spannableString);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
            showTipDialog(getString(R.string.tip));
        } else if (item.getItemId() == R.id.action_check) {
            requestFileAccessPermission(() -> {
                UpdateManager manager = new UpdateManager(MainActivity.this);
                manager.checkUpdate();
            });

        } else if (item.getItemId() == R.id.action_about) {
            String content = String.format(Locale.CHINA, getString(R.string.about), getString(R.string.app_name), Constant.versionName);
            showTipDialog(content);
        }
        return super.onOptionsItemSelected(item);
    }


    private void readDeviceApps() {
        appInfoList.clear();
        PackageManager packageManager = getPackageManager();
//        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(mainIntent, 0);
        @SuppressLint("QueryPermissionsNeeded") List<PackageInfo> packageInfoList = getPackageManager().getInstalledPackages(0);
        Collections.sort(packageInfoList, (o1, o2) -> (int) (o2.lastUpdateTime - o1.lastUpdateTime));
        for (PackageInfo packageInfo : packageInfoList) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
                String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                String packageName = packageInfo.packageName;
                AppInfo appInfo = new AppInfo();
                appInfo.setAppIcon(icon);
                appInfo.setAppName(appName);
                appInfo.setPackageName(packageName);
                appInfoList.add(appInfo);
                if (Constant.appPackageName.equals(packageName)) {
                    binding.tvChooseApp.setText(appName);
                }
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThreadDelay(() -> {
            if (isOpenFromOther) {
                binding.tvLoadWebAndFindCode.performClick();
            } else {
                checkClipboard();
            }
        }, 300);

    }

    @Override
    protected void onPause() {
        super.onPause();
        isOpenFromOther = false;
        url = null;
    }

    private void checkClipboard() {
        String textInClipboard = Utils.getTextFromClipBoard(this);
        if (textInClipboard.startsWith("http")) {
            binding.tvLoadWebAndFindCode.setVisibility(View.VISIBLE);
            url = textInClipboard;
            binding.tvLoadWebAndFindCode.performClick();
        } else if (textInClipboard.length() > 28) {
            binding.tvLoadWebAndFindCode.setVisibility(View.GONE);
            findTheCode(textInClipboard);
        } else {
            binding.tvLoadWebAndFindCode.setVisibility(View.GONE);
            if (textInClipboard.matches(getString(R.string.code_rex))) {
                binding.tvCodePreview.setText(textInClipboard);
                formatCode(textInClipboard);
            } else {
                binding.tvCodePreview.setText(R.string.nothing_found);
                binding.tvTextPreview.setText("");
            }
        }
    }

    private void formatCode(String theCode) {
        String formatted;
        if (theCode.length() == 28 && "-".equals(theCode.substring(23, 24))) {
            String code = theCode.substring(0, 23);
            String pass = theCode.substring(24, 28);
            formatted = String.format(Constant.template, code, pass);
        } else if (theCode.length() == 23) {
            formatted = String.format(Constant.template, theCode, "");
        } else {
            formatted = Constant.template.replaceAll("%s", theCode);
        }
        binding.tvTextPreview.setText(formatted);
        binding.svMain.scrollTo(0, binding.svMain.getBottom());
        Constant.isAutomatic = false;//自动功能预计废除
        if (theCode.matches(Constant.codeRex) && Constant.isAutomatic) {
            toast("Code found! now open the specific app in 3s");
            runOnUiThreadDelay(() -> binding.flOpenApp.performClick(), 3000);
        }
    }

    public void showWebLoadingDialog(View view) {
        LayoutWebLoadDialogBinding webLoadBinding = LayoutWebLoadDialogBinding.inflate(LayoutInflater.from(this));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(webLoadBinding.getRoot()).create();
        dialog.show();
        runOnThread(() -> {
            try {
                Document doc = Jsoup.connect(url).get();
                Element element = doc.body();
                Elements rich_media = element.getElementsByClass("rich_media_wrp");
                final String htmlText = rich_media.text();
                Utils.printLog(BuildConfig.DEBUG, "htmlText", "htmlText: " + htmlText);
                runOnUiThread(() -> {
                    if (htmlText.length() > 30) {
                        binding.tvLoadWebAndFindCode.setText(htmlText.substring(0, 30).concat("..."));
                    } else {
                        binding.tvLoadWebAndFindCode.setText(htmlText);
                    }
                    findTheCode(htmlText);
                    dialog.dismiss();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void findTheCode(String htmlText) {
        if (htmlText != null) {
//            String cleanHtmlText = Utils.removeHtmlTag(htmlText);
            String theCode = "";
            Pattern pattern = Pattern.compile(Constant.codeRex);
            Matcher matcher = pattern.matcher(htmlText);
            while (matcher.find()) {
                theCode = matcher.group();
            }
            if (theCode.length() > 22) {
                binding.tvCodePreview.setText(theCode);
                formatCode(theCode);
            } else {
                binding.tvCodePreview.setText("没找到code");
            }
        } else {
            binding.tvCodePreview.setText("");
        }
    }

    public void showRexEditDialog(View view) {
        final EditText editText = new EditText(this);
        editText.setPadding(50, 30, 30, 50);
        editText.setBackgroundResource(R.drawable.bg_content_box);
        editText.setText(Constant.codeRex);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        } else {
            editText.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        @SuppressLint("RestrictedApi") final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(editText, 50, 20, 50, 0).setTitle("修改正则表达式")
                .setPositiveButton("确定", (dialog1, which) -> {
                    Constant.codeRex = editText.getText().toString();
                    sp.edit().putString(Constant.SP_KEY_REX, Constant.codeRex).apply();
                    checkClipboard();
                }).create();
        dialog.show();
        editText.requestFocus();
        editText.setSelection(Constant.codeRex.length());
        QMUIKeyboardHelper.showKeyboard(editText, true);
    }

    public void showTemplateEditDialog(View view) {
        final EditText editText = new EditText(this);
        editText.setPadding(50, 30, 30, 50);
        editText.setBackgroundResource(R.drawable.bg_content_box);
        editText.setText(Constant.template);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        } else {
            editText.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        @SuppressLint("RestrictedApi") final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(editText, 50, 20, 50, 0)
                .setTitle("修改模板")
                .setPositiveButton("确定", (dialog1, which) -> {
                    Constant.template = editText.getText().toString();
                    sp.edit().putString(Constant.SP_KEY_TEMPLATE, Constant.template).apply();
                    checkClipboard();
                }).create();
        dialog.show();
        editText.requestFocus();
        editText.setSelection(Constant.template.length());
        QMUIKeyboardHelper.showKeyboard(editText, true);
    }

    public void showAppListDialog(View view) {
        AppInfo currentAppInfo = null;
        for (AppInfo appInfo : appInfoList) {
            if (appInfo.getPackageName().equals(Constant.appPackageName)) {
                currentAppInfo = appInfo;
            }
        }
        LayoutAppListDialogBinding appListDialogBinding = LayoutAppListDialogBinding.inflate(LayoutInflater.from(this));
        if (currentAppInfo != null) {
            appListDialogBinding.llCurrentApp.ivAppIcon.setImageDrawable(currentAppInfo.getAppIcon());
            appListDialogBinding.llCurrentApp.tvAppLabelName.setText(currentAppInfo.getAppName());
            appListDialogBinding.llCurrentApp.tvAppPackageName.setText(currentAppInfo.getPackageName());
        }
        MyAdapter<AppInfoItemView, AppInfo> adapter = new MyAdapter<>(this, appInfoList, AppInfoItemView.class, AppInfo.class);
        appListDialogBinding.lvAppList.setAdapter(adapter);

        final AlertDialog dialog = new AlertDialog.Builder(this).setView(appListDialogBinding.getRoot()).create();

        appListDialogBinding.llCurrentApp.getRoot().setOnClickListener(v -> dialog.dismiss());
        appListDialogBinding.lvAppList.setOnItemClickListener((parent, view1, position, id) -> {
            binding.tvChooseApp.setText(appInfoList.get(position).getAppName());
            Constant.appPackageName = appInfoList.get(position).getPackageName();
            sp.edit().putString(Constant.SP_KEY_PACKAGE_NAME, Constant.appPackageName).apply();
            dialog.dismiss();
        });
        dialog.show();
    }

    public void openBrowser(View view) {
        String fullCode = binding.tvCodePreview.getText().toString();
        if (fullCode.length() == 28 && "-".equals(fullCode.substring(23, 24))) {
            String code = fullCode.substring(0, 23);
            String pass = fullCode.substring(24, 28);
            String url = String.format(Locale.CHINA, "https://pan.baidu.com/s/%s", code);
            Utils.putTextIntoClipBoard(this, "", pass);
            Utils.openUrlWithOtherApp(this, url, false);
        } else {
            toast("没有code");
        }
    }

    public void openApp(View view) {
        String fullCode = binding.tvCodePreview.getText().toString();
        if (fullCode.length() == 28 && "-".equals(fullCode.substring(23, 24))) {
            Utils.putTextIntoClipBoard(this, "", binding.tvTextPreview.getText().toString());
            Utils.startAppByPackageName(this, Constant.appPackageName);
        } else {
            toast("没有code");
        }
    }

    public void clearClipboard(View view) {
        Utils.putTextIntoClipBoard(this, "", "");
        checkClipboard();
    }

    public void find7zFile(View view) {
        requestFileAccessPermission(() -> gotoOther(ZipFileActivity.class));
    }

}
