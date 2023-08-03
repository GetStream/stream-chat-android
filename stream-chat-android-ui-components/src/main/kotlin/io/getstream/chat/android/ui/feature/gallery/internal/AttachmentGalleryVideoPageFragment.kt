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

package io.getstream.chat.android.ui.feature.gallery.internal

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentGalleryVideoBinding
import io.getstream.chat.android.ui.feature.gallery.AttachmentGalleryViewMediaStyle
import io.getstream.chat.android.ui.feature.gallery.options.AttachmentGalleryOptionsViewStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.load

internal class AttachmentGalleryVideoPageFragment : Fragment() {

    private var _binding: StreamUiItemAttachmentGalleryVideoBinding? = null
    private val binding get() = _binding!!

    /**
     * Holds the style necessary to stylize the play button.
     *
     * Fetching the style depends on [Context] so use this property
     * only after it has been obtained during or after [onAttach].
     */
    private val style by lazy {
        AttachmentGalleryViewMediaStyle(
            context = requireContext().createStreamThemeWrapper(),
            attrs = null,
        )
    }

    /**
     * If the video has been prepared and the video player
     * is ready for playback.
     *
     * VideoView does not expose state so we use these to track
     * state externally.
     */
    private var playbackPrepared = false
        set(value) {
            field = value
            if (playbackPrepared && playbackStartRequested) {
                binding.thumbnailImageView.visibility = View.GONE
                binding.playButtonCardView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.thumbnailImageView.visibility = View.VISIBLE
                binding.playButtonCardView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }

    /**
     * If the user has pressed play.
     *
     * VideoView does not expose state so we use these to track
     * state externally.
     */
    private var playbackStartRequested = false
        set(value) {
            field = value
            if (!playbackPrepared && playbackStartRequested) {
                binding.progressBar.visibility = View.VISIBLE
                binding.playButtonCardView.visibility = View.GONE
                binding.thumbnailImageView.visibility = View.GONE
            } else {
                binding.playButtonCardView.visibility = View.GONE
                binding.thumbnailImageView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }

    /**
     * Resets the state and hides the controller.
     *
     * Important for resetting state when paging through
     * fragments in a pager.
     */
    override fun onPause() {
        super.onPause()
        resetState()
        mediaController.hide()
    }

    /**
     * Contains the URL to the thumbnail of the video.
     */
    private val thumbUrl: String? by lazy {
        requireArguments().getString(ARG_THUMB_URL)
    }

    /**
     * Contains the URL necessary to reproduce the video.
     */
    private val assetUrl: String? by lazy {
        requireArguments().getString(ARG_ASSET_URL)
    }

    private val mediaController: MediaController by lazy {
        createMediaController(requireContext())
    }

    private var imageClickListener: () -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return StreamUiItemAttachmentGalleryVideoBinding.inflate(inflater)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVideoThumbnail()
        setupPlayButton()
        loadVideo()
    }

    private fun setupVideoThumbnail() {
        if (ChatUI.videoThumbnailsEnabled) {
            binding.thumbnailImageView.load(data = thumbUrl)
        }
    }

    /**
     * Resets this video page's state.
     */
    private fun resetState() {
        playbackStartRequested = false
        binding.thumbnailImageView.visibility = View.VISIBLE
        binding.playButtonCardView.visibility = View.VISIBLE
    }

    /**
     * Sets up the play icon overlaid above video attachment previews
     * by pulling relevant values from [AttachmentGalleryOptionsViewStyle].
     **/
    private fun setupPlayButton() {
        setupPlayButtonIcon()
        setupPlayButtonCard()
        setupOnPlayButtonClickedListener()
    }

    /**
     * Sets up the play button icon hosted in an image view.
     */
    private fun setupPlayButtonIcon() {
        with(binding.playButtonImageView) {
            updateLayoutParams {
                width = style.viewMediaPlayVideoIconWidth
                height = style.viewMediaPlayVideoIconHeight
            }

            val playVideoDrawable = style.viewMediaPlayVideoButtonIcon?.mutate()?.apply {
                val tintColor = style.viewMediaPlayVideoIconTint

                if (tintColor != null) {
                    this.setTint(tintColor)
                }
            }

            setImageDrawable(playVideoDrawable)
            setPaddingRelative(
                style.viewMediaPlayVideoIconPaddingStart,
                style.viewMediaPlayVideoIconPaddingTop,
                style.viewMediaPlayVideoIconPaddingEnd,
                style.viewMediaPlayVideoIconPaddingBottom,
            )
        }
    }

    /**
     * Sets up the card wrapping the image view that contains the
     * play button icon.
     */
    private fun setupPlayButtonCard() {
        with(binding.playButtonCardView) {
            elevation = style.viewMediaPlayVideoIconElevation
            setCardBackgroundColor(style.viewMediaPlayVideoIconBackgroundColor)
            radius = style.viewMediaPlayVideoIconCornerRadius
        }
    }

    private fun setupOnPlayButtonClickedListener() {
        binding.playButtonCardView.setOnClickListener {
            binding.videoView.start()
            playbackStartRequested = true
        }
    }

    private fun loadVideo() {
        binding.videoView.apply {
            setVideoURI(Uri.parse(assetUrl))
            this.setMediaController(mediaController)
            setOnErrorListener { _, _, _ ->
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.stream_ui_attachment_gallery_video_display_error),
                    Toast.LENGTH_SHORT,
                ).show()
                true
            }
            setOnPreparedListener {
                playbackPrepared = true
            }

            mediaController.setAnchorView(binding.root)
        }
    }

    /**
     * Creates a custom instance of [MediaController].
     *
     * @param context The Context used to create the [MediaController].
     */
    private fun createMediaController(
        context: Context,
    ): MediaController {
        return object : MediaController(context) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_THUMB_URL = "thumb_url"
        private const val ARG_ASSET_URL = "asset_url"

        fun create(attachment: Attachment, imageClickListener: () -> Unit = {}): Fragment {
            return AttachmentGalleryVideoPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_THUMB_URL, attachment.thumbUrl)
                    putString(ARG_ASSET_URL, attachment.assetUrl)
                }
                this.imageClickListener = imageClickListener
            }
        }
    }
}
