package io.getstream.chat.android.common.extensions

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message

/**
 * @return If the message type is system.
 */
internal fun Message.isSystem(): Boolean = type == ModelType.message_system

/**
 * @return If the message type is error.
 */
internal fun Message.isError(): Boolean = type == ModelType.message_error