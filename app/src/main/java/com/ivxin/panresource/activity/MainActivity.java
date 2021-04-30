package com.ivxin.panresource.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

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
import com.ivxin.panresource.utils.Utils;
import com.ivxin.panresource.view.AppInfoItemView;
import com.ivxin.panresource.view.MyAdapter;

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
            showTipDialog();
            sp.edit()
                    .putString(Constant.SP_KEY_TEMPLATE, getString(R.string.default_template))
                    .putString(Constant.SP_KEY_PACKAGE_NAME, getString(R.string.default_app_package_name))
                    .apply();
        }
        initStatic();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.cbAutomaticOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(Constant.SP_KEY_AUTOMATIC, isChecked).apply();
                Constant.isAutomatic = isChecked;
            }
        });
        binding.cbAutomaticOpen.setChecked(Constant.isAutomatic);
        readDeviceApps();
    }

    private void initStatic() {
        Constant.template = sp.getString(Constant.SP_KEY_TEMPLATE, getString(R.string.default_template));
        Constant.appPackageName = sp.getString(Constant.SP_KEY_PACKAGE_NAME, getString(R.string.default_app_package_name));
        Constant.isAutomatic = sp.getBoolean(Constant.SP_KEY_AUTOMATIC, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
            showTipDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTipDialog() {
        final TextView textView = new TextView(this);
        textView.setPadding(50, 30, 30, 50);
        textView.setBackgroundResource(R.drawable.bg_content_box);
        textView.setText(R.string.tip);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(textView).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        dialog.show();
    }

    private void readDeviceApps() {
        appInfoList.clear();
        PackageManager packageManager = getPackageManager();
//        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(mainIntent, 0);
        @SuppressLint("QueryPermissionsNeeded") List<PackageInfo> packageInfoList = getPackageManager().getInstalledPackages(0);
        Collections.sort(packageInfoList, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo o1, PackageInfo o2) {
                return (int) (o2.lastUpdateTime - o1.lastUpdateTime);
            }
        });
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOpenFromOther) {
                    binding.tvTextInClipboard.setText(url);
                    binding.tvLoadWebAndFindCode.performClick();
                } else {
                    checkClipboard();
                }
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
        binding.tvTextInClipboard.setText(textInClipboard);
        if (textInClipboard.startsWith("http")) {
            binding.tvLoadWebAndFindCode.setVisibility(View.VISIBLE);
            url = textInClipboard;
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
        if (theCode.matches(getString(R.string.code_rex)) && Constant.isAutomatic) {
            toast("Code found! now open the specific app in 3s");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.flOpenApp.performClick();
                }
            }, 3000);
        }
    }

    public void showWebLoadingDialog(View view) {
        LayoutWebLoadDialogBinding webLoadBinding = LayoutWebLoadDialogBinding.inflate(LayoutInflater.from(this));
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(webLoadBinding.getRoot()).create();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(url).get();
                    Element element = doc.body();
                    Elements rich_media = element.getElementsByClass("rich_media_wrp");
                    final String htmlText = rich_media.text();
                    Utils.printLog(BuildConfig.DEBUG, "htmlText", "htmlText: " + htmlText);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.tvLoadWebAndFindCode.setText(htmlText.substring(0, 30).concat("..."));
                            findTheCode(htmlText);
                            dialog.dismiss();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        dialog.show();
    }

    private void findTheCode(String htmlText) {
        if (htmlText != null) {
//            String cleanHtmlText = Utils.removeHtmlTag(htmlText);
            String theCode = "";
            Pattern pattern = Pattern.compile(getString(R.string.code_rex));
            Matcher matcher = pattern.matcher(htmlText);
            while (matcher.find()) {
                theCode = matcher.group();
            }
            binding.tvCodePreview.setText(theCode);
            formatCode(theCode);
        } else {
            binding.tvCodePreview.setText("");
        }
    }

    public void showTemplateEditDialog(View view) {
        final EditText editText = new EditText(this);
        editText.setPadding(50, 30, 30, 50);
        editText.setBackgroundResource(R.drawable.bg_content_box);
        editText.setText(Constant.template);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(editText).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Constant.template = editText.getText().toString();
                sp.edit().putString(Constant.SP_KEY_TEMPLATE, Constant.template).apply();
                checkClipboard();
            }
        }).create();
        dialog.show();
        editText.requestFocus();
        editText.setSelection(Constant.template.length());
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

        appListDialogBinding.llCurrentApp.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        appListDialogBinding.lvAppList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                binding.tvChooseApp.setText(appInfoList.get(position).getAppName());
                Constant.appPackageName = appInfoList.get(position).getPackageName();
                sp.edit().putString(Constant.SP_KEY_PACKAGE_NAME, Constant.appPackageName).apply();
                dialog.dismiss();
            }
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
            Uri uri = Uri.parse(url);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(uri);
            startActivity(intent);
        } else {
            toast("no code found");
        }
    }

    public void openApp(View view) {
        String fullCode = binding.tvCodePreview.getText().toString();
        if (fullCode.length() == 28 && "-".equals(fullCode.substring(23, 24))) {
            Utils.putTextIntoClipBoard(this, "", binding.tvTextPreview.getText().toString());
            Utils.startAppByPackageName(this, Constant.appPackageName);
        } else {
            toast("no code found");
        }
    }

    public void clearClipboard(View view) {
        Utils.putTextIntoClipBoard(this, "", "");
        checkClipboard();
    }

    public void find7zFile(View view) {
        gotoOther(ZipFileActivity.class);
    }


}