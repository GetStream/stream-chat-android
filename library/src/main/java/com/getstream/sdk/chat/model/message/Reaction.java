package com.getstream.sdk.chat.model.message;

import com.getstream.sdk.chat.model.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Reaction {
    @SerializedName("message_id")
    @Expose
    private String message_id;

    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("type")
    @Expose
    private String type;

    public String getMessage_id() {
        return message_id;
    }

    public User getUser() {
        return user;
    }

    public String getType() {
        return type;
    }

    public enum Type {
        like("👍"),
        love("❤"),
        haha("😂"),
        wow("😲"),
        sad("😔"),
        angry("😠");

        private String value;

        Type(final String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }

        @Override
        public String toString() {
            return this.get();
        }
    }
}
