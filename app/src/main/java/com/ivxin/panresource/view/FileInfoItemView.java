package com.ivxin.panresource.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Checkable;

import com.bumptech.glide.Glide;
import com.ivxin.panresource.R;
import com.ivxin.panresource.databinding.LayoutFileListItemBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FileInfoItemView extends MyAdapterItemLayout<File> implements Checkable {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private final LayoutFileListItemBinding binding;

    public FileInfoItemView(Context context) {
        super(context);
        binding = LayoutFileListItemBinding.inflate(LayoutInflater.from(context));
        addView(binding.getRoot());
    }

    @Override
    public void setData(File bean, int position, ViewGroup parent) {
        Glide.with(this).clear(binding.ivFileIcon);
        binding.ivFileIcon.setImageDrawable(null);
        binding.ivFileIcon.setImageBitmap(null);
        binding.ivFileIcon.setBackground(null);
        binding.ivFileIcon.setImageResource(0);
        if (bean.isDirectory()) {
            binding.ivFileIcon.setImageResource(R.drawable.ic_folder_24);
        } else if (bean.isFile()) {
            binding.ivFileIcon.setImageResource(R.drawable.ic_file_24);
            String[] imageExtents = getResources().getStringArray(R.array.image_extends);
            for (String ext : imageExtents) {
                if (bean.getPath().toLowerCase().endsWith(ext)) {
                    Glide.with(this).load(bean.getPath()).error(R.drawable.ic_file_24).into(binding.ivFileIcon);
                    break;
                }
            }
            String[] zipExtents = getResources().getStringArray(R.array.zip_extends);
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

    @Override
    public void setChecked(boolean checked) {

    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void toggle() {

    }
}
