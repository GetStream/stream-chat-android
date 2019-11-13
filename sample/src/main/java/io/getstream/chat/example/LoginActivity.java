package io.getstream.chat.example;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.getstream.chat.example.adapter.UserListItemAdapter;
import io.getstream.chat.example.utils.UserConfig;

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
        String json = loadJSONFromAsset();
        if (json != null){
            Gson gson = new Gson();
            users = gson.fromJson(json, new TypeToken<List<UserConfig>>(){}.getType());
            adapter = new UserListItemAdapter(this, users);
            lv_users.setAdapter(adapter);
            lv_users.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long l)-> {
                UserConfig userConfig = users.get(position);
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("id", userConfig.id);
                intent.putExtra("token", userConfig.token);
                intent.putExtra("name", userConfig.name);
                intent.putExtra("image", userConfig.image);
                startActivity(intent);
                finish();
            });
        }
    }

    public static final String USER_DATA_FILENAME = "users-config.json";

    public String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getAssets().open(USER_DATA_FILENAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
