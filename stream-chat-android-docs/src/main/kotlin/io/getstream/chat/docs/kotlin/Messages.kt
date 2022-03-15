package io.getstream.chat.docs.kotlin

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.SearchMessagesResult
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.docs.kotlin.helpers.MyFileUploader
import java.io.File
import java.util.Calendar
import java.util.Date

class Messages(
    val client: ChatClient,
    val channelClient: ChannelClient,
    val message: Message,
    val parentMessage: Message,
) {

    /**
     * @see <a href="https://getstream.io/chat/docs/send_message/?language=kotlin">Messages Overview</a>
     */
    inner class MessagesOverview {
        fun sendAMessage() {
            val channelClient = client.channel("messaging", "general")
            val message = Message(
                text = "Josh, I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish."
            )

            channelClient.sendMessage(message).enqueue { result ->
                if (result.isSuccess) {
                    val sentMessage: Message = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        fun sendAComplexMessage() {
            // Create an image attachment
            val attachment = Attachment(
                type = "image",
                imageUrl = "https://bit.ly/2K74TaG",
                thumbUrl = "https://bit.ly/2Uumxti",
                extraData = mutableMapOf("myCustomField" to 123),
            )

            // Create a message with the attachment and a user mention
            val message = Message(
                text = "@Josh I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish.",
                attachments = mutableListOf(attachment),
                mentionedUsersIds = mutableListOf("josh-id"),
                extraData = mutableMapOf("anotherCustomField" to 234),
            )

            // Send the message to the channel
            channelClient.sendMessage(message).enqueue { /* ... */ }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=kotlin#get-a-message">Get A Message</a>
         */
        fun getAMessage() {
            channelClient.getMessage("message-id").enqueue { result ->
                if (result.isSuccess) {
                    val message = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=kotlin#update-a-message">Update A Message</a>
         */
        fun updateAMessage() {
            // Update some field of the message
            message.text = "my updated text"

            // Send the message to the channel
            channelClient.updateMessage(message).enqueue { result ->
                if (result.isSuccess) {
                    val updatedMessage = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=kotlin#delete-a-message">Delete A Message</a>
         */
        fun deleteAMessage() {
            channelClient.deleteMessage("message-id").enqueue { result ->
                if (result.isSuccess) {
                    val deletedMessage = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    inner class MessageFormat {
        fun openGraphScraper() {
            val message = Message(
                text = "Check this bear out https://imgur.com/r/bears/4zmGbMN"
            )
            channelClient.sendMessage(message).enqueue { /* ... */ }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/file_uploads/?language=kotlin">File Uploads</a>
     */
    inner class FileUploads {
        val imageFile = File("path")
        val anyOtherFile = File("path")

        fun fileUploads() {
            val channelClient = client.channel("messaging", "general")

            // Upload an image without detailed progress
            channelClient.sendImage(imageFile).enqueue { result ->
                if (result.isSuccess) {
                    // Successful upload, you can now attach this image
                    // to an message that you then send to a channel
                    val imageUrl = result.data()
                    val attachment = Attachment(
                        type = "image",
                        imageUrl = imageUrl,
                    )
                    val message = Message(
                        attachments = mutableListOf(attachment),
                    )
                    channelClient.sendMessage(message).enqueue { /* ... */ }
                }
            }

            // Upload a file, monitoring for progress with a ProgressCallback
            channelClient.sendFile(
                anyOtherFile,
                object : ProgressCallback {
                    override fun onSuccess(url: String?) {
                        val fileUrl = url
                    }

                    override fun onError(error: ChatError) {
                        // Handle error
                    }

                    override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
                        // You can render the uploading progress here.
                    }
                }
            ).enqueue() // No callback passed to enqueue, as we'll get notified above anyway
        }

        fun usingYourOwnCdn(apiKey: String, context: Context) {
            // Set a custom FileUploader implementation when building your client
            val client = ChatClient.Builder("{{ api_key }}", context)
                .fileUploader(MyFileUploader())
                .build()
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=kotlin">Reactions</a>
     */
    inner class Reactions {
        fun sendAReaction() {
            val channelClient = client.channel("messaging", "general")

            // Add reaction 'like' with a custom field
            val reaction = Reaction(
                messageId = "message-id",
                type = "like",
                score = 1,
                extraData = mutableMapOf("customField" to 1),
            )
            channelClient.sendReaction(reaction).enqueue { result ->
                if (result.isSuccess) {
                    val sentReaction: Reaction = result.data()
                } else {
                    // Handle result.error()
                }
            }

            // Add reaction 'like' and replace all other reactions of this user by it
            channelClient.sendReaction(reaction, enforceUnique = true).enqueue { result ->
                if (result.isSuccess) {
                    val sentReaction = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=kotlin#removing-a-reaction">Removing A Reaction</a>
         */
        fun removeAReaction() {
            channelClient.deleteReaction(
                messageId = "message-id",
                reactionType = "like",
            ).enqueue { result ->
                if (result.isSuccess) {
                    val message = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=kotlin#paginating-reactions">Paginating Reactions</a>
         */
        fun paginatingReactions() {
            // Get the first 10 reactions
            channelClient.getReactions(
                messageId = "message-id",
                offset = 0,
                limit = 10,
            ).enqueue { result ->
                if (result.isSuccess) {
                    val reactions: List<Reaction> = result.data()
                } else {
                    // Handle result.error()
                }
            }

            // Get the second 10 reactions
            channelClient.getReactions(
                messageId = "message-id",
                offset = 10,
                limit = 10,
            ).enqueue { /* ... */ }

            // Get 10 reactions after particular reaction
            channelClient.getReactions(
                messageId = "message-id",
                firstReactionId = "reaction-id",
                limit = 10,
            ).enqueue { /* ... */ }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=kotlin#cumulative-clap-reactions">Cumulative (Clap) Reactions</a>
         */
        fun cumulativeReactions() {
            val reaction = Reaction(messageId = "message-id", type = "clap", score = 5)
            channelClient.sendReaction(reaction).enqueue { /* ... */ }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/threads/?language=kotlin">Threads & Replies</a>
     */
    inner class ThreadsAndReplies {
        fun startAThread() {
            val message = Message(
                text = "Hello there!",
                parentId = parentMessage.id,
            )

            // Send the message to the channel
            channelClient.sendMessage(message).enqueue { result ->
                if (result.isSuccess) {
                    val sentMessage = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/threads/?language=kotlin#thread-pagination">Thread Pagination</a>
         */
        fun threadPagination() {
            // Retrieve the first 20 messages inside the thread
            client.getReplies(parentMessage.id, limit = 20).enqueue { result ->
                if (result.isSuccess) {
                    val replies: List<Message> = result.data()
                } else {
                    // Handle result.error()
                }
            }

            // Retrieve the 20 more messages before the message with id "42"
            client.getRepliesMore(
                messageId = parentMessage.id,
                firstId = "42",
                limit = 20,
            ).enqueue { /* ... */ }
        }

        val originalMessage = Message()

        fun quoteMessage() {
            val message = Message(
                text = "This message quotes another message!",
                replyMessageId = originalMessage.id,
            )
            channelClient.sendMessage(message).enqueue { /* ... */ }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/silent_messages/?language=kotlin">Silent Messages</a>
     */
    inner class SilentMessages {
        val systemUser = User()

        fun silentMessage() {
            val message = Message(
                text = "You and Jane are now matched!",
                user = systemUser,
                silent = true,
            )
            channelClient.sendMessage(message).enqueue { /* ... */ }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/search/?language=kotlin">Search</a>
     */
    inner class Search {
        fun searchMessagesLimitAndOffsetPagination() {
            client.searchMessages(
                offset = 0,
                limit = 10,
                channelFilter = Filters.`in`("members", listOf("john")),
                messageFilter = Filters.autocomplete("text", "supercalifragilisticexpialidocious"),
            ).enqueue { result ->
                if (result.isSuccess) {
                    val messages: List<Message> = result.data().messages
                } else {
                    // Handle result.error()
                }
            }
        }

        fun searchMessagesLimitAndNextPagination() {
            val channelFilter = Filters.`in`("members", listOf("john"))
            val messageFilter = Filters.autocomplete("text", "supercalifragilisticexpialidocious")

            // First 10 results sorted by relevance in descending order
            val page1: SearchMessagesResult = client.searchMessages(
                channelFilter = channelFilter,
                messageFilter = messageFilter,
                limit = 10,
                sort = QuerySort.desc("relevance")
            ).execute().data()

            // Next 10 results with the same sort order embedded in the next value
            val page2: SearchMessagesResult = client.searchMessages(
                channelFilter = channelFilter,
                messageFilter = messageFilter,
                limit = 10,
                next = page1.previous
            ).execute().data()

            // The previous 10 results
            val page1Again: SearchMessagesResult = client.searchMessages(
                channelFilter = channelFilter,
                messageFilter = messageFilter,
                limit = 10,
                next = page2.previous
            ).execute().data()
        }
    }

    /**
     * @see <a https://getstream.io/chat/docs/pinned_messages/?language=java">Pinned Messages</a>
     */
    inner class PinnedMessages {

        fun pinAndUnpinAMessage() {
            // Create pinned message
            val pinExpirationDate = Calendar.getInstance().apply {
                set(2077, 1, 1)
            }.time
            val message = Message(
                text = "Hey punk",
                pinned = true,
                pinExpires = pinExpirationDate
            )

            channelClient.sendMessage(message).enqueue { /* ... */ }

            // Unpin message
            channelClient.unpinMessage(message).enqueue { /* ... */ }

            // Pin message for 120 seconds
            channelClient.pinMessage(message, timeout = 120).enqueue { /* ... */ }

            // Change message expiration to 2077
            channelClient.pinMessage(message, expirationDate = pinExpirationDate).enqueue { /* ... */ }

            // Remove expiration date from pinned message
            channelClient.pinMessage(message, expirationDate = null).enqueue { /* ... */ }
        }

        fun retrievePinnedMessages() {
            channelClient.query(QueryChannelRequest()).enqueue { result ->
                if (result.isSuccess) {
                    val pinnedMessages: List<Message> = result.data().pinnedMessages
                } else {
                    // Handle result.error()
                }
            }
        }

        fun paginateOverAllPinnedMessages() {
            // List the first page of pinned messages, pinned before now, of the channel with descending direction (newest on top)
            channelClient.getPinnedMessages(
                limit = 10,
                sort = QuerySort.desc(Message::pinnedAt),
                pagination = PinnedMessagesPagination.BeforeDate(
                    date = Date(),
                    inclusive = false,
                ),
            ).enqueue { result ->
                if (result.isSuccess) {
                    val pinnedMessages: List<Message> = result.data()
                } else {
                    // Handle result.error()
                }
            }

            // You can use a pinnedAt date retrieved from the previous request to get the next page
            val nextDate = Date()
            // List the next page of pinned messages
            channelClient.getPinnedMessages(
                limit = 10,
                sort = QuerySort.desc(Message::pinnedAt),
                pagination = PinnedMessagesPagination.BeforeDate(
                    date = nextDate,
                    inclusive = false,
                ),
            ).enqueue { result ->
                if (result.isSuccess) {
                    val pinnedMessages: List<Message> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }
}
