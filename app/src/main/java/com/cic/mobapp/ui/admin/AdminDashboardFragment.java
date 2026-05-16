package com.cic.mobapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.cic.mobapp.databinding.FragmentAdminDashboardBinding;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private AdminViewModel viewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentAdminDashboardBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            binding.tvMembersCount.setText(String.valueOf(stats.getOrDefault("totalUsers", 0)));
            binding.tvEventsCount.setText(String.valueOf(stats.getOrDefault("totalEvents", 0)));
            binding.tvResourcesCount.setText(String.valueOf(stats.getOrDefault("totalResources", 0)));
            binding.tvAnnouncementsCount.setText(String.valueOf(stats.getOrDefault("totalAnnouncements", 0)));
        });

        // Quick action navigation — tab indices: 0=Dashboard 1=Members 2=Events 3=Resources 4=Announcements
        binding.btnGoMembers.setOnClickListener(v -> navigate(1));
        binding.btnGoEvents.setOnClickListener(v -> navigate(2));
        binding.btnGoResources.setOnClickListener(v -> navigate(3));
        binding.btnGoAnnouncements.setOnClickListener(v -> navigate(4));

        binding.btnRefresh.setOnClickListener(v -> viewModel.refresh());
    }

    private void navigate(int tab) {
        if (requireActivity() instanceof AdminActivity) {
            ((AdminActivity) requireActivity()).navigateToTab(tab);
        }
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
