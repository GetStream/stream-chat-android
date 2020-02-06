package io.getstream.chat.android.client.models


class Flag {
    lateinit var user: User
    lateinit var target_user: User
    val target_message_id: String = ""
    val created_at: String = ""
    val updated_at: String = ""
    val reviewed_at: String = ""
    val reviewed_by: String = ""
    val approved_at: String = ""
    val rejected_at: String = ""
    val created_by_automod = false
}
