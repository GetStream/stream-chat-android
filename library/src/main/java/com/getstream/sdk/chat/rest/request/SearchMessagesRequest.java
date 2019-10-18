package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.enums.FilterObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

/*
 * Created by Anton Bevza on 2019-10-17.
 */
public class SearchMessagesRequest {
    @SerializedName("filter_conditions")
    @Expose
    @NotNull
    private FilterObject filter;

    @SerializedName("query")
    @Expose
    @NotNull
    private String query;

    @SerializedName("limit")
    @Expose
    private int limit;

    @SerializedName("offset")
    @Expose
    private int offset;

    /**
     * @param filter MongoDB style filter conditions
     * @param query  search keyword
     */
    public SearchMessagesRequest(@NotNull FilterObject filter, @NotNull String query) {
        this.filter = filter;
        this.query = query;
    }

    /**
     * @param limit number of users to return
     */
    public SearchMessagesRequest withLimit(int limit) {
        SearchMessagesRequest clone = this.cloneOpts();
        clone.limit = limit;
        return clone;
    }

    /**
     * @param offset Offset for pagination
     */
    public SearchMessagesRequest withOffset(int offset) {
        SearchMessagesRequest clone = this.cloneOpts();
        clone.offset = offset;
        return clone;
    }

    private SearchMessagesRequest cloneOpts() {
        SearchMessagesRequest clone = new SearchMessagesRequest(this.filter, this.query);
        clone.filter = this.filter;
        clone.limit = this.limit;
        clone.offset = this.offset;
        clone.query = this.query;
        return clone;
    }
}
