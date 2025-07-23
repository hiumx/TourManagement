package com.example.tourmanagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tourmanagement.R;
import com.example.tourmanagement.model.User;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying and managing users in the UserManagementActivity.
 * Provides functionality for admins to view, edit, delete, and manage user accounts.
 */
public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {

    private Context context;
    private List<User> users;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEditUser(User user);
        void onDeleteUser(User user);
        void onToggleAdminStatus(User user);
        void onViewUserDetails(User user);
    }

    public UserManagementAdapter(Context context, OnUserActionListener listener) {
        this.context = context;
        this.users = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_management, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users.clear();
        this.users.addAll(newUsers);
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName, tvUserEmail, tvUserPhone, tvAdminBadge;
        private ImageView ivUserAvatar, ivAdminIcon;
        private MaterialButton btnViewDetails, btnEditUser, btnToggleAdmin, btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserPhone = itemView.findViewById(R.id.tv_user_phone);
            tvAdminBadge = itemView.findViewById(R.id.tv_admin_badge);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            ivAdminIcon = itemView.findViewById(R.id.iv_admin_icon);

            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnEditUser = itemView.findViewById(R.id.btn_edit_user);
            btnToggleAdmin = itemView.findViewById(R.id.btn_toggle_admin);
            btnDeleteUser = itemView.findViewById(R.id.btn_delete_user);
        }

        public void bind(User user) {
            tvUserName.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
            tvUserEmail.setText(user.getEmail());
            tvUserPhone.setText(user.getPhoneNumber());

            // Show/hide admin indicators
            if (user.isAdmin()) {
                tvAdminBadge.setVisibility(View.VISIBLE);
                ivAdminIcon.setVisibility(View.VISIBLE);
                btnToggleAdmin.setText("Remove Admin");
            } else {
                tvAdminBadge.setVisibility(View.GONE);
                ivAdminIcon.setVisibility(View.GONE);
                btnToggleAdmin.setText("Make Admin");
            }

            // Set click listeners
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewUserDetails(user);
                }
            });

            btnEditUser.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditUser(user);
                }
            });

            btnToggleAdmin.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleAdminStatus(user);
                }
            });

            btnDeleteUser.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUser(user);
                }
            });
        }
    }
}
