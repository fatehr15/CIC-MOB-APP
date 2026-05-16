package com.cic.mobapp.ui.resources;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.cic.mobapp.data.remote.dto.ResourceDto;
import com.cic.mobapp.databinding.ActivityResourceDetailBinding;
import com.google.gson.Gson;

public class ResourceDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RESOURCE_JSON = "resource_json";

    private ActivityResourceDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResourceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String json = getIntent().getStringExtra(EXTRA_RESOURCE_JSON);
        if (json == null) { finish(); return; }

        ResourceDto r = new Gson().fromJson(json, ResourceDto.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(r.category);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.tvResTitle.setText(r.title);
        binding.tvResType.setText(r.type);
        binding.tvResDifficulty.setText(r.difficulty);
        binding.tvResCategory.setText(r.category);
        binding.tvResDescription.setText(r.description);
        binding.tvUploadedBy.setText(r.uploadedBy != null ? r.uploadedBy : "CIC Team");

        if (r.tags != null && r.tags.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String tag : r.tags) sb.append("#").append(tag).append("  ");
            binding.tvTags.setText(sb.toString().trim());
        }

        binding.btnOpen.setOnClickListener(v -> {
            if (r.fileUrl != null && !r.fileUrl.isEmpty()
                    && !r.fileUrl.contains("example.com")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(r.fileUrl)));
            } else {
                Toast.makeText(this, "Resource link not available in demo mode",
                        Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT,
                    r.title + " — " + r.category + "\n" + r.description
                            + "\n\nShared from CIC App");
            startActivity(Intent.createChooser(share, "Share resource via"));
        });
    }
}
