package com.cse280.dwmr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
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
    GPSTracker   gps;

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
        gps = new GPSTracker(this);

        setloc.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Location loc = gps.getLocation();
                if (loc == null)
                    gps.showSettingsAlert();
                else
                {
                    SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                            .edit();
                    float latitude = (float) loc.getLatitude();
                    float longitude = (float) loc.getLongitude();
                    e.putFloat("latitude", latitude);
                    e.putFloat("longitude", longitude);
                    e.commit();
                    Toast.makeText(v.getContext(), "Location stored", Toast.LENGTH_SHORT).show();
                }
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
                    // provide default for legacy purposes (allows support for
                    // older versions of android) even though it will never be
                    // used
                    float latitude = sp.getFloat("latitude", 0.0f);
                    float longitude = sp.getFloat("longitude", 0.0f);
                    String format = "google.navigation:q=" + latitude + "," + longitude + "&mode=w";

                    Uri uri = Uri.parse(format);

                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy()
    {
        gps.stopUsingGPS();
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
