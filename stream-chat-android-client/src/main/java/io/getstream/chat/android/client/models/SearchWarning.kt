package io.getstream.chat.android.client.models

public data class SearchWarning(
    /**
     * Channel CIDs for the searched channels
     */
    val channelSearchCids: List<String>,

    /**
     * Number of channels searched
     */
    val channelSearchCount: Int,

    /**
     * Code corresponding to the warning
     */
    val warningCode: Int,

    /**
     * Description of the warning
     */
    val warningDescription: String,
)
