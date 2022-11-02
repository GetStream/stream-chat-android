package io.getstream.chat.android.state.errorhandler

import io.getstream.chat.android.client.errorhandler.CreateChannelErrorHandler
import io.getstream.chat.android.client.errorhandler.DeleteReactionErrorHandler
import io.getstream.chat.android.client.errorhandler.ErrorHandler
import io.getstream.chat.android.client.errorhandler.QueryMembersErrorHandler
import io.getstream.chat.android.client.errorhandler.SendReactionErrorHandler

internal class StateErrorHandler(
    private val deleteReactionErrorHandler: DeleteReactionErrorHandler,
    private val createChannelErrorHandler: CreateChannelErrorHandler,
    private val queryMembersErrorHandler: QueryMembersErrorHandler,
    private val sendReactionErrorHandler: SendReactionErrorHandler,
) : ErrorHandler,
    DeleteReactionErrorHandler by deleteReactionErrorHandler,
    CreateChannelErrorHandler by createChannelErrorHandler,
    QueryMembersErrorHandler by queryMembersErrorHandler,
    SendReactionErrorHandler by sendReactionErrorHandler

