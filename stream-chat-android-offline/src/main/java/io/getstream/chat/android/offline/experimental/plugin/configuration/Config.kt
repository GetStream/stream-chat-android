package io.getstream.chat.android.offline.experimental.plugin.configuration

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.utils.DefaultRetryPolicy
import io.getstream.chat.android.offline.utils.RetryPolicy

@InternalStreamChatApi
public data class Config(
    public val backgroundSyncEnabled: Boolean = true,
    public val userPresence: Boolean = true,
    public val persistenceEnabled: Boolean = true,
    public val retryPolicy: RetryPolicy = DefaultRetryPolicy()
)
