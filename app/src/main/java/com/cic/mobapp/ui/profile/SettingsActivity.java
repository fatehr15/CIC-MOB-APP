package com.cic.mobapp.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.cic.mobapp.databinding.ActivitySettingsBinding;
import com.cic.mobapp.ui.auth.LoginActivity;
import com.cic.mobapp.util.Constants;
import com.cic.mobapp.util.TokenManager;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    private static final String PREF_SETTINGS = "cic_settings";
    private static final String KEY_NOTIF_EVENTS  = "notif_events";
    private static final String KEY_NOTIF_ANNOUCEMENTS = "notif_announcements";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Populate account info from TokenManager
        TokenManager tm = new TokenManager(this);
        String userId = tm.getUserId();
        binding.tvUsername.setText(userId != null ? userId : "—");
        binding.tvRole.setText("—");

        // Load notification preferences
        SharedPreferences prefs = getSharedPreferences(PREF_SETTINGS, MODE_PRIVATE);
        binding.switchEventReminders.setChecked(prefs.getBoolean(KEY_NOTIF_EVENTS, true));
        binding.switchAnnouncements.setChecked(prefs.getBoolean(KEY_NOTIF_ANNOUCEMENTS, true));

        binding.switchEventReminders.setOnCheckedChangeListener((v, checked) ->
                prefs.edit().putBoolean(KEY_NOTIF_EVENTS, checked).apply());
        binding.switchAnnouncements.setOnCheckedChangeListener((v, checked) ->
                prefs.edit().putBoolean(KEY_NOTIF_ANNOUCEMENTS, checked).apply());

        // Clear cache
        binding.rowClearCache.setOnClickListener(v -> {
            getSharedPreferences("cic_read_announcements", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show();
        });

        // Sign out
        binding.btnSignOut.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Sign out?")
                        .setMessage("You will be returned to the login screen.")
                        .setPositiveButton("Sign out", (d, w) -> {
                            new TokenManager(this).clearTokens();
                            Intent i = new Intent(this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());
    }
}
