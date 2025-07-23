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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter for displaying booking history.
 * Handles booking item display, status indicators, and user actions.
 *
 * Features:
 * - Complete booking information display
 * - Status-based color coding
 * - Cancel booking functionality
 * - Navigate to ticket view
 * - Tour information lookup
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.BookingViewHolder> {

    /**
     * Context for accessing resources
     */
    private Context context;

    /**
     * List of bookings to display
     */
    private List<Booking> bookings;

    /**
     * Click listener interface
     */
    private OnBookingClickListener listener;

    /**
     * Database for tour information lookup
     */
    private TourManagementDatabase database;

    /**
     * Formatters
     */
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;

    /**
     * Interface for handling booking interactions
     */
    public interface OnBookingClickListener {
        /**
         * Called when a booking item is clicked
         * @param booking The clicked booking
         */
        void onBookingClick(Booking booking);

        /**
         * Called when cancel booking button is clicked
         * @param booking The booking to cancel
         */
        void onCancelBooking(Booking booking);
    }

    /**
     * Constructor for BookingHistoryAdapter
     *
     * @param context Application context
     * @param listener Click event listener
     */
    public BookingHistoryAdapter(Context context, OnBookingClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.bookings = new ArrayList<>();
        this.database = TourManagementDatabase.getDatabase(context);
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        this.dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    /**
     * Creates new ViewHolder instances
     *
     * @param parent Parent ViewGroup
     * @param viewType View type identifier
     * @return New BookingViewHolder instance
     */
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_history, parent, false);
        return new BookingViewHolder(view);
    }

    /**
     * Binds booking data to ViewHolder
     *
     * @param holder ViewHolder to bind data to
     * @param position Position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    /**
     * Returns the total number of bookings
     *
     * @return Booking count
     */
    @Override
    public int getItemCount() {
        return bookings.size();
    }

    /**
     * Updates the booking list and refreshes the RecyclerView
     *
     * @param newBookings New list of bookings
     */
    public void updateBookings(List<Booking> newBookings) {
        this.bookings.clear();
        this.bookings.addAll(newBookings);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for booking history items
     */
    public class BookingViewHolder extends RecyclerView.ViewHolder {

        /**
         * UI components for booking item
         */
        private TextView tvBookingReference, tvTourName, tvBookingDate, tvTotalAmount;
        private TextView tvBookingStatus, tvPaymentStatus, tvNumberOfPeople;
        private Button btnViewTicket, btnCancelBooking;

        /**
         * Constructor for BookingViewHolder
         *
         * @param itemView View for this ViewHolder
         */
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize UI components
            tvBookingReference = itemView.findViewById(R.id.tv_booking_reference);
            tvTourName = itemView.findViewById(R.id.tv_tour_name);
            tvBookingDate = itemView.findViewById(R.id.tv_booking_date);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvBookingStatus = itemView.findViewById(R.id.tv_booking_status);
            tvPaymentStatus = itemView.findViewById(R.id.tv_payment_status);
            tvNumberOfPeople = itemView.findViewById(R.id.tv_number_of_people);
            btnViewTicket = itemView.findViewById(R.id.btn_view_ticket);
            btnCancelBooking = itemView.findViewById(R.id.btn_cancel_booking);

            // Set up click listeners
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onBookingClick(bookings.get(position));
                    }
                }
            });

            btnViewTicket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onBookingClick(bookings.get(position));
                    }
                }
            });

            btnCancelBooking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onCancelBooking(bookings.get(position));
                    }
                }
            });
        }

        /**
         * Binds booking data to the ViewHolder components
         *
         * @param booking Booking object to display
         */
        public void bind(Booking booking) {
            // Set booking reference
            tvBookingReference.setText("Ref: " + booking.getBookingReference());

            // Get and display tour information
            Tour tour = database.tourDao().getTourById(booking.getTourId());
            if (tour != null) {
                tvTourName.setText(tour.getTourName());
            } else {
                tvTourName.setText("Tour information unavailable");
            }

            // Set booking date
            Date bookingDate = new Date(booking.getBookingDate());
            tvBookingDate.setText("Booked: " + dateFormatter.format(bookingDate));

            // Set total amount
            tvTotalAmount.setText(currencyFormatter.format(booking.getTotalAmount()));

            // Set number of people
            tvNumberOfPeople.setText(booking.getNumberOfPeople() + " people");

            // Set booking status with color
            String bookingStatus = booking.getBookingStatus();
            tvBookingStatus.setText(bookingStatus);
            setStatusColor(tvBookingStatus, bookingStatus);

            // Set payment status with color
            String paymentStatus = booking.getPaymentStatus();
            tvPaymentStatus.setText(paymentStatus);
            setPaymentStatusColor(tvPaymentStatus, paymentStatus);

            // Configure action buttons based on booking status
            configureActionButtons(booking);
        }

        /**
         * Sets the color for booking status
         *
         * @param textView TextView to set color for
         * @param status Status value
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

            // Always use white text for better contrast against colored backgrounds
            textView.setTextColor(context.getResources().getColor(R.color.text_white));

            // Create a new background drawable with the appropriate color
            try {
                // Get the background drawable and set its color
                android.graphics.drawable.GradientDrawable background =
                        (android.graphics.drawable.GradientDrawable) context.getResources().getDrawable(R.drawable.status_background).mutate();
                background.setColor(context.getResources().getColor(backgroundColorResId));
                textView.setBackground(background);
            } catch (Exception e) {
                // Fallback: just set background color
                textView.setBackgroundColor(context.getResources().getColor(backgroundColorResId));
            }
        }

        /**
         * Sets the color for payment status
         *
         * @param textView TextView to set color for
         * @param status Payment status value
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
         * Configures action buttons based on booking status
         *
         * @param booking Booking object
         */
        private void configureActionButtons(Booking booking) {
            String bookingStatus = booking.getBookingStatus();

            // Configure View Ticket button
            if ("CONFIRMED".equals(bookingStatus)) {
                btnViewTicket.setVisibility(View.VISIBLE);
                btnViewTicket.setEnabled(true);
                btnViewTicket.setText("View Ticket");
            } else {
                btnViewTicket.setVisibility(View.GONE);
            }

            // Configure Cancel button
            if ("PENDING".equals(bookingStatus)) {
                btnCancelBooking.setVisibility(View.VISIBLE);
                btnCancelBooking.setEnabled(true);
                btnCancelBooking.setText("Cancel");
                btnCancelBooking.setBackgroundColor(context.getResources().getColor(R.color.error_color));
            } else if ("CANCELLED".equals(bookingStatus)) {
                btnCancelBooking.setVisibility(View.VISIBLE);
                btnCancelBooking.setEnabled(false);
                btnCancelBooking.setText("Cancelled");
                btnCancelBooking.setBackgroundColor(context.getResources().getColor(R.color.disabled_color));
            } else {
                btnCancelBooking.setVisibility(View.GONE);
            }
        }
    }
}
