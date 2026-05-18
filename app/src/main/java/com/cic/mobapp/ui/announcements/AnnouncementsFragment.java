package com.cic.mobapp.ui.announcements;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import com.cic.mobapp.databinding.FragmentAnnouncementsBinding;
import com.cic.mobapp.databinding.ItemAnnouncementBinding;
import com.cic.mobapp.util.NetworkUtils;
import com.cic.mobapp.util.ReadTracker;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementsFragment extends Fragment {

    private FragmentAnnouncementsBinding binding;
    private AnnouncementsViewModel viewModel;
    private AnnouncementsAdapter adapter;
    private ObjectAnimator skeletonAnim;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AnnouncementsViewModel.class);

        adapter = new AnnouncementsAdapter();
        binding.rvAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAnnouncements.setAdapter(adapter);

        binding.offlineBanner.setVisibility(
                NetworkUtils.isConnected(requireContext()) ? View.GONE : View.VISIBLE);
        binding.btnRetry.setOnClickListener(v -> viewModel.refresh());

        observeViewModel();
        showState(State.LOADING);
        startSkeletonAnim();
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) {
                showState(State.LOADING);
                startSkeletonAnim();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !Boolean.TRUE.equals(viewModel.isLoading().getValue())) {
                binding.tvErrorMsg.setText(err);
                showState(State.ERROR);
            }
        });

        viewModel.getAnnouncements().observe(getViewLifecycleOwner(), items -> {
            if (items == null) return;
            adapter.setData(items);
            showState(items.isEmpty() ? State.EMPTY : State.CONTENT);
        });
    }

    private enum State { LOADING, CONTENT, EMPTY, ERROR }

    private void showState(State state) {
        binding.skeletonContainer.setVisibility(state == State.LOADING  ? View.VISIBLE : View.GONE);
        binding.rvAnnouncements.setVisibility(   state == State.CONTENT  ? View.VISIBLE : View.GONE);
        binding.emptyContainer.setVisibility(    state == State.EMPTY    ? View.VISIBLE : View.GONE);
        binding.errorContainer.setVisibility(    state == State.ERROR    ? View.VISIBLE : View.GONE);
        if (state != State.LOADING) stopSkeletonAnim();
    }

    private void startSkeletonAnim() {
        if (skeletonAnim != null && skeletonAnim.isRunning()) return;
        skeletonAnim = ObjectAnimator.ofFloat(binding.skeletonContainer, "alpha", 0.35f, 1f);
        skeletonAnim.setDuration(900);
        skeletonAnim.setRepeatCount(ValueAnimator.INFINITE);
        skeletonAnim.setRepeatMode(ValueAnimator.REVERSE);
        skeletonAnim.setInterpolator(new LinearInterpolator());
        skeletonAnim.start();
    }

    private void stopSkeletonAnim() {
        if (skeletonAnim != null) { skeletonAnim.cancel(); skeletonAnim = null; }
    }

    @Override public void onDestroyView() {
        stopSkeletonAnim();
        super.onDestroyView();
        binding = null;
    }

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

            // Unread dot
            boolean unread = a.id != null && !tracker.isRead(a.id);
            h.b.dotUnread.setVisibility(unread ? View.VISIBLE : View.GONE);

            h.b.getRoot().setOnClickListener(v -> {
                if (a.id != null) tracker.markRead(a.id);
                h.b.dotUnread.setVisibility(View.GONE);
                Intent i = new Intent(requireContext(), AnnouncementDetailActivity.class);
                i.putExtra(AnnouncementDetailActivity.EXTRA_ANNOUNCEMENT_JSON, new Gson().toJson(a));
                startActivity(i);
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAnnouncementBinding b;
            VH(ItemAnnouncementBinding b) { super(b.getRoot()); this.b = b; }
        }
    }
}
