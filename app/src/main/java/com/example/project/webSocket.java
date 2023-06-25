package com.example.project;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Random;

public class webSocket extends AppCompatActivity {

    private WebSocketClient webSocketClient = null;
    ConstraintLayout rootlayout;
    TextView random_value;
    String address = "192.168.1.108";
    ImageView imageView;
    Uri uri;
    File imageFile;
    Button select;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_socket);
        rootlayout = findViewById(R.id.root_layout);
        name=getIntent().getStringExtra("name");
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();

        Button create_random_value = findViewById(R.id.create_random_value);
        random_value = findViewById(R.id.random_output);
        EditText enter_keywords = findViewById(R.id.enter_keywords);
        Button post_keywords = findViewById(R.id.post_keywords);
        select = findViewById(R.id.select);
        imageView=findViewById(R.id.image_data);

        /** create randonm room key */
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

        /** match pair room */
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

        /** select image */
        select.setOnClickListener(v->{
            Log.d("select", "clicked");
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            //i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(i,1);
        });


        connectWebSocket();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data.getData() != null) {
            uri = data.getData();
            /** 將 URI 轉換為 File 對象(實際位置) */
            String filePath=getRealPathFromUri(webSocket.this,uri);
            imageFile = new File(filePath);
            Log.d("filepath", filePath);

            /** 放入照片 */
            imageView.setImageURI(uri);

            new PostData().execute(imageFile);

        }
        else{
            Toast.makeText(this,"返回頁面",Toast.LENGTH_SHORT).show();
        }
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
                try {
                    webSocketClient.send(new JSONObject().put("3", name).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                        select.setVisibility(View.VISIBLE);
                    });
                } else if (message.equals("disconnected")) {
                    runOnUiThread(() -> {
                        rootlayout.setBackgroundColor(Color.WHITE);
                        random_value.setText(message);
                        select.setVisibility(View.INVISIBLE);
                    });
                } else if (message.length() > 1) {
                    runOnUiThread(() -> {
                        random_value.setText(message);
                    });
                }
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                // 收到二进制消息（图像数据）
                byte[] imageData = bytes.array();
                System.out.println("onMessage: Received image data");
                // 在这里处理接收到的图像数据
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                runOnUiThread(() -> {
                    imageView.setImageBitmap(bitmap);
                    Toast.makeText(webSocket.this, "receive date", Toast.LENGTH_SHORT).show();
                });
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

    class PostData extends AsyncTask<File, Void, File> {
        @Override
        protected File doInBackground(File... file) {
            try {
                //import image
                //因為剛進來時會被包成array
                File imageFile = file[0];

                // on below line creating a url to post the data.
                URL url = new URL("http://" + address + ":8080/down"); //192.168.1.108

                // on below line opening the connection.
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                // on below line setting client.
                //用於指示此連接是否允許輸出數據
                client.setDoOutput(true);

                // on below line setting method as post.
                client.setRequestMethod("POST");

                //多部分表單數據格式
                //讓客戶端將各種不同類型的數據（例如文字、圖像、音頻等）作為單個HTTP請求傳送到服務器
                String boundary_temp = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
                client.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary_temp);

                // Create output stream
                DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());

                // Write the boundary and header information for the image data
                String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
                String imageFieldName = "image";
                String fileName = "test" + ".png";
                String mimeType = "image/png";
                String header = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + imageFieldName + "\"; filename=\"" + fileName + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n";
                outputStream.writeBytes(header);

                // Write the actual image data
                FileInputStream inputStream = new FileInputStream(imageFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();

                // Write the closing boundary
                String footer = "\r\n--" + boundary + "--\r\n";
                outputStream.writeBytes(footer);

                // Close the output stream
                outputStream.close();

                // on below line creating and initializing buffer reader.
                try (BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream(), "utf-8"))) {

                    // on below line creating a string builder.
                    StringBuilder response = new StringBuilder();

                    // on below line creating a variable for response line.
                    String responseLine = null;

                    // on below line writing the response
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                        Log.d("INPUT", response.toString());
                    }

                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Data has been posted to the API.", Toast.LENGTH_SHORT).show());

                    webSocketClient.send(new JSONObject().put("2", "send picture").toString());

                }
            } catch (Exception e) {
                // on below line handling the exception.
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Fail to post the data : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            return null;
        }
    }

    public String getRealPathFromUri(Context context, Uri uri) {
        String filePath = "";
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isExternalStorageDocument(uri)) {
                String[] split = documentId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                String[] split = documentId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                filePath = getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        Toast.makeText(getApplicationContext(), "transfer to file", Toast.LENGTH_SHORT).show();
        return filePath;
    }

    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.d("destory", "work");
        webSocketClient.close();
    }
}