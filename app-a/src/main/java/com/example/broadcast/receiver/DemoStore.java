package com.example.broadcast.receiver;

import android.content.Context;
import android.content.SharedPreferences;

/** Course demonstration only: never store a real password in plaintext. */
public final class DemoStore {
    private DemoStore() {}
    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences("broadcast_demo", Context.MODE_PRIVATE);
    }
}
