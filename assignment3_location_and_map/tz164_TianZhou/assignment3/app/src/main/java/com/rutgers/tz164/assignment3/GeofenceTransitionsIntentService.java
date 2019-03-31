package com.rutgers.tz164.assignment3;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yx960203 on 2017/11/6.
 */

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = "GeofenceTransitionsIS";
    private List<Geofence> triggeringGeofences;
    public static final int MODE_APPEND = 32768;

    private String triggeringGeofencesIdsString;
    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        Log.i("onHandleIntent", "onHandleIntent:here ");

        final int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Handler mHandler3 = new Handler(getMainLooper());
        mHandler3.post(new Runnable() {
            @Override
            public void run() {
               // Toast.makeText(getApplicationContext(),"geofenceTransition is "+ String.valueOf(geofenceTransition) , Toast.LENGTH_SHORT).show();
                Log.i("geofenceTransition","geofenceTransition is "+ String.valueOf(geofenceTransition) );
            }
        });
        // Test that the reported transition was of interest.

        //-------------------------  ENTER GEOFENCE  ---------------------------------
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {
            triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                    triggeringGeofences);
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.

            Handler mHandler1 = new Handler(getMainLooper());
            mHandler1.post(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences prefs = getSharedPreferences("currentfence",MODE_PRIVATE );//open namelst to store countId - name pair and name - count pairs//**********************
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.remove("currentfence");
                    prefsEditor.putString("currentfence",triggeringGeofencesIdsString);//set flag to notify mainactivity fragment to update list
                    prefsEditor.apply();
                    Toast.makeText(getApplication(),"Enter! current fence is : " + triggeringGeofencesIdsString, Toast.LENGTH_SHORT).show();
                }
            });

            Log.i("enter determine", geofenceTransitionDetails);
        }
        //  ---------------------    EXIT GEOFENCE ----------------------------
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                    triggeringGeofences);

            Handler mHandler2 = new Handler(getMainLooper());
            mHandler2.post(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences prefs = getSharedPreferences("currentfence",MODE_PRIVATE );//open namelst to store countId - name pair and name - count pairs//**********************
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.remove("currentfence");
               //     prefsEditor.putString("currentfence",triggeringGeofences.toString());//set flag to notify mainactivity fragment to update list
                    prefsEditor.apply();
                    Toast.makeText(getApplicationContext(),"Exit! current fence is : " + triggeringGeofencesIdsString, Toast.LENGTH_SHORT).show();
                }
            });
            Log.i("exit determine", geofenceTransitionDetails);
        }
        else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }
}