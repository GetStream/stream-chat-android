package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder

class OnlyAttachmentsMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun createAdapter(): RecyclerView.Adapter<*> {
        return DefaultAdapter(
            getDummyDeletedMessagesList(),
            ::OnlyMediaAttachmentsViewHolder,
            OnlyMediaAttachmentsViewHolder::bind
        )
    }

    private fun getDummyDeletedMessagesList(): List<MessageListItem.MessageItem> {
        return listOf(
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(Attachment(imageUrl = "https://us-east.stream-io-cdn.com/47574/images/8afae93f-6470-4850-993b-af23d5879244.STREAM_20201203_113150412_PXL_20201203_0828441.jpg?ro=0&Expires=1608286584&Signature=pzWyMXJ~JNgf9VmEpMU5KUkFELFEPZ~dIoIk2sP1DD1uBFYqO2zg8o~e-7xmNWF1eBF8tekrn8ffI5CyZMqTyREMJ1TLqwgUNdkJtkqIMiGf0pIBeKA0jJdcS9ozkIct9l4wx9K31g3jX~Ik5sl3mLd3K~F70Hg1Fe02xaDIucf0s~7Iunda77D~0kOVE-9EsLJtzJ0~BYM6YBJCPGWICsfjuvCXF4dIloo66UFwCdYvsjayRoYolpFyh-pPCWA2Q5jlxU6IIQKjDSU4~nBWrn0qpengHOIr-GIFylysOSAe823PAsYfI7twLyea5~OQoKeK0XEcEMLDC3jeCrCx6A__&Key-Pair-Id=APKAIHG36VEWPDULE23Q"))),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(Attachment(imageUrl = "https://us-east.stream-io-cdn.com/47574/images/d3f5002d-bb94-458c-b060-ace57220c87e.STREAM_20201203_113150485_PXL_20201203_0828395.jpg?ro=0&Expires=1608286584&Signature=GuQDwzxBK18Oy1EflSAWpfj7AzMrElC6YXWgY1DjH2VXmjl6YuDfODHMbq59F~037YPeDRLaieeXtNKjtGscvO1l0~Cd5BtwO88jf5hx5fEu-dOzZVkf9F3NMUNMtvPU642S2K9dZTaoSSxMN-I8ha5fkvyu8akKCozv6LZJq3Xqp~LGtRKsS49uBseaX9Zc5vvdSFwaHMkIzw8gbTAHemu6r3e0dRtI~9J2o65xUTHsBOvd8jJULfSSt9943m38T0lxF91zk9O-WjTIbY1jVLDLzBCWqvcURzOxqOvJgF5jZ5XiyjLbLSWKg2l5x7RVpcl1dmadQ39xK3hLjJs9Fg__&Key-Pair-Id=APKAIHG36VEWPDULE23Q"))),
                positions = listOf(MessageListItem.Position.MIDDLE),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(Attachment(imageUrl = "https://us-east.stream-io-cdn.com/47574/images/c97b03b0-e4f2-40d5-aa08-2abc8b32b542.STREAM_20201203_113150589_PXL_20201203_0828329.jpg?ro=0&Expires=1608286584&Signature=d93NtYyanu2anS2MjxSoxTfhI4ESxLYoPR5kUVMSm2fJju0y~bVsEJQ-NGPYmyZdLMPMeGRZspzcnOjmF4gXVts42JiKgI2cl9nZ20ySwSohGaVNr6qr6BdgCu79~q4qWUIv2pS6CgJDNbClIlXIRams33A~ZvUTxVgs7TLewJb5dgLm6dmJKCcgV5PL0jbDSIdLzKM7ephxGnJ09JLwDadOQvsmXRbE1s5EJd6V4c~i1Lr6BXtlrUVViaWni7dKTMXdOpx3PXAw~WPd~EkNUm1KwmtXU42s~xawarHLRKd9x2YViSACNQkHv8gpC8cEwJvSenQw8-V0fUN3FDUlZw__&Key-Pair-Id=APKAIHG36VEWPDULE23Q"))),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(Attachment(imageUrl = "https://us-east.stream-io-cdn.com/47574/images/8afae93f-6470-4850-993b-af23d5879244.STREAM_20201203_113150412_PXL_20201203_0828441.jpg?ro=0&Expires=1608286584&Signature=pzWyMXJ~JNgf9VmEpMU5KUkFELFEPZ~dIoIk2sP1DD1uBFYqO2zg8o~e-7xmNWF1eBF8tekrn8ffI5CyZMqTyREMJ1TLqwgUNdkJtkqIMiGf0pIBeKA0jJdcS9ozkIct9l4wx9K31g3jX~Ik5sl3mLd3K~F70Hg1Fe02xaDIucf0s~7Iunda77D~0kOVE-9EsLJtzJ0~BYM6YBJCPGWICsfjuvCXF4dIloo66UFwCdYvsjayRoYolpFyh-pPCWA2Q5jlxU6IIQKjDSU4~nBWrn0qpengHOIr-GIFylysOSAe823PAsYfI7twLyea5~OQoKeK0XEcEMLDC3jeCrCx6A__&Key-Pair-Id=APKAIHG36VEWPDULE23Q"))),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = Message(attachments = mutableListOf(Attachment(imageUrl = "https://us-east.stream-io-cdn.com/47574/images/8afae93f-6470-4850-993b-af23d5879244.STREAM_20201203_113150412_PXL_20201203_0828441.jpg?ro=0&Expires=1608286584&Signature=pzWyMXJ~JNgf9VmEpMU5KUkFELFEPZ~dIoIk2sP1DD1uBFYqO2zg8o~e-7xmNWF1eBF8tekrn8ffI5CyZMqTyREMJ1TLqwgUNdkJtkqIMiGf0pIBeKA0jJdcS9ozkIct9l4wx9K31g3jX~Ik5sl3mLd3K~F70Hg1Fe02xaDIucf0s~7Iunda77D~0kOVE-9EsLJtzJ0~BYM6YBJCPGWICsfjuvCXF4dIloo66UFwCdYvsjayRoYolpFyh-pPCWA2Q5jlxU6IIQKjDSU4~nBWrn0qpengHOIr-GIFylysOSAe823PAsYfI7twLyea5~OQoKeK0XEcEMLDC3jeCrCx6A__&Key-Pair-Id=APKAIHG36VEWPDULE23Q"))),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true
            )
        )
    }
}
