package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class login extends AppCompatActivity {

    EditText account;
    String address = "192.168.1.104";
//    String address = "35.201.153.23";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /** 宣告變數 */
        account = findViewById(R.id.address);
        EditText password = findViewById(R.id.password);
        Button login = findViewById(R.id.login);
        Button register = findViewById(R.id.register);

        /** start service */
        startService(new Intent(this, WebSocket_Service.class));

        /** 按鈕監聽 */
        login.setOnClickListener(v -> {
            if (account.length() < 1 || password.length() < 1) {
                Toast.makeText(this, "請輸入帳號、密碼", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(account.getText().toString(), password.getText().toString());
                    String jsonString = jsonObject.toString();
                    new PostDate().execute(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        register.setOnClickListener(v -> {
            if (account.length() < 1 || password.length() < 1) {
                Toast.makeText(this, "請輸入帳號、密碼", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(account.getText().toString(), password.getText().toString());
                    String jsonString = jsonObject.toString();
                    new PostDate_2().execute(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    class PostDate extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                // on below line creating a url to post the data.
                URL url = new URL("http://" + address + ":8080/login");

                // on below line opening the connection.
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                // on below line setting method as post.
                client.setRequestMethod("POST");

                // on below line setting content type and accept type.
                //告訴服務器所發送的資料格式
                client.setRequestProperty("Content-Type", "application/json");
                //告訴服務器客戶端所能夠接受的回應格式
                //client.setRequestProperty("Accept", "image/png");

                //用於指示此連接是否允許輸出數據
                client.setDoOutput(true);

                // on below line we are creating an output stream and posting the data.
                try (OutputStream os = client.getOutputStream()) {
                    byte[] input = strings[0].getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                //讀到資料才會進行
                try (InputStream inputStream = client.getInputStream()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    if (stringBuilder.toString().equals("login successfully")) {
                        // 顯示 Toast 訊息
                        runOnUiThread(() -> Toast.makeText(login.this, stringBuilder.toString(), Toast.LENGTH_SHORT).show());
                        Intent intent = new Intent(login.this, webSocket.class);
                        startActivity(intent);
                        //
                        Intent intent1 = new Intent(login.this, WebSocket_Service.class);
                        intent1.setAction("ACTION_MESSAGE");
                        intent1.putExtra("state", "3");
                        intent1.putExtra("message", account.getText().toString());
                        startService(intent1);
                        finish();
                    } else {
                        runOnUiThread(() -> Toast.makeText(login.this, stringBuilder.toString(), Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(login.this, "登入操作失敗", Toast.LENGTH_SHORT).show());
            }
            return null;
        }
    }

    class PostDate_2 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                // on below line creating a url to post the data.
                URL url = new URL("http://" + address + ":8080/register");

                // on below line opening the connection.
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                // on below line setting method as post.
                client.setRequestMethod("POST");

                // on below line setting content type and accept type.
                //告訴服務器所發送的資料格式
                client.setRequestProperty("Content-Type", "application/json");
                //告訴服務器客戶端所能夠接受的回應格式
                //client.setRequestProperty("Accept", "image/png");

                // on below line setting client.
                //用於指示此連接是否允許輸出數據
                client.setDoOutput(true);

                // on below line we are creating an output stream and posting the data.
                try (OutputStream os = client.getOutputStream()) {
                    byte[] input = strings[0].getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                //讀到資料才會進行
                try (InputStream inputStream = client.getInputStream()) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    if (stringBuilder.toString().equals("working to create")) {
                        // 顯示 Toast 訊息
                        runOnUiThread(() -> Toast.makeText(login.this, stringBuilder.toString(), Toast.LENGTH_SHORT).show());
                        Intent intent = new Intent(login.this, webSocket.class).putExtra("name", account.getText().toString());
                        startActivity(intent);
                        //
                        Intent intent1 = new Intent(login.this, WebSocket_Service.class);
                        intent1.setAction("ACTION_MESSAGE");
                        intent1.putExtra("state", "3");
                        intent1.putExtra("message", account.getText().toString());
                        startService(intent1);
                        finish();
                    } else {
                        runOnUiThread(() -> Toast.makeText(login.this, stringBuilder.toString(), Toast.LENGTH_SHORT).show());
                    }
                } catch (IOException e) {
                    // 處理錯誤
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // on below line handling the exception.
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(login.this, "登入操作失敗", Toast.LENGTH_SHORT).show());
            }
            return null;
        }
    }

}