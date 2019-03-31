package com.rutgers.pocketwallet.ui;

import android.content.Context;

import com.rutgers.pocketwallet.interfaces.IMainActivityListener;


public class MainFragment extends BaseFragment {

    protected IMainActivityListener mMainActivityListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivityListener = (IMainActivityListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainActivityListener = null;
    }

}
