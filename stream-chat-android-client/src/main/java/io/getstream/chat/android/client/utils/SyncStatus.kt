package io.getstream.chat.android.client.utils

/**
 * If the message has been sent to the servers.
 */
public enum class SyncStatus(public val status: Int) {
    /**
     * When the entity is new or changed.
     */
    SYNC_NEEDED(-1),

    /**
     * When the entity has been successfully synced.
     */
    COMPLETED(1),

    /**
     * After the retry strategy we still failed to sync this.
     */
    FAILED_PERMANENTLY(2),

    /**
     * When sync is in progress.
     */
    IN_PROGRESS(3),

    /**
     * When message waits its' attachments to be sent.
     */
    AWAITING_ATTACHMENTS(4);

    public companion object {
        private val map = values().associateBy(SyncStatus::status)
        public fun fromInt(type: Int): SyncStatus? = map[type]
    }
}
