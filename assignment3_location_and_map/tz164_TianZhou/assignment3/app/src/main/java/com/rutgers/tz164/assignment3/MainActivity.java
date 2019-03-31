package com.rutgers.tz164.assignment3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends FragmentActivity implements OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener,OnCompleteListener<Void> {

    private static final String TAG = "MainActivity" ;
    private Button btn_checkin;
    private Button btn_map;
    private Button btn_refresh;
    private TextView txt_currentlocation;
    private ListView lst_location;
    private LocationManager lm;
    private GoogleApiClient mGoogleApiClient;
    private AddressResultReceiver mResultReceiver;
    protected Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private boolean mAddressRequested;
    private String mLastUpdateTime;
    private boolean mRequestingLocationUpdates = true;
    private  StringBuilder sb;
    private String mAddressOutput;
    private String address;
    private String longitude;
    private String latitude;
    private String parentaddress;
    public String currentfence;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private static int ACCESS_FINE_LOCATION = 1;
    private EditText edt_name;
    private MyDatabaseHelper myDatabaseHelper;//declare DatabaseHelper
    private SQLiteDatabase dbW;
    private SQLiteDatabase dbR;
    private MySimpleCursorAdapter mSimpleCursorAdapter;//declare customized CursorAdapter

    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;

    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_refresh = (Button) findViewById(R.id.btn_refresh);
        btn_checkin = (Button) findViewById(R.id.btn_checkin);
        btn_map = (Button) findViewById(R.id.btn_map);
        txt_currentlocation = (TextView) findViewById(R.id.txt_currentlocation);
        lst_location = (ListView) findViewById(R.id.lst_location);
        edt_name = (EditText) findViewById(R.id.edt_name);
        myDatabaseHelper = new MyDatabaseHelper(this,"l.db",null,1);
        dbW = myDatabaseHelper.getWritableDatabase();
        dbR = myDatabaseHelper.getReadableDatabase();
        mGeofenceList = new ArrayList<>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;
      //  getGeofencePendingIntent();
        mCurrentLocation = mLastLocation;
        populateGeofenceList();
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        //******************************     GET LOCATION    ***************************************

        if (!checkPermissions()) {
            requestPermissions();
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
         //   Toast.makeText(getApplication(), "connected", Toast.LENGTH_SHORT).show();
        }

        mResultReceiver = new AddressResultReceiver(null);
        mAddressRequested = true;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
            updateUIWidgets();
        }

        //--------------  start auto checkin service -------------
        Intent intent1 = new Intent(this, AutoCheckinService.class);
        intent1.putExtra(AutoCheckinService.Constants.RECEIVER, mResultReceiver);
        intent1.putExtra(AutoCheckinService.Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent1);

        Intent intent2 = new Intent(this, AutoCheckinLocation.class);
        intent2.putExtra(AutoCheckinLocation.Constants.RECEIVER, mResultReceiver);
        intent2.putExtra(AutoCheckinLocation.Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent2);

        //--------------- auto checkin service end ----------------

        //*************************    SET LISTVIEW   ******************************
        mSimpleCursorAdapter = new MySimpleCursorAdapter(MainActivity.this, R.layout.listview_item, null, new String[]{"address", "time", "longitude", "latitude", "name"},
                new int[]{R.id.itm_txt_address, R.id.itm_txt_time, R.id.itm_txt_longitude, R.id.itm_txt_latitude, R.id.itm_txt_name},"_id",
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        lst_location.setAdapter(mSimpleCursorAdapter);//set adapter for listview
        refreshListView();//initialize to show listview

        //click check in button to store location 
        btn_checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences prefs = getSharedPreferences("currentfence",MODE_PRIVATE );//get flag info to see whether need update information
                currentfence = prefs.getString("currentfence","default");//get flag
                if (currentfence == "default")
                {
                    currentfence = null;
                    addGeofences();//add geofence if now there is no fence
                }
                insertData();
                edt_name.setText("");

                if (!checkPermissions()) {
                    mPendingGeofenceTask = PendingGeofenceTask.ADD;
                    requestPermissions();
                    return;
                }
            }

        });

        //long press to delete stored location for test convenience
        lst_location.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() //longclick to delete
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id)
            {
                new AlertDialog.Builder(MainActivity.this)//create new dialog
                        .setTitle("Alert")//set dialog title
                        .setMessage("Delete this item?")//set dialog message
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() //set yes button
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                deleteData(position);//when click yes,delete data
                            }
                        })
                        .setNegativeButton("Cancel",null)//set no button
                        .show();
                return true;
            }
        });

        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });


        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshListView();
            }
        });

        //-------------  test delay and accuracy start  ------------------------
   /*     Calendar calendar = Calendar.getInstance();
        long unixTime = calendar.getTimeInMillis();//Timestamp

        Toast.makeText(getApplication(), calendar.get(Calendar.HOUR_OF_DAY) +"：" + calendar.get(Calendar.MINUTE) +
                "：" + calendar.get(Calendar.SECOND) ,Toast.LENGTH_LONG).show();
        //-------------  test delay and accuracy end  ---------------------------
*/
    }


    //-----------------------------------------  FUNCTIONS ----------------------------------------------------


    //**********************       UPDATE ADDRESS      *************************
    private synchronized void updateUIWidgets() {
        sb = new StringBuilder();
        mCurrentLocation = mLastLocation;//handle thread issues
        longitude = String.valueOf(mLastLocation.getLongitude());
        latitude = String.valueOf(mLastLocation.getLatitude());

        sb.append("Longitude: " + longitude + "\n");
        sb.append("Latitude: " + latitude + "\n");
        sb.append("Address: " + mAddressOutput + "\n");

        txt_currentlocation.setText(sb.toString());
     //   Toast.makeText(getApplication(), "updated", Toast.LENGTH_SHORT).show();

    }


    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            Toast.makeText(getApplicationContext(),"onStart insufficient permission",Toast.LENGTH_SHORT).show();
        } else {

            performPendingGeofenceTask();
            mGoogleApiClient.connect();
            if (mGoogleApiClient.isConnected() && mLastLocation != null) {
                Toast.makeText(getApplication(), "first?", Toast.LENGTH_SHORT).show();
                startIntentService();
            }
        }
     //   Toast.makeText(this, "start successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

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
         //   Toast.makeText(getApplication(), "startlocation aupdate", Toast.LENGTH_SHORT).show();
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
     //           Toast.makeText(getApplication(), "intent started", Toast.LENGTH_SHORT).show();
            }

            updateUIWidgets();

            //auto checkin by distance
            SharedPreferences prefs = getSharedPreferences("autocheckinlocation",MODE_PRIVATE );//open namelst to store countId - name pair and name - count pairs//**********************
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putString("longitude",String .valueOf(mLastLocation.getLongitude()));//save longitude
            prefsEditor.putString("latitude",String .valueOf(mLastLocation.getLatitude()));//save longitude
            prefsEditor.apply();
        }
        else
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
        refreshListView();

    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);

        Intent intent2 = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        startService(intent2);
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
                    Toast.makeText(getApplication(), "address is " + mAddressOutput, Toast.LENGTH_SHORT).show();
          /*         //test gps delay
                    Calendar calendar = Calendar.getInstance();
                    long unixTime = calendar.getTimeInMillis();//这是时间戳

                    Toast.makeText(getApplication(),"latlog: "+mLastLocation.getLatitude()+" s "+mLastLocation.getLongitude()+" s "+ calendar.get(Calendar.HOUR_OF_DAY) +"：" + calendar.get(Calendar.MINUTE) +
                            "：" + calendar.get(Calendar.SECOND) ,Toast.LENGTH_LONG).show();
                    */
                }
            });
            displayAddressOutput();
        }
    }

    private void displayAddressOutput() {
     //   updateUIWidgets();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUIWidgets();
            }
        });
    }

    //********************************     Database     *************************************


    public synchronized void refreshListView() {
        Cursor mCursor = dbW.query("location", null, null, null, null, null, null);//query database
        mSimpleCursorAdapter.changeCursor(mCursor);//refresh cursor adapter
       // lst_location.setAdapter(mSimpleCursorAdapter);
    }

    public void insertData(){
        ContentValues mContentValues = new ContentValues();//put value into content
        if (mAddressOutput == null)
        {
            mAddressOutput = "invalid address";
        }
        else
        {
            if (currentfence != null)//inside fence
            {
                parentaddress = currentfence;

                String name = null;
                Cursor mCursor = dbW.query("location", new String[]{"name"}, "address=?"  , new String[]{parentaddress}, null, null, null);//query database
                if(mCursor.moveToFirst()){
                    name = mCursor.getString(0);
                }

                Toast.makeText(getApplicationContext(),name,Toast.LENGTH_SHORT).show();
                mContentValues.put("name",name);//customized name
                mContentValues.put("named","1");
                mContentValues.put("associated","1");
            }
            else//outside fence
            {
                parentaddress = mAddressOutput;
                mContentValues.put("name",edt_name.getText().toString());//customized name
                if (edt_name.getText() == null)
                {
                    mContentValues.put("named","0");
                }
                else
                {
                    mContentValues.put("named","1");
                }
                mContentValues.put("associated","0");
            }
            mContentValues.put("address", parentaddress);
        }
        java.util.Date Time = new java.util.Date();

        SimpleDateFormat   formatter   =   new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date curDate =  new Date(System.currentTimeMillis());
        String   timestr   =   formatter.format(curDate);
        mContentValues.put("time", timestr);
        mContentValues.put("longitude", mLastLocation.getLongitude());
        mContentValues.put("latitude",mLastLocation.getLatitude());
        mContentValues.put("marker","0");
        dbW.insert("location", null, mContentValues);//insert into database

        //geofence

    //    Toast.makeText(getApplicationContext(), "Insert Success", Toast.LENGTH_SHORT).show();//toast success add message
        refreshListView();//refresh after added
    }

    private void deleteData(int position) {
        Cursor mCursor = mSimpleCursorAdapter.getCursor();//create new Cursor
        mCursor.moveToPosition(position);//move to the item
        int itemId = mCursor.getInt(mCursor.getColumnIndex("_id"));//get item id
        dbW.delete("location", "_id=?", new String[]{itemId + ""});//delete from database
        refreshListView();//reload adapter after deleted
    }

    public class MySimpleCursorAdapter extends SimpleCursorAdapter
    {
        private ArrayList<Integer> selection = new ArrayList<Integer>();//store seletcet item id
        private String mIdColumn;//_id in database

        public MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, String idColumn, int flags)// idcolumn added
        {
            super(context, layout, c, from, to, flags);//extend from super class
            mIdColumn = idColumn;//set mIdColumn
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)//get current item position
        {
            View view = super.getView(position, convertView, parent);

            Cursor cursor = getCursor();//create cursor
            cursor.moveToPosition(position);//set cursor to current place
            int rowId = cursor.getInt(cursor.getColumnIndexOrThrow(mIdColumn));//get item _id
            if (selection.indexOf(rowId)!= -1)
            {} else
            {}
            return view;
        }
    }

