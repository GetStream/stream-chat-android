package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class UploadFileResponse {
    @SerializedName("file")
    @Expose
    var fileUrl: String = ""

}
