/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.pages

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import io.getstream.chat.android.e2e.test.mockserver.ReactionType
import io.getstream.chat.android.e2e.test.uiautomator.appContext
import java.util.regex.Pattern
import io.getstream.chat.android.compose.R as ComposeR
import io.getstream.chat.android.ui.common.R as UiCommonR

open class MessageListPage {

    class Header {

        companion object {
            val channelAvatar get() = By.res("Stream_ChannelAvatar")
            val channelName get() = By.res("Stream_ChannelName")
            val participantsInfo get() = By.res("Stream_ParticipantsInfo")
            val backButton get() = By.res("Stream_BackButton")
        }
    }

    class AttachmentPicker {

        companion object {
            val view get() = By.res("Stream_AttachmentsPicker")
            val filesTab get() = By.res("Stream_AttachmentPickerFilesTab")
            val mediaCaptureTab get() = By.res("Stream_AttachmentPickerMediaCaptureTab")
            val pollsTab get() = By.res("Stream_AttachmentPickerPollsTab")
            val findFilesButton get() = By.res("Stream_FindFilesButton")
            val rootsButton = By.descContains("Show roots")
            val downloadsView = By.text("Downloads")
            val image1 = By.text("file_1.png")
            val image2 = By.text("file_2.png")
            val pdf1 = By.text("file_1.pdf")
            val pdf2 = By.text("file_2.pdf")
        }
    }

    class Composer {

        companion object {
            val inputField get() = By.res("Stream_ComposerInputField")
            val sendButton get() = By.res("Stream_ComposerSendButton")
            val cooldownIndicator get() = By.res("Stream_ComposerCooldownIndicator")
            val saveButton get() = By.res("Stream_ComposerSaveButton")

            // The composer trailing button is the send button normally and the save button in
            // command mode; one selector covers taps that accept either.
            val confirmButton get() = By.res(Pattern.compile("Stream_Composer(Send|Save)Button"))
            val recordAudioButton get() = By.res("Stream_ComposerRecordAudioButton")
            val commandSuggestionList get() = By.res("Stream_CommandSuggestionList")
            val commandSuggestionListTitle get() = By.res("Stream_CommandSuggestionListTitle")
            val userSuggestion get() = By.res("Stream_SuggestionItem")
            val giphyButton get() = By.res("Stream_SuggestionListGiphyButton")
            val attachmentsButton get() = By.res("Stream_ComposerAttachmentsButton")
            val quotedMessage get() = By.res("Stream_QuotedMessage")
            val cancelEditButton get() = By.res("Stream_ComposerCancelEditButton")
            val attachmentCancelIcon get() = By.res("Stream_MessageComposerAttachmentCancelIcon")
            val columnWithMultipleFileAttachments get() = By.res("Stream_MessageComposerAttachments")
            val columnWithMultipleMediaAttachments get() = By.res("Stream_MessageComposerAttachments")
            val mediaAttachment get() = By.res("Stream_MessageComposerAttachmentMediaItem")
            val fileSize get() = By.res("Stream_MessageComposerAttachmentFileSize")
            val fileName get() = By.res("Stream_MessageComposerAttachmentFileName")
            val fileImage = MessageList.Message.fileImage
            val linkPreviewImage get() = By.res("Stream_LinkPreviewImage")
            val linkPreviewTitle get() = By.res("Stream_LinkPreviewTitle")
            val linkPreviewDescription get() = By.res("Stream_LinkPreviewDescription")
            val linkPreviewCancelButton get() = By.res("Stream_LinkPreviewCancelButton")
        }
    }

    class FlagMessageDialog {

        companion object {
            // The dialog title repeats the message option label, so the body text is what
            // identifies the dialog.
            val body get() = By.text(appContext.getString(ComposeR.string.stream_compose_flag_message_text))
        }
    }

    class MessageList {

        companion object {
            val messageList get() = By.res("Stream_Messages")
            val messages get() = By.res("Stream_MessageCell")
            val dateSeparator get() = By.res("Stream_MessageDateSeparator")
            val unreadMessagesBadge get() = By.res("Stream_UnreadMessagesBadge")
            val typingIndicator get() = By.res("Stream_MessageListTypingIndicator")
            val scrollToBottomButton get() = By.res("Stream_ScrollToBottomButton")
            val scrollToBottomButtonUnreadCount get() = By.res("Stream_ScrollToBottomButtonUnreadCount")
            val scrollToFirstUnreadButton get() = By.res("Stream_ScrollToFirstUnreadButton")
            val scrollToFirstUnreadDismissIcon get() = By.res("Stream_ScrollToFirstUnreadButton_Dismiss")
        }

        class Message {

