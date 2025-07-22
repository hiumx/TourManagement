package com.example.tourmanagement.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tourmanagement.R;
import com.example.tourmanagement.model.Tour;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter for displaying tour listings in the dashboard.
 * Handles tour item display, click events, and booking actions.
 *
 * Features:
 * - Tour image loading with Glide
 * - Formatted price and date display
 * - Available slots indication
 * - Click handlers for tour details and booking
 * - Admin delete functionality
 * - Dynamic list updates
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-22
 */
public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    /**
     * Context for accessing resources and starting activities
     */
    private Context context;

    /**
     * List of tours to display
     */
    private List<Tour> tours;

    /**
     * Click listener interface for handling tour interactions
     */
    private OnTourClickListener listener;

    /**
     * Number formatter for currency display
     */
    private NumberFormat currencyFormatter;

    /**
     * Date formatter for tour date display
     */
    private SimpleDateFormat dateFormatter;

    /**
     * Flag to check if current user is admin
     */
    private boolean isAdmin;

    /**
     * Interface for handling tour click events
     */
    public interface OnTourClickListener {
        /**
         * Called when a tour item is clicked
         * @param tour The clicked tour
         */
        void onTourClick(Tour tour);

        /**
         * Called when book tour button is clicked
         * @param tour The tour to book
         */
        void onBookTourClick(Tour tour);

        /**
         * Called when delete tour button is clicked (admin only)
         * @param tour The tour to delete
         */
        void onDeleteTourClick(Tour tour);
    }

    /**
     * Constructor for TourAdapter
     *
     * @param context Application context
     * @param listener Click event listener
     */
    public TourAdapter(Context context, OnTourClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.tours = new ArrayList<>();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        this.dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // Check if the user is admin
        SharedPreferences prefs = context.getSharedPreferences("TourManagementPrefs", Context.MODE_PRIVATE);
        this.isAdmin = prefs.getBoolean("is_admin", false);
    }

    /**
     * Creates new ViewHolder instances
     *
     * @param parent Parent ViewGroup
     * @param viewType View type identifier
     * @return New TourViewHolder instance
     */
    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tour, parent, false);
        return new TourViewHolder(view);
    }

    /**
     * Binds tour data to ViewHolder
     *
     * @param holder ViewHolder to bind data to
     * @param position Position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour tour = tours.get(position);
        holder.bind(tour);
    }

    /**
     * Returns the total number of tours
     *
     * @return Tour count
     */
    @Override
    public int getItemCount() {
        return tours.size();
    }

    /**
     * Updates the tour list and refreshes the RecyclerView
     *
     * @param newTours New list of tours
     */
    public void updateTours(List<Tour> newTours) {

        for (Tour tour : newTours) {
            Log.d("TourAdapter", "Tour: " + tour.getTourName() + ", Image URL: " + tour.getTourImage());
        }

        this.tours.clear();
        this.tours.addAll(newTours);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for tour items
     */
    public class TourViewHolder extends RecyclerView.ViewHolder {

        /**
         * UI components for tour item
         */
        private ImageView ivTourImage;
        private TextView tvTourName, tvTourLocation, tvTourPrice, tvTourDate;
        private TextView tvAvailableSlots, tvDuration;
        private Button btnBookTour, btnDeleteTour;

        /**
         * Constructor for TourViewHolder
         *
         * @param itemView View for this ViewHolder
         */
        public TourViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize UI components
            ivTourImage = itemView.findViewById(R.id.iv_tour_image);
            tvTourName = itemView.findViewById(R.id.tv_tour_name);
            tvTourLocation = itemView.findViewById(R.id.tv_tour_location);
            tvTourPrice = itemView.findViewById(R.id.tv_tour_price);
            tvTourDate = itemView.findViewById(R.id.tv_tour_date);
            tvAvailableSlots = itemView.findViewById(R.id.tv_available_slots);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            btnBookTour = itemView.findViewById(R.id.btn_book_tour);
            btnDeleteTour = itemView.findViewById(R.id.btn_delete_tour);

            // Set up click listeners
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onTourClick(tours.get(position));
                    }
                }
            });

            btnBookTour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onBookTourClick(tours.get(position));
                    }
                }
            });

            btnDeleteTour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onDeleteTourClick(tours.get(position));
                    }
                }
            });
        }

        /**
         * Binds tour data to the ViewHolder components
         *
         * @param tour Tour object to display
         */
        public void bind(Tour tour) {
            // Set tour name
            tvTourName.setText(tour.getTourName());

            // Set tour location
            tvTourLocation.setText(tour.getTourLocation());

            // Set formatted price
            tvTourPrice.setText(currencyFormatter.format(tour.getTourCost()));

            // Set formatted date
            Date tourDate = new Date(tour.getTourTime());
            tvTourDate.setText(dateFormatter.format(tourDate));

            // Set available slots
            int availableSlots = tour.getAvailableSlots();
            tvAvailableSlots.setText(availableSlots + " slots available");

            // Set duration
            tvDuration.setText(tour.getDuration() + " days");

            // Load tour image using Glide with better error handling
            String imageUrl = tour.getTourImage();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                // Log the image URL for debugging
                android.util.Log.d("TourAdapter", "Loading image: " + imageUrl);

                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_tour)
                    .error(R.drawable.placeholder_tour)
                    .centerCrop()
                    .into(ivTourImage);
            } else {
                // Log when no image is available
                android.util.Log.d("TourAdapter", "No image URL for tour: " + tour.getTourName());
                ivTourImage.setImageResource(R.drawable.placeholder_tour);
            }

            // Update booking button state
            if (availableSlots > 0) {
                btnBookTour.setEnabled(true);
                btnBookTour.setText("Book Now");
                btnBookTour.setBackgroundColor(context.getResources().getColor(R.color.primary_color));
            } else {
                btnBookTour.setEnabled(false);
                btnBookTour.setText("Fully Booked");
                btnBookTour.setBackgroundColor(context.getResources().getColor(R.color.disabled_color));
            }

            // Set availability indicator color
            if (availableSlots > 5) {
                tvAvailableSlots.setTextColor(context.getResources().getColor(R.color.success_color));
            } else if (availableSlots > 0) {
                tvAvailableSlots.setTextColor(context.getResources().getColor(R.color.warning_color));
            } else {
                tvAvailableSlots.setTextColor(context.getResources().getColor(R.color.error_color));
            }

            // Show or hide delete button based on admin status
            if (isAdmin) {
                btnDeleteTour.setVisibility(View.VISIBLE);
            } else {
                btnDeleteTour.setVisibility(View.GONE);
            }
        }
    }
}
