package com.getstream.sdk.chat.function;

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
import com.getstream.sdk.chat.adapter.CommandListItemAdapter;
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter;
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter;
import com.getstream.sdk.chat.databinding.ViewMessageInputBinding;
import com.getstream.sdk.chat.enums.InputType;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Command;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.interfaces.SendFileCallback;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.FileSendResponse;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SendFileFunction {

    private static final String TAG = SendFileFunction.class.getSimpleName();

    ChannelViewModel viewModel;
    MediaAttachmentAdapter mediaAttachmentAdapter = null;
    MediaAttachmentSelectedAdapter selectedMediaAttachmentAdapter = null;
    private List<Attachment> selectedAttachments = null;


    CommandListItemAdapter commandListItemAdapter = null;
    List<Object> commands = null;

    Context context;
    ViewMessageInputBinding binding;
    ChannelState channelResponse;

    public SendFileFunction(Context context, ViewMessageInputBinding binding, ChannelViewModel viewModel) {
        this.context = context;
        this.binding = binding;
        this.viewModel = viewModel;
    }

    public List<Attachment> getSelectedAttachments() {
        return selectedAttachments;
    }

    // region Attachment

    public void onClickAttachmentViewOpen(View v) {
        if (selectedAttachments != null && !selectedAttachments.isEmpty()) return;
        openAnimationView(binding.clAddFile);
        fadeAnimationView(binding.ivBackAttachment, true);
    }

    public void onClickAttachmentViewClose(View v) {
        closeAnimationView(binding.clAddFile);
        closeAnimationView(binding.clSelectPhoto);
        closeAnimationView(binding.clCommand);
        fadeAnimationView(binding.ivBackAttachment, false);
    }

    private void initLoadAttachemtView() {
        binding.rvComposer.setVisibility(View.GONE);
        binding.lvComposer.setVisibility(View.GONE);
        binding.progressBarFileLoader.setVisibility(View.VISIBLE);
        closeAnimationView(binding.clAddFile);
        openAnimationView(binding.clSelectPhoto);
    }

    public void openAnimationView(View view) {
        if (view.getVisibility() == View.VISIBLE) return;
        view.setVisibility(View.VISIBLE);
    }

    public void closeAnimationView(View view) {

        if (view.getVisibility() != View.VISIBLE) return;
        view.setVisibility(View.GONE);
    }

    public void fadeAnimationView(View view, boolean isFadeIn) {
        if (isFadeIn) {
            if (view.getVisibility() == View.VISIBLE) return;

            view.setVisibility(View.VISIBLE);
            view.setClickable(true);
        } else {
            if (view.getVisibility() == View.GONE) return;
            view.setVisibility(View.GONE);
        }
    }

    private void configSelectAttachView(boolean isMedia, List<Attachment> editAttachments) {
        if (editAttachments != null) {
            selectedAttachments = editAttachments;
            fadeAnimationView(binding.ivBackAttachment, true);
        } else {
            selectedAttachments = new ArrayList<>();
        }

        binding.setIsAttachFile(!isMedia);
        if (isMedia) {
            List<Attachment> attachments = Global.getAllShownImagesPath(context);
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
        } else {
            Global.attachments = new ArrayList<>();
            List<Attachment> attachments = Global.Search_Dir(Environment.getExternalStorageDirectory());
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
        }
    }

    // endregion

    // region Media

    public void onClickSelectMediaViewOpen(View v, List<Attachment> editAttachments) {
        initLoadAttachemtView();
        binding.tvInputboxBack.setVisibility(View.VISIBLE);
        AsyncTask.execute(() -> configSelectAttachView(true, editAttachments));
    }

    public void onClickSelectMediaViewClose(View v) {
        closeAnimationView(binding.clSelectPhoto);
        fadeAnimationView(binding.ivBackAttachment, false);
    }

    private void updateComposerViewBySelectedMedia(List<Attachment> attachments, Attachment attachment) {
        binding.rvComposer.setVisibility(View.VISIBLE);
        if (selectedAttachments == null) selectedAttachments = new ArrayList<>();
        if (attachment.config.isSelected()) {
            selectedAttachments.add(attachment);

            channelResponse.getChannel().sendFile(attachment, attachment.getType().equals(ModelType.attach_image), new SendFileCallback() {
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
//            binding.setActiveMessageComposer(true);
            binding.setActiveMessageSend(true);
            viewModel.setInputType(InputType.SELECT);
        } else if (binding.etMessage.getText().toString().length() == 0) {
//            binding.setActiveMessageComposer(false);
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
//                binding.setActiveMessageComposer(false);
                viewModel.setInputType(InputType.DEFAULT);
                binding.setActiveMessageSend(false);
            }
        });
        binding.rvComposer.setAdapter(selectedMediaAttachmentAdapter);
    }
    // endregion

    // region File

    AttachmentListAdapter fileAttachmentAdapter = null;
    AttachmentListAdapter selectedFileAttachmentAdapter = null;

    public void onClickSelectFileViewOpen(View v, List<Attachment> editAttachments) {
        initLoadAttachemtView();
        binding.tvInputboxBack.setVisibility(View.VISIBLE);
        AsyncTask.execute(() -> configSelectAttachView(false, editAttachments));
    }

    private void updateComposerViewBySelectedFile(List<Attachment> attachments, Attachment attachment) {
        binding.lvComposer.setVisibility(View.VISIBLE);
        if (selectedAttachments == null) selectedAttachments = new ArrayList<>();
        if (attachment.config.isSelected()) {
            selectedAttachments.add(attachment);
            channelResponse.getChannel().sendFile(attachment, false, new SendFileCallback() {
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
//            binding.setActiveMessageComposer(true);
            binding.setActiveMessageSend(true);
        } else if (binding.etMessage.getText().toString().length() == 0) {
//            binding.setActiveMessageComposer(false);
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
//                binding.setActiveMessageComposer(false);
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
        onClickAttachmentViewClose(null);
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
        openAnimationView(binding.clCommand);
        fadeAnimationView(binding.ivBackAttachment, true);
        binding.tvInputboxBack.setVisibility(View.VISIBLE);
    }

    private void closeCommandView() {
        closeAnimationView(binding.clCommand);
        fadeAnimationView(binding.ivBackAttachment, false);
        commands = null;
    }

    public void checkCommand(String text) {
        if (TextUtils.isEmpty(text) || (!text.startsWith("/") && !text.contains("@"))) {
            closeCommandView();
            return;
        }

        if (text.length() == 1) {
            onClickCommandViewOpen(text.startsWith("/"));
        } else if (text.endsWith("@") && binding.clCommand.getVisibility() != View.VISIBLE) {
            onClickCommandViewOpen(false);
        } else {
            setCommandsMentionUsers(text);
            if (!commands.isEmpty() && binding.clCommand.getVisibility() != View.VISIBLE)
                openCommandView();

            if (commandListItemAdapter != null)
                commandListItemAdapter.notifyDataSetChanged();

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
        String title = binding.tvCommandTitle.getContext().getResources().getString(isCommand ? R.string.command_title : R.string.mention_title);
        binding.tvCommandTitle.setText(title);
        binding.tvCommand.setText("");
        commandListItemAdapter = new CommandListItemAdapter(this.context, commands, isCommand);
        binding.lvCommand.setAdapter(commandListItemAdapter);
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
        binding.tvCommand.setText(string_);
        if (commands == null) commands = new ArrayList<>();
        commands.clear();
        if (string.startsWith("/")) {
            setCommands(string_);
        } else {
            setMentionUsers(string_);
        }
    }

    private void setCommands(String string) {
        if (commands == null) commands = new ArrayList<>();
        commands.clear();
        for (int i = 0; i < channelResponse.getChannel().getConfig().getCommands().size(); i++) {
            Command command = channelResponse.getChannel().getConfig().getCommands().get(i);
            if (command.getName().contains(string))
                commands.add(command);
        }
    }

    private void setMentionUsers(String string) {
        if (commands == null) commands = new ArrayList<>();
        commands.clear();
        for (int i = 0; i < channelResponse.getMembers().size(); i++) {
            Member member = channelResponse.getMembers().get(i);
            User user = member.getUser();
            if (user.getName().contains(string))
                commands.add(user);
        }
    }
    // endregion

    // region Mention

    // endregion
}
