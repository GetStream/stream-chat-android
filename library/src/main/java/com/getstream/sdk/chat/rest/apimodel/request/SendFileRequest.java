package com.getstream.sdk.chat.rest.apimodel.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.Map;

public class SendFileRequest {
    @SerializedName("file")
    @Expose
    File file;

    @SerializedName("type")
    @Expose
    String type;

    public SendFileRequest(File file){
        this.file = file;
    }
}
