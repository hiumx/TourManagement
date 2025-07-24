package com.example.tourmanagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tourmanagement.R;
import com.example.tourmanagement.model.Discount;
import com.example.tourmanagement.model.Tour;
import com.example.tourmanagement.database.TourManagementDatabase;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying discount suggestions to users in a beautiful horizontal scrolling layout
 */
public class DiscountSuggestionAdapter extends RecyclerView.Adapter<DiscountSuggestionAdapter.DiscountViewHolder> {

    private Context context;
    private List<Discount> discounts;
    private OnDiscountActionListener listener;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;
    private TourManagementDatabase database;

    public interface OnDiscountActionListener {
        void onUseDiscount(Discount discount);
        void onViewDiscountDetails(Discount discount);
    }

    public DiscountSuggestionAdapter(Context context, OnDiscountActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.discounts = new ArrayList<>();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        this.dateFormatter = new SimpleDateFormat("MMM dd", Locale.getDefault());
        this.database = TourManagementDatabase.getDatabase(context);
    }

    // Constructor without listener for cases where we don't need callbacks
    public DiscountSuggestionAdapter(Context context) {
        this(context, null);
    }

    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_discount_suggestion, parent, false);
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
        private TextView tvDiscountValue, tvDiscountTitle, tvDiscountDescription;
        private TextView tvTourInfo, tvMinOrder, tvExpiresSoon;
        private Button btnUseDiscount;

        public DiscountViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDiscountValue = itemView.findViewById(R.id.tv_discount_value);
            tvDiscountTitle = itemView.findViewById(R.id.tv_discount_title);
            tvDiscountDescription = itemView.findViewById(R.id.tv_discount_description);
            tvTourInfo = itemView.findViewById(R.id.tv_tour_info);
            tvMinOrder = itemView.findViewById(R.id.tv_min_order);
            tvExpiresSoon = itemView.findViewById(R.id.tv_expires_soon);
            btnUseDiscount = itemView.findViewById(R.id.btn_use_discount);

            // Set up click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onViewDiscountDetails(discounts.get(position));
                }
            });

            btnUseDiscount.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUseDiscount(discounts.get(position));
                }
            });
        }

        public void bind(Discount discount) {
            // Set discount value
            if (Discount.DiscountType.PERCENTAGE.equals(discount.getDiscountType())) {
                tvDiscountValue.setText((int)discount.getDiscountValue() + "% OFF");
            } else {
                tvDiscountValue.setText("$" + (int)discount.getDiscountValue() + " OFF");
            }

            // Set discount title and description
            tvDiscountTitle.setText(discount.getDiscountName());
            tvDiscountDescription.setText(discount.getDescription());

            // Set tour information
            if (discount.getTourId() == null) {
                tvTourInfo.setText("All Tours");
            } else {
                // Get tour name for specific tour discount
                new Thread(() -> {
                    try {
                        Tour tour = database.tourDao().getTourById(discount.getTourId());
                        if (tour != null) {
                            ((android.app.Activity) context).runOnUiThread(() ->
                                tvTourInfo.setText(tour.getTourName())
                            );
                        }
                    } catch (Exception e) {
                        ((android.app.Activity) context).runOnUiThread(() ->
                            tvTourInfo.setText("Specific Tour")
                        );
                    }
                }).start();
            }

            // Set minimum order amount
            if (discount.getMinOrderAmount() > 0) {
                tvMinOrder.setText("Min. " + currencyFormatter.format(discount.getMinOrderAmount()));
            } else {
                tvMinOrder.setText("No minimum");
            }

            // Check if discount expires soon (within 7 days)
            long currentTime = System.currentTimeMillis();
            long timeUntilExpiry = discount.getEndDate() - currentTime;
            long daysUntilExpiry = timeUntilExpiry / (24 * 60 * 60 * 1000);

            if (daysUntilExpiry <= 7 && daysUntilExpiry > 0) {
                tvExpiresSoon.setVisibility(View.VISIBLE);
                if (daysUntilExpiry <= 1) {
                    tvExpiresSoon.setText("Expires Today!");
                } else {
                    tvExpiresSoon.setText("Expires in " + daysUntilExpiry + " days");
                }
            } else {
                tvExpiresSoon.setVisibility(View.GONE);
            }
        }
    }
}
