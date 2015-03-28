package com.unetresgrossebite.myartgallery;

/**
 * Created by syn on 3/27/15.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends ArrayAdapter<String> {
    private final Context context;
    private List<String> values = new ArrayList<String>();
    private List<String> dnames = new ArrayList<String>();
    private List<String> ids = new ArrayList<String>();

    public SearchAdapter(Context context, String[] values, String[] dnames, String[] ids) {
        super(context, R.layout.activity_search_item, values);
        this.context = context;

        for (int pos = 0; values.length > pos; pos++) {
            this.values.add(values[pos]);
            this.dnames.add(dnames[pos]);
            this.ids.add(ids[pos]);
        }
    }

    public void addAll(String[] values, String[] dnames, String[] ids) {
        for (int pos = 0; values.length > pos; pos++) {
            this.values.add(values[pos]);
            this.dnames.add(dnames[pos]);
            this.ids.add(ids[pos]);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_search_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.search_item);

        textView.setText(this.values.get(position));

        return rowView;
    }

    public String getDname(int position) { return this.dnames.get(position); }
    public String getNumId(int position) { return this.ids.get(position); }
}