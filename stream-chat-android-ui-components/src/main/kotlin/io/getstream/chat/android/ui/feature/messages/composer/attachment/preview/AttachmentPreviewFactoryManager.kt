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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.preview

import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.AttachmentPreviewFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.AudioRecordAttachmentPreviewFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.FallbackAttachmentPreviewFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.FileAttachmentPreviewFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.MediaAttachmentPreviewFactory
import io.getstream.log.taggedLogger

/**
 * A manager for registered attachment preview factories.
 *
 * @param attachmentPreviewFactories The list of ViewHolder factories for attachment preview items.
 * @param fallbackAttachmentPreviewFactory The fallback factory that will be used in case there are
 * no other factories that can handle the attachment.
 */
public class AttachmentPreviewFactoryManager @JvmOverloads constructor(
    attachmentPreviewFactories: List<AttachmentPreviewFactory> = listOf(
        MediaAttachmentPreviewFactory(),
        AudioRecordAttachmentPreviewFactory(),
        FileAttachmentPreviewFactory(),
    ),
    private val fallbackAttachmentPreviewFactory: FallbackAttachmentPreviewFactory = FallbackAttachmentPreviewFactory(),
) {

    private val logger by taggedLogger("AttachPreviewFM")

    private val viewTypeToFactoryMapping = SparseArrayCompat<AttachmentPreviewFactory>()

    init {
        for (i in attachmentPreviewFactories.indices) {
            viewTypeToFactoryMapping.put(i, attachmentPreviewFactories[i])
        }
    }

    /**
     * Creates and instantiates a new instance of [AttachmentPreviewViewHolder].
     *
     * @param parentView The parent container.
     * @param attachmentRemovalListener Click listener for the remove attachment button.
     * @param style Used to style the various factories. If null, the respective factory will retain
     * the default appearance.
     *
     * @return An instance of attachment preview ViewHolder.
     */
    public fun onCreateViewHolder(
        parentView: ViewGroup,
        viewType: Int,
        attachmentRemovalListener: (Attachment) -> Unit,
        style: MessageComposerViewStyle? = null,
    ): AttachmentPreviewViewHolder = viewTypeToFactoryMapping.get(viewType, fallbackAttachmentPreviewFactory)
        .onCreateViewHolder(
            parentView = parentView,
            attachmentRemovalListener = attachmentRemovalListener,
            style = style,
        )

    /**
     * Finds the first factory that is capable of displaying the given attachment
     * and return a view type associated with the factory.
     *
     * @param attachment The attachment to display.
     */
    public fun getItemViewType(attachment: Attachment): Int {
        for (i in 0 until viewTypeToFactoryMapping.size()) {
            val factory = viewTypeToFactoryMapping.valueAt(i)
            logger.w { "[getItemViewType] i: $i, factory: $factory" }
            if (factory.canHandle(attachment)) {
                return viewTypeToFactoryMapping.keyAt(i)
            }
        }
        return FALLBACK_FACTORY_VIEW_TYPE
    }

    private companion object {
        /**
         * An arbitrary view type value for the fallback factory.
         */
        private const val FALLBACK_FACTORY_VIEW_TYPE = 100
    }
}
