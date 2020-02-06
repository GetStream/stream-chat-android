package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.UndefinedDate
import java.util.*


data class Reaction(val messageId: String){
    lateinit var user: User
    var userID: String = ""
    val type: String = ""
    val createdAt: Date = UndefinedDate
    val extraData: Map<String, Any> = emptyMap()
}
