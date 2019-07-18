package com.getstream.sdk.chat.view.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageListItemViewHolder;
import com.getstream.sdk.chat.databinding.ActivityChatBinding;
import com.getstream.sdk.chat.databinding.ViewThreadBinding;
import com.getstream.sdk.chat.function.AttachmentFunction;
import com.getstream.sdk.chat.function.EventFunction;
import com.getstream.sdk.chat.function.MessageFunction;
import com.getstream.sdk.chat.function.ReactionFunction;
import com.getstream.sdk.chat.function.SendFileFunction;
import com.getstream.sdk.chat.interfaces.EventHandler;
import com.getstream.sdk.chat.interfaces.MessageSendListener;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.channel.Channel;
import com.getstream.sdk.chat.model.channel.Event;
import com.getstream.sdk.chat.model.message.Message;
import com.getstream.sdk.chat.model.message.MessageTagModel;
import com.getstream.sdk.chat.rest.Parser;
import com.getstream.sdk.chat.rest.apimodel.request.ChannelDetailRequest;
import com.getstream.sdk.chat.rest.apimodel.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.apimodel.request.PaginationRequest;
import com.getstream.sdk.chat.rest.apimodel.request.SendActionRequest;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.rest.apimodel.response.EventResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.apimodel.response.MessageResponse;
import com.getstream.sdk.chat.rest.controller.RestController;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration;
import com.getstream.sdk.chat.model.message.SelectAttachmentModel;
import com.getstream.sdk.chat.viewmodel.ChatActivityViewModel;
import com.getstream.sdk.chat.viewmodel.ChatActivityViewModelFactory;
import com.google.gson.Gson;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatActivity extends AppCompatActivity implements EventHandler, WSResponseHandler {

    private final String TAG = ChatActivity.class.getSimpleName();

    ChatActivityViewModel mViewModel;
    ActivityChatBinding binding;
    ViewThreadBinding threadBinding;

    private ChannelResponse channelResponse;
    private Channel channel;
    private List<Message> channelMessages, threadMessages;

    private RecyclerView.LayoutManager mLayoutManager, mLayoutManager_thread;
    private MessageListItemAdapter mAdapter, mThreadAdapter;
    private int scrollPosition = 0;
    private static int fVPosition, lVPosition;
    private boolean noHistory, noHistoryThread;
    // Functions
    private MessageFunction messageFunction;
    // private EventFunction eventFunction;
    private SendFileFunction sendFileFunction;

    private boolean singleConversation;

    // region LifeCycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        singleConversation = (Global.streamChat.getChannel() != null);
        if (!singleConversation) {
            init();
            configDelivered();
            configUIs();
        } else {
            Global.webSocketService.setWSResponseHandler(this);
            if (!TextUtils.isEmpty(Global.streamChat.getClientID())) {
                getChannel();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Global.eventFunction != null) {
            Global.eventFunction.setChannel(this.channel);
            Global.eventFunction.setEventHandler(this);
        }
        startTypingStopRepeatingTask();
        startTypingClearRepeatingTask();
    }

    @Override
    public void onStop() {
        super.onStop();
        Global.streamChat.setChannel(null);

        if (Global.eventFunction != null) {
            Global.eventFunction.setEventHandler(null);
            Global.eventFunction.setChannel(null);
        }

        stopTypingStopRepeatingTask();
        stopTypingClearRepeatingTask();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.CAPTURE_IMAGE_REQUEST_CODE) {
                try {
                    Object object = data.getExtras().get("data");
                    if (object.getClass().equals(Bitmap.class)) {
                        Bitmap bitmap = (Bitmap) object;
                        Uri uri = Utils.getUriFromBitmap(this, bitmap);
                        sendFileFunction.progressCapturedMedia(this, uri, true);
                    } else {
                        Log.d(TAG, "No Captured Image");
                    }
                } catch (Exception e) {
                    Uri uri = data.getData();
                    if (uri == null) return;
                    sendFileFunction.progressCapturedMedia(this, uri, false);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.clAddFile.getVisibility() == View.VISIBLE) {
            onClickAttachmentViewClose(null);
            return;
        }
        if (binding.clSelectPhoto.getVisibility() == View.VISIBLE) {
            onClickSelectMediaViewClose(null);
            return;
        }
        super.onBackPressed();
    }
    // endregion

    // region Init
    void init() {
        try {
            Fresco.initialize(getApplicationContext());
        } catch (Exception e) {
        }

        // set viewModel
        String channelId = null;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            channelId = (String) b.get(Constant.TAG_CHANNEL_RESPONSE_ID);
        }
        Log.d(TAG, "Channel ID: " + channelId);
        if (channelId != null) {
            channelResponse = Global.getChannelResponseById(channelId);
        }

        ChatActivityViewModelFactory factory = new ChatActivityViewModelFactory(channelResponse);
        mViewModel = ViewModelProviders.of(this, factory).get(ChatActivityViewModel.class);
        binding.setViewModel(mViewModel);
        threadBinding = binding.clThread;
        threadBinding.setViewModel(mViewModel);
        channel = channelResponse.getChannel();
        channelMessages = channelResponse.getMessages();
        messageFunction = new MessageFunction(this.channelResponse);
        sendFileFunction = new SendFileFunction(this, binding, channelResponse);
        checkReadMark();
        noHistory = channelMessages.size() < Constant.CHANNEL_MESSAGE_LIMIT;
        noHistoryThread = false;

        if (Global.eventFunction == null)
            Global.eventFunction = new EventFunction();
        Global.eventFunction.setChannel(this.channel);
        Global.eventFunction.setEventHandler(this);
        // Permission Check
        PermissionChecker.permissionCheck(this, null);
    }

    boolean lockRVScrollListener = false;

    void configUIs() {
        // Message Composer
        binding.setActiveMessageComposer(false);
        binding.setActiveMessageSend(false);
        binding.setShowLoadMoreProgressbar(false);
        binding.setNoConnection(Global.noConnection);
        binding.etMessage.setOnFocusChangeListener((View view, boolean hasFocus) -> {
            binding.setActiveMessageComposer(hasFocus);
            lockRVScrollListener = hasFocus;
        });
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String text = binding.etMessage.getText().toString();
                binding.setActiveMessageSend(!(text.length() == 0));
                sendFileFunction.checkCommand(text);
                if (text.length() > 0) {
                    keystroke();
//                    sendTypeInidcator();
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        // Message RecyclerView
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManager_thread = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mLayoutManager.scrollToPosition(channelMessages.size());
        binding.rvMessage.setLayoutManager(mLayoutManager);

        setScrollDownHideKeyboard(binding.rvMessage);
        setRecyclerViewAdapder();

        KeyboardVisibilityEvent.setEventListener(
                this, (boolean isOpen) -> {
                    if (!isOpen) {
                        binding.etMessage.clearFocus();
                    } else {
                        lockRVScrollListener = true;
                        new Handler().postDelayed(() -> {
                            lockRVScrollListener = false;
                        }, 500);
                    }
                    if (lVPosition > messages().size() - 2)
                        recyclerView().scrollToPosition(lVPosition);

                });

        // File Attachment
        binding.rvMedia.setLayoutManager(new GridLayoutManager(this, 4, LinearLayoutManager.VERTICAL, false));
        binding.rvMedia.hasFixedSize();
        int spanCount = 4;  // 4 columns
        int spacing = 2;    // 1 px
        boolean includeEdge = false;
        binding.rvMedia.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));

        configHeaderView();
    }

    private void configHeaderView() {
        // Avatar
        if (!TextUtils.isEmpty(channel.getName())) {
            binding.tvChannelInitial.setText(channel.getInitials());
            Utils.circleImageLoad(binding.ivHeaderAvatar, channel.getImageURL());
            if (StringUtility.isValidImageUrl(channel.getImageURL())) {
                binding.ivHeaderAvatar.setVisibility(View.VISIBLE);
                binding.tvChannelInitial.setVisibility(View.INVISIBLE);
            } else {
                binding.ivHeaderAvatar.setVisibility(View.INVISIBLE);
                binding.tvChannelInitial.setVisibility(View.VISIBLE);
            }
        } else {
            User opponent = Global.getOpponentUser(channelResponse);
            binding.tvChannelInitial.setText(opponent.getUserInitials());
            if (opponent != null) {
                Utils.circleImageLoad(binding.ivHeaderAvatar, opponent.getImage());
                binding.tvChannelInitial.setVisibility(View.VISIBLE);
                binding.ivHeaderAvatar.setVisibility(View.VISIBLE);
            } else {
                binding.tvChannelInitial.setVisibility(View.VISIBLE);
                binding.ivHeaderAvatar.setVisibility(View.INVISIBLE);
            }
        }
        // Channel name
        String channelName = "";

        if (!TextUtils.isEmpty(channelResponse.getChannel().getName())) {
            channelName = channelResponse.getChannel().getName();
        } else {
            User opponent = Global.getOpponentUser(channelResponse);
            if (opponent != null) {
                channelName = opponent.getName();
            }
        }

        binding.tvChannelName.setText(channelName);

        // Last Active
        String lastActive = null;
        User opponent = Global.getOpponentUser(channelResponse);
        if (opponent != null) {
            if (!TextUtils.isEmpty(Global.differentTime(opponent.getLast_active()))) {
                lastActive = Global.differentTime(opponent.getLast_active());
            }
        }
        if (TextUtils.isEmpty(lastActive)) {
            binding.tvActive.setVisibility(View.GONE);
        } else {
            binding.tvActive.setVisibility(View.VISIBLE);
            binding.tvActive.setText(lastActive);
        }
        // Online Mark

        try {
            if (Global.getOpponentUser(channelResponse) == null)
                binding.ivActiveMark.setVisibility(View.GONE);
            else {
                if (Global.getOpponentUser(channelResponse).getOnline()) {
                    binding.ivActiveMark.setVisibility(View.VISIBLE);
                } else {
                    binding.ivActiveMark.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            binding.ivActiveMark.setVisibility(View.GONE);
        }
    }

    private void getChannel() {
        Channel channel_ = Global.streamChat.getChannel();
        binding.setShowMainProgressbar(true);
        channel_.setType(ModelType.channel_messaging);
        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", Constant.DEFAULT_LIMIT);

        // Additional Field
        Map<String, Object> data = new HashMap<>();
        if (channel_.getAdditionalFields() != null) {
            Set<String> keys = channel_.getAdditionalFields().keySet();
            for (String key : keys) {
                Object value = channel_.getAdditionalFields().get(key);
                if (value != null)
                    data.put(key, value);
            }
        }
        Log.d(TAG, "Channel Connecting...");

        ChannelDetailRequest request = new ChannelDetailRequest(messages, data, true, true);

        Global.mRestController.channelDetailWithID(channel_.getId(), request, (ChannelResponse response) -> {
            binding.setShowMainProgressbar(false);
            if (!response.getMessages().isEmpty())
                Global.setStartDay(response.getMessages(), null);
            Global.addChannelResponse(response);
            channelResponse = response;
            Gson gson = new Gson();
            Log.d(TAG, "Channel Response: " + gson.toJson(response));

            init();
            configDelivered();
            configUIs();
        }, (String errMsg, int errCode) -> {
            binding.setShowMainProgressbar(false);
            Log.d(TAG, "Failed Connect Channel : " + errMsg);
        });
    }

    private List<Message> messages() {
        return isThreadMode() ? threadMessages : channelMessages;
    }

    private RecyclerView recyclerView() {
        return isThreadMode() ? threadBinding.rvThread : binding.rvMessage;
    }

    private boolean isNoHistory() {
        return isThreadMode() ? noHistoryThread : noHistory;
    }

    void configDelivered() {
        if (messages() == null || messages().isEmpty()) return;
        if (!messages().get(messages().size() - 1).isIncoming())
            messages().get(messages().size() - 1).setDelivered(true);
    }

    void setRecyclerViewAdapder() {
        mAdapter = new MessageListItemAdapter(this, this.channelResponse, messages(), isThreadMode(), (View v) -> {
            Object object = v.getTag();
            Log.d(TAG, "onClick Attach : " + object);
            messageItemClickListener(object);
        }, (View v) -> {
            try {
                messageItemLongClickListener(v.getTag());
            } catch (Exception e) {
            }
            return true;
        });
        recyclerView().setAdapter(mAdapter);
        mViewModel.getChannelMessages().observe(this, (@Nullable List<Message> users) -> {
            if (scrollPosition == -1) return;

            mAdapter.notifyDataSetChanged();
            if (scrollPosition > 0) {
                recyclerView().scrollToPosition(scrollPosition);
                scrollPosition = 0;
                return;
            }
            recyclerView().scrollToPosition(messages().size());
        });
        mViewModel.setChannelMessages(messages());
    }

    private void setScrollDownHideKeyboard(RecyclerView recyclerView) {
        fVPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        Log.d(TAG, "fVPosition: " + fVPosition);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (lockRVScrollListener) return;
                int currentFirstVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (currentFirstVisible < fVPosition) {
                    Utils.hideSoftKeyboard(ChatActivity.this);
                    binding.etMessage.clearFocus();
                    if (currentFirstVisible == 0 && !isNoHistory()) loadMore();
                }
                new Handler().postDelayed(() -> {
                    lVPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                }, 500);
                fVPosition = currentFirstVisible;
            }
        });
    }


    public void onClickBackFinish(View v) {
        finish();
    }
    // endregion

    // region Message
    private Message ephemeralMessage = null;

    public void onClickSendMessage(View v) {
        if (binding.etMessage.getTag() == null) {
            if (Global.noConnection) {
                sendOfflineMessage();
                return;
            }
            if (!isThreadMode()) {
                ephemeralMessage = createOfflineMessage(false);
                handleAction(ephemeralMessage);
            }
            messageFunction.sendMessage(binding.etMessage.getText().toString(), thread_parentMessage, sendFileFunction.getSelectedAttachments(), new MessageSendListener() {
                @Override
                public void onSuccess(MessageResponse response) {
                    if (!isThreadMode()) {
                        if (Global.isCommandMessage(response.getMessage()) || response.getMessage().getType().equals(ModelType.message_error)) {
                            channelMessages.remove(ephemeralMessage);
                            response.getMessage().setDelivered(true);
                        } else {
                            ephemeralMessage.setId(response.getMessage().getId());
                        }
                    }
                    handleAction(response.getMessage());
                    Log.d(TAG, "Delivered Message");
                }

                @Override
                public void onFailed(String errMsg, int errCode) {
                    Log.d(TAG, "Failed Send message: " + errMsg);
                }
            });
            initSendMessage();
        } else
            messageFunction.updateMessage(binding.etMessage.getText().toString(), (Message) binding.etMessage.getTag(), sendFileFunction.getSelectedAttachments(), new MessageSendListener() {
                @Override
                public void onSuccess(MessageResponse response) {
                    initSendMessage();
                    binding.etMessage.setTag(null);
                }

                @Override
                public void onFailed(String errMsg, int errCode) {

                }
            });
    }

    private void initSendMessage() {
        binding.etMessage.setText("");
        sendFileFunction.initSendMessage();
    }

    private void sendOfflineMessage() {
        Message message = createOfflineMessage(true);
        handleAction(message);
        initSendMessage();
    }

    private Message createOfflineMessage(boolean isOffle) {
        Message message = new Message();
        message.setId(Global.convertDateToString(new Date()));
        message.setText(binding.etMessage.getText().toString());
        message.setType(isOffle ? ModelType.message_error : ModelType.message_ephemeral);
        message.setCreated_at(Global.convertDateToString(new Date()));
        Global.setStartDay(Arrays.asList(message), getLastMessage());
        message.setUser(Global.streamChat.getUser());
        return message;
    }

    private Message getLastMessage() {
        return messages().isEmpty() ? null : messages().get(messages().size() - 1);
    }
    // endregion

    // region Typing Indicator
    boolean isTyping = false;
    Date lastKeyStroke;
    Date lastTypingEvent;
    private Handler stopTyingEventHandler = new Handler();
    Runnable runnableTypingStop = new Runnable() {
        @Override
        public void run() {
            try {
                clean();
            } finally {
                stopTyingEventHandler.postDelayed(runnableTypingStop, 3000);
            }
        }
    };

    void startTypingStopRepeatingTask() {
        runnableTypingStop.run();
    }

    void stopTypingStopRepeatingTask() {
        stopTyping();
        stopTyingEventHandler.removeCallbacks(runnableTypingStop);
    }

    /**
     * clean - Cleans the channel state and fires stop typing if needed
     */
    void clean() {
        if (this.lastKeyStroke != null) {
            Date now = new Date();
            long diff = now.getTime() - this.lastKeyStroke.getTime();
            if (diff > 1000 && this.isTyping) {
                this.stopTyping();
            }
        }
    }

    /**
     * keystroke - First of the typing.start and typing.stop events based on the users keystrokes.
     * Call this on every keystroke
     */
    void keystroke() {
        Date now = new Date();
        long diff;
        if (this.lastKeyStroke == null)
            diff = 2001;
        else
            diff = now.getTime() - this.lastKeyStroke.getTime();


        this.lastKeyStroke = now;
        this.isTyping = true;
        // send a typing.start every 2 seconds
        if (diff > 2000) {
            this.lastTypingEvent = new Date();
            Global.eventFunction.sendEvent(Event.typing_start);
            Log.d(TAG, "typing.start");
        }
    }

    /**
     * stopTyping - Sets last typing to null and sends the typing.stop event
     */
    void stopTyping() {
        this.lastTypingEvent = null;
        this.isTyping = false;
        Global.eventFunction.sendEvent(Event.typing_stop);
        Log.d(TAG, "typing.stop");
    }


    void sendTypeInidcator() {
        if (isThreadMode()) return;
        if (!isTyping) {
            Log.d(TAG, "typing.start");
            Global.eventFunction.sendEvent(Event.typing_start);
            isTyping = true;
        }
    }

    private void stopTypingEvent() {
        new Handler().postDelayed(() -> {
            if (isTyping) {
                Global.eventFunction.sendEvent(Event.typing_stop);
                isTyping = false;
                Log.d(TAG, "typing.stop");
            }
        }, 2000);
    }

    // refresh Current Typing users in this channel
    private Handler clearTyingUserHandler = new Handler();
    Runnable runnableTypingClear = new Runnable() {
        @Override
        public void run() {
            try {
                Global.typingUsers = new ArrayList<>();
                try {
                    mAdapter.notifyItemChanged(channelMessages.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
//                eventFunction.sendEvent(Event.typing_stop);
                clearTyingUserHandler.postDelayed(runnableTypingClear, Constant.TYPYING_CLEAN_INTERVAL);
            }
        }
    };

    void startTypingClearRepeatingTask() {
        runnableTypingClear.run();
    }

    void stopTypingClearRepeatingTask() {
        clearTyingUserHandler.removeCallbacks(runnableTypingClear);
    }
    // endregion

    // region Action
    private void handleAction(Message message) {
        switch (message.getType()) {
            case ModelType.message_ephemeral:
            case ModelType.message_error:
                Event event = new Event();
                event.setType(Event.message_new);
                event.setMessage(message);
                handleEvent(event);
                break;
            default:
                break;
        }
    }
    // endregion

    // region Thread
    private Message thread_parentMessage = null;

    private void configThread(@NonNull Message message, int position) {
        mViewModel.setReplyCount(message.getReplyCount());
        cleanEditView();

        thread_parentMessage = message;
        threadMessages = new ArrayList<>();
        MessageListItemViewHolder viewHolder = new MessageListItemViewHolder((View) threadBinding.clMessageParent);
        viewHolder.bind(this, this.channelResponse, channelMessages, position, true, null, null);

        if (message.getReplyCount() == 0) {
            setThreadAdapter();
            threadBinding.setShowThread(true);
        } else {
            binding.setShowMainProgressbar(true);
            Global.mRestController.getReplies(message.getId(), String.valueOf(Constant.THREAD_MESSAGE_LIMIT), null, (GetRepliesResponse response) -> {
                threadMessages = response.getMessages();
                Global.setStartDay(threadMessages, null);
                setThreadAdapter();
                threadBinding.setShowThread(true);
                binding.setShowMainProgressbar(false);
                if (threadMessages.size() < Constant.THREAD_MESSAGE_LIMIT) noHistoryThread = true;
            }, (String errMsg, int errCode) -> {
                Utils.showMessage(ChatActivity.this, errMsg);
                thread_parentMessage = null;
                binding.setShowMainProgressbar(false);
            });
        }
        // Clean RecyclerView
    }

    public void onClickCloseThread(View v) {
        threadBinding.setShowThread(false);
        cleanEditView();
        setScrollDownHideKeyboard(binding.rvMessage);
    }

    private void setThreadAdapter() {
        if (threadMessages.size() > 0)
            mLayoutManager_thread.scrollToPosition(threadMessages.size() - 1);

        threadBinding.rvThread.setLayoutManager(mLayoutManager_thread);
        mThreadAdapter = new MessageListItemAdapter(this, this.channelResponse, threadMessages, isThreadMode(), (View v) -> {
            Object object = v.getTag();
            messageItemClickListener(object);
        }, (View v) -> {
            try {
                messageItemLongClickListener(v.getTag());
            } catch (Exception e) {
            }
            return true;
        });
        threadBinding.rvThread.setAdapter(mThreadAdapter);
        setScrollDownHideKeyboard(threadBinding.rvThread);
    }

    private boolean isThreadMode() {
        if (thread_parentMessage == null) return false;
        return true;
    }

    private void cleanEditView() {
        binding.etMessage.setTag(null);
        binding.etMessage.setText("");
        thread_parentMessage = null;
        threadMessages = null;
        lVPosition = 0;
        fVPosition = 0;
        noHistoryThread = false;
        binding.rvMessage.clearOnScrollListeners();
        threadBinding.rvThread.clearOnScrollListeners();
        Utils.hideSoftKeyboard(this);
    }
    // endregion

    // region Listener

    // region Message Item Touch Listener
    private void messageItemClickListener(Object object) {
        if (object.getClass().equals(SelectAttachmentModel.class)) {
            new AttachmentFunction().progressAttachment((SelectAttachmentModel) object, this);
            return;
        }

        if (object.getClass().equals(MessageTagModel.class)) {
            MessageTagModel tag = (MessageTagModel) object;
            Message message = messages().get(tag.position);
            switch (tag.type) {
                case Constant.TAG_MOREACTION_REPLY:
                    configThread(channelMessages.get(tag.position), tag.position);
                    Log.d(TAG, "Click Reply : " + tag.position);
                    break;
                case Constant.TAG_ACTION_SEND:
                case Constant.TAG_ACTION_SHUFFLE:
                    Map<String, String> map = new HashMap<>();
                    if (tag.type.equals(Constant.TAG_ACTION_SEND))
                        map.put("image_action", ModelType.action_send);
                    else
                        map.put("image_action", ModelType.action_shuffle);

                    SendActionRequest request = new SendActionRequest(channel.getId(), message.getId(), ModelType.channel_messaging, map);
                    RestController.SendMessageCallback callback = (MessageResponse response) -> {
                        handleAction(message);
                        response.getMessage().setDelivered(true);
                        handleAction(response.getMessage());
                    };
                    Global.mRestController.sendAction(message.getId(), request, callback, (String errMsg, int errCode) -> {
                        Log.d(TAG, errMsg);
                    });
                    Log.d(TAG, "Click ACTION_SEND : " + tag.position);
                    break;
                case Constant.TAG_ACTION_CANCEL:
                    handleAction(messages().get(tag.position));
                    break;
                case Constant.TAG_MESSAGE_REACTION:
                    int firstListItemPosition = ((LinearLayoutManager) recyclerView().getLayoutManager()).findFirstVisibleItemPosition();
                    final int lastListItemPosition = firstListItemPosition + recyclerView().getChildCount() - 1;
                    int childIndex;
                    if (tag.position < firstListItemPosition || tag.position > lastListItemPosition) {
                        childIndex = tag.position;
                    } else {
                        childIndex = tag.position - firstListItemPosition;
                    }
                    int originY = recyclerView().getChildAt(childIndex).getBottom();
                    ReactionFunction.showReactionDialog(this, message, originY);
                    Log.d(TAG, "Origin Y-" + originY);
                    break;
                case Constant.TAG_MESSAGE_RESEND:
                    if (Global.noConnection) {
                        Utils.showMessage(this, "No internet connection!");
                        break;
                    }
                    handleAction(message);
                    messageFunction.sendMessage(message.getText(), isThreadMode() ? thread_parentMessage : null, null, new MessageSendListener() {
                        @Override
                        public void onSuccess(MessageResponse response) {
                            initSendMessage();
                        }

                        @Override
                        public void onFailed(String errMsg, int errCode) {
                            initSendMessage();
                        }
                    });
                    break;
                case Constant.TAG_MESSAGE_INVALID_COMMAND:
                    handleAction(message);
                    binding.etMessage.setText("/");
                    break;
                case Constant.TAG_MESSAGE_CHECK_DELIVERED:
                    showAlertReadUsers(message);
                    break;
                default:
                    break;
            }
        }
    }

    private void messageItemLongClickListener(Object object) {
        final int position = Integer.parseInt(object.toString());
        final Message message = messages().get(position);
        ReactionFunction.showMoreActionDialog(this, message, (View v) -> {
            String type = (String) v.getTag();
            switch (type) {
                case Constant.TAG_MOREACTION_EDIT:
                    binding.etMessage.setTag(message);
                    binding.etMessage.setText(message.getText());
                    binding.etMessage.requestFocus();
                    binding.etMessage.setSelection(binding.etMessage.getText().length());
                    break;
                case Constant.TAG_MOREACTION_DELETE:
                    messageFunction.deleteMessage(binding.etMessage, message);
                    break;
                case Constant.TAG_MOREACTION_REPLY:
                    if (!isThreadMode())
                        configThread(message, position);
                    break;
                default:
                    break;
            }
        });
    }

    private void showAlertReadUsers(Message message) {
        List<User> readUsers = Global.getReadUsers(channelResponse, message);
        if (readUsers == null) return;
        String msg = "";
        if (readUsers.size() > 0) {
            if (readUsers.size() == 1) msg = readUsers.get(0).getName();
            else {
                for (int i = 0; i < readUsers.size(); i++) {
                    User user = readUsers.get(i);
                    if (i == readUsers.size() - 2) msg += user.getName() + " and ";
                    else if (i == readUsers.size() - 1) msg += user.getName();
                    else msg += user.getName() + ", ";
                }
            }
            Log.d(TAG, "Deliever Indicator 2");
        } else {
            if (message.isDelivered()) {
                msg = "Delivered";
                Log.d(TAG, "Deliever Indicator 3");
            } else {
                msg = "sending...";
                Log.d(TAG, "Deliever Indicator 4");
            }
        }
        Utils.showMessage(this, msg);
    }
    // endregion

    // WebSocket Listener
    @Override
    public void handleWSResponse(Object response) {
        Global.noConnection = false;

        if (!response.getClass().equals(String.class)) return;

        // Checking No connection
        JSONObject json = null;
        try {
            json = new JSONObject(response.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json == null) return;

        Event event = Parser.parseEvent(json);
        if (!event.getType().equals(Event.health_check))
            Log.d(TAG, "Connection Response : " + json);

        if (Global.eventFunction != null)
            Global.eventFunction.handleReceiveEvent(event);

        if (!TextUtils.isEmpty(event.getConnection_id()) && TextUtils.isEmpty(Global.streamChat.getClientID())) {
            Global.eventFunction.handleReconnect(Global.noConnection);
        }
    }

    @Override
    public void onFailed(String errMsg, int errCode) {
        Global.noConnection = true;
        Global.streamChat.setClientID(null);
        binding.setNoConnection(true);
        if (Global.eventFunction != null)
            Global.eventFunction.handleReconnect(Global.noConnection);
        binding.setShowMainProgressbar(false);
    }

    // Event Listener
    @Override
    public void handleEvent(final Event event) {
        this.runOnUiThread(() -> {
            switch (event.getType()) {
                case Event.health_check:
                    break;
                case Event.message_new:
                case Event.message_updated:
                case Event.message_deleted:
                    messageEvent(event);
                    break;
                case Event.message_read:
                    messageReadEvent(event);
                    break;
                case Event.typing_start:
                case Event.typing_stop:
                    footerEvent(event);
                    break;
                case Event.user_updated:
                    break;
                case Event.user_presence_changed:
                    break;
                case Event.user_watching_start:
                    break;
                case Event.user_watching_stop:
                    break;
                case Event.reaction_new:
                case Event.reaction_deleted:
                    reactionEvent(event);
                    break;
                case Event.channel_updated:
                case Event.channel_deleted:
                    Global.eventFunction.handleChannelEvent(channelResponse, event);
                    if (event.getType().equals(Event.channel_deleted)) {
                        Utils.showMessage(this, "Channel Owner just removed this channel!");
                        finish();
                    }
                    break;
                default:
                    break;
            }
        });
        Gson gson = new Gson();
        String eventStr = gson.toJson(event);
        Log.d(TAG, "New Event: " + eventStr);
    }

    @Override
    public void handleReconnection(boolean disconnect) {
        binding.setNoConnection(disconnect);
        if (!disconnect) {
            reconnectionHandler();
        }
    }

    private void messageEvent(Event event) {
        Message message = event.getMessage();
        if (message == null) return;

        switch (event.getType()) {
            case Event.message_new:
                Global.setStartDay(Arrays.asList(message), getLastMessage());
                switch (message.getType()) {
                    case ModelType.message_regular:
                        if (!message.isIncoming()) message.setDelivered(true);
                        try {
                            channelMessages.remove(ephemeralMessage);
                        } catch (Exception e) {
                        }
                        Global.eventFunction.newMessage(channelResponse, message);
                        scrollPosition = 0;
                        mViewModel.setChannelMessages(channelMessages);
                        messageReadMark();
                        break;
                    case ModelType.message_ephemeral:
                    case ModelType.message_error:
                        boolean isContain = false;
                        Global.setEphemeralMessage(channel.getId(), message);
                        for (int i = messages().size() - 1; i >= 0; i--) {
                            Message message1 = messages().get(i);
                            if (message1.getId().equals(message.getId())) {
                                messages().remove(message1);
                                isContain = true;
                                break;
                            }
                        }
                        if (!isContain) messages().add(message);
                        scrollPosition = 0;
                        if (isThreadMode()) {
                            mThreadAdapter.notifyDataSetChanged();
                            threadBinding.rvThread.scrollToPosition(threadMessages.size() - 1);
                        } else {
                            mViewModel.setChannelMessages(messages());
                        }
                        break;
                    case ModelType.message_reply:
                        if (isThreadMode() && message.getParent_id().equals(thread_parentMessage.getId())) {
                            threadMessages.add(message);
                            mThreadAdapter.notifyDataSetChanged();
                            threadBinding.rvThread.scrollToPosition(threadMessages.size() - 1);
                        }
                        break;
                    case ModelType.message_system:
                        break;
                    default:
                        break;
                }

                break;
            case Event.message_updated:
                if (isThreadMode() && message.getId().equals(thread_parentMessage.getId())) {
                    mViewModel.setReplyCount(message.getReplyCount());
                }
            case Event.message_deleted:
                int changedIndex_ = 0;
                if (message.getType().equals(ModelType.message_reply)) {
                    if (!isThreadMode()) return;
                    for (int i = 0; i < threadMessages.size(); i++) {
                        if (message.getId().equals(threadMessages.get(i).getId())) {
                            if (event.getType().equals(Event.message_deleted))
                                message.setText(Constant.MESSAGE_DELETED);
                            changedIndex_ = i;
                            threadMessages.set(i, message);
                            break;
                        }
                    }
                    final int changedIndex = changedIndex_;
                    mThreadAdapter.notifyItemChanged(changedIndex);
                } else {
                    for (int i = 0; i < channelMessages.size(); i++) {
                        if (message.getId().equals(channelMessages.get(i).getId())) {
                            if (event.getType().equals(Event.message_deleted))
                                message.setText(Constant.MESSAGE_DELETED);
                            changedIndex_ = i;
                            channelMessages.set(i, message);
                            break;
                        }
                    }
                    final int changedIndex = changedIndex_;
                    scrollPosition = -1;
                    mViewModel.setChannelMessages(channelMessages);
                    mAdapter.notifyItemChanged(changedIndex);
                }
                break;
            default:
                break;
        }
    }

    private void reactionEvent(Event event) {
        Message message = event.getMessage();
        if (message == null) return;
        int changedIndex_ = 0;
        if (message.getType().equals(ModelType.message_regular)) {
            for (int i = 0; i < channelMessages.size(); i++) {
                if (message.getId().equals(channelMessages.get(i).getId())) {
                    changedIndex_ = i;
                    channelMessages.set(i, message);
                    break;
                }
            }
            final int changedIndex = changedIndex_;
            this.runOnUiThread(() -> {
                scrollPosition = -1;
                mViewModel.setChannelMessages(channelMessages);
                mAdapter.notifyItemChanged(changedIndex);
            });
        } else if (message.getType().equals(ModelType.message_reply)) {
            if (thread_parentMessage == null) return;
            if (!message.getParent_id().equals(thread_parentMessage.getId())) return;

            for (int i = 0; i < threadMessages.size(); i++) {
                if (message.getId().equals(threadMessages.get(i).getId())) {
                    changedIndex_ = i;
                    threadMessages.set(i, message);
                    break;
                }
            }
            final int changedIndex = changedIndex_;
            this.runOnUiThread(() -> {
                mThreadAdapter.notifyItemChanged(changedIndex);
            });
        }
    }

    // region Footer Event

    private void footerEvent(Event event) {
        User user = event.getUser();
        if (user == null) return;
        if (user.getId().equals(Global.streamChat.getUser().getId())) return;

        switch (event.getType()) {
            case Event.typing_start:
                boolean isAdded = false; // If user already exits in typingUsers
                for (int i = 0; i < Global.typingUsers.size(); i++) {
                    User user1 = Global.typingUsers.get(i);
                    if (user1.getId().equals(user.getId())) {
                        isAdded = true;
                        break;
                    }
                }
                if (!isAdded)
                    Global.typingUsers.add(user);

                break;
            case Event.typing_stop:
                int index1 = -1; // If user already exits in typingUsers
                for (int i = 0; i < Global.typingUsers.size(); i++) {
                    User user1 = Global.typingUsers.get(i);
                    if (user1.getId().equals(user.getId())) {
                        index1 = i;
                        break;
                    }
                }
                if (index1 != -1)
                    Global.typingUsers.remove(index1);
                break;
            default:
                break;
        }
        mAdapter.notifyItemChanged(channelMessages.size());
    }

    void messageReadEvent(Event event) {
        Global.eventFunction.readMessage(channelResponse, event);
        if (!channelResponse.getLastMessage().isIncoming()) {
            mAdapter.notifyItemChanged(channelMessages.size() - 1);
        }
    }
    // endregion

    // endregion

    // region Pagination

    boolean isCalling;

    private void loadMore() {
        if (isNoHistory() || isCalling) return;
        Log.d(TAG, "Next pagination...");
        isCalling = true;
        if (!isThreadMode()) {
            binding.setShowLoadMoreProgressbar(true);
            PaginationRequest request = new PaginationRequest(Constant.DEFAULT_LIMIT, channelMessages.get(0).getId(), this.channel);
            Global.mRestController.pagination(channel.getId(), request, (ChannelResponse response) -> {
                binding.setShowLoadMoreProgressbar(false);
                List<Message> newMessages = new ArrayList<>(response.getMessages());
                Log.d(TAG, "new Message Count: " + newMessages.size());
                if (newMessages.size() < Constant.DEFAULT_LIMIT) noHistory = true;

                // Set Date Time
                Global.setStartDay(newMessages, null);
                // Add new to current Message List
                for (int i = newMessages.size() - 1; i > -1; i--) {
                    channelMessages.add(0, newMessages.get(i));
                }
                scrollPosition = ((LinearLayoutManager) binding.rvMessage.getLayoutManager()).findLastCompletelyVisibleItemPosition() + response.getMessages().size();
                mViewModel.setChannelMessages(channelMessages);
                isCalling = false;
            }, (String errMsg, int errCode) -> {
                Utils.showMessage(ChatActivity.this, errMsg);
                isCalling = false;
                binding.setShowLoadMoreProgressbar(false);
            });
        } else {
            binding.setShowMainProgressbar(true);
            Global.mRestController.getReplies(thread_parentMessage.getId(), String.valueOf(Constant.THREAD_MESSAGE_LIMIT), threadMessages.get(0).getId(), (GetRepliesResponse response) -> {
                binding.setShowMainProgressbar(false);
                List<Message> newMessages = new ArrayList<>(response.getMessages());
                Log.d(TAG, "new Message Count: " + newMessages.size());
                if (newMessages.size() < Constant.THREAD_MESSAGE_LIMIT) noHistoryThread = true;

                Global.setStartDay(newMessages, null);
                // Add new to current Message List
                for (int i = newMessages.size() - 1; i > -1; i--) {
                    threadMessages.add(0, newMessages.get(i));
                }
                int scrollPosition = ((LinearLayoutManager) recyclerView().getLayoutManager()).findLastCompletelyVisibleItemPosition() + response.getMessages().size();
                mThreadAdapter.notifyDataSetChanged();
                recyclerView().scrollToPosition(scrollPosition);
                isCalling = false;
            }, (String errMsg, int errCode) -> {
                Utils.showMessage(ChatActivity.this, errMsg);
                isCalling = false;
                binding.setShowMainProgressbar(false);
            });
        }

    }

    // endregion

    // region Check Message Read

    private void checkReadMark() {
        if (channelResponse.getLastMessage() == null) return;
        if (!Global.readMessage(channelResponse.getReadDateOfChannelLastMessage(true), channelResponse.getLastMessage().getCreated_at())) {
            messageReadMark();
        }
    }

    private void messageReadMark() {
        MarkReadRequest request = new MarkReadRequest(channelMessages.get(channelMessages.size() - 1).getId());
        RestController.EventCallback callback = (EventResponse response) -> {

        };
        Global.mRestController.readMark(channel.getId(), request, callback, (String errMsg, int errCode) -> {
            Utils.showMessage(ChatActivity.this, errMsg);
        });
    }

    // endregion

    // region Attachment

    public void onClickAttachmentViewOpen(View v) {
        sendFileFunction.onClickAttachmentViewOpen(v);
    }

    public void onClickAttachmentViewClose(View v) {
        sendFileFunction.onClickAttachmentViewClose(v);
    }

    public void onClickSelectMediaViewOpen(View v) {
        sendFileFunction.onClickSelectMediaViewOpen(v);
    }

    public void onClickSelectMediaViewClose(View v) {
        sendFileFunction.onClickSelectMediaViewClose(v);
    }

    public void onClickSelectFileViewOpen(View v) {
        sendFileFunction.onClickSelectFileViewOpen(v);
    }

    public void onClickTakePicture(View v) {
        sendFileFunction.onClickAttachmentViewClose(v);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Intent chooserIntent = Intent.createChooser(takePictureIntent, "Capture Image or Video");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeVideoIntent});
        startActivityForResult(chooserIntent, Constant.CAPTURE_IMAGE_REQUEST_CODE);
    }

    // endregion

    // region Reconnection
    private void reconnectionHandler() {
        if (singleConversation) {
            Channel channel_ = Global.streamChat.getChannel();
            binding.setShowMainProgressbar(true);
            channel_.setType(ModelType.channel_messaging);
            Map<String, Object> messages = new HashMap<>();
            messages.put("limit", Constant.DEFAULT_LIMIT);
            Map<String, Object> data = new HashMap<>();
            Log.d(TAG, "Channel Connecting...");

            ChannelDetailRequest request = new ChannelDetailRequest(messages, data, true, true);

            Global.mRestController.channelDetailWithID(channel_.getId(), request, (ChannelResponse response) -> {
                binding.setShowMainProgressbar(false);
                if (!response.getMessages().isEmpty())
                    Global.setStartDay(response.getMessages(), null);
                Global.addChannelResponse(response);
                channelResponse = response;
                Gson gson = new Gson();
                Log.d(TAG, "Channel Response: " + gson.toJson(response));

                init();
                List<Message> ephemeralMessages = Global.getEphemeralMessages(channel.getId());
                if (ephemeralMessages != null && !ephemeralMessages.isEmpty())
                    for (int i = 0; i < ephemeralMessages.size(); i++) {
                        channelMessages.add(ephemeralMessages.get(i));
                    }

                setRecyclerViewAdapder();
            }, (String errMsg, int errCode) -> {
                binding.setShowMainProgressbar(false);
                Log.d(TAG, "Failed Connect Channel : " + errMsg);
            });
            return;
        }
        for (ChannelResponse response : Global.channels) {
            if (response.getChannel().getId().equals(channel.getId())) {
                channelResponse = response;
                init();
                List<Message> ephemeralMessages = Global.getEphemeralMessages(channel.getId());
                if (ephemeralMessages != null && !ephemeralMessages.isEmpty())
                    for (int i = 0; i < ephemeralMessages.size(); i++) {
                        channelMessages.add(ephemeralMessages.get(i));
                    }

                setRecyclerViewAdapder();
                Log.d(TAG, "Refresh reconnection!");
                break;
            }
        }
    }
    // end region

    // region Permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constant.PERMISSIONS_REQUEST) {
            boolean granted = true;
            for (int grantResult : grantResults)
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            if (!granted) PermissionChecker.showRationalDialog(this, null);
        }
    }
    // endregion
}
