package io.getstream.chat.android.ui.utils.extensions

import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.common.messagelist.MessageListController

/**
 * Converts common [MessageListController.ErrorEvent] to ui-components [MessageListViewModel.ErrorEvent].
 *
 * @return Ui-components [MessageListViewModel.ErrorEvent] derived from common [MessageListController.ErrorEvent].
 */
internal fun MessageListController.ErrorEvent.toUiErrorEvent(): MessageListViewModel.ErrorEvent {
    return when (this) {
        is MessageListController.ErrorEvent.BlockUserError ->
            MessageListViewModel.ErrorEvent.BlockUserError(chatError)
        is MessageListController.ErrorEvent.FlagMessageError ->
            MessageListViewModel.ErrorEvent.FlagMessageError(chatError)
        is MessageListController.ErrorEvent.MuteUserError ->
            MessageListViewModel.ErrorEvent.MuteUserError(chatError)
        is MessageListController.ErrorEvent.PinMessageError ->
            MessageListViewModel.ErrorEvent.PinMessageError(chatError)
        is MessageListController.ErrorEvent.UnmuteUserError ->
            MessageListViewModel.ErrorEvent.UnmuteUserError(chatError)
        is MessageListController.ErrorEvent.UnpinMessageError ->
            MessageListViewModel.ErrorEvent.UnpinMessageError(chatError)
    }
}

internal fun MessageListViewModel.ErrorEvent.toControllerErrorEvent(): MessageListController.ErrorEvent {
    return when (this) {
        is MessageListViewModel.ErrorEvent.BlockUserError ->
            MessageListController.ErrorEvent.BlockUserError(chatError)
        is MessageListViewModel.ErrorEvent.FlagMessageError ->
            MessageListController.ErrorEvent.FlagMessageError(chatError)
        is MessageListViewModel.ErrorEvent.MuteUserError ->
            MessageListController.ErrorEvent.MuteUserError(chatError)
        is MessageListViewModel.ErrorEvent.PinMessageError ->
            MessageListController.ErrorEvent.PinMessageError(chatError)
        is MessageListViewModel.ErrorEvent.UnmuteUserError ->
            MessageListController.ErrorEvent.UnmuteUserError(chatError)
        is MessageListViewModel.ErrorEvent.UnpinMessageError ->
            MessageListController.ErrorEvent.UnpinMessageError(chatError)
    }
}