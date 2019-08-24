package com.getstream.sdk.chat.view;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
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
import com.getstream.sdk.chat.function.SendFileFunction;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
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
    // our connection to the channel scope
    private ChannelViewModel channelViewModel;
    // binding for this view
    private StreamViewMessageInputBinding binding;
    private MessageInputStyle style;
    // listeners
    private SendMessageListener sendMessageListener;
    private TypingListener typingListener;
    private OpenCameraViewListener openCameraViewListener;


    private static final int DEFAULT_DELAY_TYPING_STATUS = 1500;
    private Runnable typingTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTyping) {
                isTyping = false;
                stopTyping();
            }
        }
    };
    private AttachmentListener attachmentListener;
    // state
    private Message editingMessage;
    private boolean isTyping;

    // TODO Rename, it's not a function
    private SendFileFunction sendFileFunction;

    // region Constructor
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

    // region Init
    private StreamViewMessageInputBinding initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return StreamViewMessageInputBinding.inflate(inflater, this, true);
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        style = new MessageInputStyle(context, attrs);
    }

    public void setViewModel(ChannelViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.channelViewModel = viewModel;
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
        binding.ivSend.setImageDrawable(style.getInputButtonIcon());
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

        setOnSendMessageListener(channelViewModel);
        initAttachmentUI();
        KeyboardVisibilityEvent.setEventListener(
                (Activity) getContext(), (boolean isOpen) -> {
                    if (!isOpen) binding.etMessage.clearFocus();
                });
    }


    private void observeUIs(LifecycleOwner lifecycleOwner) {
        channelViewModel.getInputType().observe(lifecycleOwner, inputType -> {
            switch (inputType) {
                case DEFAULT:
                    binding.llComposer.setBackground(style.getInputBackground());
                    binding.ivOpenAttach.setImageDrawable(style.getAttachmentButtonIcon(false));
                    break;
                case SELECT:
                    binding.llComposer.setBackground(style.getInputSelectedBackground());
                    binding.ivOpenAttach.setImageDrawable(style.getAttachmentButtonIcon(true));
                    break;
                case EDIT:
                    binding.llComposer.setBackground(style.getInputEditBackground());
                    binding.ivOpenAttach.setImageDrawable(style.getAttachmentButtonIcon(true));
                    break;
            }
        });
    }

    private void initAttachmentUI() {
        // TODO: make the attachment UI into it's own view and allow you to change it.
        sendFileFunction = new SendFileFunction(getContext(), binding, this.channelViewModel);
        binding.rvMedia.setLayoutManager(new GridLayoutManager(getContext(), 4, RecyclerView.VERTICAL, false));
        binding.rvMedia.hasFixedSize();
        binding.rvComposer.setLayoutManager(new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false));
        int spanCount = 4;  // 4 columns
        int spacing = 2;    // 1 px
        boolean includeEdge = false;
        binding.rvMedia.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));

        binding.ivOpenAttach.setOnClickListener(v -> sendFileFunction.onClickAttachmentViewOpen(v));
        binding.ivBackAttachment.setOnClickListener(v -> sendFileFunction.onClickAttachmentViewClose(v));
        binding.tvCloseAttach.setOnClickListener(v -> sendFileFunction.onClickAttachmentViewClose(v));
        binding.llMedia.setOnClickListener(v -> sendFileFunction.onClickSelectMediaViewOpen(v, null));
        binding.llCamera.setOnClickListener(v -> {
            Utils.setButtonDelayEnable(v);
            sendFileFunction.onClickAttachmentViewClose(v);

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
        binding.llFile.setOnClickListener(v -> sendFileFunction.onClickSelectFileViewOpen(v, null));
        binding.tvMediaClose.setOnClickListener(v -> sendFileFunction.onClickSelectMediaViewClose(v));
    }

    Uri imageUri;
    // endregion
    public void progressCapturedMedia(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.CAPTURE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) return;
            try {
                Uri uri = data.getData();
                if (uri == null) {
                    if (imageUri != null)
                        sendFileFunction.progressCapturedMedia(getContext(), imageUri, true);
                    imageUri = null;
                } else {
                    sendFileFunction.progressCapturedMedia(getContext(), uri, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public boolean isEditing() {
        return editingMessage != null;
    }

    public void editMessage(Message message) {
        editingMessage = message;
    }

    public Message getEditMessage() {
        return editingMessage;
    }

    public void cancelEditMessage() {
        editingMessage = null;
        binding.etMessage.setText("");
        this.clearFocus();
        sendFileFunction.fadeAnimationView(binding.ivBackAttachment, false);
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
        Log.i(TAG, "click click");
        if (id == R.id.iv_send) {
            this.onSendMessage(binding.etMessage.getText().toString());
            this.stopTyping();
        } else if (id == R.id.iv_openAttach) {
            // open the attachment drawer
            Log.i(TAG, "opening the attachment drawer");
            binding.setIsAttachFile(true);
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
            this.keyStroke();
        } else {
            removeCallbacks(typingTimerRunnable);
            postDelayed(typingTimerRunnable, DEFAULT_DELAY_TYPING_STATUS);
        }
        // detect commands
        sendFileFunction.checkCommand(messageText);
        binding.setActiveMessageSend(!(messageText.length() == 0));
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            this.stopTyping();
        }
        channelViewModel.setInputType(hasFocus ? InputType.SELECT : InputType.DEFAULT);
    }

    private void onSendMessage(String input) {
        binding.ivSend.setEnabled(false);
        Message m = new Message();
        m.setText(input);
        m.setAttachments(sendFileFunction.getSelectedAttachments());
        if (sendMessageListener != null) {
            sendMessageListener.onSendMessage(m, new MessageCallback() {
                @Override
                public void onSuccess(MessageResponse response) {
                    binding.ivSend.setEnabled(true);
                    progressSendMessage(response.getMessage(), null);
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    binding.ivSend.setEnabled(true);
                }
            });
        }

    }

    public void progressSendMessage(Message message, String resendMessageId) {
        initSendMessage();
        if (resendMessageId != null) {
            Global.removeEphemeralMessage(channelViewModel.getChannel().getId(), resendMessageId);
            initSendMessage();
        } else {
//            if (Message.isCommandMessage(message) ||
//                    message.getType().equals(ModelType.message_error)) {
//                channelMessages.remove(ephemeralMessage);
//                message.setDelivered(true);
//            } else {
//                ephemeralMessage.setId(message.getId());
//            }
//
//            handleAction(message);
        }
    }

    private void initSendMessage() {
        binding.etMessage.setText("");
        sendFileFunction.initSendMessage();
    }

    // region Set Listeners
    public void setOnSendMessageListener(SendMessageListener l) {
        this.sendMessageListener = l;
    }

    public void setTypingListener(TypingListener l) {
        this.typingListener = l;
    }

    public void setOpenCameraViewListener(OpenCameraViewListener openCameraViewListener) {
        this.openCameraViewListener = openCameraViewListener;
    }

    // endregion
    private void stopTyping() {
        isTyping = false;
        channelViewModel.getChannel().stopTyping();
        if (typingListener != null) {
            typingListener.onStopTyping();
        }
    }

    private void keyStroke() {
        channelViewModel.getChannel().keystroke();
        isTyping = true;
        if (typingListener != null) {
            typingListener.onKeystroke();
        }
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


    /**
     * A simple interface for typing events
     */
    public interface TypingListener {
        void onKeystroke();

        void onStopTyping();
    }

    public interface OpenCameraViewListener {
        void openCameraView(Intent intent, int REQUEST_CODE);
    }

    // endregion
}