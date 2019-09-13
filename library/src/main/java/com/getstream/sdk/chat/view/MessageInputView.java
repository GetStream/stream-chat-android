package com.getstream.sdk.chat.view;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
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

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding;
import com.getstream.sdk.chat.enums.InputType;
import com.getstream.sdk.chat.enums.MessageInputType;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.utils.MessageInputClient;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;


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

    final String TAG = MessageInputView.class.getSimpleName();
    Uri imageUri;
    // our connection to the channel scope
    private ChannelViewModel viewModel;
    // binding for this view
    private StreamViewMessageInputBinding binding;
    private MessageInputStyle style;
    // listeners
    private SendMessageListener sendMessageListener;
    private OpenCameraViewListener openCameraViewListener;

    // TODO Rename, it's not a function
    private MessageInputClient messageInputClient;

    // region Constructor
    public MessageInputView(Context context) {
        super(context);
        binding = initBinding(context);
    }
    // endregion

    public MessageInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttr(context, attrs);
        binding = initBinding(context);
        applyStyle();
    }

    // region Init
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

        onBackPressed();
        setOnSendMessageListener(viewModel);
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
                    messageInputClient.onClickCloseBackGroundView();
                    initSendMessage();
                    return true;
                }
                if (!TextUtils.isEmpty(binding.etMessage.getText().toString())) {
                    initSendMessage();
                    return true;
                }

                if (binding.clTitle.getVisibility() == VISIBLE) {
                    messageInputClient.onClickCloseBackGroundView();
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
                    messageInputClient.onClickOpenBackGroundView(MessageInputType.EDIT_MESSAGE);
                    break;
            }
        });

        viewModel.getEditMessage().observe(lifecycleOwner, this::editMessage);
        viewModel.getMessageListScrollUp().observe(lifecycleOwner, messageListScrollup -> {
            if (messageListScrollup && !lockScrollUp)
                Utils.hideSoftKeyboard((Activity) getContext());
        });
    }

    // Edit
    private void editMessage(Message message) {
        if (message == null) {
            return;
        }

        binding.etMessage.requestFocus();
        if (!TextUtils.isEmpty(message.getText())) {
            binding.etMessage.setText(message.getText());
            binding.etMessage.setSelection(binding.etMessage.getText().length());
        }
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            for (Attachment attachment : message.getAttachments())
                attachment.config.setUploaded(true);

            if (message.getAttachments().get(0).getType().equals(ModelType.attach_file)) {
                String fileType = message.getAttachments().get(0).getMime_type();
                if (fileType.equals(ModelType.attach_mime_mov) ||
                        fileType.equals(ModelType.attach_mime_mp4)) {
                    messageInputClient.onClickOpenSelectMediaView(null, message.getAttachments());
                } else {
                    messageInputClient.onClickOpenSelectFileView(null, message.getAttachments());
                }
            } else {
                messageInputClient.onClickOpenSelectMediaView(null, message.getAttachments());
            }
        }
    }

    private void initAttachmentUI() {
        // TODO: make the attachment UI into it's own view and allow you to change it.
        messageInputClient = new MessageInputClient(getContext(), binding, this.viewModel);
        binding.rvMedia.setLayoutManager(new GridLayoutManager(getContext(), 4, RecyclerView.VERTICAL, false));
        binding.rvMedia.hasFixedSize();
        binding.rvComposer.setLayoutManager(new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false));
        int spanCount = 4;  // 4 columns
        int spacing = 2;    // 1 px
        boolean includeEdge = false;
        binding.rvMedia.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
        binding.tvClose.setOnClickListener(v -> {
            messageInputClient.onClickCloseBackGroundView();
            if (viewModel.isEditing()){
                initSendMessage();
                clearFocus();
            }

        });
        binding.llMedia.setOnClickListener(v -> messageInputClient.onClickOpenSelectMediaView(v, null));

        binding.llCamera.setOnClickListener(v -> {
            Utils.setButtonDelayEnable(v);
            messageInputClient.onClickCloseBackGroundView();

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
        binding.llFile.setOnClickListener(v -> messageInputClient.onClickOpenSelectFileView(v, null));
    }

    // endregion
    public void progressCapturedMedia(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.CAPTURE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) return;
            try {
                Uri uri = data.getData();
                if (uri == null) {
                    if (imageUri != null)
                        messageInputClient.progressCapturedMedia(getContext(), imageUri, true);
                    imageUri = null;
                } else {
                    messageInputClient.progressCapturedMedia(getContext(), uri, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_send) {
            this.onSendMessage(binding.etMessage.getText().toString(), viewModel.isEditing());
        } else if (id == R.id.iv_openAttach) {
            binding.setIsAttachFile(true);
            PermissionChecker.permissionCheck((Activity) v.getContext(), null);
            messageInputClient.onClickOpenBackGroundView(MessageInputType.ADD_FILE);
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
        messageInputClient.checkCommand(messageText);
        binding.setActiveMessageSend(!(messageText.length() == 0));
    }
    boolean lockScrollUp = false;
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        viewModel.setInputType(hasFocus ? InputType.SELECT : InputType.DEFAULT);
        if (hasFocus){
            lockScrollUp = true;
            new Handler().postDelayed(()->lockScrollUp = false, 500);
            Utils.showSoftKeyboard((Activity) getContext());
        }else
            Utils.hideSoftKeyboard((Activity) getContext());
    }

    private void onSendMessage(String input, boolean isEdit) {
        binding.ivSend.setEnabled(false);

        if (isEdit) {
            getEditMessage().setText(input);
            getEditMessage().setAttachments(messageInputClient.getSelectedAttachments());
            viewModel.getChannel().updateMessage(getEditMessage(), new MessageCallback() {
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
            Message m = new Message();
            m.setStatus(null);
            m.setText(input);
            m.setAttachments(messageInputClient.getSelectedAttachments());
            if (sendMessageListener != null) {
                sendMessageListener.onSendMessage(m, new MessageCallback() {
                    @Override
                    public void onSuccess(MessageResponse response) {
                        binding.ivSend.setEnabled(true);
                        initSendMessage();
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        initSendMessage();
                        binding.ivSend.setEnabled(true);
                        Utils.showMessage(getContext(), errMsg);
                    }
                });
            }
        }
    }

    private void initSendMessage() {
        messageInputClient.initSendMessage();
        viewModel.setEditMessage(null);
        binding.etMessage.setText("");
    }

    // region Set Listeners
    public void setOnSendMessageListener(SendMessageListener l) {
        this.sendMessageListener = l;
    }

    public void setOpenCameraViewListener(OpenCameraViewListener openCameraViewListener) {
        this.openCameraViewListener = openCameraViewListener;
    }


    // region Listeners

    /**
     * Used for listening to the sendMessage event
     */
    public interface SendMessageListener {
        void onSendMessage(Message message, MessageCallback callback);
    }

    /**
     * This interface is called when you add an attachment
     */
    public interface AttachmentListener {
        void onAddAttachments();
    }

    public interface OpenCameraViewListener {
        void openCameraView(Intent intent, int REQUEST_CODE);
    }

    // endregion
}