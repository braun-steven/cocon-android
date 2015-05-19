package com.tak3r07.CourseStatistics;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tak3r07.unihelper.R;

import java.util.ArrayList;

/**
 * Created by tak3r07 on 12/8/14.
 * Adapter for RecyclerView in MainActivity
 */
public class RecyclerViewAssignmentAdapter extends RecyclerView.Adapter<RecyclerViewAssignmentAdapter.ViewHolder> {


    private Context context;
    private final AssignmentsActivity assignmentsActivity;
    private Course currentCourse;

    RecyclerViewAssignmentAdapter(Context context,
                                  Course currentCourse,
                                  AssignmentsActivity assignmentsActivity) {
        this.context = context;
        this.assignmentsActivity = assignmentsActivity;
        this.currentCourse = currentCourse;
        if (currentCourse.getAssignments() == null) {
            throw new IllegalArgumentException("assignments ArrayList must not be null");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        //Initialize views in Viewholder
        TextView mTextViewTitle;
        TextView mTextViewPoints;
        TextView mTextViewPercentage;
        //Context to refer to app context (for intent, dialog etc)
        Context context;
        //Adapter to notifiy data set changed
        RecyclerViewAssignmentAdapter mAssignmentsAdapter;

        //Activity to notify Overview for data set change
        AssignmentsActivity assignmentsActivity;

        //Holds views
        public ViewHolder(View itemView, Context context, RecyclerViewAssignmentAdapter mAssignmentsAdapter, AssignmentsActivity activity) {

            super(itemView);
            this.assignmentsActivity = activity;
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
        public boolean onLongClick(final View v) {
            //Open Alert dialog to delete item

            final AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

            //Set title and message
            alert.setTitle(context.getString(R.string.delete));
            alert.setMessage(context.getString(R.string.do_you_want_to_delete) + mTextViewTitle.getText().toString() + "?");

            //Set positive button behaviour
            alert.setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    //Get current assignment
                    Assignment currentAssignment = mAssignmentsAdapter.getAssignments().get(getPosition());

                    //Delete assignment and notify
                    Toast.makeText(context, mTextViewTitle.getText().toString() + context.getString(R.string.deleted), Toast.LENGTH_LONG).show();

                    //LOGGING
                    Log.d("DEBUG", "Going to remove assignment: " +
                            ", Index: " + currentAssignment.getId() +
                            ", Points:" + currentAssignment.getAchievedPoints());

                    //Save changes in Database
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    boolean result = dbHelper.deleteAssignment(currentAssignment);

                    Toast.makeText(context, result ? "Delete successful" : "Delete failed", Toast.LENGTH_SHORT).show();

                    //Remove Assignment
                    mAssignmentsAdapter.removeAssignment(getPosition());


                    //Update Overview tile
                    assignmentsActivity.initOverview();
                }
            });

            alert.setNeutralButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.

                }
            });

            alert.setNegativeButton(context.getString(R.string.edit_assignment), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

                    alert.setTitle(context.getString(R.string.dialog_edit_assignment_title));
                    alert.setMessage(context.getString(R.string.enter_assignment_points));

                    // Set an custom dialog view to get user input
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View view = View.inflate(v.getContext(), R.layout.dialog_add_assignment, null);
                    alert.setView(view);

                    //Get assignment reference
                    final Assignment currentAssignment = mAssignmentsAdapter.currentCourse.getAssignment(getPosition());

                    //Checkbox reference
                    final CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.checkBox_extra_assignment);
                    mCheckBox.setChecked(currentAssignment.isExtraAssignment());

                    //Get EditText views
                    final EditText mEditTextAchievedPoints = (EditText) view.findViewById(R.id.editText_achievedPoints);
                    mEditTextAchievedPoints.setHint(currentAssignment.getAchievedPoints() + "");

                    alert.setPositiveButton(context.getString(R.string.dialog_edit_assignment_save), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {


                            //Get data from edittext
                            String achievedPointsString = mEditTextAchievedPoints.getText().toString().replace(',', '.');

                            //Check if the entered Values are numeric (doubles)
                            if (AssignmentsActivity.isNumeric(achievedPointsString)) {

                                Double achievedPoints = Double.parseDouble(achievedPointsString);

                                //Set new achieved points value
                                currentAssignment.setAchievedPoints(achievedPoints);


                                //Set extra assignment if checked
                                if (mCheckBox.isChecked()) {
                                    currentAssignment.setExtraAssignment(true);
                                } else {
                                    currentAssignment.setExtraAssignment(false, mAssignmentsAdapter.currentCourse.getReachablePointsPerAssignment());
                                }

                                //Update Overview tile
                                assignmentsActivity.initOverview();

                                //Update Listitem
                                //Set text
                                mTextViewTitle.setText(context.getString(R.string.assignment_number) + currentAssignment.getIndex());
                                mTextViewPoints.setText(currentAssignment.getAchievedPoints() + " / " + currentAssignment.getMaxPoints());

                                //Test if Assignment is Extraassignment:
                                if (currentAssignment.isExtraAssignment()) {
                                    mTextViewPercentage.setText("+");
                                } else {
                                    //Else set usual text
                                    mTextViewPercentage.setText(currentAssignment.getPercentage().toString() + " %");

                                }

                                DatabaseHelper dbHelper = new DatabaseHelper(context);
                                dbHelper.updateAssignment(currentAssignment);
                                Log.d("DATABASE", "Assignment " + currentAssignment.getIndex() + " updated");


                            } else {
                                //If data was not numeric
                                Toast.makeText(context, context.getString(R.string.invalid_values), Toast.LENGTH_LONG).show();

                            }
                        }
                    });

                    alert.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    alert.show();
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

        return new ViewHolder(itemView, context, this, assignmentsActivity);
    }


    //Sets up the view
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //Store current assignment
        Assignment currentAssignment = currentCourse.getAssignments().get(position);


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
        return currentCourse.getAssignments().size();
    }

    public void addAssignment(Assignment assignment) {
        currentCourse.getAssignments().add(assignment);
        notifyItemInserted(currentCourse.getAssignments().size());
    }

    public void removeAssignment(int position) {
        currentCourse.getAssignments().remove(position);
        notifyItemRemoved(position);
    }

    public ArrayList<Assignment> getAssignments() {
        return currentCourse.getAssignments();
    }

}
