package com.ivxin.panresource.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.ivxin.panresource.R;
import com.ivxin.panresource.databinding.LayoutFileListItemBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FileInfoItemView extends MyAdapterItemLayout<File> {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private final LayoutFileListItemBinding binding;

    public FileInfoItemView(Context context) {
        super(context);
        binding = LayoutFileListItemBinding.inflate(LayoutInflater.from(context));
        addView(binding.getRoot());
    }

    @Override
    public void setData(File bean, int position, ViewGroup parent) {
        binding.ivFileIcon.setImageDrawable(null);
        binding.ivFileIcon.setBackground(null);
        binding.ivFileIcon.setImageResource(R.drawable.ic_file_24);
        if (bean.isDirectory()) {
            binding.ivFileIcon.setImageResource(R.drawable.ic_folder_24);
        } else if (bean.isFile()) {
            binding.ivFileIcon.setImageResource(R.drawable.ic_file_24);
            String[] imageExtents = new String[]{".jpg", ".jpeg", ".png", ".webp", ".bmp"};
            for (String ext : imageExtents) {
                if (bean.getPath().toLowerCase().endsWith(ext)) {
                    Glide.with(this).load(bean.getPath()).error(R.drawable.ic_file_24).into(binding.ivFileIcon);
                    break;
                } else {
                    binding.ivFileIcon.setImageResource(R.drawable.ic_file_24);
                }
            }
            String[] zipExtents = new String[]{".zip", ".rar", ".7z", ".tar", ".cab"};
            for (String zipExtent : zipExtents) {
                if (bean.getPath().toLowerCase().endsWith(zipExtent)) {
                    binding.ivFileIcon.setImageResource(R.mipmap.compressed);
                    break;
                }
            }
            if (bean.getPath().toLowerCase().endsWith(".torrent")) {
                binding.ivFileIcon.setImageResource(R.mipmap.torrent);
            }
        }
        binding.tvFileName.setText(bean.getName());
        binding.tvFilePath.setText(bean.getParent());
        binding.tvFileCreateDate.setText(simpleDateFormat.format(bean.lastModified()));
    }
}
