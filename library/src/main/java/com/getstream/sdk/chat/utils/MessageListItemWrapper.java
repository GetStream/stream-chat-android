package com.getstream.sdk.chat.utils;

import com.getstream.sdk.chat.adapter.MessageListItem;

import java.util.List;

public class MessageListItemWrapper {
    private List<MessageListItem> messageListItemList;
    private Boolean isLoadingMore;

    MessageListItemWrapper(Boolean isLoadingMore, List<MessageListItem> messageListItemList) {
        this.isLoadingMore = isLoadingMore;
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
}
