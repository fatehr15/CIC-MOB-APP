package com.cic.mobapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import java.util.stream.Collectors;

public class AdminUsersFragment extends Fragment {

    private FragmentAdminUsersBinding binding;
    private AdminViewModel viewModel;
    private UsersAdapter adapter;
    private List<UserDto> allUsers = new ArrayList<>();

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

        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            allUsers = users != null ? users : new ArrayList<>();
            adapter.setData(allUsers);
            binding.tvUserCount.setText(allUsers.size() + " members");
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { filter(q); return true; }
            @Override public boolean onQueryTextChange(String q) { filter(q); return false; }
        });
    }

    private void filter(String query) {
        if (query == null || query.isEmpty()) { adapter.setData(allUsers); return; }
        String q = query.toLowerCase();
        adapter.setData(allUsers.stream()
                .filter(u -> (u.username != null && u.username.toLowerCase().contains(q))
                          || (u.email    != null && u.email.toLowerCase().contains(q))
                          || (u.role     != null && u.role.toLowerCase().contains(q)))
                .collect(Collectors.toList()));
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
            h.b.tvUsername.setText(u.username);
            h.b.tvEmail.setText(u.email);
            h.b.tvRole.setText(u.role);

            Glide.with(requireContext())
                    .load(u.avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.bg_avatar_circle)
                    .into(h.b.imgAvatar);

            h.b.btnChangeRole.setOnClickListener(v -> showRolePicker(u));
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAdminUserBinding b;
            VH(ItemAdminUserBinding b) { super(b.getRoot()); this.b = b; }
        }
    }

    private void showRolePicker(UserDto user) {
        String[] roles = {"Guest", "Member", "Mentor", "Event Organizer", "Administrator"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Role for " + user.username)
                .setItems(roles, (d, which) -> viewModel.changeUserRole(user.id, roles[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
