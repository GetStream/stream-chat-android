package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.databinding.StreamItemAttachedMediaBinding;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;

import java.io.File;
import java.util.List;

import top.defaults.drawabletoolbox.DrawableBuilder;

public class MediaAttachmentSelectedAdapter extends RecyclerView.Adapter<MediaAttachmentSelectedAdapter.MyViewHolder> {

    private final String TAG = MediaAttachmentSelectedAdapter.class.getSimpleName();
    private final OnItemClickListener listener;
    private Context context;
    private List<Attachment> attachments;
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
        StreamItemAttachedMediaBinding itemBinding =
                StreamItemAttachedMediaBinding.inflate(layoutInflater, parent, false);
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

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final StreamItemAttachedMediaBinding binding;

        public MyViewHolder(StreamItemAttachedMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Attachment attachment, final OnItemClickListener listener) {
            int cornerRadius = Utils.dpToPx(16);
            binding.ivMedia.setShape(context, new DrawableBuilder()
                    .rectangle()
                    .solidColor(Color.BLACK)
                    .cornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius)
                    .build());

            binding.ivMask.setShape(context, new DrawableBuilder()
                    .rectangle()
                    .solidColor(Color.BLACK)
                    .cornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius)
                    .build());

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
                        .load(StreamChat.getInstance(context).getUploadStorage().signGlideUrl(attachment.getImageURL()))
                        .into(binding.ivMedia);
            } else {
                try {
                    if (attachment.getMime_type().equals(ModelType.attach_mime_mov) ||
                            attachment.getMime_type().equals(ModelType.attach_mime_mp4)) {
                        binding.ivMedia.setImageResource(R.drawable.stream_placeholder);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (attachment.getType().equals(ModelType.attach_file)) {
                binding.tvLength.setText(StringUtility.convertVideoLength(attachment.config.getVideoLengh()));
            } else {
                binding.tvLength.setText("");
            }
            itemView.setOnClickListener(view -> listener.onItemClick(getAdapterPosition()));

            if (attachment.config.isUploaded()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.ivMask.setVisibility(View.GONE);
            }else
                binding.progressBar.setProgress(attachment.config.getProgress());

            binding.executePendingBindings();
        }
    }
}
