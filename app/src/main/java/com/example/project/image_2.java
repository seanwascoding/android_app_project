package com.example.project;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class image_2 extends AppCompatActivity {

    EditText picture_name;
    ImageView imageView;
    File imageFile;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image2);

        imageView = findViewById(R.id.imageView);
        picture_name=findViewById(R.id.text_name);

        //initialize
        imageView.setImageDrawable(null);

    }

    //select image
    public void image(View view) {
        if (picture_name.length()<1){
            Toast.makeText(this,"input the name",Toast.LENGTH_SHORT).show();
        }
        else{
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            //i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(i,1);
        }
    }

    public void post(View view) {
        if(picture_name.getText().toString().isEmpty() || imageView.getDrawable()==null || findViewById(R.id.post).isEnabled()){
            Toast.makeText(this, "Input the picture name & select photo", Toast.LENGTH_SHORT).show();
        }
        else{
            findViewById(R.id.encryption).setEnabled(false);
            /** Transfer file image to byte */
            new PostData().execute(imageFile);
        }
    }

    //encryption
    public void encryption(View view) throws JSONException {
        if(picture_name.getText().toString().isEmpty() || imageView.getDrawable()==null){
            Toast.makeText(this, "Input the picture name & select photo", Toast.LENGTH_SHORT).show();
        }
        else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(picture_name.getText().toString(), "4");
            String jsonString = jsonObject.toString();
            new PostDate_2().execute(jsonString);
        }
    }

    //decrypt
    public void decrypt(View view) throws JSONException {
        if(picture_name.getText().toString().isEmpty() || imageView.getDrawable()==null){
            Toast.makeText(this, "Input the picture name & select photo", Toast.LENGTH_SHORT).show();
        }
        else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(picture_name.getText().toString(), "5");
            String jsonString = jsonObject.toString();
            new PostDate_2().execute(jsonString);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data.getData() != null) {
            uri = data.getData();
            /** 將 URI 轉換為 File 對象(實際位置) */
            String filePath=getRealPathFromUri(this,uri);
            imageFile = new File(filePath);
            //Log.d("11111", filePath);

            /** 放入照片 */
            imageView.setImageURI(uri);

            findViewById(R.id.select_image).setEnabled(false);
            findViewById(R.id.text_name).setEnabled(false);

        }
        else {
            Toast.makeText(this,"return to page",Toast.LENGTH_SHORT).show();

        }
    }


    class PostData extends AsyncTask<File, Void, File> {
        @Override
        protected File doInBackground(File... file) {
            try {
                //import image
                //因為剛進來時會被包成array
                File imageFile = file[0];

                // on below line creating a url to post the data.
                URL url = new URL("http://192.168.1.108:8080/down2"); //192.168.1.108

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
                String imageFieldName ="image";
                String fileName = picture_name.getText().toString()+".png";
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
                }
            } catch (Exception e) {
                // on below line handling the exception.
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Fail to post the data : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            return null;
        }
    }

    class PostDate_2 extends AsyncTask<String, Void, String>{
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(String... strings){
            try {
                // on below line creating a url to post the data.
                URL url = new URL("http://192.168.1.108:8080/state2");

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
                    byte[] input = strings[0].getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                try (InputStream inputStream = client.getInputStream()) {

                    // 將 InputStream 轉換為 Bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    runOnUiThread(()->imageView.setImageBitmap(bitmap));

                    // 顯示 Toast 訊息
                    runOnUiThread(() -> Toast.makeText(image_2.this, "計算完成", Toast.LENGTH_SHORT).show());

                } catch (IOException e) {
                    // 處理錯誤
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // on below line handling the exception.
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(image_2.this, "Fail to post the data : " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

}