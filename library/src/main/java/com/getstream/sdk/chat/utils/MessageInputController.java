package com.getstream.sdk.chat.utils;

import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.AttachmentListAdapter;
import com.getstream.sdk.chat.adapter.CommandMentionListItemAdapter;
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter;
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter;
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding;
import com.getstream.sdk.chat.enums.InputType;
import com.getstream.sdk.chat.enums.MessageInputType;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Command;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.interfaces.UploadFileCallback;
import com.getstream.sdk.chat.rest.response.UploadFileResponse;
import com.getstream.sdk.chat.view.MessageInputStyle;
import com.getstream.sdk.chat.view.MessageInputView;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageInputController {

    private static final String TAG = MessageInputController.class.getSimpleName();

    private ChannelViewModel viewModel;
    private Channel channel;
    private MessageInputStyle style;
    private MediaAttachmentAdapter mediaAttachmentAdapter = null;
    private MediaAttachmentSelectedAdapter selectedMediaAttachmentAdapter = null;
    private CommandMentionListItemAdapter<MessageInputStyle> commandMentionListItemAdapter;
    private List<Object> commands = null;
    private Context context;
    private StreamViewMessageInputBinding binding;
    private AttachmentListAdapter fileAttachmentAdapter = null;
    private AttachmentListAdapter selectedFileAttachmentAdapter = null;
    private MessageInputType messageInputType;
    private List<Attachment> selectedAttachments = null;
    private MessageInputView.AttachmentListener attachmentListener;
    private boolean uploadingFile = false;

    // region Attachment

    public MessageInputController(Context context,
                                  StreamViewMessageInputBinding binding,
                                  ChannelViewModel viewModel,
                                  MessageInputStyle style,
                                  MessageInputView.AttachmentListener attachmentListener) {
        this.context = context;
        this.binding = binding;
        this.viewModel = viewModel;
        this.channel = viewModel.getChannel();
        this.style = style;
        this.attachmentListener = attachmentListener;
    }

    public List<Attachment> getSelectedAttachments() {
        return selectedAttachments;
    }

    public boolean isUploadingFile() {
        return uploadingFile;
    }

    public void setSelectedAttachments(List<Attachment> selectedAttachments) {
        this.selectedAttachments = selectedAttachments;
    }

    public void onClickOpenBackGroundView(MessageInputType type) {

        binding.getRoot().setBackgroundResource(R.drawable.stream_round_thread_toolbar);
        binding.clTitle.setVisibility(View.VISIBLE);
        binding.btnClose.setVisibility(View.VISIBLE);

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
                binding.btnClose.setVisibility(View.GONE);
                binding.clCommand.setVisibility(View.VISIBLE);
                break;
        }
        binding.tvTitle.setText(type.getLabel());
        messageInputType = type;
        configPermissions();
    }

    public void configPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            binding.ivMediaPermission.setVisibility(View.GONE);
            binding.ivCameraPermission.setVisibility(View.GONE);
            binding.ivFilePermission.setVisibility(View.GONE);
            return;
        }

        if (PermissionChecker.isGrantedCameraPermissions(context)) {
            binding.ivMediaPermission.setVisibility(View.GONE);
            binding.ivCameraPermission.setVisibility(View.GONE);
            binding.ivFilePermission.setVisibility(View.GONE);
        } else if (PermissionChecker.isGrantedStoragePermissions(context)) {
            binding.ivMediaPermission.setVisibility(View.GONE);
            binding.ivCameraPermission.setVisibility(View.VISIBLE);
            binding.ivFilePermission.setVisibility(View.GONE);
        } else {
            binding.ivMediaPermission.setVisibility(View.VISIBLE);
            binding.ivCameraPermission.setVisibility(View.VISIBLE);
            binding.ivFilePermission.setVisibility(View.VISIBLE);
        }
    }

    public void onClickCloseBackGroundView() {
        binding.clTitle.setVisibility(View.GONE);
        binding.clAddFile.setVisibility(View.GONE);
        binding.clSelectPhoto.setVisibility(View.GONE);
        binding.clCommand.setVisibility(View.GONE);
        binding.getRoot().setBackgroundResource(0);
        messageInputType = null;
        commandMentionListItemAdapter = null;
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
                if (!attachments.isEmpty()){
                    mediaAttachmentAdapter = new MediaAttachmentAdapter(context, attachments, position -> {
                        Attachment attachment = getAttachment(attachments, position);
                        if (attachment == null) return;
                        mediaAttachmentAdapter.notifyItemChanged(position);
                        updateComposerView(attachments, attachment, true);
                    });
                    binding.rvMedia.setAdapter(mediaAttachmentAdapter);
                }else{
                    Utils.showMessage(context, context.getResources().getString(R.string.stream_no_media_error));
                    onClickCloseBackGroundView();
                }

                binding.progressBarFileLoader.setVisibility(View.GONE);
                // edit
                if (editAttachments != null) {
                    binding.rvComposer.setVisibility(View.VISIBLE);
                    setSelectedAttachmentAdapter(attachments, true);
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
                        Attachment attachment = getAttachment(attachments, position);
                        if (attachment == null) return;
                        fileAttachmentAdapter.notifyDataSetChanged();
                        updateComposerView(attachments, attachment, false);
                    });
                } else {
                    Utils.showMessage(context, context.getResources().getString(R.string.stream_no_file_error));
                    onClickCloseBackGroundView();
                }
                binding.progressBarFileLoader.setVisibility(View.GONE);
                // edit
                if (editAttachments != null) {
                    binding.lvComposer.setVisibility(View.VISIBLE);
                    setSelectedAttachmentAdapter(attachments, false);
                }
            });
        }
    }

    @Nullable
    private Attachment getAttachment(List<Attachment> attachments, int position) {
        Attachment attachment = attachments.get(position);
        File file = new File(attachment.config.getFilePath());
        if (file.length() > Constant.MAX_UPLOAD_FILE_SIZE) {
            Utils.showMessage(context, context.getResources().getString(R.string.stream_large_size_file_error));
            return null;
        }
        attachment.config.setSelected(!attachment.config.isSelected());
        return attachment;
    }

    public void onClickOpenSelectMediaView(View v, List<Attachment> editAttachments) {
        if (!PermissionChecker.isGrantedStoragePermissions(context)) {
            PermissionChecker.showPermissionSettingDialog(context, context.getString(R.string.stream_storage_permission_message));
            return;
        }
        initLoadAttachemtView();
        AsyncTask.execute(() -> configSelectAttachView(true, editAttachments));
        onClickOpenBackGroundView(MessageInputType.UPLOAD_MEDIA);
    }

    // endregion

    // region File

    private void updateComposerView(List<Attachment> attachments, Attachment attachment, boolean isMedia) {
        if (isMedia)
            binding.rvComposer.setVisibility(View.VISIBLE);
        else
            binding.lvComposer.setVisibility(View.VISIBLE);

        if (selectedAttachments == null) selectedAttachments = new ArrayList<>();

        if (attachment.config.isSelected()) {
            selectedAttachments.add(attachment);
            uploadingFile = true;
            UploadFileCallback callback = new UploadFileCallback<UploadFileResponse, Integer>() {
                @Override
                public void onSuccess(UploadFileResponse response) {
                    uploadingFile = false;
                    binding.setActiveMessageSend(true);
                    attachmentListener.onAddAttachments();

                    if (isMedia && attachment.getType().equals(ModelType.attach_image)){
                        File file = new File(attachment.config.getFilePath());
                        attachment.setImageURL(response.getFileUrl());
                        attachment.setFallback(file.getName());
                    }else{
                        attachment.setAssetURL(response.getFileUrl());
                    }

                    attachment.config.setUploaded(true);
                    if (isMedia)
                        selectedMediaAttachmentAdapter.notifyDataSetChanged();
                    else
                        selectedFileAttachmentAdapter.notifyDataSetChanged();

                }

                @Override
                public void onError(String errMsg, int errCode) {
                    uploadingFile = false;
                    binding.setActiveMessageSend(true);
                    attachment.config.setSelected(false);
                    Utils.showMessage(context, errMsg);
                    updateComposerView(attachments, attachment, true);
                }

                @Override
                public void onProgress(Integer percentage) {
                    uploadingFile = true;
                    attachment.config.setProgress(percentage);
                    if (isMedia)
                        selectedMediaAttachmentAdapter.notifyDataSetChanged();
                    else
                        selectedFileAttachmentAdapter.notifyDataSetChanged();
                }
            };

            if (isMedia && attachment.getType().equals(ModelType.attach_image)) {
                channel.sendImage(attachment.config.getFilePath(), "image/jpeg", callback);
            } else {
                channel.sendFile(attachment.config.getFilePath(), attachment.getMime_type(), callback);
            }
        } else
            selectedAttachments.remove(attachment);

        setSelectedAttachmentAdapter(attachments, isMedia);

        if (selectedAttachments.size() > 0) {
            viewModel.setInputType(InputType.SELECT);
        } else if (binding.etMessage.getText().toString().length() == 0) {
            viewModel.setInputType(InputType.DEFAULT);
            binding.setActiveMessageSend(false);
        }
    }

    private void setSelectedAttachmentAdapter(final List<Attachment> attachments, boolean isMedia) {
        if (isMedia){
            selectedMediaAttachmentAdapter = new MediaAttachmentSelectedAdapter(context, selectedAttachments, position -> {
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
        }else{
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
    }

    public void onClickOpenSelectFileView(View v, List<Attachment> editAttachments) {
        if (!PermissionChecker.isGrantedStoragePermissions(context)) {
            PermissionChecker.showPermissionSettingDialog(context, context.getString(R.string.stream_storage_permission_message));
            return;
        }
        initLoadAttachemtView();
        AsyncTask.execute(() -> configSelectAttachView(false, editAttachments));
        onClickOpenBackGroundView(MessageInputType.UPLOAD_FILE);
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

    public void progressCapturedMedia(File file, boolean isImage) {
        if (file.length()> Constant.MAX_UPLOAD_FILE_SIZE){
            Utils.showMessage(context, StreamChat.getStrings().get(R.string.stream_large_size_file_error));
            return;
        }
        convertMediaAttachment(file, isImage);
    }

    private void convertMediaAttachment(File file, boolean isImage) {
        Attachment attachment = new Attachment();
        attachment.config.setFilePath(file.getPath());
        attachment.config.setSelected(true);
        if (isImage) {
            attachment.setType(ModelType.attach_image);
        } else {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.fromFile(file));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long videolengh = Long.parseLong(time );
            retriever.release();
            attachment.config.setVideoLengh((int) (videolengh / 1000));
            attachment.setType(ModelType.attach_file);
            attachment.setMime_type(ModelType.attach_mime_mp4);
        }
        updateComposerView(null, attachment, true);
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
        return messageInputType != null && ((messageInputType == MessageInputType.COMMAND)
                || (messageInputType == MessageInputType.MENTION));
    }

    public void checkCommand(String text) {
        if (TextUtils.isEmpty(text)
                || (!text.startsWith("/") && !text.contains("@"))) {
            closeCommandView();
        }else if (text.length() == 1) {
            onClickCommandViewOpen(text.startsWith("/"));
        } else if (text.endsWith("@")) {
            onClickCommandViewOpen(false);
        } else {
            setCommandsMentionUsers(text);
            if (!commands.isEmpty() && binding.clCommand.getVisibility() != View.VISIBLE)
                openCommandView();

            setCommandMentionListItemAdapter(text.startsWith("/"));
        }

        if (commands == null || commands.isEmpty())
            closeCommandView();

    }

    private void onClickCommandViewOpen(boolean isCommand) {
        if (isCommand) {
            setCommands("");
        } else {
            setMentionUsers("");
        }
        String title = binding.tvTitle.getContext().getResources().getString(isCommand ? R.string.stream_input_type_command : R.string.stream_input_type_auto_mention);
        binding.tvTitle.setText(title);
        binding.tvCommand.setText("");
        setCommandMentionListItemAdapter(isCommand);

        openCommandView();
        binding.lvCommand.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            if (isCommand)
                binding.etMessage.setText("/" + ((Command) commands.get(position)).getName() + " ");
            else {
                String messageStr = binding.etMessage.getText().toString();
                String userName = ((User) commands.get(position)).getName();
                String converted = StringUtility.convertMentionedText(messageStr, userName);
                binding.etMessage.setText(converted);
            }
            binding.etMessage.setSelection(binding.etMessage.getText().length());
            closeCommandView();
        });
    }
    private void setCommandMentionListItemAdapter(boolean isCommand){
        if (commandMentionListItemAdapter  == null) {
            commandMentionListItemAdapter = new CommandMentionListItemAdapter(this.context, commands, style, isCommand);
            binding.lvCommand.setAdapter(commandMentionListItemAdapter);
        }else{
            commandMentionListItemAdapter.setCommand(isCommand);
            commandMentionListItemAdapter.setCommands(commands);
            commandMentionListItemAdapter.notifyDataSetChanged();
        }
    }
    private void setCommandsMentionUsers(String string) {
        if (commands == null) commands = new ArrayList<>();
        commands.clear();
        if (string.startsWith("/")) {
            List<Command>commands = channel.getConfig().getCommands();
            if (commands == null || commands.isEmpty()) return;

            String commandStr = string.replace("/", "");
            setCommands(commandStr);
            binding.tvCommand.setText(commandStr);
        } else {
            String[] names = string.split("@");
            if (names.length > 0)
                setMentionUsers(names[names.length - 1]);
        }
    }

    private void setCommands(String string) {
        if (commands == null) commands = new ArrayList<>();
        commands.clear();
        for (int i = 0; i < channel.getConfig().getCommands().size(); i++) {
            Command command = channel.getConfig().getCommands().get(i);
            if (command.getName().contains(string))
                commands.add(command);
        }
    }

    private void setMentionUsers(String string) {
        Log.d(TAG, "Mention UserName: " + string);
        if (commands == null) commands = new ArrayList<>();
        commands.clear();
        List<Member> members = channel.getChannelState().getMembers();
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            User user = member.getUser();
            if (user.getName().toLowerCase().contains(string.toLowerCase()))
                commands.add(user);
        }
    }
    // endregion
}
