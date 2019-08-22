//package com.getstream.sdk.chat;
//
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//
//import com.getstream.sdk.chat.model.Channel;
//import com.getstream.sdk.chat.model.Event;
//import com.getstream.sdk.chat.model.Member;
//import com.getstream.sdk.chat.model.Watcher;
//import com.getstream.sdk.chat.rest.Message;
//import com.getstream.sdk.chat.rest.User;
//import com.getstream.sdk.chat.rest.core.ChatChannelEventHandler;
//import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
//import com.getstream.sdk.chat.rest.response.ChannelState;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class ChannelRepository {
//
//    // internal state
//    private Channel mChannel;
//    private Map<String, Event> mTypingState;
//    private TypingLooperThread looper;
//
//    // constants
//    private long TYPING_TIMEOUT = 10000;
//
//    // channel data
//    private MutableLiveData<List<Message>> mMessages;
//    private MutableLiveData<Map<String, List<Message>>> mThreads;
//    private MutableLiveData<Map<String, Member>> mMembers;
//    private MutableLiveData<Number> mWatcherCount;
//    private MutableLiveData<User> mMutedUsers;
//    private MutableLiveData<Watcher> mWatchers;
//    private MutableLiveData<Date> mLastMessageAt;
//    private MutableLiveData<List<User>> mTypingUsers;
//
//    public ChannelRepository(Channel channel) {
//        mChannel = channel;
//        mMessages = new MutableLiveData<>();
//        mWatcherCount = new MutableLiveData<>();
//        mTypingState = new HashMap<>();
//        mTypingUsers = new MutableLiveData<>();
//        attachToClient();
//
//        looper = new TypingLooperThread();
//        looper.start();
//    }
//
//    private List<User> typingUsers() {
//        List<User> users = new ArrayList<>();
//        long now = new Date().getTime();
//        for (Event event: mTypingState.values()){
//            if (now - event.getCreatedAt().getTime() < TYPING_TIMEOUT) {
//                users.add(event.getUser());
//            }
//        }
//        return users;
//    }
//
//    /**
//     * Schedules a cleanup for the typing state to remove typing users that did not send
//     * typing.stop event
//     */
//    private void scheduleCleanup() {
//        if (looper == null || looper.mHandler == null) return;
//        looper.mHandler.removeCallbacksAndMessages(null);
//        looper.mHandler.postDelayed(() ->
//                mTypingUsers.postValue(typingUsers()), 2 * TYPING_TIMEOUT);
//    }
//
//    private void setupEventSync(){
//        mChannel.addEventHandler(new ChatChannelEventHandler() {
//            @Override
//            public void onAnyEvent(Event event) {
//                Number watcherCount = event.getWatcherCount();
//                Log.w("onAnyEvent", event.getType().label);
//                if (watcherCount != null) {
//                    Log.w("watcherCount", watcherCount.toString());
//                    mWatcherCount.postValue(watcherCount);
//                }
//            }
//            @Override
//            public void onMessageNew(Event event) {
//                List<Message> list = mMessages.getValue();
//                if (list == null) {
//                    list = new ArrayList<>();
//                }
//                list.add(event.getMessage());
//                mMessages.postValue(list);
//            }
//            @Override
//            public void onTypingStart(Event event) {
//                User user = event.getUser();
//                mTypingState.put(user.getId(), event);
//                mTypingUsers.postValue(typingUsers());
//                scheduleCleanup();
//            }
//            @Override
//            public void onTypingStop(Event event) {
//                User user = event.getUser();
//                mTypingState.remove(user.getId());
//                mTypingUsers.postValue(typingUsers());
//            }
//        });
//    }
//
//    private void init(){
//        initChannelFromOffline();
//        //TODO: subscribe
//    }
//
//    private void initChannelFromOffline(){}
//
//    private void attachToClient(){
//        mChannel.query(new QueryChannelCallback(){
//            @Override
//            public void onSuccess(ChannelState response) {
//                mMessages.setValue(response.getMessages());
//                setupEventSync();
//            }
//            @Override
//            public void onError(String errMsg, int errCode) {
//            }
//        });
//    }
//
//    public LiveData<List<Message>> getMessages() {
//        return mMessages;
//    }
//
//    public synchronized LiveData<List<User>> getTypingUsers() {
//        return mTypingUsers;
//    }
//
//    public synchronized LiveData<Number> getWatcherCount() {
//        return mWatcherCount;
//    }
//
//    class TypingLooperThread extends Thread {
//        Handler mHandler;
//
//        public void run() {
//            Looper.prepare();
//            mHandler = new Handler();
//            Looper.loop();
//        }
//    }
//}