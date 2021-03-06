package com.cse280.dwmr;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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
    int          imageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle("Dude, Where's Your Ride?");
        imageCount = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.IMAGE_COUNT_KEY, 0);

        setContentView(R.layout.activity_main);
        Button setloc = (Button) findViewById(R.id.btSetLoc);
        Button notes = (Button) findViewById(R.id.btNotes);
        Button findcar = (Button) findViewById(R.id.btFindCar);
        Button pic = (Button) findViewById(R.id.btTakePic);
        Button clearloc = (Button) findViewById(R.id.btClearCarLoc);

        imageLayout = (LinearLayout) findViewById(R.id.imageLayout);
        loadSavedImages();

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
                File f = getImageFileName();
                if (f != null)
                {
                    i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(i, Constants.TAKE_PICTURE);
                }
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
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                alertDialog.setTitle("Clear Ride Location");
                alertDialog.setMessage("Are you sure you want to clear all ride location data?");

                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // remove markers, images, ride location, notes, and
                        // images
                        map.clear();
                        imageLayout.removeAllViews();
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().clear().commit();
                        getSharedPreferences(Constants.NOTE_PREF, MODE_PRIVATE).edit().clear().commit();

                        File storageDir = getStorageDirectory();
                        String[] contents = storageDir.list();
                        for (String s : contents)
                            new File(storageDir, s).delete();
                        storageDir.delete();
                        imageCount = 0;

                        Toast.makeText(MainActivity.this, "Ride location data was cleared", Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
            }
        });
    }

    protected void loadSavedImages()
    {
        for (int i = 1; i <= imageCount; ++i)
        {
            addImage(getImageFileName(i).getAbsolutePath());
        }
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

    private File getStorageDirectory()
    {
        File dir = new File(Environment.getExternalStorageDirectory(), "DWMR");

        if (!dir.exists())
            dir.mkdir();

        return dir;
    }

    private File getImageFileName()
    {
        String imageFileName = "RIDE_IMAGE_" + (imageCount + 1) + ".jpg";
        return new File(getStorageDirectory(), imageFileName);
    }

    private File getImageFileName(int id)
    {
        String imageFileName = "RIDE_IMAGE_" + id + ".jpg";
        return new File(getStorageDirectory(), imageFileName);
    }

    protected void addImage(String photoPath)
    {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(photoPath, bmOptions);

        int w = bmOptions.outWidth;
        int h = bmOptions.outHeight;

        int scale = Math.min(w, h) / 150;

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scale;
        bmOptions.inPurgeable = true;

        Bitmap bm = BitmapFactory.decodeFile(photoPath, bmOptions);

        if (bm == null)
        {
            Toast.makeText(this, "Error getting picture", Toast.LENGTH_SHORT).show();
            Log.e("DWMR", "Problem getting picture from camera result intent");
        }
        else
        {
            ImageView imgView = new ImageView(this);
            imgView.setImageBitmap(bm);
            imgView.setPadding(2, 0, 2, 5);
            imgView.setOnClickListener(new ImageListener(photoPath));
            imageLayout.addView(imgView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i)
    {
        switch (requestCode)
        {
            case Constants.TAKE_PICTURE:
                if (resultCode == RESULT_OK)
                {
                    addImage(getImageFileName().getAbsolutePath());
                    ++imageCount;
                }
        }
    }

    @Override
    protected void onPause()
    {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(Constants.IMAGE_COUNT_KEY, imageCount)
                .commit();
        gps.stopUsingGPS();
        super.onPause();
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
            else if (singlePos != null)
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

    public class ImageListener implements OnClickListener
    {
        String bitmapPath;

        public ImageListener(String pathToBmp)
        {
            bitmapPath = pathToBmp;
        }

        @Override
        public void onClick(View v)
        {
            Intent i = new Intent(v.getContext(), PictureActivity.class);
            i.putExtra("pic", bitmapPath);
            startActivity(i);
        }
    }

    public class DWMRClickListener implements OnMapLongClickListener
    {
        private LatLng clickLoc;

        @Override
        public void onMapLongClick(LatLng pos)
        {
            clickLoc = pos;
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

            alertDialog.setTitle("Store Custom Ride Location");
            alertDialog.setMessage("Would you like to select this as the location of your ride?");

            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    setRidePos((float) DWMRClickListener.this.clickLoc.latitude,
                            (float) DWMRClickListener.this.clickLoc.longitude);
                    Toast.makeText(MainActivity.this, "Stored custom ride location", Toast.LENGTH_LONG).show();
                }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });

            alertDialog.show();
        }
    }
}
