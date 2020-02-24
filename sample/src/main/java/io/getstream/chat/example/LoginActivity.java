package io.getstream.chat.example;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.getstream.sdk.chat.StreamChat;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.appcompat.app.AppCompatActivity;
import io.getstream.chat.android.client.utils.Result;
import io.getstream.chat.example.adapter.UserListItemAdapter;
import io.getstream.chat.example.navigation.HomeDestination;
import io.getstream.chat.example.utils.AppConfig;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

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

                            StreamChat.getInstance().addDevice(task.getResult().getToken()).enqueue(new Function1<Result<Unit>, Unit>() {
                                @Override
                                public Unit invoke(Result<Unit> unitResult) {
                                    return null;
                                }
                            });


                        }
                );
    }
}
