package com.getstream.sdk.chat.view.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.databinding.ChannelFragmentBinding;
import com.getstream.sdk.chat.databinding.ViewThreadBinding;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.enums.Pagination;
import com.getstream.sdk.chat.function.AttachmentFunction;
import com.getstream.sdk.chat.function.ReactionFunction;
import com.getstream.sdk.chat.function.SendFileFunction;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.model.MessageTagModel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.SelectAttachmentModel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ChatChannelEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelFragmentViewModelFactory;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.getstream.sdk.chat.utils.Utils.TAG;

public class ChannelFragment extends Fragment {

    private Client client;
    public String channelType;
    public String channelID;
    public HashMap<String, Object> channelExtraData;

    private Channel channel;

    // ViewModel & Binding
    private ChannelViewModel mViewModel;
    private ChannelFragmentBinding binding;
    private ViewThreadBinding threadBinding;
    // Arguments for Channel
    String channelIdFromChannelList = null;
    private ChannelState channelState;
    private List<Message> channelMessages, threadMessages;
    // Adapter & LayoutManager
    private MessageListItemAdapter mChannelMessageAdapter, mThreadAdapter;
    private RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    private RecyclerView.LayoutManager mLayoutManager_thread = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    private RecyclerView.LayoutManager mLayoutManager_thread_header = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    // Functions
    private SendFileFunction sendFileFunction;
    private ReactionFunction reactionFunction;
    // Customization MessageItemView
    private int messageItemLayoutId;
    private String messageItemViewHolderName;
    // Misc
    public boolean singleConversation;
    private boolean isShowLastMessage;
    private int scrollPosition = 0;
    private static int fVPosition, lVPosition;
    private boolean noHistory, noHistoryThread;

    private int channelSubscriptionId;

    public void unsubscribeFromChannelEvents(){
        channel.removeEventHandler(channelSubscriptionId);
        channelSubscriptionId = 0;
    }

    public void subscribeToChannelEvents(){
        if (channelSubscriptionId != 0) {
            return;
        }

        channelSubscriptionId = channel.addEventHandler(new ChatChannelEventHandler(){
            @Override
            public void onTypingStart(Event event) {
                footerEvent(event);
            }
            @Override
            public void onTypingStop(Event event) {
                footerEvent(event);
            }
            @Override
            public void onMessageNew(Event event) {
                messageEvent(event);
            }
            @Override
            public void onMessageUpdated(Event event) {
                messageEvent(event);
            }
            @Override
            public void onMessageDeleted(Event event) {
                messageEvent(event);
            }
            @Override
            public void onMessageRead(Event event) {
                if (!channelState.getLastMessage().isIncoming()) {
                    mChannelMessageAdapter.notifyItemChanged(channelMessages.size() - 1);
                }
            }
            @Override
            public void onReactionNew(Event event) {
                reactionEvent(event);
            }
            @Override
            public void onReactionDeleted(Event event) {
               reactionEvent(event);
            }
            @Override
            public void onChannelDeleted(Event event) {
                getActivity().runOnUiThread(() -> {
                    Utils.showMessage(getContext(), "Channel Owner just removed this channel!");
                    finish();
                });
            }
        });
    }

    // region LifeCycle
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        client = StreamChat.getInstance();
        channel = client.channel(channelType, channelID, channelExtraData);

        // set binding
        binding = ChannelFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ChannelFragmentViewModelFactory factory = new ChannelFragmentViewModelFactory(channelState);
        mViewModel = ViewModelProviders.of(this, factory).get(ChannelViewModel.class);

        // set WS handler
        subscribeToChannelEvents();

        // Permission Check
        PermissionChecker.permissionCheck(getActivity(), null);
        try {
            Fresco.initialize(getContext().getApplicationContext());
        } catch (Exception e) {
        }

        threadBinding = binding.clThread;
        onBackPressed();

        if (singleConversation) {
            binding.setShowMainProgressbar(true);
            client.waitForConnection(new ClientConnectionCallback() {
                @Override
                public void onSuccess() {
                    getChannel();
                }
                @Override
                public void onError(String errMsg, int errCode) {
                    binding.setShowMainProgressbar(false);
                }
            });
        } else {
            initReconnection();
            setDeliverLastMessage();
            configUIs();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        client = StreamChat.getInstance();
        channel = client.channel(channelType, channelID);

        subscribeToChannelEvents();

        startTypingStopRepeatingTask();
        startTypingClearRepeatingTask();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BC_RECONNECT_CHANNEL);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getContext().registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unsubscribeFromChannelEvents();

