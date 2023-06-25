package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class start extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Intent intent_temp=getIntent();

        //change activity to audio
        Button selectAudioButton = findViewById(R.id.select_audio_button);
        selectAudioButton.setOnClickListener(view -> {
            Intent intent = new Intent(start.this,audio.class);                     //read another page(activity)
            startActivity(intent);
        });

        //change activity to image
        Button photo = findViewById(R.id.select_photo_button);
        photo.setOnClickListener(view -> {
            Intent intent = new Intent(start.this,image.class);                     //read another page(activity)
            startActivity(intent);
        });

        //post
        Button post = findViewById(R.id.post);
        post.setOnClickListener(view -> {
            Intent intent = new Intent(start.this,image_2.class);                 //read another page(activity)
            startActivity(intent);
        });

        //websocket
        Button websocket=findViewById(R.id.websocket);
        websocket.setOnClickListener(v->{
            Intent websocket_intent=new Intent(this, webSocket.class).putExtra("name",intent_temp.getStringExtra("name"));
            startActivity(websocket_intent);
        });
    }
}