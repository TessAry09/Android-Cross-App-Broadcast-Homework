package com.example.broadcast.receiver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public abstract class BasicActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        ActivityCollector.add(this);
        Log.i("BroadcastHomework", "create " + getClass().getSimpleName());
    }
    @Override protected void onDestroy() {
        ActivityCollector.remove(this);
        Log.i("BroadcastHomework", "destroy " + getClass().getSimpleName());
        super.onDestroy();
    }
}
