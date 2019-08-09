package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.enums.Pagination;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.view.MessageInputView;

import java.util.ArrayList;
import java.util.List;


/*
* - store the channel data
* - load more data
* -
 */
public class ChannelViewModel extends AndroidViewModel implements MessageInputView.SendMessageListener {
    private final String TAG = ChannelViewModel.class.getSimpleName();

    public MutableLiveData<Boolean> loading;
    public MutableLiveData<Boolean> loadingMore;
    public MutableLiveData<Boolean> failed;
    public MutableLiveData<Boolean> online;

    public Channel getChannel() {
        return channel;
    }

    private Channel channel;
    public ChannelState channelState;

    // TODO: Thread
    // TODO: Editing
    // TODO: Event handler
    public MutableLiveData<Boolean> endOfPagination;


    public ChannelViewModel(Application application, Channel channel) {
        super(application);
        this.channel = channel;
        Log.i(TAG, "BROKEN1" + channel.getCid());
    }

    public ChannelViewModel(@NonNull Application application) {
        super(application);
        loading = new MutableLiveData<>(true);
        loadingMore = new MutableLiveData<>(false);
        failed = new MutableLiveData<>(false);
        online = new MutableLiveData<>(true);
        endOfPagination = new MutableLiveData<>(false);
        Log.i(TAG, "BROKEN2");
    }


//    public void setChannel(Channel channel) {
//        this.channel = channel;
//        this.loadChannelState();
//        // TODO: set the adapter
//        this.channel.addEventHandler(new ChatChannelEventHandler(){
//            @Override
//            public void onMessageNew(Event event) {
//                messageEvent(event);
//                // update the adapter
//            }
//        });
//    }

    private void loadChannelState() {
        ChannelViewModel m = this;
        loading.setValue(true);
        Log.d(TAG, "Channel Connecting...");

        // TODO: figure out why postValue or setValue from the callback don't actually update the UI...

        channel.query(new QueryChannelCallback() {
            @Override
            public void onSuccess(ChannelState response) {
                m.loading.postValue(false);
                m.loading.setValue(false);
                m.channelState = response;
                List<Message> newMessages = new ArrayList<>(response.getMessages());
                if (newMessages.size() < Constant.DEFAULT_LIMIT) endOfPagination.setValue(true);

                Log.d(TAG, "channelState loaded" + newMessages.size());
            }

            @Override
            public void onError(String errMsg, int errCode) {
                m.loading.postValue(false);
                Log.d(TAG, "Failed Connect Channel : " + errMsg);
            }
        });
    }

    private void messageEvent(Event event) {
        Message message = event.getMessage();
        if (message == null) return;

        switch (event.getType()) {
            case MESSAGE_NEW:
                //newMessageEvent(message);
                break;
            case MESSAGE_UPDATED:
//                if (isThreadMode() && message.getId().equals(thread_parentMessage.getId()))
//                    mViewModel.setReplyCount(message.getReplyCount());
            case MESSAGE_DELETED:
                //updateOrDeleteMessageEvent(event, message);
                break;
            default:
                break;
        }
    }

//    private void newMessageEvent(Message message) {
//        Message.setStartDay(Arrays.asList(message), getLastMessage());
//
//        switch (message.getType()) {
//            case ModelType.message_regular:
//                if (!message.isIncoming())
//                    message.setDelivered(true);
//
//                messages().remove(ephemeralMessage);
//                if (message.isIncoming() && !isShowLastMessage) {
//                    scrollPosition = -1;
////                    binding.tvNewMessage.setVisibility(View.VISIBLE);
//                } else {
//                    scrollPosition = 0;
//                }
//                mViewModel.setChannelMessages(channelMessages);
//                messageMarkRead();
//                break;
//            case ModelType.message_ephemeral:
//            case ModelType.message_error:
//                boolean isContain = false;
//                for (int i = messages().size() - 1; i >= 0; i--) {
//                    Message message1 = messages().get(i);
//                    if (message1.getId().equals(message.getId())) {
//                        messages().remove(message1);
//                        isContain = true;
//                        break;
//                    }
//                }
//                if (!isContain) messages().add(message);
//                scrollPosition = 0;
//                if (isThreadMode()) {
//                    mThreadAdapter.notifyDataSetChanged();
//                    threadBinding.rvThread.scrollToPosition(threadMessages.size() - 1);
//                } else {
//                    mViewModel.setChannelMessages(messages());
//                }
//                break;
//            case ModelType.message_reply:
//                if (isThreadMode() && message.getParentId().equals(thread_parentMessage.getId())) {
//                    messages().remove(ephemeralMessage);
//                    threadMessages.add(message);
//                    mThreadAdapter.notifyDataSetChanged();
//                    threadBinding.rvThread.scrollToPosition(threadMessages.size() - 1);
//                }
//                break;
//            case ModelType.message_system:
//                break;
//            default:
//                break;
//        }
//    }

//    public void sendNewMessage(Message message) {
//        if (offline) {
//            //sendOfflineMessage();
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

