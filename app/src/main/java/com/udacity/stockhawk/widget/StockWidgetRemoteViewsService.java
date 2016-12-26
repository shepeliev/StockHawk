package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.StockViewData;

public class StockWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockWidgetRemoteViewsFactory();
    }

    private class StockWidgetRemoteViewsFactory implements RemoteViewsFactory {

        private Cursor data;

        @Override
        public void onCreate() {
            long identityToken = Binder.clearCallingIdentity();
            data = getContentResolver().query(Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS, null, null, Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDataSetChanged() {
            if (data != null) {
                data.close();
            }

            onCreate();
        }

        @Override
        public void onDestroy() {
            if (data != null) {
                data.close();
                data = null;
            }
        }

        @Override
        public int getCount() {
            return data != null ? data.getCount() : 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION
                    || data == null
                    || !data.moveToPosition(position)) {
                return null;
            }

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);

            StockViewData viewData = StockViewData.fromCursor(data);

            views.setTextViewText(R.id.symbol, viewData.getSymbol());
            views.setTextViewText(R.id.price, viewData.getPrice());

            if (viewData.getRawAbsoluteChange() > 0) {
                views.setInt(R.id.change, "setBackgroundResource",
                        R.drawable.percent_change_pill_green);
            } else {
                views.setInt(R.id.change, "setBackgroundResource",
                        R.drawable.percent_change_pill_red);
            }

            if (PrefUtils.getDisplayMode(StockWidgetRemoteViewsService.this)
                    .equals(getString(R.string.pref_display_mode_absolute_key))) {
                views.setTextViewText(R.id.change, viewData.getChange());
            } else {
                views.setTextViewText(R.id.change, viewData.getPercentage());
            }

            Intent fillInIntent = new Intent();
            fillInIntent.setData(Contract.Quote.makeUriForStock(viewData.getSymbol()));
            views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.list_item_quote);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return data.moveToPosition(position)
                    ? data.getLong(Contract.Quote.POSITION_ID) : position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
