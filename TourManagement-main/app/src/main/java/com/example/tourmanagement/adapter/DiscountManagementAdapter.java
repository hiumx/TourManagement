package com.example.tourmanagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tourmanagement.R;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Discount;
import com.example.tourmanagement.model.Tour;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying discounts in the discount management interface
 */
public class DiscountManagementAdapter extends RecyclerView.Adapter<DiscountManagementAdapter.DiscountViewHolder> {

    private Context context;
    private List<Discount> discounts;
    private OnDiscountActionListener listener;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;
    private TourManagementDatabase database;

    public interface OnDiscountActionListener {
        void onEditDiscount(Discount discount);
        void onDeleteDiscount(Discount discount);
        void onToggleDiscountStatus(Discount discount);
    }

    public DiscountManagementAdapter(Context context, OnDiscountActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.discounts = new ArrayList<>();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        this.dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.database = TourManagementDatabase.getDatabase(context);
    }

    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_discount_management, parent, false);
        return new DiscountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscountViewHolder holder, int position) {
        Discount discount = discounts.get(position);
        holder.bind(discount);
    }

    @Override
    public int getItemCount() {
        return discounts.size();
    }

    public void updateDiscounts(List<Discount> newDiscounts) {
        this.discounts.clear();
        this.discounts.addAll(newDiscounts);
        notifyDataSetChanged();
    }

    public class DiscountViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDiscountName, tvDiscountDescription, tvDiscountValue;
        private TextView tvDiscountType, tvValidityPeriod, tvUsageInfo;
        private TextView tvTourName, tvMinOrder;
        private ImageView ivDiscountStatus, ivDiscountType;
        private Button btnEdit, btnDelete, btnToggleStatus;

        public DiscountViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDiscountName = itemView.findViewById(R.id.tv_discount_name);
            tvDiscountDescription = itemView.findViewById(R.id.tv_discount_description);
            tvDiscountValue = itemView.findViewById(R.id.tv_discount_value);
            tvDiscountType = itemView.findViewById(R.id.tv_discount_type);
            tvValidityPeriod = itemView.findViewById(R.id.tv_validity_period);
            tvUsageInfo = itemView.findViewById(R.id.tv_usage_info);
            tvTourName = itemView.findViewById(R.id.tv_tour_name);
            tvMinOrder = itemView.findViewById(R.id.tv_min_order);
            ivDiscountStatus = itemView.findViewById(R.id.iv_discount_status);
            ivDiscountType = itemView.findViewById(R.id.iv_discount_type);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnToggleStatus = itemView.findViewById(R.id.btn_toggle_status);

            // Set click listeners
            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditDiscount(discounts.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteDiscount(discounts.get(position));
                }
            });

            btnToggleStatus.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onToggleDiscountStatus(discounts.get(position));
                }
            });
        }

        public void bind(Discount discount) {
            // Basic discount information
            tvDiscountName.setText(discount.getDiscountName());
            tvDiscountDescription.setText(discount.getDescription());

            // Discount value and type
            if (discount.getDiscountType().equals(Discount.DiscountType.PERCENTAGE)) {
                tvDiscountValue.setText((int)discount.getDiscountValue() + "%");
                tvDiscountType.setText("Percentage Discount");
                ivDiscountType.setImageResource(R.drawable.ic_percent);
            } else {
                tvDiscountValue.setText(currencyFormatter.format(discount.getDiscountValue()));
                tvDiscountType.setText("Fixed Amount Discount");
                ivDiscountType.setImageResource(R.drawable.ic_money);
            }

            // Validity period
            Date startDate = new Date(discount.getStartDate());
            Date endDate = new Date(discount.getEndDate());
            tvValidityPeriod.setText(dateFormatter.format(startDate) + " - " + dateFormatter.format(endDate));

            // Usage information
            if (discount.getUsageLimit() > 0) {
                int remaining = discount.getUsageLimit() - discount.getCurrentUsage();
                tvUsageInfo.setText(discount.getCurrentUsage() + "/" + discount.getUsageLimit() + " used (" + remaining + " remaining)");
            } else {
                tvUsageInfo.setText(discount.getCurrentUsage() + " used (Unlimited)");
            }

            // Tour-specific or global
            if (discount.getTourId() != null) {
                Tour tour = database.tourDao().getTourById(discount.getTourId());
                if (tour != null) {
                    tvTourName.setText("Tour: " + tour.getTourName());
                    tvTourName.setVisibility(View.VISIBLE);
                } else {
                    tvTourName.setText("Tour: Not Found");
                    tvTourName.setVisibility(View.VISIBLE);
                }
            } else {
                tvTourName.setText("Global Discount");
                tvTourName.setVisibility(View.VISIBLE);
            }

            // Minimum order amount
            if (discount.getMinOrderAmount() > 0) {
                tvMinOrder.setText("Min. Order: " + currencyFormatter.format(discount.getMinOrderAmount()));
            } else {
                tvMinOrder.setText("No minimum order");
            }

            // Status indicator
            if (discount.isActive()) {
                ivDiscountStatus.setImageResource(R.drawable.ic_check_circle);
                ivDiscountStatus.setColorFilter(context.getResources().getColor(R.color.success_color));
                btnToggleStatus.setText("Deactivate");
                btnToggleStatus.setBackgroundColor(context.getResources().getColor(R.color.warning_color));
            } else {
                ivDiscountStatus.setImageResource(R.drawable.ic_cancel);
                ivDiscountStatus.setColorFilter(context.getResources().getColor(R.color.error_color));
                btnToggleStatus.setText("Activate");
                btnToggleStatus.setBackgroundColor(context.getResources().getColor(R.color.success_color));
            }

            // Check if discount is expired
            long currentTime = System.currentTimeMillis();
            if (discount.getEndDate() < currentTime) {
                itemView.setAlpha(0.6f);
                tvValidityPeriod.setTextColor(context.getResources().getColor(R.color.error_color));
                tvValidityPeriod.setText(tvValidityPeriod.getText() + " (EXPIRED)");
            } else {
                itemView.setAlpha(1.0f);
                tvValidityPeriod.setTextColor(context.getResources().getColor(R.color.text_secondary));
            }
        }
    }
}
