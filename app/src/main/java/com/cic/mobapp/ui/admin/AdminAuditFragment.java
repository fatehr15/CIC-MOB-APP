package com.cic.mobapp.ui.admin;

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
import com.cic.mobapp.R;
import com.cic.mobapp.databinding.FragmentAdminAuditBinding;
import com.cic.mobapp.databinding.ItemAuditEntryBinding;
import java.util.ArrayList;
import java.util.List;

public class AdminAuditFragment extends Fragment {

    private FragmentAdminAuditBinding binding;
    private AdminViewModel viewModel;
    private AuditAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentAdminAuditBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new AuditAdapter();
        binding.rvAudit.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAudit.setAdapter(adapter);

        viewModel.getAuditLog().observe(getViewLifecycleOwner(), entries -> {
            adapter.setData(entries);
            boolean empty = entries == null || entries.isEmpty();
            binding.rvAudit.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.emptyAudit.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        binding.btnClearLog.setOnClickListener(v -> viewModel.clearAuditLog());
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    class AuditAdapter extends RecyclerView.Adapter<AuditAdapter.VH> {
        private List<AuditEntry> data = new ArrayList<>();

        void setData(List<AuditEntry> d) {
            data = d != null ? new ArrayList<>(d) : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemAuditEntryBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            AuditEntry e = data.get(pos);
            h.b.tvAuditAction.setText(e.action);
            h.b.tvAuditEntity.setText(e.entity);
            h.b.tvAuditDetail.setText(e.detail);
            h.b.tvAuditTime.setText(e.time);

            // Color the dot and action label by type
            int color;
            switch (e.action) {
                case AuditEntry.CREATE: color = requireContext().getColor(R.color.matrix_green);  break;
                case AuditEntry.DELETE: color = requireContext().getColor(R.color.status_error);  break;
                case AuditEntry.UPDATE: color = requireContext().getColor(R.color.accent_cyan);   break;
                default:                color = requireContext().getColor(R.color.accent_purple); break;
            }
            h.b.tvAuditAction.setTextColor(color);
            h.b.dotAction.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(color));
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAuditEntryBinding b;
            VH(ItemAuditEntryBinding b) { super(b.getRoot()); this.b = b; }
        }
    }
}
