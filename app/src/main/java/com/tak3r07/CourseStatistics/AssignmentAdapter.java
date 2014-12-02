package com.tak3r07.CourseStatistics;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tak3r07.unihelper.R;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by tak3r07 on 11/10/14.
 */
public class AssignmentAdapter extends BaseAdapter implements View.OnClickListener {
    private final Context context;


    private final ArrayList<Assignment> assignments;

    private LayoutInflater inflater;
    private ViewHolder holder;


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

    static class ViewHolder {
        TextView mTextViewTitle;
        TextView mTextViewPoints;
        TextView mTextViewPercentage;
    }



    public View getView(int position, View convertView, ViewGroup parent) {
        if (getCount()<=0) return null;

        //If convertview is null -> inflate layout from resource
        if(convertView==null){

            // inflate the layout
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_assignment, parent, false);

            holder = new ViewHolder();
            //Inflate item layout and get views

            holder.mTextViewTitle = (TextView) convertView.findViewById(R.id.assignment_title);
            holder.mTextViewPoints = (TextView) convertView.findViewById(R.id.points_textview);
            holder.mTextViewPercentage = (TextView) convertView.findViewById(R.id.percentage_textview);
            convertView.setTag(holder);
        }else{
            //if convertview is not null -> get holder from tag
            holder = (ViewHolder) convertView.getTag();
        }

        //Store current assignment
        Assignment currentAssignment = assignments.get(position);



        //Set text
        holder.mTextViewTitle.setText(context.getString(R.string.assignment_number) + currentAssignment.getIndex());
        holder.mTextViewPoints.setText(currentAssignment.getAchievedPoints() +" / "+currentAssignment.getMaxPoints());

        //Test if Assignment is Extraassignment:
        if (currentAssignment.isExtraAssignment()){
            holder.mTextViewPercentage.setText("+");
        } else {
            //Else set usual text
            holder.mTextViewPercentage.setText(currentAssignment.getPercentage().toString()+" %");

        }


        return convertView;
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

    public ArrayList<Assignment> getAssignments() {
        return assignments;
    }


    public void setAssignments(ArrayList<Assignment> newAssignmentsArrayList){
        assignments.clear();

        //update assignments array list
        for (Iterator<Assignment> it = newAssignmentsArrayList.iterator(); it.hasNext();){
            assignments.add(it.next());
        }
    }

}
