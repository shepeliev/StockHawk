package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ChartActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SYMBOL_LOADER = 0;
    private static final DecimalFormat priceFormat = new DecimalFormat("$##0.00");

    private static final IAxisValueFormatter priceAxisFormatter = new IAxisValueFormatter() {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return priceFormat.format(value);
        }
    };

    private static final IValueFormatter priceFormatter = new IValueFormatter() {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return priceFormat.format(value);
        }
    };

    @BindView(R.id.chart)
    LineChart chart;

    private Uri symbolUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        ButterKnife.bind(this);

        setupChart();

        Intent intent = getIntent();
        if (intent != null) {
            symbolUri = intent.getData();
        }

        getSupportLoaderManager().initLoader(SYMBOL_LOADER, null, this);
    }

    private void setupChart() {

        YAxis axisLeft = chart.getAxisLeft();
        YAxis axisRight = chart.getAxisLeft();


        YAxis invisibleYAxis = isLayoutDirectionRtl() ? axisRight : axisLeft;
        invisibleYAxis.setDrawLabels(false);

        YAxis visibleYAxis = isLayoutDirectionRtl() ? axisLeft : axisRight;
        int textColor = getResources().getColor(android.R.color.white);
        visibleYAxis.setTextColor(textColor);
        visibleYAxis.setValueFormatter(priceAxisFormatter);

        chart.getLegend().setTextColor(textColor);
    }

    private boolean isLayoutDirectionRtl() {
        int layoutDirection = TextUtils.getLayoutDirectionFromLocale(
                getResources().getConfiguration().locale);
        return layoutDirection == View.LAYOUT_DIRECTION_LTR;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (symbolUri == null) {
            return null;
        }

        return new CursorLoader(this, symbolUri, Contract.Quote.QUOTE_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            String history = data.getString(Contract.Quote.POSITION_HISTORY);
            fillChart(symbol, history);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Timber.d("Loader reset.");
    }

    private void fillChart(String symbol, String history) {
        if (TextUtils.isEmpty(history)) {
            return;
        }

        int textColor = getResources().getColor(android.R.color.white);
        String label = getString(R.string.chart_description, symbol);

        List<Quote> quotes = parseHistory(history);
        List<Entry> entries = createEntries(quotes);
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setValueTextColor(textColor);
        LineData lineData = new LineData(dataSet);
        lineData.setValueFormatter(priceFormatter);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(textColor);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new XAxisFormatter(quotes));

        Description description = new Description();
        description.setTextColor(textColor);
        description.setText(label);
        chart.setDescription(description);

        chart.invalidate();
    }

    private List<Entry> createEntries(List<Quote> quotes) {
        List<Entry> entries = new ArrayList<>();
        float i = 0;
        for (Quote quote : quotes) {
            entries.add(new Entry(i++, quote.price.floatValue()));
        }

        return entries;
    }

    private List<Quote> parseHistory(String history) {
        List<Quote> quotes = new LinkedList<>();
        for (String historyValue : history.split("\n")) {
            String[] historyParts = historyValue.split(", ");

            Quote quote = new Quote(Long.valueOf(historyParts[0]), new BigDecimal(historyParts[1]));
            if (isLayoutDirectionRtl()) {
                quotes.add(0, quote);
            } else {
                quotes.add(quote);
            }
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

    private class XAxisFormatter implements IAxisValueFormatter {

        private final List<Quote> quotes;

        private XAxisFormatter(List<Quote> quotes) {
            this.quotes = quotes;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long date = quotes.get(((int) value)).date;
            Locale locale = getResources().getConfiguration().locale;
            return DateFormat.getDateInstance(DateFormat.SHORT, locale).format(new Date(date));
        }
    }
}
