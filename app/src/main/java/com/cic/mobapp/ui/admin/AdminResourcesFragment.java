package com.cic.mobapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cic.mobapp.R;
import com.cic.mobapp.data.remote.dto.ResourceDto;
import com.cic.mobapp.databinding.FragmentAdminResourcesBinding;
import com.cic.mobapp.databinding.ItemAdminResourceBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminResourcesFragment extends Fragment {

    private FragmentAdminResourcesBinding binding;
    private AdminViewModel viewModel;
    private ResourcesAdapter adapter;
    private List<ResourceDto> allResources = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentAdminResourcesBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new ResourcesAdapter();
        binding.rvResources.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvResources.setAdapter(adapter);

        viewModel.getResources().observe(getViewLifecycleOwner(), resources -> {
            allResources = resources != null ? resources : new ArrayList<>();
            adapter.setData(allResources);
            binding.tvResourceCount.setText(allResources.size() + " resources");
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.fabCreate.setOnClickListener(v -> showResourceDialog(null));

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { filter(q); return true; }
            @Override public boolean onQueryTextChange(String q) { filter(q); return false; }
        });
    }

    private void filter(String query) {
        if (query == null || query.isEmpty()) {
            adapter.setData(allResources);
            binding.tvResourceCount.setText(allResources.size() + " resources");
            return;
        }
        String q = query.toLowerCase();
        List<ResourceDto> filtered = allResources.stream()
                .filter(r -> (r.title    != null && r.title.toLowerCase().contains(q))
                          || (r.category != null && r.category.toLowerCase().contains(q))
                          || (r.type     != null && r.type.toLowerCase().contains(q))
                          || (r.difficulty != null && r.difficulty.toLowerCase().contains(q)))
                .collect(Collectors.toList());
        adapter.setData(filtered);
        binding.tvResourceCount.setText(filtered.size() + " / " + allResources.size());
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    // ── Adapter ───────────────────────────────────────────────────────────────

    class ResourcesAdapter extends RecyclerView.Adapter<ResourcesAdapter.VH> {
        private List<ResourceDto> data = new ArrayList<>();

        void setData(List<ResourceDto> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemAdminResourceBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            ResourceDto r = data.get(pos);
            h.b.tvResourceTitle.setText(r.title);
            h.b.tvResourceType.setText(r.type);
            h.b.tvResourceMeta.setText(r.category + " · " + r.difficulty);
            h.b.tvResourceDesc.setText(r.description);
            h.b.btnEdit.setOnClickListener(v -> showResourceDialog(r));
            h.b.btnDelete.setOnClickListener(v -> confirmDelete(r));
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAdminResourceBinding b;
            VH(ItemAdminResourceBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void showResourceDialog(@Nullable ResourceDto existing) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_resource, null);
        EditText etTitle       = dv.findViewById(R.id.etResTitle);
        EditText etCategory    = dv.findViewById(R.id.etResCategory);
        EditText etType        = dv.findViewById(R.id.etResType);
        EditText etDifficulty  = dv.findViewById(R.id.etResDifficulty);
        EditText etUrl         = dv.findViewById(R.id.etResUrl);
        EditText etDescription = dv.findViewById(R.id.etResDescription);

        if (existing != null) {
            etTitle.setText(existing.title);
            etCategory.setText(existing.category);
            etType.setText(existing.type);
            etDifficulty.setText(existing.difficulty);
            etUrl.setText(existing.fileUrl);
            etDescription.setText(existing.description);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Publish Resource" : "Edit Resource")
                .setView(dv)
                .setPositiveButton(existing == null ? "Publish" : "Save", (d, w) -> {
                    String title    = etTitle.getText().toString().trim();
                    String category = etCategory.getText().toString().trim();
                    String type     = etType.getText().toString().trim();
                    String diff     = etDifficulty.getText().toString().trim();
                    String url      = etUrl.getText().toString().trim();
                    String desc     = etDescription.getText().toString().trim();
                    if (title.isEmpty()) return;
                    if (existing == null) {
                        viewModel.createResource(title,
                                category.isEmpty() ? "General" : category,
                                type.isEmpty() ? "PDF" : type,
                                diff.isEmpty() ? "Beginner" : diff, url, desc);
                    } else {
                        viewModel.updateResource(existing.id, title,
                                category.isEmpty() ? "General" : category,
                                type.isEmpty() ? "PDF" : type,
                                diff.isEmpty() ? "Beginner" : diff, url, desc);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(ResourceDto resource) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete resource?")
                .setMessage("\"" + resource.title + "\" will be permanently deleted.")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteResource(resource.id))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
