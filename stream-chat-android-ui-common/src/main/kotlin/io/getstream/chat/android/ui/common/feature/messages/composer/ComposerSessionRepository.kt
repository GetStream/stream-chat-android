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

package io.getstream.chat.android.ui.common.feature.messages.composer

import androidx.lifecycle.SavedStateHandle
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists and restores the message composer session across process death.
 *
 * Stores both the picker-selected attachments and the current edit-mode state as a single
 * JSON string under one key in the provided [SavedStateHandle].
 */
internal class ComposerSessionRepository(private val savedStateHandle: SavedStateHandle) {

    /**
     * Snapshot of the edit-mode state: the message being edited and its base attachments.
     *
     * @param message The message currently being edited.
     * @param attachments The original attachments of [message].
     */
    data class EditMode(val message: Message, val attachments: List<Attachment>)

    /**
     * Saves the current composer session to the [SavedStateHandle].
     *
     * Removes the persisted entry when both [selectedAttachments] is empty and [editMode] is null.
     *
     * @param selectedAttachments Picker selections keyed by source URI.
     * @param editMode The current edit-mode state, or `null` if not editing.
     */
    fun save(selectedAttachments: List<Attachment>, editMode: EditMode?) {
        if (selectedAttachments.isEmpty() && editMode == null) {
            savedStateHandle.remove<String>(KEY_SESSION)
            return
        }
        val json = JSONObject()
        json.put(KEY_SELECTED_ATTACHMENTS, JSONArray(selectedAttachments.map(::attachmentToJson)))
        editMode?.let { mode ->
            json.put(KEY_EDIT_MESSAGE, messageToJson(mode.message))
            json.put(KEY_EDIT_ATTACHMENTS, JSONArray(mode.attachments.map(::attachmentToJson)))
        }
        savedStateHandle[KEY_SESSION] = json.toString()
    }

    /**
     * Restores the picker-selected attachments from the [SavedStateHandle].
     *
     * @return The list of previously selected attachments, or an empty list if none were persisted.
     */
    fun restoreSelectedAttachments(): List<Attachment> =
        sessionJson()?.optJSONArray(KEY_SELECTED_ATTACHMENTS)?.toAttachmentList() ?: emptyList()

    /**
     * Restores the edit-mode state from the [SavedStateHandle].
     *
     * @return The previously persisted [EditMode], or `null` if no edit-mode state was saved.
     */
    fun restoreEditMode(): EditMode? {
        val json = sessionJson() ?: return null
        val message = json.optJSONObject(KEY_EDIT_MESSAGE)?.let(::jsonToMessage) ?: return null
        val attachments = json.optJSONArray(KEY_EDIT_ATTACHMENTS)?.toAttachmentList() ?: emptyList()
        return EditMode(message, attachments)
    }

    private fun sessionJson(): JSONObject? {
        val str = savedStateHandle.get<String>(KEY_SESSION) ?: return null
        return try { JSONObject(str) } catch (_: Exception) { null }
    }

    private fun JSONArray.toAttachmentList(): List<Attachment> =
        (0 until length()).mapNotNull { jsonToAttachment(getJSONObject(it)) }

    private fun attachmentToJson(attachment: Attachment): JSONObject = JSONObject().apply {
        attachment.sourceUriString()?.let { put(KEY_URI, it) }
        attachment.type?.let { put(KEY_TYPE, it) }
        put(KEY_NAME, attachment.name)
        put(KEY_FILE_SIZE, attachment.fileSize)
        attachment.mimeType?.let { put(KEY_MIME_TYPE, it) }
        attachment.assetUrl?.let { put(KEY_ASSET_URL, it) }
        attachment.imageUrl?.let { put(KEY_IMAGE_URL, it) }
        attachment.thumbUrl?.let { put(KEY_THUMB_URL, it) }
        attachment.title?.let { put(KEY_TITLE, it) }
        extraDataToJson(attachment.extraData)?.let { put(KEY_EXTRA_DATA, it) }
    }

