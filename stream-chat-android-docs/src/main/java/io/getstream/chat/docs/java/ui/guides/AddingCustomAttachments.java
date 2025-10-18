package io.getstream.chat.docs.java.ui.guides;

import static io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.MessageComposerCapabilitiesKt.canSendMessage;
import static io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.MessageComposerCapabilitiesKt.canUploadFile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.models.Attachment;
import io.getstream.chat.android.models.Message;
import io.getstream.chat.android.ui.ChatUI;
import io.getstream.chat.android.ui.common.state.messages.Edit;
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState;
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext;
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView;
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle;
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewFactoryManager;
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewViewHolder;
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.AttachmentPreviewFactory;
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.FileAttachmentPreviewFactory;
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.factory.MediaAttachmentPreviewFactory;
import io.getstream.chat.android.ui.feature.messages.composer.content.MessageComposerLeadingContent;
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListeners;
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager;
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.BaseAttachmentFactory;
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.DefaultQuotedAttachmentMessageFactory;
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.InnerAttachmentViewHolder;
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.QuotedAttachmentFactory;
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.QuotedAttachmentFactoryManager;
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel;
import io.getstream.chat.docs.databinding.CustomMessageComposerLeadingContentBinding;
import io.getstream.chat.docs.databinding.ItemDateAttachmentBinding;
import io.getstream.chat.docs.databinding.ItemDateAttachmentPreviewBinding;
import io.getstream.chat.docs.databinding.ViewQuotedDateAttachmentBinding;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * [Adding Custom Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments/)
 */
public class AddingCustomAttachments extends Fragment {

    /**
     * [Sending Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments/#sending-date-attachments)
     */
    class SendingDateAttachments extends Fragment {

        private MessageComposerView messageComposerView;
        private MessageComposerViewModel messageComposerViewModel;

        class CustomMessageComposerLeadingContent extends FrameLayout implements MessageComposerLeadingContent {

            private CustomMessageComposerLeadingContentBinding binding;
            private MessageComposerViewStyle style;

            public Function0<Unit> attachmentsButtonClickListener;
            public Function0<Unit> commandsButtonClickListener;

            // Click listener for the date picker button
            public Function1<Unit, Unit> calendarButtonClickListener;

            public CustomMessageComposerLeadingContent(@NonNull Context context) {
                this(context, null);
            }

            public CustomMessageComposerLeadingContent(@NonNull Context context, @Nullable AttributeSet attrs) {
                this(context, attrs, 0);
            }

            public CustomMessageComposerLeadingContent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
                binding = CustomMessageComposerLeadingContentBinding.inflate(LayoutInflater.from(getContext()), this);
                binding.attachmentsButton.setOnClickListener(v -> {
                    if (attachmentsButtonClickListener != null) {
                        attachmentsButtonClickListener.invoke();
                    }
                });
                binding.commandsButton.setOnClickListener(v -> {
                    if (commandsButtonClickListener != null) {
                        commandsButtonClickListener.invoke();
                    }
                });

                // Set click listener for the date picker button
                binding.calendarButton.setOnClickListener(v -> calendarButtonClickListener.invoke(Unit.INSTANCE));
            }

            @Override
            public void attachContext(@NonNull MessageComposerContext messageComposerContext) {
                this.style = messageComposerContext.getStyle();
            }

            @Override
            public void renderState(@NonNull MessageComposerState state) {
                boolean canSendMessage = canSendMessage(state);
                boolean canUploadFile = canUploadFile(state);
                boolean hasTextInput = !state.getInputValue().isEmpty();
                boolean hasAttachments = !state.getAttachments().isEmpty();
                boolean hasCommandInput = state.getInputValue().startsWith("/");
                boolean hasCommandSuggestions = !state.getCommandSuggestions().isEmpty();
                boolean hasMentionSuggestions = !state.getMentionSuggestions().isEmpty();
                boolean isInEditMode = state.getAction() instanceof Edit;

                boolean attachmentsButtonEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions;
                binding.attachmentsButton.setEnabled(attachmentsButtonEnabled);

                boolean attachmentsButtonVisible = style.getAttachmentsButtonVisible() && canSendMessage && canUploadFile && !isInEditMode;
                binding.attachmentsButton.setVisibility(attachmentsButtonVisible ? VISIBLE : GONE);

                boolean commandsButtonEnabled = !hasTextInput && !hasAttachments;
                binding.commandsButton.setEnabled(commandsButtonEnabled);

                boolean commandsButtonVisible = style.getCommandsButtonVisible() && canSendMessage && !isInEditMode;
                binding.commandsButton.setVisibility(commandsButtonVisible ? VISIBLE : GONE);

                binding.commandsButton.setSelected(hasCommandSuggestions);
            }

