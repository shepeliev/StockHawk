package com.udacity.stockhawk.data;

import android.database.Cursor;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class StockViewData {

    private static final DecimalFormat dollarFormatWithPlus;
    private static final DecimalFormat dollarFormat;
    private static final DecimalFormat percentageFormat;

    static {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    private final String symbol;
    private final String price;
    private final String change;
    private final String percentage;
    private final float rawAbsoluteChange;

    private StockViewData(String symbol,
                           String price,
                           String change,
                           String percentage,
                           float rawAbsoluteChange) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.percentage = percentage;
        this.rawAbsoluteChange = rawAbsoluteChange;
    }
    public static StockViewData fromCursor(Cursor cursor) {
        String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        String price = dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE));
        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        return new StockViewData(symbol, price, change, percentage, rawAbsoluteChange);
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPrice() {
        return price;
    }

    public String getChange() {
        return change;
    }

    public String getPercentage() {
        return percentage;
    }

    public float getRawAbsoluteChange() {
        return rawAbsoluteChange;
    }
}
