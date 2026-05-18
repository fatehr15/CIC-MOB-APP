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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.cic.mobapp.R;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.data.local.entity.ResourceEntity;
import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import com.cic.mobapp.data.remote.dto.ResourceDto;
import com.cic.mobapp.databinding.FragmentHomeBinding;
import com.cic.mobapp.databinding.ItemAnnouncementBinding;
import com.cic.mobapp.databinding.ItemEventBinding;
import com.cic.mobapp.databinding.ItemResourceBinding;
import com.cic.mobapp.ui.announcements.AnnouncementDetailActivity;
import com.cic.mobapp.ui.events.EventDetailActivity;
import com.cic.mobapp.ui.resources.ResourceDetailActivity;
import com.cic.mobapp.util.ReadTracker;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private EventsAdapter eventsAdapter;
    private ResourcesAdapter resourcesAdapter;
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

        eventsAdapter = new EventsAdapter();
        binding.rvUpcomingEvents.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvUpcomingEvents.setAdapter(eventsAdapter);

        resourcesAdapter = new ResourcesAdapter();
        binding.rvResources.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvResources.setAdapter(resourcesAdapter);

        announcementsAdapter = new AnnouncementsAdapter();
        binding.rvAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAnnouncements.setAdapter(announcementsAdapter);

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

        viewModel.getUpcomingEvents().observe(getViewLifecycleOwner(),
                events -> eventsAdapter.setData(events));
        viewModel.getResources().observe(getViewLifecycleOwner(),
                resources -> resourcesAdapter.setData(resources));
        viewModel.getAnnouncements().observe(getViewLifecycleOwner(),
                announcements -> announcementsAdapter.setData(announcements));
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    // ── Events adapter ────────────────────────────────────────────────────────

    class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {
        private List<EventEntity> data = new ArrayList<>();

        void setData(List<EventEntity> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemEventBinding b = ItemEventBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            // Fixed card width for horizontal carousel
            int widthPx = (int) (260 * parent.getContext().getResources().getDisplayMetrics().density);
            b.getRoot().getLayoutParams().width = widthPx;
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

    // ── Resources adapter ─────────────────────────────────────────────────────

    class ResourcesAdapter extends RecyclerView.Adapter<ResourcesAdapter.VH> {
        private List<ResourceEntity> data = new ArrayList<>();

        void setData(List<ResourceEntity> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemResourceBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            ResourceEntity r = data.get(pos);
            h.b.tvResourceTitle.setText(r.title);
            h.b.tvCategory.setText(r.category);
            h.b.tvDifficulty.setText(r.difficulty);
            h.b.tvResourceType.setText(r.type);
            h.b.imgResourceIcon.setImageResource(iconFor(r.type));
            h.b.getRoot().setOnClickListener(v -> {
                ResourceDto dto = toDto(r);
                Intent i = new Intent(requireContext(), ResourceDetailActivity.class);
                i.putExtra(ResourceDetailActivity.EXTRA_RESOURCE_JSON, new Gson().toJson(dto));
                startActivity(i);
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemResourceBinding b;
            VH(ItemResourceBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    // ── Announcements adapter ─────────────────────────────────────────────────

    class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.VH> {
        private List<AnnouncementDto> data = new ArrayList<>();
        private final ReadTracker tracker = new ReadTracker(requireContext());

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

            boolean unread = a.id != null && !tracker.isRead(a.id);
            h.b.dotUnread.setVisibility(unread ? View.VISIBLE : View.GONE);

            h.b.getRoot().setOnClickListener(v -> {
                if (a.id != null) tracker.markRead(a.id);
                h.b.dotUnread.setVisibility(View.GONE);
                Intent i = new Intent(requireContext(), AnnouncementDetailActivity.class);
                i.putExtra(AnnouncementDetailActivity.EXTRA_ANNOUNCEMENT_JSON,
                        new Gson().toJson(a));
                startActivity(i);
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAnnouncementBinding b;
            VH(ItemAnnouncementBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String fmt(String iso) {
        return (iso != null && iso.length() >= 10) ? iso.substring(0, 10) : "";
    }

    private static ResourceDto toDto(ResourceEntity e) {
        ResourceDto d = new ResourceDto();
        d.id = e.id; d.title = e.title; d.category = e.category;
        d.type = e.type; d.difficulty = e.difficulty; d.fileUrl = e.fileUrl;
        d.description = e.description; d.uploadedBy = e.uploadedBy;
        if (e.tags != null && !e.tags.isEmpty()) d.tags = e.tags.split(",");
        return d;
    }

    private static int iconFor(String type) {
        if (type == null) return android.R.drawable.ic_menu_info_details;
        switch (type.toLowerCase()) {
            case "video":                        return android.R.drawable.ic_media_play;
            case "pdf": case "slides":
            case "documentation":                return android.R.drawable.ic_menu_agenda;
            case "lab": case "challenge":        return android.R.drawable.ic_menu_compass;
            default:                             return android.R.drawable.ic_menu_info_details;
        }
    }
}
