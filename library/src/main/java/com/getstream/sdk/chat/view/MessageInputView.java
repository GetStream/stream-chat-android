package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.ViewMessageInputBinding;
import com.getstream.sdk.chat.function.SendFileFunction;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.ArrayList;
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
    /*
    TODO:
    - MessageInputView needs to be aware of the channel (for the list of commands, perhaps other settings);
    - SendFileFunction (attachments and commands needs extensive changes);
    - Data bindings need to be renamed, super confusing atm
    - Make more things configurable
    - Documentation
     */

    final String TAG = MessageInputView.class.getSimpleName();

    // our connection to the channel scope
    private ChannelViewModel channelViewModel;

    // binding for this view
    private ViewMessageInputBinding binding;
    private MessageInputStyle style;

    // listeners
    private SendMessageListener sendMessageListener;
    private TypingListener typingListener;
    private AttachmentListener attachmentListener;
    private OnFocusChangeListener onFocusChangeListener;

    // state
    private Message editingMessage;
    private boolean isTyping;

    // TODO Rename, it's not a function
    private SendFileFunction sendFileFunction;

    // region Constructor
    public MessageInputView(Context context) {
        super(context);
        binding = initBinding(context);
        initAttachmentUI(context);
    }

    public MessageInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttr(context, attrs);
        binding = initBinding(context);
        applyStyle();
        initAttachmentUI(context);
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        style = new MessageInputStyle(context, attrs);
    }

    public void setViewModel(ChannelViewModel model, LifecycleOwner lifecycleOwner) {
        this.channelViewModel = model;
        binding.setLifecycleOwner(lifecycleOwner);
        this.setOnSendMessageListener(model);
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

    private ViewMessageInputBinding initBinding(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ViewMessageInputBinding.inflate(inflater, this, true);

        binding.setActiveMessageComposer(false);
        binding.setActiveMessageSend(false);

        binding.ivSend.setOnClickListener(this);
        binding.ivOpenAttach.setOnClickListener(this);
        binding.etMessage.setOnFocusChangeListener(this);
        binding.etMessage.addTextChangedListener(this);
        return binding;
    }

    public List<Attachment> GetAttachments() {
        List<Attachment> a = new ArrayList<>();
        return a;
    }


    private void initAttachmentUI(Context context) {
        // TODO: make the attachment UI into it's own view and allow you to change it.
        sendFileFunction = new SendFileFunction(context, binding);
        binding.rvMedia.setLayoutManager(new GridLayoutManager(context, 4, LinearLayoutManager.VERTICAL, false));
        binding.rvMedia.hasFixedSize();
        binding.rvComposer.setLayoutManager(new GridLayoutManager(context, 1, LinearLayoutManager.HORIZONTAL, false));
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
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            Intent chooserIntent = Intent.createChooser(takePictureIntent, "Capture Image or Video");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeVideoIntent});
            // TODO: somehow fix this
            // startActivityForResult(chooserIntent, Constant.CAPTURE_IMAGE_REQUEST_CODE);

        });
        binding.llFile.setOnClickListener(v -> sendFileFunction.onClickSelectFileViewOpen(v, null));
        binding.tvMediaClose.setOnClickListener(v -> sendFileFunction.onClickSelectMediaViewClose(v));
    }

    public boolean IsEditing() {
        return editingMessage != null;
    }

    public void EditMessage(Message message) {
        editingMessage = message;
    }

    public Message GetEditMessage() {
        return editingMessage;
    }

    public void CancelEditMessage() {
        editingMessage = null;
        binding.etMessage.setText("");
        this.clearFocus();
        sendFileFunction.fadeAnimationView(binding.ivBackAttachment, false);
    }

    public void setEnabled(boolean enabled) {
        binding.etMessage.setEnabled(true);
    }

    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        this.onFocusChangeListener = l;
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
        if (id == R.id.tv_send) {
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
        if (s.length() > 0) {
            this.keyStroke();
        } else {
            this.stopTyping();
        }
        // detect commands
        sendFileFunction.checkCommand(messageText);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            this.stopTyping();
        }

        //binding.setActiveMessageComposer(hasFocus);
    }


    private void onSendMessage(String input) {
        Message m = new Message();
        m.setText(input);
        if (sendMessageListener != null) {
            sendMessageListener.onSendMessage(m);
        }
    }

    public void setOnSendMessageListener(SendMessageListener l) {
        this.sendMessageListener = l;
    }

    public void setTypingListener(TypingListener l) {
        this.typingListener = l;
    }

    private void stopTyping() {
        isTyping = false;
        if (typingListener != null) {
            typingListener.onStopTyping();
        }
    }

    private void keyStroke() {
        isTyping = true;
        if (typingListener != null) {
            typingListener.onKeystroke();
        }
    }

    public boolean IsTyping() {
        return isTyping;
    }

    /**
     * Used for listening to the sendMessage event
     */
    public interface SendMessageListener {
        void onSendMessage(Message message);
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
}