package com.rutgers.tz164.assignment3;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.google.android.gms.internal.zzahg.runOnUiThread;

public class AutoCheckinLocation extends Service implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener{
    public AutoCheckinLocation() {
    }
    protected ResultReceiver mReceiver;
    private MyDatabaseHelper myDatabaseHelper;//declare DatabaseHelper
    private SQLiteDatabase dbW;
    private SQLiteDatabase dbR;
    private static final String TAG = "AutoCheckinLocation";
    private LocationManager lm;
    private GoogleApiClient mGoogleApiClient;
    private AddressResultReceiver mResultReceiver;
    protected Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private boolean mAddressRequested;
    private String mLastUpdateTime;
    private boolean mRequestingLocationUpdates = true;
    private StringBuilder sb;
    private String mAddressOutput;
    private String address;
    private String longitude;
    private String latitude;
    private String parentaddress;
    private ArrayList<String> addressFragments;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myDatabaseHelper = new MyDatabaseHelper(this, "l.db", null, 1);
        dbW = myDatabaseHelper.getWritableDatabase();
        dbR = myDatabaseHelper.getReadableDatabase();
        mCurrentLocation = mLastLocation;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            //Toast.makeText(getApplication(), "connected", Toast.LENGTH_SHORT).show();
        }

        //-----------------  on start moved here --------------------
        if (!checkPermissions()) {
            // requestPermissions();
            Toast.makeText(getApplicationContext(), "onStart insufficient permission", Toast.LENGTH_SHORT).show();
        } else {
            mGoogleApiClient.connect();
            if (mGoogleApiClient.isConnected() && mLastLocation != null) {
               // Toast.makeText(getApplication(), "Location service launched", Toast.LENGTH_SHORT).show();
                startIntentService();
            }
        }
        //-------------------  on start end ------------------------

        //delete receiver
        mResultReceiver = new AddressResultReceiver(null);
        mAddressRequested = true;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            //Toast.makeText(getApplication(), "Location Checkin Launched", Toast.LENGTH_SHORT).show();
            startIntentService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.rutgers.xy213.dailypath";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
        public static final int GEOFENCE_RADIUS_IN_METERS = 1;
        public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 999999999;

    }
    //--------------------  get latlog  ----------------------------

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "connection failed", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        if (mLastLocation != null) {
            // Toast.makeText(getApplication(), "mLastlocation not null", Toast.LENGTH_SHORT).show();
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, "no geocoder available",
                        Toast.LENGTH_LONG).show();
            }
            mAddressRequested = true;
            if (mAddressRequested) {
                startIntentService();
                //Toast.makeText(getApplication(), "intent started", Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(this, "no last location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        startIntentService();
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            Handler mHandler = new Handler(getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplication(), "Location Checkin address is " + mAddressOutput, Toast.LENGTH_SHORT).show();

                }
            });
            displayAddressOutput();
        }
    }

    private void displayAddressOutput() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkDistance();
            }
        });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    //------------------- database ----------------------

    public void insertDataLocation(){
        ContentValues mContentValues = new ContentValues();//put value into content
        if (mAddressOutput == null)
        {
            mAddressOutput = "invalid address";
        }
        else
        {
            parentaddress = mAddressOutput;
            mContentValues.put("name","AutoCheckinLocation");//customized name
            mContentValues.put("named","1");
            mContentValues.put("associated","0");
            mContentValues.put("address", parentaddress);
        }
        java.util.Date Time = new java.util.Date();
        SimpleDateFormat formatter   =   new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date curDate =  new Date(System.currentTimeMillis());
        String   timestr   =   formatter.format(curDate);
        mContentValues.put("time", timestr);
        mContentValues.put("longitude", mLastLocation.getLongitude());
        mContentValues.put("latitude",mLastLocation.getLatitude());
        mContentValues.put("marker","0");
        dbW.insert("location", null, mContentValues);//insert into database
       // Toast.makeText(getApplicationContext(), "Location Check in Insert Success", Toast.LENGTH_SHORT).show();//toast success add message
    }
    public void checkDistance()
    {
        SharedPreferences prefs = getSharedPreferences("autocheckinlocation",MODE_PRIVATE );//open namelst to store countId - name pair and name - count pairs//**********************
        //get distance
        Double beforelon = Double.parseDouble(prefs.getString("longitude",String .valueOf(mLastLocation.getLongitude()))) ;//save longitude
        Double beforelat = Double.parseDouble(prefs.getString("latitude",String .valueOf(mLastLocation.getLatitude()))) ;//save longitude
        Double R = 6371.0;
        Double D =Math.acos(Math.sin(beforelat)*Math.sin(mLastLocation.getLatitude())+Math.cos(beforelat)*Math.cos(mLastLocation.getLatitude())*Math.cos(mLastLocation.getLongitude()-beforelon))*R*1000;
        if (D > 100.0)
        {
            insertDataLocation();
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.remove("longitude");
            prefsEditor.remove("latitude");
            prefsEditor.putString("longitude",String .valueOf(mLastLocation.getLongitude()));//save longitude
            prefsEditor.putString("latitude",String .valueOf(mLastLocation.getLatitude()));//save longitude
            prefsEditor.apply();
        }
    }
}