            @Nullable
            @Override
            public View findViewByKey(@NonNull String key) {
                switch (key) {
                    case "attachmentsButton":
                        return binding.attachmentsButton;
                    case "commandsButton":
                        return binding.commandsButton;
                    case "calendarButton":
                        return binding.calendarButton;
                    default:
                        return null;
                }
            }

            @Nullable
            @Override
            public Function0<Unit> getAttachmentsButtonClickListener() {
                return attachmentsButtonClickListener;
            }

            @Override
            public void setAttachmentsButtonClickListener(@Nullable Function0<Unit> unitFunction0) {
                this.attachmentsButtonClickListener = unitFunction0;
            }

            @Nullable
            @Override
            public Function0<Unit> getCommandsButtonClickListener() {
                return commandsButtonClickListener;
            }

            @Override
            public void setCommandsButtonClickListener(@Nullable Function0<Unit> unitFunction0) {
                this.commandsButtonClickListener = unitFunction0;
            }
        }

        private void setLeadingContent(Context context) {
            CustomMessageComposerLeadingContent leadingContent = new CustomMessageComposerLeadingContent(context);
            leadingContent.attachmentsButtonClickListener = () -> messageComposerView.getAttachmentsButtonClickListener().invoke();
            leadingContent.commandsButtonClickListener = () -> messageComposerView.getCommandsButtonClickListener().invoke();
            leadingContent.calendarButtonClickListener = unit -> {
                // Create an instance of a date picker dialog
                MaterialDatePicker<Long> datePickerDialog = MaterialDatePicker.Builder
                        .datePicker()
                        .build();

                // Add an attachment to the message input when the user selects a date
                datePickerDialog.addOnPositiveButtonClickListener(date -> {
                    String payload = new SimpleDateFormat("MMMM dd, yyyy").format(new Date(date));
                    Map<String, Object> extraData = new HashMap<>();
                    extraData.put("payload", payload);
                    Attachment attachment = new Attachment.Builder()
                            .withType("date")
                            .withExtraData(extraData)
                            .build();
                    messageComposerViewModel.addSelectedAttachments(Collections.singletonList(attachment));
                });

                // Show the date picker dialog on a click on the calendar button
                datePickerDialog.show(getChildFragmentManager(), null);
                return Unit.INSTANCE;
            };

            // Set custom leading content view
            messageComposerView.setLeadingContent(leadingContent);
        }

        class DateAttachmentPreviewFactory implements AttachmentPreviewFactory {

            @Override
            public boolean canHandle(@NonNull Attachment attachment) {
                return attachment.getType().equals("date");
            }

            @NonNull
            @Override
            public AttachmentPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parentView,
                                                                  @NonNull Function1<? super Attachment, Unit> attachmentRemovalListener,
                                                                  @Nullable MessageComposerViewStyle style) {
                ItemDateAttachmentPreviewBinding binding = ItemDateAttachmentPreviewBinding
                        .inflate(LayoutInflater.from(parentView.getContext()), parentView, false);
                return new DateAttachmentPreviewViewHolder(binding, attachmentRemovalListener);
            }

            class DateAttachmentPreviewViewHolder extends AttachmentPreviewViewHolder {

                private ItemDateAttachmentPreviewBinding binding;
                private Attachment attachment;

                public DateAttachmentPreviewViewHolder(ItemDateAttachmentPreviewBinding binding, Function1<? super Attachment, Unit> attachmentRemovalListener) {
                    super(binding.getRoot());
                    this.binding = binding;

                    binding.deleteButton.setOnClickListener(v -> attachmentRemovalListener.invoke(attachment));
                }

