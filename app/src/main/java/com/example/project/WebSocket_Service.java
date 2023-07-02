package com.example.project;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class WebSocket_Service extends Service {

    private WebSocketClient webSocketClient = null;
    String address = "192.168.1.108";
    Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("start", "created");
        connectWebSocket();
        intent = new Intent("service");
//        intent.putExtra("test", "work");
//        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            /** message */
            if (action != null && action.equals("ACTION_MESSAGE")) {
                String state = intent.getStringExtra("state");
                String message = intent.getStringExtra("message");
                if (message != null && state != null) {
                    Log.d("Received Message", state + ":" + message);
                    try {
                        webSocketClient.send(new JSONObject().put(state, message).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Log.d("onStartCommand", "test");
        return super.onStartCommand(intent, flags, startId);
    }

    private void connectWebSocket() {
        URI uri_2;
        try {
            uri_2 = new URI("ws://" + address + ":8080/");
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
                intent.putExtra("services", message);
                sendBroadcast(intent);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                // 收到二进制消息（图像数据）
                byte[] imageData = bytes.array();
                System.out.println("onMessage: Received image data");
                // 在这里处理接收到的图像数据
                intent.putExtra("services", "image");
                intent.putExtra("image", imageData);
                sendBroadcast(intent);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("destory", "work");
        webSocketClient.close();
    }

}