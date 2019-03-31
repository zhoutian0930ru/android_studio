package com.rutgers.pocketwallet.ui.reminders;

import android.content.Intent;
import android.os.Bundle;

import com.rutgers.pocketwallet.HeatmapsActivity;
import com.rutgers.pocketwallet.R;
import com.rutgers.pocketwallet.interfaces.IUserActionsMode;
import com.rutgers.pocketwallet.ui.BaseActivity;


public class NewReminderActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        @IUserActionsMode int mode = getIntent().getIntExtra(IUserActionsMode.MODE_TAG, IUserActionsMode.MODE_CREATE);
        String reminderId = getIntent().getStringExtra(NewReminderFragment.REMINDER_ID_KEY);
        replaceFragment(NewReminderFragment.newInstance(mode, reminderId), false);
    }

}
