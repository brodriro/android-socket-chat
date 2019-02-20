package com.demo.socketchat.ui.chat;

import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;

import com.demo.socketchat.R;

import androidx.appcompat.app.AppCompatActivity;


public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }

        setContentView(R.layout.activity_main);
    }
}
