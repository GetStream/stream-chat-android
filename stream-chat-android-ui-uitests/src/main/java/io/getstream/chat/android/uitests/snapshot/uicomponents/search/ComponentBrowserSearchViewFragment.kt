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

package io.getstream.chat.android.uitests.snapshot.uicomponents.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.getstream.chat.android.uitests.databinding.FragmentComponentBrowserSearchViewBinding

class ComponentBrowserSearchViewFragment : Fragment() {

    private val TAG = ComponentBrowserSearchViewFragment::class.simpleName

    private var _binding: FragmentComponentBrowserSearchViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentComponentBrowserSearchViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.apply {
            setDebouncedInputChangedListener { query ->
                Log.d(TAG, "Debounced input: '$query'")
            }
            setSearchStartedListener { query ->
                Log.d(TAG, "Search: '$query'")
                Toast.makeText(this.context, "Search: '$query'", Toast.LENGTH_SHORT).show()
            }
        }

        binding.searchView2.setQuery("Gaming")
    }
}
