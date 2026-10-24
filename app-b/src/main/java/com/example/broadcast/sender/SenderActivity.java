package com.example.broadcast.sender;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.UUID;

public class SenderActivity extends Activity {
    private static final String A = "com.example.broadcast.receiver";
    private EditText message;
    private TextView status;
    private Button send;
    private boolean foreground, pendingOpen;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_sender);
        message = findViewById(R.id.send_message);
        status = findViewById(R.id.send_status);
        send = findViewById(R.id.send);
        message.setText(getPreferences(MODE_PRIVATE).getString("draft", "课程广播测试：请重新登录"));
        status.setText(getPreferences(MODE_PRIVATE).getString("status", "准备就绪 · 请先在 A 中登录并进入详情页"));
        send.setOnClickListener(v -> sendReset());
    }
    private void report(String text) {
        status.setText(text);
        getPreferences(MODE_PRIVATE).edit().putString("status", text).apply();
    }
    private void sendReset() {
        String text = message.getText().toString().trim();
        if (text.isEmpty()) { message.setError("请输入广播消息"); return; }
        try {
            getPackageManager().getActivityInfo(new ComponentName(A, A + ".LoginActivity"), 0);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            report("发送失败：未找到 App A，请先安装并打开 A。"); return;
        }
        getPreferences(MODE_PRIVATE).edit().putString("draft", text).apply();
        String requestId = UUID.randomUUID().toString();
        Intent broadcast = new Intent("com.example.broadcast.action.RESET")
                .setComponent(new ComponentName(A, A + ".ResetReceiver"))
                .putExtra("message", text).putExtra("request_id", requestId);
        send.setEnabled(false);
        report("正在等待 A 的广播接收回执……");
        try {
            sendOrderedBroadcast(broadcast, null, new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
                    send.setEnabled(true);
                    if (getResultCode() != RESULT_OK || !requestId.equals(getResultData())) {
                        report("发送失败：A 未确认接收。请打开 A 后重试，并检查两个 APK 的签名是否一致。");
                        return;
                    }
                    report("发送成功：A 已保存消息并清理旧活动。已请求返回登录页。");
                    pendingOpen = true;
                    openLoginWhenVisible();
                }
            }, null, RESULT_CANCELED, null, null);
        } catch (SecurityException e) {
            send.setEnabled(true);
            report("发送失败：广播权限被拒绝，请使用同一工程构建的 A、B。");
        }
    }
    private void openLoginWhenVisible() {
        if (!foreground || !pendingOpen || isFinishing() || isDestroyed()) return;
        pendingOpen = false;
        // No message extras: Login reads only the data already saved by A's receiver.
        Intent login = new Intent().setComponent(new ComponentName(A, A + ".LoginActivity"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try { startActivity(login); }
        catch (ActivityNotFoundException | SecurityException e) {
            report("广播已接收，但打开 A 失败，请点击 A 图标查看保存的消息。");
        }
    }
    @Override protected void onResume() { super.onResume(); foreground = true; openLoginWhenVisible(); }
    @Override protected void onPause() { foreground = false; super.onPause(); }
}
