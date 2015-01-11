package com.tak3r07.CourseStatistics;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.GraphViewXML;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.tak3r07.unihelper.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AssignmentsActivity extends ActionBarActivity {


    private final String COURSE_TAG = "COURSE_TAG";
    private final String COURSE_TAG_POSITION = "COURSE_TAG_POSITION";
    private final String COURSE_ARRAY_LIST = "COURSE_ARRAY_LIST";


    private RecyclerViewAssignmentAdapter mAssignmentAdapter;
    private ArrayList<Assignment> mAssignmentArrayList;
    private int coursePositionInArray;
    private Course course;
    private ArrayList<Course> mCourseArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        //setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {

            //Get back Course-Arraylist from savedInstanceState
            mCourseArrayList = (ArrayList<Course>) savedInstanceState.getSerializable(COURSE_ARRAY_LIST);

        } else {
            //Restore from data
            mCourseArrayList = CourseDataHandler.restore(getApplicationContext(), mCourseArrayList);
        }


        //Get intent and implement received data
        Intent intent = getIntent();

        //position of the course which was opened
        coursePositionInArray = intent.getExtras().getInt(COURSE_TAG_POSITION);

        //Set current course
        course = mCourseArrayList.get(coursePositionInArray);

        //update assignments to be shown from the intents course
        mAssignmentArrayList = course.getAssignments();


        //Set activity title
        this.setTitle(course.getCourseName());


        //RecyclerView Setup
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_assignments);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mAssignmentAdapter = new RecyclerViewAssignmentAdapter(mAssignmentArrayList, getApplicationContext(), course, this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAssignmentAdapter);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        //Initialize "Overview"-Cardview
        initOverview();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }


        return super.onOptionsItemSelected(item);
    }

    public void onClickAddAssignment(MenuItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.new_assignment));
        alert.setMessage(getString(R.string.enter_assignment_points));

        // Set an custom dialog view to get user input
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = View.inflate(this, R.layout.dialog_add_assignment, null);
        alert.setView(view);

        alert.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //First check if there are already too many assignments
                //Count extra-assignments
                int countExtraAssignments = 0;
                for (Assignment aMAssignmentArrayList : mAssignmentArrayList) {
                    if (aMAssignmentArrayList.isExtraAssignment()) countExtraAssignments++;
                }

                if (course.getNumberOfAssignments() - (mAssignmentArrayList.size() - countExtraAssignments) > 0) {
                    //Get EditText views
                    EditText mEditTextAchievedPoints = (EditText) view.findViewById(R.id.editText_achievedPoints);

                    //Get data from edittext
                    String achievedPointsString = mEditTextAchievedPoints.getText().toString().replace(',', '.');

                    //Check if the entered Values are numeric (doubles)
                    if (isNumeric(achievedPointsString)) {

                        Double achievedPoints = Double.parseDouble(achievedPointsString);


                        // Index is arraylist size + 1 (so another item is added)
                        int index = mAssignmentArrayList.size() + 1;


                        //Create new assignment from pulled data
                        Assignment newAssignment = new Assignment(index, course.getReachablePointsPerAssignment(), achievedPoints);

                        //Check if this is an extra assignment
                        CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.checkBox_extra_assignment);
                        if (mCheckBox.isChecked()) {
                            newAssignment.setExtraAssignment(true);
                        }

                        //add new assignment
                        addAssignment(newAssignment);
                    } else {
                        //If data was not numeric
                        Toast.makeText(getApplicationContext(), getString(R.string.invalid_values), Toast.LENGTH_LONG).show();

                    }
                } else {
                    //already too many assignments
                    Toast.makeText(getApplicationContext(), getString(R.string.reached_assignments_limit), Toast.LENGTH_LONG).show();
                }
            }
        });

        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

    }

    //Add assignment to adapter (Eventually add to course necessary?)
    public void addAssignment(Assignment assignment) {
        mAssignmentAdapter.addAssignment(assignment);

        //Update Overview
        initOverview();

        Toast.makeText(getApplicationContext(), getString(R.string.new_assignment_added), Toast.LENGTH_SHORT).show();
    }


    //Checks if a string is Numeric (Source: http://goo.gl/mGQ3Sp)
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public void onBackPressed() {

        //Save data
        CourseDataHandler.save(getApplicationContext(), mCourseArrayList);

        //Set result and finish
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        // Save array list
        savedInstanceState.putSerializable(COURSE_ARRAY_LIST, mCourseArrayList);


        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onEditCourseClick(MenuItem item) {
        //Put data into intent
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), EditCourseActivity.class);

        //Add Course-Number
        intent.putExtra(COURSE_TAG_POSITION, coursePositionInArray);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {


            //Restore data from saved data
            mCourseArrayList = CourseDataHandler.restore(getApplicationContext(), mCourseArrayList);
            course = mCourseArrayList.get(coursePositionInArray);

            //Set new Title

            setTitle(course.getCourseName());

            //Set new assignments for data update
            mAssignmentAdapter.setAssignments(course.getAssignments());

            //Notify adapter for changes
            mAssignmentAdapter.notifyDataSetChanged();

            //Update overview
            initOverview();
        }
    }

    //Store data on pause
    public void onPause() {
        CourseDataHandler.save(getApplicationContext(), mCourseArrayList);
        super.onPause();
    }

    public void initOverview() {
        //Refer to TextView objects
        TextView mTextViewAverage = (TextView) findViewById(R.id.course_overview_average);
        TextView mTextViewNecPoiPerAss = (TextView) findViewById(R.id.course_overview_nec_pointspass);
        TextView mTextViewAssUntilFin = (TextView) findViewById(R.id.course_overview_assignments_until_finished);
        TextView mTextViewOverall = (TextView) findViewById(R.id.course_overview_overall_percentage_text);

        //Set texts


        //Overall
        mTextViewOverall.setText(course.getAverage().toString() + " % - " + course.getTotalPoints() + "/" + course.getNumberOfAssignments() * course.getReachablePointsPerAssignment());

        //Average
        mTextViewAverage.setText(course.getOverAllPercentage(true).toString() + " % - " + course.getAveragePointsPerAssignment(true) + "/" + course.getReachablePointsPerAssignment()); //Warning: "getOverAll = average in course classe"

        //Nedded Points per assignment until 50% is reached
        mTextViewNecPoiPerAss.setText(course.getNecessaryPointsPerAssignmentUntilFin().toString());

        //Number of assignments until 50% is reached
        mTextViewAssUntilFin.setText(String.valueOf(course.getNumberOfAssUntilFin()));

        //Graph


        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
        for (Assignment currentAssignment : course.getAssignments()) {
            //exclude extra assignments
            if (!currentAssignment.isExtraAssignment()) {
                dataPoints.add(new DataPoint(currentAssignment.getIndex(), currentAssignment.getAchievedPoints()));
            }
        }


        //Count extra-assignments
        int countExtraAssignments = 0;
        for (Assignment assignment : course.getAssignments()) {
            if (assignment.isExtraAssignment()) countExtraAssignments++;
        }


        DataPoint[] points = new DataPoint[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            points[i] = dataPoints.get(i);
        }
        GraphView graph = (GraphView) findViewById(R.id.graph);
        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<DataPoint>(points);
        series.setSize(8);

        //Setup Graph
        graph.addSeries(series);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(course.getAssignments().size() - countExtraAssignments);
        graph.getViewport().setMaxY(course.getReachablePointsPerAssignment());



        graph.getGridLabelRenderer().setNumHorizontalLabels(course.getAssignments().size() - countExtraAssignments);
    }
}