//******************************    GEOFENCE    ********************************

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions));
            return;
        }

        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(mAddressOutput)

                // Set the circular region of this geofence.
                .setCircularRegion(
                        mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude(),
                        Constants.GEOFENCE_RADIUS_IN_METERS
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(Geofence.NEVER_EXPIRE)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build());

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
  /*  private void removeGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions));
            return;
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }*/
    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            updateGeofencesAdded(!getGeofencesAdded());
            int messageId = getGeofencesAdded() ? R.string.geofences_added :
                    R.string.geofences_removed;
            Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    @SuppressLint("MissingPermission")
    private void populateGeofenceList() {

        Cursor mCursor = dbW.query("location", null, "marker=? and associated=?"  ,new String[]{"0","0"},null, null, null, null);//query database
        if(mCursor.moveToFirst()){
            do{
                mGeofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(mCursor.getString(mCursor.getColumnIndex("address")))

                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                Double.parseDouble(mCursor.getString(mCursor.getColumnIndex("latitude"))),
                                Double.parseDouble(mCursor.getString(mCursor.getColumnIndex("longitude"))),
                                Constants.GEOFENCE_RADIUS_IN_METERS
                        )

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)

                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)

                        // Create the geofence.
                        .build());
            }
            while (mCursor.moveToNext());

            if (mGeofenceList!=null)
            {
              //  mPendingGeofenceTask = PendingGeofenceTask.ADD;//***************************
              //  mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent()).addOnCompleteListener(this);
            }
        }
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }
    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private boolean getGeofencesAdded() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Constants.GEOFENCES_ADDED_KEY, false);
    }

    /**
     * Stores whether geofences were added ore removed in {@link SharedPreferences};
     *
     * @param added Whether geofences were added or removed.
     */
    private void updateGeofencesAdded(boolean added) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private void performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences();
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            mPendingGeofenceTask = PendingGeofenceTask.NONE;
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
                performPendingGeofenceTask();
                //start updating location once permission granted
                onStart();

            } else {// Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                mPendingGeofenceTask = PendingGeofenceTask.NONE;
            }
        }
    }

    @Override
    public void onResume()//on resume
    {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("currentfence",MODE_PRIVATE );//get flag info to see whether need update information
        currentfence = prefs.getString("currentfence","default");//get flag
        if (currentfence == "default")
        {
            currentfence = null;
        }
        refreshListView();
    }
}

