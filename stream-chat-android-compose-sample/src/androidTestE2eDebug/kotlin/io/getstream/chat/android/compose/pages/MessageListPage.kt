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

open class MessageListPage {

    class Header {

        companion object {
            val channelAvatar get() = By.res("Stream_ChannelAvatar")
            val channelName get() = By.res("Stream_ChannelName")
            val participantsInfo get() = By.res("Stream_ParticipantsInfo")
            val backButton get() = By.res("Stream_BackButton")
        }
    }

    class MembersList {

        companion object {
            val members get() = By.res("Stream_MembersList")
        }
    }

    class AttachmentPicker {

        companion object {
            val view get() = By.res("Stream_AttachmentsPicker")
            val sendButton get() = By.res("Stream_AttachmentPickerSendButton")
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
            val recordAudioButton get() = By.res("Stream_ComposerAudioRecordingButton")
            val commandsButton get() = By.res("Stream_ComposerCommandsButton")
            val suggestionList get() = By.res("Stream_SuggestionList")
            val suggestionListTitle get() = By.res("Stream_SuggestionListTitle")
            val participantMentionSuggestion get() = By.res("Stream_MentionSuggestionItem")
            val giphyButton get() = By.res("Stream_SuggestionListGiphyButton")
            val attachmentsButton get() = By.res("Stream_ComposerAttachmentsButton")
            val quotedMessage get() = By.res("Stream_QuotedMessage")
            val cancelButton get() = By.res("Stream_ComposerCancelButton")
            val attachmentCancelIcon get() = By.res("Stream_AttachmentCancelIcon")
            val columnWithMultipleFileAttachments get() = By.res("Stream_FileAttachmentPreviewContent")
            val columnWithMultipleMediaAttachments get() = By.res("Stream_MediaAttachmentPreviewContent")
            val mediaAttachment get() = By.res("Stream_MediaAttachmentPreviewItem")
            val fileSize get() = By.res("Stream_FileSizeInPreview")
            val fileName get() = By.res("Stream_FileNameInPreview")
            val fileImage = MessageList.Message.fileImage
            val linkPreviewImage get() = By.res("Stream_LinkPreviewImage")
            val linkPreviewTitle get() = By.res("Stream_LinkPreviewTitle")
            val linkPreviewDescription get() = By.res("Stream_LinkPreviewDescription")
            val linkPreviewCancelButton get() = By.res("Stream_LinkPreviewCancelButton")
        }
    }

    class MessageList {

        companion object {
            val messageList get() = By.res("Stream_MessageList")
            val messages get() = By.res("Stream_MessageCell")
            val dateSeparator get() = By.res("Stream_MessageDateSeparator")
            val unreadMessagesBadge get() = By.res("Stream_UnreadMessagesBadge")
            val typingIndicator get() = By.res("Stream_MessageListTypingIndicator")
            val scrollToBottomButton get() = By.res("Stream_ScrollToBottomButton")
            val systemMessage get() = By.res("Stream_SystemMessage")
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
                val readCount get() = By.res("Stream_MessageReadCount")
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
                val fileDownloadButton get() = By.res("Stream_FileAttachmentDownloadButton")
                val columnWithMultipleFileAttachments get() = By.res("Stream_MultipleFileAttachmentsColumn")
                val giphy get() = By.res("Stream_GiphyContent")
                val linkPreviewImage get() = By.res("Stream_LinkAttachmentPreview")
                val linkPreviewTitle get() = By.res("Stream_LinkAttachmentTitle")
                val linkPreviewDescription get() = By.res("Stream_LinkAttachmentDescription")
            }

            class Reactions {

                companion object {
                    val reactions get() = By.res("Stream_MessageReaction")
                    private val like get() = By.res("Stream_MessageReaction_${ReactionType.LIKE.reaction}")
                    private val love get() = By.res("Stream_MessageReaction_${ReactionType.LOVE.reaction}")
                    private val lol get() = By.res("Stream_MessageReaction_${ReactionType.LOL.reaction}")
                    private val wow get() = By.res("Stream_MessageReaction_${ReactionType.WOW.reaction}")
                    private val sad get() = By.res("Stream_MessageReaction_${ReactionType.SAD.reaction}")

                    fun reaction(type: ReactionType): BySelector {
                        return when (type) {
                            ReactionType.LIKE -> like
                            ReactionType.LOVE -> love
                            ReactionType.LOL -> lol
                            ReactionType.SAD -> sad
                            ReactionType.WOW -> wow
                        }
                    }
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
                    val block get() = By.res("Stream_ContextMenu_Block user")
                    val delete get() = By.res("Stream_ContextMenu_Delete Message")
                    val ok = By.text("OK")
                }

                class ReactionsView {

                    companion object {
                        private val like get() = By.res("Stream_Reaction_${ReactionType.LIKE.reaction}")
                        private val love get() = By.res("Stream_Reaction_${ReactionType.LOVE.reaction}")
                        private val lol get() = By.res("Stream_Reaction_${ReactionType.LOL.reaction}")
                        private val wow get() = By.res("Stream_Reaction_${ReactionType.WOW.reaction}")
                        private val sad get() = By.res("Stream_Reaction_${ReactionType.SAD.reaction}")

                        fun reaction(type: ReactionType): BySelector {
                            return when (type) {
                                ReactionType.LIKE -> like
                                ReactionType.LOVE -> love
                                ReactionType.LOL -> lol
                                ReactionType.SAD -> sad
                                ReactionType.WOW -> wow
                            }
                        }
                    }
                }
            }
        }
    }
}
