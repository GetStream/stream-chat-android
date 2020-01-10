package io.getstream.chat.example.view.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.fragment.app.Fragment;
import io.getstream.chat.example.BaseApplication;
import io.getstream.chat.example.R;
import io.getstream.chat.example.databinding.FragmentProfileBinding;
import io.getstream.chat.example.navigation.LoginDestination;
import io.getstream.chat.example.utils.AppConfig;

public class ProfileFragment extends Fragment {

    ProgressDialog pd;
    AlertDialog errorDialog;
    AppConfig appConfig;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentProfileBinding binding = FragmentProfileBinding.inflate(inflater, container, false);
        Client client = StreamChat.getInstance(getContext());
        appConfig = ((BaseApplication) getContext().getApplicationContext()).getAppConfig();
        binding.setUser(client.getUser());
        binding.setAppConfig(appConfig);
        binding.btnLogOut.setOnClickListener(view -> logOut());

        binding.btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appConfig.reset();
                binding.invalidateAll();
            }
        });

        binding.btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @InverseBindingAdapter(attribute = "android:text")
    public static int getIntegerFromBinding(EditText view) {
        String string = view.getText().toString();
        return string.isEmpty() ? 0 : Integer.parseInt(string);
    }

    @BindingAdapter("android:text")
    public static void bindIntegerInText(EditText tv, int value) {
        tv.setText(String.valueOf(value));
        tv.setSelection(tv.getText().length());
    }

    public void logOut() {
        showProgress();
        getDeviceId();
    }

    private void getDeviceId() {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                String token = task.getResult().getToken();
                                removeDevice(token);
                            } else {
                                onError(getString(R.string.error_getting_firebase_token));
                            }

                        }
                );
    }

    private void removeDevice(String deviceId) {
        Client client = StreamChat.getInstance(getContext());
        client.removeDevice(deviceId, new CompletableCallback() {

            @Override
            public void onSuccess(CompletableResponse response) {
                ProfileFragment.this.onSuccess();
            }

            @Override
            public void onError(String errMsg, int errCode) {
                ProfileFragment.this.onError(
                        getString(R.string.error_removing_device, errMsg, errCode)
                );
            }
        });
    }

    private void showProgress() {
        pd = new ProgressDialog(getContext());
        pd.setMessage(getString(R.string.logging_out));
        pd.show();
    }

    private void hideProgress() {
        pd.dismiss();
    }

    private void onSuccess() {
        hideProgress();
        Client client = StreamChat.getInstance(getContext());
        client.disconnect();
        StreamChat.getNavigator().navigate(new LoginDestination(getContext()));
        getActivity().finish();
        ((BaseApplication) getContext().getApplicationContext()).getAppConfig().setCurrentUser(null);
    }

    private void onError(String message) {
        hideProgress();
        errorDialog = new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> errorDialog.dismiss()).create();
        errorDialog.show();
    }

    private void updateAppConfigView(FragmentProfileBinding binding) {
        //binding.
    }
}