    public void loadMore() {
        Log.d(TAG, "ViewModel loadMore called");
        if (loadingMore.getValue()) return;

        loadingMore.setValue(true);

        channel.query(
                new ChannelQueryRequest().withMessages(Pagination.LESS_THAN, channelState.getMessages().get(0).getId(), Constant.DEFAULT_LIMIT),
                new QueryChannelCallback() {
                    @Override
                    public void onSuccess(ChannelState response) {
                        loadingMore.setValue(false);
                        List<Message> newMessages = new ArrayList<>(response.getMessages());
                        if (newMessages.size() < Constant.DEFAULT_LIMIT) endOfPagination.setValue(true);
                    }
                    @Override
                    public void onError(String errMsg, int errCode) {
                        loadingMore.setValue(false);
                    }
                }
        );

        // TODO: Handle thread...

        //if (isThreadMode()) {
//            binding.setShowMainProgressbar(true);
//            client.getReplies(thread_parentMessage.getId(),
//                    String.valueOf(Constant.THREAD_MESSAGE_LIMIT),
//                    threadMessages.get(0).getId(), new GetRepliesCallback() {
//                        @Override
//                        public void onSuccess(GetRepliesResponse response) {
//                            binding.setShowMainProgressbar(false);
//                            List<Message> newMessages = new ArrayList<>(response.getMessages());
//                            if (newMessages.size() < Constant.THREAD_MESSAGE_LIMIT)
//                                noHistoryThread = true;
//
//                            Message.setStartDay(newMessages, null);
//                            // Add new to current Message List
//                            for (int i = newMessages.size() - 1; i > -1; i--) {
//                                threadMessages.add(0, newMessages.get(i));
//                            }
//                            int scrollPosition = ((LinearLayoutManager) recyclerView().getLayoutManager()).findLastCompletelyVisibleItemPosition() + response.getMessages().size();
//                            mThreadAdapter.notifyDataSetChanged();
//                            recyclerView().scrollToPosition(scrollPosition);
//                            isCalling = false;
//                        }
//
//                        @Override
//                        public void onError(String errMsg, int errCode) {
//                            Utils.showMessage(getContext(), errMsg);
//                            isCalling = false;
//                            binding.setShowMainProgressbar(false);
//                        }
//                    }
//            );
       // } else {


       // }
    }

    @Override
    public void onSendMessage(Message message) {
        Log.i(TAG, "onSendMessage handler called at viewmodel level");
        channel.sendMessage(message,
            new MessageCallback() {
                @Override
                public void onSuccess(MessageResponse response) {
                    Message responseMessage = response.getMessage();
                    Log.i(TAG, "onSuccess event for sending the message");
                    // somehow we need to reach the adapter... (but the viewmodel can't know about the adapter)
                    // - livedata.observe is one way
                    // - the adapter listening to an event from the viewmodel or client is another
                    // -- new message event
                    // -- updated message event
                    // -- deleted message event
                    // -- load more event
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    //binding.messageInput.setEnabled(true);
                }
        });

    }
}