            companion object {
                val avatar get() = By.res("Stream_UserAvatar")
                val authorName get() = By.res("Stream_MessageAuthorName")
                val text get() = By.res("Stream_MessageText")
                val clickableText get() = By.res("Stream_MessageClickableText")
                val deliveryStatusIsRead get() = By.res("Stream_MessageReadStatus_isRead")
                val deliveryStatusIsPending get() = By.res("Stream_MessageReadStatus_isPending")
                val deliveryStatusIsSent get() = By.res("Stream_MessageReadStatus_isSent")
                val deliveryStatusIsFailed get() = By.res("Stream_MessageFailedIcon")
                val timestamp get() = By.res("Stream_Timestamp")
                val quotedMessage get() = By.res("Stream_QuotedMessage")
                val threadRepliesLabel get() = By.res("Stream_ThreadRepliesLabel")
                val threadParticipantAvatar get() = By.res("Stream_ThreadParticipantAvatar")
                val editedLabel get() = By.res("Stream_MessageEditedLabel")
                val deletedMessage get() = By.res("Stream_MessageDeleted")
                val messageHeaderLabel get() = By.res("Stream_MessageHeaderLabel") // e.g.: Pinned by you
                val image get() = By.res("Stream_MediaContent_Image")
                val video get() = By.res("Stream_MediaContent_Video")
                val columnWithMultipleMediaAttachments get() = By.res("Stream_MultipleMediaAttachmentsColumn")
                val fileImage get() = By.res("Stream_FileAttachmentImage")
                val fileName get() = By.res("Stream_FileAttachmentName")
                val fileSize get() = By.res("Stream_FileAttachmentSize")
                val columnWithMultipleFileAttachments get() = By.res("Stream_MultipleFileAttachmentsColumn")
                val giphy get() = By.res("Stream_GiphyContent")
                val linkPreviewImage get() = By.res("Stream_LinkAttachmentPreview")
                val linkPreviewTitle get() = By.res("Stream_LinkAttachmentTitle")
                val linkPreviewDescription get() = By.res("Stream_LinkAttachmentDescription")
            }

            class Poll {

                companion object {
                    val singleVoteSubtitle get() = By.text(appContext.getString(UiCommonR.string.stream_ui_poll_description_single_answer))
                    val closedSubtitle get() = By.text(appContext.getString(UiCommonR.string.stream_ui_poll_description_closed))
                    val viewResultsButton get() = By.res("Stream_PollViewResultsButton")
                    val endPollButton get() = By.res("Stream_PollEndButton")
                    val endPollConfirmationAction get() = By.res("Stream_PollEndConfirmButton")
                    val resultsTitle get() = By.text(appContext.getString(ComposeR.string.stream_compose_poll_results))

                    // The row is the toggle: the option text is a plain child node, while the
                    // node carrying the vote state (and the click handling) is the row around it.
                    fun option(text: String): BySelector =
                        By.res("Stream_PollOptionVotingRow").hasDescendant(By.text(text))

                    fun optionWithVoteCount(text: String, count: Int): BySelector = option(text).hasDescendant(
                        By.desc(appContext.resources.getQuantityString(ComposeR.plurals.stream_compose_poll_vote_counts, count, count)),
                    )

                    fun question(text: String): BySelector = By.text(text)
                }
            }

            class Reactions {

                companion object {
                    val reactions get() = By.res("Stream_MessageReaction")
                    val reactionAuthor get() = By.res("Stream_ReactionAuthor")

                    fun reaction(type: ReactionType): BySelector =
                        By.res("Stream_MessageReaction_${type.reaction}")
                }
            }

            class GiphyButtons {

                companion object {
                    val cancel get() = By.res("Stream_GiphyButton_Cancel")
                    val shuffle get() = By.res("Stream_GiphyButton_Shuffle")
                    val send get() = By.res("Stream_GiphyButton_Send")
                }
            }

            class ContextMenu {

                companion object {
                    val reply get() = By.res("Stream_ContextMenu_Reply")
                    val resend get() = By.res("Stream_ContextMenu_Resend")
                    val threadReply get() = By.res("Stream_ContextMenu_Thread reply")
                    val markAsUnread get() = By.res("Stream_ContextMenu_Mark as Unread")
                    val copy get() = By.res("Stream_ContextMenu_Copy Message")
                    val edit get() = By.res("Stream_ContextMenu_Edit Message")
                    val flag get() = By.res("Stream_ContextMenu_Flag Message")
                    val pin get() = By.res("Stream_ContextMenu_Pin to this Chat")
                    val unpin get() = By.res("Stream_ContextMenu_Unpin from this Chat")
                    val muteUser get() = By.res("Stream_ContextMenu_Mute User")
                    val unmuteUser get() = By.res("Stream_ContextMenu_Unmute User")
                    val block get() = By.res("Stream_ContextMenu_Block user")
                    val unblock get() = By.res("Stream_ContextMenu_Unblock user")
                    val delete get() = By.res("Stream_ContextMenu_Delete Message")
                    val showMoreReactions = By.desc("Show more reactions")
                    val ok = By.text("OK")
                }

                class ReactionsView {

                    companion object {
                        fun reaction(type: ReactionType): BySelector =
                            By.res("Stream_Reaction_${type.reaction}")
                    }
                }
            }
        }
    }
}
