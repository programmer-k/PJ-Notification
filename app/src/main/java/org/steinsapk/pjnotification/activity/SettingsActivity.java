package org.steinsapk.pjnotification.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.steinsapk.pjnotification.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 설정 창을 닫거나 나갈 때 JobScheduler에 JobService 재등록
        MyService.registerJobService(this);
    }
}
