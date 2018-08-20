package com.dev.digitectask.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dev.digitectask.R;
import com.dev.digitectask.utils.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectedFilesAdapter extends RecyclerView.Adapter<SelectedFilesAdapter.ViewHolder> {

    private List<String> mFiles;
    private Context mContext;

    public SelectedFilesAdapter(Context context, List<String> files) {
        this.mFiles = files;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.selected_files_list_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mFileNameTxt.setText(Utils.getNameFromPath(mFiles.get(position)));
        holder.mFileSizeTxt.setText(Utils.getFileSizeInMB(mFiles.get(position)) + " MB");
        Glide.with(mContext).load("file://" + mFiles.get(position))
                .override((int) Utils.convertDpToPixel(40), (int) Utils.convertDpToPixel(40))
                .centerCrop().into(holder.mUploadFileBtn);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.file_name_txt)
        TextView mFileNameTxt;
        @BindView(R.id.file_size_txt)
        TextView mFileSizeTxt;
        @BindView(R.id.upload_file_btn)
        ImageButton mUploadFileBtn;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }


}
