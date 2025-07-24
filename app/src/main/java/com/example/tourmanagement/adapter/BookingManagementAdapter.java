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
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Booking;
import com.example.tourmanagement.model.Tour;
import com.example.tourmanagement.model.User;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter for admin booking management.
 * Displays bookings with admin action buttons for approval/rejection.
 *
 * Features:
 * - Display booking information with customer and tour details
 * - Admin action buttons (Approve, Reject, View Details)
 * - Status-based color coding and button visibility
 * - Real-time booking status updates
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-24
 */
public class BookingManagementAdapter extends RecyclerView.Adapter<BookingManagementAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> bookings;
    private OnBookingActionListener listener;
    private TourManagementDatabase database;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;

    /**
     * Interface for handling admin booking actions
     */
    public interface OnBookingActionListener {
        void onApproveBooking(Booking booking);
        void onRejectBooking(Booking booking);
        void onViewBookingDetails(Booking booking);
    }

    public BookingManagementAdapter(Context context, OnBookingActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.bookings = new ArrayList<>();
        this.database = TourManagementDatabase.getDatabase(context);
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        this.dateFormatter = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_management, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    /**
     * Updates the booking list and refreshes the RecyclerView
     */
    public void updateBookings(List<Booking> newBookings) {
        this.bookings.clear();
        this.bookings.addAll(newBookings);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for booking management items
     */
    public class BookingViewHolder extends RecyclerView.ViewHolder {

        private TextView tvBookingReference, tvCustomerName, tvTourName;
        private TextView tvBookingDate, tvTotalAmount, tvNumberOfPeople;
        private TextView tvBookingStatus, tvPaymentStatus, tvCustomerEmail;
        private Button btnApprove, btnReject, btnViewDetails;
        private View statusIndicator;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize UI components
            tvBookingReference = itemView.findViewById(R.id.tv_booking_reference);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvCustomerEmail = itemView.findViewById(R.id.tv_customer_email);
            tvTourName = itemView.findViewById(R.id.tv_tour_name);
            tvBookingDate = itemView.findViewById(R.id.tv_booking_date);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvNumberOfPeople = itemView.findViewById(R.id.tv_number_of_people);
            tvBookingStatus = itemView.findViewById(R.id.tv_booking_status);
            tvPaymentStatus = itemView.findViewById(R.id.tv_payment_status);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            statusIndicator = itemView.findViewById(R.id.status_indicator);

            // Set up click listeners
            btnApprove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onApproveBooking(bookings.get(position));
                }
            });

            btnReject.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRejectBooking(bookings.get(position));
                }
            });

            btnViewDetails.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onViewBookingDetails(bookings.get(position));
                }
            });
        }

        /**
         * Binds booking data to the ViewHolder components
         */
        public void bind(Booking booking) {
            // Set booking reference
            tvBookingReference.setText("Ref: " + booking.getBookingReference());

            // Get and display customer information
            User customer = database.userDao().getUserById(booking.getUserId());
            if (customer != null) {
                tvCustomerName.setText(customer.getFullName());
                tvCustomerEmail.setText(customer.getEmail());
            } else {
                tvCustomerName.setText("Unknown Customer");
                tvCustomerEmail.setText("No email");
            }

            // Get and display tour information
            Tour tour = database.tourDao().getTourById(booking.getTourId());
            if (tour != null) {
                tvTourName.setText(tour.getTourName());
            } else {
                tvTourName.setText("Unknown Tour");
            }

            // Set booking details
            Date bookingDate = new Date(booking.getBookingDate());
            tvBookingDate.setText("Booked: " + dateFormatter.format(bookingDate));
            tvTotalAmount.setText(currencyFormatter.format(booking.getTotalAmount()));
            tvNumberOfPeople.setText(booking.getNumberOfPeople() + " people");

            // Set booking status with color
            String bookingStatus = booking.getBookingStatus();
            tvBookingStatus.setText(bookingStatus);
            setStatusColor(tvBookingStatus, bookingStatus);

            // Set payment status
            String paymentStatus = booking.getPaymentStatus();
            tvPaymentStatus.setText(paymentStatus);
            setPaymentStatusColor(tvPaymentStatus, paymentStatus);

            // Configure action buttons based on booking status
            configureActionButtons(booking);

            // Set status indicator color
            setStatusIndicatorColor(bookingStatus);
        }

        /**
         * Sets the color for booking status
         */
        private void setStatusColor(TextView textView, String status) {
            int backgroundColorResId;

            switch (status.toUpperCase()) {
                case "CONFIRMED":
                    backgroundColorResId = R.color.success_color;
                    break;
                case "PENDING":
                    backgroundColorResId = R.color.warning_color;
                    break;
                case "CANCELLED":
                    backgroundColorResId = R.color.error_color;
                    break;
                default:
                    backgroundColorResId = R.color.text_secondary;
                    break;
            }

            textView.setTextColor(context.getResources().getColor(R.color.text_white));

            try {
                android.graphics.drawable.GradientDrawable background =
                        (android.graphics.drawable.GradientDrawable) context.getResources()
                                .getDrawable(R.drawable.status_background).mutate();
                background.setColor(context.getResources().getColor(backgroundColorResId));
                textView.setBackground(background);
            } catch (Exception e) {
                textView.setBackgroundColor(context.getResources().getColor(backgroundColorResId));
            }
        }

        /**
         * Sets the color for payment status
         */
        private void setPaymentStatusColor(TextView textView, String status) {
            int colorResId;
            switch (status.toUpperCase()) {
                case "PAID":
                    colorResId = R.color.success_color;
                    break;
                case "PENDING":
                    colorResId = R.color.warning_color;
                    break;
                case "CANCELLED":
                case "REFUNDED":
                    colorResId = R.color.error_color;
                    break;
                default:
                    colorResId = R.color.text_secondary;
                    break;
            }
            textView.setTextColor(context.getResources().getColor(colorResId));
        }

        /**
         * Sets the status indicator color
         */
        private void setStatusIndicatorColor(String status) {
            int colorResId;
            switch (status.toUpperCase()) {
                case "CONFIRMED":
                    colorResId = R.color.success_color;
                    break;
                case "PENDING":
                    colorResId = R.color.warning_color;
                    break;
                case "CANCELLED":
                    colorResId = R.color.error_color;
                    break;
                default:
                    colorResId = R.color.text_secondary;
                    break;
            }
            statusIndicator.setBackgroundColor(context.getResources().getColor(colorResId));
        }

        /**
         * Configures action buttons based on booking status
         */
        private void configureActionButtons(Booking booking) {
            String bookingStatus = booking.getBookingStatus();

            if ("PENDING".equals(bookingStatus)) {
                // Show approve and reject buttons for pending bookings
                btnApprove.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                btnApprove.setEnabled(true);
                btnReject.setEnabled(true);
                btnApprove.setText("Approve");
                btnReject.setText("Reject");
            } else {
                // Hide action buttons for confirmed/cancelled bookings
                btnApprove.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
            }

            // View details button is always visible
            btnViewDetails.setVisibility(View.VISIBLE);
            btnViewDetails.setEnabled(true);
        }
    }
}
