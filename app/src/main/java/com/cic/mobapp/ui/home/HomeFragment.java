package com.cic.mobapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.cic.mobapp.R;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import com.cic.mobapp.databinding.FragmentHomeBinding;
import com.cic.mobapp.databinding.ItemAnnouncementBinding;
import com.cic.mobapp.databinding.ItemEventBinding;
import com.cic.mobapp.ui.events.EventDetailActivity;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private EventsAdapter eventsAdapter;
    private AnnouncementsAdapter announcementsAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Events carousel
        eventsAdapter = new EventsAdapter();
        binding.rvUpcomingEvents.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvUpcomingEvents.setAdapter(eventsAdapter);

        // Announcements list
        announcementsAdapter = new AnnouncementsAdapter();
        binding.rvAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAnnouncements.setAdapter(announcementsAdapter);

        // User header
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            binding.tvWelcome.setText("Welcome back, " + user.username);
            binding.tvRole.setText(user.role);
            binding.tvLevel.setText("Lv " + user.level);

            int xpInLevel = user.xp % 1000;
            binding.progressXp.setProgress(xpInLevel);
            binding.tvXp.setText(xpInLevel + " / 1000 XP");
            binding.tvXpNext.setText((1000 - xpInLevel) + " to next level");

            Glide.with(this).load(user.avatarUrl).circleCrop()
                    .placeholder(R.drawable.bg_avatar_circle)
                    .into(binding.imgAvatar);
        });

        // Lists
        viewModel.getUpcomingEvents().observe(getViewLifecycleOwner(),
                events -> eventsAdapter.setData(events));
        viewModel.getAnnouncements().observe(getViewLifecycleOwner(),
                announcements -> announcementsAdapter.setData(announcements));

        // Quick actions — navigate to the matching bottom-nav tab
        binding.btnGoEvents.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.eventsFragment));
        binding.btnGoResources.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.resourcesFragment));
        binding.btnGoAnnouncements.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.announcementsFragment));

        // See all links
        binding.tvSeeAllEvents.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.eventsFragment));
        binding.tvSeeAllAnn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.announcementsFragment));
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    // ── Events carousel adapter ───────────────────────────────────────────────

    class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {
        private List<EventEntity> data = new ArrayList<>();

        void setData(List<EventEntity> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemEventBinding b = ItemEventBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            b.getRoot().getLayoutParams().width =
                    (int)(260 * parent.getContext().getResources().getDisplayMetrics().density);
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            EventEntity e = data.get(pos);
            h.b.tvEventTitle.setText(e.title);
            h.b.tvEventDate.setText(fmt(e.date) + "  ·  " + e.type);
            h.b.tvEventType.setText(e.difficulty);
            Glide.with(requireContext()).load(e.bannerUrl).centerCrop()
                    .placeholder(R.drawable.placeholder_banner).into(h.b.imgEventBanner);
            h.b.getRoot().setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), EventDetailActivity.class);
                i.putExtra(EventDetailActivity.EXTRA_EVENT_ID, e.id);
                startActivity(i);
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemEventBinding b;
            VH(ItemEventBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    // ── Announcements adapter ─────────────────────────────────────────────────

    class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.VH> {
        private List<AnnouncementDto> data = new ArrayList<>();

        void setData(List<AnnouncementDto> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemAnnouncementBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            AnnouncementDto a = data.get(pos);
            h.b.tvAnnTitle.setText(a.title);
            h.b.tvAnnBody.setText(a.body);
            h.b.tvAnnType.setText(a.type != null ? a.type : "General");
            h.b.tvAnnPinned.setVisibility(a.isPinned ? View.VISIBLE : View.GONE);
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAnnouncementBinding b;
            VH(ItemAnnouncementBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    private static String fmt(String iso) {
        return (iso != null && iso.length() >= 10) ? iso.substring(0, 10) : "";
    }
}
