package com.bankbazaar.teamplayer;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private ImageView imageHolder;
    private final int requestCode = 20;
    private String apiKey;
    private String langCode;
    private final int RESPONSE_OK = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageHolder = (ImageView)findViewById(R.id.captured_photo);
        Button capturedImageButton = (Button)findViewById(R.id.photo_button);
        capturedImageButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                photoCaptureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
                startActivityForResult(photoCaptureIntent, requestCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(this.requestCode == requestCode && resultCode == RESULT_OK){
            Bitmap bitmap = (Bitmap)data.getExtras().get("data");

            String partFilename = currentDateFormat();
            storeCameraPhotoInSDCard(bitmap, partFilename);

            // display the image from SD Card to ImageView Control
            String storeFilename = "photo_" + partFilename + ".jpg";
            final File file = getImageFileFromSDCard(storeFilename);
            final ProgressDialog dialog = ProgressDialog.show( MainActivity.this, "Loading ...", "Converting to text.", true, false);
            apiKey = "cHcJqVV3WG";
            langCode = "en";
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final OCRServiceAPI apiClient = new OCRServiceAPI(apiKey);
                    apiClient.convertToText(langCode, file);

                    // Doing UI related code in UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();

                            // Showing response dialog
                            final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                            alert.setMessage(apiClient.getResponseText());
                            alert.setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick( DialogInterface dialog, int id) {
                                        }
                                    });

                            // Setting dialog title related from response code
                            if (apiClient.getResponseCode() == RESPONSE_OK) {
                                alert.setTitle("Success");
                            } else {
                                alert.setTitle("Faild");
                            }

                            alert.show();
                        }
                    });
                }
            });
            thread.start();
        }
    }

    private String currentDateFormat(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
        String  currentTimeStamp = dateFormat.format(new Date());
        return currentTimeStamp;
    }

    private void storeCameraPhotoInSDCard(Bitmap bitmap, String currentDate){
        File outputFile = new File(Environment.getExternalStorageDirectory(), "photo_" + currentDate + ".jpg");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private File getImageFileFromSDCard(String filename){
        Bitmap bitmap = null;
//        File imageFile = new File(Environment.getExternalStorageDirectory() + "/" + filename);
        File imageFile = new File("/sdcard/WhatsApp/Media/WhatsApp Images/IMG-20160720-WA0005.jpg");
        return imageFile;
    }

}