package com.seventythree_apps.takeorselectimage;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.seventythree_apps.takeorselectimage.library.TakeOrSelectImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends ActionBarActivity {

    public static final int REQUEST_CODE = 42;
    private TakeOrSelectImage takeOrSelectImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File externalStorageDirectory = getExternalFilesDir(null);
        File outputFile = new File(externalStorageDirectory, "temp.jpg");

        takeOrSelectImage = new TakeOrSelectImage.Builder()
                .outputFile(outputFile)
                .chooserTitle("Take or select an image")
                .requestCode(REQUEST_CODE).build();

        Button takeOrSelectImageButton = (Button)findViewById(R.id.take_or_select_image_btn);
        takeOrSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeOrSelectImage();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        takeOrSelectImage.onActivityResult(requestCode, resultCode, data, new TakeOrSelectImage.Callback() {
            @Override
            public void onComplete(boolean fromCamera, Uri imageUri) {
                showImage(imageUri);
            }

            @Override
            public void onError(boolean cancelled) {
                if (!cancelled) {
                    Toast.makeText(MainActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void takeOrSelectImage() {
        takeOrSelectImage.startFrom(this);
    }

    private void showImage(Uri imageUri) {
        InputStream imageInputStream;
        try {
            imageInputStream = getContentResolver().openInputStream(imageUri);
            ImageView imageView = (ImageView)findViewById(R.id.image_iv);
            imageView.setImageDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeStream(imageInputStream)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
