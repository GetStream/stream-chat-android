package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

/*
 * Created by Anton Bevza on 2019-10-08.
 */
@SuppressWarnings("FieldCanBeLocal")
public class AcceptInviteRequest {

    @SerializedName("accept_invite")
    @Expose
    private boolean acceptInvite = true;

    @SerializedName("message")
    @Nullable
    @Expose
    private AcceptInviteMessage message;

    public AcceptInviteRequest(@Nullable String message) {
        this.message = message != null ? new AcceptInviteMessage(message) : null;
    }

    public class AcceptInviteMessage {

        @Nullable
        @Expose
        @SerializedName("text")
        private String text;

        AcceptInviteMessage(@Nullable String text) {
            this.text = text;
        }

        @Nullable
        public String getText() {
            return text;
        }
    }

    public boolean isAcceptInvite() {
        return acceptInvite;
    }

    @Nullable
    public AcceptInviteMessage getMessage() {
        return message;
    }
}
