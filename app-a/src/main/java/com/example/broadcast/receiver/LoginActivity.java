package com.example.broadcast.receiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;

public class LoginActivity extends BasicActivity {
    private EditText account, password;
    private CheckBox showPassword;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_login);
        account = findViewById(R.id.account);
        password = findViewById(R.id.password);
        showPassword = findViewById(R.id.show_password);
        SharedPreferences prefs = DemoStore.get(this);
        account.setText(prefs.getString("account", ""));
        password.setText(prefs.getString("password", ""));
        showPassword.setChecked(prefs.getBoolean("show_password", false));
        updatePasswordVisibility();
        showPassword.setOnCheckedChangeListener((button, checked) -> {
            updatePasswordVisibility();
            saveInputs();
        });
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { saveInputs(); }
            public void afterTextChanged(Editable text) {}
        };
        account.addTextChangedListener(watcher);
        password.addTextChangedListener(watcher);
        findViewById(R.id.login).setOnClickListener(v -> {
            if (account.getText().toString().trim().isEmpty()) { account.setError("请输入演示账号"); return; }
            if (password.getText().length() == 0) { password.setError("请输入演示密码"); return; }
            saveInputs();
            startActivity(new Intent(this, HomeActivity.class));
            // Keep Login alive so the actual stack is Login -> Home -> Detail.
        });
    }
    private void updatePasswordVisibility() {
        password.setTransformationMethod(showPassword.isChecked()
                ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
        password.setSelection(password.length());
    }
    private void saveInputs() {
        DemoStore.get(this).edit().putString("account", account.getText().toString())
                .putString("password", password.getText().toString())
                .putBoolean("show_password", showPassword.isChecked()).apply();
    }
    @Override protected void onResume() {
        super.onResume();
        SharedPreferences prefs = DemoStore.get(this);
        String message = prefs.getString("message", "");
        ((TextView) findViewById(R.id.message)).setText(message.isEmpty() ? "尚未收到广播" : message);
        long time = prefs.getLong("received_at", 0);
        ((TextView) findViewById(R.id.received_time)).setText(time == 0
                ? "在 App B 中发送消息后，这里会持续显示接收结果。"
                : "已由 A 的 BroadcastReceiver 接收 · " + DateFormat.getDateTimeInstance().format(new Date(time)));
    }
}
