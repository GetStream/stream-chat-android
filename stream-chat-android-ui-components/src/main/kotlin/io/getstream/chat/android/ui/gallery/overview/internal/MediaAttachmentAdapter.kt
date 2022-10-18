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

package io.getstream.chat.android.ui.gallery.overview.internal

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMediaAttachmentBinding
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem
import io.getstream.chat.android.ui.gallery.MediaAttachmentGridViewStyle
import io.getstream.chat.android.ui.gallery.options.AttachmentGalleryOptionsViewStyle

internal class MediaAttachmentAdapter(
    private val style: MediaAttachmentGridViewStyle,
    private val mediaAttachmentClickListener: MediaAttachmentClickListener,
) : ListAdapter<AttachmentGalleryItem, MediaAttachmentAdapter.MediaAttachmentViewHolder>(
    AttachmentGalleryItemDiffCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaAttachmentViewHolder {

        return StreamUiItemMediaAttachmentBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let {
                MediaAttachmentViewHolder(
                    binding = it,
                    mediaAttachmentClickListener = mediaAttachmentClickListener,
                    style = style
                )
            }
    }

    override fun onBindViewHolder(holder: MediaAttachmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * The ViewHolder used for displaying media previews.
     *
     * @param binding The binding used to build a UI.
     * @param mediaAttachmentClickListener Click listener used to detect
     * clicks on media attachment previews.
     * @param style Used to change the appearance of the UI.
     */
    class MediaAttachmentViewHolder(
        private val binding: StreamUiItemMediaAttachmentBinding,
        private val mediaAttachmentClickListener: MediaAttachmentClickListener,
        private val style: MediaAttachmentGridViewStyle,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.mediaContainer.setOnClickListener {
                mediaAttachmentClickListener.onMediaAttachmentClick(bindingAdapterPosition)
            }
        }

        fun bind(attachmentGalleryItem: AttachmentGalleryItem) {
            setupUserAvatar(attachmentGalleryItem)
            setupPlayButton(attachmentGalleryItem.attachment.type)
            loadImage(attachmentGalleryItem)
        }

        /**
         * Loads the given image.
         *
         * @param attachmentGalleryItem The attachment to be displayed.
         */
        private fun loadImage(attachmentGalleryItem: AttachmentGalleryItem) {
            val isVideoAttachment = attachmentGalleryItem.attachment.type == ModelType.attach_video
            val shouldLoadImage = attachmentGalleryItem.attachment.type == ModelType.attach_image ||
                (attachmentGalleryItem.attachment.type == ModelType.attach_video && ChatUI.videoThumbnailsEnabled)

            binding.mediaImageView.load(
                data = if (shouldLoadImage) {
                    attachmentGalleryItem.attachment.imagePreviewUrl
                } else {
                    null
                },
                placeholderDrawable = if (!isVideoAttachment) {
                    style.imagePlaceholder
                } else {
                    null
                },
                onStart = { binding.progressBar.visibility = View.VISIBLE },
                onComplete = {
                    binding.playButtonCardView.isVisible =
                        attachmentGalleryItem.attachment.type == ModelType.attach_video
                    binding.progressBar.visibility = View.GONE
                }
            )
        }

        /**
         * Toggles the visibility of user avatars and load the
         * given image.
         *
         * @param attachmentGalleryItem The attachment to be displayed.
         */
        private fun setupUserAvatar(attachmentGalleryItem: AttachmentGalleryItem) {
            val user = attachmentGalleryItem.user

            if (style.showUserAvatars) {
                binding.userAvatarCardView.isVisible = true
                binding.userAvatarView.setUser(user)
            } else {
                binding.userAvatarCardView.isVisible = false
            }
        }

        /**
         * Sets up the play icon overlaid above video attachment previews
         * by pulling relevant values from [AttachmentGalleryOptionsViewStyle].
         **/
        private fun setupPlayButton(attachmentType: String?) {
            if (attachmentType == ModelType.attach_video) {
                setupPlayButtonIcon()
                setupPlayButtonCard()
            }
        }

        /**
         * Sets up the play button icon hosted in an image view.
         */
        private fun setupPlayButtonIcon() {
            with(binding.playButtonImageView) {
                val playVideoDrawable = style.playVideoButtonIcon?.mutate()?.apply {
                    val tintColor = style.playVideoIconTint

                    if (tintColor != null) {
                        this.setTint(tintColor)
                    }
                }

                setImageDrawable(playVideoDrawable)
                setPaddingRelative(
                    style.playVideoIconPaddingStart,
                    style.playVideoIconPaddingTop,
                    style.playVideoIconPaddingEnd,
                    style.playVideoIconPaddingBottom
                )
            }
        }

        /**
         * Sets up the card wrapping the image view that contains the
         * play button icon.
         */
        private fun setupPlayButtonCard() {
            with(binding.playButtonCardView) {
                elevation = style.playVideoIconElevation
                setCardBackgroundColor(style.playVideoIconBackgroundColor)
                radius = style.playVideoIconCornerRadius
            }
        }
    }

    internal fun interface MediaAttachmentClickListener {
        fun onMediaAttachmentClick(position: Int)
    }

    private object AttachmentGalleryItemDiffCallback : DiffUtil.ItemCallback<AttachmentGalleryItem>() {
        override fun areItemsTheSame(oldItem: AttachmentGalleryItem, newItem: AttachmentGalleryItem): Boolean {
            return oldItem.attachment.imagePreviewUrl == newItem.attachment.imagePreviewUrl &&
                oldItem.createdAt == newItem.createdAt
        }

        override fun areContentsTheSame(oldItem: AttachmentGalleryItem, newItem: AttachmentGalleryItem): Boolean {
            return oldItem == newItem
        }
    }
}
