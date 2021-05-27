package io.getstream.chat.android.client.utils

public enum class SyncStatus(public val status: Int) {
    /** when the entity is new or changed */
    SYNC_NEEDED(-1),
    /** when the entity has been succesfully synced */
    COMPLETED(1),
    /** after the retry strategy we still failed to sync this */
    FAILED_PERMANENTLY(2),
    /** when sync is in progress */
    IN_PROGRESS(3),
    /** when message waits its' attachments to be sent */
    WAIT_ATTACHMENTS(4);

    public companion object {
        private val map = values().associateBy(SyncStatus::status)
        public fun fromInt(type: Int): SyncStatus? = map[type]
    }
}
