package io.getstream.chat.docs.kotlin

import android.util.Log
import io.getstream.chat.android.client.ChatClient
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

class Messages(
    val client: ChatClient,
    val channelController: ChannelClient,
    val message: Message,
    val parentMessage: Message
) {

    fun sendAMessage() {
        // create a message
        val message = Message()
        message.text =
            "Josh I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish."
        message.extraData["anotherCustomField"] = 234

        // add an image attachment to the message
        val attachment = Attachment()
        attachment.type = "image"
        attachment.imageUrl = "https://bit.ly/2K74TaG"
        attachment.fallback = "test image"
        // add some custom data to the attachment
        attachment.extraData["myCustomField"] = 123

        message.attachments.add(attachment)

        // include the user id of the mentioned user
        message.mentionedUsers.add(User("josh-id"))

        // send the message to the channel
        channelController.sendMessage(message).enqueue {
            if (it.isSuccess) {
                val sentMessage = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

    fun getAMessage() {
        channelController.getMessage("message-id").enqueue {
            if (it.isSuccess) {
                val message = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

    fun updateAMessage() {
        // update some field of the message
        message.text = "my updated text"

        // send the message to the channel
        channelController.updateMessage(message).enqueue {
            if (it.isSuccess) {
                val updatedMessage = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

    fun deleteAMessage() {
        channelController.deleteMessage("message-id").enqueue {
            if (it.isSuccess) {
                val deletedMessage = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

    fun fileUploads() {
        val imageFile = File("path")
        val anyOtherFile = File("path")

        // upload an image
        channelController.sendImage(
            imageFile,
            object : ProgressCallback {
                override fun onSuccess(file: String) {
                    val fileUrl = file
                }

                override fun onError(error: ChatError) {
                    Log.e(TAG, String.format("There was an error %s", error, error.cause))
                }

                override fun onProgress(progress: Long) {
                    // you can render uploading progress here
                }
            }
        )

        // upload a file
        channelController.sendFile(
            anyOtherFile,
            object : ProgressCallback {
                override fun onSuccess(file: String) {
                    val fileUrl = file
                }

                override fun onError(error: ChatError) {
                    Log.e(TAG, String.format("There was an error %s", error, error.cause))
                }

                override fun onProgress(progress: Long) {
                    // you can render uploading progress here
                }
            }
        )
    }

    fun sendAReaction() {
        val reaction = Reaction("message-id", "like", 1)
        channelController.sendReaction(reaction).enqueue {
            if (it.isSuccess) {
                val sentReaction = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

    fun removeAReaction() {
        channelController.deleteReaction("message-id", "like").enqueue {
            if (it.isSuccess) {
                val message = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

    fun paginatingReactions() {
        // get the first 10 reactions
        channelController.getReactions("message-id", 0, 10).enqueue {
            if (it.isSuccess) {
                val reactions = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }

        // get the second 10 reactions
        channelController.getReactions("message-id", 10, 10).enqueue {
            if (it.isSuccess) {
                val reactions = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }

        // get 10 reactions after particular reaction
        val reactionId = "reaction-id"
        channelController.getReactions("message-id", reactionId, 10).enqueue {
            if (it.isSuccess) {
                val reactions = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

    fun cumulativeReactions() {
        val score = 5
        val reaction = Reaction("message-id", "like", score)
        channelController.sendReaction(reaction).enqueue {
            if (it.isSuccess) {
                val sentReaction = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

    fun startAThread() {
        // set the parent id to make sure a message shows up in a thread
        val message = Message()
        message.text = "hello world"
        message.parentId = parentMessage.id

        // send the message to the channel
        channelController.sendMessage(message).enqueue {
            if (it.isSuccess) {
                val sentMessage = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

    fun threadPagination() {
        val limit = 20
        // retrieve the first 20 messages inside the thread
        client.getReplies(parentMessage.id, limit).enqueue {
            if (it.isSuccess) {
                val replies = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }

        // retrieve the 20 more messages before the message with id "42"
        client.getRepliesMore(parentMessage.id, "42", limit).enqueue {
            if (it.isSuccess) {
                val replies = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

    fun silentMessage() {
        val message = Message("text-of-a-message", silent = true)
        channelController.sendMessage(message).enqueue {
            if (it.isSuccess) {
                val sentMessage = it.data()
            } else {
                Log.e(TAG, String.format("There was an error %s", it.error(), it.error().cause))
            }
        }
    }

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
