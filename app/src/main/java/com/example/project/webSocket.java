package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

public class webSocket extends AppCompatActivity {

    private WebSocketClient webSocketClient = null;
    ConstraintLayout rootlayout;
    TextView random_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_socket);
        rootlayout = findViewById(R.id.root_layout);

        connectWebSocket();

        Button create_random_value = findViewById(R.id.create_random_value);
        random_value = findViewById(R.id.random_output);
        EditText enter_keywords = findViewById(R.id.enter_keywords);
        Button post_keywords = findViewById(R.id.post_keywords);

        create_random_value.setOnClickListener(v -> {
            StringBuilder random_temp = new StringBuilder(generateRandomValue(10));
            Log.d("random", random_temp.toString());
            random_value.setText(random_temp.toString());
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("0", random_temp.toString());
                String jsonString = jsonObject.toString();
                webSocketClient.send(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        post_keywords.setOnClickListener(v -> {
            if (enter_keywords.length() < 1) {
                Toast.makeText(this, "null keywords", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("1", enter_keywords.getText());
                    String jsonString = jsonObject.toString();
                    webSocketClient.send(jsonString);
                    enter_keywords.setText(null);
                    enter_keywords.clearFocus();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @NonNull
    private String generateRandomValue(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.d("destory", "work");
        webSocketClient.close();
    }

    private void connectWebSocket() {
        URI uri_2;
        try {
            uri_2 = new URI("ws://192.168.1.108:8080/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        Log.d("connect_test", uri_2.toString());

        webSocketClient = new WebSocketClient(uri_2) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                // 連接成功
                Log.d("onOpen", "working to connect");
            }

            @Override
            public void onMessage(String message) {
                // 收到訊息
                Log.d("onMessage", message);
                runOnUiThread(() -> Toast.makeText(webSocket.this, message, Toast.LENGTH_SHORT).show());
                if (message.equals("client working connected")) {
                    runOnUiThread(() -> {
                        rootlayout.setBackgroundColor(Color.MAGENTA);
                        random_value.setText("Connecting");
                    });
                } else if (message.equals("disconnected")) {
                    runOnUiThread(() -> {
                        rootlayout.setBackgroundColor(Color.WHITE);
                        random_value.setText(message);
                    });
                } else if (message.length() > 1) {
                    runOnUiThread(() -> {
                        random_value.setText(message);
                    });
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                // 連線關閉
                Log.d("close", "close connect");
            }

            @Override
            public void onError(Exception ex) {
                // 連線錯誤
                Log.d("error", "error connect");
            }
        };

        webSocketClient.connect();

    }
}