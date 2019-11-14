package io.getstream.chat.example.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.core.Client;

import io.getstream.chat.example.LoginActivity;
import io.getstream.chat.example.databinding.FragmentProfileBinding;
import io.getstream.chat.example.utils.UserStorage;

public class ProfileFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentProfileBinding binding = FragmentProfileBinding.inflate(inflater, container, false);

        if (UserStorage.getCurrentUser() == null){
            Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
        }else {
            String USER_NAME = UserStorage.getCurrentUser().getName();
            String USER_IMAGE = UserStorage.getCurrentUser().getImage();
            binding.setName(USER_NAME);
            binding.setImage(USER_IMAGE);
            binding.btnLogOut.setOnClickListener(view -> logOut());
        }
        return binding.getRoot();
    }

    public void logOut(){
        UserStorage.logout();
        Client client = StreamChat.getInstance(getContext());
        client.disconnect();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }
}
