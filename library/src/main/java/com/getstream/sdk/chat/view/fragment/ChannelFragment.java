//package com.getstream.sdk.chat.view.fragment;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProviders;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.facebook.drawee.backends.pipeline.Fresco;
//import com.getstream.sdk.chat.StreamChat;
//import com.getstream.sdk.chat.adapter.Entity;
//import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
//import com.getstream.sdk.chat.databinding.ChannelFragmentBinding;
//import com.getstream.sdk.chat.databinding.ViewThreadBinding;
//import com.getstream.sdk.chat.enums.EventType;
//import com.getstream.sdk.chat.function.AttachmentFunction;
//import com.getstream.sdk.chat.function.ReactionFunction;
//import com.getstream.sdk.chat.function.SendFileFunction;
//import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
//import com.getstream.sdk.chat.model.Attachment;
//import com.getstream.sdk.chat.model.Channel;
//import com.getstream.sdk.chat.model.Event;
//import com.getstream.sdk.chat.model.MessageTagModel;
//import com.getstream.sdk.chat.model.ModelType;
//import com.getstream.sdk.chat.model.SelectAttachmentModel;
//import com.getstream.sdk.chat.rest.Message;
//import com.getstream.sdk.chat.rest.User;
//import com.getstream.sdk.chat.rest.core.ChatChannelEventHandler;
//import com.getstream.sdk.chat.rest.core.Client;
//import com.getstream.sdk.chat.rest.interfaces.EventCallback;
//import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
//import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
//import com.getstream.sdk.chat.rest.request.MarkReadRequest;
//import com.getstream.sdk.chat.rest.request.SendActionRequest;
//import com.getstream.sdk.chat.rest.response.ChannelState;
//import com.getstream.sdk.chat.rest.response.EventResponse;
//import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
//import com.getstream.sdk.chat.rest.response.MessageResponse;
//import com.getstream.sdk.chat.utils.Constant;
//import com.getstream.sdk.chat.utils.Global;
//import com.getstream.sdk.chat.utils.PermissionChecker;
//import com.getstream.sdk.chat.utils.Utils;
//import com.getstream.sdk.chat.view.MessageInputView;
//import com.getstream.sdk.chat.viewmodel.ChannelFragmentViewModelFactory;
//import com.getstream.sdk.chat.viewmodel.ChannelViewModelOld;
//
//import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static com.getstream.sdk.chat.utils.Utils.TAG;
//
//public class ChannelFragment extends Fragment {
//
//    private Client client;
//    public String channelType;
//    public String channelID;
//    public HashMap<String, Object> channelExtraData;
//
//    private Channel channel;
//
//    // ViewModel & Binding
//    private ChannelViewModelOld mViewModel;
//    private ChannelFragmentBinding binding;
//    private ViewThreadBinding threadBinding;
//    // Arguments for Channel
//    String channelIdFromChannelList = null;
//    private ChannelState channelState;
//    private List<Message> channelMessages, threadMessages;
//    // Adapter & LayoutManager
//    private MessageListItemAdapter mChannelMessageAdapter, mThreadAdapter;
//    private RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
//    private RecyclerView.LayoutManager mLayoutManager_thread = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
//    private RecyclerView.LayoutManager mLayoutManager_thread_header = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
//    // Functions
//    private SendFileFunction sendFileFunction;
//    private ReactionFunction reactionFunction;
//    // Customization MessageItemView
//    private int messageItemLayoutId;
//    private String messageItemViewHolderName;
//    // Misc
//    public boolean singleConversation;
//    private boolean isShowLastMessage;
//    private int scrollPosition = 0;
//    private static int fVPosition, lVPosition;
//    private boolean noHistory, noHistoryThread;
//
//    private int channelSubscriptionId;
//
//    public void unsubscribeFromChannelEvents(){
//        channel.removeEventHandler(channelSubscriptionId);
//        channelSubscriptionId = 0;
//    }
//
//    public Client client(){
//        return StreamChat.getInstance(getActivity().getApplication());
//    }
//
//    public void subscribeToChannelEvents(){
//        if (channelSubscriptionId != 0) {
//            return;
//        }
//
//        channelSubscriptionId = channel.addEventHandler(new ChatChannelEventHandler(){
//            @Override
//            public void onTypingStart(Event event) {
//                footerEvent(event);
//            }
//            @Override
//            public void onTypingStop(Event event) {
//                footerEvent(event);
//            }
////            @Override
////            public void onMessageNew(Event event) {
////                messageEvent(event);
////            }
////            @Override
////            public void onMessageUpdated(Event event) {
////                messageEvent(event);
////            }
////            @Override
////            public void onMessageDeleted(Event event) {
////                messageEvent(event);
////            }
//            @Override
//            public void onMessageRead(Event event) {
//                if (!channelState.getLastMessage().isIncoming()) {
//                    mChannelMessageAdapter.notifyItemChanged(channelMessages.size() - 1);
//                }
//            }
//            @Override
//            public void onReactionNew(Event event) {
//                reactionEvent(event);
//            }
//            @Override
//            public void onReactionDeleted(Event event) {
//               reactionEvent(event);
//            }
//            @Override
//            public void onChannelDeleted(Event event) {
//                getActivity().runOnUiThread(() -> {
//                    Utils.showMessage(getContext(), "Channel Owner just removed this channel!");
//                });
//            }
//        });
//    }
//
//    // region LifeCycle
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//
//        client = StreamChat.getInstance(getActivity().getApplication());
//        channel = client.channel(channelType, channelID, channelExtraData);
//
//        // set binding
//        binding = ChannelFragmentBinding.inflate(inflater, container, false);
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        ChannelFragmentViewModelFactory factory = new ChannelFragmentViewModelFactory(channelState);
//        mViewModel = ViewModelProviders.of(this, factory).get(ChannelViewModelOld.class);
//
//        // set WS handler
//        subscribeToChannelEvents();
//
//        // Permission Check
//        PermissionChecker.permissionCheck(getActivity(), null);
//        try {
//            Fresco.initialize(getContext().getApplicationContext());
//        } catch (Exception e) {
//        }
//
//        threadBinding = binding.clThread;
//        onBackPressed();
//
//        if (singleConversation) {
//            client.waitForConnection(new ClientConnectionCallback() {
//                @Override
//                public void onSuccess() {
//                    //getChannel();
//                }
//                @Override
//                public void onError(String errMsg, int errCode) {
//                    binding.setShowMainProgressbar(false);
//                }
//            });
//        } else {
//            initReconnection();
//            setDeliverLastMessage();
//            configUIs();
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        client = StreamChat.getInstance(getActivity().getApplication());
//        channel = client.channel(channelType, channelID);
//
//        subscribeToChannelEvents();
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Constant.BC_RECONNECT_CHANNEL);
//        filter.addCategory(Intent.CATEGORY_DEFAULT);
//        getContext().registerReceiver(receiver, filter);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        unsubscribeFromChannelEvents();
//
//        try {
//            getContext().unregisterReceiver(receiver);
//        } catch (IllegalArgumentException e) {
//            if (e.getMessage().contains("Receiver not registered")) {
//                Log.w(TAG, "Tried to unregister the reciver when it's not registered");
//            } else {
//                throw e;
//            }
//        }
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == Constant.CAPTURE_IMAGE_REQUEST_CODE) {
//            try {
//                Object object = data.getExtras().get("data");
//                if (object.getClass().equals(Bitmap.class)) {
//                    Bitmap bitmap = (Bitmap) object;
//                    Uri uri = Utils.getUriFromBitmap(getContext(), bitmap);
//                    sendFileFunction.progressCapturedMedia(getContext(), uri, true);
//                }
//            } catch (Exception e) {
//                Uri uri = data.getData();
//                if (uri == null) return;
//                sendFileFunction.progressCapturedMedia(getContext(), uri, false);
//            }
//        }
//    }
//
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//    }
//
//    private void onBackPressed() {
//        getView().setFocusableInTouchMode(true);
//        getView().requestFocus();
//        getView().setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
//            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
//                // Close if File Attach View is opened.
////                if (binding.clAddFile.getVisibility() == View.VISIBLE) {
////                    sendFileFunction.onClickAttachmentViewClose(null);
////                    return true;
////                }
////                // Close if Selecting Photo View is opened.
////                if (binding.clSelectPhoto.getVisibility() == View.VISIBLE) {
////                    sendFileFunction.onClickSelectMediaViewClose(null);
////                    return true;
////                }
//                // Close if Thread View is opened.
//                if (isThreadMode()) {
//                    onClickCloseThread(null);
//                    return true;
//                }
//                // Cancel if editing message.
////                if (binding.etMessage.getTag() != null) {
////                    cancelEditMessage();
////                    return true;
////                }
//                if (!singleConversation) {
//                    //finish();
//                    return true;
//                }
//                return false;
//            }
//            return false;
//        });
//    }
//
//
//    private void initReconnection() {
//        binding.setViewModel(mViewModel);
//        threadBinding.setViewModel(mViewModel);
//        channelMessages = channelState.getMessages();
//        channel.setChannelState(this.channelState);
//
//        reactionFunction = new ReactionFunction(channel);
//
//        checkReadMark();
//        noHistory = channelMessages.size() < Constant.CHANNEL_MESSAGE_LIMIT;
//        noHistoryThread = false;
//    }
//
//    private boolean lockRVScrollListener = false;
//
//
//    private List<Message> messages() {
//        return isThreadMode() ? threadMessages : channelMessages;
//    }
//
//    private RecyclerView recyclerView() {
//        return isThreadMode() ? threadBinding.rvThread : binding.mlvMessageList;
//    }
//
//    private boolean isNoHistory() {
//        return isThreadMode() ? noHistoryThread : noHistory;
//    }
//
//    // endregion
//
//    // region Config UIs
//    private void configUIs() {
//        configActionBar(); // Hides Action Bar
//        configMessageInputView();
//        configMessageRecyclerView(); // Message RecyclerView
//        // Bottom View
////        binding.tvNewMessage.setVisibility(View.GONE);
////        binding.tvNewMessage.setOnClickListener((View v) -> {
////            scrollPosition = 0;
////            recyclerView().scrollToPosition(messages().size());
////            binding.tvNewMessage.setVisibility(View.GONE);
////        });
//        // File Attachment
//    }
//
//    private void configActionBar() {
//        try {
//            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void configMessageRecyclerView() {
//        mLayoutManager.scrollToPosition(channelMessages.size());
//        binding.mlvMessageList.setLayoutManager(mLayoutManager);
//        setScrollDownHideKeyboard(binding.mlvMessageList);
//
//    }
//
//
//
//    private void setScrollDownHideKeyboard(RecyclerView recyclerView) {
//        fVPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
//        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (lockRVScrollListener) return;
//                int currentFirstVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
//                int currentLastVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
//                if (!isThreadMode()) {
//                    if (currentLastVisible >= messages().size() - 1) isShowLastMessage = true;
//                    else isShowLastMessage = false;
//                }
//
//                if (currentFirstVisible < fVPosition) {
//                    Utils.hideSoftKeyboard(getActivity());
//                    binding.messageInput.clearFocus();
//                    //if (currentFirstVisible == 0 && !isNoHistory()) loadMore();
//
//                }
//
//                new Handler().postDelayed(() -> {
//                    lVPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
//                }, 500);
//                fVPosition = currentFirstVisible;
//            }
//        });
//    }
//
//    // endregion
//
//    // region Send Message
//    private Message ephemeralMessage = null;
//
//    /**
//     * Send Message - Send a message to this channel
//     */
//    public void sendMessage(String messageText) {
//        if (binding.messageInput.IsEditing()) {
//            updateMessage(binding.messageInput.getMessageText());
//        } else {
//            sendNewMessage(messageText, binding.messageInput.GetAttachments(), null);
//        }
//    }
//
//    public void sendNewMessage(String text, List<Attachment> attachments, String resendMessageId) {
//        if (!client.isConnected()) {
//            sendOfflineMessage();
//            return;
//        }
//        if (resendMessageId == null) {
//            ephemeralMessage = createEphemeralMessage(false);
//            handleAction(ephemeralMessage);
//        }
//        binding.messageInput.setEnabled(false);
//        channel.sendMessage(text,
//                attachments,
//                isThreadMode() ? thread_parentMessage.getId() : null,
//                new MessageCallback() {
//                    @Override
//                    public void onSuccess(MessageResponse response) {
//                        binding.messageInput.setEnabled(true);
//                        progressSendMessage(response.getMessage(), resendMessageId);
//                    }
//
//                    @Override
//                    public void onError(String errMsg, int errCode) {
//                        binding.messageInput.setEnabled(true);
//                        Utils.showMessage(getContext(), errMsg);
//                    }
//                });
//    }
//
//    public void updateMessage(String messageText) {
//        if (!client.isConnected()) {
//            Utils.showMessage(getContext(), "No internet connection!");
//            return;
//        }
//        binding.messageInput.setEnabled(false);
//        channel.updateMessage(binding.messageInput.getMessageText(),
//                (Message) binding.messageInput.GetEditMessage(),
//                sendFileFunction.getSelectedAttachments(),
//                new MessageCallback() {
//                    @Override
//                    public void onSuccess(MessageResponse response) {
//                        response.getMessage().setDelivered(true);
//                        binding.messageInput.CancelEditMessage();
//                        binding.messageInput.setEnabled(true);
//                    }
//
//                    @Override
//                    public void onError(String errMsg, int errCode) {
//                        binding.messageInput.setEnabled(true);
//                        Utils.showMessage(getContext(), errMsg);
//                    }
//                });
//    }
//
//    private void configMessageInputView() {
//
//        binding.setShowLoadMoreProgressbar(false);
//        binding.setNoConnection(!StreamChat.getInstance(getActivity().getApplication()).isConnected());
//        binding.messageInput.setOnFocusChangeListener((View view, boolean hasFocus) -> {
//            lockRVScrollListener = hasFocus;
//        });
//
//        // TODO: move this
//        KeyboardVisibilityEvent.setEventListener(
//                getActivity(), (boolean isOpen) -> {
//                    if (!isOpen) {
//                        binding.messageInput.clearFocus();
//                    } else {
//                        lockRVScrollListener = true;
//                        new Handler().postDelayed(() -> {
//                            lockRVScrollListener = false;
//                        }, 500);
//                    }
//                    if (lVPosition > messages().size() - 2)
//                        recyclerView().scrollToPosition(lVPosition);
//
//                });
//
//        ChannelFragment a = this;
//
//        binding.messageInput.setOnSendMessageListener(new MessageInputView.SendMessageListener() {
//            @Override
//            public void onSendMessage(Message message) {
//                // TODO send the message
//                Log.i(TAG, "actually sending a message, epic");
//                a.sendMessage(message.getText());
//            }
//        });
//
//        binding.messageInput.setTypingListener(new MessageInputView.TypingListener() {
//            @Override
//            public void onKeystroke() {
//                // TODO: forward to the client
//                Log.i(TAG, "i entered a letter, awesome");
//            }
//
//            @Override
//            public void onStopTyping() {
//                // TODO:  forward to the client
//                Log.i(TAG, "stop typing");
//            }
//        });
//    }
//
//    public void resendMessage(Message message) {
//        if (!client.isConnected()) {
//            Utils.showMessage(getContext(), Constant.NO_INTERNET);
//            return;
//        }
//        handleAction(message);
//        sendNewMessage(message.getText(), null, message.getId());
//    }
//
//    public void sendGiphy(String type, Message message) {
//        Map<String, String> map = new HashMap<>();
//        if (type.equals(Constant.TAG_ACTION_SEND))
//            map.put("image_action", ModelType.action_send);
//        else
//            map.put("image_action", ModelType.action_shuffle);
//
//        SendActionRequest request = new SendActionRequest(channel.getId(), message.getId(), ModelType.channel_messaging, map);
//        client.sendAction(message.getId(), request, new MessageCallback() {
//            @Override
//            public void onSuccess(MessageResponse response) {
//                handleAction(message);
//                response.getMessage().setDelivered(true);
//                handleAction(response.getMessage());
//            }
//
//            @Override
//            public void onError(String errMsg, int errCode) {
//                Log.d(TAG, errMsg);
//            }
//        });
//    }
//
//    public void progressSendMessage(Message message, String resendMessageId) {
//        if (resendMessageId != null) {
//            Global.removeEphemeralMessage(channel.getId(), resendMessageId);
//            //initSendMessage();
//        } else {
//            if (Message.isCommandMessage(message) ||
//                    message.getType().equals(ModelType.message_error)) {
//                channelMessages.remove(ephemeralMessage);
//                message.setDelivered(true);
//            } else {
//                ephemeralMessage.setId(message.getId());
//            }
//
//            handleAction(message);
//        }
//    }
//
//    private void setDeliverLastMessage() {
//        if (messages() == null || messages().isEmpty()) return;
//        if (!messages().get(messages().size() - 1).isIncoming())
//            messages().get(messages().size() - 1).setDelivered(true);
//    }
//
//    private void sendOfflineMessage() {
//        handleAction(createEphemeralMessage(true));
//    }
//
//    private Message createEphemeralMessage(boolean isOffle) {
//        Message message = new Message();
//        message.setId(message.convertDateToString(new Date()));
//        message.setText(binding.messageInput.getMessageText());
//        message.setType(isOffle ? ModelType.message_error : ModelType.message_ephemeral);
//        message.setCreatedAt(new Date());
//        message.setStartDay(Arrays.asList(message), getLastMessage());
////        message.setUser(StreamChat.getInstance().getUser());
//        if (isThreadMode())
//            message.setParent_id(thread_parentMessage.getId());
//        if (isOffle)
//            Global.setEphemeralMessage(channel.getId(), message);
//        return message;
//    }
//
//    private Message getLastMessage() {
//        return messages().isEmpty() ? null : messages().get(messages().size() - 1);
//    }
//
//    // Edit
//    private void editMessage(Message message) {
//        binding.messageInput.EditMessage(message);
//        binding.messageInput.requestInputFocus();
//        if (!TextUtils.isEmpty(message.getText())) {
//            // TODO: figure out what this does...
////            binding.etMessage.setText(message.getText());
////            binding.etMessage.setSelection(binding.etMessage.getText().length());
//        }
//        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
//            for (Attachment attachment : message.getAttachments())
//                attachment.config.setUploaded(true);
//
//            if (message.getAttachments().get(0).getType().equals(ModelType.attach_file)) {
//                String fileType = message.getAttachments().get(0).getMime_type();
//                if (fileType.equals(ModelType.attach_mime_mov) ||
//                        fileType.equals(ModelType.attach_mime_mp4)) {
//                    sendFileFunction.onClickSelectMediaViewOpen(null, message.getAttachments());
//                } else {
//                    sendFileFunction.onClickSelectFileViewOpen(null, message.getAttachments());
//                }
//            } else {
//                sendFileFunction.onClickSelectMediaViewOpen(null, message.getAttachments());
//            }
//        }
//    }
//
//    private void cancelEditMessage() {
//        binding.messageInput.CancelEditMessage();
//    }
//
//    // endregion
//
//    // region Message Item Touch Action
//    private void messageItemClickListener(Object object) {
//        if (isCallingThread) return;
//        if (object.getClass().equals(SelectAttachmentModel.class)) {
//            new AttachmentFunction().progressAttachment((SelectAttachmentModel) object, getActivity());
//            return;
//        }
//
//        if (object.getClass().equals(MessageTagModel.class)) {
//            MessageTagModel tag = (MessageTagModel) object;
//            Message message = messages().get(tag.position);
//            switch (tag.type) {
//                case Constant.TAG_MOREACTION_REPLY:
//                    configThread(channelMessages.get(tag.position));
//                    break;
//                case Constant.TAG_ACTION_SEND:
//                case Constant.TAG_ACTION_SHUFFLE:
//                    sendGiphy(tag.type, message);
//                    break;
//                case Constant.TAG_ACTION_CANCEL:
//                    handleAction(messages().get(tag.position));
//                    break;
//                case Constant.TAG_MESSAGE_REACTION:
//                    int firstListItemPosition = ((LinearLayoutManager) recyclerView().getLayoutManager()).findFirstVisibleItemPosition();
//                    final int lastListItemPosition = firstListItemPosition + recyclerView().getChildCount() - 1;
//                    int childIndex;
//                    if (tag.position < firstListItemPosition || tag.position > lastListItemPosition) {
//                        childIndex = tag.position;
//                    } else {
//                        childIndex = tag.position - firstListItemPosition;
//                    }
//                    int originY = recyclerView().getChildAt(childIndex).getBottom();
//                    reactionFunction.showReactionDialog(getContext(), message, originY);
//                    break;
//                case Constant.TAG_MESSAGE_RESEND:
//                    resendMessage(message);
//                    break;
//                case Constant.TAG_MESSAGE_INVALID_COMMAND:
//                    handleAction(message);
//                    //binding.etMessage.setText("/");
//                    break;
//                case Constant.TAG_MESSAGE_CHECK_DELIVERED:
//                    showAlertReadUsers(message);
//                    break;
//                default:
//                    break;
//            }
//        }
//    }
//
//    private void messageItemLongClickListener(Object object) {
//        final int position = Integer.parseInt(object.toString());
//        final Message message = messages().get(position);
//
//        reactionFunction.showMoreActionDialog(getContext(), message, (View v) -> {
//            String type = (String) v.getTag();
//            switch (type) {
//                case Constant.TAG_MOREACTION_EDIT:
//                    editMessage(message);
//                    break;
//                case Constant.TAG_MOREACTION_DELETE:
//                    channel.deleteMessage(message,
//                            new MessageCallback() {
//                                @Override
//                                public void onSuccess(MessageResponse response) {
//                                    Utils.showMessage(getContext(), "Deleted Successfully");
//                                }
//
//                                @Override
//                                public void onError(String errMsg, int errCode) {
//                                    Log.d(TAG, "Failed DeleteMessage : " + errMsg);
//                                }
//                            });
//                    break;
//                case Constant.TAG_MOREACTION_REPLY:
//                    if (!isThreadMode())
//                        configThread(message);
//                    break;
//                default:
//                    break;
//            }
//        });
//    }
//
//    private void showAlertReadUsers(Message message) {
//        List<User> readUsers = Global.getReadUsers(channelState, message);
//        if (readUsers == null) return;
//        String msg = "";
//        if (readUsers.size() > 0) {
//            if (readUsers.size() == 1) msg = readUsers.get(0).getName();
//            else {
//                for (int i = 0; i < readUsers.size(); i++) {
//                    User user = readUsers.get(i);
//                    if (i == readUsers.size() - 2) msg += user.getName() + " and ";
//                    else if (i == readUsers.size() - 1) msg += user.getName();
//                    else msg += user.getName() + ", ";
//                }
//            }
//        } else {
//            if (message.isDelivered()) {
//                msg = "Delivered";
//            } else {
//                msg = "sending...";
//            }
//        }
//        Utils.showMessage(getContext(), msg);
//    }
//
//
//
//
//    // refresh Current Typing users in this channel
//    private Handler clearTyingUserHandler = new Handler();
//    private Runnable runnableTypingClear = new Runnable() {
//        @Override
//        public void run() {
//            try {
//                Global.typingUsers = new ArrayList<>();
//                try {
//                    mChannelMessageAdapter.notifyItemChanged(channelMessages.size());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } finally {
////                eventFunction.sendEvent(Event.typing_stop);
//                clearTyingUserHandler.postDelayed(runnableTypingClear, Constant.TYPYING_CLEAN_INTERVAL);
//            }
//        }
//    };
//
//    // endregion
//
//    // region Action
//    private void handleAction(Message message) {
//        switch (message.getType()) {
//            case ModelType.message_ephemeral:
//            case ModelType.message_error:
//                Event event = new Event();
//                event.setType(EventType.MESSAGE_NEW);
//                event.setMessage(message);
//                //messageEvent(event);
//                break;
//            default:
//                break;
//        }
//    }
//    // endregion
//
//    // region Thread
//    private Message thread_parentMessage = null;
//    private boolean isCallingThread = false;
//
//    private void configThread(@NonNull Message message) {
//
//        mViewModel.setReplyCount(message.getReplyCount());
//        cleanEditView();
//
//        thread_parentMessage = message;
//        threadMessages = new ArrayList<>();
//
//        threadBinding.rvHeader.setLayoutManager(mLayoutManager_thread_header);
//        MessageListItemAdapter mThreadHeaderAdapter = new MessageListItemAdapter(getContext(), this.channelState, new ArrayList<Entity>());
//        threadBinding.rvHeader.setAdapter(mThreadHeaderAdapter);
//
//        if (message.getReplyCount() == 0) {
//            setThreadAdapter();
//            threadBinding.setShowThread(true);
//        } else {
//            binding.setShowMainProgressbar(true);
//
//            if (isCallingThread) return;
//            isCallingThread = true;
//            client.getReplies(message.getId(), String.valueOf(Constant.THREAD_MESSAGE_LIMIT), null, new GetRepliesCallback() {
//                @Override
//                public void onSuccess(GetRepliesResponse response) {
//                    threadMessages = response.getMessages();
//
//                    List<Message> ephemeralThreadMessages = Global.getEphemeralMessages(channel.getId(), thread_parentMessage.getId());
//                    if (ephemeralThreadMessages != null && !ephemeralThreadMessages.isEmpty()) {
//                        for (int i = 0; i < ephemeralThreadMessages.size(); i++) {
//                            threadMessages.add(ephemeralThreadMessages.get(i));
//                        }
//                    }
//
//                    Message.setStartDay(threadMessages, null);
//                    setThreadAdapter();
//                    threadBinding.setShowThread(true);
//                    binding.setShowMainProgressbar(false);
//                    if (threadMessages.size() < Constant.THREAD_MESSAGE_LIMIT)
//                        noHistoryThread = true;
//                    isCallingThread = false;
//                }
//
//                @Override
//                public void onError(String errMsg, int errCode) {
//                    Utils.showMessage(getContext(), errMsg);
//                    thread_parentMessage = null;
//                    binding.setShowMainProgressbar(false);
//                    isCallingThread = false;
//                }
//            });
//        }
//        // Clean RecyclerView
//    }
//
//    /**
//     * Hide thread view
//     */
//    private void onClickCloseThread(View v) {
//        threadBinding.setShowThread(false);
//        cleanEditView();
//        setScrollDownHideKeyboard(binding.mlvMessageList);
//        isCallingThread = false;
//    }
//
//    private void setThreadAdapter() {
//        if (threadMessages.size() > 0)
//            mLayoutManager_thread.scrollToPosition(threadMessages.size() - 1);
//
//        threadBinding.rvThread.setLayoutManager(mLayoutManager_thread);
//        mThreadAdapter = new MessageListItemAdapter(getContext(), this.channelState, new ArrayList<Entity>());
//        threadBinding.rvThread.setAdapter(mThreadAdapter);
//        setScrollDownHideKeyboard(threadBinding.rvThread);
//    }
//
//    private boolean isThreadMode() {
//        if (thread_parentMessage == null) return false;
//        return true;
//    }
//
//    private void cleanEditView() {
//        binding.messageInput.CancelEditMessage();
//        thread_parentMessage = null;
//        threadMessages = null;
//        lVPosition = 0;
//        fVPosition = 0;
//        noHistoryThread = false;
//        binding.mlvMessageList.clearOnScrollListeners();
//        threadBinding.rvThread.clearOnScrollListeners();
//        Utils.hideSoftKeyboard(getActivity());
//    }
//    // region Listener
//
//    // Event Listener
//
//
//
//
//
//    private void updateOrDeleteMessageEvent(Event event, Message message) {
//        if (!message.isIncoming())
//            message.setDelivered(true);
//        int changedIndex_ = 0;
//        if (message.getType().equals(ModelType.message_reply)) {
//            if (!isThreadMode()) return;
//            for (int i = 0; i < threadMessages.size(); i++) {
//                if (message.getId().equals(threadMessages.get(i).getId())) {
//                    if (event.getType().equals(EventType.MESSAGE_DELETED))
//                        message.setText(Constant.MESSAGE_DELETED);
//                    changedIndex_ = i;
//                    threadMessages.set(i, message);
//                    break;
//                }
//            }
//            final int changedIndex = changedIndex_;
//            mThreadAdapter.notifyItemChanged(changedIndex);
//        } else {
//            for (int i = 0; i < channelMessages.size(); i++) {
//                if (message.getId().equals(channelMessages.get(i).getId())) {
//                    if (event.getType().equals(EventType.MESSAGE_DELETED))
//                        message.setText(Constant.MESSAGE_DELETED);
//                    changedIndex_ = i;
//                    channelMessages.set(i, message);
//                    break;
//                }
//            }
//            final int changedIndex = changedIndex_;
//            scrollPosition = -1;
//            mViewModel.setChannelMessages(channelMessages);
//            mChannelMessageAdapter.notifyItemChanged(changedIndex);
//        }
//    }
//
//    private void reactionEvent(Event event) {
//        Message message = event.getMessage();
//        if (message == null) return;
//        int changedIndex_ = 0;
//        if (message.getType().equals(ModelType.message_regular)) {
//            for (int i = 0; i < channelMessages.size(); i++) {
//                if (message.getId().equals(channelMessages.get(i).getId())) {
//                    changedIndex_ = i;
//                    channelMessages.set(i, message);
//                    break;
//                }
//            }
//            final int changedIndex = changedIndex_;
//            getActivity().runOnUiThread(() -> {
//                scrollPosition = -1;
//                mViewModel.setChannelMessages(channelMessages);
//                mChannelMessageAdapter.notifyItemChanged(changedIndex);
//            });
//        } else if (message.getType().equals(ModelType.message_reply)) {
//            if (thread_parentMessage == null) return;
//            if (!message.getParentId().equals(thread_parentMessage.getId())) return;
//
//            for (int i = 0; i < threadMessages.size(); i++) {
//                if (message.getId().equals(threadMessages.get(i).getId())) {
//                    changedIndex_ = i;
//                    threadMessages.set(i, message);
//                    break;
//                }
//            }
//            final int changedIndex = changedIndex_;
//            getActivity().runOnUiThread(() -> {
//                mThreadAdapter.notifyItemChanged(changedIndex);
//            });
//        }
//    }
//
//    private BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (!intent.getAction().equals(Constant.BC_RECONNECT_CHANNEL))
//                return;
//
//            Log.d(TAG, "Reconnection!");
//            channelState = client.getChannelByCid(channel.getCid()).getChannelState();
//            Message.setStartDay(channelState.getMessages(), null);
//            initReconnection();
//            // Check Ephemeral Messages
//            getActivity().runOnUiThread(() -> {
//                setDeliverLastMessage();
//                configUIs();
//                if (isThreadMode())
//                    configThread(thread_parentMessage);
//            });
//        }
//    };
//    // endregion
//
//    // region Footer Event
//
//    private void footerEvent(Event event) {
//        User user = event.getUser();
//        if (user == null) return;
//        if (user.getId().equals(client.getUserId())) return;
//
//        switch (event.getType()) {
//            case TYPING_START:
//                boolean isAdded = false; // If user already exits in typingUsers
//                for (int i = 0; i < Global.typingUsers.size(); i++) {
//                    User user1 = Global.typingUsers.get(i);
//                    if (user1.getId().equals(user.getId())) {
//                        isAdded = true;
//                        break;
//                    }
//                }
//                if (!isAdded)
//                    Global.typingUsers.add(user);
//
//                break;
//            case TYPING_STOP:
//                int index1 = -1; // If user already exits in typingUsers
//                for (int i = 0; i < Global.typingUsers.size(); i++) {
//                    User user1 = Global.typingUsers.get(i);
//                    if (user1.getId().equals(user.getId())) {
//                        index1 = i;
//                        break;
//                    }
//                }
//                if (index1 != -1)
//                    Global.typingUsers.remove(index1);
//                break;
//            default:
//                break;
//        }
//        mChannelMessageAdapter.notifyItemChanged(channelMessages.size());
//    }
//
//    // endregion
//
//    // endregion
//
//    // region Pagination
//
//
//
//
//    // endregion
//
//    // region Check Message Read
//
//    private void checkReadMark() {
//        if (channelState.getLastMessage() == null) return;
////        if (!Global.readMessage(channelState.getReadDateOfChannelLastMessage(StreamChat.getInstance().getUserId()),
////                channelState.getLastMessage().getCreatedAt___OLD())) {
////            messageMarkRead();
////        }
//    }
//
//    private void messageMarkRead() {
//        MarkReadRequest request = new MarkReadRequest(channelMessages.get(channelMessages.size() - 1).getId());
//        client.markRead(channel.getId(), request, new EventCallback() {
//            @Override
//            public void onSuccess(EventResponse response) {
//
//            }
//
//            @Override
//            public void onError(String errMsg, int errCode) {
//                Utils.showMessage(getContext(), errMsg);
//            }
//        });
//    }
//
//    // endregion
//
//    /**
//     * Permission check
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == Constant.PERMISSIONS_REQUEST) {
//            boolean granted = true;
//            for (int grantResult : grantResults)
//                if (grantResult != PackageManager.PERMISSION_GRANTED) {
//                    granted = false;
//                    break;
//                }
//            if (!granted) PermissionChecker.showRationalDialog(getContext(), this);
//        }
//    }
//
//    // endregion
//}
