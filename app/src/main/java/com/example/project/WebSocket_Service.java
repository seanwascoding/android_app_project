package com.example.project;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class WebSocket_Service extends Service {

    private WebSocketClient webSocketClient = null;
    //    String address = "34.81.249.124";
    String address = "192.168.1.101";
    Intent intent = new Intent("service");

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("start", "created");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (webSocketClient == null) {
                Log.d("connect", "reconnect");
                connectWebSocket();
            }
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
            } else if (action != null && action.equals("CLOSING_WEBSOCKET")) {
//                Log.d("test", webSocketClient.getReadyState().toString());
                webSocketClient.close();
                webSocketClient = null;
            }
        }
//        Log.d("onStartCommand", "test");
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
            String[] temp = null;
            int i = 0;
            boolean lock = true;

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
                if (message.startsWith("filenames")) {
                    String substring = message.substring(message.indexOf("filenames") + "filenames".length());
                    temp = substring.split(",");
                    Log.d("file", Arrays.toString(temp));
                    lock = false;
                } else {
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                try {
                    byte[] imageData = bytes.array();
                    Log.d("onMessage", "Received image data");

                    while (lock) {
                        Thread.sleep(2000);
                        Log.d("wait", "waiting");
                    }

                    Log.d("temp state", temp.length + ":" + i);
                    //
                    if (temp.length > 0 && i != temp.length) {
                        Log.d("image", temp[i]);
                        File file = new File(getCacheDir(), temp[i]); // need to edit
                        FileOutputStream outputStream = new FileOutputStream(file);
                        outputStream.write(imageData);
                        outputStream.close();
                        i++;
                        // 在这里处理接收到的图像数据
                        intent.putExtra("services", "image");
                        intent.putExtra("image", file.getAbsoluteFile().toString());
                        sendBroadcast(intent);
                        if (i == temp.length) {
                            lock = true;
                            i = 0;
                            temp = null;
                        }
                    } else {
                        Log.d("error", "no image");
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
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
                Toast.makeText(WebSocket_Service.this, "restart app", Toast.LENGTH_SHORT).show();
            }
        };
        webSocketClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("destory", "work");
    }
}