package com.example.project;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class audio extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST = 1;
    public MediaPlayer mySong;
    Uri uri;
    String path;

    //begin
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_page);
    }

    //select
    public void select_audio(View view) {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            path = uri.getPath();
            mySong = MediaPlayer.create(this, uri);
        }
    }

    //play
    public void PlayAudio(View view) {
        /** Play Song */
        mySong.start();
    }

    //stop
    public void StopAudio(View view) {
        /** Stop Song */
        mySong.release();
//        mySong = MediaPlayer.create(this, uri);
    }

    //pause
    public void PauseResumeAudio(View view) {
        mySong.pause();
    }

}


