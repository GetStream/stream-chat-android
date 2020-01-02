package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Reaction {
    var messageId: String = ""
    @SerializedName("user")
    @Expose
    lateinit var user: User
    var userID: String = ""
    @SerializedName("type")
    @Expose
    var type: String = ""
    var createdAt: Long = 0

    var extraData: Map<String, Any>? = null

    constructor() {
        syncStatus = Sync.SYNCED
    }

    constructor(
        messageId: String,
        user: User,
        type: String,
        extraData: Map<String, Any>
    ) {
        this.messageId = messageId
        this.user = user
        this.userID = user.id
        this.type = type
        this.extraData = extraData
    }

    @get:Sync.Status
    var syncStatus: Int = Sync.IN_MEMORY

}
