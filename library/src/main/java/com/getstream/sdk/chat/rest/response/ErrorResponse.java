package com.getstream.sdk.chat.rest.response;

import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

import okhttp3.Response;

public class ErrorResponse extends IOException {

    public static final int TOKEN_EXPIRED_CODE = 40;

    @SerializedName("code")
    @Expose
    private int code;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("StatusCode")
    @Expose
    private int StatusCode;

    @SerializedName("duration")
    @Expose
    private String duration;

    public ErrorResponse(String message, int code, String message1, int statusCode, String duration) {
        super(message);
        this.code = code;
        this.message = message1;
        StatusCode = statusCode;
        this.duration = duration;
    }

    public static ErrorResponse parseError(String response) {
        return GsonConverter.Gson().fromJson(response, ErrorResponse.class);
    }

    public static ErrorResponse parseError(Response response) {
        String message;

        if (response.body() == null) {
            return new ErrorResponse("", -1, "", 0,"");
        }

        try {
            // avoid consuming the response body stream (might crash other readers)
            message = response.peekBody(Long.MAX_VALUE).string();
            return parseError(message);
        } catch (Throwable e) {
            return new ErrorResponse("", -1, "", 0,"");
        }
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return StatusCode;
    }

    public String getDuration() {
        return duration;
    }
}
