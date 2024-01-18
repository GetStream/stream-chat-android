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

package io.getstream.chat.android.ui.feature.messages.list

import android.animation.LayoutTransition
import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.client.utils.message.isModerationError
import io.getstream.chat.android.client.utils.message.isThreadReply
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Flag
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.downloadAttachment
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.state.messages.Copy
import io.getstream.chat.android.ui.common.state.messages.CustomAction
import io.getstream.chat.android.ui.common.state.messages.Delete
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MarkAsUnread
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.Pin
import io.getstream.chat.android.ui.common.state.messages.React
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.Resend
import io.getstream.chat.android.ui.common.state.messages.ThreadReply
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.ModeratedMessageOption
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.databinding.StreamUiMessageListViewBinding
import io.getstream.chat.android.ui.feature.gallery.AttachmentGalleryActivity
import io.getstream.chat.android.ui.feature.gallery.AttachmentGalleryDestination
import io.getstream.chat.android.ui.feature.gallery.AttachmentGalleryItem
import io.getstream.chat.android.ui.feature.gallery.toAttachment
import io.getstream.chat.android.ui.feature.messages.dialog.ModeratedMessageDialogFragment
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.AttachmentClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.AttachmentDownloadHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.BottomEndRegionReachedHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ConfirmDeleteMessageHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ConfirmFlagMessageHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.CustomActionHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.EndRegionReachedHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.EnterThreadListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ErrorEventHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.FlagMessageResultHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.GiphySendHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.GiphySendListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.LastMessageReadHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.LinkClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageDeleteHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageEditHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageFlagHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageListItemTransformer
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageLongClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessagePinHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageReactionHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageReplyHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageRetryHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageRetryListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageUnpinHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ModeratedMessageLongClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ModeratedMessageOptionHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ReplyMessageClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ThreadClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ThreadStartHandler
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.UserClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.UserReactionClickListener
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListenerContainerImpl
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.MessageListItemAdapter
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.background.MessageBackgroundFactory
import io.getstream.chat.android.ui.feature.messages.list.background.MessageBackgroundFactoryImpl
import io.getstream.chat.android.ui.feature.messages.list.internal.HiddenMessageListItemPredicate
import io.getstream.chat.android.ui.feature.messages.list.internal.MessageListScrollHelper
import io.getstream.chat.android.ui.feature.messages.list.options.message.MessageOptionItem
import io.getstream.chat.android.ui.feature.messages.list.options.message.MessageOptionItemsFactory
import io.getstream.chat.android.ui.feature.messages.list.options.message.MessageOptionsDialogFragment
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.model.MessageListItemWrapper
import io.getstream.chat.android.ui.navigation.destinations.AttachmentDestination
import io.getstream.chat.android.ui.navigation.destinations.WebLinkDestination
import io.getstream.chat.android.ui.utils.ListenerDelegate
import io.getstream.chat.android.ui.utils.extensions.activity
import io.getstream.chat.android.ui.utils.extensions.copyToClipboard
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import io.getstream.chat.android.ui.utils.extensions.getTranslatedText
import io.getstream.chat.android.ui.utils.extensions.isCurrentUser
import io.getstream.chat.android.ui.utils.extensions.isGiphyNotEphemeral
import io.getstream.chat.android.ui.utils.extensions.showToast
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.extensions.use
import io.getstream.chat.android.uiutils.extension.hasLink
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import io.getstream.chat.android.ui.common.state.messages.Flag as FlagAction

/**
 * MessageListView renders a list of messages and extends the [RecyclerView]
 * The most common customizations are
 * - Disabling Reactions
 * - Disabling Threads
 * - Customizing the click and longCLick (via the adapter)
 * - The list_item_message template to use (perhaps, multiple ones...?)
 */
public class MessageListView : ConstraintLayout {

    private companion object {
        private const val LOAD_MORE_THRESHOLD = 10
    }

    private val logger by taggedLogger("Chat:MessageListView")

    private var messageListViewStyle: MessageListViewStyle? = null

    private lateinit var binding: StreamUiMessageListViewBinding

    private lateinit var adapter: MessageListItemAdapter
    private lateinit var loadingView: View
    private lateinit var loadingViewContainer: ViewGroup
    private lateinit var emptyStateView: View
    private lateinit var emptyStateViewContainer: ViewGroup
    private lateinit var scrollHelper: MessageListScrollHelper

    /**
     * Used to enable or disable parts of the UI depending
     * on which abilities the user has in the given channel.
     */
    private var ownCapabilities: Set<String> = setOf()

