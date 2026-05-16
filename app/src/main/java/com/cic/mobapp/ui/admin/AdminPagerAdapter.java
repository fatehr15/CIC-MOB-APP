package com.cic.mobapp.ui.admin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdminPagerAdapter extends FragmentStateAdapter {

    public AdminPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new AdminDashboardFragment();
            case 1: return new AdminUsersFragment();
            case 2: return new AdminEventsFragment();
            case 3: return new AdminResourcesFragment();
            case 4: return new AdminAnnouncementsFragment();
            case 5: return new AdminAuditFragment();
            default: return new AdminDashboardFragment();
        }
    }

    @Override
    public int getItemCount() { return 6; }
}
