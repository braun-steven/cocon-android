package com.tak3r07.CourseStatistics;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tak3r07.unihelper.R;

import java.util.ArrayList;

/**
 * Created by tak3r07 on 12/8/14.
 * Adapter for RecyclerView in MainActivity
 */
public class RecyclerViewAssignmentAdapter extends RecyclerView.Adapter<RecyclerViewAssignmentAdapter.ViewHolder> {



    private ArrayList<Assignment> mAssignmentsArrayList;
    private Context context;


    RecyclerViewAssignmentAdapter(ArrayList<Assignment> assignments, Context context) {
        this.context = context;
        if (assignments == null) {
            throw new IllegalArgumentException("assignments ArrayList must not be null");
        }

        mAssignmentsArrayList = assignments;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        //Initialize views in Viewholder
        TextView mTextViewTitle;
        TextView mTextViewPoints;
        TextView mTextViewPercentage;
        //Context to refer to app context (for intent, dialog etc)
        Context context;
        //Adapter to notifiy data set changed
        RecyclerViewAssignmentAdapter mAssignmentsAdapter;

        //Holds views
        public ViewHolder(View itemView, Context context, RecyclerViewAssignmentAdapter mAssignmentsAdapter) {

            super(itemView);
            this.context = context;
            this.mAssignmentsAdapter = mAssignmentsAdapter;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mTextViewTitle = (TextView) itemView.findViewById(R.id.assignment_title);
            mTextViewPoints = (TextView) itemView.findViewById(R.id.points_textview);
            mTextViewPercentage = (TextView) itemView.findViewById(R.id.percentage_textview);
        }


        /*
        OnClick: Assignment at the specific position shall be opened
         */
        @Override
        public void onClick(View v) {
            //@TODO: Implement onlcik

        }


        @Override
        public boolean onLongClick(View v) {
            //Open Alert dialog to delete item

            AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

            //Set title and message
            alert.setTitle(context.getString(R.string.delete));
            alert.setMessage(context.getString(R.string.do_you_want_to_delete) + mTextViewTitle.getText().toString() + "?");

            //Set positive button behaviour
            alert.setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    //Delete course and notify
                    Toast.makeText(context, mTextViewTitle.getText().toString() + context.getString(R.string.deleted), Toast.LENGTH_LONG).show();

                    //Remove Course
                    mAssignmentsAdapter.removeAssignment(getPosition());
                }
            });

            alert.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();


            return true;
        }


    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_assignment, parent, false);
        ViewHolder vh = new ViewHolder(itemView, context, this);

        return vh;
    }


    //Sets up the view
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //Store current assignment
        Assignment currentAssignment = mAssignmentsArrayList.get(position);


        //Set text
        holder.mTextViewTitle.setText(context.getString(R.string.assignment_number) + currentAssignment.getIndex());
        holder.mTextViewPoints.setText(currentAssignment.getAchievedPoints() + " / " + currentAssignment.getMaxPoints());

        //Test if Assignment is Extraassignment:
        if (currentAssignment.isExtraAssignment()) {
            holder.mTextViewPercentage.setText("+");
        } else {
            //Else set usual text
            holder.mTextViewPercentage.setText(currentAssignment.getPercentage().toString() + " %");

        }

    }

    @Override
    public int getItemCount() {
        return mAssignmentsArrayList.size();
    }

    public void addAssignment(Assignment assignment){
        mAssignmentsArrayList.add(assignment);
        notifyItemInserted(mAssignmentsArrayList.size());
    }

    public void removeAssignment(int position){
        mAssignmentsArrayList.remove(position);
        notifyItemRemoved(position);
    }

    public ArrayList<Assignment> getAssignments() {
        return mAssignmentsArrayList;
    }

    public void setAssignments(ArrayList<Assignment> mAssignmentsArrayList) {
        this.mAssignmentsArrayList = mAssignmentsArrayList;
    }



}
