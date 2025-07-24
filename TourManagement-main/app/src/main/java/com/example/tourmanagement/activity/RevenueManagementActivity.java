package com.example.tourmanagement.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tourmanagement.R;
import com.example.tourmanagement.adapter.RevenueReportAdapter;
import com.example.tourmanagement.database.TourManagementDatabase;
import com.example.tourmanagement.model.Booking;
import com.example.tourmanagement.model.Tour;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Revenue Management Activity for admin users.
 * Displays revenue analytics and detailed financial reports.
 *
 * Features:
 * - Total revenue overview
 * - Monthly revenue trends
 * - Tour-wise revenue breakdown
 * - Top performing tours
 * - Revenue analytics
 *
 * @author Tour Management Team
 * @version 1.0
 * @since 2025-07-23
 */
public class RevenueManagementActivity extends AppCompatActivity {

    // UI Components
    private TextView tvTotalRevenue, tvMonthlyRevenue, tvTotalBookings, tvAverageBookingValue;
    private RecyclerView recyclerViewRevenueDetails;
    private LineChart lineChartRevenue;
    private PieChart pieChartTours;

    // Data and Database
    private TourManagementDatabase database;
    private ExecutorService executorService;
    private RevenueReportAdapter revenueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue_management);

        initializeViews();
        setupToolbar();
        setupDatabase();
        setupRecyclerView();
        loadRevenueData();
    }

    private void initializeViews() {
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvMonthlyRevenue = findViewById(R.id.tv_monthly_revenue);
        tvTotalBookings = findViewById(R.id.tv_total_bookings);
        tvAverageBookingValue = findViewById(R.id.tv_average_booking_value);
        recyclerViewRevenueDetails = findViewById(R.id.recycler_view_revenue_details);
        lineChartRevenue = findViewById(R.id.line_chart_revenue);
        pieChartTours = findViewById(R.id.pie_chart_tours);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Revenue Management");
            getSupportActionBar().setSubtitle("Financial Analytics");
        }
    }

    private void setupDatabase() {
        database = TourManagementDatabase.getDatabase(this);
        executorService = Executors.newFixedThreadPool(2);
    }

    private void setupRecyclerView() {
        revenueAdapter = new RevenueReportAdapter(this);
        recyclerViewRevenueDetails.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRevenueDetails.setAdapter(revenueAdapter);
    }

    private void loadRevenueData() {
        executorService.execute(() -> {
            // Get total revenue
            Double totalRevenueResult = database.bookingDao().getTotalRevenue();
            final double totalRevenue = totalRevenueResult != null ? totalRevenueResult : 0.0;

            // Get monthly revenue
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long monthStart = calendar.getTimeInMillis();
            final double monthlyRevenue = database.bookingDao().getMonthlyRevenue(monthStart);

            // Get total bookings
            final int totalBookings = database.bookingDao().getTotalBookingsCount();

            // Calculate average booking value
            final double averageBookingValue = totalBookings > 0 ? totalRevenue / totalBookings : 0.0;

            // Get tour-wise revenue data
            List<RevenueReportAdapter.RevenueItem> revenueItems = generateRevenueItems();

            runOnUiThread(() -> {
                updateUI(totalRevenue, monthlyRevenue, totalBookings, averageBookingValue);
                revenueAdapter.updateRevenueItems(revenueItems);
                setupCharts();
                loadChartData();
            });
        });
    }

    private List<RevenueReportAdapter.RevenueItem> generateRevenueItems() {
        List<RevenueReportAdapter.RevenueItem> items = new ArrayList<>();

        // Get all tours and calculate revenue for each
        List<Tour> tours = database.tourDao().getAllTours();
        for (Tour tour : tours) {
            List<Booking> tourBookings = database.bookingDao().getBookingsByTourId(tour.getId());
            double tourRevenue = 0.0;
            int confirmedBookings = 0;

            for (Booking booking : tourBookings) {
                if ("CONFIRMED".equals(booking.getBookingStatus())) {
                    tourRevenue += booking.getTotalAmount();
                    confirmedBookings++;
                }
            }

            if (confirmedBookings > 0) {
                items.add(new RevenueReportAdapter.RevenueItem(
                    tour.getTourName(),
                    tourRevenue,
                    confirmedBookings
                ));
            }
        }

        // Sort by revenue (highest first)
        items.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));

        return items;
    }

    private void updateUI(double totalRevenue, double monthlyRevenue, int totalBookings, double averageBookingValue) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        tvTotalRevenue.setText(currencyFormat.format(totalRevenue));
        tvMonthlyRevenue.setText(currencyFormat.format(monthlyRevenue));
        tvTotalBookings.setText(String.valueOf(totalBookings));
        tvAverageBookingValue.setText(currencyFormat.format(averageBookingValue));
    }

    private void setupCharts() {
        // Line Chart setup
        Description lineDesc = new Description();
        lineDesc.setText("Monthly Revenue Trend");
        lineChartRevenue.setDescription(lineDesc);
        lineChartRevenue.setDrawGridBackground(false);
        lineChartRevenue.setPinchZoom(true);
        lineChartRevenue.setScaleEnabled(true);

        XAxis lineXAxis = lineChartRevenue.getXAxis();
        lineXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineXAxis.setDrawGridLines(false);

        YAxis lineYAxis = lineChartRevenue.getAxisLeft();
        lineYAxis.setDrawGridLines(true);
        lineYAxis.setGranularity(100.0f);
        lineYAxis.setGranularityEnabled(true);

        lineChartRevenue.getAxisRight().setEnabled(false);

        // Pie Chart setup
        Description pieDesc = new Description();
        pieDesc.setText("Tour-wise Revenue Breakdown");
        pieChartTours.setDescription(pieDesc);
        pieChartTours.setDrawHoleEnabled(true);
        pieChartTours.setHoleColor(Color.WHITE);
        pieChartTours.setTransparentCircleColor(Color.WHITE);
        pieChartTours.setTransparentCircleAlpha(110);
        pieChartTours.setRotationEnabled(true);
        pieChartTours.setHighlightPerTapEnabled(true);

        Legend pieLegend = pieChartTours.getLegend();
        pieLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        pieLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        pieLegend.setOrientation(Legend.LegendOrientation.VERTICAL);
        pieLegend.setDrawInside(false);
    }

    private void loadChartData() {
        executorService.execute(() -> {
            // Calculate start date for last 12 months
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -12);
            long startDate = calendar.getTimeInMillis();

            // Prepare data for line chart (monthly revenue trend)
            List<Double> monthlyRevenues = database.bookingDao().getMonthlyRevenueData(startDate);
            List<String> months = new ArrayList<>();
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.US);

            // Generate month labels for the last 12 months
            calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -11); // Start from 11 months ago
            for (int i = 0; i < 12; i++) {
                months.add(monthFormat.format(calendar.getTime()));
                calendar.add(Calendar.MONTH, 1);
            }

            // Ensure we have data for all 12 months (fill with 0 if no data)
            while (monthlyRevenues.size() < 12) {
                monthlyRevenues.add(0.0);
            }

            LineDataSet lineDataSet = new LineDataSet(createLineDataEntries(monthlyRevenues), "Revenue");
            lineDataSet.setColor(ContextCompat.getColor(this, R.color.primary_color));
            lineDataSet.setValueTextColor(Color.BLACK);
            lineDataSet.setLineWidth(3f);
            lineDataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary_color));
            lineDataSet.setCircleRadius(6f);
            lineDataSet.setDrawValues(true);
            lineDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "$" + Math.round(value);
                }
            });

            LineData lineData = new LineData(lineDataSet);
            runOnUiThread(() -> {
                lineChartRevenue.setData(lineData);
                lineChartRevenue.getXAxis().setValueFormatter(new IndexAxisValueFormatter(months));
                lineChartRevenue.invalidate();
            });

            // Prepare data for pie chart (tour-wise revenue breakdown)
            List<RevenueReportAdapter.RevenueItem> revenueItems = generateRevenueItems();
            ArrayList<PieEntry> pieEntries = new ArrayList<>();

            // Only show top 5 tours to avoid cluttered chart
            int maxTours = Math.min(5, revenueItems.size());
            for (int i = 0; i < maxTours; i++) {
                RevenueReportAdapter.RevenueItem item = revenueItems.get(i);
                pieEntries.add(new PieEntry((float) item.getRevenue(), item.getTourName()));
            }

            if (!pieEntries.isEmpty()) {
                PieDataSet pieDataSet = new PieDataSet(pieEntries, "Tours");
                pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                pieDataSet.setValueTextColor(Color.WHITE);
                pieDataSet.setValueTextSize(12f);
                pieDataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return "$" + Math.round(value);
                    }
                });

                PieData pieData = new PieData(pieDataSet);
                runOnUiThread(() -> {
                    pieChartTours.setData(pieData);
                    pieChartTours.invalidate();
                });
            } else {
                // Handle case when no revenue data is available
                runOnUiThread(() -> {
                    pieChartTours.setNoDataText("No revenue data available");
                    pieChartTours.invalidate();
                });
            }
        });
    }

    private List<Entry> createLineDataEntries(List<Double> monthlyRevenues) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < monthlyRevenues.size(); i++) {
            entries.add(new Entry(i, monthlyRevenues.get(i).floatValue()));
        }
        return entries;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
