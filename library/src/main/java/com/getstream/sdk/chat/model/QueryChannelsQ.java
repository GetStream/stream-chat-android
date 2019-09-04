package com.getstream.sdk.chat.model;

import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.getstream.sdk.chat.storage.converter.DateConverter;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.storage.QueryChannelsQDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "stream_queries")
public class QueryChannelsQ {
    // ID generation...
    private String id;

    private FilterObject filter;
    private QuerySort sort;
    private List<String> channelIDs;

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

    public List<ChannelState> getChannelStates(QueryChannelsQDao queryChannelsQDao, Integer limit) {
        List<Channel> channels = getChannels(queryChannelsQDao, limit);
        List<ChannelState> channelStates = new ArrayList<>();
        for (Channel c: channels) {
            channelStates.add(c.getLastState());
        }
        return channelStates;
    }

    public List<Channel> getChannels(QueryChannelsQDao queryChannelsQDao, Integer limit) {
        List<String> selectedChannelIDs = this.getChannelIDs();
        // TODO: slice
        List<Channel> channels = queryChannelsQDao.getChannels(selectedChannelIDs);
        Map<String, Channel> channelMap = new HashMap<String, Channel>();
        for (Channel c : channels) {
            channelMap.put(c.getCid(), c);
        }
        // restore the original sort
        List<Channel> selectedChannels = new ArrayList<>();
        for (String cid: selectedChannelIDs) {
            selectedChannels.add(channelMap.get(cid));
        }

        return selectedChannels;

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

    public List<String> getChannelIDs() {
        return channelIDs;
    }

    public void setChannelIDs(List<String> channelIDs) {
        this.channelIDs = channelIDs;
    }
}
