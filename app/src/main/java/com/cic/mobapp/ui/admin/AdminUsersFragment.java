package com.cic.mobapp.ui.admin;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.cic.mobapp.data.remote.dto.UserDto;
import com.cic.mobapp.databinding.FragmentAdminUsersBinding;
import com.cic.mobapp.databinding.ItemAdminUserBinding;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminUsersFragment extends Fragment {

    // ── Sort options ──────────────────────────────────────────────────────────
    private enum SortMode {
        XP_DESC("Sort: XP"),
        LEVEL_DESC("Sort: Level"),
        JOIN_NEWEST("Sort: Newest"),
        JOIN_OLDEST("Sort: Oldest"),
        NAME_AZ("Sort: A-Z"),
        ROLE("Sort: Role");

        final String label;
        SortMode(String l) { this.label = l; }
    }

    private FragmentAdminUsersBinding binding;
    private AdminViewModel viewModel;
    private UsersAdapter adapter;

    private List<UserDto> allUsers   = new ArrayList<>();
    private String        activeFilter = "ALL";
    private String        searchQuery  = "";
    private SortMode      sortMode     = SortMode.XP_DESC;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        binding = FragmentAdminUsersBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new UsersAdapter();
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsers.setAdapter(adapter);

        // Observe user list
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            allUsers = users != null ? users : new ArrayList<>();
            updateStatsBar(allUsers);
            applyFilterAndSort();
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.swipeRefresh.setRefreshing(false);
        });

        // Search
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { searchQuery = q; applyFilterAndSort(); return true; }
            @Override public boolean onQueryTextChange(String q) { searchQuery = q; applyFilterAndSort(); return false; }
        });

        // Filter chips
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == binding.chipAdmin.getId())     activeFilter = "Administrator";
            else if (id == binding.chipMentor.getId())    activeFilter = "Mentor";
            else if (id == binding.chipOrganizer.getId()) activeFilter = "Event Organizer";
            else if (id == binding.chipMember.getId())    activeFilter = "Member";
            else if (id == binding.chipGuest.getId())     activeFilter = "Guest";
            else if (id == binding.chipActive.getId())    activeFilter = "ACTIVE";
            else if (id == binding.chipDiscord.getId())   activeFilter = "DISCORD";
            else                                           activeFilter = "ALL";
            applyFilterAndSort();
        });

        // Sort button
        binding.btnSort.setOnClickListener(v -> showSortPicker());
    }

    // ── Filter + Sort + Search ────────────────────────────────────────────────

    private void applyFilterAndSort() {
        List<UserDto> result = new ArrayList<>(allUsers);

        // Filter by role / special
        if (!activeFilter.equals("ALL")) {
            if (activeFilter.equals("ACTIVE")) {
                result = result.stream().filter(u -> u.xp >= 100).collect(Collectors.toList());
            } else if (activeFilter.equals("DISCORD")) {
                result = result.stream().filter(u -> u.discordId != null && !u.discordId.isEmpty()).collect(Collectors.toList());
            } else {
                final String f = activeFilter;
                result = result.stream().filter(u -> f.equalsIgnoreCase(u.role)).collect(Collectors.toList());
            }
        }

        // Search
        if (searchQuery != null && !searchQuery.isEmpty()) {
            String q = searchQuery.toLowerCase();
            result = result.stream()
                    .filter(u -> (u.username != null && u.username.toLowerCase().contains(q))
                              || (u.email    != null && u.email.toLowerCase().contains(q))
                              || (u.role     != null && u.role.toLowerCase().contains(q))
                              || (u.id       != null && u.id.toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        // Sort
        Comparator<UserDto> cmp;
        switch (sortMode) {
            case LEVEL_DESC:   cmp = (a, b) -> Integer.compare(b.level, a.level); break;
            case JOIN_NEWEST:  cmp = (a, b) -> compareStr(b.createdAt, a.createdAt); break;
            case JOIN_OLDEST:  cmp = (a, b) -> compareStr(a.createdAt, b.createdAt); break;
            case NAME_AZ:      cmp = (a, b) -> compareStr(a.username, b.username);    break;
            case ROLE:         cmp = (a, b) -> roleRank(a.role) - roleRank(b.role);  break;
            default:           cmp = (a, b) -> Integer.compare(b.xp, a.xp);          break;
        }
        result.sort(cmp);

        adapter.setData(result);
        binding.tvUserCount.setText(result.size() + " / " + allUsers.size() + " members  |  " + sortMode.label);
    }

    private static int compareStr(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }

    private static int roleRank(String role) {
        if (role == null) return 99;
        switch (role) {
            case "Administrator":   return 0;
            case "Mentor":          return 1;
            case "Event Organizer": return 2;
            case "Moderator":       return 3;
            case "Member":          return 4;
            default:                return 5;
        }
    }

    private void showSortPicker() {
        SortMode[] modes = SortMode.values();
        String[] labels = new String[modes.length];
        for (int i = 0; i < modes.length; i++) labels[i] = modes[i].label;
        new AlertDialog.Builder(requireContext())
                .setTitle("Sort members by")
                .setItems(labels, (d, which) -> {
                    sortMode = modes[which];
                    binding.btnSort.setText(sortMode.label);
                    applyFilterAndSort();
                })
                .show();
    }

    // ── Stats bar ─────────────────────────────────────────────────────────────

    private void updateStatsBar(List<UserDto> users) {
        binding.statsRow.removeAllViews();
        long total    = users.size();
        long active   = users.stream().filter(u -> u.xp >= 100).count();
        long admins   = users.stream().filter(u -> "Administrator".equalsIgnoreCase(u.role)).count();
        long mentors  = users.stream().filter(u -> "Mentor".equalsIgnoreCase(u.role)).count();
        long discord  = users.stream().filter(u -> u.discordId != null && !u.discordId.isEmpty()).count();

        addStat("TOTAL",   String.valueOf(total),   "#00D1FF");
        addStat("ACTIVE",  String.valueOf(active),  "#22C55E");
        addStat("ADMINS",  String.valueOf(admins),  "#EF4444");
        addStat("MENTORS", String.valueOf(mentors), "#8B5CF6");
        addStat("DISCORD", String.valueOf(discord), "#7289DA");
    }

    private void addStat(String label, String value, String colorHex) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(18), dp(4), dp(18), dp(4));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(10));
        bg.setColor(Color.parseColor(colorHex + "22"));
        bg.setStroke(dp(1), Color.parseColor(colorHex + "55"));
        card.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(8));
        card.setLayoutParams(lp);

        TextView tvValue = new TextView(requireContext());
        tvValue.setText(value);
        tvValue.setTextColor(Color.parseColor(colorHex));
        tvValue.setTextSize(18);
        tvValue.setTypeface(android.graphics.Typeface.MONOSPACE);
        tvValue.setGravity(Gravity.CENTER);

        TextView tvLabel = new TextView(requireContext());
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor(colorHex + "99"));
        tvLabel.setTextSize(9);
        tvLabel.setGravity(Gravity.CENTER);
        tvLabel.setLetterSpacing(0.1f);

        card.addView(tvValue);
        card.addView(tvLabel);
        binding.statsRow.addView(card);
    }

    private int dp(int dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density);
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }

    // ── Adapter ───────────────────────────────────────────────────────────────

    class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.VH> {
        private List<UserDto> data = new ArrayList<>();

        void setData(List<UserDto> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemAdminUserBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            UserDto u = data.get(pos);
            bindUser(h.b, u);
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAdminUserBinding b;
            VH(ItemAdminUserBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    private void bindUser(ItemAdminUserBinding b, UserDto u) {
        // Avatar
        Glide.with(requireContext()).load(u.avatarUrl).circleCrop()
                .placeholder(R.drawable.bg_avatar_circle).into(b.imgAvatar);

        // Username + email
        b.tvUsername.setText(u.username != null ? u.username : "Unknown");
        b.tvEmail.setText(u.email != null ? u.email : "");

        // Role badge
        String role = u.role != null ? u.role : "Guest";
        b.tvRoleBadge.setText(role);
        styleRoleBadge(b.tvRoleBadge, role);

        // Level + XP + Joined
        b.tvLevel.setText("Lv " + u.level);
        b.tvXp.setText(u.xp + " XP");
        b.tvJoined.setText(u.createdAt != null && u.createdAt.length() >= 7
                ? u.createdAt.substring(0, 7) : "");

        // XP progress bar
        b.progressXp.setProgress(u.xp % 1000);

        // Status dot
        int dotColor = statusColor(u);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(dotColor);
        dot.setStroke(dp(2), Color.parseColor("#12161C"));
        b.viewStatusDot.setBackground(dot);

        // Activity label
        String[] act = activityLabel(u);
        b.tvActivityLabel.setText(act[0]);
        b.tvActivityLabel.setTextColor(Color.parseColor(act[1]));

        // Discord badge
        boolean hasDiscord = u.discordId != null && !u.discordId.isEmpty();
        b.tvDiscordBadge.setVisibility(hasDiscord ? View.VISIBLE : View.GONE);

        // More button
        b.btnMore.setOnClickListener(v -> showQuickActions(u, v));

        // Tap card = open detail
        b.getRoot().setOnClickListener(v -> showUserDetail(u));
    }

    // ── Quick actions popup ───────────────────────────────────────────────────

    private void showQuickActions(UserDto user, View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add(0, 1, 0, "View Profile");
        menu.getMenu().add(0, 2, 0, "Change Role");
        menu.getMenu().add(0, 3, 0, "Issue Warning");
        menu.getMenu().add(0, 4, 0, "Mute User");
        menu.getMenu().add(0, 5, 0, "Suspend");
        menu.getMenu().add(0, 6, 0, "Ban User");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: showUserDetail(user);     break;
                case 2: showRolePicker(user);     break;
                case 3: issueWarning(user);       break;
                case 4: muteUser(user);           break;
                case 5: suspendUser(user);        break;
                case 6: confirmBan(user);         break;
            }
            return true;
        });
        menu.show();
    }

    // ── User detail bottom sheet ──────────────────────────────────────────────

    private void showUserDetail(UserDto u) {
        View dv = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_user_detail_sheet, null);

        // Header
        ImageView imgAvatar = dv.findViewById(R.id.imgDetailAvatar);
        Glide.with(requireContext()).load(u.avatarUrl).circleCrop()
                .placeholder(R.drawable.bg_avatar_circle).into(imgAvatar);

        TextView tvName = dv.findViewById(R.id.tvDetailUsername);
        tvName.setText(u.username != null ? u.username : "Unknown");

        TextView tvRole = dv.findViewById(R.id.tvDetailRoleBadge);
        tvRole.setText(u.role != null ? u.role : "Guest");
        styleRoleBadge(tvRole, u.role != null ? u.role : "Guest");

        // Status dot
        View statusDot = dv.findViewById(R.id.viewDetailStatusDot);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(statusColor(u));
        dot.setStroke(dp(2), Color.parseColor("#12161C"));
        statusDot.setBackground(dot);

        // Activity status text
        String[] act = activityLabel(u);
        TextView tvStatus = dv.findViewById(R.id.tvDetailActivityStatus);
        tvStatus.setText(act[0]);
        tvStatus.setTextColor(Color.parseColor(act[1]));

        // Quick stats
        ((TextView) dv.findViewById(R.id.tvDetailLevel)).setText(String.valueOf(u.level));
        ((TextView) dv.findViewById(R.id.tvDetailXp)).setText(String.valueOf(u.xp));
        int xpInLevel = u.xp % 1000;
        ((TextView) dv.findViewById(R.id.tvDetailXpProgress)).setText(String.valueOf(xpInLevel));

        // Identity
        ((TextView) dv.findViewById(R.id.tvDetailId)).setText(
                u.id != null ? u.id : "N/A");
        ((TextView) dv.findViewById(R.id.tvDetailEmail)).setText(
                u.email != null ? u.email : "N/A");

        TextView tvDiscord = dv.findViewById(R.id.tvDetailDiscord);
        if (u.discordId != null && !u.discordId.isEmpty()) {
            tvDiscord.setText("Linked (" + u.discordId + ")");
            tvDiscord.setTextColor(Color.parseColor("#7289DA"));
        } else {
            tvDiscord.setText("Not linked");
            tvDiscord.setTextColor(Color.parseColor("#3D4A5C"));
        }

        ((TextView) dv.findViewById(R.id.tvDetailJoined)).setText(
                u.createdAt != null && u.createdAt.length() >= 10
                        ? u.createdAt.substring(0, 10) : "Unknown");

        // Activity profile
        String[] eng = engagementProfile(u);
        ((TextView) dv.findViewById(R.id.tvDetailEngagement)).setText(eng[0]);
        ((TextView) dv.findViewById(R.id.tvDetailEngagement))
                .setTextColor(Color.parseColor(eng[1]));
        ((TextView) dv.findViewById(R.id.tvDetailEngagementDesc)).setText(eng[2]);

        ProgressBar progressXp = dv.findViewById(R.id.progressDetailXp);
        progressXp.setProgress(xpInLevel);
        ((TextView) dv.findViewById(R.id.tvDetailXpLabel))
                .setText(xpInLevel + " / 1000 XP");

        // Action buttons
        dv.findViewById(R.id.btnDetailRole).setOnClickListener(v -> showRolePicker(u));
        dv.findViewById(R.id.btnDetailWarn).setOnClickListener(v -> issueWarning(u));
        dv.findViewById(R.id.btnDetailMute).setOnClickListener(v -> muteUser(u));
        dv.findViewById(R.id.btnDetailSuspend).setOnClickListener(v -> suspendUser(u));
        dv.findViewById(R.id.btnDetailBan).setOnClickListener(v -> confirmBan(u));

        new AlertDialog.Builder(requireContext())
                .setView(dv)
                .setNegativeButton("Close", null)
                .show();
    }

    // ── Moderation actions ────────────────────────────────────────────────────

    private void showRolePicker(UserDto user) {
        String[] roles = {"Guest", "Member", "Mentor", "Event Organizer", "Moderator", "Administrator"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Change role — " + user.username)
                .setItems(roles, (d, which) ->
                        viewModel.changeUserRole(user.id, roles[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void issueWarning(UserDto user) {
        EditText etReason = new EditText(requireContext());
        etReason.setHint("Reason for warning...");
        etReason.setHintTextColor(Color.parseColor("#3D4A5C"));
        etReason.setTextColor(Color.WHITE);
        etReason.setPadding(dp(16), dp(12), dp(16), dp(12));

        new AlertDialog.Builder(requireContext())
                .setTitle("Issue Warning to " + user.username)
                .setMessage("Describe the reason for this warning. It will be logged to the audit trail.")
                .setView(etReason)
                .setPositiveButton("Issue Warning", (d, w) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) reason = "No reason specified";
                    viewModel.auditAction("WARN", "User", user.username + " — " + reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void muteUser(UserDto user) {
        String[] durations = {"1 hour", "6 hours", "24 hours", "7 days", "Permanent"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Mute " + user.username)
                .setMessage("Select mute duration:")
                .setItems(durations, (d, which) -> {
                    String dur = durations[which];
                    viewModel.auditAction("MUTE", "User", user.username + " for " + dur);
                    viewModel.showToast("Muted " + user.username + " for " + dur);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void suspendUser(UserDto user) {
        String[] durations = {"1 day", "3 days", "7 days", "14 days", "30 days"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Suspend " + user.username)
                .setMessage("Account will be temporarily disabled. Select duration:")
                .setItems(durations, (d, which) -> {
                    String dur = durations[which];
                    viewModel.auditAction("SUSPEND", "User", user.username + " for " + dur);
                    viewModel.showToast("Suspended " + user.username + " for " + dur);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmBan(UserDto user) {
        EditText etConfirm = new EditText(requireContext());
        etConfirm.setHint("Type BAN to confirm");
        etConfirm.setHintTextColor(Color.parseColor("#3D4A5C"));
        etConfirm.setTextColor(Color.parseColor("#EF4444"));
        etConfirm.setPadding(dp(16), dp(12), dp(16), dp(12));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Ban " + user.username + "?")
                .setMessage("This permanently deletes the account. Type BAN in the field below to confirm.")
                .setView(etConfirm)
                .setPositiveButton("Ban User", null) // set below to prevent auto-dismiss
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if ("BAN".equals(etConfirm.getText().toString().trim())) {
                    viewModel.deleteUser(user.id, user.username);
                    dialog.dismiss();
                } else {
                    etConfirm.setError("Type BAN (all caps) to confirm");
                }
            });
        });
        dialog.show();
    }

    // ── Styling helpers ───────────────────────────────────────────────────────

    private static void styleRoleBadge(TextView tv, String role) {
        String hex;
        switch (role != null ? role : "") {
            case "Administrator":   hex = "#EF4444"; break;
            case "Mentor":          hex = "#8B5CF6"; break;
            case "Event Organizer": hex = "#F59E0B"; break;
            case "Moderator":       hex = "#3D8EFF"; break;
            case "Member":          hex = "#00D1FF"; break;
            default:                hex = "#8B9AB0"; break;
        }
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(20f);
        bg.setColor(Color.parseColor(hex + "22"));
        bg.setStroke(1, Color.parseColor(hex + "88"));
        tv.setBackground(bg);
        tv.setTextColor(Color.parseColor(hex));
    }

    private static int statusColor(UserDto u) {
        if (u.xp >= 500) return Color.parseColor("#22C55E"); // green — active
        if (u.xp >= 50)  return Color.parseColor("#F59E0B"); // amber — regular
        return Color.parseColor("#3D4A5C");                   // gray  — new/inactive
    }

    private static String[] activityLabel(UserDto u) {
        if (u.xp >= 1000) return new String[]{"HIGHLY ACTIVE", "#22C55E"};
        if (u.xp >= 500)  return new String[]{"ACTIVE",        "#22C55E"};
        if (u.xp >= 100)  return new String[]{"REGULAR",       "#F59E0B"};
        if (u.xp >= 10)   return new String[]{"NEW MEMBER",    "#00D1FF"};
        return                   new String[]{"INACTIVE",       "#3D4A5C"};
    }

    private static String[] engagementProfile(UserDto u) {
        if (u.xp >= 1000) return new String[]{
            "Community Champion",  "#22C55E",
            "Highly active contributor. Has accumulated significant XP and demonstrated strong engagement across club activities."
        };
        if (u.xp >= 500) return new String[]{
            "Active Contributor",  "#22C55E",
            "Regular participant with solid engagement. Consistently contributes to club events and resources."
        };
        if (u.xp >= 100) return new String[]{
            "Regular Member",      "#F59E0B",
            "Moderately engaged member. Participates in some activities but has room to grow within the community."
        };
        if (u.xp >= 10) return new String[]{
            "New Member",          "#00D1FF",
            "Recently joined and getting started. Limited activity so far — may need onboarding support."
        };
        return new String[]{
            "Inactive",            "#3D4A5C",
            "No recorded activity. Account may be dormant or not yet onboarded into club activities."
        };
    }
}
