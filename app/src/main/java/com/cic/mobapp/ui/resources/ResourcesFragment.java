package com.cic.mobapp.ui.resources;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import com.cic.mobapp.data.local.entity.ResourceEntity;
import com.cic.mobapp.data.remote.dto.ResourceDto;
import com.cic.mobapp.databinding.FragmentResourcesBinding;
import com.cic.mobapp.databinding.ItemResourceBinding;
import android.os.Handler;
import android.os.Looper;
import com.cic.mobapp.util.NetworkUtils;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class ResourcesFragment extends Fragment {

    private FragmentResourcesBinding binding;
    private ResourcesViewModel viewModel;
    private ResourcesAdapter adapter;
    private ObjectAnimator skeletonAnim;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentResourcesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ResourcesViewModel.class);

        adapter = new ResourcesAdapter();
        binding.rvResources.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvResources.setAdapter(adapter);

        binding.offlineBanner.setVisibility(
                NetworkUtils.isConnected(requireContext()) ? View.GONE : View.VISIBLE);
        binding.btnRetry.setOnClickListener(v -> viewModel.filterByCategory(null));

        // Difficulty / type chips
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == binding.chipAll.getId())          viewModel.filterByCategory(null);
            else if (id == binding.chipBeginner.getId())     viewModel.filterByDifficulty("Beginner");
            else if (id == binding.chipIntermediate.getId()) viewModel.filterByDifficulty("Intermediate");
            else if (id == binding.chipAdvanced.getId())     viewModel.filterByDifficulty("Advanced");
            else if (id == binding.chipPDF.getId())          viewModel.filterByType("PDF");
            else if (id == binding.chipVideo.getId())        viewModel.filterByType("Video");
        });

        // Debounced search — waits 350ms after last keystroke before querying
        Handler debounce = new Handler(Looper.getMainLooper());
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { viewModel.search(q); return true; }
            @Override public boolean onQueryTextChange(String q) {
                debounce.removeCallbacksAndMessages(null);
                if (q.isEmpty()) { viewModel.filterByCategory(null); return false; }
                debounce.postDelayed(() -> viewModel.search(q), 350);
                return false;
            }
        });

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

        viewModel.getResources().observe(getViewLifecycleOwner(), resources -> {
            if (resources == null) return;
            adapter.setData(resources);
            showState(resources.isEmpty() ? State.EMPTY : State.CONTENT);
        });
    }

    private enum State { LOADING, CONTENT, EMPTY, ERROR }

    private void showState(State state) {
        binding.skeletonContainer.setVisibility(state == State.LOADING  ? View.VISIBLE : View.GONE);
        binding.rvResources.setVisibility(       state == State.CONTENT  ? View.VISIBLE : View.GONE);
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

    private static ResourceDto toDto(ResourceEntity e) {
        ResourceDto d = new ResourceDto();
        d.id          = e.id;
        d.title       = e.title;
        d.category    = e.category;
        d.type        = e.type;
        d.difficulty  = e.difficulty;
        d.fileUrl     = e.fileUrl;
        d.description = e.description;
        d.uploadedBy  = e.uploadedBy;
        if (e.tags != null && !e.tags.isEmpty()) {
            d.tags = e.tags.split(",");
        }
        return d;
    }

    private static int iconFor(String type) {
        if (type == null) return android.R.drawable.ic_menu_info_details;
        switch (type.toLowerCase()) {
            case "video":   return android.R.drawable.ic_media_play;
            case "pdf":
            case "slides":
            case "documentation": return android.R.drawable.ic_menu_agenda;
            case "lab":
            case "challenge": return android.R.drawable.ic_menu_compass;
            default: return android.R.drawable.ic_menu_info_details;
        }
    }
}
