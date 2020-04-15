package io.getstream.chat.android.livedata.usecase

data class UseCaseHelper(
    var createChannel: CreateChannel,
    var deleteMessage: DeleteMessage,
    var deleteReaction: DeleteReaction,
    var editMessage: EditMessage,
    var keystroke: Keystroke,
    var sendMessage: SendMessage,
    var sendReaction: SendReaction,
    var stopTyping: StopTyping,
    var watchChannel: WatchChannel
)