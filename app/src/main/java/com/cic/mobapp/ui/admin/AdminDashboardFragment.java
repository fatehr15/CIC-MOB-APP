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

        // Members — live count from Room (falls back to Room when API offline)
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null)
                binding.tvMembersCount.setText(String.valueOf(users.size()));
        });

        // Events
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null)
                binding.tvEventsCount.setText(String.valueOf(events.size()));
        });

        // Resources
        viewModel.getResources().observe(getViewLifecycleOwner(), resources -> {
            if (resources != null)
                binding.tvResourcesCount.setText(String.valueOf(resources.size()));
        });

        // Announcements — Room LiveData via AdminViewModel
        viewModel.getAnnouncements().observe(getViewLifecycleOwner(), anns -> {
            if (anns != null)
                binding.tvAnnouncementsCount.setText(String.valueOf(anns.size()));
        });

        // Quick action navigation
        binding.btnGoMembers.setOnClickListener(v -> navigate(1));
        binding.btnGoEvents.setOnClickListener(v -> navigate(2));
        binding.btnGoResources.setOnClickListener(v -> navigate(3));
        binding.btnGoAnnouncements.setOnClickListener(v -> navigate(4));

        binding.btnRefresh.setOnClickListener(v -> viewModel.refresh());
    }

    private void navigate(int tab) {
        if (requireActivity() instanceof AdminActivity)
            ((AdminActivity) requireActivity()).navigateToTab(tab);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
