package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.Expose
import io.getstream.chat.android.core.poc.library.Channel


class ChannelResponse {
    @Expose
    var duration: String = ""
    @Expose
    lateinit var channel: Channel

}
