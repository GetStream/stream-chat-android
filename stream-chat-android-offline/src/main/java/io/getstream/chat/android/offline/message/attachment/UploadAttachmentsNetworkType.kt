package io.getstream.chat.android.offline.message.attachment

import androidx.work.NetworkType

/**
 * An enumeration of various network types used as a constraint in [UploadAttachmentsAndroidWorker].
 */
public enum class UploadAttachmentsNetworkType {
    /**
     * Any working network connection is required.
     */
    CONNECTED,

    /**
     * An unmetered network connection is required.
     */
    UNMETERED,

    /**
     * A non-roaming network connection is required.
     */
    NOT_ROAMING,

    /**
     * A metered network connection is required.
     */
    METERED;

    internal fun toNetworkType(): NetworkType = when (this) {
        CONNECTED -> NetworkType.CONNECTED
        UNMETERED -> NetworkType.UNMETERED
        NOT_ROAMING -> NetworkType.NOT_ROAMING
        METERED -> NetworkType.METERED
    }
}
