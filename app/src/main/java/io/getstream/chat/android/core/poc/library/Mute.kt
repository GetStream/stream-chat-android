package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class Mute {
    @SerializedName("user")
    
    var user: User? = null

    @SerializedName("target")
    
    var target: User? = null

    @SerializedName("created_at")
    
    var created_at: String = ""

    @SerializedName("updated_at")
    
    var updated_at: String = ""

}
