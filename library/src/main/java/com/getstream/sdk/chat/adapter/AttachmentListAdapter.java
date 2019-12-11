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
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.StringUtility;

import java.io.File;
import java.util.List;

public class AttachmentListAdapter extends BaseAdapter {

    private final String TAG = AttachmentListAdapter.class.getSimpleName();

    private LayoutInflater layoutInflater;
    private List<Attachment> attachments;
    private boolean localAttach;
    private boolean isTotalFileAdapter;
    private OnAttachmentCancelListener cancelListener;

    public AttachmentListAdapter(Context context,
                                 List<Attachment> attachments,
                                 boolean localAttach,
                                 boolean isTotalFileAdapter) {
        this.attachments = attachments;
        this.layoutInflater = LayoutInflater.from(context);
        this.localAttach = localAttach;
        this.isTotalFileAdapter = isTotalFileAdapter;
    }

    public AttachmentListAdapter(Context context,
                                 List<Attachment> attachments,
                                 boolean localAttach,
                                 boolean isTotalFileAdapter,
                                 OnAttachmentCancelListener cancelListener) {
        this(context, attachments, localAttach, isTotalFileAdapter);
        this.cancelListener = cancelListener;
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
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.stream_item_attach_file, null);

            holder = new ViewHolder();
            holder.iv_file_thumb = convertView.findViewById(R.id.iv_file_thumb);
            holder.tv_file_title = convertView.findViewById(R.id.tv_file_title);
            holder.tv_file_size = convertView.findViewById(R.id.tv_file_size);

            holder.iv_select_mark = convertView.findViewById(R.id.iv_select_mark);
            holder.iv_large_file_mark = convertView.findViewById(R.id.iv_large_file_mark);
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
                holder.iv_large_file_mark = convertView.findViewById(R.id.iv_large_file_mark);
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

        holder.iv_large_file_mark.setVisibility(View.INVISIBLE);
        holder.iv_select_mark.setVisibility(View.GONE);
        holder.tv_close.setVisibility(View.INVISIBLE);
        holder.progressBar.setVisibility(View.GONE);

        long fileSize = attachment.getFile_size();
        holder.tv_file_size.setText(StringUtility.convertFileSizeByteCount(fileSize));

        if (!this.localAttach) return;

        if (this.isTotalFileAdapter) {
            if (attachment.config.isSelected())
                holder.iv_select_mark.setVisibility(View.VISIBLE);
            File file = new File(attachment.config.getFilePath());
            holder.iv_large_file_mark.setVisibility(file.length()> Constant.MAX_UPLOAD_FILE_SIZE ? View.VISIBLE : View.INVISIBLE);
        } else {
            holder.tv_close.setVisibility(View.VISIBLE);
            holder.tv_close.setOnClickListener(view -> {
                if (cancelListener != null)
                    cancelListener.onCancel(attachment);
            });

            if (!attachment.config.isUploaded()) {
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.progressBar.setProgress(attachment.config.getProgress());
            }
        }
    }

    // endregion
    public class ViewHolder {
        ImageView iv_file_thumb, iv_select_mark, iv_large_file_mark;
        TextView tv_file_title, tv_file_size, tv_close;
        ProgressBar progressBar;
    }

    public interface OnAttachmentCancelListener {
        void onCancel(Attachment attachment);
    }
}
