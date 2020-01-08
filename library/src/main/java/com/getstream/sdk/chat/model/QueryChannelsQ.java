package com.getstream.sdk.chat.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.storage.converter.ChannelIdListConverter;
import com.getstream.sdk.chat.storage.converter.DateConverter;
import com.getstream.sdk.chat.storage.converter.FilterObjectConverter;
import com.getstream.sdk.chat.storage.converter.QuerySortConverter;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "stream_query")
public class QueryChannelsQ {
    @Ignore
    final String TAG = QueryChannelsQ.class.getSimpleName();

    // ID generation...
    @PrimaryKey
    @NotNull
    private String id;


    @TypeConverters({FilterObjectConverter.class})
    private FilterObject filter;
    @TypeConverters({QuerySortConverter.class})
    private QuerySort sort;
    @TypeConverters({ChannelIdListConverter.class})

    private List<String> channelCIDs;

    @TypeConverters({DateConverter.class})
    private Date createdAt;

    @TypeConverters({DateConverter.class})
    private Date updatedAt;

    public QueryChannelsQ(@NonNull FilterObject filter, @NonNull QuerySort sort) {
        this.filter = filter;
        this.sort = sort;
        computeID();
    }

    private void computeID() {
        Map<String, Object> data = new HashMap<>();
        data.put("sort", this.getSort().getData());
        data.put("filter", this.getFilter().getData());
        Gson gson = GsonConverter.Gson();
        String json = gson.toJson(data);

        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(json.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            this.id = hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            StreamChat.getLogger().logT(this, e);
            this.id = "errorCreatingQueryID";
        }


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

    public List<String> getChannelCIDs() {
        return channelCIDs;
    }

    public void setChannelCIDs(List<String> channelCIDs) {
        this.channelCIDs = channelCIDs;
    }
}
