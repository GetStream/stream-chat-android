package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/*
 * Created by Anton Bevza on 2019-10-08.
 */
@SuppressWarnings("FieldCanBeLocal")
public class RejectInviteRequest {

    @SerializedName("reject_invite")
    @Expose
    private boolean rejectInvite = true;

    public boolean isRejectInvite() {
        return rejectInvite;
    }
}
