package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.ListItemAttachedMediaBinding;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.Attachment;

import java.io.File;
import java.util.List;

public class MediaAttachmentSelectedAdapter extends RecyclerView.Adapter<MediaAttachmentSelectedAdapter.MyViewHolder> {

    private final String TAG = MediaAttachmentSelectedAdapter.class.getSimpleName();

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private Context context;
    private List<Attachment> attachments;
    private final OnItemClickListener listener;

    public MediaAttachmentSelectedAdapter(Context context, List<Attachment> attachments, OnItemClickListener listener) {
        this.context = context;
        this.attachments = attachments;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        LayoutInflater layoutInflater =
                LayoutInflater.from(parent.getContext());
        ListItemAttachedMediaBinding itemBinding =
                ListItemAttachedMediaBinding.inflate(layoutInflater, parent, false);
        return new MyViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.bind(attachments.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final ListItemAttachedMediaBinding binding;

        public MyViewHolder(ListItemAttachedMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Attachment attachment, final OnItemClickListener listener) {
            if (attachment.config.getFilePath() != null) {
                File file = new File(attachment.config.getFilePath());
                if (file.exists()) {
                    Uri imageUri = Uri.fromFile(file);
                    Glide.with(context)
                            .load(imageUri)
                            .into(binding.ivMedia);
                }
            } else if (!TextUtils.isEmpty(attachment.getImageURL())) {
                Glide.with(context)
                        .load(attachment.getImageURL())
                        .into(binding.ivMedia);
            } else {
                try {
                    if (attachment.getMime_type().equals(ModelType.attach_mime_mov) ||
                            attachment.getMime_type().equals(ModelType.attach_mime_mp4)) {
                        binding.ivMedia.setImageResource(R.drawable.videoplay);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

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
            if (attachment.config.isUploaded()) binding.progressBar.setVisibility(View.GONE);
            binding.executePendingBindings();
        }
    }
}
