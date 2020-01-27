package io.getstream.chat.android.core.poc.library.requests

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.FilterObject

data class QueryUsers(
    @SerializedName("filter_conditions") @Expose
    private var filter: FilterObject? = null,

    @SerializedName("sort")
    @Expose
    private val sort: QuerySort? = null,

    @SerializedName("presence")
    @Expose
    private var presence: Boolean? = false,

    @SerializedName("limit")
    @Expose
    private var limit: Int? = null,

    @SerializedName("offset")
    @Expose
    private var offset: Int? = null
) {
    /**
     * Get updates when the user goes offline/online
     */
    fun withPresence()= this.copy().apply {
        presence = true
    }

    /**
     * Disable updates when the user goes offline/online
     */
    fun noPresence() = this.copy().apply {
        presence = false
    }

    /**
     * @param limit number of users to return
     */
    fun withLimit(limit: Int)= this.copy().apply {
        this.limit = limit
    }

    /**
     * @param offset Offset for pagination
     */
    fun withOffset(offset: Int)= this.copy().apply {
        this.offset = offset
    }
}