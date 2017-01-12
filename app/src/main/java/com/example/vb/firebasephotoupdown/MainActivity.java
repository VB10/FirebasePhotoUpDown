package com.example.vb.firebasephotoupdown;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity {

    private ImageView img;
    private static final int CAMERA_REQUEST_CODE = 1111;
    private ProgressDialog mProgress;
    private Uri mImageCaptureUri;

    //resimleri kayıt edeceğimiz yer
    private StorageReference mFirebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this, "Interneti açmayı unutmayın..", Toast.LENGTH_SHORT).show();
        //firebase ulaşıyoruz
        mFirebaseStorage = FirebaseStorage.getInstance().getReference();
        img = (ImageView) findViewById(R.id.imgUpload);
        mProgress = new ProgressDialog(this);

    }

    public void capture(View view) {

        //bir intent oluşturup cihazımızın kamerasına erişerek fotoğraf çekme işlemini gerçekleyeceğiz

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //forresult metodu ile bizim ıntentimiz bir geriye değer döndürerek işlemi kontrol edecek
        //buradan dönen değer ActviityREsult func düşecek
        startActivityForResult(intent, CAMERA_REQUEST_CODE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //eğer dönen değer bizim yolladığımz değerle eşitse ve değer true ise işlemi başlatıyor
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            mProgress.setMessage("Uploading Image....");
            mProgress.show();

            //photos adında bir alt dal açıyor bize
            final StorageReference phothosRef = mFirebaseStorage.child("Photos");

            Bundle extras = data.getExtras();
            Bitmap bmp = (Bitmap) extras.get("data");

            //gelen resmi bitmap formatında alıp byte ceviriyourz
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] datas = baos.toByteArray();


            //ve resmi sisteme gönderiyoruz
            UploadTask upload = phothosRef.putBytes(datas);
            upload.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Eğer resim başarıyla upload olursa kayıt alıp gösteriyor
                    Uri dowloaduri = taskSnapshot.getDownloadUrl();
                    Picasso.with(getApplicationContext()).load(dowloaduri).into(img);
                    mProgress.dismiss();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        } else Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();


    }


}
