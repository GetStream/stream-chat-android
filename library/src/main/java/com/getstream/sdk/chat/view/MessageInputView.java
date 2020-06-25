package com.getstream.sdk.chat.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.BuildCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding;
import com.getstream.sdk.chat.enums.InputType;
import com.getstream.sdk.chat.enums.MessageInputType;
import com.getstream.sdk.chat.interfaces.MessageSendListener;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.navigation.destinations.CameraDestination;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.utils.CaptureController;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration;
import com.getstream.sdk.chat.utils.MessageInputController;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.TextViewUtils;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bolts.Capture;


/**
 * Rich Message Input View component, allows you to:
 * - type messages
 * - run slash commands
 * - emoticons
 * - file uploads
 * - send typing events
 * <p>
 * The view is made reusable by allowing
 * - Customization via attrs/style
 * - Data binding
 */
public class MessageInputView extends RelativeLayout {

    /**
     * Tag for logging purposes
     */
    final String TAG = MessageInputView.class.getSimpleName();

    private StreamViewMessageInputBinding binding;
    /**
     * Styling class for the MessageInput
     */
    private MessageInputStyle style;
    /**
     * Fired when a message is sent
     */
    private MessageSendListener messageSendListener;
    /**
     * Permission Request listener
     */
    private PermissionRequestListener permissionRequestListener;
    /**
     * The viewModel for handling typing etc.
     */
    protected ChannelViewModel viewModel;

    private MessageInputController messageInputController;
    private Unregistrar keyboardListener;

