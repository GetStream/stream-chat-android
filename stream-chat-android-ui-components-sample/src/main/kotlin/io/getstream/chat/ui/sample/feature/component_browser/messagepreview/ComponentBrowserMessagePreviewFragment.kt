package io.getstream.chat.ui.sample.feature.component_browser.messagepreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessagePreviewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser
import java.util.Date

class ComponentBrowserMessagePreviewFragment : Fragment() {

    private var _binding: FragmentComponentBrowserMessagePreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComponentBrowserMessagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messagePreviewViewDirectMessage.setMessage(
            Message(
                id = "",
                user = randomUser(),
                createdAt = Date(120, 7, 15, 14, 22),
                text = "Hello world, how are you doing?",
                channel = Channel(
                    memberCount = 2,
                ).apply {
                    name = "Direct Message"
                }
            )
        )

        binding.messagePreviewViewInChannel.setMessage(
            Message(
                id = "",
                user = randomUser(),
                createdAt = Date(120, 7, 15, 14, 22),
                text = "Hello world, how are you doing?",
                channel = Channel(
                    memberCount = 3,
                ).apply {
                    name = "General"
                }
            )
        )

        binding.messagePreviewViewWithImageAttachment.setMessage(
            Message(
                id = "",
                user = randomUser(),
                createdAt = Date(120, 7, 15, 14, 22),
                text = "Hello world, how are you doing?",
                attachments = mutableListOf(
                    Attachment(
                        type = "image",
                        name = "DSC_20201125.jpg",
                    ),
                ),
            )
        )

        binding.messagePreviewViewWithFileAttachment.setMessage(
            Message(
                id = "",
                user = randomUser(),
                createdAt = Date(120, 7, 15, 14, 22),
                text = "Hello world, how are you doing?",
                attachments = mutableListOf(
                    Attachment(
                        type = "file",
                        name = "my-important-document.pdf",
                        mimeType = "image/jpeg"
                    ),
                ),
            )
        )

        binding.messagePreviewViewWithMentions.setMessage(
            Message(
                id = "",
                user = randomUser(),
                createdAt = Date(120, 7, 15, 14, 22),
                text = "Hey @James, what's @James up to?",
            ),
            currentUserMention = "@James"
        )

        binding.messagePreviewViewWithLongText.setMessage(
            Message(
                id = "",
                user = randomUser().apply {
                    name = "Mr Someone Longname"
                },
                createdAt = Date(120, 7, 15, 14, 22),
                text = "This is a really long message which will not fit on a single line in the preview",
                channel = Channel(
                    memberCount = 3,
                ).apply {
                    name = "General Team Chat"
                }
            )
        )
    }
}
