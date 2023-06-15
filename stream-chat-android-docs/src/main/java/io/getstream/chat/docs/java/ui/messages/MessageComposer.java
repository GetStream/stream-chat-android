package io.getstream.chat.docs.java.ui.messages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;

import io.getstream.chat.android.ui.common.state.messages.Edit;
import io.getstream.chat.android.ui.common.state.messages.MessageMode;
import io.getstream.chat.android.ui.common.state.messages.Reply;
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState;
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext;
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView;
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle;
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerCenterContent;
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerCommandSuggestionsContent;
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerFooterContent;
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerHeaderContent;
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerLeadingContent;
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerMentionSuggestionsContent;
import io.getstream.chat.android.ui.feature.messages.composer.content.DefaultMessageComposerTrailingContent;
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerContent;
import io.getstream.chat.android.ui.feature.messages.list.MessageListView;
import io.getstream.chat.android.ui.helper.TransformStyle;
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory;
import io.getstream.chat.docs.databinding.MessageComposerLeadingContentBinding;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * [Message Composer](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer)
 */
public class MessageComposer extends Fragment {

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer/#usage)
     */
    class Usage extends Fragment {

        private MessageComposerView messageComposerView;
        private MessageListView messageListView;

        public void usage1() {
            // Create MessageComposerViewModel for a given channel
            ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder(requireContext())
                    .cid("messaging:123")
                    .build();
            ViewModelProvider provider = new ViewModelProvider(this, factory);
            MessageComposerViewModel viewModel = provider.get(MessageComposerViewModel.class);

            // Bind MessageComposerViewModel with MessageComposerView
            MessageComposerViewModelBinding.bind(viewModel, messageComposerView, getViewLifecycleOwner());
        }


        public void usage2() {
            // Create ViewModels for MessageComposerView and MessageListView
            ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder(requireContext())
                    .cid("messaging:123")
                    .build();
            ViewModelProvider provider = new ViewModelProvider(this, factory);
            MessageComposerViewModel messageComposerViewModel = provider.get(MessageComposerViewModel.class);
            MessageListViewModel messageListViewModel = provider.get(MessageListViewModel.class);

            // Bind MessageComposerViewModel with MessageComposerView
            MessageComposerViewModelBinding.bind(messageComposerViewModel, messageComposerView, getViewLifecycleOwner());

            // Bind MessageListViewModel with MessageListView
            MessageListViewModelBinding.bind(messageListViewModel, messageListView, getViewLifecycleOwner());

            // Integrate MessageComposerView with MessageListView
            messageListViewModel.getMode().observe(getViewLifecycleOwner(), mode -> {
                if (mode instanceof MessageMode.MessageThread) {
                    messageComposerViewModel.setMessageMode(new MessageMode.MessageThread(((MessageMode.MessageThread) mode).getParentMessage()));
                } else if (mode instanceof MessageMode.Normal) {
                    messageComposerViewModel.leaveThread();
                }
            });
            messageListView.setMessageReplyHandler((cid, message) -> messageComposerViewModel.performMessageAction(new Reply(message)));
            messageListView.setMessageEditHandler((message) -> messageComposerViewModel.performMessageAction(new Edit(message)));
        }
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer/#handling-actions)
     */
    class HandlingActions {

        private MessageComposerView messageComposerView;
        private MessageComposerViewModel messageComposerViewModel;

        public void handlingActions1() {
            messageComposerView.setSendMessageButtonClickListener(() -> {
                // Handle send button click
                return Unit.INSTANCE;
            });
            messageComposerView.setTextInputChangeListener((text) -> {
                // Handle input text change
                return Unit.INSTANCE;
            });
            messageComposerView.setAttachmentSelectionListener((attachments) -> {
                // Handle attachment selection
                return Unit.INSTANCE;
            });
            messageComposerView.setAttachmentRemovalListener((attachment) -> {
                // Handle attachment removal
                return Unit.INSTANCE;
            });
            messageComposerView.setMentionSelectionListener((user) -> {
                // Handle mention selection
                return Unit.INSTANCE;
            });
            messageComposerView.setCommandSelectionListener((command) -> {
                // Handle command selection
                return Unit.INSTANCE;
            });
            messageComposerView.setAlsoSendToChannelSelectionListener((checked) -> {
                // Handle "also send to channel" checkbox selection
                return Unit.INSTANCE;
            });
            messageComposerView.setDismissActionClickListener(() -> {
                // Handle dismiss action button click
                return Unit.INSTANCE;
            });
            messageComposerView.setCommandsButtonClickListener(() -> {
                // Handle commands button click
                return Unit.INSTANCE;
            });
            messageComposerView.setDismissSuggestionsListener(() -> {
                // Handle when suggestions popup is dismissed
                return Unit.INSTANCE;
            });
            messageComposerView.setAttachmentsButtonClickListener(() -> {
                // Handle attachments button click
                return Unit.INSTANCE;
            });
        }

