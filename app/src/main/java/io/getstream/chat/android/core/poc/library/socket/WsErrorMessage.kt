package io.getstream.chat.android.core.poc.library.socket

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class WsErrorMessage {

    @SerializedName("error")
    
    val error: ErrorResponse? = null
}
