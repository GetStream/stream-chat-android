package com.getstream.sdk.chat.view.fragment;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.getstream.sdk.chat.adapter.ChannelListItemAdapter;
import com.getstream.sdk.chat.databinding.FragmentChannelListBinding;
import com.getstream.sdk.chat.interfaces.ChannelListEventHandler;
import com.getstream.sdk.chat.rest.core.StreamChat;
import com.getstream.sdk.chat.rest.interfaces.AddDeviceCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.response.AddDevicesResponse;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.PermissionChecker;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.activity.UsersActivity;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


/**
 * A Fragment for Channels preview.
 */
public class ChannelListFragment extends Fragment implements ChannelListEventHandler {

    final String TAG = ChannelListFragment.class.getSimpleName();

    private ChannelListViewModel mViewModel;
    private FragmentChannelListBinding binding;
    private ChannelListItemAdapter adapter;

    public int containerResId;

    private boolean isLastPage = false;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private RecyclerView.LayoutManager mLayoutManager;

    private int channelItemLayoutId;
    private String channelItemViewHolderName;

    public StreamChat client;

    // region LifeCycle
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentChannelListBinding.inflate(inflater, container, false);
        mViewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);
        binding.setViewModel(mViewModel);

        init();
        configUIs();
        queryChannels();
        PermissionChecker.permissionCheck(getActivity(), this);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ChannelListViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        client.setChannelListEventHandler(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.USERSLISTACTIVITY_REQUEST) {
            try {
                boolean result = data.getBooleanExtra("result", false);
                if (result) {
                    String channelId = data.getStringExtra(Constant.TAG_CHANNEL_RESPONSE_ID);
                    navigationChannelFragment(client.getChannelResponseById(channelId));
                }
            } catch (Exception e) {
            }
        }
    }

    private Activity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
    }

    //endregion

    // region Private Functions

    private void init() {
        client.setChannelListEventHandler(this);
        try {
            Fresco.initialize(getContext());
        } catch (Exception e) {
        }

        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
    }

    private void configUIs() {
        // Fits SystemWindows
        try {
            FrameLayout frameLayout = getActivity().findViewById(this.containerResId);
            frameLayout.setFitsSystemWindows(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // hides Action Bar
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }

        configCustomChannelItemView();

        binding.clHeader.setVisibility(client.getComponent().channel.isShowSearchBar() ? View.VISIBLE : View.GONE);

        configChannelListView();
        configChannelRecyclerView();


        binding.tvSend.setOnClickListener((View view) -> {
            navigateUserList();
            Utils.setButtonDelayEnable(view);
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                adapter.filter = binding.etSearch.getText().toString();
                adapter.notifyDataSetChanged();
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
        binding.etSearch.clearFocus();
    }

    private void configChannelRecyclerView() {
        binding.listChannels.setVisibility(View.VISIBLE);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.listChannels.setLayoutManager(mLayoutManager);

        binding.listChannels.setOnScrollListener(new RecyclerView.OnScrollListener() {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                int currentLastVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                if (mLastFirstVisibleItem < firstVisibleItem) {
                    if (currentLastVisible == client.channels.size() - 1)
                        queryChannels();
                }
                if (mLastFirstVisibleItem > firstVisibleItem) {
                    Log.d(TAG, "SCROLLING UP");
                }
                mLastFirstVisibleItem = firstVisibleItem;
            }
        });
    }

    private void configCustomChannelItemView() {
        channelItemLayoutId = client.getComponent().channel.getChannelItemLayoutId();
        channelItemViewHolderName = client.getComponent().channel.getChannelItemViewHolderName();
    }

    private void setAfterFirstConnection() {
        // Initialize Channels
        client.channels.clear();
        initLoadingChannels();
        queryChannels();

        // get and save Device TokenService
        try {
            getDeviceToken();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Failed adding device token");
        }
    }

    private void initLoadingChannels() {
        isCalling = false;
        isLastPage = false;
    }

    boolean isCalling;

    /**
     * Getting channels
     */
    public void queryChannels() {
        if (TextUtils.isEmpty(client.connectionId)) return;
        if (isLastPage || isCalling) return;
        binding.setShowMainProgressbar(true);
        isCalling = true;
        client.queryChannels(new QueryChannelListCallback() {
            @Override
            public void onSuccess(QueryChannelsResponse response) {
                binding.setShowMainProgressbar(false);
                isCalling = false;
                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                isLastPage = (response.getChannels().size() < Constant.CHANNEL_LIMIT);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                binding.setShowMainProgressbar(false);
                isCalling = false;
                Utils.showMessage(getContext(), errMsg);
            }
        });
    }

    private void configChannelListView() {
        adapter = new ChannelListItemAdapter(getContext(), client.channels,
                channelItemViewHolderName, channelItemLayoutId, (View view) -> {

            String channelId = view.getTag().toString();
            ChannelResponse response = client.getChannelResponseById(channelId);
            getActivity().runOnUiThread(() -> navigationChannelFragment(response));

        }, (View view) -> {
            String channelId = view.getTag().toString();
            final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Do you want to delete this channel?")
                    .setMessage("If you delete this channel, will delete all chat history for this channel!")
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

            alertDialog.setOnShowListener((DialogInterface dialog) -> {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener((View v) -> {
                    ChannelResponse response_ = client.getChannelResponseById(channelId);
                    client.deleteChannel(channelId, new QueryChannelCallback() {
                        @Override
                        public void onSuccess(ChannelResponse response) {
                            Utils.showMessage(getContext(), "Deleted successfully!");
                            client.channels.remove(response_);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(String errMsg, int errCode) {
                            Utils.showMessage(getContext(), errMsg);
                        }
                    });
                    alertDialog.dismiss();
                });

            });
            alertDialog.show();
            return true;
        });
        binding.listChannels.setAdapter(adapter);
    }

    private void navigationChannelFragment(ChannelResponse response) {
        ChannelFragment fragment = new ChannelFragment();
        fragment.client = client;
        fragment.channelIdFromChannelList = response.getChannel().getId();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(() -> {
            if (adapter != null)
                adapter.notifyDataSetChanged();
        });
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(containerResId, fragment);
        fragmentTransaction.addToBackStack("OK");
        fragmentTransaction.commit();
    }

    private void navigateUserList() {
        Intent intent = new Intent(getContext(), UsersActivity.class);
        startActivityForResult(intent, Constant.USERSLISTACTIVITY_REQUEST);
    }

    private void getDeviceToken() {
        String token = pref.getString("TokenService", null);
        if (token != null) {
            Log.d(TAG, "device Token: " + token);
            return;
        }

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener((@NonNull Task<InstanceIdResult> task) -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(getActivity(), "getInstanceId failed:" + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "getInstanceId failed", task.getException());
                        return;
                    }
                    String token_ = task.getResult().getToken();
                    Log.d(TAG, "device TokenService: " + token_);
                    // Save to Server
                    client.addDevice(token_, new AddDeviceCallback() {
                        @Override
                        public void onSuccess(AddDevicesResponse response) {

                        }

                        @Override
                        public void onError(String errMsg, int errCode) {

                        }
                    });
                    // Save to Local
                    editor.putString("TokenService", token_);
                    editor.commit();
                });
    }

    //endregion

    // region Listners

    /**
     * Handle server response
     */
    @Override
    public void updateChannels() {
        activity.runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    @Override
    public void handleConnection() {
        setAfterFirstConnection();
        binding.setNoConnection(false);
    }

    /**
     * Handle server response failures.
     *
     * @param errMsg  Error message
     * @param errCode Error code
     */
    @Override
    public void onConnectionFailed(String errMsg, int errCode) {
        binding.setNoConnection(true);
        binding.setShowMainProgressbar(false);
    }

    //endregion

    // region Permission

    /**
     * Permission check
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constant.PERMISSIONS_REQUEST) {
            boolean granted = true;
            for (int grantResult : grantResults)
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            if (!granted) PermissionChecker.showRationalDialog(getContext(), this);
        }
    }

    // endregion
}
