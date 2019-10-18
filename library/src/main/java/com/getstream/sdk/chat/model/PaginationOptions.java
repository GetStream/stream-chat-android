package com.getstream.sdk.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/*
 * Created by Anton Bevza on 2019-10-16.
 */
public class PaginationOptions {
    @SerializedName("limit")
    @Expose
    private int limit;

    @SerializedName("offset")
    @Expose
    private int offset;

    private PaginationOptions(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public static class Builder {
        private int limit;
        private int offset;

        /**
         * @param limit number of objects to return
         */
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * @param offset offset for pagination
         */
        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public PaginationOptions build() {
            return new PaginationOptions(limit, offset);
        }
    }

}
