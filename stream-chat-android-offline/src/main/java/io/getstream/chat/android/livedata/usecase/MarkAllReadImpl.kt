package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.offline.usecase.MarkAllRead as OfflineMarkAllRead

public interface MarkAllRead {
    @CheckResult
    public operator fun invoke(): Call<Boolean>
}

internal data class MarkAllReadImpl(private val offlineMarkAllRead: OfflineMarkAllRead) : MarkAllRead {
    override fun invoke(): Call<Boolean> = offlineMarkAllRead.invoke()
}
