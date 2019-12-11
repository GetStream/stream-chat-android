package com.getstream.sdk.chat.rest.request;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class GuestUserRequest {

    @NotNull
    @Expose
    @SerializedName("user")
    GuestUserBody user;

    public GuestUserRequest(@NotNull String id, @NotNull String name) {
        this.user = new GuestUserBody(id, name);
    }

    private class GuestUserBody {
        @NotNull
        @Expose
        @SerializedName("name")
        String name;

        @Nullable
        @Expose
        @SerializedName("id")
        String id = "";

        private GuestUserBody(@NotNull String id, @NotNull String name) {
            this.name = name;
            this.id = id;
        }
    }
}
