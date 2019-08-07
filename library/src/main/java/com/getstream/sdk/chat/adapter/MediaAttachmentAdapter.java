package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.databinding.ListItemSelectPhotoBinding;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.Attachment;

import java.io.File;
import java.util.List;

public class MediaAttachmentAdapter extends RecyclerView.Adapter<MediaAttachmentAdapter.MyViewHolder> {

    private final String TAG = MediaAttachmentAdapter.class.getSimpleName();

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private Context context;
    private List<Attachment> mediaPaths;
    private final OnItemClickListener listener;

    public MediaAttachmentAdapter(Context context, List<Attachment> mediaPaths, OnItemClickListener listener) {
        this.context = context;
        this.mediaPaths = mediaPaths;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        LayoutInflater layoutInflater =
                LayoutInflater.from(parent.getContext());
        ListItemSelectPhotoBinding itemBinding =
                ListItemSelectPhotoBinding.inflate(layoutInflater, parent, false);
        return new MyViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.bind(mediaPaths.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return mediaPaths.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final ListItemSelectPhotoBinding binding;
        public MyViewHolder(ListItemSelectPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Attachment attachment, final OnItemClickListener listener) {
            File file = new File(attachment.config.getFilePath());
            if (file.exists()) {
                Uri imageUri = Uri.fromFile(file);
                Glide.with(context)
                        .load(imageUri)
                        .into(binding.ivMedia);
            } else {
                Log.d(TAG, "There is no image from this path");
            }
            if (attachment.config.isSelected())
                binding.ivSelectMark.setVisibility(View.VISIBLE);
            else
                binding.ivSelectMark.setVisibility(View.GONE);

            if (attachment.getType().equals(ModelType.attach_file)) {
                String videoLength;
                int videoLeng = attachment.config.getVideoLengh();
                if (videoLeng < 10) {
                    videoLength = "00:0" + videoLeng;
                } else if (videoLeng < 60) {
                    videoLength = "00:" + videoLeng;
                } else if (videoLeng < 600) {
                    videoLength = "0" + (int) (videoLeng / 60) + (videoLeng % 60 < 10 ? ":0" + videoLeng % 60 : ":" + videoLeng % 60);
                } else {
                    videoLength = (int) (videoLeng / 60) + (videoLeng % 60 < 10 ? ":0" + videoLeng % 60 : ":" + videoLeng % 60);
                }
                binding.tvLength.setText(videoLength);
            } else {
                binding.tvLength.setText("");
            }
            itemView.setOnClickListener((View v) -> {
                listener.onItemClick(getAdapterPosition());
            });
            binding.executePendingBindings();
        }
    }
}
