package com.getstream.sdk.chat.storage;

import androidx.annotation.Nullable;
import io.getstream.chat.android.client.models.User;

public interface InMemoryCache {
    @Nullable
    User getUserById(String userId);
}