package com.getstream.sdk.chat.utils;

import com.getstream.sdk.chat.adapter.Entity;

import java.util.List;

public class EntityListWrapper {
    private List<Entity> listEntities;
    private Boolean isLoadingMore;

    EntityListWrapper(Boolean isLoadingMore, List<Entity> listEntities) {
        this.isLoadingMore = isLoadingMore;
        this.listEntities = listEntities;
    }

    public List<Entity> getListEntities() {
        return listEntities;
    }

    public void setListEntities(List<Entity> listEntities) {
        this.listEntities = listEntities;
    }

    public Boolean getLoadingMore() {
        return isLoadingMore;
    }

    public void setLoadingMore(Boolean loadingMore) {
        isLoadingMore = loadingMore;
    }
}
