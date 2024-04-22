package hcmus.android.crm.activities.Sales;

import android.graphics.Color;
import android.os.Bundle;


import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;

import hcmus.android.crm.R;
import hcmus.android.crm.activities.DrawerBaseActivity;
import hcmus.android.crm.databinding.ActivitySalesBinding;

public class SaleActivity extends DrawerBaseActivity {
    private ActivitySalesBinding binding;
    private FirebaseFirestore db;
    private CombinedChart combinedChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Sales");

        binding = ActivitySalesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        showCharts();
    }

    private void showCharts() {

        combinedChart.getDescription().setEnabled(false);
        combinedChart.setDrawGridBackground(false);
        combinedChart.setDrawBarShadow(false);
        combinedChart.setHighlightFullBarEnabled(false);
        Legend l = combinedChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = combinedChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);

        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);

        // Add custom legend entries if needed
        LegendEntry legendEntry = new LegendEntry();
        legendEntry.label = "Offline";
        legendEntry.formColor = Color.parseColor("#8E97A3"); // Color for the form of the legend entry
        LegendEntry secondLegendEntry = new LegendEntry();
        secondLegendEntry.label = "Online";
        secondLegendEntry.formColor = Color.parseColor("#1C2E46");
        LegendEntry[] legendEntries = new LegendEntry[]{legendEntry, secondLegendEntry};
        l.setCustom(legendEntries);

        CombinedData data = new CombinedData();
        data.setData(generateBarData());
        combinedChart.setData(data);
        combinedChart.invalidate();
    }

    private ArrayList<BarEntry> getBar1Enteries(ArrayList<BarEntry> entries) {
        entries.add(new BarEntry(1, 25));
        entries.add(new BarEntry(2, 30));
        entries.add(new BarEntry(3, 38));
        entries.add(new BarEntry(4, 10));
        entries.add(new BarEntry(5, 15));
        return entries;
    }


    private ArrayList<BarEntry> getBar2Enteries(ArrayList<BarEntry> entries) {
        entries.add(new BarEntry(1, 20));
        entries.add(new BarEntry(2, 25));
        entries.add(new BarEntry(3, 33));
        entries.add(new BarEntry(4, 5));
        entries.add(new BarEntry(5, 10));
        return entries;
    }

    private BarData generateBarData() {

        ArrayList<BarEntry> entries1 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> entries2 = new ArrayList<BarEntry>();

        // Add your entries for both sets

        BarDataSet set1 = new BarDataSet(getBar1Enteries(entries1), "Bar 1");
        set1.setColor(Color.parseColor("#8E97A3"));
        set1.setValueTextColor(Color.parseColor("#8E97A3"));
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        BarDataSet set2 = new BarDataSet(getBar2Enteries(entries2), "");
        set2.setStackLabels(new String[]{"Stack 1", "Stack 2"});
        set2.setColors(Color.parseColor("#1C2E46"));
        set2.setValueTextColor(Color.parseColor("#1C2E46"));
        set2.setValueTextSize(10f);
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);

        float groupSpace = 0.3f;
        float barSpace = 0.08f;
        float barWidth = 0.25f;

        BarData d = new BarData(set1, set2);
        d.setBarWidth(barWidth);

        // make this BarData object grouped
        d.groupBars(0, groupSpace, barSpace); // start at x = 0

        return d;
    }
    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_sales);
    }
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
