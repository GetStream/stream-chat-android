package io.getstream.chat.ui.sample.feature.component_browser.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessageReplyViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomImageUrl
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser

class MessageReplyViewComponentBrowserFragment : Fragment() {
    private var _binding: FragmentComponentBrowserMessageReplyViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentComponentBrowserMessageReplyViewBinding
            .inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @InternalStreamChatApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textReply.setMessage(
            Message(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                user = randomUser()
            ),
            false
        )
        binding.textReplyMine.setMessage(
            Message(
                text = "Lorem ipsum dolor",
                user = randomUser()
            ),
            false
        )
        binding.textReplyTheirs.setMessage(
            Message(
                text = "Lorem ipsum dolor",
                user = randomUser()
            ),
            true
        )
        binding.photoReply.setMessage(
            Message(
                user = randomUser(),
                attachments = mutableListOf(
                    Attachment(
                        title = "Image attachment",
                        type = ModelType.attach_image,
                        imageUrl = randomImageUrl()
                    )
                )
            ),
            false
        )
        binding.videoReply.setMessage(
            Message(
                user = randomUser(),
                attachments = mutableListOf(
                    Attachment(
                        title = "Video attachment",
                        type = ModelType.attach_video,
                        thumbUrl = randomImageUrl()
                    )
                )
            ),
            false
        )
        binding.linkReply.setMessage(
            Message(
                user = randomUser(),
                attachments = mutableListOf(
                    Attachment(
                        type = ModelType.attach_link,
                        image = randomImageUrl(),
                        ogUrl = "https://google.com"
                    )
                )
            ),
            true
        )
        binding.fileReply.setMessage(
            Message(
                user = randomUser(),
                attachments = mutableListOf(
                    Attachment(
                        mimeType = ModelType.attach_mime_ppt,
                        title = "attachment.ppt",
                        type = ModelType.attach_file,
                        imageUrl = randomImageUrl()
                    )
                )
            ),
            false
        )
    }
}
