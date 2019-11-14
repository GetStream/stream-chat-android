package io.getstream.chat.example.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.core.Client;

import io.getstream.chat.example.LoginActivity;
import io.getstream.chat.example.databinding.FragmentProfileBinding;

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

        Intent i = getActivity().getIntent();
        String USER_NAME = i.getStringExtra("name");
        String USER_IMAGE = i.getStringExtra("image");
        binding.setName(USER_NAME);
        binding.setImage(USER_IMAGE);
        binding.btnLogOut.setOnClickListener(view -> {
            Client client = StreamChat.getInstance(getContext());
            client.disconnect();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
        return binding.getRoot();
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
