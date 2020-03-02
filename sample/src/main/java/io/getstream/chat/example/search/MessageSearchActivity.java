package io.getstream.chat.example.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.getstream.sdk.chat.StreamChat;

import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.example.navigation.ChannelDestination;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Utils;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.getstream.chat.example.R;
import io.getstream.chat.example.databinding.ActivityMessageSearchBinding;

public class MessageSearchActivity extends AppCompatActivity implements SearchMessageRecyclerAdapter.OnSearchItemClickListener {

    private static final String CID = "cid";
    private static final String OPEN_CHANNEL = "open-channel";

    private MessageSearchVM viewModel;
    private ActivityMessageSearchBinding binding;
    private SearchMessageRecyclerAdapter adapter;

    public static Intent searchAndOpenChannel(Context context) {
        Intent intent = new Intent(context, MessageSearchActivity.class);
        intent.putExtra(OPEN_CHANNEL, true);
        return intent;
    }

    public static Intent search(Context context, @Nullable String cid) {
        Intent intent = new Intent(context, MessageSearchActivity.class);
        intent.putExtra(CID, cid);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(MessageSearchVM.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message_search);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        extractData();
        initViews();
        observeData();
        observeErrors();

        viewModel.loadChannel();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onItemClicked(String channelType, String channelId, String messageId) {

        if (getIntent().getBooleanExtra(OPEN_CHANNEL, false)) {
            StreamChat.getNavigator().navigate(new ChannelDestination(channelType, channelId, this));
        }

    }

    private void extractData() {
        viewModel.setCid(getIntent().getStringExtra(CID));
    }

    private void initViews() {
        initToolbar();
        addListeners();
    }

    private void initToolbar() {
        setSupportActionBar(binding.searchMessagesToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void addListeners() {
        binding.searchEt.requestFocus();
        binding.searchEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.search();
                Utils.hideSoftKeyboard(this);
            }
            return false;
        });
    }

    private void initRecyclerView(Channel channel) {
        adapter = new SearchMessageRecyclerAdapter(channel);
        adapter.setOnItemClickListener(this);
        binding.searchMessagesMessagesRv.setAdapter(adapter);
        binding.searchMessagesMessagesRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void observeData() {
        viewModel.searchResult.observe(this, (Observer) items -> {
                    List<MessageResponse> messages = (List<MessageResponse>) items;
                    if (messages != null) {
                        adapter.setItems(((List<MessageResponse>) items));
                    }
                }
        );

        viewModel.channelResult.observe(this, new Observer<Channel>() {
            @Override
            public void onChanged(Channel channel) {
                initRecyclerView(channel);
            }
        });
    }

    private void observeErrors() {
        viewModel.onError.observe(this, (Observer) errorMsg -> Toast.makeText(this, (String) errorMsg, Toast.LENGTH_SHORT).show());
    }
}
