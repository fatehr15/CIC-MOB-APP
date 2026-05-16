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
import com.bumptech.glide.Glide;
import com.cic.mobapp.R;
import com.cic.mobapp.data.remote.dto.EventDto;
import com.cic.mobapp.databinding.FragmentAdminEventsBinding;
import com.cic.mobapp.databinding.ItemAdminEventBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminEventsFragment extends Fragment {

    private FragmentAdminEventsBinding binding;
    private AdminViewModel viewModel;
    private EventsAdapter adapter;
    private List<EventDto> allEvents = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentAdminEventsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new EventsAdapter();
        binding.rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvEvents.setAdapter(adapter);

        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            allEvents = events != null ? events : new ArrayList<>();
            adapter.setData(allEvents);
            binding.tvEventCount.setText(allEvents.size() + " events");
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.fabCreate.setOnClickListener(v -> showEventDialog(null));

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { filter(q); return true; }
            @Override public boolean onQueryTextChange(String q) { filter(q); return false; }
        });
    }

    private void filter(String query) {
        if (query == null || query.isEmpty()) {
            adapter.setData(allEvents);
            binding.tvEventCount.setText(allEvents.size() + " events");
            return;
        }
        String q = query.toLowerCase();
        List<EventDto> filtered = allEvents.stream()
                .filter(e -> (e.title != null && e.title.toLowerCase().contains(q))
                          || (e.type  != null && e.type.toLowerCase().contains(q))
                          || (e.difficulty != null && e.difficulty.toLowerCase().contains(q))
                          || (e.location   != null && e.location.toLowerCase().contains(q)))
                .collect(Collectors.toList());
        adapter.setData(filtered);
        binding.tvEventCount.setText(filtered.size() + " / " + allEvents.size());
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    // ── Adapter ───────────────────────────────────────────────────────────────

    class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {
        private List<EventDto> data = new ArrayList<>();

        void setData(List<EventDto> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemAdminEventBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            EventDto e = data.get(pos);
            h.b.tvEventTitle.setText(e.title);
            h.b.tvEventMeta.setText(e.type + " · " + e.difficulty + " · " + fmt(e.date));
            h.b.tvSeats.setText((e.capacity - e.registeredCount) + " seats left");
            Glide.with(requireContext()).load(e.bannerUrl).centerCrop()
                    .placeholder(R.drawable.placeholder_banner).into(h.b.imgBanner);
            h.b.btnEdit.setOnClickListener(v -> showEventDialog(e));
            h.b.btnDelete.setOnClickListener(v -> confirmDelete(e));
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAdminEventBinding b;
            VH(ItemAdminEventBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void showEventDialog(@Nullable EventDto existing) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_event, null);
        EditText etTitle       = dv.findViewById(R.id.etEventTitle);
        EditText etDescription = dv.findViewById(R.id.etEventDescription);
        EditText etLocation    = dv.findViewById(R.id.etEventLocation);
        EditText etDate        = dv.findViewById(R.id.etEventDate);
        EditText etType        = dv.findViewById(R.id.etEventType);
        EditText etDifficulty  = dv.findViewById(R.id.etEventDifficulty);
        EditText etCapacity    = dv.findViewById(R.id.etEventCapacity);

        if (existing != null) {
            etTitle.setText(existing.title);
            etDescription.setText(existing.description);
            etLocation.setText(existing.location);
            etDate.setText(existing.date);
            etType.setText(existing.type);
            etDifficulty.setText(existing.difficulty);
            etCapacity.setText(String.valueOf(existing.capacity));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Create Event" : "Edit Event")
                .setView(dv)
                .setPositiveButton(existing == null ? "Create" : "Save", (d, w) -> {
                    String title    = etTitle.getText().toString().trim();
                    String desc     = etDescription.getText().toString().trim();
                    String location = etLocation.getText().toString().trim();
                    String date     = etDate.getText().toString().trim();
                    String type     = etType.getText().toString().trim();
                    String diff     = etDifficulty.getText().toString().trim();
                    String capStr   = etCapacity.getText().toString().trim();
                    int capacity    = capStr.isEmpty() ? 30 : Integer.parseInt(capStr);
                    if (title.isEmpty()) return;
                    if (existing == null) {
                        viewModel.createEvent(title, desc, location, date,
                                type.isEmpty() ? "Workshop" : type,
                                diff.isEmpty() ? "Beginner" : diff, capacity);
                    } else {
                        viewModel.updateEvent(existing.id, title, desc, location, date,
                                type.isEmpty() ? "Workshop" : type,
                                diff.isEmpty() ? "Beginner" : diff, capacity);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(EventDto event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete event?")
                .setMessage("\"" + event.title + "\" will be permanently deleted.")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteEvent(event.id))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static String fmt(String iso) {
        return (iso != null && iso.length() >= 10) ? iso.substring(0, 10) : "";
    }
}
