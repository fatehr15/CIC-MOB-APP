package com.cic.mobapp.ui.admin;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.cic.mobapp.databinding.ActivityAdminBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class AdminActivity extends AppCompatActivity {

    ActivityAdminBinding binding;
    AdminViewModel viewModel;

    private static final String[] TABS = {
        "Dashboard", "Members", "Events", "Resources", "Announcements", "Audit"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Panel");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        AdminPagerAdapter adapter = new AdminPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(4);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, pos) -> tab.setText(TABS[pos])).attach();

        viewModel.getError().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        });
        viewModel.getToast().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    public void navigateToTab(int position) {
        binding.viewPager.setCurrentItem(position, true);
    }
}
