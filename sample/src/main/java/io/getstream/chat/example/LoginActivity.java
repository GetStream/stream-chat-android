package io.getstream.chat.example;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import io.getstream.chat.example.adapter.UserListItemAdapter;
import io.getstream.chat.example.utils.UserConfig;
import io.getstream.chat.example.utils.UserStorage;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private UserListItemAdapter adapter;
    private List<UserConfig>users;
    private ListView lv_users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lv_users = findViewById(R.id.lv_users);
        UserStorage.init(this);

        if (UserStorage.getCurrentUser() != null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (UserStorage.getUsers() == null){
            Toast.makeText(this, R.string.failed_load_json, Toast.LENGTH_SHORT).show();
            return;
        }

        users = UserStorage.getUsers();
        adapter = new UserListItemAdapter(this, users);
        lv_users.setAdapter(adapter);
        lv_users.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l)-> {
            UserStorage.setCurrentUser(users.get(position).getId());
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

}