                @Override
                public void bind(@NonNull Attachment attachment) {
                    this.attachment = attachment;

                    binding.dateTextView.setText((String) attachment.getExtraData().get("payload"));
                }
            }
        }

        private void renderingDateAttachmentPreviews() {
            List<AttachmentPreviewFactory> factories = new ArrayList<>();
            factories.add(new DateAttachmentPreviewFactory());
            // The default factories
            factories.add(new MediaAttachmentPreviewFactory());
            factories.add(new FileAttachmentPreviewFactory());

            ChatUI.setAttachmentPreviewFactoryManager(new AttachmentPreviewFactoryManager(factories));
        }
    }

    /**
     * [Rendering Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments/#rendering-date-attachments)
     */
    class RenderingDateAttachments {

        class DateAttachmentFactory extends BaseAttachmentFactory {

            @Override
            public boolean canHandle(@NonNull Message message) {
                for (Attachment attachment : message.getAttachments()) {
                    if ("date".equals(attachment.getType())) {
                        return true;
                    }
                }
                return false;
            }

            @NonNull
            @Override
            public InnerAttachmentViewHolder createViewHolder(@NonNull Message message,
                                                              @Nullable MessageListListeners listeners,
                                                              @NonNull ViewGroup parent) {
                ItemDateAttachmentBinding binding = ItemDateAttachmentBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new DateAttachmentViewHolder(binding, listeners);
            }

            class DateAttachmentViewHolder extends InnerAttachmentViewHolder {

                private ItemDateAttachmentBinding binding;
                private Message message;

                public DateAttachmentViewHolder(ItemDateAttachmentBinding binding, MessageListListeners listeners) {
                    super(binding.getRoot());
                    this.binding = binding;

                    binding.dateTextView.setOnClickListener(v -> listeners.getAttachmentClickListener().onAttachmentClick(
                            message,
                            message.getAttachments().get(0)
                    ));
                    binding.dateTextView.setOnLongClickListener(v -> {
                        listeners.getMessageLongClickListener().onMessageLongClick(message);
                        return true;
                    });
                }

                @Override
                public void onBindViewHolder(@NonNull Message message) {
                    this.message = message;

                    Attachment dateAttachment = null;
                    for (Attachment attachment : message.getAttachments()) {
                        if ("date".equals(attachment.getType())) {
                            dateAttachment = attachment;
                            break;
                        }
                    }

                    binding.dateTextView.setText((String) dateAttachment.getExtraData().get("payload"));
                }
            }
        }

        private void renderingDateAttachments() {
            List<DateAttachmentFactory> factories = Collections.singletonList(new DateAttachmentFactory());

            ChatUI.setAttachmentFactoryManager(new AttachmentFactoryManager(factories));
        }
    }

    /**
     * [Rendering Quoted Date Attachments](https://getstream.io/chat/docs/sdk/android/ui/guides/adding-custom-attachments/#rendering-quoted-date-attachments)
     */
    class RenderingQuotedDateAttachments {

        class QuotedDateAttachmentFactory implements QuotedAttachmentFactory {

            @Override
            public boolean canHandle(@NonNull Message message) {
                for (Attachment attachment : message.getAttachments()) {
                    if (attachment.getType().equals("date")) {
                        return true;
                    }
                }
                return false;
            }

            @NonNull
            @Override
            public View generateQuotedAttachmentView(@NonNull Message message, @NonNull ViewGroup parent) {
                QuotedDateAttachmentView quotedDateAttachmentView = new QuotedDateAttachmentView(parent.getContext());
                quotedDateAttachmentView.showDate(message.getAttachments().get(0));
                return quotedDateAttachmentView;
            }

            class QuotedDateAttachmentView extends FrameLayout {

                private ViewQuotedDateAttachmentBinding binding;

                public QuotedDateAttachmentView(@NonNull Context context) {
                    this(context, null);
                }

                public QuotedDateAttachmentView(@NonNull Context context, @Nullable AttributeSet attrs) {
                    this(context, attrs, 0);
                }

                public QuotedDateAttachmentView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                    super(context, attrs, defStyleAttr);
                    binding = ViewQuotedDateAttachmentBinding.inflate(LayoutInflater.from(getContext()), this);
                }

                public void showDate(Attachment attachment) {
                    String payload = (String) attachment.getExtraData().get("payload");
                    binding.dateTextView.setText(payload.replace(",", "\n"));
                }
            }
        }

        private void renderingQuotedDateAttachments() {
            List<QuotedAttachmentFactory> factories = new ArrayList<>();
            factories.add(new QuotedDateAttachmentFactory());
            // The default factory
            factories.add(new DefaultQuotedAttachmentMessageFactory());

            ChatUI.setQuotedAttachmentFactoryManager(new QuotedAttachmentFactoryManager(factories));
        }
    }
}
