package com.cic.mobapp.ui.events;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
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
import com.bumptech.glide.Glide;
import com.cic.mobapp.R;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.databinding.FragmentEventsBinding;
import com.cic.mobapp.databinding.ItemEventBinding;
import com.cic.mobapp.util.NetworkUtils;
import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    private FragmentEventsBinding binding;
    private EventsViewModel viewModel;
    private EventsAdapter adapter;
    private ObjectAnimator skeletonAnim;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EventsViewModel.class);

        adapter = new EventsAdapter();
        binding.rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvEvents.setAdapter(adapter);

        binding.offlineBanner.setVisibility(
                NetworkUtils.isConnected(requireContext()) ? View.GONE : View.VISIBLE);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            binding.swipeRefresh.setRefreshing(false);
            viewModel.refresh();
        });

        binding.btnRetry.setOnClickListener(v -> viewModel.refresh());

        observeViewModel();
        showState(State.LOADING);
        startSkeletonAnim();
    }

    private void observeViewModel() {
        // Loading → show skeleton
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) {
                showState(State.LOADING);
                startSkeletonAnim();
            }
        });

        // Error → show error panel (only when not loading and no data yet)
        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !Boolean.TRUE.equals(viewModel.isLoading().getValue())) {
                binding.tvErrorMsg.setText(err);
                showState(State.ERROR);
            }
        });

        // Data → ALWAYS transition to content/empty once data arrives
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (events == null) return;
            adapter.setData(events);
            showState(events.isEmpty() ? State.EMPTY : State.CONTENT);
        });
    }

    private enum State { LOADING, CONTENT, EMPTY, ERROR }

    private void showState(State state) {
        binding.skeletonContainer.setVisibility(state == State.LOADING  ? View.VISIBLE : View.GONE);
        binding.swipeRefresh.setVisibility(      state == State.CONTENT  ? View.VISIBLE : View.GONE);
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

    class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {
        private List<EventEntity> data = new ArrayList<>();

        void setData(List<EventEntity> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemEventBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            EventEntity e = data.get(pos);
            h.b.tvEventTitle.setText(e.title);
            h.b.tvEventDate.setText(fmt(e.date) + "  ·  " + e.location);
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

    private static String fmt(String iso) {
        return (iso != null && iso.length() >= 10) ? iso.substring(0, 10) : "";
    }
}
