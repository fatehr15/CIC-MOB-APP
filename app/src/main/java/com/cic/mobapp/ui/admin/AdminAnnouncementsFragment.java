package com.cic.mobapp.ui.admin;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cic.mobapp.R;
import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import com.cic.mobapp.databinding.FragmentAdminAnnouncementsBinding;
import com.cic.mobapp.databinding.ItemAdminAnnouncementBinding;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminAnnouncementsFragment extends Fragment {

    private enum SortMode {
        LATEST("Sort: Latest"),
        OLDEST("Sort: Oldest"),
        PRIORITY("Sort: Priority");
        final String label;
        SortMode(String l) { this.label = l; }
    }

    private FragmentAdminAnnouncementsBinding binding;
    private AdminViewModel viewModel;
    private AnnAdapter adapter;

    private List<AnnouncementDto> allAnn   = new ArrayList<>();
    private String                activeFilter = "ALL";
    private SortMode              sortMode     = SortMode.LATEST;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentAdminAnnouncementsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new AnnAdapter();
        binding.rvAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAnnouncements.setAdapter(adapter);

        viewModel.getAnnouncements().observe(getViewLifecycleOwner(), anns -> {
            allAnn = anns != null ? anns : new ArrayList<>();
            updateStatsBar(allAnn);
            applyFilterAndSort();
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.fabCreate.setOnClickListener(v -> showDialog(null));
        binding.btnSort.setOnClickListener(v -> showSortPicker());

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            if      (id == binding.chipPinned.getId())    activeFilter = "PINNED";
            else if (id == binding.chipEmergency.getId()) activeFilter = "Emergency";
            else if (id == binding.chipGeneral.getId())   activeFilter = "General";
            else if (id == binding.chipTechnical.getId()) activeFilter = "Technical";
            else if (id == binding.chipWorkshop.getId())  activeFilter = "Workshop";
            else if (id == binding.chipCTF.getId())       activeFilter = "CTF";
            else                                           activeFilter = "ALL";
            applyFilterAndSort();
        });
    }

    // ── Filter + Sort ─────────────────────────────────────────────────────────

    private void applyFilterAndSort() {
        List<AnnouncementDto> result = new ArrayList<>(allAnn);

        if ("PINNED".equals(activeFilter)) {
            result = result.stream().filter(a -> a.isPinned).collect(Collectors.toList());
        } else if (!"ALL".equals(activeFilter)) {
            final String f = activeFilter;
            result = result.stream()
                    .filter(a -> f.equalsIgnoreCase(a.type))
                    .collect(Collectors.toList());
        }

        Comparator<AnnouncementDto> cmp;
        switch (sortMode) {
            case OLDEST:   cmp = (a, b) -> compareStr(a.createdAt, b.createdAt); break;
            case PRIORITY: cmp = (a, b) -> priorityRank(a.priority) - priorityRank(b.priority); break;
            default:       cmp = (a, b) -> compareStr(b.createdAt, a.createdAt); break;
        }
        // Pinned always first
        result.sort((a, b) -> {
            if (a.isPinned && !b.isPinned) return -1;
            if (!a.isPinned && b.isPinned) return 1;
            return cmp.compare(a, b);
        });

        adapter.setData(result);
        binding.tvAnnCount.setText(result.size() + " / " + allAnn.size() + " announcements  |  " + sortMode.label);
    }

    private static int priorityRank(String p) {
        if (p == null) return 3;
        switch (p) {
            case "Emergency": return 0;
            case "Critical":  return 1;
            case "Important": return 2;
            case "Normal":    return 3;
            default:          return 4;
        }
    }

    private static int compareStr(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }

    private void showSortPicker() {
        SortMode[] modes = SortMode.values();
        String[] labels  = new String[modes.length];
        for (int i = 0; i < modes.length; i++) labels[i] = modes[i].label;
        new AlertDialog.Builder(requireContext())
                .setTitle("Sort by")
                .setItems(labels, (d, w) -> {
                    sortMode = modes[w];
                    binding.btnSort.setText(sortMode.label);
                    applyFilterAndSort();
                }).show();
    }

    // ── Stats bar ─────────────────────────────────────────────────────────────

    private void updateStatsBar(List<AnnouncementDto> anns) {
        binding.statsRow.removeAllViews();
        long total     = anns.size();
        long pinned    = anns.stream().filter(a -> a.isPinned).count();
        long emergency = anns.stream().filter(a -> "Emergency".equalsIgnoreCase(a.type)).count();
        long critical  = anns.stream()
                .filter(a -> "Critical".equalsIgnoreCase(a.priority) || "Emergency".equalsIgnoreCase(a.priority))
                .count();

        addStat("TOTAL",     String.valueOf(total),     "#8B5CF6");
        addStat("PINNED",    String.valueOf(pinned),    "#F59E0B");
        addStat("EMERGENCY", String.valueOf(emergency), "#EF4444");
        addStat("CRITICAL",  String.valueOf(critical),  "#F97316");
    }

    private void addStat(String label, String value, String hex) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(18), dp(4), dp(18), dp(4));
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
        tvV.setTextSize(18); tvV.setTypeface(android.graphics.Typeface.MONOSPACE);
        tvV.setGravity(Gravity.CENTER);
        TextView tvL = new TextView(requireContext());
        tvL.setText(label); tvL.setTextColor(Color.parseColor(hex + "99"));
        tvL.setTextSize(9); tvL.setGravity(Gravity.CENTER); tvL.setLetterSpacing(0.1f);
        card.addView(tvV); card.addView(tvL);
        binding.statsRow.addView(card);
    }

    private int dp(int v) {
        return (int)(v * requireContext().getResources().getDisplayMetrics().density);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    // ── Adapter ───────────────────────────────────────────────────────────────

    class AnnAdapter extends RecyclerView.Adapter<AnnAdapter.VH> {
        private List<AnnouncementDto> data = new ArrayList<>();
        void setData(List<AnnouncementDto> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemAdminAnnouncementBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            AnnouncementDto a = data.get(pos);
            bindAnn(h.b, a);
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAdminAnnouncementBinding b;
            VH(ItemAdminAnnouncementBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    private void bindAnn(ItemAdminAnnouncementBinding b, AnnouncementDto a) {
        b.tvAnnTitle.setText(a.title);
        b.tvAnnBody.setText(a.body);

        // Type badge
        String type = a.type != null ? a.type : "General";
        String typeHex = typeColor(type);
        b.tvAnnType.setText(type);
        styleBadge(b.tvAnnType, typeHex);

        // Priority bar + badge
        String prio = a.priority != null ? a.priority : "Normal";
        String prioHex = priorityColor(prio);
        b.viewPriorityBar.setBackgroundColor(Color.parseColor(prioHex));
        if (!"Normal".equals(prio) && !"Low".equals(prio)) {
            b.tvAnnPriority.setText(prio);
            styleBadge(b.tvAnnPriority, prioHex);
            b.tvAnnPriority.setVisibility(View.VISIBLE);
        } else {
            b.tvAnnPriority.setVisibility(View.GONE);
        }

        // Pinned
        b.tvAnnPinned.setVisibility(a.isPinned ? View.VISIBLE : View.GONE);
        b.btnTogglePin.setText(a.isPinned ? "Unpin" : "Pin");

        // Meta
        b.tvAnnMeta.setText(a.createdAt != null && a.createdAt.length() >= 10
                ? a.createdAt.substring(0, 10) : "");

        // Buttons
        b.btnEdit.setOnClickListener(v -> showDialog(a));
        b.btnTogglePin.setOnClickListener(v ->
                viewModel.updateAnnouncement(a.id, a.title, a.body, a.type, !a.isPinned));
        b.btnDelete.setOnClickListener(v -> confirmDelete(a));
        b.btnMore.setOnClickListener(v -> showQuickActions(a, v));
        b.getRoot().setOnClickListener(v -> showDetail(a));
    }

    // ── Quick actions ─────────────────────────────────────────────────────────

    private void showQuickActions(AnnouncementDto a, View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add(0, 1, 0, "View Details");
        menu.getMenu().add(0, 2, 0, "Edit");
        menu.getMenu().add(0, 3, 0, a.isPinned ? "Unpin" : "Pin");
        menu.getMenu().add(0, 4, 0, "Delete");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: showDetail(a);  break;
                case 2: showDialog(a);  break;
                case 3: viewModel.updateAnnouncement(a.id, a.title, a.body, a.type, !a.isPinned); break;
                case 4: confirmDelete(a); break;
            }
            return true;
        });
        menu.show();
    }

    private void showDetail(AnnouncementDto a) {
        String type    = a.type != null ? a.type : "General";
        String prio    = a.priority != null ? a.priority : "Normal";
        String message = type + " — " + prio + "\n\n" + a.body;
        new AlertDialog.Builder(requireContext())
                .setTitle(a.title)
                .setMessage(message)
                .setPositiveButton("Edit", (d, w) -> showDialog(a))
                .setNegativeButton("Close", null)
                .show();
    }

    // ── Create / Edit dialog ──────────────────────────────────────────────────

    private void showDialog(@Nullable AnnouncementDto existing) {
        View dv = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_create_announcement, null);

        android.widget.EditText etTitle = dv.findViewById(R.id.etAnnTitle);
        android.widget.EditText etBody  = dv.findViewById(R.id.etAnnBody);
        CheckBox cbPinned = dv.findViewById(R.id.cbPinned);

        // Resolve type from chip group
        com.google.android.material.chip.ChipGroup cgType =
                dv.findViewById(R.id.chipGroupType);
        com.google.android.material.chip.ChipGroup cgPrio =
                dv.findViewById(R.id.chipGroupPriority);

        if (existing != null) {
            etTitle.setText(existing.title);
            etBody.setText(existing.body);
            cbPinned.setChecked(existing.isPinned);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "New Announcement" : "Edit Announcement")
                .setView(dv)
                .setPositiveButton(existing == null ? "Publish" : "Save", (d, w) -> {
                    String title  = etTitle.getText().toString().trim();
                    String body   = etBody.getText().toString().trim();
                    boolean pinned = cbPinned.isChecked();
                    if (title.isEmpty() || body.isEmpty()) return;

                    // Read type from chip group
                    String type = chipLabel(cgType, dv, "General");
                    String prio = chipLabel(cgPrio, dv, "Normal");

                    if (existing == null) {
                        viewModel.createAnnouncement(title, body, type, pinned);
                    } else {
                        viewModel.updateAnnouncement(existing.id, title, body, type, pinned);
                    }
                    viewModel.auditAction(
                            existing == null ? AuditEntry.CREATE : AuditEntry.UPDATE,
                            "Announcement", title + " [" + prio + "]");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String chipLabel(com.google.android.material.chip.ChipGroup group,
                             View root, String fallback) {
        java.util.List<Integer> checked = group.getCheckedChipIds();
        if (checked.isEmpty()) return fallback;
        com.google.android.material.chip.Chip chip = root.findViewById(checked.get(0));
        return chip != null ? chip.getText().toString() : fallback;
    }

    private void confirmDelete(AnnouncementDto a) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete announcement?")
                .setMessage("\"" + a.title + "\" will be permanently removed.")
                .setPositiveButton("Delete", (d, w) -> {
                    viewModel.deleteAnnouncement(a.id);
                    viewModel.auditAction(AuditEntry.DELETE, "Announcement", a.title);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Styling helpers ───────────────────────────────────────────────────────

    private static String typeColor(String type) {
        switch (type != null ? type : "") {
            case "Emergency":        return "#EF4444";
            case "CTF":              return "#EF4444";
            case "Workshop":         return "#F59E0B";
            case "Technical":        return "#3D8EFF";
            case "Meeting":          return "#22C55E";
            case "Resource Release": return "#8B5CF6";
            case "Mentorship":       return "#14B8A6";
            case "Recruitment":      return "#EC4899";
            case "Maintenance":      return "#8B9AB0";
            default:                 return "#00D1FF";
        }
    }

    private static String priorityColor(String p) {
        switch (p != null ? p : "") {
            case "Emergency": return "#EF4444";
            case "Critical":  return "#F97316";
            case "Important": return "#F59E0B";
            case "Low":       return "#8B9AB0";
            default:          return "#00D1FF";
        }
    }

    private static void styleBadge(TextView tv, String hex) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(100f);
        bg.setColor(Color.parseColor(hex + "33"));
        bg.setStroke(1, Color.parseColor(hex + "AA"));
        tv.setBackground(bg);
        tv.setTextColor(Color.parseColor(hex));
    }
}
