package com.example.broadcast.receiver;
import android.os.Bundle;
public class DetailActivity extends BasicActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_detail);
        findViewById(R.id.back_home).setOnClickListener(v -> finish());
    }
}
