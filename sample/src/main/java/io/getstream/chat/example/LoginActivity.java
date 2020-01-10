package io.getstream.chat.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.google.firebase.iid.FirebaseInstanceId;

import io.getstream.chat.example.adapter.UserListItemAdapter;
import io.getstream.chat.example.navigation.HomeDestination;
import io.getstream.chat.example.utils.AppConfig;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private UserListItemAdapter adapter;
    private ListView lv_users;
    private AppConfig appConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        appConfig = ((BaseApplication) getApplicationContext()).getAppConfig();

        lv_users = findViewById(R.id.lv_users);

        if (appConfig.getCurrentUser() != null) {
            StreamChat.getNavigator().navigate(new HomeDestination(this));
            finish();
            return;
        }
        if (appConfig.getUsers() == null) {
            Toast.makeText(this, R.string.failed_load_json, Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new UserListItemAdapter(this, appConfig.getUsers());
        lv_users.setAdapter(adapter);
        lv_users.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            appConfig.setCurrentUser(appConfig.getUsers().get(position).getId());
            setPushToken();
            StreamChat.getNavigator().navigate(new HomeDestination(this));
            finish();
        });
    }

    private void setPushToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                return;
                            }

                            if (task.getResult() == null) {
                                StreamChat.getLogger().logE(this, "Filed to get firebase result. Result:" + task.getResult());
                                return;
                            }

                            StreamChat.getInstance(getApplicationContext()).addDevice(task.getResult().getToken(), new CompletableCallback() {
                                @Override
                                public void onSuccess(CompletableResponse response) {
                                    // device is now registered!
                                }

                                @Override
                                public void onError(String errMsg, int errCode) {
                                    // something went wrong registering this device, ouch!
                                }
                            });
                        }
                );
    }
}
