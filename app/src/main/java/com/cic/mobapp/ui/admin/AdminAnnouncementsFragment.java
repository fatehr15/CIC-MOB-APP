package com.cic.mobapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
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
import java.util.List;

public class AdminAnnouncementsFragment extends Fragment {

    private FragmentAdminAnnouncementsBinding binding;
    private AdminViewModel viewModel;
    private AnnouncementsAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentAdminAnnouncementsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new AnnouncementsAdapter();
        binding.rvAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAnnouncements.setAdapter(adapter);

        viewModel.getAnnouncements().observe(getViewLifecycleOwner(),
                announcements -> adapter.setData(announcements));

        binding.fabCreate.setOnClickListener(v -> showAnnouncementDialog(null));

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    // ── Adapter ───────────────────────────────────────────────────────────────

    class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.VH> {
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
            h.b.tvAnnTitle.setText(a.title);
            h.b.tvAnnBody.setText(a.body);
            h.b.tvAnnType.setText(a.type != null ? a.type : "General");
            h.b.tvAnnMeta.setText(a.priority + (a.isPinned ? " · PINNED" : ""));
            h.b.tvAnnPinned.setVisibility(a.isPinned ? View.VISIBLE : View.GONE);
            h.b.btnEdit.setOnClickListener(v -> showAnnouncementDialog(a));
            h.b.btnDelete.setOnClickListener(v -> confirmDelete(a));
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAdminAnnouncementBinding b;
            VH(ItemAdminAnnouncementBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    // ── Create / Edit dialog ─────────────────────────────────────────────────

    private void showAnnouncementDialog(@Nullable AnnouncementDto existing) {
        View dv = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_create_announcement, null);
        EditText etTitle  = dv.findViewById(R.id.etAnnTitle);
        EditText etBody   = dv.findViewById(R.id.etAnnBody);
        EditText etType   = dv.findViewById(R.id.etAnnType);
        CheckBox cbPinned = dv.findViewById(R.id.cbPinned);

        if (existing != null) {
            etTitle.setText(existing.title);
            etBody.setText(existing.body);
            etType.setText(existing.type);
            cbPinned.setChecked(existing.isPinned);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "New Announcement" : "Edit Announcement")
                .setView(dv)
                .setPositiveButton(existing == null ? "Post" : "Save", (d, w) -> {
                    String title  = etTitle.getText().toString().trim();
                    String body   = etBody.getText().toString().trim();
                    String type   = etType.getText().toString().trim();
                    boolean pinned = cbPinned.isChecked();
                    if (title.isEmpty() || body.isEmpty()) return;

                    if (existing == null) {
                        viewModel.createAnnouncement(title, body,
                                type.isEmpty() ? "General" : type, pinned);
                    } else {
                        viewModel.updateAnnouncement(existing.id, title, body,
                                type.isEmpty() ? "General" : type, pinned);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(AnnouncementDto ann) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete announcement?")
                .setMessage("\"" + ann.title + "\" will be permanently deleted.")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteAnnouncement(ann.id))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
