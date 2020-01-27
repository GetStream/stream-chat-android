package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.util.*


data class Reaction(val messageId: String){
    lateinit var user: User
    var userID: String = ""
    val type: String = ""
    val createdAt: Date = UndefinedDate
    val extraData: Map<String, Any> = emptyMap()
}
