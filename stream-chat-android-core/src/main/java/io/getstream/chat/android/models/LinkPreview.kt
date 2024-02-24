package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * A data class that represents a link preview.
 *
 * @property originUrl The original URL of the link.
 * @property attachment The attachment that represents the link preview.
 */
@Immutable
public data class LinkPreview(
    val originUrl: String,
    val attachment: Attachment
) {

    public companion object {
        /**
         * An empty [LinkPreview].
         */
        public val EMPTY: LinkPreview = LinkPreview(originUrl = "", attachment = Attachment())
    }

}
