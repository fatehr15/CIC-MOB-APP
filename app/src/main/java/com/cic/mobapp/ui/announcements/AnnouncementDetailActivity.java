package com.cic.mobapp.ui.announcements;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import com.cic.mobapp.databinding.ActivityAnnouncementDetailBinding;
import com.google.gson.Gson;

public class AnnouncementDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ANNOUNCEMENT_JSON = "announcement_json";

    private ActivityAnnouncementDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnnouncementDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String json = getIntent().getStringExtra(EXTRA_ANNOUNCEMENT_JSON);
        if (json == null) { finish(); return; }

        AnnouncementDto a = new Gson().fromJson(json, AnnouncementDto.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Announcement");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.tvAnnTitle.setText(a.title);
        binding.tvAnnType.setText(a.type != null ? a.type : "General");
        binding.tvAnnPriority.setText(a.priority);
        binding.tvAnnBody.setText(a.body);
        binding.tvAnnPinned.setVisibility(a.isPinned ? View.VISIBLE : View.GONE);

        String date = a.createdAt != null && a.createdAt.length() >= 10
                ? a.createdAt.substring(0, 10) : "";
        binding.tvAnnMeta.setText(date);
    }
}
