package io.getstream.chat.android.client.parser

import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.CustomObject
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.adapters.CustomObjectGsonAdapter
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Ignore
import org.junit.Test
import kotlin.collections.set

internal class CustomObjectParsingTests {

    val parser = GsonChatParser()

    val gson = SkipExtraDataGson()
    val typeAdapterFactory = TypeAdapterFactory()

    val customObjectImplementations = listOf(
        Message::class.java,
        User::class.java,
        Reaction::class.java,
        Channel::class.java,
        User::class.java,
        Attachment::class.java
    )

    @Test
    fun nullValuesRestoration() {
        val json =
            "{latest_reactions:null,silent:null,attachments:null,cid:null,reaction_counts:null, reply_count:null}"
        val message = parser.fromJson(json, Message::class.java)

        assertThat(message.latestReactions).isEmpty()
        assertThat(message.reactionCounts).isEmpty()
        assertThat(message.attachments).isEmpty()
        assertThat(message.cid).isEmpty()
        assertThat(message.silent).isFalse()

        message.reactionCounts["like"] = 1
        message.latestReactions.add(Reaction("x"))
        message.attachments.add(Attachment("author"))

        assertThat(message.reactionCounts).isEqualTo(mapOf("like" to 1))
        assertThat(message.latestReactions).isEqualTo(listOf(Reaction("x")))
        assertThat(message.attachments).isEqualTo(listOf(Attachment("author")))
    }

    @Test
    fun verifyAdapter() {
        customObjectImplementations.forEach { clazz ->
            verifyAdapter(clazz)
        }
    }

    @Test
    fun verifyCustomDataReadAndWrite() {
        verifyAllImplementations { obj -> verifyCustomDataReadAndWrite(obj) }
    }

    @Test
    fun verifyRawJsonWrite() {
        verifyAllImplementations { obj -> verifyRawJson(obj) }
    }

    /**
     * Disabled for now since duplicate field message.mentioned_users was added
     */
    @Ignore
    @Test
    fun verifyCollectionParsing() {
        val id = "message-id"
        val reactionId = "like"
        val reactionScore = 13
        val inputMessage = Message().apply {
            this.id = id
            this.latestReactions.add(Reaction(id))
            this.reactionScores[reactionId] = reactionScore
        }

        val outputMessage = convert(inputMessage, Message::class.java)

        assertThat(inputMessage.id).isEqualTo(outputMessage.id)
        assertThat(inputMessage.latestReactions).isEqualTo(outputMessage.latestReactions)
        assertThat(inputMessage.reactionScores).isEqualTo(outputMessage.reactionScores)
    }

    private fun <T : Any> convert(obj: Any, clazz: Class<T>): T {
        val jsonMessage = gson.toJson(obj)
        return parser.fromJson(jsonMessage, clazz)
    }

    private fun verifyRawJson(customObject: CustomObject) {

        val keyA = "key-a"
        val valueA = "value-a"
        val keyB = "key-b"
        val valueB = "value-b"

        customObject.putExtraValue(keyA, valueA)
        customObject.putExtraValue(keyB, valueB)

        val rawJson = parser.toJson(customObject)
        val jsonObject = JSONObject(rawJson)

        assertThat(jsonObject.getString(keyA)).isEqualTo(valueA)
        assertThat(jsonObject.getString(keyB)).isEqualTo(valueB)
    }

    private fun verifyCustomDataReadAndWrite(customObject: CustomObject) {

        val key = "key"
        val value = "value"
        val json = parser.toJson(
            customObject.apply {
                extraData[key] = value
            }
        )

        val obj = parser.fromJson(json, customObject::class.java)
        assertThat(obj.extraData).isEqualTo(mapOf(Pair(key, value)))
    }

    private fun verifyAdapter(clazz: Class<*>) {
        val adapter = typeAdapterFactory.create(gson.instance, TypeToken.get(clazz))
        assertThat(adapter).isInstanceOf(CustomObjectGsonAdapter::class.java)
    }

    private fun verifyAllImplementations(verify: (CustomObject) -> Unit) {
        customObjectImplementations.forEach { clazz ->
            var verified = false
            clazz.constructors.forEach { constructor ->
                if (constructor.parameters.isEmpty()) {
                    val instance = constructor.newInstance()
                    verify(instance as CustomObject)
                    verified = true
                }
            }

            if (!verified) {
                throw RuntimeException("No default(empty) constructor for custom object: $clazz")
            }
        }
    }
}
