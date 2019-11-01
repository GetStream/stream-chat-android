package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;

import java.util.List;

public class AttachmentListAdapter extends BaseAdapter {

    private final String TAG = AttachmentListAdapter.class.getSimpleName();

    private LayoutInflater layoutInflater;
    private List<Attachment> attachments;
    private boolean localAttach;
    private boolean isTotalFileAdapter;


    public AttachmentListAdapter(Context context, List<Attachment> attachments, boolean localAttach, boolean isTotalFileAdapter) {
        this.attachments = attachments;
        this.layoutInflater = LayoutInflater.from(context);
        this.localAttach = localAttach;
        this.isTotalFileAdapter = isTotalFileAdapter;
    }

    @Override
    public int getCount() {
        return attachments.size();
    }

    @Override
    public Object getItem(int position) {
        return attachments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final Attachment attachment = attachments.get(position);
        final String type = attachment.getType();

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.stream_item_attach_file, null);

            holder = new ViewHolder();
            holder.iv_file_thumb = convertView.findViewById(R.id.iv_file_thumb);
            holder.tv_file_title = convertView.findViewById(R.id.tv_file_title);
            holder.tv_file_size = convertView.findViewById(R.id.tv_file_size);

            holder.iv_select_mark = convertView.findViewById(R.id.iv_select_mark);
            holder.tv_close = convertView.findViewById(R.id.tv_close);
            holder.progressBar = convertView.findViewById(R.id.progressBar);
            convertView.setTag(holder);
        } else {
            if (convertView.getTag().getClass().equals(ViewHolder.class)) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                holder = new ViewHolder();
                holder.iv_file_thumb = convertView.findViewById(R.id.iv_file_thumb);
                holder.tv_file_title = convertView.findViewById(R.id.tv_file_title);
                holder.tv_file_size = convertView.findViewById(R.id.tv_file_size);
                holder.iv_select_mark = convertView.findViewById(R.id.iv_select_mark);
                holder.tv_close = convertView.findViewById(R.id.tv_close);
                holder.progressBar = convertView.findViewById(R.id.progressBar);
                convertView.setTag(holder);
            }
        }
        configureFileAttach(holder, attachment);
        return convertView;
    }

    // endregion

    // region Configure Attachments


    private void configureFileAttach(ViewHolder holder, Attachment attachment) {

        holder.iv_file_thumb.setImageResource(attachment.getIcon());

        holder.tv_file_title.setText(attachment.getTitle());

        int fileSize = attachment.getFile_size();

        if (fileSize >= 1024 * 1024) {
            int fileSizeMB = fileSize / (1024 * 1024);
            holder.tv_file_size.setText(fileSizeMB + " MB");
        } else if (fileSize >= 1024) {
            int fileSizeKB = fileSize / 1024;
            holder.tv_file_size.setText(fileSizeKB + " KB");
        } else {
            holder.tv_file_size.setText(fileSize + " Bytes");
        }

        if (this.localAttach) {
            if (this.isTotalFileAdapter) {
                holder.tv_close.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.GONE);
                if (attachment.config.isSelected()) {
                    holder.iv_select_mark.setVisibility(View.VISIBLE);
                } else {
                    holder.iv_select_mark.setVisibility(View.GONE);
                }
            } else {
                holder.iv_select_mark.setVisibility(View.GONE);
                holder.tv_close.setVisibility(View.VISIBLE);
                if (attachment.config.isUploaded()) {
                    holder.progressBar.setVisibility(View.GONE);
                } else {
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.progressBar.setProgress(attachment.config.getProgress());
                }
            }

        } else {
            holder.iv_select_mark.setVisibility(View.GONE);
            holder.tv_close.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
        }
    }

    // endregion
    public class ViewHolder {
        ImageView iv_file_thumb, iv_select_mark;
        TextView tv_file_title, tv_file_size, tv_close;
        ProgressBar progressBar;
    }
}
