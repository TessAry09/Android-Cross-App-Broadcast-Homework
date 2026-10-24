package com.example.broadcast.receiver;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
public class HomeActivity extends BasicActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_home);
        ((TextView) findViewById(R.id.welcome)).setText(getString(R.string.welcome_account, DemoStore.get(this).getString("account", "")));
        findViewById(R.id.open_detail).setOnClickListener(v -> startActivity(new Intent(this, DetailActivity.class)));
    }
}
