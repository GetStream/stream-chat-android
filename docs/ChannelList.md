### ChannelList

The ChannelListView shows a list of channel previews.
Typically it will show an unread/read state, the last message and who is participating in the conversation.

The easiest way to render a ChannelList is to add it to your layout:

```xml
<com.getstream.sdk.chat.view.ChannelListView android:id="@+id/channelList"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_marginBottom="10dp"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintBottom_toBottomOf="parent"
     />
```

And in activity do something like this:

```java
package io.getstream.chat.example;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.ChannelListItemAdapter;
import com.getstream.sdk.chat.adapter.ChannelListItemViewHolder;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

import java.util.HashMap;

import io.getstream.chat.example.databinding.ActivityMainBinding;

import static com.getstream.sdk.chat.enums.Filters.in;


/**
 * This activity shows a list of channels
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE";
    public static final String EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID";
    final String USER_ID = "broken-waterfall-5";
    // User token is typically provided by your server when the user authenticates
    final String USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJva2VuLXdhdGVyZmFsbC01In0.d1xKTlD_D0G-VsBoDBNbaLjO-2XWNA8rlTm4ru4sMHg";
    private ChannelListViewModel viewModel;

    // establish a websocket connection to stream
    protected Client configureStreamClient() {
        Client client = StreamChat.getInstance(this.getApplication());
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Broken waterfall");
        extraData.put("image", "https://bit.ly/2u9Vc0r");
        User user = new User(USER_ID, extraData);
        client.setUser(user, USER_TOKEN);

        return client;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup the client
        Client client = configureStreamClient();

        // we're using data binding in this example
        ActivityMainBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Specify the current activity as the lifecycle owner.
        binding.setLifecycleOwner(this);

        // most the business logic for chat is handled in the ChannelListViewModel view model
        viewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);
        // set the viewModel data for the activity_main.xml layout
        binding.setViewModel(viewModel);

        ChannelListItemAdapter adapter = new ChannelListItemAdapter(this);
        adapter.setCustomViewHolder(ChannelListItemViewHolder.class);
        binding.channelList.setViewModel(viewModel, this, adapter);

        // query all channels where the current user is a member
        // FilterObject filter = in("members", USER_ID);
        FilterObject filter = in("type", "messaging");
        viewModel.setChannelFilter(filter);

        // setup an onclick listener to capture clicks to the user profile or channel
        MainActivity parent = this;
        binding.channelList.setOnChannelClickListener(channel -> {
            // open the channel activity
            Intent intent = new Intent(parent, ChannelActivity.class);
            intent.putExtra(EXTRA_CHANNEL_TYPE, channel.getType());
            intent.putExtra(EXTRA_CHANNEL_ID, channel.getId());
            startActivity(intent);
        });
        binding.channelList.setOnUserClickListener(user -> {
            // TODO: open your user profile
        });

    }
}
```

#### Listeners

The following listeners can be set

* setOnChannelClickListener
* setOnLongClickListener
* setOnUserClickListener

#### Styling using Attributes

The following attributes are available:

```xml
<com.getstream.sdk.chat.view.ChannelListView android:id="@+id/channelList"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_marginBottom="10dp"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintBottom_toBottomOf="parent"
    stream:avatarWidth="40dp"
    stream:avatarHeight="40dp"
    stream:avatarInitialsTextSize="16sp"
    stream:avatarInitialsTextColor="#000"
    stream:avatarInitialsTextStyle="bold"
    stream:dateTextColor="#858585"
    stream:dateTextSize="11sp"
    stream:titleTextColor="#000"
    stream:titleTextSize="16sp"
    stream:titleTextStyleChannel="bold"
    stream:unreadTitleTextColor="#000"
    stream:unreadTitleTextStyle="bold"
    stream:messageTextColor="#858585"
    stream:messageTextSize="16sp"
    stream:messageTextStyle="normal"
    stream:unreadMessageTextColor="#000"
    stream:unreadMessageTextStyle="bold"
    stream:channelPreviewLayout="@layout/list_item_channel_custom"
     />
```

#### Changing the layout

If you need to make a bigger change you can swap the layout for the channel previews.

```xml
<com.getstream.sdk.chat.view.ChannelListView android:id="@+id/channelList"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_marginBottom="10dp"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintBottom_toBottomOf="parent"
    stream:channelPreviewLayout="@layout/list_item_channel_custom"
     />
```

That only works for simple changes where you don't change the IDs of views, or their types.
You can find the default layout and copy and paste it in **list_item_channel.xml**

#### Custom Viewholder

If you need full control over the styling for the channel preview you can overwrite the view holder.

```java
ChannelListItemAdapter adapter = new ChannelListItemAdapter(this);
adapter.setCustomViewHolder(MyCustomViewHolder.class);
binding.channelList.setViewModel(viewModel, this, adapter);
```

You'll typically want to extend either the `ChannelListItemViewHolder` or the `BaseChannelListItemViewHolder` class.

#### Client usage

Alternatively you can of course build your own ChannelListView using the low level client.
