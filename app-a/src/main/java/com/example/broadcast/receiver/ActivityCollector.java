package com.example.broadcast.receiver;

import android.app.Activity;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/** All calls run on the main thread (Activity lifecycle and onReceive). */
public final class ActivityCollector {
    private static final List<Activity> ACTIVITIES = new ArrayList<>();
    private ActivityCollector() {}
    public static void add(Activity activity) { ACTIVITIES.add(activity); }
    public static void remove(Activity activity) { ACTIVITIES.remove(activity); }
    public static int finishAll() {
        int count = ACTIVITIES.size();
        for (Activity activity : new ArrayList<>(ACTIVITIES)) {
            if (!activity.isFinishing()) activity.finish();
        }
        ACTIVITIES.clear();
        Log.i("BroadcastHomework", "finishAll count=" + count);
        return count;
    }
}
