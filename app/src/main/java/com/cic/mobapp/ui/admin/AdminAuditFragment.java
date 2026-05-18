package com.cic.mobapp.ui.admin;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cic.mobapp.databinding.FragmentAdminAuditBinding;
import com.cic.mobapp.databinding.ItemAuditEntryBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminAuditFragment extends Fragment {

    private FragmentAdminAuditBinding binding;
    private AdminViewModel viewModel;
    private AuditAdapter adapter;

    private List<AuditEntry> allEntries  = new ArrayList<>();
    private String           activeFilter = "ALL";
    private String           searchQuery  = "";

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
            allEntries = entries != null ? entries : new ArrayList<>();
            updateStatsBar(allEntries);
            applyFilter();
        });

        binding.btnClearLog.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Clear audit log?")
                        .setMessage("All entries in this session's log will be removed.")
                        .setPositiveButton("Clear", (d, w) -> viewModel.clearAuditLog())
                        .setNegativeButton("Cancel", null)
                        .show());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { searchQuery = q; applyFilter(); return true; }
            @Override public boolean onQueryTextChange(String q) { searchQuery = q; applyFilter(); return false; }
        });

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            if      (id == binding.chipInfo.getId())     activeFilter = AuditEntry.INFO;
            else if (id == binding.chipWarning.getId())  activeFilter = AuditEntry.WARNING;
            else if (id == binding.chipCritical.getId()) activeFilter = AuditEntry.CRITICAL;
            else if (id == binding.chipSecurity.getId()) activeFilter = AuditEntry.SECURITY;
            else                                          activeFilter = "ALL";
            applyFilter();
        });
    }

    private void applyFilter() {
        List<AuditEntry> result = new ArrayList<>(allEntries);

        if (!"ALL".equals(activeFilter)) {
            final String f = activeFilter;
            result = result.stream()
                    .filter(e -> f.equals(e.severity))
                    .collect(Collectors.toList());
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String q = searchQuery.toLowerCase();
            result = result.stream()
                    .filter(e -> (e.action != null && e.action.toLowerCase().contains(q))
                              || (e.entity != null && e.entity.toLowerCase().contains(q))
                              || (e.detail != null && e.detail.toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        adapter.setData(result);
        boolean empty = result.isEmpty();
        binding.rvAudit.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.emptyAudit.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.tvAuditCount.setText(result.size() + " / " + allEntries.size() + " log entries");
    }

    // ── Stats bar ─────────────────────────────────────────────────────────────

    private void updateStatsBar(List<AuditEntry> entries) {
        binding.statsRow.removeAllViews();
        long total    = entries.size();
        long info     = entries.stream().filter(e -> AuditEntry.INFO.equals(e.severity)).count();
        long warning  = entries.stream().filter(e -> AuditEntry.WARNING.equals(e.severity)).count();
        long critical = entries.stream().filter(e -> AuditEntry.CRITICAL.equals(e.severity)).count();
        long security = entries.stream().filter(e -> AuditEntry.SECURITY.equals(e.severity)).count();

        addStat("TOTAL",    String.valueOf(total),    "#00D1FF");
        addStat("INFO",     String.valueOf(info),     "#22C55E");
        addStat("WARNING",  String.valueOf(warning),  "#F59E0B");
        addStat("CRITICAL", String.valueOf(critical), "#EF4444");
        addStat("SECURITY", String.valueOf(security), "#8B5CF6");
    }

    private void addStat(String label, String value, String hex) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(16), dp(4), dp(16), dp(4));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(10));
        bg.setColor(Color.parseColor(hex + "22"));
        bg.setStroke(dp(1), Color.parseColor(hex + "55"));
        card.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(8));
        card.setLayoutParams(lp);
        TextView tvV = new TextView(requireContext());
        tvV.setText(value); tvV.setTextColor(Color.parseColor(hex));
        tvV.setTextSize(16); tvV.setTypeface(android.graphics.Typeface.MONOSPACE);
        tvV.setGravity(Gravity.CENTER);
        TextView tvL = new TextView(requireContext());
        tvL.setText(label); tvL.setTextColor(Color.parseColor(hex + "99"));
        tvL.setTextSize(8); tvL.setGravity(Gravity.CENTER); tvL.setLetterSpacing(0.1f);
        card.addView(tvV); card.addView(tvL);
        binding.statsRow.addView(card);
    }

    private int dp(int v) {
        return (int)(v * requireContext().getResources().getDisplayMetrics().density);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    // ── Adapter ───────────────────────────────────────────────────────────────

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

            String hex = severityColor(e.severity);

            // Severity bar
            h.b.dotAction.setBackgroundColor(Color.parseColor(hex));

            // Action badge
            h.b.tvAuditAction.setText(e.action);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(100f);
            bg.setColor(Color.parseColor(hex + "33"));
            bg.setStroke(1, Color.parseColor(hex + "AA"));
            h.b.tvAuditAction.setBackground(bg);
            h.b.tvAuditAction.setTextColor(Color.parseColor(hex));

            h.b.tvAuditEntity.setText(e.entity);
            h.b.tvAuditDetail.setText(e.detail);
            h.b.tvAuditTime.setText(e.date + " " + e.time);
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAuditEntryBinding b;
            VH(ItemAuditEntryBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    private static String severityColor(String s) {
        switch (s != null ? s : "") {
            case AuditEntry.CRITICAL: return "#EF4444";
            case AuditEntry.WARNING:  return "#F59E0B";
            case AuditEntry.SECURITY: return "#8B5CF6";
            default:                  return "#22C55E";
        }
    }
}
