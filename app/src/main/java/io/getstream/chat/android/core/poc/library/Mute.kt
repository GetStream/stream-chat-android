package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class Mute {
    @SerializedName("user")
    @Expose
    var user: User? = null
    @SerializedName("target")
    @Expose
    var target: User? = null
    @SerializedName("created_at")
    @Expose
    var created_at: String = ""
    @SerializedName("updated_at")
    @Expose
    var updated_at: String = ""

}