    private fun jsonToAttachment(json: JSONObject): Attachment? {
        val uri = json.optString(KEY_URI).takeIf(String::isNotEmpty)
        val name = json.optString(KEY_NAME).takeIf(String::isNotEmpty)
        val assetUrl = json.optString(KEY_ASSET_URL).takeIf(String::isNotEmpty)
        val imageUrl = json.optString(KEY_IMAGE_URL).takeIf(String::isNotEmpty)
        val thumbUrl = json.optString(KEY_THUMB_URL).takeIf(String::isNotEmpty)
        val hasIdentifier = uri != null || name != null || assetUrl != null || imageUrl != null || thumbUrl != null
        if (!hasIdentifier) return null
        val extraData = buildMap {
            json.optJSONObject(KEY_EXTRA_DATA)?.let { obj ->
                for (key in obj.keys()) put(key, obj.get(key))
            }
            if (uri != null) put(EXTRA_SOURCE_URI, uri)
        }
        return Attachment(
            type = json.optString(KEY_TYPE).takeIf(String::isNotEmpty),
            name = json.optString(KEY_NAME),
            fileSize = json.optInt(KEY_FILE_SIZE),
            mimeType = json.optString(KEY_MIME_TYPE).takeIf(String::isNotEmpty),
            assetUrl = assetUrl,
            imageUrl = imageUrl,
            thumbUrl = thumbUrl,
            title = json.optString(KEY_TITLE).takeIf(String::isNotEmpty),
            extraData = extraData,
        )
    }

    private fun messageToJson(message: Message): JSONObject = JSONObject().apply {
        put(KEY_MSG_ID, message.id)
        put(KEY_MSG_CID, message.cid)
        put(KEY_MSG_TEXT, message.text)
        message.parentId?.let { put(KEY_MSG_PARENT_ID, it) }
        put(KEY_MSG_TYPE, message.type)
    }

    private fun jsonToMessage(json: JSONObject): Message? {
        val id = json.optString(KEY_MSG_ID).takeIf(String::isNotEmpty) ?: return null
        val cid = json.optString(KEY_MSG_CID).takeIf(String::isNotEmpty) ?: return null
        return Message(
            id = id,
            cid = cid,
            text = json.optString(KEY_MSG_TEXT),
            parentId = json.optString(KEY_MSG_PARENT_ID).takeIf(String::isNotEmpty),
            type = json.optString(KEY_MSG_TYPE),
        )
    }

    /**
     * Serializes [extraData] to a [JSONObject], excluding [EXTRA_SOURCE_URI] (stored separately).
     *
     * Only primitive types (String, Boolean, Int, Long, Double, Float) are included.
     * Non-serializable values are silently skipped. Note that [Float] values are promoted to
     * [Double] during serialization because JSON has no single-precision type; callers should
     * read restored values as [Number] rather than casting directly to [Float].
     *
     * @return A [JSONObject], or `null` if there are no serializable entries.
     */
    private fun extraDataToJson(extraData: Map<String, Any>): JSONObject? {
        val json = JSONObject()
        for ((key, value) in extraData) {
            if (key == EXTRA_SOURCE_URI) continue
            when (value) {
                is String -> json.put(key, value)
                is Boolean -> json.put(key, value)
                is Int -> json.put(key, value)
                is Long -> json.put(key, value)
                is Double -> json.put(key, value)
                is Float -> json.put(key, value.toDouble())
            }
        }
        return if (json.length() > 0) json else null
    }

    private fun Attachment.sourceUriString(): String? = extraData[EXTRA_SOURCE_URI]?.toString()
}

private const val KEY_SESSION = "stream_composer_session"
private const val KEY_SELECTED_ATTACHMENTS = "stream_composer_selected_attachments"
private const val KEY_EDIT_MESSAGE = "stream_composer_edit_message"
private const val KEY_EDIT_ATTACHMENTS = "stream_composer_edit_attachments"

private const val KEY_URI = "uri"
private const val KEY_TYPE = "type"
private const val KEY_NAME = "name"
private const val KEY_FILE_SIZE = "fileSize"
private const val KEY_MIME_TYPE = "mimeType"
private const val KEY_ASSET_URL = "assetUrl"
private const val KEY_IMAGE_URL = "imageUrl"
private const val KEY_THUMB_URL = "thumbUrl"
private const val KEY_TITLE = "title"
private const val KEY_EXTRA_DATA = "extraData"
private const val KEY_MSG_ID = "msgId"
private const val KEY_MSG_CID = "msgCid"
private const val KEY_MSG_TEXT = "msgText"
private const val KEY_MSG_PARENT_ID = "msgParentId"
private const val KEY_MSG_TYPE = "msgType"
