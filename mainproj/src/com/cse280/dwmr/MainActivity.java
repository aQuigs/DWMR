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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends ActionBarActivity
{
    boolean      triggerZoom;
    LinearLayout imageLayout;
    GPSTracker   gps;
    GoogleMap    map;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button setloc = (Button) findViewById(R.id.btSetLoc);
        Button notes = (Button) findViewById(R.id.btNotes);
        Button findcar = (Button) findViewById(R.id.btFindCar);
        Button pic = (Button) findViewById(R.id.btTakePic);
        Button clearloc = (Button) findViewById(R.id.btClearCarLoc);

        imageLayout = (LinearLayout) findViewById(R.id.imageLayout);
        gps = new GPSTracker(this);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        // set up map settings
        map.setMyLocationEnabled(true);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);

        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);

        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setScrollGesturesEnabled(true);

        map.setOnMapLongClickListener(new DWMRClickListener());

        setloc.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Location loc = gps.getLocation();
                if (loc == null)
                {
                    gps.showSettingsAlert();
                }
                else
                {
                    setRidePos((float) loc.getLatitude(), (float) loc.getLongitude());
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
                if (hasRideLocationStored(sp))
                {
                    // provide default for legacy purposes (allows support for
                    // older versions of android) even though it will never be
                    // used
                    float latitude = sp.getFloat(Constants.LATITUDE_KEY, 0.0f);
                    float longitude = sp.getFloat(Constants.LONGITUDE_KEY, 0.0f);
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

        clearloc.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // remove markers, images, ride location, and notes
                map.clear();
                imageLayout.removeAllViews();
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().clear().commit();
                getSharedPreferences(Constants.NOTE_PREF, MODE_PRIVATE).edit().clear().commit();
                Toast.makeText(MainActivity.this, "Ride location data was cleared", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setRidePos(float latitude, float longitude)
    {
        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        e.putFloat(Constants.LATITUDE_KEY, latitude);
        e.putFloat(Constants.LONGITUDE_KEY, longitude);
        map.clear();
        map.addMarker(new MarkerOptions().title(Constants.MARKER_TITLE).draggable(false)
                .position(new LatLng((double) latitude, (double) longitude)));
        e.commit();
    }

    private boolean hasRideLocationStored(SharedPreferences sp)
    {
        return sp.contains("longitude") && sp.contains("latitude");
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
                        imgView.setPadding(5, 0, 5, 5);
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
    protected void onStop()
    {
        gps.stopUsingGPS();
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if (hasFocus && triggerZoom)
        {
            LatLng singlePos = null;
            triggerZoom = false;
            map.clear();
            int locCode = 0;

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            if (hasRideLocationStored(sp))
            {
                ++locCode;
                float lat = sp.getFloat(Constants.LATITUDE_KEY, 0.0f);
                float lng = sp.getFloat(Constants.LONGITUDE_KEY, 0.0f);
                singlePos = new LatLng((double) lat, (double) lng);

                map.addMarker(new MarkerOptions().title(Constants.MARKER_TITLE).position(singlePos).draggable(false));
                builder.include(singlePos);
            }

            Location loc = gps.getLocation();
            if (loc == null)
                gps.showSettingsAlert();
            else
            {
                locCode += 2;
                LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
                builder.include(pos);
                if (locCode == 2)
                    singlePos = pos;
            }

            if (locCode == 3)
            {
                // zoom to show ride and current position with 10% padding
                View v = getFragmentManager().findFragmentById(R.id.map).getView();
                int width = v.getMeasuredWidth();
                int height = v.getMeasuredHeight();
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height,
                        (int) (Math.min(width, height) * 0.10)));
            }
            else
            {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(singlePos, 17.5f));
            }
        }

        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onStart()
    {
        triggerZoom = true;
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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

    public class DWMRClickListener implements OnMapLongClickListener
    {
        @Override
        public void onMapLongClick(LatLng pos)
        {
            setRidePos((float) pos.latitude, (float) pos.longitude);
            Toast.makeText(MainActivity.this, "Stored custom ride location", Toast.LENGTH_LONG).show();
        }
    }
}
