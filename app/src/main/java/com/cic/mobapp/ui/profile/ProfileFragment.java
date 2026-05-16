package com.cic.mobapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.cic.mobapp.databinding.FragmentProfileBinding;
import com.cic.mobapp.ui.admin.AdminActivity;
import com.cic.mobapp.ui.auth.LoginActivity;
import com.cic.mobapp.util.TokenManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel       viewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            binding.tvUsername.setText(user.username);
            binding.tvRole.setText(user.role);

            // Joined date
            String joined = user.createdAt != null && user.createdAt.length() >= 10
                    ? "Joined " + user.createdAt.substring(0, 10) : "";
            binding.tvJoined.setText(joined);

            // Level + XP progress
            int xpInLevel = user.xp % 1000;
            binding.tvLevel.setText("Level " + user.level);
            binding.tvXpLabel.setText(xpInLevel + " / 1000 XP");
            binding.tvTotalXp.setText(String.valueOf(user.xp));
            binding.tvLevelBig.setText(String.valueOf(user.level));
            binding.progressXp.setProgress(xpInLevel);
            binding.tvXpNext.setText((1000 - xpInLevel) + " XP to Level " + (user.level + 1));

            // Avatar
            Glide.with(this).load(user.avatarUrl).circleCrop()
                    .placeholder(com.cic.mobapp.R.drawable.bg_avatar_circle)
                    .into(binding.imgAvatar);

            // Admin button
            boolean isAdmin = "Administrator".equals(user.role);
            binding.btnAdminPanel.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        });

        binding.btnSettings.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        binding.btnAdminPanel.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AdminActivity.class)));

        binding.btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign out?")
                .setMessage("You will be returned to the login screen.")
                .setPositiveButton("Sign out", (d, w) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        new TokenManager(requireContext()).clearTokens();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
