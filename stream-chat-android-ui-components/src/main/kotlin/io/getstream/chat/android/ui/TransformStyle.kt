package io.getstream.chat.android.ui

import io.getstream.chat.android.ui.avatar.AvatarStyle
import io.getstream.chat.android.ui.channel.list.ChannelActionsDialogViewStyle
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.list.FileAttachmentsViewStyle
import io.getstream.chat.android.ui.message.list.GiphyViewHolderStyle
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import io.getstream.chat.android.ui.message.list.MessageReplyStyle
import io.getstream.chat.android.ui.message.list.ScrollButtonViewStyle
import io.getstream.chat.android.ui.message.list.adapter.view.MediaAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderViewStyle
import io.getstream.chat.android.ui.message.list.reactions.edit.EditReactionsViewStyle
import io.getstream.chat.android.ui.message.list.reactions.view.ViewReactionsViewStyle
import io.getstream.chat.android.ui.search.SearchInputViewStyle
import io.getstream.chat.android.ui.suggestion.list.SuggestionListViewStyle

public object TransformStyle {
    public var avatarStyleTransformer: StyleTransformer<AvatarStyle> = noopTransformer()
    public var channelListStyleTransformer: StyleTransformer<ChannelListViewStyle> = noopTransformer()
    public var messageListStyleTransformer: StyleTransformer<MessageListViewStyle> = noopTransformer()
    public var messageListItemStyleTransformer: StyleTransformer<MessageListItemStyle> = noopTransformer()
    public var messageInputStyleTransformer: StyleTransformer<MessageInputViewStyle> = noopTransformer()
    public var scrollButtonStyleTransformer: StyleTransformer<ScrollButtonViewStyle> = noopTransformer()
    public var viewReactionsStyleTransformer: StyleTransformer<ViewReactionsViewStyle> = noopTransformer()
    public var editReactionsStyleTransformer: StyleTransformer<EditReactionsViewStyle> = noopTransformer()
    public var channelActionsDialogStyleTransformer: StyleTransformer<ChannelActionsDialogViewStyle> = noopTransformer()
    public var giphyViewHolderStyleTransformer: StyleTransformer<GiphyViewHolderStyle> = noopTransformer()
    public var mediaAttachmentStyleTransformer: StyleTransformer<MediaAttachmentViewStyle> = noopTransformer()
    public var messageReplyStyleTransformer: StyleTransformer<MessageReplyStyle> = noopTransformer()
    public var fileAttachmentStyleTransformer: StyleTransformer<FileAttachmentsViewStyle> = noopTransformer()
    public var suggestionListStyleTransformer: StyleTransformer<SuggestionListViewStyle> = noopTransformer()
    public var messageListHeaderStyleTransformer: StyleTransformer<MessageListHeaderViewStyle> = noopTransformer()
    public var searchInputViewStyleTransformer: StyleTransformer<SearchInputViewStyle> = noopTransformer()

    private fun <T> noopTransformer() = StyleTransformer<T> { it }
}

public fun interface StyleTransformer<T> {
    public fun transform(source: T): T
}