        public void handlingActions2() {
            messageComposerView.setSendMessageButtonClickListener(() -> {
                messageComposerViewModel.sendMessage();
                return Unit.INSTANCE;
            });
            messageComposerView.setTextInputChangeListener((text) -> {
                messageComposerViewModel.setMessageInput(text);
                return Unit.INSTANCE;
            });
            messageComposerView.setAttachmentSelectionListener((attachments) -> {
                messageComposerViewModel.addSelectedAttachments(attachments);
                return Unit.INSTANCE;
            });
            messageComposerView.setAttachmentRemovalListener((attachment) -> {
                messageComposerViewModel.removeSelectedAttachment(attachment);
                return Unit.INSTANCE;
            });
            messageComposerView.setMentionSelectionListener((user) -> {
                messageComposerViewModel.selectMention(user);
                return Unit.INSTANCE;
            });
            messageComposerView.setCommandSelectionListener((command) -> {
                messageComposerViewModel.selectCommand(command);
                return Unit.INSTANCE;
            });
            messageComposerView.setAlsoSendToChannelSelectionListener((checked) -> {
                messageComposerViewModel.setAlsoSendToChannel(checked);
                return Unit.INSTANCE;
            });
            messageComposerView.setDismissActionClickListener(() -> {
                messageComposerViewModel.dismissMessageActions();
                return Unit.INSTANCE;
            });
            messageComposerView.setCommandsButtonClickListener(() -> {
                messageComposerViewModel.toggleCommandsVisibility();
                return Unit.INSTANCE;
            });
            messageComposerView.setDismissSuggestionsListener(() -> {
                messageComposerViewModel.dismissSuggestionsPopup();
                return Unit.INSTANCE;
            });
            messageComposerView.setAttachmentsButtonClickListener(() -> {
                // Handle attachments button click
                return Unit.INSTANCE;
            });
        }
    }

    /**
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer/#customization)
     */
    class Customization {

        private Context context;
        private FragmentManager supportFragmentManager;
        private MessageComposerView messageComposerView;
        private MessageComposerViewModel messageComposerViewModel;

        public void styleTransformation() {
            TransformStyle.setMessageComposerStyleTransformer(source -> {
                // Customize the style
                return source;
            });
        }

        public void contentCustomization1() {
            DefaultMessageComposerLeadingContent leadingContent = new DefaultMessageComposerLeadingContent(context);
            leadingContent.setAttachmentsButtonClickListener(() -> messageComposerView.getAttachmentsButtonClickListener().invoke());
            leadingContent.setCommandsButtonClickListener(() -> messageComposerView.getCommandsButtonClickListener().invoke());

            messageComposerView.setLeadingContent(leadingContent);

            DefaultMessageComposerCenterContent centerContent = new DefaultMessageComposerCenterContent(context);
            centerContent.setTextInputChangeListener((text) -> messageComposerView.getTextInputChangeListener().invoke(text));
            centerContent.setAttachmentRemovalListener((attachment -> messageComposerView.getAttachmentRemovalListener().invoke(attachment)));

            messageComposerView.setCenterContent(centerContent);

            DefaultMessageComposerTrailingContent trailingContent = new DefaultMessageComposerTrailingContent(context);
            trailingContent.setSendMessageButtonClickListener(() -> messageComposerView.getSendMessageButtonClickListener().invoke());

            messageComposerView.setTrailingContent(trailingContent);

            DefaultMessageComposerHeaderContent headerContent = new DefaultMessageComposerHeaderContent(context);
            headerContent.setDismissActionClickListener(() -> messageComposerView.getDismissActionClickListener().invoke());
            messageComposerView.setHeaderContent(headerContent);

            DefaultMessageComposerFooterContent footerContent = new DefaultMessageComposerFooterContent(context);
            footerContent.setAlsoSendToChannelSelectionListener((checked) -> messageComposerView.getAlsoSendToChannelSelectionListener().invoke(checked));
            messageComposerView.setFooterContent(footerContent);

            DefaultMessageComposerCommandSuggestionsContent commandSuggestionsContent = new DefaultMessageComposerCommandSuggestionsContent(context);
            commandSuggestionsContent.setCommandSelectionListener((command) -> messageComposerView.getCommandSelectionListener().invoke(command));
            messageComposerView.setCommandSuggestionsContent(commandSuggestionsContent);

            DefaultMessageComposerMentionSuggestionsContent mentionSuggestionsContent = new DefaultMessageComposerMentionSuggestionsContent(context);
            mentionSuggestionsContent.setMentionSelectionListener((user) -> messageComposerView.getMentionSelectionListener().invoke(user));
            messageComposerView.setMentionSuggestionsContent(mentionSuggestionsContent);
        }

