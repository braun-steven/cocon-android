package com.tak3r07.CourseStatistics.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tak3r07.unihelper.R;

import java.util.ArrayList;

/**
 * Created by tak3r07 on 3/22/15.
 */
public class PathAdapter<String> extends ArrayAdapter<String> {

    private ArrayList<String> backupList;

    public PathAdapter(Context context, ArrayList<String> backupList) {
        super(context, 0, backupList);
        this.backupList = backupList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String backupPath = backupList.get(position);


        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_backupdialog, parent, false);
        }
        // Lookup view for data population
        TextView pathTextView = (TextView) convertView.findViewById(R.id.backuppath_textView);

        // Populate the data into the template view using the data object
        pathTextView.setText((CharSequence) backupPath);
        // Return the completed view to render on screen
        return convertView;

    }
}
