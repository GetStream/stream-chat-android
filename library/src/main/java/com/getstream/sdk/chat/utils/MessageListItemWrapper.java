package com.getstream.sdk.chat.utils;

import com.getstream.sdk.chat.adapter.MessageListItem;

import java.util.List;

public class MessageListItemWrapper {
    private List<MessageListItem> messageListItemList;
    private Boolean isLoadingMore;
    private Boolean hasNewMessages;
    private boolean isTyping;
    private boolean isThread;

    MessageListItemWrapper(Boolean isLoadingMore, Boolean hasNewMessages, List<MessageListItem> messageListItemList) {
        this.isLoadingMore = isLoadingMore;
        this.hasNewMessages = hasNewMessages;
        this.messageListItemList = messageListItemList;
    }

    public List<MessageListItem> getListEntities() {
        return messageListItemList;
    }

    public void setListEntities(List<MessageListItem> messageListItemList) {
        this.messageListItemList = messageListItemList;
    }

    public Boolean getLoadingMore() {
        return isLoadingMore;
    }

    public void setLoadingMore(Boolean loadingMore) {
        isLoadingMore = loadingMore;
    }

    public Boolean getHasNewMessages() {
        return hasNewMessages;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public boolean isThread() {
        return isThread;
    }

    public void setThread(boolean thread) {
        isThread = thread;
    }
}
