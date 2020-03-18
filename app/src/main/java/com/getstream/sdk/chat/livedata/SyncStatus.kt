package com.getstream.sdk.chat.livedata

enum class SyncStatus(val status: Int) {
    /** when the entity is new or changed */
    SYNC_NEEDED(-1),
    /** when the entity has been succesfully synced */
    SYNCED(1),
    /** after the retry strategy we still failed to sync this */
    SYNC_FAILED(2)
}