package com.getstream.sdk.chat.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;


public class TextViewUtils {
    interface Actions{
        interface BeforeChangedAction{
            void act(CharSequence s, int start, int count, int after);
        }

        interface TextChangedAction {
            void act(CharSequence s, int start, int before, int count);
        }

        interface AfterChangedAction {
            void act(Editable editable);
        }
    }

    public static void beforeTextChanged(TextView textView, Actions.BeforeChangedAction action){
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                action.act(s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //ignore
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //ignore
            }
        });
    }

    public static void onTextChanged(TextView textView, Actions.TextChangedAction action){
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                action.act(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //ignore
            }
        });
    }

    public static void afterTextChanged(TextView textView, Actions.AfterChangedAction action){
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //ignore
            }

            @Override
            public void afterTextChanged(Editable editable) {
                action.act(editable);
            }
        });
    }
}