    // region constructor
    public MessageInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttr(context, attrs);
        binding = initBinding(context);
        applyStyle();
    }
    // endregion

    // region init
    private StreamViewMessageInputBinding initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return StreamViewMessageInputBinding.inflate(inflater, this, true);
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        style = new MessageInputStyle(context, attrs);
    }

    public void setViewModel(ChannelViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;
        binding.setLifecycleOwner(lifecycleOwner);
        init();
        observeUIs(lifecycleOwner);
    }

    private void init() {
        binding.setActiveMessageSend(false);
        configOnClickListener();
        configInputEditText();
        configAttachmentUI();
        onBackPressed();
        setKeyboardEventListener();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (keyboardListener != null) keyboardListener.unregister();
        super.onDetachedFromWindow();
    }

    private void applyStyle() {
        // Attachment Button
        binding.ivOpenAttach.setVisibility(style.isShowAttachmentButton() ? VISIBLE : GONE);
        binding.ivOpenAttach.setImageDrawable(style.getAttachmentButtonIcon(false));
        binding.ivOpenAttach.getLayoutParams().width = style.getAttachmentButtonWidth();
        binding.ivOpenAttach.getLayoutParams().height = style.getAttachmentButtonHeight();
        // Send Button
        binding.ivSend.setImageDrawable(style.getInputButtonIcon(false));
        binding.ivSend.getLayoutParams().width = style.getInputButtonWidth();
        binding.ivSend.getLayoutParams().height = style.getInputButtonHeight();
        // Input Background
        binding.llComposer.setBackground(style.getInputBackground());
        // Input Text
        style.inputText.apply(binding.etMessage);
        style.inputBackgroundText.apply(binding.tvTitle);
        style.inputBackgroundText.apply(binding.tvCommand);
        style.inputBackgroundText.apply(binding.tvUploadPhotoVideo);
        style.inputBackgroundText.apply(binding.tvUploadFile);
        style.inputBackgroundText.apply(binding.tvUploadCamera);
    }

    private void configOnClickListener() {
        binding.ivSend.setOnClickListener(view -> onSendMessage());
        binding.ivOpenAttach.setOnClickListener(view -> {
            binding.setIsAttachFile(true);
            messageInputController.onClickOpenBackGroundView(MessageInputType.ADD_FILE);
            if (!PermissionChecker.isGrantedCameraPermissions(getContext())
                    && permissionRequestListener != null
                    && !style.passedPermissionCheck())
                permissionRequestListener.openPermissionRequest();
        });
    }

    private void configInputEditText() {
        binding.etMessage.setOnFocusChangeListener((View view, boolean hasFocus) -> {
            viewModel.setInputType(hasFocus ? InputType.SELECT : InputType.DEFAULT);
            if (hasFocus) {
                Utils.showSoftKeyboard((Activity) getContext());
            } else
                Utils.hideSoftKeyboard((Activity) getContext());
        });
        TextViewUtils.afterTextChanged(binding.etMessage, this::keyStroke);
        binding.etMessage.setCallback(this::sendGiphyFromKeyboard);
    }

    private void keyStroke(Editable editable) {
        if (editable.toString().length() > 0)
            viewModel.keystroke();

        String messageText = getMessageText();
        // detect commands
        messageInputController.checkCommand(messageText);
        String s_ = messageText.replaceAll("\\s+", "");
        if (TextUtils.isEmpty(s_))
            binding.setActiveMessageSend(false);
        else
            binding.setActiveMessageSend(messageText.length() != 0);
        configSendButtonEnableState();
    }

    private void configMessageInputBackground(LifecycleOwner lifecycleOwner) {

        viewModel.getInputType().observe(lifecycleOwner, inputType -> {
            switch (inputType) {
                case DEFAULT:
                    binding.llComposer.setBackground(style.getInputBackground());
                    binding.ivOpenAttach.setImageDrawable(style.getAttachmentButtonIcon(false));
                    binding.ivSend.setImageDrawable(style.getInputButtonIcon(viewModel.isEditing()));
                    break;
                case SELECT:
                    binding.llComposer.setBackground(style.getInputSelectedBackground());
                    binding.ivOpenAttach.setImageDrawable(style.getAttachmentButtonIcon(true));
                    binding.ivSend.setImageDrawable(style.getInputButtonIcon(false));
                    break;
                case EDIT:
                    binding.llComposer.setBackground(style.getInputEditBackground());
                    binding.ivOpenAttach.setImageDrawable(style.getAttachmentButtonIcon(true));
                    binding.ivSend.setImageDrawable(style.getInputButtonIcon(true));
                    messageInputController.onClickOpenBackGroundView(MessageInputType.EDIT_MESSAGE);
                    break;
            }
        });

    }

    private void configSendButtonEnableState() {
        List<Attachment> attachments = messageInputController.getSelectedAttachments();
        boolean hasAttachment = attachments != null && !attachments.isEmpty();
        boolean notEmptyMessage = !StringUtility.isEmptyTextMessage(getMessageText()) || (!messageInputController.isUploadingFile() && hasAttachment);
        binding.setActiveMessageSend(notEmptyMessage);
    }

    private void configAttachmentUI() {
        // TODO: make the attachment UI into it's own view and allow you to change it.
        messageInputController = new MessageInputController(getContext(), binding, this.viewModel, style, attachment -> {
            if (binding.ivSend.isEnabled()) return;
            for (Attachment attachment_ : messageInputController.getSelectedAttachments())
                if (!attachment_.config.isUploaded())
                    return;

            onSendMessage();
        });
        binding.rvMedia.setLayoutManager(new GridLayoutManager(getContext(), 4, RecyclerView.VERTICAL, false));
        binding.rvMedia.hasFixedSize();
        binding.rvComposer.setLayoutManager(new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false));
        int spanCount = 4;  // 4 columns
        int spacing = 2;    // 1 px
        boolean includeEdge = false;
        binding.rvMedia.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        binding.btnClose.setOnClickListener(v -> {
            messageInputController.onClickCloseBackGroundView();
            Utils.hideSoftKeyboard((Activity) getContext());
            if (viewModel.isEditing()) {
                initSendMessage();
                clearFocus();
            }
        });

        binding.llMedia.setOnClickListener(v -> messageInputController.onClickOpenSelectView(null, true));

        binding.llCamera.setOnClickListener(v -> {
            if (!PermissionChecker.isGrantedCameraPermissions(getContext())) {
                PermissionChecker.showPermissionSettingDialog(getContext(), getContext().getString(R.string.stream_camera_permission_message));
                return;
            }
            Utils.setButtonDelayEnable(v);
            messageInputController.onClickCloseBackGroundView();
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Utils.hideSoftKeyboard((Activity) getContext());

            StreamChat.getNavigator().navigate(new CameraDestination((Activity) getContext()));
        });
        binding.llFile.setOnClickListener(v -> messageInputController.onClickOpenSelectView(null, false));
    }

    private void onBackPressed() {
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (viewModel.isThread()) {
                    viewModel.initThread();
                    initSendMessage();
                    return true;
                }
                if (viewModel.isEditing()) {
                    messageInputController.onClickCloseBackGroundView();
                    initSendMessage();
                    return true;
                }
                if (!TextUtils.isEmpty(getMessageText())) {
                    initSendMessage();
                    return true;
                }

                if (binding.clTitle.getVisibility() == VISIBLE) {
                    messageInputController.onClickCloseBackGroundView();
                    initSendMessage();
                    return true;
                }

                return false;
            }
            return false;
        });
    }

    private void setKeyboardEventListener() {
        keyboardListener = KeyboardVisibilityEvent.registerEventListener((Activity) getContext(), (boolean isOpen) -> {
            if (!isOpen) {
                binding.etMessage.clearFocus();
                onBackPressed();
            }
        });
    }

    public void setEnabled(boolean enabled) {
        binding.etMessage.setEnabled(true);
    }

    public void clearFocus() {
        binding.etMessage.clearFocus();
    }

    public String getMessageText() {
        return binding.etMessage.getText().toString();
    }

    public void setMessageText(String text) {
        if (TextUtils.isEmpty(text)) return;

        binding.etMessage.requestFocus();
        binding.etMessage.setText(text);
        binding.etMessage.setSelection(binding.etMessage.getText().length());
    }
    // endregion

    // region observe
    private void observeUIs(LifecycleOwner lifecycleOwner) {
        configMessageInputBackground(lifecycleOwner);
        viewModel.getEditMessage().observe(lifecycleOwner, this::editMessage);
        viewModel.getMessageListScrollUp().observe(lifecycleOwner, messageListScrollup -> {
            if (messageListScrollup)
                Utils.hideSoftKeyboard((Activity) getContext());
        });
        viewModel.getThreadParentMessage().observe(lifecycleOwner, threadParentMessage -> {
            if (threadParentMessage == null) {
                initSendMessage();
                Utils.hideSoftKeyboard((Activity) getContext());
            }
        });
    }
    // endregion

    // region send message

    /**
     * Prepare message takes the message input string and returns a message object
     * You can overwrite this method in case you want to attach more custom properties to the message
     */
    private void onSendMessage(Message message, MessageCallback callback) {
        if (isEdit())
            viewModel.editMessage(message, callback);
        else
            viewModel.sendMessage(message, callback);
    }

    protected void onSendMessage() {
        Message message = isEdit() ? getEditMessage() : new Message(getMessageText());
        onSendMessage(isEdit() ? prepareEditMessage(message) : prepareNewMessage(message));
        if (isEdit())
            Utils.hideSoftKeyboard((Activity) getContext());
    }

    protected void onSendMessage(Message message) {
        binding.ivSend.setEnabled(false);
        onSendMessage(message, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                if (messageSendListener != null)
                    messageSendListener.onSendMessageSuccess(response.getMessage());
                initSendMessage();
                if (isEdit()) clearFocus();
            }

            @Override
            public void onError(String errMsg, int errCode) {
                if (messageSendListener != null)
                    messageSendListener.onSendMessageError(errMsg);
                initSendMessage();
                if (isEdit()) clearFocus();
            }
        });
    }

    private void initSendMessage() {
        messageInputController.initSendMessage();
        viewModel.setEditMessage(null);
        binding.etMessage.setText("");
        binding.ivSend.setEnabled(true);
    }

    /**
     * Prepare message takes the message input string and returns a message object
     * You can overwrite this method in case you want to attach more custom properties to the message
     */
    protected Message prepareNewMessage(Message message) {
        // Check file uploading
        if (messageInputController.isUploadingFile()) {
            message.setUser(viewModel.client().getUser());
            String clientSideID = viewModel.getChannel().getClient().generateMessageID();
            message.setId(clientSideID);
            message.setCreatedAt(new Date());
            message.setSyncStatus(Sync.LOCAL_UPDATE_PENDING);
        } else
            message.setAttachments(messageInputController.getSelectedAttachments());
        return message;
    }

    protected Message prepareEditMessage(Message message) {
        message.setText(getMessageText());
        List<Attachment> newAttachments = messageInputController.getSelectedAttachments();
        message.setAttachments(newAttachments);
        return message;
    }

    protected Message getEditMessage() {
        return viewModel.getEditMessage().getValue();
    }
    // endregion

    // region send giphy from keyboard
    private boolean sendGiphyFromKeyboard(InputContentInfoCompat inputContentInfo,
                                          int flags, Bundle opts) {
        if (BuildCompat.isAtLeastQ()
                && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                inputContentInfo.requestPermission();
            } catch (Exception e) {
                return false;
            }
        }
        if (inputContentInfo.getLinkUri() == null) return false;

        String url = inputContentInfo.getLinkUri().toString();
        Attachment attachment = new Attachment();
        attachment.setThumbURL(url);
        attachment.setTitleLink(url);
        attachment.setTitle(inputContentInfo.getDescription().getLabel().toString());
        attachment.setType(ModelType.attach_giphy);

        List<Attachment> selectedAttachments = new ArrayList<>();
        selectedAttachments.add(attachment);

        messageInputController.setSelectedAttachments(selectedAttachments);
        binding.etMessage.setText("");
        onSendMessage();
        return true;
    }
    // endregion

    protected boolean isEdit() {
        return viewModel.isEditing();
    }

    // region edit message
    protected void editMessage(@Nullable Message message) {
        if (message == null) return;

        // Set Text to Inputbox
        setMessageText(message.getText());
        binding.etMessage.requestFocus();

        List<Attachment> attachments = new ArrayList<>(message.getAttachments());
        if (!attachments.isEmpty())
            binding.ivOpenAttach.setVisibility(GONE);
        // Set Attachments to Inputbox
        if (attachments.isEmpty()
                || attachments.get(0).getType().equals(ModelType.attach_giphy)
                || attachments.get(0).getType().equals(ModelType.attach_unknown))
            return;

        for (Attachment attachment : attachments)
            attachment.config.setUploaded(true);

        Attachment attachment = attachments.get(0);
        if (attachment.getType().equals(ModelType.attach_file)) {
            String fileType = attachment.getMime_type();
            if (fileType.equals(ModelType.attach_mime_mov) ||
                    fileType.equals(ModelType.attach_mime_mp4)) {
                messageInputController.onClickOpenSelectView(attachments, true);
            } else {
                messageInputController.onClickOpenSelectView(attachments, false);
            }
        } else {
            messageInputController.onClickOpenSelectView(attachments, true);
        }
    }

    // endregion

    // region permission check
    public void captureMedia(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.CAPTURE_IMAGE_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            File imageFile = CaptureController.getCaptureFile(true);
            File vieoFile = CaptureController.getCaptureFile(false);
            if (imageFile == null && vieoFile == null) {
                Utils.showMessage(getContext(), getContext().getString(R.string.stream_take_photo_failed));
                return;
            }
            if (imageFile != null && imageFile.length() > 0) {
                messageInputController.progressCapturedMedia(imageFile, CaptureController.getImage(), true);
                updateGallery(imageFile);
            } else if (vieoFile != null && vieoFile.length() > 0) {
                messageInputController.progressCapturedMedia(vieoFile, CaptureController.getVideo(),false);
                updateGallery(vieoFile);
            } else
                Utils.showMessage(getContext(), getContext().getString(R.string.stream_take_photo_failed));
        }
    }

    private void updateGallery(File outputFile) {
        final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        final Uri contentUri = Uri.fromFile(outputFile);
        scanIntent.setData(contentUri);
        getContext().sendBroadcast(scanIntent);
    }

    /*Used for handling requestPermissionsResult*/
    public void permissionResult(int requestCode, @NonNull String[] permissions,
                                 @NonNull int[] grantResults) {
        if (requestCode == Constant.PERMISSIONS_REQUEST) {
            boolean storageGranted = true, cameraGranted = true;
            String permission;
            int grantResult;
            for (int i = 0; i < permissions.length; i++) {
                permission = permissions[i];
                grantResult = grantResults[i];
                if (permission.equals(Manifest.permission.CAMERA)) {
                    cameraGranted = grantResult == PackageManager.PERMISSION_GRANTED;
                } else if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    storageGranted = false;
                }
            }

            if (storageGranted && cameraGranted) {
                messageInputController.onClickOpenBackGroundView(MessageInputType.ADD_FILE);
                style.setCheckPermissions(true);
            } else {
                String message;
                if (!storageGranted && !cameraGranted) {
                    message = getContext().getString(R.string.stream_both_permissions_message);
                } else if (!cameraGranted) {
                    style.setCheckPermissions(true);
                    message = getContext().getString(R.string.stream_camera_permission_message);
                } else {
                    style.setCheckPermissions(true);
                    message = getContext().getString(R.string.stream_storage_permission_message);
                }
                PermissionChecker.showPermissionSettingDialog(getContext(), message);
            }
            messageInputController.configPermissions();
        }
    }

    // endregion

    // region listeners
    protected void setMessageSendListener(MessageSendListener manager) {
        this.messageSendListener = manager;
    }

    public void setPermissionRequestListener(PermissionRequestListener l) {
        this.permissionRequestListener = l;
    }

    /**
     * This interface is called when you add an attachment
     */
    public interface AttachmentListener {
        void onAddAttachment(Attachment attachment);
    }

    /**
     * Interface for Permission request
     */
    public interface PermissionRequestListener {
        void openPermissionRequest();
    }

    // endregion
}