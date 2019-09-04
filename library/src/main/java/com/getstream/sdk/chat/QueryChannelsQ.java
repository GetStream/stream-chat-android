package com.getstream.sdk.chat;

import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.model.Channel;

import java.util.Date;
import java.util.List;

@Entity(tableName = "stream_queries")
public class QueryChannelsQ {
    // ID generation...
    private String id;

    private FilterObject filter;
    private QuerySort sort;

    private List<Channel> channels;

    @TypeConverters({DateConverter.class})
    private Date createdAt;

    @TypeConverters({DateConverter.class})
    private Date updatedAt;

    public QueryChannelsQ(FilterObject filter, QuerySort sort) {
        this.filter = filter;
        this.sort = sort;
        computeID();
    }

    private void computeID() {
        // TODO: hash the filter and sort
        this.id = "myid";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FilterObject getFilter() {
        return filter;
    }

    public void setFilter(FilterObject filter) {
        this.filter = filter;
        computeID();
    }

    public QuerySort getSort() {
        return sort;
    }

    public void setSort(QuerySort sort) {
        this.sort = sort;
        computeID();
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
