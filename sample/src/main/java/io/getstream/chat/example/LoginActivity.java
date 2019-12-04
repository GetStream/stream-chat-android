package io.getstream.chat.example;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import io.getstream.chat.example.adapter.UserListItemAdapter;
import io.getstream.chat.example.utils.AppDataStorage;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private UserListItemAdapter adapter;
    private ListView lv_users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lv_users = findViewById(R.id.lv_users);
        AppDataStorage.init(this);

        if (AppDataStorage.getCurrentUser() != null){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (AppDataStorage.getUsers() == null){
            Toast.makeText(this, R.string.failed_load_json, Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new UserListItemAdapter(this, AppDataStorage.getUsers());
        lv_users.setAdapter(adapter);
        lv_users.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l)-> {
            AppDataStorage.setCurrentUser(AppDataStorage.getUsers().get(position).getId());
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

}
