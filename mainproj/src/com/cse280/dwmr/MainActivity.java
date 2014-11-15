package com.cse280.dwmr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity
{
    LinearLayout imageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button setloc = (Button) findViewById(R.id.btSetLoc);
        Button notes = (Button) findViewById(R.id.btNotes);
        Button findcar = (Button) findViewById(R.id.btFindCar);
        Button pic = (Button) findViewById(R.id.btTakePic);
        imageLayout = (LinearLayout) findViewById(R.id.imageLayout);

        setloc.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                // TODO get the longitude and laittude
                // float latitude =
                // float longitude =
                // e.putFloat("latitude", latitude);
                // e.putFloat("longitude", longitude);
                e.commit();
            }
        });

        findcar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                if (sp.contains("longitude") && sp.contains("latitude"))
                {
                    float latitude = sp.getFloat("latitude", 0.0f);
                    float longitude = sp.getFloat("longitude", 0.0f);
                    // TODO: send it to the map
                }
                else
                    Toast.makeText(v.getContext(), "No location stored", Toast.LENGTH_SHORT).show();
            }
        });

        pic.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i, Constants.TAKE_PICTURE);
            }
        });

        notes.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO start note taking activity
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i)
    {
        switch (requestCode)
        {
            case Constants.TAKE_PICTURE:
                if (resultCode == RESULT_OK)
                {
                    ImageView imgView = new ImageView(this);
                    Bitmap bm = (Bitmap) i.getExtras().get("data");
                    if (bm == null)
                    {
                        Toast.makeText(this, "Error getting picture", Toast.LENGTH_SHORT).show();
                        Log.e("DWMR", "Problem getting picture from camera result intent");
                    }
                    else
                    {
                        imgView.setImageBitmap(bm);
                        imgView.setPadding(5, 5, 5, 5);
                        imgView.setOnClickListener(new ImageListener(bm));
                        imageLayout.addView(imgView);
                    }
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // TODO implement this
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO implement this
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ImageListener implements OnClickListener
    {
        Bitmap mBitmap;

        public ImageListener(Bitmap b)
        {
            mBitmap = b;
        }

        @Override
        public void onClick(View v)
        {
            Intent i = new Intent(v.getContext(), PictureActivity.class);
            i.putExtra("pic", mBitmap);
            startActivity(i);
        }
    }
}
