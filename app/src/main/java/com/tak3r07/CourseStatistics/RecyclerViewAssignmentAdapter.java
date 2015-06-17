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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.tak3r07.unihelper.R;

import java.util.ArrayList;

/**
 * Created by tak3r07 on 12/8/14.
 * Adapter for RecyclerView in MainActivity
 */
public class RecyclerViewAssignmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;
    private final AssignmentsActivity assignmentsActivity;
    private static Course currentCourse;
    private ArrayList<Assignment> mAssignments;
    private static boolean hasFixedPoints;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    RecyclerViewAssignmentAdapter(Context context,
                                  Course currentCourse,
                                  AssignmentsActivity assignmentsActivity) {
        this.context = context;
        this.assignmentsActivity = assignmentsActivity;
        RecyclerViewAssignmentAdapter.currentCourse = currentCourse;
        this.mAssignments = currentCourse.getAssignments();
        if (mAssignments == null) {
            throw new IllegalArgumentException("assignments ArrayList must not be null");
        }

        hasFixedPoints = (currentCourse instanceof FixedPointsCourse);
    }

    public static class VHHeader extends RecyclerView.ViewHolder {

        //Refer to TextView objects
        TextView mTextViewAverage;
        TextView mTextViewNecPoiPerAss;
        TextView mTextViewAssUntilFin;
        TextView mTextViewOverall;
        TextView mTextViewProgress;
        GraphView graph;

        public VHHeader(View view) {
            super(view);

            //Refer to TextView objects
            mTextViewAverage = (TextView) view.findViewById(R.id.course_overview_average);
            mTextViewNecPoiPerAss = (TextView) view.findViewById(R.id.course_overview_nec_pointspass);
            mTextViewAssUntilFin = (TextView) view.findViewById(R.id.course_overview_assignments_until_finished);
            mTextViewOverall = (TextView) view.findViewById(R.id.course_overview_overall_percentage_text);
            graph = (GraphView) view.findViewById(R.id.graph);
            if(!currentCourse.hasFixedPoints()){
                mTextViewProgress = (TextView) view.findViewById(R.id.textView_progress);
            }
        }
    }

    public static class VHItem extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

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
        public VHItem(View itemView, Context context, RecyclerViewAssignmentAdapter mAssignmentsAdapter, AssignmentsActivity activity) {

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
                    Assignment currentAssignment = mAssignmentsAdapter.getAssignments().get(getItemPosition(getPosition()));

                    //Delete assignment and notify
                    Toast.makeText(context, mTextViewTitle.getText().toString() + context.getString(R.string.deleted), Toast.LENGTH_LONG).show();

                    //Save changes in Database
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    boolean result = dbHelper.deleteAssignment(currentAssignment);

                    //Remove Assignment
                    mAssignmentsAdapter.removeAssignment(getItemPosition(getPosition()));
                    mAssignmentsAdapter.notifyDataSetChanged();
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
                    final View view = View.inflate(v.getContext(), R.layout.dialog_add_assignment, null);
                    alert.setView(view);

                    //Get assignment reference
                    final Assignment currentAssignment = currentCourse.getAssignment(getItemPosition(getPosition()));

                    final EditText mEditTextMaxPoints = (EditText) view.findViewById(R.id.editText_maxPoints);
                    final TextView textView = (TextView) view.findViewById(R.id.textView_maxPoints);

                    //If this course has fixed Points, dont show the possibilities of setting maxPoints
                    if(hasFixedPoints){
                        mEditTextMaxPoints.setHeight(0);
                        textView.setHeight(0);

                    }

                    //Checkbox reference
                    final CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.checkBox_extra_assignment);
                    mCheckBox.setChecked(currentAssignment.isExtraAssignment());

                    //Get EditText views
                    final EditText mEditTextAchievedPoints = (EditText) view.findViewById(R.id.editText_achievedPoints);

                    //Set hints
                    mEditTextAchievedPoints.setHint(currentAssignment.getAchievedPoints() + "");
                    mEditTextMaxPoints.setHint(currentAssignment.getMaxPoints() + "");


                    alert.setPositiveButton(context.getString(R.string.dialog_edit_assignment_save), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {


                            //Get data from edittext
                            String achievedPointsString = mEditTextAchievedPoints.getText().toString().replace(',', '.');
                            String maxPointsString = mEditTextMaxPoints.getText().toString().replace(',', '.');

                            //If no new maxPoints is entered: get the old one
                            if(maxPointsString.isEmpty()){
                                maxPointsString = String.valueOf(currentAssignment.getMaxPoints());
                            }
                            //If no new achievedPoints is entered: get the old one
                            if(achievedPointsString.isEmpty()){
                                achievedPointsString = String.valueOf(currentAssignment.getAchievedPoints());
                            }

                            //Check if the entered Values are numeric (doubles)
                            if (AssignmentsActivity.isNumeric(achievedPointsString) && AssignmentsActivity.isNumeric(maxPointsString)) {

                                Double achievedPoints = Double.parseDouble(achievedPointsString);

                                Double maxPoints;
                                if(hasFixedPoints){
                                    maxPoints = currentCourse.toFPC().getMaxPoints();
                                } else{

                                    maxPoints = Double.parseDouble(maxPointsString);
                                }

                                //Set new achieved points value
                                currentAssignment.setAchievedPoints(achievedPoints);


                                //Set extra assignment if checked
                                if (mCheckBox.isChecked()) {
                                    currentAssignment.isExtraAssignment(true);
                                } else {
                                    currentAssignment.isExtraAssignment(false);
                                    currentAssignment.setMaxPoints(maxPoints);
                                }

                                //Update
                                mAssignmentsAdapter.notifyDataSetChanged();

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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate layout
        View itemView;
        switch (viewType) {
            case TYPE_ITEM:
                itemView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item_assignment, parent, false);

                return new VHItem(itemView, context, this, assignmentsActivity);

            case TYPE_HEADER:
                //Get correct header for dynamic/fixed points
                int layout;
                if(currentCourse.hasFixedPoints()){
                    layout = R.layout.assignments_header_fixed_points;
                } else {
                    layout = R.layout.assignments_header_dynamic_points;
                }
                itemView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(layout, parent, false);

                return new VHHeader(itemView);

        }
        throw new RuntimeException(
                "there is no type that matches the type "
                        + viewType
                        + " + make sure your using types correctly");

    }


    //Sets up the view
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof VHItem) {
            VHItem itemHolder = (VHItem) holder;
            //Store current assignment
            Assignment currentAssignment = mAssignments.get(position - 1); // -1 since first position is header


            //Set text
            itemHolder.mTextViewTitle.setText(context.getString(R.string.assignment_number) + currentAssignment.getIndex());
            itemHolder.mTextViewPoints.setText(currentAssignment.getAchievedPoints() + " / " + currentAssignment.getMaxPoints());

            //Test if Assignment is Extraassignment:
            if (currentAssignment.isExtraAssignment()) {
                itemHolder.mTextViewPercentage.setText("+");
            } else {
                //Else set usual text
                itemHolder.mTextViewPercentage.setText(currentAssignment.getPercentage().toString() + " %");

            }
        } else {
            if (holder instanceof VHHeader) {
                VHHeader headerHolder = (VHHeader) holder;

                //SetupHeader
                if(currentCourse.hasFixedPoints()){
                    setupFixedPointsHeaderHolder(headerHolder);
                } else{
                    setupDynamicPointsHeaderHolder(headerHolder);
                }

            }
        }


    }

    @Override
    public int getItemCount() {
        return mAssignments.size()+1;
    }

    public void addAssignment(Assignment assignment) {
        mAssignments.add(assignment);
        notifyItemInserted(mAssignments.size());
    }

    public void removeAssignment(int position) {
        mAssignments.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public ArrayList<Assignment> getAssignments() {
        return mAssignments;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


    /**
     * Returns correct item position since first item is header
     * @return item positon
     */
    public static int getItemPosition(int position){
        return position -1;
    }

    public void setupFixedPointsHeaderHolder(VHHeader headerHolder){

        FixedPointsCourse fpc = currentCourse.toFPC();


        headerHolder.mTextViewOverall.setText(fpc.getProgress().toString()
                + " % - " + fpc.getTotalPoints()
                + "/"
                + fpc.getNumberOfAssignments() * fpc.getMaxPoints());

        //Average
        headerHolder.mTextViewAverage.setText(fpc.getAverage(true).toString()
                + " % - " + fpc.getAveragePointsPerAssignment(true)
                + "/" + fpc.getMaxPoints());

        //Nedded Points per assignment until 50% is reached
        headerHolder.mTextViewNecPoiPerAss.setText(fpc.getNecessaryPointsPerAssignmentUntilFin().toString());

        //Number of assignments until 50% is reached
        headerHolder.mTextViewAssUntilFin.setText(String.valueOf(fpc.getNumberOfAssUntilFin()));

        //Graph

        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        for (Assignment currentAssignment : mAssignments) {
            //exclude extra assignments
            if (!currentAssignment.isExtraAssignment()) {
                dataPoints.add(new DataPoint(currentAssignment.getIndex(), currentAssignment.getAchievedPoints()));
            }
        }


        //Count extra-assignments
        int countExtraAssignments = getCountExtraAssignments();


        DataPoint[] points = getDataPoints(dataPoints);

        //if only 1 assignment has been added, hide the graph
        if (hideGraphIfTooLessAssignments(headerHolder)) return;

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>(points);
        series.setSize(8);

        //Setup Graph
        headerHolder.graph.removeAllSeries();
        headerHolder.graph.addSeries(series);
        headerHolder.graph.getViewport().setYAxisBoundsManual(true);
        headerHolder.graph.getViewport().setMinY(0);
        headerHolder.graph.getViewport().setMaxX(mAssignments.size() - countExtraAssignments);
        headerHolder.graph.getViewport().setMaxY(fpc.getMaxPoints());
        headerHolder.graph.getGridLabelRenderer().setNumHorizontalLabels(mAssignments.size() - countExtraAssignments);
    }
    public void setupDynamicPointsHeaderHolder(VHHeader headerHolder){
        headerHolder.mTextViewAverage.setText(currentCourse.getAverage(true) + " %");
        headerHolder.mTextViewProgress.setText(currentCourse.getProgress() + " %");


        //Graph

        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        for (Assignment currentAssignment : mAssignments) {
            //exclude extra assignments
            if (!currentAssignment.isExtraAssignment()) {
                dataPoints.add(new DataPoint(currentAssignment.getIndex(), currentAssignment.getPercentage()));
            }
        }
        int countExtraAssignments = getCountExtraAssignments();


        DataPoint[] points = getDataPoints(dataPoints);

        //if only 1 assignment has been added, hide the graph
        if (hideGraphIfTooLessAssignments(headerHolder)) return;

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>(points);
        series.setSize(8);

        //Setup Graph
        headerHolder.graph.removeAllSeries();
        headerHolder.graph.addSeries(series);
        headerHolder.graph.getViewport().setYAxisBoundsManual(true);
        headerHolder.graph.getViewport().setMinY(0);
        headerHolder.graph.getViewport().setMaxX(mAssignments.size() - countExtraAssignments);
        //Max is 100%
        headerHolder.graph.getViewport().setMaxY(100);
        headerHolder.graph.getGridLabelRenderer().setNumHorizontalLabels(mAssignments.size() - countExtraAssignments);

    }

    public boolean hideGraphIfTooLessAssignments(VHHeader headerHolder) {
        if (mAssignments.size() < 2) {
            //hide
            headerHolder.graph.setVisibility(View.INVISIBLE);
            return true;
        } else {
            headerHolder.graph.setVisibility(View.VISIBLE);
        }
        return false;
    }

    public DataPoint[] getDataPoints(ArrayList<DataPoint> dataPoints) {
        DataPoint[] points = new DataPoint[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            points[i] = dataPoints.get(i);
        }
        return points;
    }

    public int getCountExtraAssignments() {
        //Count extra-assignments
        int countExtraAssignments = 0;
        for (Assignment assignment : mAssignments) {
            if (assignment.isExtraAssignment()) countExtraAssignments++;
        }
        return countExtraAssignments;
    }

}
