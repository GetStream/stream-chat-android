package com.getstream.sdk.chat.view;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration;
import com.getstream.sdk.chat.utils.MessageInputController;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
public class MessageInputView extends RelativeLayout
        implements View.OnClickListener, TextWatcher, View.OnFocusChangeListener {

    /**
     * Tag for logging purposes
     */
    final String TAG = MessageInputView.class.getSimpleName();
    /** Store the image if you take a picture */
    Uri imageUri;
    /** If you are allowed to scroll up or not */
    boolean lockScrollUp = false;

    private StreamViewMessageInputBinding binding;
    /**
     * Styling class for the MessageInput
     */
    private MessageInputStyle style;
    /** Fired when a message is sent */
    private SendMessageListener sendMessageListener;
    /** Permission Request listener */
    private PermissionRequestListener permissionRequestListener;
    /** Camera view listener */
    private OpenCameraViewListener openCameraViewListener;
    /**
     * The viewModel for handling typing etc.
     */
    private ChannelViewModel viewModel;
    
    private MessageInputController messageInputController;

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

    private void init() {
        binding.setActiveMessageSend(false);
        binding.ivSend.setOnClickListener(this);
        binding.ivOpenAttach.setOnClickListener(this);
        binding.etMessage.setOnFocusChangeListener(this);
        binding.etMessage.addTextChangedListener(this);
        binding.etMessage.setCallback((InputContentInfoCompat inputContentInfo,
                                       int flags, Bundle opts) -> {
            if (BuildCompat.isAtLeastQ()
                    && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                try {
                    inputContentInfo.requestPermission();
                } catch (Exception e) {
                    return false;
                }
            }
            if (inputContentInfo.getLinkUri() == null)
                return false;

            String url = inputContentInfo.getLinkUri().toString();
            Log.d(TAG, "getLinkUri:" + url);
            Attachment attachment = new Attachment();
            attachment.setThumbURL(url);
            attachment.setTitleLink(url);
            attachment.setTitle(inputContentInfo.getDescription().getLabel().toString());
            attachment.setType(ModelType.attach_giphy);
            messageInputController.setSelectedAttachments(Arrays.asList(attachment));
            onSendMessage("", viewModel.isEditing());
            return true;
        });


        onBackPressed();
        initAttachmentUI();
        KeyboardVisibilityEvent.setEventListener(
                (Activity) getContext(), (boolean isOpen) -> {
                    if (!isOpen) {
                        binding.etMessage.clearFocus();
                        onBackPressed();
                    }
                });
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

    // Edit
    private void editMessage(Message message) {
        if (message == null) return;
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

    private void initAttachmentUI() {
        // TODO: make the attachment UI into it's own view and allow you to change it.
        messageInputController = new MessageInputController(getContext(), binding, this.viewModel, style);
        binding.rvMedia.setLayoutManager(new GridLayoutManager(getContext(), 4, RecyclerView.VERTICAL, false));
        binding.rvMedia.hasFixedSize();
        binding.rvComposer.setLayoutManager(new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false));
        int spanCount = 4;  // 4 columns
        int spacing = 2;    // 1 px
        boolean includeEdge = false;
        binding.rvMedia.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        binding.tvClose.setOnClickListener(v -> {
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

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri = getContext().getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            Intent chooserIntent = Intent.createChooser(takePictureIntent, "Capture Image or Video");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeVideoIntent});

            if (this.openCameraViewListener != null)
                openCameraViewListener.openCameraView(chooserIntent, Constant.CAPTURE_IMAGE_REQUEST_CODE);
        });
        binding.llFile.setOnClickListener(v -> messageInputController.onClickOpenSelectFileView(v, null));
    }

    // endregion
    public void captureMedia(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.CAPTURE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                File file = null;
                try {
                    String path = imageUri.getPath();
                    file = new File(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (file != null) {
                    messageInputController.progressCapturedMedia(getContext(), imageUri, true);
                    imageUri = null;
                }
                return;
            }
            try {
                Uri uri = data.getData();
                if (uri == null) {
                    if (imageUri != null)
                        messageInputController.progressCapturedMedia(getContext(), imageUri, true);
                    imageUri = null;
                } else {
                    messageInputController.progressCapturedMedia(getContext(), uri, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                style.passedPermissionCheck(true);
            }else {
                String message;
                if (!storageGranted && !cameraGranted) {
                    message = getContext().getString(R.string.stream_both_permissions_message);
                } else if (!cameraGranted) {
                    style.passedPermissionCheck(true);
                    message = getContext().getString(R.string.stream_camera_permission_message);
                } else {
                    style.passedPermissionCheck(true);
                    message = getContext().getString(R.string.stream_storage_permission_message);
                }
                PermissionChecker.showPermissionSettingDialog(getContext(), message);
            }
            messageInputController.configPermissions();
        }
    }

    public Message getEditMessage() {
        return viewModel.getEditMessage().getValue();
    }

    public void setEnabled(boolean enabled) {
        binding.etMessage.setEnabled(true);
    }

    public void clearFocus() {
        binding.etMessage.clearFocus();
    }

    public boolean requestInputFocus() {
        return binding.etMessage.requestFocus();
    }

    public String getMessageText() {
        return binding.etMessage.getText().toString();
    }

    public void setMessageText(String t) {
        binding.etMessage.setText(t);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_send) {
            this.onSendMessage(binding.etMessage.getText().toString(), viewModel.isEditing());
        } else if (id == R.id.iv_openAttach) {
            binding.setIsAttachFile(true);
            messageInputController.onClickOpenBackGroundView(MessageInputType.ADD_FILE);
            if (!PermissionChecker.isGrantedCameraPermissions(getContext())
                    && permissionRequestListener != null
                    && !style.isPermissionSet())
                permissionRequestListener.openPermissionRequest();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //noop
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //noop
    }

    @Override
    public void afterTextChanged(Editable s) {
        String messageText = this.getMessageText();
        Log.i(TAG, "Length is " + s.length());
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
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        viewModel.setInputType(hasFocus ? InputType.SELECT : InputType.DEFAULT);
        if (hasFocus) {
            lockScrollUp = true;
            new Handler().postDelayed(() -> lockScrollUp = false, 500);
            Utils.showSoftKeyboard((Activity) getContext());
        } else
            Utils.hideSoftKeyboard((Activity) getContext());
    }

    /**
     Prepare message takes the message input string and returns a message object
     You can overwrite this method in case you want to attach more custom properties to the message
     */
    public Message prepareMessage(String input) {
        Message m = new Message();
        m.setStatus(null);
        m.setText(input);
        m.setAttachments(messageInputController.getSelectedAttachments());
        // set the thread id if we are viewing a thread
        if (viewModel.isThread())
            m.setParentId(viewModel.getThreadParentMessage().getValue().getId());
        // set the current user
        m.setUser(viewModel.client().getUser());
        return m;
    }

    private void onSendMessage(String input, boolean isEdit) {
        binding.ivSend.setEnabled(false);

        if (isEdit) {
            Message message = getEditMessage();
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

            viewModel.getChannel().updateMessage(message,  new MessageCallback() {
                @Override
                public void onSuccess(MessageResponse response) {
                    initSendMessage();
                    binding.ivSend.setEnabled(true);
                    clearFocus();
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    initSendMessage();
                    binding.ivSend.setEnabled(true);
                    clearFocus();
                }
            });
        } else {
            Message m = prepareMessage(input);
            viewModel.sendMessage(m, new MessageCallback() {
                @Override
                public void onSuccess(MessageResponse response) {
                    binding.ivSend.setEnabled(true);
                    initSendMessage();
                    if (sendMessageListener != null)
                        sendMessageListener.onSendMessageSuccess(response.getMessage());
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    initSendMessage();
                    binding.ivSend.setEnabled(true);
                    if (sendMessageListener != null) {
                        sendMessageListener.onSendMessageError(errMsg);
                    } else {
                        Utils.showMessage(getContext(), errMsg);
                    }
                }
            });
        }
    }

    private void initSendMessage() {
        messageInputController.initSendMessage();
        viewModel.setEditMessage(null);
        binding.etMessage.setText("");
    }

    // region Set Listeners
    public void setOnSendMessageListener(SendMessageListener l) {
        this.sendMessageListener = l;
    }

    public void setPermissionRequestListener(PermissionRequestListener l) {
        this.permissionRequestListener = l;
    }

    public void setOpenCameraViewListener(OpenCameraViewListener l) {
        this.openCameraViewListener = l;
    }


    // region Listeners

    /**
     * Used for listening to the sendMessage event
     */
    public interface SendMessageListener {
        void onSendMessageSuccess(Message message);
        void onSendMessageError(String errMsg);

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