package com.getstream.sdk.chat.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.AttachmentListAdapter;
import com.getstream.sdk.chat.adapter.CommandMentionListItemAdapter;
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter;
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter;
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding;
import com.getstream.sdk.chat.enums.InputType;
import com.getstream.sdk.chat.enums.MessageInputType;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Command;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.interfaces.SendFileCallback;
import com.getstream.sdk.chat.rest.response.FileSendResponse;
import com.getstream.sdk.chat.view.MessageInputStyle;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageInputClient {

    private static final String TAG = MessageInputClient.class.getSimpleName();

    ChannelViewModel viewModel;
    MessageInputStyle style;
    MediaAttachmentAdapter mediaAttachmentAdapter = null;
    MediaAttachmentSelectedAdapter selectedMediaAttachmentAdapter = null;
    CommandMentionListItemAdapter commandMentionListItemAdapter = null;
    List<Object> commands = null;
    Context context;
    StreamViewMessageInputBinding binding;
    AttachmentListAdapter fileAttachmentAdapter = null;
    AttachmentListAdapter selectedFileAttachmentAdapter = null;
    private List<Attachment> selectedAttachments = null;

    // region Attachment

    public MessageInputClient(Context context, StreamViewMessageInputBinding binding, ChannelViewModel viewModel, MessageInputStyle style) {
        this.context = context;
        this.binding = binding;
        this.viewModel = viewModel;
        this.style = style;
    }

    public List<Attachment> getSelectedAttachments() {
        return selectedAttachments;
    }

    public void setSelectedAttachments(List<Attachment> selectedAttachments) {
        this.selectedAttachments = selectedAttachments;
    }

    public void onClickOpenBackGroundView(MessageInputType type) {

        binding.getRoot().setBackgroundResource(R.drawable.stream_round_thread_toolbar);
        binding.clTitle.setVisibility(View.VISIBLE);
        binding.tvClose.setVisibility(View.VISIBLE);

        binding.clAddFile.setVisibility(View.GONE);
        binding.clCommand.setVisibility(View.GONE);
        binding.clSelectPhoto.setVisibility(View.GONE);

        switch (type){
            case EDIT_MESSAGE:
                break;
            case ADD_FILE:
                if (selectedAttachments != null && !selectedAttachments.isEmpty()) return;
                binding.clAddFile.setVisibility(View.VISIBLE);
                break;
            case UPLOAD_MEDIA:
            case UPLOAD_FILE:
                binding.clSelectPhoto.setVisibility(View.VISIBLE);
                break;
            case COMMAND:
            case MENTION:
                binding.tvClose.setVisibility(View.GONE);
                binding.clCommand.setVisibility(View.VISIBLE);
                break;
        }
        binding.tvTitle.setText(type.label);
    }


    public void onClickCloseBackGroundView() {
        binding.clTitle.setVisibility(View.GONE);
        binding.clAddFile.setVisibility(View.GONE);
        binding.clSelectPhoto.setVisibility(View.GONE);
        binding.clCommand.setVisibility(View.GONE);
        binding.getRoot().setBackgroundResource(0);
    }

    private void initLoadAttachemtView() {
        binding.rvComposer.setVisibility(View.GONE);
        binding.lvComposer.setVisibility(View.GONE);
        binding.progressBarFileLoader.setVisibility(View.VISIBLE);
    }

    // endregion

    // region Media

    private void configSelectAttachView(boolean isMedia, List<Attachment> editAttachments) {
        if (editAttachments != null) {
            selectedAttachments = editAttachments;
        } else {
            selectedAttachments = new ArrayList<>();
        }
        binding.setIsAttachFile(!isMedia);
        if (isMedia) {
            List<Attachment> attachments = Utils.getAllShownImagesPath(context);
            ((Activity) context).runOnUiThread(() -> {
                mediaAttachmentAdapter = new MediaAttachmentAdapter(context, attachments, position -> {
                    Attachment attachment = attachments.get(position);
                    attachment.config.setSelected(!attachment.config.isSelected());
                    mediaAttachmentAdapter.notifyItemChanged(position);
                    updateComposerViewBySelectedMedia(attachments, attachment);
                });
                binding.rvMedia.setAdapter(mediaAttachmentAdapter);
                binding.progressBarFileLoader.setVisibility(View.GONE);
                // edit
                if (editAttachments != null) {
                    binding.rvComposer.setVisibility(View.VISIBLE);
                    setSelectedMediaAttachmentRecyclerViewAdapter(attachments);
                }
            });
        } else {
            Utils.attachments = new ArrayList<>();
            List<Attachment> attachments = Utils.Search_Dir(Environment.getExternalStorageDirectory());
            ((Activity) context).runOnUiThread(() -> {
                if (attachments.size() > 0) {
                    fileAttachmentAdapter = new AttachmentListAdapter(context, attachments, true, true);
                    binding.lvFile.setAdapter(fileAttachmentAdapter);
                    binding.lvFile.setOnItemClickListener((AdapterView<?> parent, View view,
                                                           int position, long id) -> {
                        Attachment attachment = attachments.get(position);

                        attachment.config.setSelected(!attachment.config.isSelected());
                        fileAttachmentAdapter.notifyDataSetChanged();
                        updateComposerViewBySelectedFile(attachments, attachment);
                    });
                } else {
                    Utils.showMessage(context, "There is no file");

                }
                binding.progressBarFileLoader.setVisibility(View.GONE);
                // edit
                if (editAttachments != null) {
                    binding.lvComposer.setVisibility(View.VISIBLE);
                    setSelectedFileAttachmentListAdapter(attachments);
                }
            });
        }
    }

    public void onClickOpenSelectMediaView(View v, List<Attachment> editAttachments) {
        initLoadAttachemtView();
        AsyncTask.execute(() -> configSelectAttachView(true, editAttachments));
        onClickOpenBackGroundView(MessageInputType.UPLOAD_MEDIA);
    }

    // endregion

    // region File

    private void updateComposerViewBySelectedMedia(List<Attachment> attachments, Attachment attachment) {
        binding.rvComposer.setVisibility(View.VISIBLE);
        if (selectedAttachments == null) selectedAttachments = new ArrayList<>();
        if (attachment.config.isSelected()) {
            selectedAttachments.add(attachment);

            viewModel.getChannel().sendFile(attachment, attachment.getType().equals(ModelType.attach_image), new SendFileCallback() {
                @Override
                public void onSuccess(FileSendResponse response) {
                    binding.setActiveMessageSend(true);
                    File file = new File(attachment.config.getFilePath());
                    if (attachment.getType().equals(ModelType.attach_image)) {
                        attachment.setImageURL(response.getFileUrl());
                        attachment.setFallback(file.getName());
                    } else {
                        attachment.setTitle(file.getName());
                        long size = file.length();
                        attachment.setFile_size((int) size);
                        attachment.setAssetURL(response.getFileUrl());
                    }
                    attachment.config.setUploaded(true);
                    selectedMediaAttachmentAdapter.notifyItemChanged(selectedAttachments.size() - 1);
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    binding.setActiveMessageSend(true);
                    attachment.config.setSelected(false);
                    Utils.showMessage(context, "Failed upload image!");
                    updateComposerViewBySelectedMedia(attachments, attachment);
                }
            });
        } else
            selectedAttachments.remove(attachment);

        setSelectedMediaAttachmentRecyclerViewAdapter(attachments);

        if (selectedAttachments.size() > 0) {
            binding.setActiveMessageSend(true);
            viewModel.setInputType(InputType.SELECT);
        } else if (binding.etMessage.getText().toString().length() == 0) {
            viewModel.setInputType(InputType.DEFAULT);
            binding.setActiveMessageSend(false);
        }
    }

    private void setSelectedMediaAttachmentRecyclerViewAdapter(final List<Attachment> attachments) {
        selectedMediaAttachmentAdapter = new MediaAttachmentSelectedAdapter(context, selectedAttachments, (int position) -> {
            Attachment attachment1 = selectedAttachments.get(position);
            attachment1.config.setSelected(false);
            selectedAttachments.remove(attachment1);
            selectedMediaAttachmentAdapter.notifyDataSetChanged();
            if (attachments != null) {
                int position_ = -1;
                for (int i = 0; i < attachments.size(); i++) {
                    Attachment attachment2 = attachments.get(i);
                    if (attachment2.config.getFilePath().equals(attachment1.config.getFilePath())) {
                        position_ = i;
                        break;
                    }
                }
                if (position_ != -1)
                    mediaAttachmentAdapter.notifyItemChanged(position_);
            }

            if (selectedAttachments.size() == 0 && binding.etMessage.getText().toString().length() == 0) {
                viewModel.setInputType(InputType.DEFAULT);
                binding.setActiveMessageSend(false);
            }
        });
        binding.rvComposer.setAdapter(selectedMediaAttachmentAdapter);
    }

    public void onClickOpenSelectFileView(View v, List<Attachment> editAttachments) {
        initLoadAttachemtView();
        AsyncTask.execute(() -> configSelectAttachView(false, editAttachments));
        onClickOpenBackGroundView(MessageInputType.UPLOAD_FILE);
    }

    private void updateComposerViewBySelectedFile(List<Attachment> attachments, Attachment attachment) {
        binding.lvComposer.setVisibility(View.VISIBLE);
        if (selectedAttachments == null) selectedAttachments = new ArrayList<>();
        if (attachment.config.isSelected()) {
            selectedAttachments.add(attachment);
            viewModel.getChannel().sendFile(attachment, false, new SendFileCallback() {
                @Override
                public void onSuccess(FileSendResponse response) {
                    attachment.setAssetURL(response.getFileUrl());
                    attachment.config.setUploaded(true);
                    selectedFileAttachmentAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    attachment.config.setSelected(false);
                    updateComposerViewBySelectedFile(attachments, attachment);
                }
            });
        } else
            selectedAttachments.remove(attachment);

        setSelectedFileAttachmentListAdapter(attachments);

        if (selectedAttachments.size() > 0) {
            viewModel.setInputType(InputType.SELECT);
            binding.setActiveMessageSend(true);
        } else if (binding.etMessage.getText().toString().length() == 0) {
            viewModel.setInputType(InputType.DEFAULT);
            binding.setActiveMessageSend(false);
        }
    }

    private void setSelectedFileAttachmentListAdapter(final List<Attachment> attachments) {
        selectedFileAttachmentAdapter = new AttachmentListAdapter(context, selectedAttachments, true, false);
        binding.lvComposer.setAdapter(selectedFileAttachmentAdapter);
        binding.lvComposer.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            Attachment attachment1 = selectedAttachments.get(position);
            attachment1.config.setSelected(false);
            selectedAttachments.remove(attachment1);
            selectedFileAttachmentAdapter.notifyDataSetChanged();
            int position_ = -1;
            for (int i = 0; i < attachments.size(); i++) {
                Attachment attachment2 = attachments.get(i);
                if (attachment2.config.getFilePath().equals(attachment1.config.getFilePath())) {
                    position_ = i;
                    break;
                }
            }
            if (position_ != -1)
                fileAttachmentAdapter.notifyDataSetChanged();
            if (selectedAttachments.size() == 0 && binding.etMessage.getText().toString().length() == 0) {
                viewModel.setInputType(InputType.DEFAULT);
                binding.setActiveMessageSend(false);
            }
        });
    }

    public void initSendMessage() {
        binding.etMessage.setText("");
        selectedAttachments = new ArrayList<>();

        binding.lvComposer.removeAllViewsInLayout();
        binding.rvComposer.removeAllViewsInLayout();

        binding.lvComposer.setVisibility(View.GONE);
        binding.rvComposer.setVisibility(View.GONE);

        selectedFileAttachmentAdapter = null;
        onClickCloseBackGroundView();
    }
    // endregion

    // region Camera
    public void progressCapturedMedia(Context context, Uri contentUri, boolean isImage) {
        Cursor cursor = null;
        try {
            String[] proj = isImage ? new String[]{MediaStore.Images.Media.DATA} : new String[]{MediaStore.Video.Media.DATA, MediaStore.Video.Media.RESOLUTION, MediaStore.Video.VideoColumns.DURATION};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = isImage ? cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA) : cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            File file = new File(cursor.getString(column_index));
            if (file.exists()) {
                convertAttachment(file, cursor, isImage);
            } else {
                Log.d(TAG, "No Captured Video");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void convertAttachment(File file, Cursor cursor, boolean isImage) {
        Attachment attachment = new Attachment();
        attachment.config.setFilePath(file.getPath());
        attachment.config.setSelected(true);
        if (isImage) {
            attachment.setType(ModelType.attach_image);
        } else {
            float videolengh = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
            attachment.config.setVideoLengh((int) (videolengh / 1000));
            attachment.setType(ModelType.attach_file);
            attachment.setMime_type(ModelType.attach_mime_mp4);
        }
        updateComposerViewBySelectedMedia(null, attachment);
    }
    // endregion

    // region Cammand
    private void openCommandView() {
        onClickOpenBackGroundView(MessageInputType.COMMAND);
    }

    private void closeCommandView() {
        if (isCommandOrMention())
            onClickCloseBackGroundView();
        commands = null;
    }

    private boolean isCommandOrMention(){
        String title = binding.tvTitle.getText().toString();
        return title.equals(MessageInputType.COMMAND.label)
                || title.equals(MessageInputType.MENTION.label);
    }

    public void checkCommand(String text) {
        if (TextUtils.isEmpty(text)
                || (!text.startsWith("/") && !text.contains("@"))) {
            closeCommandView();
        }else if (text.length() == 1) {
            onClickCommandViewOpen(text.startsWith("/"));
        } else if (text.endsWith("@") && binding.clCommand.getVisibility() != View.VISIBLE) {
            onClickCommandViewOpen(false);
        } else {
            setCommandsMentionUsers(text);
            if (!commands.isEmpty() && binding.clCommand.getVisibility() != View.VISIBLE)
                openCommandView();

            if (commandMentionListItemAdapter != null)
                commandMentionListItemAdapter.notifyDataSetChanged();

            if (commands.isEmpty())
                closeCommandView();
        }
    }

    private void onClickCommandViewOpen(boolean isCommand) {
        if (isCommand) {
            setCommands("");
        } else {
            setMentionUsers("");
        }
        String title = binding.tvTitle.getContext().getResources().getString(isCommand ? R.string.stream_command_title : R.string.stream_mention_title);
        binding.tvTitle.setText(title);
        binding.tvCommand.setText("");
        commandMentionListItemAdapter = new CommandMentionListItemAdapter(this.context, commands, style, isCommand);
        binding.lvCommand.setAdapter(commandMentionListItemAdapter);
        openCommandView();
        binding.lvCommand.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            if (isCommand) {
                binding.etMessage.setText("/" + ((Command) commands.get(i)).getName() + " ");
            } else {
                binding.etMessage.setText(binding.etMessage.getText().toString() + ((User) commands.get(i)).getName() + " ");
            }
            binding.etMessage.setSelection(binding.etMessage.getText().length());
            closeCommandView();
        });
    }

    private void setCommandsMentionUsers(String string) {
        String string_ = string.replace(string.substring(0, 1), "");
        if (commands == null) commands = new ArrayList<>();
        commands.clear();
        if (string.startsWith("/")) {
            setCommands(string_);
            binding.tvCommand.setText(string_);
        } else {
            setMentionUsers(string_);
        }
    }

    private void setCommands(String string) {
        if (commands == null) commands = new ArrayList<>();
        commands.clear();
        for (int i = 0; i < viewModel.getChannel().getConfig().getCommands().size(); i++) {
            Command command = viewModel.getChannel().getConfig().getCommands().get(i);
            if (command.getName().contains(string))
                commands.add(command);
        }
    }

    private void setMentionUsers(String string) {
        if (commands == null) commands = new ArrayList<>();
        commands.clear();
        for (int i = 0; i < viewModel.getChannel().getChannelState().getMembers().size(); i++) {
            Member member = viewModel.getChannel().getChannelState().getMembers().get(i);
            User user = member.getUser();
            if (user.getName().contains(string))
                commands.add(user);
        }
    }
    // endregion
}
