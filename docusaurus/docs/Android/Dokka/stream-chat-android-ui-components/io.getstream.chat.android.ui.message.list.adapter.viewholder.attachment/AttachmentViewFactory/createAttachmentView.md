---
title: createAttachmentView
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment](../index.md)/[AttachmentViewFactory](index.md)/[createAttachmentView](createAttachmentView.md)  
  
  
  
# createAttachmentView  
open fun [createAttachmentView](createAttachmentView.md)(data: MessageListItem.MessageItem, listeners: [MessageListListenerContainer](../../io.getstream.chat.android.ui.message.list.adapter/MessageListListenerContainer/index.md), style: [MessageListItemStyle](../../io.getstream.chat.android.ui.message.list/MessageListItemStyle/index.md), parent: [ViewGroup](https://developer.android.com/reference/kotlin/android/view/ViewGroup.html)): [View](https://developer.android.com/reference/kotlin/android/view/View.html)Create a content view for particular collection of attachments. If the collection contains only link attachment then it creates a link content attachment view, if the collection contains attachments without links then it creates a content view for the list of attachments, otherwise it creates a content view with both links and list contents views.  
  
#### Return  
[View](https://developer.android.com/reference/kotlin/android/view/View.html) as content view for passed attachments.  
  
## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment/AttachmentViewFactory/createAttachmentView/#com.getstream.sdk.chat.adapter.MessageListItem.MessageItem#io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer#io.getstream.chat.android.ui.message.list.MessageListItemStyle#android.view.ViewGroup/PointingToDeclaration/"></a>data| <a name="io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment/AttachmentViewFactory/createAttachmentView/#com.getstream.sdk.chat.adapter.MessageListItem.MessageItem#io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer#io.getstream.chat.android.ui.message.list.MessageListItemStyle#android.view.ViewGroup/PointingToDeclaration/"></a>MessageListItem.MessageItem with particular data and attachments for the message list.|
| <a name="io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment/AttachmentViewFactory/createAttachmentView/#com.getstream.sdk.chat.adapter.MessageListItem.MessageItem#io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer#io.getstream.chat.android.ui.message.list.MessageListItemStyle#android.view.ViewGroup/PointingToDeclaration/"></a>listeners| <a name="io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment/AttachmentViewFactory/createAttachmentView/#com.getstream.sdk.chat.adapter.MessageListItem.MessageItem#io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer#io.getstream.chat.android.ui.message.list.MessageListItemStyle#android.view.ViewGroup/PointingToDeclaration/"></a>[MessageListListenerContainer](../../io.getstream.chat.android.ui.message.list.adapter/MessageListListenerContainer/index.md) with listeners for the message list.|
| <a name="io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment/AttachmentViewFactory/createAttachmentView/#com.getstream.sdk.chat.adapter.MessageListItem.MessageItem#io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer#io.getstream.chat.android.ui.message.list.MessageListItemStyle#android.view.ViewGroup/PointingToDeclaration/"></a>style| <a name="io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment/AttachmentViewFactory/createAttachmentView/#com.getstream.sdk.chat.adapter.MessageListItem.MessageItem#io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer#io.getstream.chat.android.ui.message.list.MessageListItemStyle#android.view.ViewGroup/PointingToDeclaration/"></a>[MessageListItemStyle](../../io.getstream.chat.android.ui.message.list/MessageListItemStyle/index.md) style container with text colors params for the message list.|
| <a name="io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment/AttachmentViewFactory/createAttachmentView/#com.getstream.sdk.chat.adapter.MessageListItem.MessageItem#io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer#io.getstream.chat.android.ui.message.list.MessageListItemStyle#android.view.ViewGroup/PointingToDeclaration/"></a>parent| <a name="io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment/AttachmentViewFactory/createAttachmentView/#com.getstream.sdk.chat.adapter.MessageListItem.MessageItem#io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer#io.getstream.chat.android.ui.message.list.MessageListItemStyle#android.view.ViewGroup/PointingToDeclaration/"></a>[View](https://developer.android.com/reference/kotlin/android/view/View.html) of VH's root where such attachment content view is supposed to be placed.|
  

