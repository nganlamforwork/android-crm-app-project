package hcmus.android.crm.activities.Sales;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivitySalesBinding;
import hcmus.android.crm.models.Opportunity;
import hcmus.android.crm.utilities.Constants;

public class SalesActivity extends DrawerBaseActivity {

    private ActivitySalesBinding binding;
    private LineChart lineChart;
    private PieChart pieChart;
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Sales");
        binding = ActivitySalesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
        db = FirebaseFirestore.getInstance();
        lineChart = binding.lineChart;
        pieChart = binding.pieChart;

        setupLineChart();
        setupPieChart();

        binding.selectDatesButton.setOnClickListener(view -> showDateRangePicker());

        fetchOpportunities();
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return "";
            }
        });

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
    }

    private void setupPieChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleRadius(30f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(true);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(10f);
    }


    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker().build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            long startDate = selection.first;
            long endDate = selection.second;

            startDateCalendar.setTimeInMillis(startDate);
            endDateCalendar.setTimeInMillis(endDate);
            updateSelectedDatesUI();
        });
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void updateSelectedDatesUI() {
        if (endDateCalendar.before(startDateCalendar)) {
            return;
        }

        binding.startDateTextview.setText("Start Date: " + formatDate(startDateCalendar));
        binding.endDateTextview.setText("End Date: " + formatDate(endDateCalendar));

        fetchOpportunities();
    }

    private String formatDate(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Months are zero-based
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return String.format("%02d/%02d/%04d", dayOfMonth, month, year);
    }

    private void fetchOpportunities() {
        Query query = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_COLLECTION_OPPORTUNITIES)
                .orderBy("expectedDate", Query.Direction.ASCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Opportunity> opportunities = new ArrayList<>();
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Opportunity opportunity = document.toObject(Opportunity.class);
                        try {
                            Date expectedDate = inputFormat.parse(opportunity.getExpectedDate());
                            if (expectedDate != null) {
                                String formattedDate = outputFormat.format(expectedDate);
                                opportunity.setExpectedDate(formattedDate);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        opportunities.add(opportunity);
                        Log.d("Opportunity", "Expected Date: " + opportunity.getExpectedDate());
                    }
                    List<Opportunity> opportunitiesInRange = filterOpportunitiesByDate(opportunities, startDateCalendar, endDateCalendar);
                    showOpportunitiesInGraph(opportunitiesInRange);
                    showOpportunitiesInPieChart(opportunitiesInRange);
                }
            }
        });
    }

    private static List<Opportunity> filterOpportunitiesByDate(List<Opportunity> opportunities, Calendar startDate, Calendar endDate) {
        List<Opportunity> filteredOpportunities = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Opportunity opportunity : opportunities) {
            try {
                Date expectedDate = sdf.parse(opportunity.getExpectedDate());
                if (expectedDate != null) {
                    Calendar expectedDateCalendar = Calendar.getInstance();
                    expectedDateCalendar.setTime(expectedDate);

                    if (expectedDateCalendar.compareTo(startDate) >= 0 && expectedDateCalendar.compareTo(endDate) <= 0) {
                        filteredOpportunities.add(opportunity);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return filteredOpportunities;
    }

    private void showOpportunitiesInGraph(List<Opportunity> opportunitiesInRange) {
            lineChart.clear();
            List<Entry> entries = new ArrayList<>();

            for (Opportunity opportunity : opportunitiesInRange) {
                try {
                    Calendar expectedDateCalendar = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date expectedDate;
                    expectedDate = sdf.parse(opportunity.getExpectedDate());
                    assert expectedDate != null;
                    expectedDateCalendar.setTime(expectedDate);

                    float price = opportunity.getPrice().floatValue();
                    if (Objects.equals(opportunity.getStatus(), "Closed Won")) {
                        entries.add(new Entry(expectedDateCalendar.getTimeInMillis(), price));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            LineDataSet dataSet = new LineDataSet(entries, "Opportunity Prices");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setValueTextSize(9f);

            LineData lineData = new LineData(dataSet);

            CustomMarkerView markerView = new CustomMarkerView(this, R.layout.custom_marker_view);
            lineChart.setMarker(markerView);
            lineChart.setData(lineData);
            lineChart.invalidate();
    }

    private void showOpportunitiesInPieChart(List<Opportunity> opportunities) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        int prospectCount = 0;
        int negotiationCount = 0;
        int wonCount = 0;
        int lostCount = 0;

        for (Opportunity opportunity : opportunities) {
            switch (opportunity.getStatus()) {
                case "In Prospect":
                    prospectCount++;
                    break;
                case "Negotiation":
                    negotiationCount++;
                    break;
                case "Closed Won":
                    wonCount++;
                    break;
                case "Closed Lost":
                    lostCount++;
                    break;
            }
        }
        
        if (prospectCount > 0) {
            entries.add(new PieEntry(prospectCount, "In Prospect"));
        }
        if (negotiationCount > 0) {
            entries.add(new PieEntry(negotiationCount, "Negotiation"));
        }
        if (wonCount > 0) {
            entries.add(new PieEntry(wonCount, "Closed Won"));
        }
        if (lostCount > 0) {
            entries.add(new PieEntry(lostCount, "Closed Lost"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f%%", value);
            }
        });

        pieChart.setData(pieData);
        pieChart.setDrawEntryLabels(false);
        pieChart.notifyDataSetChanged();
        pieChart.invalidate();
    }
}
