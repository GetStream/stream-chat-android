package io.getstream.chat.example.view.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.getstream.sdk.chat.Chat;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.fragment.app.Fragment;
import io.getstream.chat.android.client.utils.Result;
import io.getstream.chat.example.App;
import io.getstream.chat.example.R;
import io.getstream.chat.example.databinding.FragmentProfileBinding;
import io.getstream.chat.example.navigation.LoginDestination;
import io.getstream.chat.example.utils.AppConfig;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileFragment extends Fragment {

    ProgressDialog pd;
    AlertDialog errorDialog;
    AppConfig appConfig;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentProfileBinding binding = FragmentProfileBinding.inflate(inflater, container, false);
        appConfig = ((App) getContext().getApplicationContext()).getAppConfig();
        binding.setUser(Chat.getInstance().getClient().getCurrentUser());
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

        Chat.getInstance().getClient().deleteDevice(deviceId).enqueue(new Function1<Result<Unit>, Unit>() {
            @Override
            public Unit invoke(Result<Unit> unitResult) {

                if (unitResult.isSuccess()) {
                    ProfileFragment.this.onSuccess();
                } else {
                    ProfileFragment.this.onError(
                            unitResult.error().getMessage()
                    );
                }

                return null;
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
        logOutAndGoToLoginScreen();
    }

    private void logOutAndGoToLoginScreen() {
        hideProgress();
        Chat.getInstance().getClient().disconnect();
        Chat.getInstance().getNavigator().navigate(new LoginDestination(getContext()));
        getActivity().finish();
        ((App) getContext().getApplicationContext()).getAppConfig().setCurrentUser(null);
    }

    private void onError(String message) {
        hideProgress();
        errorDialog = new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    errorDialog.dismiss();
                    logOutAndGoToLoginScreen();
                }).create();
        errorDialog.show();
    }

    private void updateAppConfigView(FragmentProfileBinding binding) {
        //binding.
    }
}
