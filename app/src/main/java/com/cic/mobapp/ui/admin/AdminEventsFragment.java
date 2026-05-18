package com.cic.mobapp.ui.admin;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class AdminEventsFragment extends Fragment {

    // ── Sort options ──────────────────────────────────────────────────────────
    private enum SortMode {
        DATE_ASC("Sort: Date"),
        DATE_DESC("Sort: Latest"),
        TITLE_AZ("Sort: A-Z"),
        CAPACITY("Sort: Capacity"),
        FILL_RATE("Sort: Fill Rate");
        final String label;
        SortMode(String l) { this.label = l; }
    }

    private FragmentAdminEventsBinding binding;
    private AdminViewModel viewModel;
    private EventsAdapter adapter;

    private List<EventDto> allEvents   = new ArrayList<>();
    private String         activeFilter = "ALL";
    private String         searchQuery  = "";
    private SortMode       sortMode     = SortMode.DATE_ASC;

    private Uri     pendingBannerUri = null;
    private ImageView activePreview   = null;

    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || activePreview == null) return;
                pendingBannerUri = uri;
                Glide.with(this).load(uri).centerCrop().into(activePreview);
            });

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
            updateStatsBar(allEvents);
            applyFilterAndSort();
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.fabCreate.setOnClickListener(v -> showEventDialog(null));

        // Search
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { searchQuery = q; applyFilterAndSort(); return true; }
            @Override public boolean onQueryTextChange(String q) { searchQuery = q; applyFilterAndSort(); return false; }
        });

        // Filter chips
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == binding.chipLive.getId())      activeFilter = "Live";
            else if (id == binding.chipSoon.getId())      activeFilter = "Starting Soon";
            else if (id == binding.chipOpen.getId())      activeFilter = "Registration Open";
            else if (id == binding.chipFull.getId())      activeFilter = "Full";
            else if (id == binding.chipCompleted.getId()) activeFilter = "Completed";
            else if (id == binding.chipWorkshop.getId())  activeFilter = "TYPE:Workshop";
            else if (id == binding.chipCTF.getId())       activeFilter = "TYPE:CTF";
            else if (id == binding.chipEvents.getId())    activeFilter = "TYPE:Event";
            else                                           activeFilter = "ALL";
            applyFilterAndSort();
        });

        binding.btnSort.setOnClickListener(v -> showSortPicker());
    }

    // ── Filter + Sort ─────────────────────────────────────────────────────────

    private void applyFilterAndSort() {
        List<EventDto> result = new ArrayList<>(allEvents);

        if (!activeFilter.equals("ALL")) {
            if (activeFilter.startsWith("TYPE:")) {
                String typeFilter = activeFilter.substring(5);
                result = result.stream()
                        .filter(e -> typeFilter.equalsIgnoreCase(e.type))
                        .collect(Collectors.toList());
            } else {
                result = result.stream()
                        .filter(e -> activeFilter.equals(deriveStatus(e)))
                        .collect(Collectors.toList());
            }
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String q = searchQuery.toLowerCase();
            result = result.stream()
                    .filter(e -> (e.title    != null && e.title.toLowerCase().contains(q))
                              || (e.type     != null && e.type.toLowerCase().contains(q))
                              || (e.location != null && e.location.toLowerCase().contains(q))
                              || (e.difficulty != null && e.difficulty.toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        Comparator<EventDto> cmp;
        switch (sortMode) {
            case DATE_DESC: cmp = (a, b) -> compareStr(b.date, a.date);    break;
            case TITLE_AZ:  cmp = (a, b) -> compareStr(a.title, b.title);  break;
            case CAPACITY:  cmp = (a, b) -> Integer.compare(b.capacity, a.capacity); break;
            case FILL_RATE: cmp = (a, b) -> Double.compare(fillRate(b), fillRate(a)); break;
            default:        cmp = (a, b) -> compareStr(a.date, b.date);    break;
        }
        result.sort(cmp);

        adapter.setData(result);
        binding.tvEventCount.setText(result.size() + " / " + allEvents.size() + " events  |  " + sortMode.label);
    }

    private static double fillRate(EventDto e) {
        return e.capacity > 0 ? (double) e.registeredCount / e.capacity : 0;
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
                .setTitle("Sort events by")
                .setItems(labels, (d, which) -> {
                    sortMode = modes[which];
                    binding.btnSort.setText(sortMode.label);
                    applyFilterAndSort();
                })
                .show();
    }

    // ── Stats bar ─────────────────────────────────────────────────────────────

    private void updateStatsBar(List<EventDto> events) {
        binding.statsRow.removeAllViews();
        long total     = events.size();
        long live      = events.stream().filter(e -> "Live".equals(deriveStatus(e))).count();
        long open      = events.stream().filter(e -> "Registration Open".equals(deriveStatus(e))).count();
        long soon      = events.stream().filter(e -> "Starting Soon".equals(deriveStatus(e))).count();
        long completed = events.stream().filter(e -> "Completed".equals(deriveStatus(e))).count();

        addStat("TOTAL",     String.valueOf(total),     "#00D1FF");
        addStat("LIVE",      String.valueOf(live),      "#22C55E");
        addStat("OPEN",      String.valueOf(open),      "#00D1FF");
        addStat("SOON",      String.valueOf(soon),      "#F59E0B");
        addStat("COMPLETED", String.valueOf(completed), "#8B5CF6");
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

        TextView tvValue = new TextView(requireContext());
        tvValue.setText(value);
        tvValue.setTextColor(Color.parseColor(hex));
        tvValue.setTextSize(18);
        tvValue.setTypeface(android.graphics.Typeface.MONOSPACE);
        tvValue.setGravity(Gravity.CENTER);

        TextView tvLabel = new TextView(requireContext());
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor(hex + "99"));
        tvLabel.setTextSize(9);
        tvLabel.setGravity(Gravity.CENTER);
        tvLabel.setLetterSpacing(0.1f);

        card.addView(tvValue);
        card.addView(tvLabel);
        binding.statsRow.addView(card);
    }

    private int dp(int v) {
        return (int)(v * requireContext().getResources().getDisplayMetrics().density);
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
            bindEvent(h.b, e);
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAdminEventBinding b;
            VH(ItemAdminEventBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    // ── Card binding ──────────────────────────────────────────────────────────

    private void bindEvent(ItemAdminEventBinding b, EventDto e) {
        // Banner
        Glide.with(requireContext()).load(e.bannerUrl).centerCrop()
                .placeholder(R.drawable.placeholder_banner).into(b.imgBanner);

        // Title
        b.tvEventTitle.setText(e.title != null ? e.title : "Untitled Event");

        // Date + Location
        b.tvEventDate.setText(formatDate(e.date));
        b.tvEventLocation.setText(e.location != null ? e.location : "");

        // Status badge
        String status = deriveStatus(e);
        String statusHex = statusColor(status);
        b.tvStatusBadge.setText(status);
        styleBadge(b.tvStatusBadge, statusHex);

        // Type badge
        String type = e.type != null ? e.type : "Event";
        String typeHex = typeColor(type);
        b.tvTypeBadge.setText(type);
        styleBadge(b.tvTypeBadge, typeHex);

        // Difficulty badge
        String diff = e.difficulty != null ? e.difficulty : "";
        b.tvDifficultyBadge.setText(diff);
        styleBadge(b.tvDifficultyBadge, difficultyColor(diff));

        // Capacity
        int registered = e.registeredCount;
        int capacity   = e.capacity;
        if (capacity > 0) {
            int seatsLeft = capacity - registered;
            int pct = Math.min(100, (int)((float) registered / capacity * 100));
            b.tvSeats.setText(registered + " / " + capacity + " seats");
            b.progressCapacity.setProgress(pct);
            b.progressCapacity.setVisibility(View.VISIBLE);
            if (seatsLeft <= 0) {
                b.tvSeatsStatus.setText("FULL");
                b.tvSeatsStatus.setTextColor(Color.parseColor("#EF4444"));
            } else if (seatsLeft <= 5) {
                b.tvSeatsStatus.setText("ALMOST FULL");
                b.tvSeatsStatus.setTextColor(Color.parseColor("#F59E0B"));
            } else {
                b.tvSeatsStatus.setText("OPEN");
                b.tvSeatsStatus.setTextColor(Color.parseColor("#22C55E"));
            }
        } else {
            b.tvSeats.setText("Unlimited capacity");
            b.tvSeatsStatus.setText("OPEN");
            b.tvSeatsStatus.setTextColor(Color.parseColor("#22C55E"));
            b.progressCapacity.setVisibility(View.GONE);
        }

        // Buttons
        b.btnEdit.setOnClickListener(v -> showEventDialog(e));
        b.btnParticipants.setOnClickListener(v -> showParticipants(e));
        b.btnDuplicate.setOnClickListener(v -> confirmDuplicate(e));
        b.btnMore.setOnClickListener(v -> showQuickActions(e, v));
        b.getRoot().setOnClickListener(v -> showEventDetail(e));

        // Update participants button label with live count
        new Thread(() -> {
            int count = viewModel.getParticipantCount(e.id);
            requireActivity().runOnUiThread(() ->
                    b.btnParticipants.setText("Members (" + count + ")"));
        }).start();
    }

    // ── Quick actions ─────────────────────────────────────────────────────────

    private void showQuickActions(EventDto e, View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add(0, 1, 0, "View Details");
        menu.getMenu().add(0, 2, 0, "Edit");
        menu.getMenu().add(0, 3, 0, "Duplicate");
        menu.getMenu().add(0, 4, 0, "Cancel Event");
        menu.getMenu().add(0, 5, 0, "Delete Event");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: showEventDetail(e);   break;
                case 2: showEventDialog(e);   break;
                case 3: confirmDuplicate(e);  break;
                case 4: confirmCancelEvent(e); break;
                case 5: confirmDelete(e);     break;
            }
            return true;
        });
        menu.show();
    }

    // ── Event detail sheet ────────────────────────────────────────────────────

    private void showEventDetail(EventDto e) {
        View dv = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_event_detail_sheet, null);

        // Banner
        ImageView imgBanner = dv.findViewById(R.id.imgDetailBanner);
        Glide.with(requireContext()).load(e.bannerUrl).centerCrop()
                .placeholder(R.drawable.placeholder_banner).into(imgBanner);

        // Badges
        String status  = deriveStatus(e);
        String type    = e.type != null ? e.type : "Event";
        TextView tvStatus = dv.findViewById(R.id.tvDetailStatusBadge);
        TextView tvType   = dv.findViewById(R.id.tvDetailTypeBadge);
        tvStatus.setText(status);   styleBadge(tvStatus, statusColor(status));
        tvType.setText(type);       styleBadge(tvType, typeColor(type));

        // Title + Difficulty
        ((TextView) dv.findViewById(R.id.tvDetailTitle)).setText(
                e.title != null ? e.title : "Untitled");
        TextView tvDiff = dv.findViewById(R.id.tvDetailDifficulty);
        String diff = e.difficulty != null ? e.difficulty : "";
        tvDiff.setText(diff);
        styleBadge(tvDiff, difficultyColor(diff));

        // Capacity stats
        ((TextView) dv.findViewById(R.id.tvDetailRegistered)).setText(
                String.valueOf(e.registeredCount));
        ((TextView) dv.findViewById(R.id.tvDetailCapacity)).setText(
                e.capacity > 0 ? String.valueOf(e.capacity) : "∞");
        int seatsLeft = e.capacity > 0 ? e.capacity - e.registeredCount : -1;
        ((TextView) dv.findViewById(R.id.tvDetailSeatsLeft)).setText(
                seatsLeft >= 0 ? String.valueOf(seatsLeft) : "∞");

        ProgressBar prog = dv.findViewById(R.id.progressDetailCapacity);
        if (e.capacity > 0) {
            prog.setProgress(Math.min(100, e.registeredCount * 100 / e.capacity));
        } else {
            prog.setProgress(0);
        }

        // Info
        ((TextView) dv.findViewById(R.id.tvDetailDate)).setText(formatDate(e.date));
        ((TextView) dv.findViewById(R.id.tvDetailLocation)).setText(
                e.location != null ? e.location : "N/A");
        ((TextView) dv.findViewById(R.id.tvDetailType)).setText(type);
        ((TextView) dv.findViewById(R.id.tvDetailDifficultyInfo)).setText(
                diff.isEmpty() ? "N/A" : diff);
        ((TextView) dv.findViewById(R.id.tvDetailDifficultyInfo))
                .setTextColor(Color.parseColor(difficultyColor(diff)));
        ((TextView) dv.findViewById(R.id.tvDetailDescription)).setText(
                e.description != null && !e.description.isEmpty()
                        ? e.description : "No description provided.");

        // Actions
        dv.findViewById(R.id.btnDetailEdit).setOnClickListener(v -> showEventDialog(e));
        dv.findViewById(R.id.btnDetailDuplicate).setOnClickListener(v -> confirmDuplicate(e));
        dv.findViewById(R.id.btnDetailCancel).setOnClickListener(v -> confirmCancelEvent(e));
        dv.findViewById(R.id.btnDetailDelete).setOnClickListener(v -> confirmDelete(e));

        new AlertDialog.Builder(requireContext())
                .setView(dv)
                .setNegativeButton("Close", null)
                .show();
    }

    // ── Create / Edit dialog ──────────────────────────────────────────────────

    private void showEventDialog(@Nullable EventDto existing) {
        pendingBannerUri = null;
        View dv = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_create_event, null);

        android.widget.ImageView imgPreview = dv.findViewById(R.id.imgBannerPreview);
        View btnPickBanner = dv.findViewById(R.id.btnPickBanner);
        android.widget.EditText etTitle       = dv.findViewById(R.id.etEventTitle);
        android.widget.EditText etDescription = dv.findViewById(R.id.etEventDescription);
        android.widget.EditText etLocation    = dv.findViewById(R.id.etEventLocation);
        android.widget.EditText etDate        = dv.findViewById(R.id.etEventDate);
        android.widget.EditText etType        = dv.findViewById(R.id.etEventType);
        android.widget.EditText etDifficulty  = dv.findViewById(R.id.etEventDifficulty);
        android.widget.EditText etCapacity    = dv.findViewById(R.id.etEventCapacity);

        if (existing != null) {
            etTitle.setText(existing.title);
            etDescription.setText(existing.description);
            etLocation.setText(existing.location);
            etDate.setText(existing.date);
            etType.setText(existing.type);
            etDifficulty.setText(existing.difficulty);
            etCapacity.setText(String.valueOf(existing.capacity));
            if (existing.bannerUrl != null && !existing.bannerUrl.isEmpty())
                Glide.with(this).load(existing.bannerUrl).centerCrop().into(imgPreview);
        }

        activePreview = imgPreview;
        btnPickBanner.setOnClickListener(v -> imagePicker.launch("image/*"));

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Create Event" : "Edit Event")
                .setView(dv)
                .setOnDismissListener(d -> activePreview = null)
                .setPositiveButton(existing == null ? "Create" : "Save", (d, w) -> {
                    String title    = etTitle.getText().toString().trim();
                    String desc     = etDescription.getText().toString().trim();
                    String location = etLocation.getText().toString().trim();
                    String date     = etDate.getText().toString().trim();
                    String type     = etType.getText().toString().trim();
                    String diff     = etDifficulty.getText().toString().trim();
                    String capStr   = etCapacity.getText().toString().trim();
                    int    capacity = capStr.isEmpty() ? 30 : Integer.parseInt(capStr);

                    String bannerUrl = pendingBannerUri != null
                            ? pendingBannerUri.toString()
                            : (existing != null ? existing.bannerUrl : null);

                    if (title.isEmpty()) return;

                    EventDto dto = new EventDto();
                    dto.title = title; dto.description = desc;
                    dto.location = location; dto.date = date;
                    dto.type  = type.isEmpty()  ? "Workshop" : type;
                    dto.difficulty = diff.isEmpty() ? "Beginner" : diff;
                    dto.capacity = capacity; dto.bannerUrl = bannerUrl;

                    if (existing == null) viewModel.createEvent(dto);
                    else                 viewModel.updateEvent(existing.id, dto);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void confirmDuplicate(EventDto e) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Duplicate event?")
                .setMessage("A copy of \"" + e.title + "\" will be created. You can update the date and details after.")
                .setPositiveButton("Duplicate", (d, w) -> {
                    viewModel.duplicateEvent(e);
                    viewModel.showToast("Event duplicated");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmCancelEvent(EventDto e) {
        android.widget.EditText etReason = new android.widget.EditText(requireContext());
        etReason.setHint("Reason for cancellation...");
        etReason.setHintTextColor(Color.parseColor("#3D4A5C"));
        etReason.setTextColor(Color.WHITE);
        etReason.setPadding(dp(16), dp(12), dp(16), dp(12));

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel \"" + e.title + "\"?")
                .setMessage("Registered participants will be notified. Enter a reason:")
                .setView(etReason)
                .setPositiveButton("Cancel Event", (d, w) -> {
                    String reason = etReason.getText().toString().trim();
                    viewModel.auditAction("CANCEL", "Event",
                            e.title + (reason.isEmpty() ? "" : " — " + reason));
                    viewModel.showToast("Event canceled — logged to audit");
                })
                .setNegativeButton("Go Back", null)
                .show();
    }

    private void confirmDelete(EventDto e) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete event?")
                .setMessage("\"" + e.title + "\" will be permanently deleted.")
                .setPositiveButton("Delete", (d, w) -> viewModel.deleteEvent(e.id))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Participant management ─────────────────────────────────────────────────

    private void showParticipants(EventDto event) {
        new Thread(() -> {
            List<com.cic.mobapp.data.local.entity.EventRegistrationEntity> participants =
                    viewModel.getParticipantsSync(event.id);
            List<String> registeredIds = viewModel.getParticipantIds(event.id);
            requireActivity().runOnUiThread(() ->
                    openParticipantSheet(event, participants, registeredIds));
        }).start();
    }

    private void openParticipantSheet(
            EventDto event,
            List<com.cic.mobapp.data.local.entity.EventRegistrationEntity> participants,
            List<String> registeredIds) {

        com.cic.mobapp.databinding.LayoutEventParticipantsBinding dv =
                com.cic.mobapp.databinding.LayoutEventParticipantsBinding.inflate(
                        LayoutInflater.from(requireContext()));

        // Stats bar
        int count      = participants.size();
        int capacity   = event.capacity;
        int attending  = 0;
        for (com.cic.mobapp.data.local.entity.EventRegistrationEntity r : participants)
            if ("ATTENDING".equals(r.status)) attending++;

        dv.tvParticipantCount.setText(String.valueOf(count));
        dv.tvCapacityInfo.setText(capacity > 0 ? String.valueOf(capacity) : "∞");
        dv.tvAttendingCount.setText(String.valueOf(attending));
        if (capacity > 0)
            dv.progressFill.setProgress(Math.min(100, count * 100 / capacity));

        // Adapter
        ParticipantAdapter adapter = new ParticipantAdapter(event, participants);
        dv.rvParticipants.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Participants — " + event.title)
                .setView(dv.getRoot())
                .setNegativeButton("Close", null)
                .create();

        dv.btnAddParticipant.setOnClickListener(v ->
                pickMemberToAdd(event, registeredIds, dialog, dv, adapter));

        dialog.show();
    }

    private void pickMemberToAdd(EventDto event, List<String> alreadyIn,
                                 AlertDialog parentDialog,
                                 com.cic.mobapp.databinding.LayoutEventParticipantsBinding dv,
                                 ParticipantAdapter adapter) {
        List<com.cic.mobapp.data.remote.dto.UserDto> all = viewModel.getUsers().getValue();
        if (all == null || all.isEmpty()) { viewModel.showToast("No members loaded"); return; }

        List<com.cic.mobapp.data.remote.dto.UserDto> eligible = new java.util.ArrayList<>();
        for (com.cic.mobapp.data.remote.dto.UserDto u : all)
            if (!alreadyIn.contains(u.id)) eligible.add(u);

        if (eligible.isEmpty()) { viewModel.showToast("All members are already registered"); return; }

        String[] labels = new String[eligible.size()];
        for (int i = 0; i < eligible.size(); i++)
            labels[i] = eligible.get(i).username + "  [" + eligible.get(i).role + "]";

        new AlertDialog.Builder(requireContext())
                .setTitle("Add member to event")
                .setItems(labels, (d, which) -> {
                    com.cic.mobapp.data.remote.dto.UserDto selected = eligible.get(which);
                    viewModel.registerParticipant(event.id, selected);
                    alreadyIn.add(selected.id);
                    // Refresh list after brief delay for Room write
                    dv.getRoot().postDelayed(() -> new Thread(() -> {
                        List<com.cic.mobapp.data.local.entity.EventRegistrationEntity> updated =
                                viewModel.getParticipantsSync(event.id);
                        requireActivity().runOnUiThread(() -> {
                            adapter.setData(updated);
                            dv.tvParticipantCount.setText(String.valueOf(updated.size()));
                        });
                    }).start(), 300);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Participant adapter ────────────────────────────────────────────────────

    class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.VH> {
        private final EventDto event;
        private List<com.cic.mobapp.data.local.entity.EventRegistrationEntity> data;

        ParticipantAdapter(EventDto event,
                           List<com.cic.mobapp.data.local.entity.EventRegistrationEntity> data) {
            this.event = event;
            this.data  = new java.util.ArrayList<>(data);
        }

        void setData(List<com.cic.mobapp.data.local.entity.EventRegistrationEntity> d) {
            data = new java.util.ArrayList<>(d);
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(com.cic.mobapp.databinding.ItemParticipantBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            com.cic.mobapp.data.local.entity.EventRegistrationEntity reg = data.get(pos);

            h.b.tvParticipantName.setText(reg.username != null ? reg.username : reg.userId);
            h.b.tvParticipantRole.setText(
                    (reg.userRole != null ? reg.userRole : "Member")
                    + " · Joined " + (reg.registeredAt != null ? reg.registeredAt : "—"));

            // Status badge
            String status = reg.status != null ? reg.status : "REGISTERED";
            h.b.tvParticipantStatus.setText(status);
            styleBadge(h.b.tvParticipantStatus, statusToColor(status));

            // Avatar
            Glide.with(requireContext()).load(reg.avatarUrl).circleCrop()
                    .placeholder(R.drawable.bg_avatar_circle)
                    .into(h.b.imgParticipantAvatar);

            // Attend toggle — cycles REGISTERED → ATTENDING → ABSENT → REGISTERED
            h.b.btnMarkAttend.setOnClickListener(v -> {
                String next = "ATTENDING".equals(status) ? "ABSENT"
                            : "ABSENT".equals(status)    ? "REGISTERED"
                            : "ATTENDING";
                viewModel.updateParticipantStatus(event.id, reg.userId, next);
                reg.status = next;
                notifyItemChanged(pos);
            });

            // Remove
            h.b.btnRemoveParticipant.setOnClickListener(v -> {
                String uname = reg.username != null ? reg.username : reg.userId;
                new AlertDialog.Builder(requireContext())
                        .setTitle("Remove participant?")
                        .setMessage("Remove " + uname + " from this event?")
                        .setPositiveButton("Remove", (d, w) -> {
                            viewModel.unregisterParticipant(event.id, reg.userId, uname);
                            int idx = h.getAdapterPosition();
                            if (idx >= 0) { data.remove(idx); notifyItemRemoved(idx); }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final com.cic.mobapp.databinding.ItemParticipantBinding b;
            VH(com.cic.mobapp.databinding.ItemParticipantBinding b) {
                super(b.getRoot()); this.b = b;
            }
        }
    }

    private static String statusToColor(String status) {
        if (status == null) return "#8B9AB0";
        switch (status) {
            case "ATTENDING": return "#22C55E";
            case "ABSENT":    return "#EF4444";
            default:          return "#00D1FF";
        }
    }

    // ── Status / type / difficulty helpers ────────────────────────────────────

    static String deriveStatus(EventDto e) {
        if (e.date == null || e.date.isEmpty()) return "Draft";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(e.date);
            if (d == null) return "Published";
            long diff = d.getTime() - System.currentTimeMillis();
            if (diff < -7_200_000) return "Completed";
            if (diff < 0)          return "Live";
            if (diff < 86_400_000) return "Starting Soon";
            if (e.capacity > 0 && e.registeredCount >= e.capacity) return "Full";
            return "Registration Open";
        } catch (Exception ex) { return "Published"; }
    }

    private static String statusColor(String s) {
        switch (s != null ? s : "") {
            case "Live":              return "#22C55E";
            case "Starting Soon":    return "#F59E0B";
            case "Registration Open": return "#00D1FF";
            case "Full":             return "#F97316";
            case "Completed":        return "#8B5CF6";
            case "Draft":            return "#8B9AB0";
            default:                 return "#3D8EFF";
        }
    }

    private static String typeColor(String t) {
        switch (t != null ? t : "") {
            case "CTF":           return "#EF4444";
            case "Bootcamp":      return "#F59E0B";
            case "Conference":    return "#8B5CF6";
            case "Hackathon":     return "#EC4899";
            case "Meeting":       return "#22C55E";
            default:              return "#00D1FF";
        }
    }

    private static String difficultyColor(String d) {
        switch (d != null ? d : "") {
            case "Beginner":     return "#22C55E";
            case "Intermediate": return "#F59E0B";
            case "Advanced":     return "#EF4444";
            case "Expert":       return "#8B5CF6";
            default:             return "#8B9AB0";
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

    private static String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "Date TBD";
        try {
            String[] parts = iso.split("T");
            String[] d = parts[0].split("-");
            String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                               "Jul","Aug","Sep","Oct","Nov","Dec"};
            String month = months[Integer.parseInt(d[1]) - 1];
            String time  = parts.length > 1 && parts[1].length() >= 5
                    ? "  " + parts[1].substring(0, 5) : "";
            return month + " " + d[2] + ", " + d[0] + time;
        } catch (Exception e) { return iso.substring(0, 10); }
    }
}
