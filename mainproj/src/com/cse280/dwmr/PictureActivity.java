package com.cse280.dwmr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class PictureActivity extends FullscreenActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if (i == null)
        {
            problemLoading("Starting intent was null for PictureActivity");
        }
        else
        {
            Bitmap b = (Bitmap) i.getExtras().get("pic");
            if (b == null)
            {
                problemLoading("Bitmap was null for PictureActivity");
            }
            else
            {
                setContentView(R.layout.activity_picture);
                ImageView iv = (ImageView) findViewById(R.id.ivFullImage);
                iv.setImageBitmap(b);
            }
        }
    }

    private void problemLoading(String details)
    {
        Toast.makeText(this, "Error getting image", Toast.LENGTH_LONG).show();
        Log.e("DWMR", details);
        finish();
    }
}
