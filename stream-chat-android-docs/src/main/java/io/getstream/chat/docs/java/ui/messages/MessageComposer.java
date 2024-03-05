package io.getstream.chat.docs.java.ui.messages;

import static java.util.Collections.emptyList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.models.Filters;
import io.getstream.chat.android.models.Member;
import io.getstream.chat.android.models.User;
import io.getstream.chat.android.models.querysort.QuerySortByField;
import io.getstream.chat.android.models.querysort.QuerySorter;
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.JavaCompatUserLookupHandler;
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.UserLookupHandler;
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
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelBinder;
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelBinding;
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory;
import io.getstream.chat.docs.databinding.MessageComposerLeadingContentBinding;
import io.getstream.result.Result;
import io.getstream.result.call.Call;
import io.getstream.result.call.CallKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

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
            // Create MessageComposerViewModel for a given channel
            ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder(requireContext())
                    .cid("messaging:123")
                    .build();
            ViewModelProvider provider = new ViewModelProvider(this, factory);
            MessageComposerViewModel viewModel = provider.get(MessageComposerViewModel.class);

            // Bind MessageComposerViewModel with MessageComposerView
            // Handle message building
            MessageComposerViewModelBinder.with(viewModel)
                    .messageBuilder(viewModel::buildNewMessage)
                    .onSendMessageButtonClick((message) -> {
                        // Handle send button click
                        return Unit.INSTANCE;
                    })
                    .onTextInputChange((text) -> {
                        // Handle input text change
                        return Unit.INSTANCE;
                    })
                    .onAttachmentSelection((attachments) -> {
                        // Handle attachment selection
                        return Unit.INSTANCE;
                    })
                    .onAttachmentRemoval((attachment) -> {
                        // Handle attachment removal
                        return Unit.INSTANCE;
                    })
                    .onMentionSelection((user) -> {
                        // Handle mention selection
                        return Unit.INSTANCE;
                    })
                    .onCommandSelection((command) -> {
                        // Handle command selection
                        return Unit.INSTANCE;
                    })
                    .onAlsoSendToChannelSelection((checked) -> {
                        // Handle "also send to channel" checkbox selection
                        return Unit.INSTANCE;
                    })
                    .onDismissActionClick(() -> {
                        // Handle dismiss action button click
                        return Unit.INSTANCE;
                    })
                    .onCommandsButtonClick(() -> {
                        // Handle commands button click
                        return Unit.INSTANCE;
                    })
                    .onDismissSuggestions(() -> {
                        // Handle when suggestions popup is dismissed
                        return Unit.INSTANCE;
                    })
                    .onAudioRecordButtonHold(() -> {
                        // Handle audio recording button hold
                        return Unit.INSTANCE;
                    })
                    .onAudioRecordButtonLock(() -> {
                        // Handle audio recording button lock
                        return Unit.INSTANCE;
                    })
                    .onAudioRecordButtonCancel(() -> {
                        // Handle audio recording button cancel
                        return Unit.INSTANCE;
                    })
                    .onAudioRecordButtonRelease(() -> {
                        // Handle audio recording button release
                        return Unit.INSTANCE;
                    })
                    .onAudioDeleteButtonClick(() -> {
                        // Handle audio recording delete button click
                        return Unit.INSTANCE;
                    })
                    .onAudioStopButtonClick(() -> {
                        // Handle audio recording stop button click
                        return Unit.INSTANCE;
                    })
                    .onAudioPlaybackButtonClick(() -> {
                        // Handle audio recording playback button click
                        return Unit.INSTANCE;
                    })
                    .onAudioCompleteButtonClick(() -> {
                        // Handle audio recording completion
                        return Unit.INSTANCE;
                    })
                    .onAudioSliderDragStart((progress) -> {
                        // Handle audio recording slider drag start
                        return Unit.INSTANCE;
                    })
                    .onAudioSliderDragStop((progress) -> {
                        // Handle audio recording slider drag stop
                        return Unit.INSTANCE;
                    })
                    .bind(messageComposerView, getViewLifecycleOwner());
            MessageComposerViewModelBinding.bind(viewModel, messageComposerView, getViewLifecycleOwner());
        }

        public void usage3() {
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
            messageComposerView.setDismissSuggestionsListener(() -> {
                // Handle when suggestions popup is dismissed
                return Unit.INSTANCE;
            });
            messageComposerView.setAudioRecordButtonLockListener(() -> {
                // Handle audio record button lock
                return Unit.INSTANCE;
            });
            messageComposerView.setAudioRecordButtonHoldListener(() -> {
                // Handle audio record button hold
                return Unit.INSTANCE;
            });
            messageComposerView.setAudioRecordButtonCancelListener(() -> {
                // Handle audio record button cancel
                return Unit.INSTANCE;
            });
            messageComposerView.setAudioRecordButtonReleaseListener(() -> {
                // Handle audio record button release
                return Unit.INSTANCE;
            });
            messageComposerView.setAudioDeleteButtonClickListener(() -> {
                // Handle audio delete button click
                return Unit.INSTANCE;
            });
            messageComposerView.setAudioStopButtonClickListener(() -> {
                // Handle audio stop button click
                return Unit.INSTANCE;
            });
            messageComposerView.setAudioPlaybackButtonClickListener(() -> {
                // Handle audio playback button click
                return Unit.INSTANCE;
            });
            messageComposerView.setAudioCompleteButtonClickListener(() -> {
                // Handle audio complete button click
                return Unit.INSTANCE;
            });
            messageComposerView.setAudioSliderDragStartListener((progress) -> {
                // Handle audio slider drag start
                return Unit.INSTANCE;
            });
            messageComposerView.setAudioSliderDragStopListener((progress) -> {
                // Handle audio slider drag stop
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

            @Nullable
            @Override
            public View findViewByKey(@NonNull String key) {
                // Return the required view if contained in the component
                return null;
            }
        }
    }

    /**
     * [Changing Mention Search](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-composer/#changing-mention-search)
     */
    class ChangingMentionSearch extends Fragment {

        private ChatClient chatClient;
        private ChannelClient channelClient;

        private MessageComposerView messageComposerView;
        private MessageListView messageListView;

        private Call<List<User>> queryMembers(String query) {
            FilterObject filter = Filters.eq("name", query);
            QuerySorter<Member> sort = QuerySortByField.descByName("name");
            Call<List<Member>> membersCall = channelClient.queryMembers(0, 30, filter, sort, emptyList());
            return CallKt.map(membersCall, members -> {
                List<User> users = new ArrayList<>();
                for (Member member : members) {
                    users.add(member.getUser());
                }
                return users;
            });
        }

        public void usage1() {
            // Create MessageComposerViewModel for a given channel
            ViewModelProvider.Factory factory = new MessageListViewModelFactory.Builder(requireContext())
                    .cid("messaging:123")
                    .userLookupHandler((query, callback) -> {
                        // Implement your custom user lookup
                        Call<List<User>> queryCall = queryMembers(query);
                        queryCall.enqueue(result -> {
                            if (result.isSuccess()) {
                                callback.invoke(result.getOrThrow());
                            } else {
                                callback.invoke(Collections.emptyList());
                            }
                        });
                        return () -> {
                            queryCall.cancel();
                            return Unit.INSTANCE;
                        };
                    })
                    .build();
            ViewModelProvider provider = new ViewModelProvider(this, factory);
            MessageComposerViewModel viewModel = provider.get(MessageComposerViewModel.class);

            // Bind MessageComposerViewModel with MessageComposerView
            MessageComposerViewModelBinding.bind(viewModel, messageComposerView, getViewLifecycleOwner());
        }
    }
}
