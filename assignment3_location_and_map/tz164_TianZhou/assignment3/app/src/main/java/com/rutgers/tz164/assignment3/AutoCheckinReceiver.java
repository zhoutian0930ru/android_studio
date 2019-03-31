package com.rutgers.tz164.assignment3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoCheckinReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Intent callauto = new Intent(context, AutoCheckinService.class);
        context.startService(callauto);

       // throw new UnsupportedOperationException("Not yet implemented");
    }
}
