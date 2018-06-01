package de.ostfalia.mobile.orgelhelfer.activitys;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;

import de.ostfalia.mobile.orgelhelfer.MidiConnectionManager;

/**
 * Created by Aaron on 01.06.2018.
 */

public class BaseActivity extends AppCompatActivity {

    private static final String LOG_TAG = BaseActivity.class.getSimpleName();

    @Override
    public void onPause() {
        super.onPause();
    }
}
