package io.getstream.chat.android.core.poc.library.rest

import com.google.gson.annotations.Expose
import io.getstream.chat.android.core.poc.library.Channel


class ChannelResponse {
    
    var duration: String = ""
    
    lateinit var channel: Channel

}
