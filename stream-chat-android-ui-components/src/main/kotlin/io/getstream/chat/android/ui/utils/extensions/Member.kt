package io.getstream.chat.android.ui.utils.extensions

import io.getstream.chat.android.client.models.Member

public val Member.isOwnerOrAdmin: Boolean
    get() = role == "owner" || role == "admin"
