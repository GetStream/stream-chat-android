package io.getstream.chat.android.core.poc.library

import androidx.annotation.IntDef


class Sync(@Status status: Int) {
    @IntDef(
        IN_MEMORY,
        LOCAL_ONLY,
        LOCAL_UPDATE_PENDING,
        SYNCED,
        LOCAL_FAILED
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Status

    companion object {
        const val IN_MEMORY = -1
        const val LOCAL_ONLY = 0
        const val LOCAL_UPDATE_PENDING = 1
        const val SYNCED = 2
        const val LOCAL_FAILED = 3
    }

    init {
        println("status :$status")
    }
}