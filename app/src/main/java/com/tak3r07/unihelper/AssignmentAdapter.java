package com.tak3r07.unihelper;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by tak3r07 on 11/10/14.
 */
public class AssignmentAdapter extends BaseAdapter implements View.OnClickListener {
    private final Context context;
    private final ArrayList<Assignment> assignments;

    private LayoutInflater inflater;


    public AssignmentAdapter(Context context, ArrayList<Assignment> assignments) {

        this.context = context;
        this.assignments = assignments;
        //Get layoutinflater
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (assignments.size() <= 0) return 0;
        return assignments.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }



    public View getView(int position, View convertView, ViewGroup parent) {
        if (getCount()<=0) return null;

        //Store current assignment
        Assignment currentAssignment = assignments.get(position);

        //Inflate item layout and get views
        View rowView = inflater.inflate(R.layout.list_item_assignment, parent, false);
        TextView mTextViewTitle = (TextView) rowView.findViewById(R.id.assignment_title);
        TextView mTextViewPoints = (TextView) rowView.findViewById(R.id.points_textview);
        TextView mTextViewPercentage = (TextView) rowView.findViewById(R.id.percentage_textview);

        //Set text
        mTextViewTitle.setText("Assignment Nr. " + currentAssignment.getIndex());
        mTextViewPoints.setText(currentAssignment.getAchievedPoints() +"/"+currentAssignment.getMaxPoints());
        mTextViewPercentage.setText(currentAssignment.getPercentage().toString()+" %");


        return rowView;
    }


    @Override
    public void onClick(View v) {


    }

    public void addAssignment(Assignment assignment){
        if (assignment!=null){
            assignments.add(assignment);
            this.notifyDataSetChanged();
        }
    }


}
