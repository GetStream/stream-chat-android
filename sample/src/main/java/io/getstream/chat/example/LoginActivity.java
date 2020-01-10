package io.getstream.chat.example;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.google.firebase.iid.FirebaseInstanceId;

import io.getstream.chat.example.adapter.UserListItemAdapter;
import io.getstream.chat.example.navigation.HomeDestination;
import io.getstream.chat.example.utils.AppDataConfig;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ListView lv_users = findViewById(R.id.lv_users);

        if (AppDataConfig.getCurrentUser() != null) {
            StreamChat.getNavigator().navigate(new HomeDestination(this));
            finish();
            return;
        }
        if (AppDataConfig.getUsers() == null) {
            Toast.makeText(this, R.string.failed_load_json, Toast.LENGTH_SHORT).show();
            return;
        }

        UserListItemAdapter adapter = new UserListItemAdapter(this, AppDataConfig.getUsers());
        lv_users.setAdapter(adapter);
        lv_users.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l) -> {
            AppDataConfig.setCurrentUser(AppDataConfig.getUsers().get(position).getId());
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
