package com.getstream.sdk.chat.rest.request;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QuerySort;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

/*
 * Created by Anton Bevza on 2019-10-16.
 */

@SuppressWarnings("FieldCanBeLocal")
public class QueryUserRequest {

    @SerializedName("filter_conditions")
    @Expose
    @NotNull
    private FilterObject filter;

    @SerializedName("sort")
    @Expose
    @Nullable
    private QuerySort sort;

    @SerializedName("presence")
    @Expose
    private boolean presence;

    @SerializedName("limit")
    @Expose
    private int limit;

    @SerializedName("offset")
    @Expose
    private int offset;

    /**
     * @param filter filter MongoDB style filter conditions
     * @param sort   sort options
     */
    public QueryUserRequest(@NotNull FilterObject filter, @Nullable QuerySort sort) {
        this.filter = filter;
        this.sort = sort;
    }

    /**
     * Get updates when the user goes offline/online
     */
    public QueryUserRequest withPresence() {
        QueryUserRequest clone = this.cloneOpts();
        clone.presence = true;
        return clone;
    }

    /**
     * Disable updates when the user goes offline/online
     */
    public QueryUserRequest noPresence() {
        QueryUserRequest clone = this.cloneOpts();
        clone.presence = false;
        return clone;
    }

    /**
     * @param limit number of users to return
     */
    public QueryUserRequest withLimit(int limit) {
        QueryUserRequest clone = this.cloneOpts();
        clone.limit = limit;
        return clone;
    }

    /**
     * @param offset Offset for pagination
     */
    public QueryUserRequest withOffset(int offset) {
        QueryUserRequest clone = this.cloneOpts();
        clone.offset = offset;
        return clone;
    }

    private QueryUserRequest cloneOpts() {
        QueryUserRequest clone = new QueryUserRequest(this.filter, this.sort);
        clone.presence = this.presence;
        clone.sort = this.sort;
        clone.filter = this.filter;
        clone.limit = this.limit;
        clone.offset = this.offset;
        return clone;
    }
}
