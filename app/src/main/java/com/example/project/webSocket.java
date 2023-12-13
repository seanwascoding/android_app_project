package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class webSocket extends AppCompatActivity {

    ConstraintLayout rootlayout;
    TextView random_value;
    String address = "192.168.1.104";
    //    String address = "35.201.153.23";
    Uri uri;
    File imageFile;
    Button select;
    EditText enter_keywords;
    Button download;
    Button delete;
    ArrayList<item> items = new ArrayList<>();
    Adapter adapter = new Adapter(items, R.layout.image_recycle);
    RecyclerView recyclerView;
    Button create_random_value;
    Button decryption_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_socket);
        rootlayout = findViewById(R.id.root_layout);

        create_random_value = findViewById(R.id.create_random_value);
        random_value = findViewById(R.id.random_output);
        enter_keywords = findViewById(R.id.enter_keywords);
        Button post_keywords = findViewById(R.id.post_keywords);
        select = findViewById(R.id.select);
        download = findViewById(R.id.download);
        delete = findViewById(R.id.delete);
        recyclerView = findViewById(R.id.image_gallery);
        decryption_button = findViewById(R.id.decryption_button);

        /** create random room key */
        create_random_value.setOnClickListener(v -> {
            StringBuilder random_temp = new StringBuilder(generateRandomValue(10));
            Log.d("random", random_temp.toString());
            random_value.setText(random_temp.toString());
            Intent intent = new Intent(this, WebSocket_Service.class);
            intent.putExtra("state", "0");
            intent.putExtra("message", random_temp.toString());
            intent.setAction("ACTION_MESSAGE");
            startService(intent);
        });

        //todo add situation about match room, such as boolean that about detect connecting, not detect enter_keywords.length
        /** match pair room */
        post_keywords.setOnClickListener(v -> {
            if (enter_keywords.length() < 1) {
                Toast.makeText(this, "null keywords", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, WebSocket_Service.class);
                intent.putExtra("state", "1");
                intent.putExtra("message", enter_keywords.getText().toString());
                intent.setAction("ACTION_MESSAGE");
                startService(intent);
            }
            enter_keywords.setText(null);
        });

        /** select image */
        select.setOnClickListener(v -> {
            items.clear();
            Log.d("select", "clicked");
            Intent i = new Intent();
            i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            i.setAction(Intent.ACTION_PICK); // only select image
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); //only available in Android API 18 and higher
            startActivityForResult(i, 1);
        });

        /** download / delete */
        download.setOnClickListener(v -> {
            if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
                for (int i = 0; i < items.size(); i++) {
                    String filename = items.get(i).image.toString();
                    File read_file = new File(filename);
                    File output_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), read_file.getName());
                    try {
                        // read file
                        FileInputStream inputStream = new FileInputStream(read_file);

                        // write file
                        FileOutputStream outputStream = new FileOutputStream(output_file);

                        // copy file (stream)
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        // close stream
                        outputStream.close();
                        inputStream.close();

                        // call system to update media (specific name)
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(output_file));
                        sendBroadcast(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // delete cache
                for (int i = 0; i < items.size(); i++) {
                    File cache_data = new File(items.get(i).image.toString());
                    if (cache_data.delete()) {
                        System.out.println("delete cache work");
                    } else {
                        System.out.println("no working delete cache");
                    }
                }
                Toast.makeText(this, "download complete", Toast.LENGTH_SHORT).show();
                // turn off button & reset
                download.setEnabled(false);
            } else {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            }
        });
        delete.setOnClickListener(v -> {
            // delete cache
            for (int i = 0; i < items.size(); i++) {
                File cache_data = new File(items.get(i).image.toString());
                if (cache_data.delete()) {
                    System.out.println("delete cache work");
                } else {
                    System.out.println("no working delete cache");
                }
            }

            // clear temp
            items.clear();
            recyclerView.setAdapter(adapter);
//            imageView.setImageBitmap(null);
            download.setEnabled(false);
            delete.setEnabled(false);
            decryption_button.setEnabled(false);
            select.setEnabled(true);
        });
        decryption_button.setOnClickListener(v -> {
            if (decryption_button.isEnabled()) {
                File[] file = new File[items.size()];
                for (int i = 0; i < items.size(); i++) {
                    file[i] = new File(String.valueOf(items.get(i).image));
                    Log.d("test", String.valueOf(items.get(i).image));
                }
                new PostData1().execute(file);
            }
        });

        /** broadcast */
        IntentFilter intentFilter = new IntentFilter("service");
        registerReceiver(broadcastReceiver, intentFilter);
        Intent intent = new Intent(this, WebSocket_Service.class);

        /** test websocket state */
        startService(intent);

        /** recyclerview */
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        int times;
        int i = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("services");
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            if (message.equals("client working connected")) {
                runOnUiThread(() -> {
                    rootlayout.setBackgroundColor(Color.MAGENTA);
                    random_value.setText("Connecting");
                    select.setVisibility(View.VISIBLE);
                    create_random_value.setVisibility(View.INVISIBLE);
                    decryption_button.setVisibility(View.VISIBLE);
                    decryption_button.setEnabled(false);
                });
            } else if (message.equals("disconnected")) {
                runOnUiThread(() -> {
                    rootlayout.setBackgroundColor(Color.WHITE);
                    random_value.setText(message);
                    select.setVisibility(View.INVISIBLE);
                    create_random_value.setVisibility(View.VISIBLE);
                    decryption_button.setVisibility(View.INVISIBLE);
                    decryption_button.setEnabled(false);
                });
            } else if (message.equals("image")) {
                Log.d("test", "receive");
                String filepath = intent.getStringExtra("image");
                uri = Uri.parse(filepath);
                items.add(new item(uri));
                i++;
                if (i == times) {
                    i = 0;
                    recyclerView.setAdapter(adapter);
                    if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
                        download.setEnabled(true);
                        delete.setEnabled(true);
                        decryption_button.setEnabled(!decryption_button.isEnabled());
                        select.setEnabled(false);
                    }
//                    Log.d("uri test", items.get(0).image.toString());
                } else {
                    Log.d("state", i + ":" + times);
                }
            } else if (message.length() > 1 && !message.startsWith("times")) {
                // receive message
                runOnUiThread(() -> random_value.setText(message));
            } else if (message.startsWith("times")) {
                items.clear();
//                runOnUiThread(() -> Toast.makeText(context, "error situation", Toast.LENGTH_SHORT).show());
                String substring = message.substring(message.indexOf("times") + "times".length());
                times = Integer.parseInt(substring);
                runOnUiThread(() -> Toast.makeText(context, String.valueOf(times), Toast.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() -> Toast.makeText(context, "error situation", Toast.LENGTH_SHORT).show());
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "entry", Toast.LENGTH_SHORT).show();
        if (requestCode == 1 && resultCode == RESULT_OK && data.getData() != null) {
            /** single image */
            uri = data.getData();
            String filePath = uri.toString();
            Log.d("filepath", filePath);
            imageFile = new File(filePath);
            /** 放入照片 */
            items.add(new item(uri));
            recyclerView.setAdapter(adapter);
            if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
                download.setEnabled(false);
                delete.setEnabled(true);
                select.setEnabled(false);
            }
            new PostData().execute(imageFile);
        } else if (requestCode == 1 && resultCode == RESULT_OK && data.getClipData() != null) {
            /** multiple image */
            ClipData clipData = data.getClipData();
            File[] file = new File[clipData.getItemCount()];
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                uri = item.getUri();
                items.add(new item(uri));
                file[i] = new File(String.valueOf(uri));
            }
            recyclerView.setAdapter(adapter);
            if (recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 0) {
                download.setEnabled(false);
                delete.setEnabled(true);
                select.setEnabled(false);
            }
            Toast.makeText(this, "ClipData", Toast.LENGTH_SHORT).show();
            new PostData().execute(file);
        } else {
            Toast.makeText(this, "返回頁面", Toast.LENGTH_SHORT).show();
            Log.d("error message", String.valueOf(resultCode));
            if (data == null) {
                Toast.makeText(this, "data is null", Toast.LENGTH_SHORT).show();
            }
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

    class PostData extends AsyncTask<File, Void, File> {
        @Override
        protected File doInBackground(File... file) {
            try {
                // on below line creating a url to post the data.
                URL url = new URL("http://" + address + ":8080/down");

                // on below line opening the connection.
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                // on below line setting client.
                //用於指示此連接是否允許輸出數據
                client.setDoOutput(true);

                // read timeout
                client.setReadTimeout(10000000);

                // on below line setting method as post.
                client.setRequestMethod("POST");

                //多部分表單數據格式
                //讓客戶端將各種不同類型的數據（例如文字、圖像、音頻等）作為單個HTTP請求傳送到服務器
                String boundary_temp = "----WebKitFormBoundary7MA4YWxkTrZu0g";
                client.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary_temp);

                // Create output stream
                DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());

                // Write the actual image data
                InputStream inputStream = null;
                String temp = UUID.randomUUID().toString(); //generateRandomValue(10);
                Log.d("uuid", temp);
                try {
                    String imageFieldName = "image";
                    for (int i = 0; i < file.length; i++) {
                        // Write the boundary and header information for the image data
                        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0g";
                        String fileName = temp + System.currentTimeMillis() + ".png";
                        String mimeType = "image/png";
                        String header = "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + imageFieldName + "\"; filename=\"" + fileName + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n";
                        outputStream.writeBytes(header);
                        inputStream = getContentResolver().openInputStream(items.get(i).image);
                        // copy file
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        Log.d("table", header);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // Closing boundary
                String footer = "\r\n--" + boundary_temp + "--\r\n";
                outputStream.writeBytes(footer);

                // Close the input/output stream
                if (inputStream != null) {
                    inputStream.close();
                }
                outputStream.close();

                // on below line creating and initializing buffer reader.
                try (BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))) {

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

                    Intent intent = new Intent(webSocket.this, WebSocket_Service.class);
                    intent.putExtra("state", "2");
                    intent.putExtra("message", response.toString());
                    intent.setAction("ACTION_MESSAGE");
                    startService(intent);
                }
            } catch (Exception e) {
                // on below line handling the exception.
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Fail to post the data : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            return null;
        }
    }

    class PostData1 extends AsyncTask<File, Void, File> {
        @Override
        protected File doInBackground(File... file) {
            try {
                // on below line creating a url to post the data.
                URL url = new URL("http://" + address + ":8080/decrytion");

                // on below line opening the connection.
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                // on below line setting client.
                //用於指示此連接是否允許輸出數據
                client.setDoOutput(true);

                // read timeout
                client.setReadTimeout(10000000);

                // on below line setting method as post.
                client.setRequestMethod("POST");

                //多部分表單數據格式
                //讓客戶端將各種不同類型的數據（例如文字、圖像、音頻等）作為單個HTTP請求傳送到服務器
                String boundary_temp = "----WebKitFormBoundary7MA4YWxkTrZu0g";
                client.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary_temp);

                // Create output stream
                DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());

                // Write the actual image data
//                InputStream inputStream = null;
                FileInputStream fileInputStream = null;
                String temp = UUID.randomUUID().toString(); //generateRandomValue(10);
                Log.d("uuid", temp);
                try {
                    String imageFieldName = "image";
                    for (int i = 0; i < file.length; i++) {
                        // Write the boundary and header information for the image data
                        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0g";
                        String fileName = temp + System.currentTimeMillis() + ".png";
                        String mimeType = "image/png";
                        String header = "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + imageFieldName + "\"; filename=\"" + fileName + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n";
                        outputStream.writeBytes(header);
//                        inputStream = getContentResolver().openInputStream(Uri.fromFile(file[i]));
                        fileInputStream = new FileInputStream(file[i]);
                        // copy file
                        byte[] buffer = new byte[4096];
                        int bytesRead;
//                        while ((bytesRead = inputStream.read(buffer)) != -1) {
//                            outputStream.write(buffer, 0, bytesRead);
//                        }
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        Log.d("table", header);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // Closing boundary
                String footer = "\r\n--" + boundary_temp + "--\r\n";
                outputStream.writeBytes(footer);

                // Close the input/output stream
//                if (inputStream != null) {
//                    inputStream.close();
//                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                outputStream.close();

                // on below line creating and initializing buffer reader.
                try (BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))) {

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

                    if (response.toString().equals("work"))
                        return null;

                    Intent intent = new Intent(webSocket.this, WebSocket_Service.class);
                    intent.putExtra("state", "4");
                    intent.putExtra("message", response.toString());
                    intent.setAction("ACTION_MESSAGE");
                    startService(intent);
                    items.clear();
                }
            } catch (Exception e) {
                // on below line handling the exception.
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Fail to post the data : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.d("destory", "work");
        Intent intent = new Intent(webSocket.this, WebSocket_Service.class);
        intent.setAction("CLOSING_WEBSOCKET");
        startService(intent);
        unregisterReceiver(broadcastReceiver);
    }
}