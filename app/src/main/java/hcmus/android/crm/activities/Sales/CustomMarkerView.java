package hcmus.android.crm.activities.Sales;

import android.content.Context;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.Locale;
import hcmus.android.crm.R;

public class CustomMarkerView extends MarkerView {
    private final TextView tvContent;
    private final SimpleDateFormat dateFormat;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String formattedDate = dateFormat.format(e.getX());
        tvContent.setText(formattedDate);
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-((float) getWidth() / 2), -getHeight());
    }
}
