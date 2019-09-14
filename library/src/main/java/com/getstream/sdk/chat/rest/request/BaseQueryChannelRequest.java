package com.getstream.sdk.chat.rest.request;

import com.google.gson.annotations.SerializedName;


public abstract class BaseQueryChannelRequest<T extends BaseQueryChannelRequest<T>> {

    @SerializedName("state")
    protected boolean state;

    @SerializedName("watch")
    protected boolean watch;

    @SerializedName("presence")
    protected boolean presence;

    protected abstract T cloneOpts();

    public T withWatch() {
        BaseQueryChannelRequest clone = this.cloneOpts();
        clone.watch = true;
        return (T) clone;
    }

    public T noWatch() {
        BaseQueryChannelRequest clone = this.cloneOpts();
        clone.watch = false;
        return (T) clone;
    }

    public T withState() {
        BaseQueryChannelRequest clone = this.cloneOpts();
        clone.state = true;
        return (T) clone;
    }

    public T noState() {
        BaseQueryChannelRequest clone = this.cloneOpts();
        clone.state = false;
        return (T) clone;
    }

    public T withPresence() {
        BaseQueryChannelRequest clone = this.cloneOpts();
        return (T) clone;
    }

    public T noPresence() {
        BaseQueryChannelRequest clone = this.cloneOpts();
        clone.presence = false;
        return (T) clone;
    }

}