    private val defaultChildLayoutParams by lazy {
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER,
        )
    }

    private var endRegionReachedHandler = EndRegionReachedHandler {
        throw IllegalStateException("endRegionReachedHandler must be set.")
    }
    private var bottomEndRegionReachedHandler = BottomEndRegionReachedHandler {
        throw IllegalStateException("bottomEndRegionReachedHandler must be set.")
    }
    private var lastMessageReadHandler = LastMessageReadHandler {
        throw IllegalStateException("lastMessageReadHandler must be set.")
    }
    private var messageEditHandler = MessageEditHandler {
        throw IllegalStateException("onMessageEditHandler must be set.")
    }
    private var messageDeleteHandler = MessageDeleteHandler {
        throw IllegalStateException("onMessageDeleteHandler must be set.")
    }
    private var threadStartHandler = ThreadStartHandler {
        throw IllegalStateException("onStartThreadHandler must be set.")
    }
    private var replyMessageClickListener = ReplyMessageClickListener {
        // no-op
    }
    private var messageFlagHandler = MessageFlagHandler {
        throw IllegalStateException("onMessageFlagHandler must be set.")
    }
    private var flagMessageResultHandler = FlagMessageResultHandler {
        // no-op
    }
    private var messagePinHandler = MessagePinHandler {
        throw IllegalStateException("onMessagePinHandler must be set.")
    }
    private var messageMarkAsUnreadHandler = MessageMarkAsUnreadHandler {
        throw IllegalStateException("onMessageMarkAsUnreadHandler must be set.")
    }
    private var messageUnpinHandler = MessageUnpinHandler {
        throw IllegalStateException("onMessageUnpinHandler must be set.")
    }
    private var giphySendHandler = GiphySendHandler {
        throw IllegalStateException("onSendGiphyHandler must be set.")
    }
    private var messageRetryHandler = MessageRetryHandler {
        throw IllegalStateException("onMessageRetryHandler must be set.")
    }
    private var messageReactionHandler = MessageReactionHandler { _, _ ->
        throw IllegalStateException("onMessageReactionHandler must be set.")
    }
    private var customActionHandler = CustomActionHandler { _, _ ->
        throw IllegalStateException("onCustomActionHandler must be set.")
    }
    private var messageReplyHandler = MessageReplyHandler { _, _ ->
        throw IllegalStateException("onReplyMessageHandler must be set")
    }
    private var attachmentDownloadHandler = AttachmentDownloadHandler {
        throw IllegalStateException("onAttachmentDownloadHandler must be set")
    }

    private var confirmDeleteMessageHandler = ConfirmDeleteMessageHandler { _, confirmCallback ->
        AlertDialog.Builder(context)
            .setTitle(R.string.stream_ui_message_list_delete_confirmation_title)
            .setMessage(R.string.stream_ui_message_list_delete_confirmation_message)
            .setPositiveButton(R.string.stream_ui_message_list_delete_confirmation_positive_button) { dialog, _ ->
                dialog.dismiss()
                confirmCallback()
            }
            .setNegativeButton(R.string.stream_ui_message_list_delete_confirmation_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private var confirmFlagMessageHandler = ConfirmFlagMessageHandler { _, confirmCallback ->
        AlertDialog.Builder(context)
            .setTitle(R.string.stream_ui_message_list_flag_confirmation_title)
            .setMessage(R.string.stream_ui_message_list_flag_confirmation_message)
            .setPositiveButton(R.string.stream_ui_message_list_flag_confirmation_positive_button) { dialog, _ ->
                dialog.dismiss()
                confirmCallback()
            }
            .setNegativeButton(R.string.stream_ui_message_list_flag_confirmation_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private var _attachmentReplyOptionHandler by ListenerDelegate(
        initialValue = AttachmentGalleryActivity.AttachmentReplyOptionHandler {
            throw IllegalStateException("onAttachmentReplyOptionClickHandler must be set")
        },
    ) { realListener ->
        AttachmentGalleryActivity.AttachmentReplyOptionHandler { realListener().onClick(it) }
    }

    private var _attachmentShowInChatOptionClickHandler by ListenerDelegate(
        initialValue = AttachmentGalleryActivity.AttachmentShowInChatOptionHandler {
            throw IllegalStateException("onAttachmentShowInChatOptionClickHandler must be set")
        },
    ) { realListener ->
        AttachmentGalleryActivity.AttachmentShowInChatOptionHandler { realListener().onClick(it) }
    }

    private var _attachmentDownloadOptionHandler by ListenerDelegate(
        initialValue = AttachmentGalleryActivity.AttachmentDownloadOptionHandler { attachmentData ->
            defaultAttachmentDownloadClickListener.onAttachmentDownloadClick(attachmentData.toAttachment())
        },
    ) { realListener ->
        AttachmentGalleryActivity.AttachmentDownloadOptionHandler { realListener().onClick(it) }
    }

    private var _attachmentDeleteOptionHandler by ListenerDelegate(
        initialValue = AttachmentGalleryActivity.AttachmentDeleteOptionHandler {
            throw IllegalStateException("onAttachmentDeleteOptionClickHandler must be set")
        },
    ) { realListener ->
        AttachmentGalleryActivity.AttachmentDeleteOptionHandler { attachmentData ->
            realListener().onClick(attachmentData)
        }
    }

    private var errorEventHandler = ErrorEventHandler { error ->
        when (error) {
            is MessageListController.ErrorEvent.MuteUserError -> R.string.stream_ui_message_list_error_mute_user
            is MessageListController.ErrorEvent.UnmuteUserError -> R.string.stream_ui_message_list_error_unmute_user
            is MessageListController.ErrorEvent.BlockUserError -> R.string.stream_ui_message_list_error_block_user
            is MessageListController.ErrorEvent.FlagMessageError -> R.string.stream_ui_message_list_error_flag_message
            is MessageListController.ErrorEvent.PinMessageError -> R.string.stream_ui_message_list_error_pin_message
            is MessageListController.ErrorEvent.UnpinMessageError -> R.string.stream_ui_message_list_error_unpin_message
            is MessageListController.ErrorEvent.MarkUnreadError ->
                R.string.stream_ui_message_list_error_mark_as_unread_message
        }.let(::showToast)
    }

    private var moderatedMessageOptionHandler = ModeratedMessageOptionHandler { _, _ ->
        throw IllegalStateException("onModeratedMessageOptionSelected must be set")
    }

    private var messageListItemPredicate: MessageListItemPredicate = HiddenMessageListItemPredicate
    private var messageListItemTransformer: MessageListItemTransformer = MessageListItemTransformer { it }
    private var showAvatarPredicate: ShowAvatarPredicate = DefaultShowAvatarPredicate()

    private var deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE

    private lateinit var loadMoreListener: EndlessMessageListScrollListener

    private lateinit var channel: Channel

    /**
     * If you are allowed to scroll up or not.
     */
    private var lockScrollUp = true

    private val defaultMessageClickListener =
        MessageClickListener { message ->
            val replyTo = message.replyTo

            when {
                message.replyCount > 0 -> {
                    threadStartHandler.onStartThread(message)
                }

                replyTo?.id != null -> {
                    replyMessageClickListener.onReplyClick(replyTo)
                }
            }
        }

    /**
     * Provides a default long click handler for all non moderated messages. Based on the configuration options we have
     * and the message type, we show different kind of options.
     *
     * We also disable editing of certain messages, like Giphy messages.
     */
    private val defaultMessageLongClickListener =
        MessageLongClickListener { message ->
            context.getFragmentManager()?.let { fragmentManager ->
                val currentUserId = ChatClient.instance().getCurrentUser()?.id
                if (message.isModerationError(currentUserId)) {
                    moderatedMessageLongClickListener?.onModeratedMessageLongClick(message)
                } else {
                    val style = requireStyle()
                    val isEditEnabled = style.editMessageEnabled && !message.isGiphyNotEphemeral()
                    val isThreadEnabled = style.threadsEnabled && channel.config.isThreadEnabled
                    val viewStyle = style.copy(
                        editMessageEnabled = isEditEnabled,
                        threadsEnabled = isThreadEnabled,
                    )

                    val messageOptionItems = messageOptionItemsFactory.createMessageOptionItems(
                        selectedMessage = message,
                        currentUser = ChatUI.currentUserProvider.getCurrentUser(),
                        isInThread = adapter.isThread || message.isThreadReply(),
                        ownCapabilities = ownCapabilities,
                        style = viewStyle,
                    )

                    showMessageOptionsDialog(fragmentManager, message, messageOptionItems)
                }
            }
        }

    /**
     * Show message options dialog for the given set of message options.
     *
     * @param fragmentManager The FragmentManager this dialog fragment will be added to.
     * @param message The selected message.
     * @param messageOptionItems The list of message options to display.
     * @param reactionClickListener The callback to be invoked on reaction item click.
     * @param optionClickListener The callback to be invoked on option item click.
     */
    @ExperimentalStreamChatApi
    public fun showMessageOptionsDialog(
        fragmentManager: FragmentManager,
        message: Message,
        messageOptionItems: List<MessageOptionItem>,
        reactionClickListener: (Message, String) -> Unit = { message: Message, reactionType: String ->
            messageReactionHandler.onMessageReaction(message, reactionType)
        },
        optionClickListener: (MessageAction) -> Unit = { messageAction: MessageAction ->
            handleMessageAction(messageAction)
        },
    ) {
        MessageOptionsDialogFragment.newInstance(
            context = context,
            optionsDialogType = MessageOptionsDialogFragment.OptionsDialogType.MESSAGE_OPTIONS,
            message = message,
            style = requireStyle(),
            messageListItemViewHolderFactory = messageListItemViewHolderFactory,
            messageBackgroundFactory = messageBackgroundFactory,
            attachmentFactoryManager = attachmentFactoryManager,
            showAvatarPredicate = showAvatarPredicate,
            messageOptionItems = messageOptionItems,
        )
            .apply {
                setReactionClickListener { message, reactionType ->
                    reactionClickListener(message, reactionType)
                }
                setMessageOptionClickListener { messageAction ->
                    optionClickListener(messageAction)
                }
            }
            .show(fragmentManager, MessageOptionsDialogFragment.TAG)
    }

    /**
     * Provides long click listener for moderated messages. By default opens the [ModeratedMessageDialogFragment].
     */
    private var moderatedMessageLongClickListener: ModeratedMessageLongClickListener? =
        ModeratedMessageLongClickListener { message ->
            showModeratedMessageDialog(message)
        }

    private val defaultMessageRetryListener =
        MessageRetryListener { message ->
            messageRetryHandler.onMessageRetry(message)
        }

    private val defaultThreadClickListener =
        ThreadClickListener { message ->
            if (message.replyCount > 0) {
                threadStartHandler.onStartThread(message)
            }
        }

    private val attachmentGalleryDestination =
        AttachmentGalleryDestination(
            context,
            _attachmentReplyOptionHandler,
            _attachmentShowInChatOptionClickHandler,
            _attachmentDownloadOptionHandler,
            _attachmentDeleteOptionHandler,
        )

    private val languageCache = hashMapOf<String, String>()

    private val getLanguageDisplayName: (String) -> String = { code ->
        languageCache.getOrPut(code) {
            Locale(code).getDisplayName(Locale.getDefault())
        }
    }

    /**
     * Handles attachment clicks which by default open the attachment preview.
     *
     * Can be customized by [setAttachmentClickListener].
     *
     * In case the attachments are being uploaded, they cannot be opened for preview until all of the attachments within
     * a message are uploaded.
     */
    private val defaultAttachmentClickListener =
        AttachmentClickListener { message, attachment ->
            val hasInvalidAttachments = message.attachments.any { it.uploadState != null }
            if (hasInvalidAttachments) {
                return@AttachmentClickListener
            }

            if (attachment.isGiphy()) {
                val url = attachment.imagePreviewUrl ?: attachment.titleLink ?: attachment.ogUrl

                if (url != null) {
                    ChatUI.navigator.navigate(WebLinkDestination(context, url))
                }
            } else {
                val destination = when {
                    message.attachments.all { it.isImage() || it.isVideo() } -> {
                        val filteredAttachments = message.attachments
                            .filter {
                                (
                                    it.isImage() && !it.imagePreviewUrl.isNullOrEmpty() ||
                                        it.isVideo() && !it.assetUrl.isNullOrEmpty()
                                    ) &&
                                    !it.hasLink()
                            }
                        val attachmentGalleryItems = filteredAttachments.map {
                            AttachmentGalleryItem(
                                attachment = it,
                                user = message.user,
                                createdAt = message.getCreatedAtOrThrow(),
                                messageId = message.id,
                                cid = message.cid,
                                isMine = message.user.isCurrentUser(),
                            )
                        }
                        val attachmentIndex = filteredAttachments.indexOf(attachment)

                        attachmentGalleryDestination.setData(attachmentGalleryItems, attachmentIndex)
                        attachmentGalleryDestination
                    }
                    else -> AttachmentDestination(message, attachment, context)
                }

                ChatUI.navigator.navigate(destination)
            }
        }

    private val defaultAttachmentDownloadClickListener =
        AttachmentDownloadClickListener { attachment ->
            attachmentDownloadHandler.onAttachmentDownload {
                Toast.makeText(
                    context,
                    context.getString(R.string.stream_ui_message_list_download_started),
                    Toast.LENGTH_SHORT,
                ).show()
                ChatClient.instance().downloadAttachment(context, attachment)
            }
        }
    private val defaultReactionViewClickListener =
        ReactionViewClickListener { message: Message ->
            context.getFragmentManager()?.let {
                MessageOptionsDialogFragment.newInstance(
                    context = context,
                    optionsDialogType = MessageOptionsDialogFragment.OptionsDialogType.REACTION_OPTIONS,
                    message = message,
                    style = requireStyle(),
                    messageListItemViewHolderFactory = messageListItemViewHolderFactory,
                    messageBackgroundFactory = messageBackgroundFactory,
                    attachmentFactoryManager = attachmentFactoryManager,
                    showAvatarPredicate = showAvatarPredicate,
                    messageOptionItems = emptyList(),
                ).apply {
                    setReactionClickListener { message, reactionType ->
                        messageReactionHandler.onMessageReaction(message, reactionType)
                    }
                    setUserReactionClickListener { message, user, reaction ->
                        userReactionClickListener.onUserReactionClick(message, user, reaction)
                    }
                }
                    .show(it, MessageOptionsDialogFragment.TAG)
            }
        }
    private val defaultUserClickListener = UserClickListener { /* Empty */ }
    private val defaultGiphySendListener =
        GiphySendListener { action ->
            giphySendHandler.onSendGiphy(action)
        }
    private val defaultLinkClickListener = LinkClickListener { url ->
        ChatUI.navigator.navigate(WebLinkDestination(context, url))
    }
    private val defaultEnterThreadListener = EnterThreadListener {
        // Empty
    }
    private val defaultUserReactionClickListener = UserReactionClickListener { _, _, _ ->
        // Empty
    }

    private val listenerContainer = MessageListListenerContainerImpl(
        messageClickListener = defaultMessageClickListener,
        messageLongClickListener = defaultMessageLongClickListener,
        messageRetryListener = defaultMessageRetryListener,
        threadClickListener = defaultThreadClickListener,
        attachmentClickListener = defaultAttachmentClickListener,
        attachmentDownloadClickListener = defaultAttachmentDownloadClickListener,
        reactionViewClickListener = defaultReactionViewClickListener,
        userClickListener = defaultUserClickListener,
        giphySendListener = defaultGiphySendListener,
        linkClickListener = defaultLinkClickListener,
    )
    private var enterThreadListener = defaultEnterThreadListener
    private var userReactionClickListener = defaultUserReactionClickListener

    private lateinit var messageListItemViewHolderFactory: MessageListItemViewHolderFactory
    private lateinit var messageDateFormatter: DateFormatter
    private lateinit var attachmentFactoryManager: AttachmentFactoryManager
    private lateinit var messageBackgroundFactory: MessageBackgroundFactory
    private lateinit var messageOptionItemsFactory: MessageOptionItemsFactory

    public constructor(context: Context) : this(context, null, 0)
    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(attrs)
    }

    private fun init(attr: AttributeSet?) {
        binding = StreamUiMessageListViewBinding.inflate(streamThemeInflater, this)

        messageListViewStyle = MessageListViewStyle(context, attr)
        messageListViewStyle?.messagesStart?.let(::chatMessageStart)

        initRecyclerView()
        initScrollHelper()
        initLoadingView()
        initEmptyStateView()

        configureAttributes(attr)

        binding.defaultEmptyStateView.setTextStyle(requireStyle().emptyViewTextStyle)

        layoutTransition = LayoutTransition()
    }

    private fun initLoadingView() {
        loadingViewContainer = binding.loadingViewContainer

        loadingViewContainer.removeView(binding.defaultLoadingView)
        messageListViewStyle?.loadingView?.let { loadingView ->
            this.loadingView = streamThemeInflater.inflate(loadingView, loadingViewContainer, false).apply {
                isVisible = true
                loadingViewContainer.addView(this)
            }
        }
    }

    private fun initEmptyStateView() {
        emptyStateView = binding.defaultEmptyStateView
        emptyStateViewContainer = binding.emptyStateViewContainer
    }

    private fun initRecyclerView() {
        binding.chatMessagesRV.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            setHasFixedSize(false)
            setItemViewCacheSize(20)
        }
    }

    private fun initScrollHelper() {
        scrollHelper = MessageListScrollHelper(
            recyclerView = binding.chatMessagesRV,
            scrollButtonView = binding.scrollToBottomButton,
            disableScrollWhenShowingDialog = messageListViewStyle?.disableScrollWhenShowingDialog ?: true,
        ) {
            lastMessageReadHandler.onLastMessageRead()
        }
    }

    private fun configureAttributes(attributeSet: AttributeSet?) {
        context.obtainStyledAttributes(
            attributeSet,
            R.styleable.MessageListView,
            R.attr.streamUiMessageListStyle,
            R.style.StreamUi_MessageList,
        ).use { tArray ->
            tArray.getInteger(
                R.styleable.MessageListView_streamUiLoadMoreThreshold,
                LOAD_MORE_THRESHOLD,
            ).also { loadMoreThreshold ->
                val loadMoreAtTop = {
                    endRegionReachedHandler.onEndRegionReached()
                }
                val loadMoreAtBottom = {
                    val last = adapter.currentList
                        .asSequence()
                        .filterIsInstance<MessageListItem.MessageItem>()
                        .lastOrNull()
                        ?.message

                    logger.i { "[loadMoreAtBottom] last.id: ${last?.id}, last.text: ${last?.text}" }
                    bottomEndRegionReachedHandler.onBottomEndRegionReached(last?.id)
                }

                loadMoreListener = EndlessMessageListScrollListener(loadMoreThreshold, loadMoreAtTop, loadMoreAtBottom)
            }

            val style = requireStyle()
            binding.scrollToBottomButton.setScrollButtonViewStyle(style.scrollButtonViewStyle)
            (binding.scrollToBottomButton.layoutParams as? LayoutParams)?.let { layoutParams ->
                layoutParams.bottomMargin = style.scrollButtonBottomMargin
                layoutParams.marginEnd = style.scrollButtonEndMargin
            }
            scrollHelper.scrollToBottomButtonEnabled = style.scrollButtonViewStyle.scrollButtonEnabled
            scrollHelper.alwaysScrollToBottom = style.scrollButtonBehaviour == NewMessagesBehaviour.SCROLL_TO_BOTTOM
        }

        if (background == null) {
            setBackgroundColor(requireStyle().backgroundColor)
        }
    }

    /**
     * Setter method for own capabilities which dictate which
     * parts of the UI are enabled or disabled for the current user
     * in the given channel.
     */
    public fun setOwnCapabilities(ownCapabilities: Set<String>) {
        this.ownCapabilities = ownCapabilities
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activity?.activityResultRegistry?.let { registry ->
            attachmentGalleryDestination.register(registry)
        }
    }

    override fun onDetachedFromWindow() {
        if (isAdapterInitialized()) {
            adapter.onDetachedFromRecyclerView(binding.chatMessagesRV)
        }
        attachmentGalleryDestination.unregister()
        super.onDetachedFromWindow()
    }

    /**
     * Returns the inner [RecyclerView] that is used to display a list of message list items.
     *
     * @return The inner [RecyclerView] with messages.
     */
    public fun getRecyclerView(): RecyclerView {
        return binding.chatMessagesRV
    }

    /**
     * Used to indicate that the message list is loading more messages.
     *
     * @param loadingMore True if the list the next page of messages is loading.
     */
    public fun setLoadingMore(loadingMore: Boolean) {
        if (loadingMore) {
            loadMoreListener.disablePagination()
        } else {
            loadMoreListener.enablePagination()
        }
    }

    /**
     * Scrolls the list to the target message and highlights it. Only works if the target message
     * is already present in the list.
     *
     * @param message The message to scroll to and highlight.
     */
    public fun scrollToMessage(message: Message) {
        scrollHelper.scrollToMessage(message)
    }

    private fun setMessageListItemAdapter(adapter: MessageListItemAdapter) {
        binding.chatMessagesRV.addOnScrollListener(loadMoreListener)
        /*
         * Lock for 500 milliseconds setMessageListScrollUp in here.
         * Because when keyboard shows up, MessageList is scrolled up and it triggers hiding keyboard.
         */
        addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                lockScrollUp = true
                postDelayed({ lockScrollUp = false }, 500)
            }
        }

        binding.chatMessagesRV.adapter = adapter
    }

    /**
     * Initializes the message list view with the [Channel] object.
     *
     * @param channel The channel object.
     */
    public fun init(channel: Channel) {
        this.channel = channel
        initAdapter()
    }

    private fun initAdapter() {
        if (::adapter.isInitialized) {
            logger.v { "[initAdapter] rejected (already initialized)" }
            return
        }
        val style = requireStyle()
        // Create default DateFormatter if needed
        if (::messageDateFormatter.isInitialized.not()) {
            messageDateFormatter = ChatUI.dateFormatter
        }

        if (::attachmentFactoryManager.isInitialized.not()) {
            attachmentFactoryManager = ChatUI.attachmentFactoryManager
        }

        // Create default ViewHolderFactory if needed
        if (::messageListItemViewHolderFactory.isInitialized.not()) {
            messageListItemViewHolderFactory = MessageListItemViewHolderFactory()
        }

        if (::messageBackgroundFactory.isInitialized.not()) {
            messageBackgroundFactory = MessageBackgroundFactoryImpl(style.itemStyle)
        }

        if (::messageOptionItemsFactory.isInitialized.not()) {
            messageOptionItemsFactory = MessageOptionItemsFactory.defaultFactory(context)
        }

        messageListItemViewHolderFactory.decoratorProvider = ChatUI.decoratorProviderFactory.createDecoratorProvider(
            channel = channel,
            dateFormatter = messageDateFormatter,
            messageListViewStyle = style,
            showAvatarPredicate = this.showAvatarPredicate,
            messageBackgroundFactory = messageBackgroundFactory,
            deletedMessageVisibility = { deletedMessageVisibility },
            getLanguageDisplayName = getLanguageDisplayName,
        )

        messageListItemViewHolderFactory.setListenerContainer(this.listenerContainer)
        messageListItemViewHolderFactory.setAttachmentFactoryManager(this.attachmentFactoryManager)
        messageListItemViewHolderFactory.setMessageListItemStyle(style.itemStyle)
        messageListItemViewHolderFactory.setGiphyViewHolderStyle(style.giphyViewHolderStyle)
        messageListItemViewHolderFactory.setAudioRecordViewStyle(style.audioRecordPlayerViewStyle)
        messageListItemViewHolderFactory.setReplyMessageListItemViewStyle(style.replyMessageStyle)

        adapter = MessageListItemAdapter(messageListItemViewHolderFactory).also {
            setMessageListItemAdapter(it)
        }
    }

    /**
     * Scrolls the message list to the bottom.sl
     */
    public fun scrollToBottom() {
        scrollHelper.scrollToBottom()
    }

    /**
     * Set a custom layout manager for MessageListView. This can be used to change orientation of messages.
     *
     * @param layoutManager
     */
    public fun setCustomLinearLayoutManager(layoutManager: LinearLayoutManager) {
        binding.chatMessagesRV.layoutManager = layoutManager
    }

    /**
     * Set a custom item animator for MessageListView.
     * That will handle animations involving changes to the items in this MessageListView.
     *
     * @param itemAnimator The ItemAnimator being set. If null, no animations will occur when changes occur
     * to the items in this MessageListView.
     */
    public fun setCustomItemAnimator(itemAnimator: ItemAnimator?) {
        binding.chatMessagesRV.itemAnimator = itemAnimator
    }

    /**
     * Sets the view to be displayed when the message list is loading.
     *
     * @param view Will be added to the view hierarchy of [MessageListView] and managed by it.
     * The view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams Defines how the view will be situated inside its container [ViewGroup].
     */
    @JvmOverloads
    public fun setLoadingView(view: View, layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams) {
        loadingViewContainer.removeView(loadingView)
        loadingView = view
        loadingViewContainer.addView(loadingView, layoutParams)
    }

    /**
     * Shows the loading view.
     */
    public fun showLoadingView() {
        loadingViewContainer.isVisible = true
    }

    /**
     * Hides the loading view.
     */
    public fun hideLoadingView() {
        loadingViewContainer.isVisible = false
    }

    /**
     * Enables fetch for messages at the bottom.
     */
    public fun shouldRequestMessagesAtBottom(shouldRequest: Boolean) {
        loadMoreListener.fetchAtBottom(shouldRequest)
        scrollHelper.unreadCountEnabled = !shouldRequest
    }

    /**
     * Sets the view to be displayed when the message list is empty.
     *
     * @param view Will be added to the view hierarchy of [MessageListView] and managed by it.
     * The view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams Defines how the view will be situated inside its container [ViewGroup].
     */
    @JvmOverloads
    public fun setEmptyStateView(view: View, layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams) {
        emptyStateViewContainer.removeView(emptyStateView)
        emptyStateView = view
        emptyStateViewContainer.addView(emptyStateView, layoutParams)
    }

    /**
     * Shows the empty view.
     */
    public fun showEmptyStateView() {
        emptyStateViewContainer.isVisible = true
    }

    /**
     * Hides the empty view.
     */
    public fun hideEmptyStateView() {
        emptyStateViewContainer.isVisible = false
    }

    /**
     * Shows a error for one of the reasons defined in [MessageListController.ErrorEvent].
     *
     * @param errorEvent The error event containing information about the error.
     */
    public fun showError(errorEvent: MessageListController.ErrorEvent) {
        errorEventHandler.onErrorEvent(errorEvent)
    }

    /**
     * Used to control whether the message list is scrolled to the bottom when new messages arrive
     * or the unread count badge is incremented instead.
     *
     * @param newMessagesBehaviour The behavior to be used when new messages are added to the list.
     */
    public fun setNewMessagesBehaviour(newMessagesBehaviour: NewMessagesBehaviour) {
        scrollHelper.alwaysScrollToBottom = newMessagesBehaviour == NewMessagesBehaviour.SCROLL_TO_BOTTOM
    }

    /**
     * Enables or disables the scroll to bottom button.
     *
     * @param scrollToBottomButtonEnabled True if scroll to bottom button should be displayed.
     */
    public fun setScrollToBottomButtonEnabled(scrollToBottomButtonEnabled: Boolean) {
        scrollHelper.scrollToBottomButtonEnabled = scrollToBottomButtonEnabled
    }

    /**
     * Enables or disables the message editing feature.
     *
     * @param enabled True if editing a message is enabled, false otherwise.
     */
    public fun setEditMessageEnabled(enabled: Boolean) {
        messageListViewStyle = requireStyle().copy(editMessageEnabled = enabled)
    }

    /**
     * Enables or disables the message deleting feature.
     *
     * @param enabled True if deleting a message is enabled, false otherwise.
     */
    public fun setDeleteMessageEnabled(enabled: Boolean) {
        messageListViewStyle = requireStyle().copy(deleteMessageEnabled = enabled)
    }

    /**
     * Enables or disables the message delete confirmation showing.
     *
     * @param enabled True if deleting a message is enabled, false otherwise.
     */
    public fun setDeleteMessageConfirmationEnabled(enabled: Boolean) {
        messageListViewStyle = requireStyle().copy(deleteConfirmationEnabled = enabled)
    }

    /**
     * Enables or disables the message copy feature.
     *
     * @param enabled True if copying a message is enabled, false otherwise.
     */
    public fun setCopyMessageEnabled(enabled: Boolean) {
        messageListViewStyle = requireStyle().copy(copyTextEnabled = enabled)
    }

    /**
     * Enables or disables the message flagging feature.
     *
     * @param enabled True if user muting is enabled, false otherwise.
     */
    public fun setMessageFlagEnabled(enabled: Boolean) {
        messageListViewStyle = requireStyle().copy(flagEnabled = enabled)
    }

    /**
     * Enables or disables the message reactions feature.
     *
     * @param enabled True if user muting is enabled, false otherwise.
     */
    public fun setReactionsEnabled(enabled: Boolean) {
        messageListViewStyle = requireStyle().copy(reactionsEnabled = enabled)
    }

    /**
     * Enables or disables the message threading feature.
     *
     * @param enabled True if user muting is enabled, false otherwise.
     */
    public fun setThreadsEnabled(enabled: Boolean) {
        messageListViewStyle = requireStyle().copy(threadsEnabled = enabled)
    }

    /**
     * Enables or disables the message threading feature.
     *
     * @param enabled True if user muting is enabled, false otherwise.
     */
    public fun setRepliesEnabled(enabled: Boolean) {
        messageListViewStyle = requireStyle().copy(replyEnabled = enabled)
    }

    /**
     * Allows clients to set a custom implementation of [MessageListItemViewHolderFactory]. Use this
     * method if you want completely custom views for the message list items.
     *
     * @param messageListItemViewHolderFactory The custom factory to be used when generating item ViewHolders.
     */
    public fun setMessageViewHolderFactory(messageListItemViewHolderFactory: MessageListItemViewHolderFactory) {
        check(isAdapterInitialized().not()) {
            "Adapter was already initialized, please set MessageViewHolderFactory first"
        }
        this.messageListItemViewHolderFactory = messageListItemViewHolderFactory
    }

    /**
     * Allows clients to set a custom implementation of [MessageBackgroundFactory]. Use this
     * method if you want to change the background of messages
     *
     * @param messageBackgroundFactory The custom factory that provides drawables to be used in the messages background
     */
    public fun setMessageBackgroundFactory(messageBackgroundFactory: MessageBackgroundFactory) {
        check(isAdapterInitialized().not()) {
            "Adapter was already initialized, please set MessageBackgroundFactory first"
        }
        this.messageBackgroundFactory = messageBackgroundFactory
    }

    /**
     * Allows clients to set a custom implementation of [MessageOptionItemsFactory]. Use this
     * method if you want to change the message options on the message options overlay.
     *
     * @param messageOptionItemsFactory The custom factory that provides option items for the message options overlay.
     */
    public fun setMessageOptionItemsFactory(messageOptionItemsFactory: MessageOptionItemsFactory) {
        check(isAdapterInitialized().not()) {
            "Adapter was already initialized, please set MessageOptionItemsFactory first"
        }
        this.messageOptionItemsFactory = messageOptionItemsFactory
    }

    /**
     * Allows clients to set a custom implementation of [DateFormatter] to format the message date.
     *
     * @param messageDateFormatter The formatter that is used to format the message date.
     */
    public fun setMessageDateFormatter(messageDateFormatter: DateFormatter) {
        check(isAdapterInitialized().not()) { "Adapter was already initialized; please set DateFormatter first" }
        this.messageDateFormatter = messageDateFormatter
    }

    /**
     * Used to control the visibility of the user avatar for a particular message list item.
     *
     * @param showAvatarPredicate The predicate that checks if the avatar should be shown.
     */
    public fun setShowAvatarPredicate(showAvatarPredicate: ShowAvatarPredicate) {
        check(isAdapterInitialized().not()) {
            "Adapter was already initialized; please set ShowAvatarPredicate first"
        }
        this.showAvatarPredicate = showAvatarPredicate
    }

    /**
     * Shows the message list items.
     *
     * @param messageListItemWrapper The object containing all the information required to render
     * the message list.
     */
    public fun displayNewMessages(messageListItemWrapper: MessageListItemWrapper) {
        handleNewWrapper(messageListItemWrapper)
    }

    /**
     * Allows applying a filter condition to the message list before it is rendered.
     *
     * @param messageListItemPredicate The predicate used to filter the list of [MessageListItem].
     */
    public fun setMessageListItemPredicate(messageListItemPredicate: MessageListItemPredicate) {
        check(isAdapterInitialized().not()) {
            "Adapter was already initialized, please set MessageListItemPredicate first"
        }
        this.messageListItemPredicate = messageListItemPredicate
    }

    /**
     * Allows to transform the message list data before it is rendered on the screen.
     *
     * @param messageListItemTransformer The transformer used to modify the message list item.
     *
     */
    public fun setMessageItemTransformer(messageListItemTransformer: MessageListItemTransformer) {
        this.messageListItemTransformer = messageListItemTransformer
    }

    /**
     * Sets the messages unread count to the scroll to bottom button.
     *
     * @param unreadCount The count of unread [Message]s for the thread/channel.
     */
    internal fun setUnreadCount(unreadCount: Int) {
        binding.scrollToBottomButton.setUnreadCount(unreadCount)
    }

    /**
     * Allows clients to set an instance of [AttachmentFactoryManager] that holds
     * a list of custom attachment factories. Use this method to create a custom
     * content view for the message attachments.
     *
     * @param attachmentFactoryManager Hold the list of factories for custom attachments.
     */
    public fun setAttachmentFactoryManager(attachmentFactoryManager: AttachmentFactoryManager) {
        check(isAdapterInitialized().not()) {
            "Adapter was already initialized, please set attachment factories first"
        }
        this.attachmentFactoryManager = attachmentFactoryManager
    }

    public fun handleFlagMessageResult(result: Result<Flag>) {
        flagMessageResultHandler.onHandleResult(result)
    }

    /**
     * Returns an instance of [MessageListViewStyle] associated with this instance of [MessageListView].
     * Be sure invoke this method after this view laid out on layout and already initialized, otherwise you'll get an
     * exception.
     *
     * @return The instance of [MessageListViewStyle] associated with this [MessageListView].
     */
    public fun requireStyle(): MessageListViewStyle {
        return checkNotNull(messageListViewStyle) {
            "View must be initialized first to obtain style!"
        }
    }

    private fun handleNewWrapper(listItem: MessageListItemWrapper) {
        CoroutineScope(DispatcherProvider.IO).launch {
            val filteredList = listItem.items
                .filter(messageListItemPredicate::predicate)
                .let(messageListItemTransformer::transform)

            withContext(DispatcherProvider.Main) {
                val isThreadStart = !adapter.isThread && listItem.isThread ||
                    (listItem.isThread && listItem.items.size > 1 && adapter.itemCount <= 1)
                val isNormalModeStart = adapter.isThread && !listItem.isThread
                val isOldListEmpty = adapter.currentList.isEmpty()
                if (isThreadStart) {
                    listItem.items
                        .asSequence()
                        .filterIsInstance(MessageListItem.MessageItem::class.java)
                        .firstOrNull { it.message.parentId == null }
                        ?.let { enterThreadListener.onThreadEntered(it.message) }
                }
                adapter.isThread = listItem.isThread

                if (isThreadStart) {
                    messageListViewStyle?.threadMessagesStart?.let(::chatMessageStart)
                } else if (isNormalModeStart) {
                    messageListViewStyle?.messagesStart?.let(::chatMessageStart)
                }

                logger.v { "[handleNewWrapper] filteredList.size: ${filteredList.size}" }
                adapter.submitList(filteredList) {
                    scrollHelper.onMessageListChanged(
                        isThreadStart = isThreadStart,
                        hasNewMessages = listItem.hasNewMessages,
                        isInitialList = isOldListEmpty && filteredList.isNotEmpty(),
                        areNewestMessagesLoaded = listItem.areNewestMessagesLoaded,
                    )
                }
            }
        }
    }

    private fun chatMessageStart(messageStartValue: Int) {
        messageStartValue
            .let(MessagesStart::parseValue)
            .let(::changeLayoutForMessageStart)
    }

    private fun changeLayoutForMessageStart(messagesStart: MessagesStart) {
        val messagesRV = binding.chatMessagesRV

        when (messagesStart) {
            MessagesStart.BOTTOM -> {
                messagesRV.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                messagesRV.overScrollMode = View.OVER_SCROLL_NEVER
            }

            MessagesStart.TOP -> {
                messagesRV.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                messagesRV.overScrollMode = View.OVER_SCROLL_ALWAYS
            }
        }
    }

    /**
     * @return if the adapter has been initialized or not.
     */
    public fun isAdapterInitialized(): Boolean {
        return ::adapter.isInitialized
    }

    //region Listener setters
    /**
     * Sets the message click listener to be used by MessageListView.
     *
     * @param messageClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setMessageClickListener(messageClickListener: MessageClickListener?) {
        listenerContainer.messageClickListener =
            messageClickListener ?: defaultMessageClickListener
    }

    /**
     * Sets the message long click listener to be used by MessageListView.
     *
     * @param messageLongClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setMessageLongClickListener(messageLongClickListener: MessageLongClickListener?) {
        listenerContainer.messageLongClickListener =
            messageLongClickListener ?: defaultMessageLongClickListener
    }

    /**
     * Sets the moderated message long click listener to be used by MessageListView.
     *
     * @param moderatedMessageLongClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setModeratedMessageLongClickListener(
        moderatedMessageLongClickListener: ModeratedMessageLongClickListener?,
    ) {
        this.moderatedMessageLongClickListener = moderatedMessageLongClickListener
    }

    /**
     * Sets the message retry listener to be used by MessageListView.
     *
     * @param messageRetryListener The listener to use. If null, the default will be used instead.
     */
    public fun setMessageRetryListener(messageRetryListener: MessageRetryListener?) {
        listenerContainer.messageRetryListener =
            messageRetryListener ?: defaultMessageRetryListener
    }

    /**
     * Sets the thread click listener to be used by MessageListView.
     *
     * @param threadClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setThreadClickListener(threadClickListener: ThreadClickListener?) {
        listenerContainer.threadClickListener =
            threadClickListener ?: defaultThreadClickListener
    }

    /**
     * Sets the attachment click listener to be used by MessageListView.
     *
     * @param attachmentClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setAttachmentClickListener(attachmentClickListener: AttachmentClickListener?) {
        listenerContainer.attachmentClickListener =
            attachmentClickListener ?: defaultAttachmentClickListener
    }

    /**
     * Sets the attachment download click listener to be used by MessageListView.
     *
     * @param attachmentDownloadClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setAttachmentDownloadClickListener(attachmentDownloadClickListener: AttachmentDownloadClickListener?) {
        listenerContainer.attachmentDownloadClickListener =
            attachmentDownloadClickListener ?: defaultAttachmentDownloadClickListener
    }

    /**
     * Sets the reaction view click listener to be used by MessageListView.
     *
     * @param reactionViewClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setReactionViewClickListener(reactionViewClickListener: ReactionViewClickListener?) {
        listenerContainer.reactionViewClickListener =
            reactionViewClickListener ?: defaultReactionViewClickListener
    }

    /**
     * Sets the user click listener to be used by MessageListView.
     *
     * @param userClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setUserClickListener(userClickListener: UserClickListener?) {
        listenerContainer.userClickListener = userClickListener ?: defaultUserClickListener
    }

    /**
     * Sets the link click listener to be used by MessageListView.
     *
     * @param linkClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setLinkClickListener(linkClickListener: LinkClickListener?) {
        listenerContainer.linkClickListener = linkClickListener ?: defaultLinkClickListener
    }

    /**
     * Sets the thread click listener to be used by MessageListView.
     *
     * @param enterThreadListener The listener to use. If null, the default will be used instead.
     */
    public fun setEnterThreadListener(enterThreadListener: EnterThreadListener?) {
        this.enterThreadListener = enterThreadListener ?: defaultEnterThreadListener
    }

    /**
     * Sets the click listener to be used when a reaction left by a user is clicked on the message options overlay.
     *
     * @param userReactionClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setUserReactionClickListener(userReactionClickListener: UserReactionClickListener?) {
        this.userReactionClickListener = userReactionClickListener ?: defaultUserReactionClickListener
    }

    /** Sets the click listener to be used when a message that is a reply is clicked.
     *
     * @param replyMessageClickListener The listener to use. If null, no behaviour is added.
     */
    public fun setReplyMessageClickListener(replyMessageClickListener: ReplyMessageClickListener) {
        this.replyMessageClickListener = replyMessageClickListener
    }
    //endregion

    //region Handler setters

    /**
     * Sets the handler used when the end region is reached.
     *
     * @param endRegionReachedHandler The handler to use.
     */
    public fun setEndRegionReachedHandler(endRegionReachedHandler: EndRegionReachedHandler) {
        this.endRegionReachedHandler = endRegionReachedHandler
    }

    /**
     * Sets the handler used when the bottom end region is reached. This runs whe list of messages in this
     * view becomes non linear and it will be called until it becomes linear again.
     *
     * @param bottomEndRegionReachedHandler The handler to use.
     */
    public fun setBottomEndRegionReachedHandler(bottomEndRegionReachedHandler: BottomEndRegionReachedHandler) {
        this.bottomEndRegionReachedHandler = bottomEndRegionReachedHandler
    }

    public fun interface BottomEndRegionReachedHandler {
        public fun onBottomEndRegionReached(messageId: String?)
    }

    /**
     * Sets the handler used when the last message is read.
     *
     * @param lastMessageReadHandler The handler to use.
     */
    public fun setLastMessageReadHandler(lastMessageReadHandler: LastMessageReadHandler) {
        this.lastMessageReadHandler = lastMessageReadHandler
    }

    /**
     * Sets the handler used to let the message input know when we are editing a message.
     *
     * @param messageEditHandler The handler to use.
     */
    public fun setMessageEditHandler(messageEditHandler: MessageEditHandler) {
        this.messageEditHandler = messageEditHandler
    }

    /**
     * Sets the handler used when the the message is going to be deleted.
     *
     * @param messageDeleteHandler The handler to use.
     */
    public fun setMessageDeleteHandler(messageDeleteHandler: MessageDeleteHandler) {
        this.messageDeleteHandler = messageDeleteHandler
    }

    /**
     * Sets the handler used when a new thread for the message is started.
     *
     * @param threadStartHandler The handler to use.
     */
    public fun setThreadStartHandler(threadStartHandler: ThreadStartHandler) {
        this.threadStartHandler = threadStartHandler
    }

    /**
     * Sets the handler used when the message is going to be flagged.
     *
     * @param messageFlagHandler The handler to use.
     */
    public fun setMessageFlagHandler(messageFlagHandler: MessageFlagHandler) {
        this.messageFlagHandler = messageFlagHandler
    }

    /**
     * Sets the handler used to handle flag message result.
     *
     * @param flagMessageResultHandler The handler to use.
     */
    public fun setFlagMessageResultHandler(flagMessageResultHandler: FlagMessageResultHandler) {
        this.flagMessageResultHandler = flagMessageResultHandler
    }

    /**
     * Sets the handler used to handle when the message is going to be pinned.
     *
     * @param messagePinHandler The handler to use.
     */
    public fun setMessagePinHandler(messagePinHandler: MessagePinHandler) {
        this.messagePinHandler = messagePinHandler
    }

    /**
     * Sets the handler used to handle when the message is going to be marked as read.
     *
     * @param MessageMarkAsUnreadHandler The handler to use.
     */
    public fun setMessageMarkAsUnreadHandler(messageMarkAsUnreadHandler: MessageMarkAsUnreadHandler) {
        this.messageMarkAsUnreadHandler = messageMarkAsUnreadHandler
    }

    /**
     * Sets the handler used to handle when the message is going to be unpinned.
     *
     * @param messageUnpinHandler The handler to use.
     */
    public fun setMessageUnpinHandler(messageUnpinHandler: MessageUnpinHandler) {
        this.messageUnpinHandler = messageUnpinHandler
    }

    /**
     * Sets the handler used when giphy action is going to be performed.
     *
     * @param giphySendHandler The handler to use.
     */
    public fun setGiphySendHandler(giphySendHandler: GiphySendHandler) {
        this.giphySendHandler = giphySendHandler
    }

    /**
     * Sets the handler used when the failed message is going to be retried.
     *
     * @param messageRetryHandler The handler to use.
     */
    public fun setMessageRetryHandler(messageRetryHandler: MessageRetryHandler) {
        this.messageRetryHandler = messageRetryHandler
    }

    /**
     * Sets the handler used when a reaction for the message is going to be send.
     *
     * @param messageReactionHandler The handler to use.
     */
    public fun setMessageReactionHandler(messageReactionHandler: MessageReactionHandler) {
        this.messageReactionHandler = messageReactionHandler
    }

    /**
     * Set the handler used when the custom action is going to be executed.
     *
     * @param customActionHandler The handler to use.
     */
    public fun setCustomActionHandler(customActionHandler: CustomActionHandler) {
        this.customActionHandler = customActionHandler
    }

    /**
     * Sets the handler used when the message is going to be replied in the channel.
     *
     * @param messageReplyHandler The handler to use.
     */
    public fun setMessageReplyHandler(messageReplyHandler: MessageReplyHandler) {
        this.messageReplyHandler = messageReplyHandler
    }

    /**
     * Sets the handler used when the attachment is going to be downloaded.
     *
     * @param attachmentDownloadHandler The handler to use.
     */
    public fun setAttachmentDownloadHandler(attachmentDownloadHandler: AttachmentDownloadHandler) {
        this.attachmentDownloadHandler = attachmentDownloadHandler
    }

    /**
     * Sets the handler used to confirm that the message is going to be deleted.
     *
     * @param confirmDeleteMessageHandler The handler to use.
     */
    public fun setConfirmDeleteMessageHandler(confirmDeleteMessageHandler: ConfirmDeleteMessageHandler) {
        this.confirmDeleteMessageHandler = confirmDeleteMessageHandler
    }

    /**
     * Sets the handler used to confirm that the message is going to be flagged.
     *
     * @param confirmFlagMessageHandler The handler to use.
     */
    public fun setConfirmFlagMessageHandler(confirmFlagMessageHandler: ConfirmFlagMessageHandler) {
        this.confirmFlagMessageHandler = confirmFlagMessageHandler
    }

    /**
     * Sets the handler used when replying to an attachment from the gallery screen.
     *
     * @param handler The handler to use.
     */
    public fun setAttachmentReplyOptionClickHandler(handler: AttachmentGalleryActivity.AttachmentReplyOptionHandler) {
        this._attachmentReplyOptionHandler = handler
    }

    /**
     * Sets the handler used when navigating to a message from the gallery screen.
     *
     * @param handler The handler to use.
     */
    public fun setAttachmentShowInChatOptionClickHandler(
        handler: AttachmentGalleryActivity.AttachmentShowInChatOptionHandler,
    ) {
        this._attachmentShowInChatOptionClickHandler = handler
    }

    /**
     * Sets the handler used when downloading an attachment from the gallery screen.
     *
     * @param handler The handler to use.
     */
    public fun setDownloadOptionHandler(handler: AttachmentGalleryActivity.AttachmentDownloadOptionHandler) {
        this._attachmentDownloadOptionHandler = handler
    }

    /**
     * Sets the handler used when deleting an attachment from the gallery screen.
     *
     * @param handler The handler to use.
     */
    public fun setAttachmentDeleteOptionClickHandler(handler: AttachmentGalleryActivity.AttachmentDeleteOptionHandler) {
        this._attachmentDeleteOptionHandler = handler
    }

    /**
     * Sets the handler used when handling the errors defined in [MessageListController.ErrorEvent].
     *
     * @param handler The handler to use.
     */
    public fun setErrorEventHandler(handler: ErrorEventHandler) {
        this.errorEventHandler = handler
    }

    /**
     * Sets the value used to filter deleted messages.
     * @see DeletedMessageVisibility
     *
     * @param deletedMessageVisibility Changes the visibility of deleted messages.
     */
    public fun setDeletedMessageVisibility(deletedMessageVisibility: DeletedMessageVisibility) {
        this.deletedMessageVisibility = deletedMessageVisibility
        if (isAdapterInitialized()) {
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
        }
    }

    /**
     * Sets the handler used when the user interacts with [ModeratedMessageDialogFragment].
     *
     * @param handler The handler to use.
     */
    public fun setModeratedMessageHandler(handler: ModeratedMessageOptionHandler) {
        this.moderatedMessageOptionHandler = handler
    }

    /**
     * Sets the handler used when the user interacts with [ScrollButtonView].
     *
     * @param handler The handler to use.
     */
    public fun setOnScrollToBottomHandler(handler: OnScrollToBottomHandler) {
        this.scrollHelper.setScrollToBottomHandler(handler)
    }

    /**
     * Used to display the moderated message dialog when you long click on a message that has failed the moderation
     * check.
     *
     * Used by the default moderated message long click listener as well as the general default message long click
     * listener which internally calls the moderated message long click listener if the message has failed the
     * moderation check.
     * @see moderatedMessageLongClickListener
     * @see defaultMessageLongClickListener
     *
     * @param message The message that has failed moderation, used to show the moderation dialog.
     */
    public fun showModeratedMessageDialog(message: Message) {
        context.getFragmentManager()?.let { fragmentManager ->
            ModeratedMessageDialogFragment.newInstance(message).apply {
                setDialogSelectionHandler(object : ModeratedMessageDialogFragment.DialogSelectionHandler {
                    override fun onModeratedOptionSelected(message: Message, action: ModeratedMessageOption) {
                        moderatedMessageOptionHandler.onModeratedMessageOptionSelected(message, action)
                    }
                })
            }.show(fragmentManager, ModeratedMessageDialogFragment.TAG)
        }
    }

    /**
     * Handles the selected [messageAction].
     *
     * @param messageAction The newly selected action.
     */
    private fun handleMessageAction(messageAction: MessageAction) {
        val message = messageAction.message
        val style = requireStyle()

        when (messageAction) {
            is Resend -> messageRetryHandler.onMessageRetry(message)
            is Reply -> messageReplyHandler.onMessageReply(message.cid, message)
            is ThreadReply -> threadStartHandler.onStartThread(message)
            is Copy -> {
                val displayedText = message.getTranslatedText()
                context.copyToClipboard(displayedText)
            }
            is Edit -> messageEditHandler.onMessageEdit(message)
            is Pin -> {
                if (message.pinned) {
                    messageUnpinHandler.onMessageUnpin(message)
                } else {
                    messagePinHandler.onMessagePin(message)
                }
            }
            is MarkAsUnread -> messageMarkAsUnreadHandler.onMessageMarkAsUnread(message)
            is Delete -> {
                if (style.deleteConfirmationEnabled) {
                    confirmDeleteMessageHandler.onConfirmDeleteMessage(message) {
                        messageDeleteHandler.onMessageDelete(message)
                    }
                } else {
                    messageDeleteHandler.onMessageDelete(message)
                }
            }
            is FlagAction -> {
                if (style.flagMessageConfirmationEnabled) {
                    confirmFlagMessageHandler.onConfirmFlagMessage(message) {
                        messageFlagHandler.onMessageFlag(message)
                    }
                } else {
                    messageFlagHandler.onMessageFlag(message)
                }
            }
            is CustomAction -> customActionHandler.onCustomAction(message, messageAction.extraProperties)
            is React -> {
                // Handled by a separate handler.
            }
        }
    }
    //endregion

    //region Listener declarations
    public fun interface EnterThreadListener {
        public fun onThreadEntered(message: Message)
    }

    public fun interface MessageClickListener {
        public fun onMessageClick(message: Message)
    }

    public fun interface ReplyMessageClickListener {
        public fun onReplyClick(
            replyTo: Message,
        )
    }

    public fun interface MessageRetryListener {
        public fun onRetryMessage(message: Message)
    }

    public fun interface MessageLongClickListener {
        public fun onMessageLongClick(message: Message)
    }

    public fun interface ModeratedMessageLongClickListener {
        public fun onModeratedMessageLongClick(message: Message)
    }

    public fun interface ThreadClickListener {
        public fun onThreadClick(message: Message)
    }

    public fun interface AttachmentClickListener {
        public fun onAttachmentClick(message: Message, attachment: Attachment)
    }

    public fun interface AttachmentDownloadClickListener {
        public fun onAttachmentDownloadClick(attachment: Attachment)
    }

    public fun interface GiphySendListener {
        public fun onGiphySend(action: GiphyAction)
    }

    public fun interface LinkClickListener {
        public fun onLinkClick(url: String)
    }

    public fun interface UserClickListener {
        public fun onUserClick(user: User)
    }

    public fun interface ReactionViewClickListener {
        public fun onReactionViewClick(message: Message)
    }

    /**
     * Interface definition for a callback to be invoked when a user reaction is clicked on the message
     * options overlay.
     */
    public fun interface UserReactionClickListener {
        /**
         * Called when a reaction left by a user is clicked.
         *
         * @param message The message the reaction was left for.
         * @param user The user who reacted to the message.
         * @param reaction The reaction object.
         */
        public fun onUserReactionClick(message: Message, user: User, reaction: Reaction)
    }
    //endregion

    //region Handler declarations
    public fun interface EndRegionReachedHandler {
        public fun onEndRegionReached()
    }

    public fun interface LastMessageReadHandler {
        public fun onLastMessageRead()
    }

    public fun interface MessageEditHandler {
        public fun onMessageEdit(message: Message)
    }

    public fun interface MessageDeleteHandler {
        public fun onMessageDelete(message: Message)
    }

    public fun interface ConfirmDeleteMessageHandler {
        public fun onConfirmDeleteMessage(
            message: Message,
            confirmCallback: () -> Unit,
        )
    }

    public fun interface MessageFlagHandler {
        public fun onMessageFlag(message: Message)
    }

    public fun interface MessagePinHandler {
        public fun onMessagePin(message: Message)
    }

    public fun interface MessageMarkAsUnreadHandler {
        public fun onMessageMarkAsUnread(message: Message)
    }

    public fun interface MessageUnpinHandler {
        public fun onMessageUnpin(message: Message)
    }

    public fun interface FlagMessageResultHandler {
        public fun onHandleResult(result: Result<Flag>)
    }

    public fun interface ConfirmFlagMessageHandler {
        public fun onConfirmFlagMessage(message: Message, confirmCallback: () -> Unit)
    }

    public fun interface MessageRetryHandler {
        public fun onMessageRetry(message: Message)
    }

    public fun interface MessageReactionHandler {
        public fun onMessageReaction(message: Message, reactionType: String)
    }

    public fun interface MessageReplyHandler {
        public fun onMessageReply(cid: String, message: Message)
    }

    public fun interface ThreadStartHandler {
        public fun onStartThread(message: Message)
    }

    public fun interface GiphySendHandler {
        public fun onSendGiphy(action: GiphyAction)
    }

    public fun interface CustomActionHandler {
        public fun onCustomAction(message: Message, extraProperties: Map<String, Any>)
    }

    public fun interface AttachmentDownloadHandler {
        public fun onAttachmentDownload(attachmentDownloadCall: () -> Call<Unit>)
    }

    public fun interface ErrorEventHandler {
        public fun onErrorEvent(errorEvent: MessageListController.ErrorEvent)
    }

    public fun interface ModeratedMessageOptionHandler {
        public fun onModeratedMessageOptionSelected(message: Message, moderatedMessageOption: ModeratedMessageOption)
    }

    public fun interface OnScrollToBottomHandler {
        public fun onScrollToBottom()
    }
    //endregion

    /**
     * Predicate object with a filter condition for MessageListItem. Used to filter a list of
     * MessageListItem before applying it to MessageListView.
     */
    public fun interface MessageListItemPredicate {
        public fun predicate(item: MessageListItem): Boolean
    }

    /**
     * Predicate object with a filter condition for MessageListItem.
     * Used to filter a list of MessageListItem before applying it to MessageListView.
     */
    public fun interface ShowAvatarPredicate {
        public fun shouldShow(messageItem: MessageListItem.MessageItem): Boolean
    }

    public fun interface MessageListItemTransformer {
        public fun transform(itemList: List<MessageListItem>): List<MessageListItem>
    }

    public enum class NewMessagesBehaviour(internal val value: Int) {
        SCROLL_TO_BOTTOM(0), COUNT_UPDATE(1);

        internal companion object {
            fun parseValue(value: Int): NewMessagesBehaviour {
                return values().find { behaviour -> behaviour.value == value }
                    ?: throw IllegalArgumentException("Unknown behaviour type. It must be either SCROLL_TO_BOTTOM (int 0) or COUNT_UPDATE (int 1)")
            }
        }
    }

    public enum class MessagesStart(internal val value: Int) {
        BOTTOM(0), TOP(1);

        internal companion object {
            fun parseValue(value: Int): MessagesStart {
                return values().find { behaviour -> behaviour.value == value }
                    ?: throw IllegalArgumentException("Unknown messages start type. It must be either BOTTOM (int 0) or TOP (int 1)")
            }
        }
    }
}
