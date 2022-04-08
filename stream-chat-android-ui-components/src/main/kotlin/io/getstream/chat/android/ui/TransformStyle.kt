/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui

import io.getstream.chat.android.ui.avatar.AvatarStyle
import io.getstream.chat.android.ui.channel.list.ChannelActionsDialogViewStyle
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.mention.list.MentionListViewStyle
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.GiphyViewHolderStyle
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import io.getstream.chat.android.ui.message.list.MessageReplyStyle
import io.getstream.chat.android.ui.message.list.ScrollButtonViewStyle
import io.getstream.chat.android.ui.message.list.adapter.view.ImageAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderViewStyle
import io.getstream.chat.android.ui.message.list.reactions.edit.EditReactionsViewStyle
import io.getstream.chat.android.ui.message.list.reactions.view.ViewReactionsViewStyle
import io.getstream.chat.android.ui.pinned.list.PinnedMessageListViewStyle
import io.getstream.chat.android.ui.search.SearchInputViewStyle
import io.getstream.chat.android.ui.search.list.SearchResultListViewStyle
import io.getstream.chat.android.ui.suggestion.list.SuggestionListViewStyle
import io.getstream.chat.android.ui.typing.TypingIndicatorViewStyle

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
    public var imageAttachmentStyleTransformer: StyleTransformer<ImageAttachmentViewStyle> = noopTransformer()
    public var messageReplyStyleTransformer: StyleTransformer<MessageReplyStyle> = noopTransformer()
    public var fileAttachmentStyleTransformer: StyleTransformer<FileAttachmentViewStyle> = noopTransformer()
    public var suggestionListStyleTransformer: StyleTransformer<SuggestionListViewStyle> = noopTransformer()
    public var messageListHeaderStyleTransformer: StyleTransformer<MessageListHeaderViewStyle> = noopTransformer()
    public var mentionListViewStyleTransformer: StyleTransformer<MentionListViewStyle> = noopTransformer()
    public var searchInputViewStyleTransformer: StyleTransformer<SearchInputViewStyle> = noopTransformer()
    public var searchResultListViewStyleTransformer: StyleTransformer<SearchResultListViewStyle> = noopTransformer()
    public var typingIndicatorViewStyleTransformer: StyleTransformer<TypingIndicatorViewStyle> = noopTransformer()
    public var pinnedMessageListViewStyleTransformer: StyleTransformer<PinnedMessageListViewStyle> = noopTransformer()

    private fun <T> noopTransformer() = StyleTransformer<T> { it }
}

public fun interface StyleTransformer<T> {
    public fun transform(source: T): T
}
