package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.compose.state.messages.list.MessageItemState

/**
 * @return If the current message failed to send.
 */
internal fun MessageItemState.isFailed(): Boolean = isMine && message.syncStatus == SyncStatus.FAILED_PERMANENTLY
