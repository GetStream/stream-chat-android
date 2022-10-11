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

package io.getstream.chat.android.ui.gallery.internal

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentGalleryVideoBinding

internal class AttachmentGalleryVideoPageFragment : Fragment() {

    private var _binding: StreamUiItemAttachmentGalleryVideoBinding? = null
    private val binding get() = _binding!!

    private val thumbUrl: String? by lazy {
        requireArguments().getString(ARG_THUMB_URL)
    }
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
        loadVideo()
    }

    private fun loadVideo() {
        binding.videoView.apply {
            setVideoURI(Uri.parse(assetUrl))
            this.setMediaController(mediaController)
            setOnErrorListener { _, _, _ ->
                // TODO
                true
            }
            setOnPreparedListener {
                // TODO fill in when there's a thumbnail preview
            }

            mediaController.setAnchorView(binding.videoView)

            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.CENTER
            }
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
