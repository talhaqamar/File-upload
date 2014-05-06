package com.example.fileuploadtest;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
 
    private static final int SELECT_FILE1 = 1;
    private static final int SELECT_FILE2 = 2;
    String selectedPath1 = "NONE";
    String selectedPath2 = "NONE";
    TextView tv, res;
    ProgressDialog progressDialog;
    Button b1,b2,b3;
    HttpEntity resEntity;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.fileuploadtest.R.layout.activity_main);
 
        tv = (TextView)findViewById(com.example.fileuploadtest.R.id.tv);
        res = (TextView)findViewById(com.example.fileuploadtest.R.id.res);
        tv.setText(tv.getText() + selectedPath1 + "," + selectedPath2);
        b1 = (Button)findViewById(com.example.fileuploadtest.R.id.Button01);
        b2 = (Button)findViewById(com.example.fileuploadtest.R.id.Button02);
        b3 = (Button)findViewById(com.example.fileuploadtest.R.id.upload);
        b1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery(SELECT_FILE1);
            }
        });
        b2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery(SELECT_FILE2);
            }
        });
        b3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(selectedPath1.trim().equalsIgnoreCase("NONE")) && !(selectedPath2.trim().equalsIgnoreCase("NONE"))){
                    progressDialog = ProgressDialog.show(MainActivity.this, "", "Uploading files to server.....", false);
                     Thread thread=new Thread(new Runnable(){
                            public void run(){
                                doFileUpload();
                                runOnUiThread(new Runnable(){
                                    public void run() {
                                        if(progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    }
                                });
                            }
                    });
                    thread.start();
                }else{
                            Toast.makeText(getApplicationContext(),"Please select two files to upload.", Toast.LENGTH_SHORT).show();
                }
            }
        });
 
    }
 
    public void openGallery(int req_code){
 
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select file to upload "), req_code);
   }
 
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
 
        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (requestCode == SELECT_FILE1)
            {
                selectedPath1 = getPath(selectedImageUri);
                System.out.println("selectedPath1 : " + selectedPath1);
            }
            if (requestCode == SELECT_FILE2)
            {
                selectedPath2 = getPath(selectedImageUri);
                System.out.println("selectedPath2 : " + selectedPath2);
            }
            tv.setText("Selected File paths : " + selectedPath1 + "," + selectedPath2);
        }
    }
 
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
 
    private void doFileUpload(){
 
        File file1 = new File(selectedPath1);
        File file2 = new File(selectedPath2);
        String urlString = "http://10.0.2.2/upload_test/upload_media.php";
        try
        {
             HttpClient client = new DefaultHttpClient();
             HttpPost post = new HttpPost(urlString);
             FileBody bin1 = new FileBody(file1);
             FileBody bin2 = new FileBody(file2);
             MultipartEntity reqEntity = new MultipartEntity();
             reqEntity.addPart("uploadedfile1", bin1);
             reqEntity.addPart("uploadedfile2", bin2);
             reqEntity.addPart("user", new StringBody("User"));
             post.setEntity(reqEntity);
             HttpResponse response = client.execute(post);
             resEntity = response.getEntity();
             final String response_str = EntityUtils.toString(resEntity);
             if (resEntity != null) {
                 Log.i("RESPONSE",response_str);
                 runOnUiThread(new Runnable(){
                        public void run() {
                             try {
                                res.setTextColor(Color.GREEN);
                                res.setText("n Response from server : n " + response_str);
                                Toast.makeText(getApplicationContext(),"Upload Complete. Check the server uploads directory.", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                           }
                    });
             }
        }
        catch (Exception ex){
             Log.e("Debug", "error: " + ex.getMessage(), ex);
        }
      }
}