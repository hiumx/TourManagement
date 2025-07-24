package com.example.tourmanagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tourmanagement.R;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying revenue report details in the RevenueManagementActivity.
 * Shows tour-wise revenue breakdown and financial analytics.
 */
public class RevenueReportAdapter extends RecyclerView.Adapter<RevenueReportAdapter.RevenueViewHolder> {

    private Context context;
    private List<RevenueItem> revenueItems;
    private NumberFormat currencyFormat;

    public static class RevenueItem {
        private String tourName;
        private double revenue;
        private int bookingsCount;

        public RevenueItem(String tourName, double revenue, int bookingsCount) {
            this.tourName = tourName;
            this.revenue = revenue;
            this.bookingsCount = bookingsCount;
        }

        // Getters
        public String getTourName() { return tourName; }
        public double getRevenue() { return revenue; }
        public int getBookingsCount() { return bookingsCount; }
    }

    public RevenueReportAdapter(Context context) {
        this.context = context;
        this.revenueItems = new ArrayList<>();
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @NonNull
    @Override
    public RevenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_revenue_report, parent, false);
        return new RevenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RevenueViewHolder holder, int position) {
        RevenueItem item = revenueItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return revenueItems.size();
    }

    public void updateRevenueItems(List<RevenueItem> newItems) {
        this.revenueItems.clear();
        this.revenueItems.addAll(newItems);
        notifyDataSetChanged();
    }

    class RevenueViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTourName, tvRevenue, tvBookingsCount;

        public RevenueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTourName = itemView.findViewById(R.id.tv_tour_name);
            tvRevenue = itemView.findViewById(R.id.tv_revenue);
            tvBookingsCount = itemView.findViewById(R.id.tv_bookings_count);
        }

        public void bind(RevenueItem item) {
            tvTourName.setText(item.getTourName());
            tvRevenue.setText(currencyFormat.format(item.getRevenue()));
            tvBookingsCount.setText(item.getBookingsCount() + " bookings");
        }
    }
}