        public void contentCustomization2() {
            DefaultMessageComposerLeadingContent leadingContent = new DefaultMessageComposerLeadingContent(context);
            leadingContent.setAttachmentsButtonClickListener(() -> {
                // Show attachment dialog and invoke messageComposerViewModel.addSelectedAttachments(attachments)
                return Unit.INSTANCE;
            });
            leadingContent.setCommandsButtonClickListener(() -> {
                messageComposerViewModel.toggleCommandsVisibility();
                return Unit.INSTANCE;
            });
            messageComposerView.setLeadingContent(leadingContent);

            DefaultMessageComposerCenterContent centerContent = new DefaultMessageComposerCenterContent(context);
            centerContent.setTextInputChangeListener((text) -> {
                messageComposerViewModel.setMessageInput(text);
                return Unit.INSTANCE;
            });
            centerContent.setAttachmentRemovalListener((attachment -> {
                messageComposerViewModel.removeSelectedAttachment(attachment);
                return Unit.INSTANCE;
            }));
            messageComposerView.setCenterContent(centerContent);

            DefaultMessageComposerTrailingContent trailingContent = new DefaultMessageComposerTrailingContent(context);
            trailingContent.setSendMessageButtonClickListener(() -> {
                messageComposerViewModel.buildNewMessage();
                return Unit.INSTANCE;
            });
            messageComposerView.setTrailingContent(trailingContent);

            DefaultMessageComposerHeaderContent headerContent = new DefaultMessageComposerHeaderContent(context);
            headerContent.setDismissActionClickListener(() -> {
                messageComposerViewModel.dismissMessageActions();
                return Unit.INSTANCE;
            });
            messageComposerView.setHeaderContent(headerContent);

            DefaultMessageComposerFooterContent footerContent = new DefaultMessageComposerFooterContent(context);
            footerContent.setAlsoSendToChannelSelectionListener((checked) -> {
                messageComposerViewModel.setAlsoSendToChannel(checked);
                return Unit.INSTANCE;
            });
            messageComposerView.setFooterContent(footerContent);

            DefaultMessageComposerCommandSuggestionsContent commandSuggestionsContent = new DefaultMessageComposerCommandSuggestionsContent(context);
            commandSuggestionsContent.setCommandSelectionListener((command) -> {
                messageComposerViewModel.selectCommand(command);
                return Unit.INSTANCE;
            });
            messageComposerView.setCommandSuggestionsContent(commandSuggestionsContent);

            DefaultMessageComposerMentionSuggestionsContent mentionSuggestionsContent = new DefaultMessageComposerMentionSuggestionsContent(context);
            mentionSuggestionsContent.setMentionSelectionListener((user) -> {
                messageComposerViewModel.selectMention(user);
                return Unit.INSTANCE;
            });
            messageComposerView.setMentionSuggestionsContent(mentionSuggestionsContent);
        }

        public void contentCustomization3() {
            // Create an instance of a date picker dialog
            MaterialDatePicker<Long> datePickerDialog = MaterialDatePicker.Builder.datePicker().build();
            datePickerDialog.addOnPositiveButtonClickListener(selection -> {
                // Handle date selection
            });

            CustomMessageComposerLeadingContent leadingContent = new CustomMessageComposerLeadingContent(context);
            leadingContent.datePickerButtonClickListener = () -> {
                // Show the date picker dialog
                datePickerDialog.show(supportFragmentManager, null);
                return Unit.INSTANCE;
            };

            messageComposerView.setLeadingContent(leadingContent);
        }

        public class CustomMessageComposerLeadingContent extends FrameLayout implements MessageComposerContent {

            private MessageComposerLeadingContentBinding binding;
            public Function0<Unit> datePickerButtonClickListener = () -> null;


            public CustomMessageComposerLeadingContent(@NonNull Context context) {
                this(context, null);
            }

            public CustomMessageComposerLeadingContent(@NonNull Context context, @Nullable AttributeSet attrs) {
                this(context, attrs, 0);
            }

            public CustomMessageComposerLeadingContent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
                binding = MessageComposerLeadingContentBinding.inflate(LayoutInflater.from(context), this, true);
                binding.datePickerButton.setOnClickListener((view) -> {
                    datePickerButtonClickListener.invoke();
                });
            }

            @Override
            public void attachContext(@NonNull MessageComposerContext messageComposerContext) {
                // Access the style if necessary
                MessageComposerViewStyle style = messageComposerContext.getStyle();
            }

            @Override
            public void renderState(@NonNull MessageComposerState state) {
                // Render the state of the component
            }
        }
    }
}
