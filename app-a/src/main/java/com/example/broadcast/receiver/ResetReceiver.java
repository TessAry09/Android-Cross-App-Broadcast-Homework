package com.example.broadcast.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ResetReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.example.broadcast.action.RESET";
    @Override public void onReceive(Context context, Intent intent) {
        if (!ACTION.equals(intent.getAction()) || !isOrderedBroadcast()) return;
        String message = intent.getStringExtra("message");
        String requestId = intent.getStringExtra("request_id");
        if (message == null || message.trim().isEmpty() || message.length() > 500
                || requestId == null) return;
        boolean saved = DemoStore.get(context).edit()
                .putString("message", message.trim())
                .putLong("received_at", System.currentTimeMillis()).commit();
        if (!saved) { setResultCode(Activity.RESULT_CANCELED); return; }
        int closed = ActivityCollector.finishAll();
        Log.i("BroadcastHomework", "receiver persisted message; closed=" + closed);
        setResultCode(Activity.RESULT_OK);
        setResultData(requestId);
        // Foreground App B opens Login only after this ordered result is delivered.
    }
}
