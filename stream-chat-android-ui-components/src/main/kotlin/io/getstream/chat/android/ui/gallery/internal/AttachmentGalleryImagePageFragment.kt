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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.images.load
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentGalleryImageBinding
import io.getstream.chat.android.ui.gallery.AttachmentGalleryViewMediaStyle

internal class AttachmentGalleryImagePageFragment : Fragment() {

    private var _binding: StreamUiItemAttachmentGalleryImageBinding? = null
    private val binding get() = _binding!!

    /**
     * Holds the style necessary to stylize the play button.
     *
     * Fetching the style depends on [Context] so use this property
     * only after it has been obtained during or after [onAttach].
     */
    private val style by lazy {
        AttachmentGalleryViewMediaStyle(
            context = requireContext(),
            attrs = null
        )
    }

    private val imageUrl: String? by lazy {
        requireArguments().getString(ARG_IMAGE_URL)
    }

    private var imageClickListener: () -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return StreamUiItemAttachmentGalleryImageBinding.inflate(inflater)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.photoView) {
            load(
                data = imageUrl,
                onStart = { binding.progressBar.visibility = View.VISIBLE },
                onComplete = { binding.progressBar.visibility = View.GONE }
            )

            setOnClickListener {
                imageClickListener()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_IMAGE_URL = "image_url"

        fun create(attachment: Attachment, imageClickListener: () -> Unit = {}): Fragment {
            return AttachmentGalleryImagePageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_IMAGE_URL, attachment.imageUrl)
                }
                this.imageClickListener = imageClickListener
            }
        }
    }
}
