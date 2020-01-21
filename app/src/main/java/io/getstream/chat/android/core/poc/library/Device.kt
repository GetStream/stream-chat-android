package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class Device {
    @SerializedName("id")
    
    var id: String = ""
    @SerializedName("push_provider")
    
    var push_provider: String = ""

}