        stopTypingStopRepeatingTask();
        stopTypingClearRepeatingTask();

        try {
            getContext().unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                Log.w(TAG, "Tried to unregister the reciver when it's not registered");
            } else {
                throw e;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.CAPTURE_IMAGE_REQUEST_CODE) {
            try {
                Object object = data.getExtras().get("data");
                if (object.getClass().equals(Bitmap.class)) {
                    Bitmap bitmap = (Bitmap) object;
                    Uri uri = Utils.getUriFromBitmap(getContext(), bitmap);
                    sendFileFunction.progressCapturedMedia(getContext(), uri, true);
                }
            } catch (Exception e) {
                Uri uri = data.getData();
                if (uri == null) return;
                sendFileFunction.progressCapturedMedia(getContext(), uri, false);
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void onBackPressed() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                // Close if File Attach View is opened.
                if (binding.clAddFile.getVisibility() == View.VISIBLE) {
                    sendFileFunction.onClickAttachmentViewClose(null);
                    return true;
                }
                // Close if Selecting Photo View is opened.
                if (binding.clSelectPhoto.getVisibility() == View.VISIBLE) {
                    sendFileFunction.onClickSelectMediaViewClose(null);
                    return true;
                }
                // Close if Thread View is opened.
                if (isThreadMode()) {
                    onClickCloseThread(null);
                    return true;
                }
                // Cancel if editing message.
                if (binding.etMessage.getTag() != null) {
                    cancelEditMessage();
                    return true;
                }
                if (!singleConversation) {
                    finish();
                    return true;
                }
                return false;
            }
            return false;
        });
    }

    private void finish() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(this);
        trans.commit();
        manager.popBackStack();
    }
    //endregion

    private void initReconnection() {
        binding.setViewModel(mViewModel);
        threadBinding.setViewModel(mViewModel);
        channelMessages = channelState.getMessages();
        channel.setChannelState(this.channelState);

        sendFileFunction = new SendFileFunction(channel, getActivity(), binding, channelState);
        reactionFunction = new ReactionFunction(channel);

        checkReadMark();
        noHistory = channelMessages.size() < Constant.CHANNEL_MESSAGE_LIMIT;
        noHistoryThread = false;
    }

    private boolean lockRVScrollListener = false;

    private void getChannel() {
        binding.setShowMainProgressbar(true);
        Log.d(TAG, "Channel Connecting...");

        channel.query(new QueryChannelCallback() {
            @Override
            public void onSuccess(ChannelState response) {
                binding.setShowMainProgressbar(false);
                channelState = response;
                initReconnection();
                setDeliverLastMessage();
                configUIs();
            }

            @Override
            public void onError(String errMsg, int errCode) {
                binding.setShowMainProgressbar(false);
                Log.d(TAG, "Failed Connect Channel : " + errMsg);
            }
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

    // endregion

    // region Config UIs
    private void configUIs() {
        configActionBar(); // Hides Action Bar
        configHeaderView(); // Header View
        configCustomMessageItemView(); // custom MessageItemView
        configMessageInputView(); // Message Input box
        configMessageRecyclerView(); // Message RecyclerView
        // Bottom View
//        binding.tvNewMessage.setVisibility(View.GONE);
//        binding.tvNewMessage.setOnClickListener((View v) -> {
//            scrollPosition = 0;
//            recyclerView().scrollToPosition(messages().size());
//            binding.tvNewMessage.setVisibility(View.GONE);
//        });
        // File Attachment
        configAttachmentUIs();
    }

    private void configActionBar() {
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configCustomMessageItemView() {
        messageItemLayoutId = client.getComponent().message.getMessageItemLayoutId();
        messageItemViewHolderName = client.getComponent().message.getMessageItemViewHolderName();
    }

    private void configHeaderView() {
        // Avatar
        if (!TextUtils.isEmpty(channel.getName())) {
            binding.tvChannelInitial.setText(channel.getInitials());
            Utils.circleImageLoad(binding.ivHeaderAvatar, channel.getImage());
            if (StringUtility.isValidImageUrl(channel.getImage())) {
                binding.ivHeaderAvatar.setVisibility(View.VISIBLE);
                binding.tvChannelInitial.setVisibility(View.INVISIBLE);
            } else {
                binding.ivHeaderAvatar.setVisibility(View.INVISIBLE);
                binding.tvChannelInitial.setVisibility(View.VISIBLE);
            }
        } else {
            User opponent = Global.getOpponentUser(channelState);
            if (opponent != null) {
                binding.tvChannelInitial.setText(opponent.getUserInitials());
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

        if (!TextUtils.isEmpty(channelState.getChannel().getName())) {
            channelName = channelState.getChannel().getName();
        } else {
            User opponent = Global.getOpponentUser(channelState);
            if (opponent != null) {
                channelName = opponent.getName();
            }
        }

        binding.tvChannelName.setText(channelName);

        // Last Active
        Message lastMessage = channelState.getLastMessageFromOtherUser();
        configHeaderLastActive(lastMessage);
        // Online Mark
        try {
            if (Global.getOpponentUser(channelState) == null)
                binding.ivActiveMark.setVisibility(View.GONE);
            else {
                if (Global.getOpponentUser(channelState).getOnline()) {
                    binding.ivActiveMark.setVisibility(View.VISIBLE);
                } else {
                    binding.ivActiveMark.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            binding.ivActiveMark.setVisibility(View.GONE);
        }

        binding.tvBack.setVisibility(singleConversation ? View.INVISIBLE : View.VISIBLE);
        binding.tvBack.setOnClickListener((View v) -> finish());
    }

    private void configMessageInputView() {
        binding.setActiveMessageComposer(false);
        binding.setActiveMessageSend(false);
        binding.setShowLoadMoreProgressbar(false);
        binding.setNoConnection(!client.isConnected());
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
                    channel.keystroke();
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
        KeyboardVisibilityEvent.setEventListener(
                getActivity(), (boolean isOpen) -> {
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
        binding.tvSend.setOnClickListener(this::sendMessage);
    }

    private void configMessageRecyclerView() {
        mLayoutManager.scrollToPosition(channelMessages.size());
        binding.rvMessage.setLayoutManager(mLayoutManager);
        setScrollDownHideKeyboard(binding.rvMessage);
        setChannelMessageRecyclerViewAdapder();
    }

    private void configHeaderLastActive(@Nullable Message message) {
        if (message == null || message.getUser().isMe()) {
            return;
        }
        binding.tvActive.setVisibility(View.GONE);
        String lastActive = null;
        if (message != null) {
            if (!TextUtils.isEmpty(Message.differentTime(message.getCreatedAt()))) {
                lastActive = Message.differentTime(message.getCreatedAt());
            }
        }

        if (TextUtils.isEmpty(lastActive)) {
            binding.tvActive.setVisibility(View.GONE);
        } else {
            binding.tvActive.setVisibility(View.VISIBLE);
            binding.tvActive.setText(lastActive);
        }
    }

    private void configAttachmentUIs() {
        binding.rvMedia.setLayoutManager(new GridLayoutManager(getContext(), 4, LinearLayoutManager.VERTICAL, false));
        binding.rvMedia.hasFixedSize();
        binding.rvComposer.setLayoutManager(new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false));
        int spanCount = 4;  // 4 columns
        int spacing = 2;    // 1 px
        boolean includeEdge = false;
        binding.rvMedia.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));

        binding.tvOpenAttach.setOnClickListener(v -> sendFileFunction.onClickAttachmentViewOpen(v));
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
            startActivityForResult(chooserIntent, Constant.CAPTURE_IMAGE_REQUEST_CODE);
        });
        binding.llFile.setOnClickListener(v -> sendFileFunction.onClickSelectFileViewOpen(v, null));
        binding.tvMediaClose.setOnClickListener(v -> sendFileFunction.onClickSelectMediaViewClose(v));
        threadBinding.tvClose.setOnClickListener(this::onClickCloseThread);
    }

    private void setChannelMessageRecyclerViewAdapder() {
        mChannelMessageAdapter = new MessageListItemAdapter(getContext(), channelState, channelMessages,
                false, messageItemViewHolderName, messageItemLayoutId,
                (View v) -> {
                    Object object = v.getTag();
                    messageItemClickListener(object);
                }, (View v) -> {
            try {
                messageItemLongClickListener(v.getTag());
            } catch (Exception e) {
            }
            return true;
        });
        binding.rvMessage.setAdapter(mChannelMessageAdapter);
        mViewModel.getChannelMessages().observe(this, (@Nullable List<Message> users) -> {
            if (scrollPosition == -1) return;

            mChannelMessageAdapter.notifyDataSetChanged();
            if (scrollPosition > 0) {
                binding.rvMessage.scrollToPosition(scrollPosition);
                scrollPosition = 0;
                return;
            }
            binding.rvMessage.scrollToPosition(channelMessages.size());
        });
        mViewModel.setChannelMessages(channelMessages);
    }

    private void setScrollDownHideKeyboard(RecyclerView recyclerView) {
        fVPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
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
                int currentLastVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                if (!isThreadMode()) {
                    if (currentLastVisible >= messages().size() - 1) isShowLastMessage = true;
                    else isShowLastMessage = false;
                }

                if (currentFirstVisible < fVPosition) {
                    Utils.hideSoftKeyboard(getActivity());
                    binding.etMessage.clearFocus();
                    if (currentFirstVisible == 0 && !isNoHistory()) loadMore();
//                    if (currentLastVisible >= messages().size() - 1)
//                        binding.tvNewMessage.setVisibility(View.GONE);
                }

                new Handler().postDelayed(() -> {
                    lVPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                }, 500);
                fVPosition = currentFirstVisible;
            }
        });
    }

    // endregion

    // region Send Message
    private Message ephemeralMessage = null;

    /**
     * Send Message - Send a message to this channel
     */
    public void sendMessage(View view) {
        if (binding.etMessage.getTag() == null) {
            sendNewMessage(binding.etMessage.getText().toString(), sendFileFunction.getSelectedAttachments(), null);
        } else
            updateMessage();
    }

    public void sendNewMessage(String text, List<Attachment> attachments, String resendMessageId) {
        if (!client.isConnected()) {
            sendOfflineMessage();
            return;
        }
        if (resendMessageId == null) {
            ephemeralMessage = createEphemeralMessage(false);
            handleAction(ephemeralMessage);
        }
        binding.tvSend.setEnabled(false);
        channel.sendMessage(text,
                attachments,
                isThreadMode() ? thread_parentMessage.getId() : null,
                new MessageCallback() {
                    @Override
                    public void onSuccess(MessageResponse response) {
                        binding.tvSend.setEnabled(true);
                        progressSendMessage(response.getMessage(), resendMessageId);
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        binding.tvSend.setEnabled(true);
                        Utils.showMessage(getContext(), errMsg);
                    }
                });
        initSendMessage();
    }

    public void updateMessage() {
        if (!client.isConnected()) {
            Utils.showMessage(getContext(), "No internet connection!");
            return;
        }
        binding.tvSend.setEnabled(false);
        channel.updateMessage(binding.etMessage.getText().toString(),
                (Message) binding.etMessage.getTag(),
                sendFileFunction.getSelectedAttachments(),
                new MessageCallback() {
                    @Override
                    public void onSuccess(MessageResponse response) {
                        initSendMessage();
                        response.getMessage().setDelivered(true);
                        binding.etMessage.setTag(null);
                        binding.tvSend.setEnabled(true);
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        binding.tvSend.setEnabled(true);
                        Utils.showMessage(getContext(), errMsg);
                    }
                });
    }

    public void resendMessage(Message message) {
        if (!client.isConnected()) {
            Utils.showMessage(getContext(), Constant.NO_INTERNET);
            return;
        }
        handleAction(message);
        sendNewMessage(message.getText(), null, message.getId());
    }

    public void sendGiphy(String type, Message message) {
        Map<String, String> map = new HashMap<>();
        if (type.equals(Constant.TAG_ACTION_SEND))
            map.put("image_action", ModelType.action_send);
        else
            map.put("image_action", ModelType.action_shuffle);

        SendActionRequest request = new SendActionRequest(channel.getId(), message.getId(), ModelType.channel_messaging, map);
        client.sendAction(message.getId(), request, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                handleAction(message);
                response.getMessage().setDelivered(true);
                handleAction(response.getMessage());
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.d(TAG, errMsg);
            }
        });
    }

    public void progressSendMessage(Message message, String resendMessageId) {
        if (resendMessageId != null) {
            Global.removeEphemeralMessage(channel.getId(), resendMessageId);
            initSendMessage();
        } else {
            if (Message.isCommandMessage(message) ||
                    message.getType().equals(ModelType.message_error)) {
                channelMessages.remove(ephemeralMessage);
                message.setDelivered(true);
            } else {
                ephemeralMessage.setId(message.getId());
            }

            handleAction(message);
        }
    }

    private void setDeliverLastMessage() {
        if (messages() == null || messages().isEmpty()) return;
        if (!messages().get(messages().size() - 1).isIncoming())
            messages().get(messages().size() - 1).setDelivered(true);
    }

    private void initSendMessage() {
        binding.etMessage.setText("");
        sendFileFunction.initSendMessage();
    }

    private void sendOfflineMessage() {
        handleAction(createEphemeralMessage(true));
        initSendMessage();
    }

    private Message createEphemeralMessage(boolean isOffle) {
        Message message = new Message();
        message.setId(Message.convertDateToString(new Date()));
        message.setText(binding.etMessage.getText().toString());
        message.setType(isOffle ? ModelType.message_error : ModelType.message_ephemeral);
        message.setCreated_at(Message.convertDateToString(new Date()));
        Message.setStartDay(Arrays.asList(message), getLastMessage());
        message.setUser(client.getUser());
        if (isThreadMode())
            message.setParent_id(thread_parentMessage.getId());
        if (isOffle)
            Global.setEphemeralMessage(channel.getId(), message);
        return message;
    }

    private Message getLastMessage() {
        return messages().isEmpty() ? null : messages().get(messages().size() - 1);
    }

    // Edit
    private void editMessage(Message message) {
        binding.etMessage.setTag(message);
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
                    sendFileFunction.onClickSelectMediaViewOpen(null, message.getAttachments());
                } else {
                    sendFileFunction.onClickSelectFileViewOpen(null, message.getAttachments());
                }
            } else {
                sendFileFunction.onClickSelectMediaViewOpen(null, message.getAttachments());
            }
        }
    }

    private void cancelEditMessage() {
        initSendMessage();
        binding.etMessage.clearFocus();
        binding.etMessage.setTag(null);
        sendFileFunction.fadeAnimationView(binding.ivBackAttachment, false);
    }

    // endregion

    // region Message Item Touch Action
    private void messageItemClickListener(Object object) {
        if (isCallingThread) return;
        if (object.getClass().equals(SelectAttachmentModel.class)) {
            new AttachmentFunction().progressAttachment((SelectAttachmentModel) object, getActivity());
            return;
        }

        if (object.getClass().equals(MessageTagModel.class)) {
            MessageTagModel tag = (MessageTagModel) object;
            Message message = messages().get(tag.position);
            switch (tag.type) {
                case Constant.TAG_MOREACTION_REPLY:
                    configThread(channelMessages.get(tag.position));
                    break;
                case Constant.TAG_ACTION_SEND:
                case Constant.TAG_ACTION_SHUFFLE:
                    sendGiphy(tag.type, message);
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
                    reactionFunction.showReactionDialog(getContext(), message, originY);
                    break;
                case Constant.TAG_MESSAGE_RESEND:
                    resendMessage(message);
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

        reactionFunction.showMoreActionDialog(getContext(), message, (View v) -> {
            String type = (String) v.getTag();
            switch (type) {
                case Constant.TAG_MOREACTION_EDIT:
                    editMessage(message);
                    break;
                case Constant.TAG_MOREACTION_DELETE:
                    channel.deleteMessage(message,
                            new MessageCallback() {
                                @Override
                                public void onSuccess(MessageResponse response) {
                                    Utils.showMessage(getContext(), "Deleted Successfully");
                                }

                                @Override
                                public void onError(String errMsg, int errCode) {
                                    Log.d(TAG, "Failed DeleteMessage : " + errMsg);
                                }
                            });
                    break;
                case Constant.TAG_MOREACTION_REPLY:
                    if (!isThreadMode())
                        configThread(message);
                    break;
                default:
                    break;
            }
        });
    }

    private void showAlertReadUsers(Message message) {
        List<User> readUsers = Global.getReadUsers(channelState, message);
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
        } else {
            if (message.isDelivered()) {
                msg = "Delivered";
            } else {
                msg = "sending...";
            }
        }
        Utils.showMessage(getContext(), msg);
    }


    // endregion

    // region Typing Indicator


    private Handler stopTyingEventHandler = new Handler();
    Runnable runnableTypingStop = new Runnable() {
        @Override
        public void run() {
            try {
                if (channel != null)
                    channel.clean();
            } finally {
                stopTyingEventHandler.postDelayed(runnableTypingStop, 3000);
            }
        }
    };

    void startTypingStopRepeatingTask() {
        runnableTypingStop.run();
    }

    void stopTypingStopRepeatingTask() {
        channel.stopTyping();
        stopTyingEventHandler.removeCallbacks(runnableTypingStop);
    }

    // refresh Current Typing users in this channel
    private Handler clearTyingUserHandler = new Handler();
    private Runnable runnableTypingClear = new Runnable() {
        @Override
        public void run() {
            try {
                Global.typingUsers = new ArrayList<>();
                try {
                    mChannelMessageAdapter.notifyItemChanged(channelMessages.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
//                eventFunction.sendEvent(Event.typing_stop);
                clearTyingUserHandler.postDelayed(runnableTypingClear, Constant.TYPYING_CLEAN_INTERVAL);
            }
        }
    };

    private void startTypingClearRepeatingTask() {
        runnableTypingClear.run();
    }

    private void stopTypingClearRepeatingTask() {
        clearTyingUserHandler.removeCallbacks(runnableTypingClear);
    }
    // endregion

    // region Action
    private void handleAction(Message message) {
        switch (message.getType()) {
            case ModelType.message_ephemeral:
            case ModelType.message_error:
                Event event = new Event();
                event.setType(EventType.MESSAGE_NEW);
                event.setMessage(message);
                messageEvent(event);
                break;
            default:
                break;
        }
    }
    // endregion

    // region Thread
    private Message thread_parentMessage = null;
    private boolean isCallingThread = false;

    private void configThread(@NonNull Message message) {

        mViewModel.setReplyCount(message.getReplyCount());
        cleanEditView();

        thread_parentMessage = message;
        threadMessages = new ArrayList<>();

        threadBinding.rvHeader.setLayoutManager(mLayoutManager_thread_header);
        MessageListItemAdapter mThreadHeaderAdapter = new MessageListItemAdapter(getContext(), this.channelState, Arrays.asList(thread_parentMessage), isThreadMode(), messageItemViewHolderName, messageItemLayoutId, null, null);
        threadBinding.rvHeader.setAdapter(mThreadHeaderAdapter);

        if (message.getReplyCount() == 0) {
            setThreadAdapter();
            threadBinding.setShowThread(true);
        } else {
            binding.setShowMainProgressbar(true);

            if (isCallingThread) return;
            isCallingThread = true;
            client.getReplies(message.getId(), String.valueOf(Constant.THREAD_MESSAGE_LIMIT), null, new GetRepliesCallback() {
                @Override
                public void onSuccess(GetRepliesResponse response) {
                    threadMessages = response.getMessages();

                    List<Message> ephemeralThreadMessages = Global.getEphemeralMessages(channel.getId(), thread_parentMessage.getId());
                    if (ephemeralThreadMessages != null && !ephemeralThreadMessages.isEmpty()) {
                        for (int i = 0; i < ephemeralThreadMessages.size(); i++) {
                            threadMessages.add(ephemeralThreadMessages.get(i));
                        }
                    }

                    Message.setStartDay(threadMessages, null);
                    setThreadAdapter();
                    threadBinding.setShowThread(true);
                    binding.setShowMainProgressbar(false);
                    if (threadMessages.size() < Constant.THREAD_MESSAGE_LIMIT)
                        noHistoryThread = true;
                    isCallingThread = false;
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    Utils.showMessage(getContext(), errMsg);
                    thread_parentMessage = null;
                    binding.setShowMainProgressbar(false);
                    isCallingThread = false;
                }
            });
        }
        // Clean RecyclerView
    }

    /**
     * Hide thread view
     */
    private void onClickCloseThread(View v) {
        threadBinding.setShowThread(false);
        cleanEditView();
        setScrollDownHideKeyboard(binding.rvMessage);
        isCallingThread = false;
    }

    private void setThreadAdapter() {
        if (threadMessages.size() > 0)
            mLayoutManager_thread.scrollToPosition(threadMessages.size() - 1);

        threadBinding.rvThread.setLayoutManager(mLayoutManager_thread);
        mThreadAdapter = new MessageListItemAdapter(getContext(), this.channelState, threadMessages, isThreadMode(), messageItemViewHolderName, messageItemLayoutId, (View v) -> {
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
        Utils.hideSoftKeyboard(getActivity());
    }
    // endregion

    // region Listener

    // Event Listener

    private void messageEvent(Event event) {
        Message message = event.getMessage();
        if (message == null) return;

        switch (event.getType()) {
            case MESSAGE_NEW:
                newMessageEvent(message);
                break;
            case MESSAGE_UPDATED:
                if (isThreadMode() && message.getId().equals(thread_parentMessage.getId()))
                    mViewModel.setReplyCount(message.getReplyCount());
            case MESSAGE_DELETED:
                updateOrDeleteMessageEvent(event, message);
                break;
            default:
                break;
        }
    }

    private void newMessageEvent(Message message) {
        configHeaderLastActive(message);
        Message.setStartDay(Arrays.asList(message), getLastMessage());

        switch (message.getType()) {
            case ModelType.message_regular:
                if (!message.isIncoming())
                    message.setDelivered(true);

                messages().remove(ephemeralMessage);
                if (message.isIncoming() && !isShowLastMessage) {
                    scrollPosition = -1;
//                    binding.tvNewMessage.setVisibility(View.VISIBLE);
                } else {
                    scrollPosition = 0;
                }
                mViewModel.setChannelMessages(channelMessages);
                messageMarkRead();
                break;
            case ModelType.message_ephemeral:
            case ModelType.message_error:
                boolean isContain = false;
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
                if (isThreadMode() && message.getParentId().equals(thread_parentMessage.getId())) {
                    messages().remove(ephemeralMessage);
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
    }

    private void updateOrDeleteMessageEvent(Event event, Message message) {
        if (!message.isIncoming())
            message.setDelivered(true);
        int changedIndex_ = 0;
        if (message.getType().equals(ModelType.message_reply)) {
            if (!isThreadMode()) return;
            for (int i = 0; i < threadMessages.size(); i++) {
                if (message.getId().equals(threadMessages.get(i).getId())) {
                    if (event.getType().equals(EventType.MESSAGE_DELETED))
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
                    if (event.getType().equals(EventType.MESSAGE_DELETED))
                        message.setText(Constant.MESSAGE_DELETED);
                    changedIndex_ = i;
                    channelMessages.set(i, message);
                    break;
                }
            }
            final int changedIndex = changedIndex_;
            scrollPosition = -1;
            mViewModel.setChannelMessages(channelMessages);
            mChannelMessageAdapter.notifyItemChanged(changedIndex);
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
            getActivity().runOnUiThread(() -> {
                scrollPosition = -1;
                mViewModel.setChannelMessages(channelMessages);
                mChannelMessageAdapter.notifyItemChanged(changedIndex);
            });
        } else if (message.getType().equals(ModelType.message_reply)) {
            if (thread_parentMessage == null) return;
            if (!message.getParentId().equals(thread_parentMessage.getId())) return;

            for (int i = 0; i < threadMessages.size(); i++) {
                if (message.getId().equals(threadMessages.get(i).getId())) {
                    changedIndex_ = i;
                    threadMessages.set(i, message);
                    break;
                }
            }
            final int changedIndex = changedIndex_;
            getActivity().runOnUiThread(() -> {
                mThreadAdapter.notifyItemChanged(changedIndex);
            });
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(Constant.BC_RECONNECT_CHANNEL))
                return;

            Log.d(TAG, "Reconnection!");
            channelState = client.getChannelByCid(channel.getCid()).getChannelState();
            Message.setStartDay(channelState.getMessages(), null);
            initReconnection();
            // Check Ephemeral Messages
            getActivity().runOnUiThread(() -> {
                setDeliverLastMessage();
                configUIs();
                if (isThreadMode())
                    configThread(thread_parentMessage);
            });
        }
    };
    // endregion

    // region Footer Event

    private void footerEvent(Event event) {
        User user = event.getUser();
        if (user == null) return;
        if (user.getId().equals(client.getUserId())) return;

        switch (event.getType()) {
            case TYPING_START:
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
            case TYPING_STOP:
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
        mChannelMessageAdapter.notifyItemChanged(channelMessages.size());
    }

    // endregion

    // endregion

    // region Pagination

    private boolean isCalling;

    private void loadMore() {
        if (isNoHistory() || isCalling) return;
        Log.d(TAG, "Next pagination...");
        isCalling = true;
        if (isThreadMode()) {
            binding.setShowMainProgressbar(true);
            client.getReplies(thread_parentMessage.getId(),
                String.valueOf(Constant.THREAD_MESSAGE_LIMIT),
                threadMessages.get(0).getId(), new GetRepliesCallback() {
                    @Override
                    public void onSuccess(GetRepliesResponse response) {
                        binding.setShowMainProgressbar(false);
                        List<Message> newMessages = new ArrayList<>(response.getMessages());
                        if (newMessages.size() < Constant.THREAD_MESSAGE_LIMIT)
                            noHistoryThread = true;

                        Message.setStartDay(newMessages, null);
                        // Add new to current Message List
                        for (int i = newMessages.size() - 1; i > -1; i--) {
                            threadMessages.add(0, newMessages.get(i));
                        }
                        int scrollPosition = ((LinearLayoutManager) recyclerView().getLayoutManager()).findLastCompletelyVisibleItemPosition() + response.getMessages().size();
                        mThreadAdapter.notifyDataSetChanged();
                        recyclerView().scrollToPosition(scrollPosition);
                        isCalling = false;
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        Utils.showMessage(getContext(), errMsg);
                        isCalling = false;
                        binding.setShowMainProgressbar(false);
                    }
                }
            );
        } else {
            binding.setShowLoadMoreProgressbar(true);
            channel.query(
                new ChannelQueryRequest().withMessages(Pagination.LESS_THAN, channelState.getMessages().get(0).getId(), Constant.DEFAULT_LIMIT),
                new QueryChannelCallback() {
                    @Override
                    public void onSuccess(ChannelState response) {
                        binding.setShowLoadMoreProgressbar(false);
                        List<Message> newMessages = new ArrayList<>(response.getMessages());
                        if (newMessages.size() < Constant.DEFAULT_LIMIT) noHistory = true;

                        // Set Date Time
                        Message.setStartDay(newMessages, null);
                        // Add new to current Message List
                        for (int i = newMessages.size() - 1; i > -1; i--)
                            channelMessages.add(0, newMessages.get(i));

                        scrollPosition = ((LinearLayoutManager) binding.rvMessage.getLayoutManager()).findLastCompletelyVisibleItemPosition() + response.getMessages().size();
                        mViewModel.setChannelMessages(channelMessages);
                        isCalling = false;
                    }
                    @Override
                    public void onError(String errMsg, int errCode) {
                        Utils.showMessage(getContext(), errMsg);
                        isCalling = false;
                        binding.setShowLoadMoreProgressbar(false);
                    }
                }
            );
        }
    }

    // endregion

    // region Check Message Read

    private void checkReadMark() {
        if (channelState.getLastMessage() == null) return;
//        if (!Global.readMessage(channelState.getReadDateOfChannelLastMessage(StreamChat.getInstance().getUserId()),
//                channelState.getLastMessage().getCreatedAt())) {
//            messageMarkRead();
//        }
    }

    private void messageMarkRead() {
        MarkReadRequest request = new MarkReadRequest(channelMessages.get(channelMessages.size() - 1).getId());
        client.markRead(channel.getId(), request, new EventCallback() {
            @Override
            public void onSuccess(EventResponse response) {

            }

            @Override
            public void onError(String errMsg, int errCode) {
                Utils.showMessage(getContext(), errMsg);
            }
        });
    }

    // endregion

    /**
     * Permission check
     */
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
            if (!granted) PermissionChecker.showRationalDialog(getContext(), this);
        }
    }

    // endregion
}
