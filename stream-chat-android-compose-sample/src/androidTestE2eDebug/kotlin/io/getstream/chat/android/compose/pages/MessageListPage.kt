/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
            val channelAvatar = By.res("Stream_ChannelAvatar")
            val channelName = By.res("Stream_ChannelName")
            val participantsInfo = By.res("Stream_ParticipantsInfo")
            val backButton = By.res("Stream_BackButton")
        }
    }

    class MembersList {

        companion object {
            val members = By.res("Stream_MembersList")
        }
    }

    class AttachmentPicker {

        companion object {
            val view = By.res("Stream_AttachmentsPicker")
            val sendButton = By.res("Stream_AttachmentPickerSendButton")
            val imagesTab = By.res("Stream_AttachmentPickerImagesTab")
            val filesTab = By.res("Stream_AttachmentPickerFilesTab")
            val mediaCaptureTab = By.res("Stream_AttachmentPickerMediaCaptureTab")
            val pollsTab = By.res("Stream_AttachmentPickerPollsTab")
            val sampleImage = By.res("Stream_AttachmentPickerSampleImage")
        }
    }

    class Composer {

        companion object {
            val inputField = By.res("Stream_ComposerInputField")
            val sendButton = By.res("Stream_ComposerSendButton")
            val recordAudioButton = By.res("Stream_ComposerRecordAudioButton")
            val commandsButton = By.res("Stream_ComposerCommandsButton")
            val suggestionList = By.res("Stream_SuggestionList")
            val suggestionListTitle = By.res("Stream_SuggestionListTitle")
            val participantMentionSuggestion = By.res("Stream_MentionSuggestionItem")
            val giphyButton = By.res("Stream_SuggestionListGiphyButton")
            val attachmentsButton = By.res("Stream_ComposerAttachmentsButton")
            val quotedMessage = By.res("Stream_QuotedMessage")
            val quotedMessageAvatar = By.res("Stream_QuotedMessageAuthorAvatar")
            val cancelButton = By.res("Stream_ComposerCancelButton")
        }
    }

    class MessageList {

        companion object {
            val messageList = By.res("Stream_MessageList")
            val messages = By.res("Stream_MessageCell")
            val dateSeparator = By.res("Stream_MessageDateSeparator")
            val unreadMessagesBadge = By.res("Stream_UnreadMessagesBadge")
            val typingIndicator = By.res("Stream_MessageListTypingIndicator")
            val scrollToBottomButton = By.res("Stream_ScrollToBottomButton")
        }

        class Message {

            companion object {
                val avatar = By.res("Stream_UserAvatar")
                val authorName = By.res("Stream_MessageAuthorName")
                val text = By.res("Stream_MessageText")
                val clickableText = By.res("Stream_MessageClickableText")
                val readStatusIsRead = By.res("Stream_MessageReadStatus_isRead")
                val readStatusIsPending = By.res("Stream_MessageReadStatus_isPending")
                val readStatusIsSent = By.res("Stream_MessageReadStatus_isSent")
                val failedIcon = By.res("Stream_MessageFailedIcon")
                val readCount = By.res("Stream_MessageReadCount")
                val timestamp = By.res("Stream_Timestamp")
                val quotedMessage = By.res("Stream_QuotedMessage")
                val quotedMessageAvatar = By.res("Stream_QuotedMessageAuthorAvatar")
                val threadRepliesLabel = By.res("Stream_ThreadRepliesLabel")
                val threadParticipantAvatar = By.res("Stream_ThreadParticipantAvatar")
                val editedLabel = By.res("Stream_MessageEditedLabel")
                val deletedMessage = By.res("Stream_MessageDeleted")
                val messageHeaderLabel = By.res("Stream_MessageHeaderLabel") // e.g.: Pinned by you
                val image = By.res("Stream_MediaContent")
                val video = By.res("Stream_PlayButton")
                val fileImage = By.res("Stream_FileAttachmentImage")
                val fileDescription = By.res("Stream_FileAttachmentDescription")
                val fileSize = By.res("Stream_FileAttachmentSize")
                val fileDownloadButton = By.res("Stream_FileAttachmentDownloadButton")
                val giphy = By.res("Stream_GiphyContent")
                val linkAttachmentPreview = By.res("Stream_LinkAttachmentPreview")
                val linkAttachmentTitle = By.res("Stream_LinkAttachmentTitle")
                val linkAttachmentDescription = By.res("Stream_LinkAttachmentDescription")
            }

            class Reactions {

                companion object {
                    val reactions = By.res("Stream_MessageReaction")
                    private val like = By.res("Stream_MessageReaction_${ReactionType.LIKE.reaction}")
                    private val love = By.res("Stream_MessageReaction_${ReactionType.LOVE.reaction}")
                    private val lol = By.res("Stream_MessageReaction_${ReactionType.LOL.reaction}")
                    private val wow = By.res("Stream_MessageReaction_${ReactionType.WOW.reaction}")
                    private val sad = By.res("Stream_MessageReaction_${ReactionType.SAD.reaction}")

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
                    val cancel = By.res("Stream_GiphyButton_Cancel")
                    val shuffle = By.res("Stream_GiphyButton_Shuffle")
                    val send = By.res("Stream_GiphyButton_Send")
                }
            }

            class ContextMenu {

                companion object {
                    val reply = By.res("Stream_ContextMenu_Reply")
                    val resend = By.res("Stream_ContextMenu_Resend")
                    val threadReply = By.res("Stream_ContextMenu_Thread reply")
                    val markAsUnread = By.res("Stream_ContextMenu_Mark as Unread")
                    val copy = By.res("Stream_ContextMenu_Copy Message")
                    val edit = By.res("Stream_ContextMenu_Edit Message")
                    val flag = By.res("Stream_ContextMenu_Flag Message")
                    val pin = By.res("Stream_ContextMenu_Pin to this Chat")
                    val unpin = By.res("Stream_ContextMenu_Unpin from this Chat")
                    val block = By.res("Stream_ContextMenu_Block user")
                    val delete = By.res("Stream_ContextMenu_Delete Message")
                    val ok = By.text("OK")
                }

                class ReactionsView {

                    companion object {
                        private val like = By.res("Stream_Reaction_${ReactionType.LIKE.reaction}")
                        private val love = By.res("Stream_Reaction_${ReactionType.LOVE.reaction}")
                        private val lol = By.res("Stream_Reaction_${ReactionType.LOL.reaction}")
                        private val wow = By.res("Stream_Reaction_${ReactionType.WOW.reaction}")
                        private val sad = By.res("Stream_Reaction_${ReactionType.SAD.reaction}")

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
