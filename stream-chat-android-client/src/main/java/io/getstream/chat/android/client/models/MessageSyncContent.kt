package io.getstream.chat.android.client.models

public sealed class MessageSyncContent

public sealed class MessageSyncInProgress : MessageSyncContent()
public sealed class MessageSyncFailed : MessageSyncContent()

public object MessageAwaitingAttachments : MessageSyncInProgress() {
    override fun toString(): String = "MessageAwaitingAttachments"
}

public data class MessageModerationFailed(
    val violations: List<Violation>,
) : MessageSyncFailed() {
    public data class Violation(
        val code: Int,
        val messages: List<String>,
    )
}


