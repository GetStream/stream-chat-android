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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.images.load
import io.getstream.chat.android.ui.databinding.StreamUiItemImageGalleryBinding

internal class AttachmentGalleryPageFragment : Fragment() {

    private var _binding: StreamUiItemImageGalleryBinding? = null
    private val binding get() = _binding!!

    private val imageUrl: String by lazy {
        requireNotNull(requireArguments().getString(ARG_IMAGE_URL)) { "Image URL must not be null" }
    }

    private var imageClickListener: () -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return StreamUiItemImageGalleryBinding.inflate(inflater)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.photoView) {
            load(data = imageUrl)
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

        fun create(imageUrl: String, imageClickListener: () -> Unit = {}): Fragment {
            return AttachmentGalleryPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_IMAGE_URL, imageUrl)
                }
                this.imageClickListener = imageClickListener
            }
        }
    }
}
