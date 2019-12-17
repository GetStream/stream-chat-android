package io.getstream.chat.example;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import io.getstream.chat.example.adapter.UserListItemAdapter;
import io.getstream.chat.example.utils.AppDataConfig;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private UserListItemAdapter adapter;
    private ListView lv_users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lv_users = findViewById(R.id.lv_users);

        if (AppDataConfig.getCurrentUser() != null){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (AppDataConfig.getUsers() == null){
            Toast.makeText(this, R.string.failed_load_json, Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new UserListItemAdapter(this, AppDataConfig.getUsers());
        lv_users.setAdapter(adapter);
        lv_users.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l)-> {
            AppDataConfig.setCurrentUser(AppDataConfig.getUsers().get(position).getId());
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

}
