package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChartActivity extends AppCompatActivity {

    @BindView(R.id.chart)
    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        ButterKnife.bind(this);
        List<Quote> quotes = generateFakeQuotes(10);
        List<Entry> entries = new ArrayList<>();
        for (Quote quote : quotes) {
            entries.add(new Entry(((float) quote.date), quote.price.floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "AAPL");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    private List<Quote> generateFakeQuotes(int count) {
        List<Quote> quotes = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -count);
        for (int i = 0; i < count; i++) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
            Quote quote = new Quote(calendar.getTimeInMillis(), new BigDecimal(10));
            quotes.add(quote);
        }

        return quotes;
    }

    private static class Quote {
        private final long date;
        private final BigDecimal price;

        private Quote(long date, BigDecimal price) {
            this.date = date;
            this.price = price;
        }
    }
}
