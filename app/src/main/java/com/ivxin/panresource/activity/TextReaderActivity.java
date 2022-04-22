package com.ivxin.panresource.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;

import androidx.annotation.Nullable;

import com.ivxin.panresource.base.BaseActivity;
import com.ivxin.panresource.databinding.ActivityTxtReaderBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextReaderActivity extends BaseActivity {
    private static ExecutorService singleThreadPool;
    public static final String FILE_NAME = "FILE_NAME";
    private ActivityTxtReaderBinding binding;
    private String fileName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(FILE_NAME)) {
            fileName = getIntent().getStringExtra(FILE_NAME);
            if (!TextUtils.isEmpty(fileName)) {
                File file = new File(fileName);
                if (file.exists() && file.isFile()) {
                    initView();
                    return;
                }
            }
        }
        finish();
    }

    private void initView() {
        singleThreadPool = Executors.newSingleThreadExecutor();
        binding = ActivityTxtReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileReader fileReader = new FileReader(new File(fileName));
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    final StringBuffer stringBuffer = new StringBuffer();
                    String tempString = bufferedReader.readLine();
                    while (tempString != null) {
                        stringBuffer.append(tempString).append("\n");
                        tempString = bufferedReader.readLine();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.tvContent.setText(stringBuffer);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
