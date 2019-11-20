package com.getstream.sdk.chat.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
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
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding;
import com.getstream.sdk.chat.enums.InputType;
import com.getstream.sdk.chat.enums.MessageInputType;
import com.getstream.sdk.chat.interfaces.StreamMessageInputManager;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.EditTextUtils;
import com.getstream.sdk.chat.utils.CaptureController;
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration;
import com.getstream.sdk.chat.utils.MessageInputController;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


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

    /** If you are allowed to scroll up or not */
    boolean lockScrollUp = false;

    private StreamViewMessageInputBinding binding;
    /**
     * Styling class for the MessageInput
     */
    private MessageInputStyle style;
    /** Fired when a message is sent */
    private StreamMessageInputManager messageInputManager;
    /** Permission Request listener */
    private PermissionRequestListener permissionRequestListener;
    /** Camera view listener */
    private OpenCameraViewListener openCameraViewListener;
    /**
     * The viewModel for handling typing etc.
     */
    private ChannelViewModel viewModel;

    private MessageInputController messageInputController;

    /*The pendingMessage for uploading File*/
    private Message pendingMessage;

    // region constructor
    public MessageInputView(Context context) {
        super(context);
        binding = initBinding(context);
    }

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

    private void applyStyle() {
        // Attachment Button
        binding.ivOpenAttach.setVisibility(style.showAttachmentButton() ? VISIBLE : GONE);
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
        binding.etMessage.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getInputTextSize());
        binding.etMessage.setHint(style.getInputHint());
        binding.etMessage.setTextColor(style.getInputTextColor());
        binding.etMessage.setHintTextColor(style.getInputHintColor());
        binding.etMessage.setTypeface(Typeface.DEFAULT, style.getInputTextStyle());
    }

    private void configOnClickListener(){
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

    private void configInputEditText(){
        binding.etMessage.setOnFocusChangeListener((View view, boolean hasFocus)-> {
            viewModel.setInputType(hasFocus ? InputType.SELECT : InputType.DEFAULT);
            if (hasFocus) {
                lockScrollUp = true;
                postDelayed(() -> lockScrollUp = false, 500);
                Utils.showSoftKeyboard((Activity) getContext());
            } else
                Utils.hideSoftKeyboard((Activity) getContext());
        });

        EditTextUtils.afterTextChanged(binding.etMessage, editable -> {
            String messageText = getMessageText();
            Log.i(TAG, "Length is " + editable.length());
            if (messageText.length() > 0) {
                viewModel.keystroke();
            }
            // detect commands
            messageInputController.checkCommand(messageText);
            String s_ = messageText.replaceAll("\\s+","");
            if (TextUtils.isEmpty(s_))
                binding.setActiveMessageSend(false);
            else
                binding.setActiveMessageSend(messageText.length() != 0);
        });

        binding.etMessage.setCallback(this::sendGiphyFromKeyboard);
    }


    private void configAttachmentUI() {
        // TODO: make the attachment UI into it's own view and allow you to change it.
        messageInputController = new MessageInputController(getContext(), binding, this.viewModel, style,  ()-> {
            if (binding.ivSend.isEnabled()) return;
            for (Attachment attachment : messageInputController.getSelectedAttachments())
                if (!attachment.config.isUploaded())
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
            if (viewModel.isEditing()) {
                initSendMessage();
                clearFocus();
            }
        });

        binding.llMedia.setOnClickListener(v -> messageInputController.onClickOpenSelectMediaView(v, null));

        binding.llCamera.setOnClickListener(v -> {
            if (!PermissionChecker.isGrantedCameraPermissions(getContext())) {
                PermissionChecker.showPermissionSettingDialog(getContext(), getContext().getString(R.string.stream_camera_permission_message));
                return;
            }
            Utils.setButtonDelayEnable(v);
            messageInputController.onClickCloseBackGroundView();
            Intent takePictureIntent = CaptureController.getTakePictureIntent(getContext());
            Intent takeVideoIntent = CaptureController.getTakeVideoIntent(getContext());
            Intent chooserIntent = Intent.createChooser(takePictureIntent, getContext().getString(R.string.stream_input_camera_title));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeVideoIntent});
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            if (this.openCameraViewListener != null)
                openCameraViewListener.openCameraView(chooserIntent, Constant.CAPTURE_IMAGE_REQUEST_CODE);
        });
        binding.llFile.setOnClickListener(v -> messageInputController.onClickOpenSelectFileView(v, null));
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
                if (!TextUtils.isEmpty(binding.etMessage.getText().toString())) {
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

    private void setKeyboardEventListener(){
        KeyboardVisibilityEvent.setEventListener(
                (Activity) getContext(), (boolean isOpen) -> {
                    if (!isOpen) {
                        binding.etMessage.clearFocus();
                        onBackPressed();
                    }
                });
    }

    @NonNull
    private Message getEditMessage(String input) {
        Message message = viewModel.getEditMessage().getValue();
        message.setText(input);
        List<Attachment>newAttachments = messageInputController.getSelectedAttachments();
        if (newAttachments != null
                && !newAttachments.isEmpty()){
            List<Attachment>attachments = message.getAttachments();
            for (Attachment attachment : newAttachments){
                if (attachments == null)
                    attachments = new ArrayList<>();
                attachments.add(attachment);
            }
            message.setAttachments(attachments);
        }

        return message;
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

    public void setMessageText(String t) {
        binding.etMessage.setText(t);
    }
    // endregion

    // region observe
    private void observeUIs(LifecycleOwner lifecycleOwner) {
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

        viewModel.getEditMessage().observe(lifecycleOwner, this::editMessage);
        viewModel.getMessageListScrollUp().observe(lifecycleOwner, messageListScrollup -> {
            if (messageListScrollup && !lockScrollUp)
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
     Prepare message takes the message input string and returns a message object
     You can overwrite this method in case you want to attach more custom properties to the message
     */
    public void onSendMessage(Message message) {
        if (viewModel.isThread())
            message.setParentId(viewModel.getThreadParentMessage().getValue().getId());
        // set the current user
        message.setUser(viewModel.client().getUser());

        onSendMessage(message, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                if (messageInputManager != null)
                    messageInputManager.onSendMessageSuccess(response.getMessage());
            }

            @Override
            public void onError(String errMsg, int errCode) {
                if (messageInputManager != null)
                    messageInputManager.onSendMessageError(errMsg);
            }
        });
    }

    public void onSendMessage(Message message, MessageCallback callback) {
        if (isEdit())
            viewModel.editMessage(message, callback);
        else
            viewModel.sendMessage(message, callback);
    }

    private void onSendMessage() {
        String input = binding.etMessage.getText().toString();
        binding.ivSend.setEnabled(false);
        onSendMessage(isEdit() ? getEditMessage(input) : prepareMessage(input), new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                binding.ivSend.setEnabled(true);
                initSendMessage();
                if (isEdit())
                    clearFocus();
                if (messageInputManager != null)
                    messageInputManager.onSendMessageSuccess(response.getMessage());
            }

            @Override
            public void onError(String errMsg, int errCode) {
                initSendMessage();
                binding.ivSend.setEnabled(true);
                if (isEdit())
                    clearFocus();
                if (messageInputManager != null) {
                    messageInputManager.onSendMessageError(errMsg);
                } else {
                    Utils.showMessage(getContext(), errMsg);
                }
            }
        });
    }

    private void initSendMessage() {
        messageInputController.initSendMessage();
        viewModel.setEditMessage(null);
        binding.etMessage.setText("");
    }

    // endregion

    // region send giphy from keyboard
    private boolean sendGiphyFromKeyboard(InputContentInfoCompat inputContentInfo,
                                       int flags, Bundle opts){
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
        messageInputController.setSelectedAttachments(Arrays.asList(attachment));
        binding.etMessage.setText("");
        onSendMessage();
        return true;
    }
    // endregion

    private boolean isEdit(){
        return viewModel.isEditing();
    }
    // region edit message
    private void editMessage(Message message) {
        if (message == null) return;

        if (messageInputManager != null)
            messageInputManager.onEditMessage(message);
        // Set Text to Inputbox
        binding.etMessage.requestFocus();
        if (!TextUtils.isEmpty(message.getText())) {
            binding.etMessage.setText(message.getText());
            binding.etMessage.setSelection(binding.etMessage.getText().length());
        }

        // Set Attachments to Inputbox
        if (message.getAttachments() == null
                || message.getAttachments().isEmpty()
                || message.getAttachments().get(0).getType().equals(ModelType.attach_giphy)
                || message.getAttachments().get(0).getType().equals(ModelType.attach_unknown))
            return;

        for (Attachment attachment : message.getAttachments())
            attachment.config.setUploaded(true);

        Attachment attachment = message.getAttachments().get(0);
        if (attachment.getType().equals(ModelType.attach_file)) {
            String fileType = attachment.getMime_type();
            if (fileType.equals(ModelType.attach_mime_mov) ||
                    fileType.equals(ModelType.attach_mime_mp4)) {
                messageInputController.onClickOpenSelectMediaView(null, message.getAttachments());
            } else {
                messageInputController.onClickOpenSelectFileView(null, message.getAttachments());
            }
        } else {
            messageInputController.onClickOpenSelectMediaView(null, message.getAttachments());
        }
    }
    // endregion

    // region permission check
    public void captureMedia(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.CAPTURE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            File imageFile = CaptureController.getCaptureFile(true);
            File vieoFile = CaptureController.getCaptureFile(false);
            if (imageFile == null && vieoFile == null) {
                Utils.showMessage(getContext(), getContext().getString(R.string.stream_take_photo_failed));
                return;
            }
            if (imageFile != null && imageFile.length() > 0) {
                messageInputController.progressCapturedMedia(imageFile, true);
                updateGallery(imageFile);
            }else if (vieoFile != null && vieoFile.length() > 0) {
                messageInputController.progressCapturedMedia(vieoFile, false);
                updateGallery(vieoFile);
            }else
                Utils.showMessage(getContext(), getContext().getString(R.string.stream_take_photo_failed));
        }
    }

    private void updateGallery(File outputFile){
        final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        final Uri contentUri = Uri.fromFile(outputFile);
        scanIntent.setData(contentUri);
        getContext().sendBroadcast(scanIntent);
    }
    /*Used for handling requestPermissionsResult*/
    public void permissionResult(int requestCode, @NonNull String[] permissions,
                                 @NonNull int[] grantResults) {
        if (requestCode == Constant.PERMISSIONS_REQUEST) {
            boolean storageGranted  = true, cameraGranted = true;
            String permission; int grantResult;
            for (int i = 0; i < permissions.length; i++) {
                permission = permissions[i];
                grantResult = grantResults[i];
                if (permission.equals(Manifest.permission.CAMERA)){
                    cameraGranted = grantResult == PackageManager.PERMISSION_GRANTED;
                }else if(grantResult != PackageManager.PERMISSION_GRANTED) {
                    storageGranted = false;
                }
            }

            if (storageGranted && cameraGranted) {
                messageInputController.onClickOpenBackGroundView(MessageInputType.ADD_FILE);
                style.setCheckPermissions(true);
            }else {
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
    /**
     Prepare message takes the message input string and returns a message object
     You can overwrite this method in case you want to attach more custom properties to the message
     */
    public Message prepareMessage(String input) {
        Message m = new Message();
        m.setText(input);
        m.setAttachments(messageInputController.getSelectedAttachments());
        // set the thread id if we are viewing a thread
        if (viewModel.isThread())
            m.setParentId(viewModel.getThreadParentMessage().getValue().getId());
        // set the current user
        m.setUser(viewModel.client().getUser());
        // Check file uploading
        if (messageInputController.isUploadingFile()){
            String clientSideID = viewModel.getChannel().getClient().generateMessageID();
            m.setId(clientSideID);
            m.setCreatedAt(new Date());
            m.setSyncStatus(Sync.LOCAL_UPDATE_PENDING);
            m.setAttachments(null);
        }
        return m;
    }

    public void setMessageInputManager(StreamMessageInputManager messageInputManager) {
        this.messageInputManager = messageInputManager;
    }

    public void setPermissionRequestListener(PermissionRequestListener l) {
        this.permissionRequestListener = l;
    }

    public void setOpenCameraViewListener(OpenCameraViewListener l) {
        this.openCameraViewListener = l;
    }

    /**
     * This interface is called when you add an attachment
     */
    public interface AttachmentListener {
        void onAddAttachments();
    }
    /**
     * Interface for Permission request
     */
    public interface PermissionRequestListener {
        void openPermissionRequest();
    }
    /**
     * Interface for opening the camera view
     */
    public interface OpenCameraViewListener {
        void openCameraView(Intent intent, int REQUEST_CODE);
    }

    // endregion
}