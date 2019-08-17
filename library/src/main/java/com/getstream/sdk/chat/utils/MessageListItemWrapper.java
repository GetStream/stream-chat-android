package com.getstream.sdk.chat.utils;

import com.getstream.sdk.chat.adapter.MessageListItem;

import java.util.List;

public class MessageListItemWrapper {
    private List<MessageListItem> listEntities;
    private Boolean isLoadingMore;

    MessageListItemWrapper(Boolean isLoadingMore, List<MessageListItem> listEntities) {
        this.isLoadingMore = isLoadingMore;
        this.listEntities = listEntities;
    }

    public List<MessageListItem> getListEntities() {
        return listEntities;
    }

    public void setListEntities(List<MessageListItem> listEntities) {
        this.listEntities = listEntities;
    }

    public Boolean getLoadingMore() {
        return isLoadingMore;
    }

    public void setLoadingMore(Boolean loadingMore) {
        isLoadingMore = loadingMore;
    }
}
