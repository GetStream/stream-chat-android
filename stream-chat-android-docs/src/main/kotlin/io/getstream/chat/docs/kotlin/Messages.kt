package io.getstream.chat.docs.kotlin

import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.docs.StaticInstances.TAG
import java.io.File
import java.util.Calendar

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
            // Create a message
            val message = Message()
            message.text =
                "Josh I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish."
            message.extraData["anotherCustomField"] = 234

            // Add an image attachment to the message
            val attachment = Attachment()
            attachment.type = "image"
            attachment.imageUrl = "https://bit.ly/2K74TaG"
            attachment.fallback = "test image"
            // Add some custom data to the attachment
            attachment.extraData["myCustomField"] = 123

            message.attachments.add(attachment)

            // Include the user id of the mentioned user
            message.mentionedUsers.add(User("josh-id"))

            // Send the message to the channel
            channelClient.sendMessage(message).enqueue {
                if (it.isSuccess) {
                    val sentMessage = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=kotlin#get-a-message">Get A Message</a>
         */
        fun getAMessage() {
            channelClient.getMessage("message-id").enqueue {
                if (it.isSuccess) {
                    val message = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
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
            channelClient.updateMessage(message).enqueue {
                if (it.isSuccess) {
                    val updatedMessage = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_message/?language=kotlin#delete-a-message">Delete A Message</a>
         */
        fun deleteAMessage() {
            channelClient.deleteMessage("message-id").enqueue {
                if (it.isSuccess) {
                    val deletedMessage = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/file_uploads/?language=kotlin">File Uploads</a>
     */
    inner class FileUploads {
        fun fileUploads() {
            val imageFile = File("path")
            val anyOtherFile = File("path")

            // Upload an image
            channelClient.sendImage(
                imageFile,
                object : ProgressCallback {
                    override fun onSuccess(file: String) {
                        val fileUrl = file
                    }

                    override fun onError(error: ChatError) {
                        Log.e(TAG, String.format("There was an error %s", error, error.cause))
                    }

                    override fun onProgress(progress: Long) {
                        // You can render uploading progress here
                    }
                }
            )

            // Upload a file
            channelClient.sendFile(
                anyOtherFile,
                object : ProgressCallback {
                    override fun onSuccess(file: String) {
                        val fileUrl = file
                    }

                    override fun onError(error: ChatError) {
                        Log.e(TAG, String.format("There was an error %s", error, error.cause))
                    }

                    override fun onProgress(progress: Long) {
                        // You can render uploading progress here
                    }
                }
            )
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=kotlin">Reactions</a>
     */
    inner class Reactions {
        fun sendAReaction() {
            // Add reaction 'like'
            val reaction = Reaction("message-id", type = "like", score = 1)
            channelClient.sendReaction(reaction).enqueue {
                if (it.isSuccess) {
                    val sentReaction = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error()))
                }
            }

            // Add reaction 'like' and replace all other reactions of this user by it
            channelClient.sendReaction(reaction, enforceUnique = true).enqueue {
                if (it.isSuccess) {
                    val sentReaction = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error()))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=kotlin#removing-a-reaction">Removing A Reaction</a>
         */
        fun removeAReaction() {
            channelClient.deleteReaction("message-id", reactionType = "like").enqueue {
                if (it.isSuccess) {
                    val message = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error()))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=kotlin#paginating-reactions">Paginating Reactions</a>
         */
        fun paginatingReactions() {
            // Get the first 10 reactions
            channelClient.getReactions("message-id", offset = 0, limit = 10).enqueue {
                if (it.isSuccess) {
                    val reactions = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error()))
                }
            }

            // Get the second 10 reactions
            channelClient.getReactions("message-id", offset = 10, limit = 10).enqueue {
                if (it.isSuccess) {
                    val reactions = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error()))
                }
            }

            // Get 10 reactions after particular reaction
            channelClient.getReactions("message-id", "reaction-id", limit = 10).enqueue {
                if (it.isSuccess) {
                    val reactions = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error()))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/send_reaction/?language=kotlin#cumulative-clap-reactions">Cumulative (Clap) Reactions</a>
         */
        fun cumulativeReactions() {
            val reaction = Reaction("message-id", type = "clap", score = 5)
            channelClient.sendReaction(reaction).enqueue {
                if (it.isSuccess) {
                    val sentReaction = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error()))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/threads/?language=kotlin">Threads & Replies</a>
     */
    inner class ThreadsAndReplies {
        fun startAThread() {
            // Set the parent id to make sure a message shows up in a thread
            val message = Message()
            message.text = "hello world"
            message.parentId = parentMessage.id

            // Send the message to the channel
            channelClient.sendMessage(message).enqueue {
                if (it.isSuccess) {
                    val sentMessage = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/threads/?language=kotlin#thread-pagination">Thread Pagination</a>
         */
        fun threadPagination() {
            val limit = 20
            // Retrieve the first 20 messages inside the thread
            client.getReplies(parentMessage.id, limit).enqueue {
                if (it.isSuccess) {
                    val replies = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }

            // Retrieve the 20 more messages before the message with id "42"
            client.getRepliesMore(parentMessage.id, "42", limit).enqueue {
                if (it.isSuccess) {
                    val replies = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/silent_messages/?language=kotlin">Silent Messages</a>
     */
    inner class SilentMessages {
        fun silentMessage() {
            val message = Message("text-of-a-message", silent = true)
            channelClient.sendMessage(message).enqueue {
                if (it.isSuccess) {
                    val sentMessage = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/search/?language=kotlin">Search</a>
     */
    inner class Search {
        fun searchMessages() {
            val offset = 0
            val limit = 10
            val query = "supercalifragilisticexpialidocious"
            val channelFilter: FilterObject = Filters.`in`("members", listOf("john"))
            val messageFilter: FilterObject = Filters.autocomplete("text", query)

            client.searchMessages(
                SearchMessagesRequest(
                    offset,
                    limit,
                    channelFilter,
                    messageFilter
                )
            ).enqueue {
                if (it.isSuccess) {
                    val messages: List<Message> = it.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
                }
            }
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
            val message = Message("my message", pinned = true, pinExpires = pinExpirationDate)
            channelClient.sendMessage(message).enqueue { result ->
                if (result.isSuccess) {
                    val sentMessage = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()))
                }
            }

            // Unpin message
            channelClient.unpinMessage(message).enqueue { result ->
                if (result.isSuccess) {
                    val unpinnedMessage = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()))
                }
            }

            // Pin message for 120 seconds
            channelClient.pinMessage(message, 120).enqueue { result ->
                if (result.isSuccess) {
                    val pinnedMessage = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()))
                }
            }

            // Change message expiration to 2077
            channelClient.pinMessage(message, pinExpirationDate).enqueue { result ->
                if (result.isSuccess) {
                    val pinnedMessage = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()))
                }
            }

            // Remove expiration date from pinned message
            channelClient.pinMessage(message, null).enqueue { result ->
                if (result.isSuccess) {
                    val pinnedMessage = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()))
                }
            }
        }

        fun retrievePinnedMessages() {
            val request = QueryChannelRequest().withMessages(limit = 10)
            channelClient.query(request).enqueue { result ->
                if (result.isSuccess) {
                    val pinnedMessages = result.data().pinnedMessages
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()))
                }
            }
        }

        fun searchForAllPinnedMessages() {
            val channelFilter = Filters.`in`("cid", "channelType:channelId")
            val messageFilter = Filters.eq("pinned", true)
            val request = SearchMessagesRequest(offset = 0, limit = 30, channelFilter, messageFilter)

            client.searchMessages(request).enqueue { result ->
                if (result.isSuccess) {
                    val pinnedMessages = result.data()
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error()))
                }
            }
        }
    }
}
