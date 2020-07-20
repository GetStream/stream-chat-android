package io.getstream.chat.android.client.utils

enum class SyncStatus(val status: Int) {
    /** when the entity is new or changed */
    SYNC_NEEDED(-1),
    /** when the entity has been succesfully synced */
    COMPLETED(1),
    /** after the retry strategy we still failed to sync this */
    FAILED_PERMANENTLY(2),
    /** when sync is in progress */
    IN_PROGRESS(3);

    companion object {
        private val map = SyncStatus.values().associateBy(SyncStatus::status)
        fun fromInt(type: Int) = map[type]
    }
}
