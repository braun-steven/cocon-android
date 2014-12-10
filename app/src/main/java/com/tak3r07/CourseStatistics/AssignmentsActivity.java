package com.tak3r07.CourseStatistics;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tak3r07.unihelper.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Iterator;


public class AssignmentsActivity extends ActionBarActivity {


    private final String COURSE_TAG = "COURSE_TAG";
    private final String COURSE_TAG_POSITION = "COURSE_TAG_POSITION";
    private final String COURSE_ARRAY_LIST = "COURSE_ARRAY_LIST";


    private AssignmentAdapter mAssignmentAdapter;
    private ArrayList<Assignment> mAssignmentArrayList;
    private ListView mListView;
    private int coursePositionInArray;
    private Course course;
    private ArrayList<Course> mCourseArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

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


        //ListView setup
        mListView = (ListView) findViewById(R.id.listView_assignments);

        setClickListener();

        //Create and add listview adapter
        mAssignmentAdapter = new AssignmentAdapter(this, mAssignmentArrayList);
        mListView.setAdapter(mAssignmentAdapter);

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

    public void setClickListener() {
        //Set click listener for Listview

        //On LONG click:
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int position, long id) {
                //Open Alert dialog to delete item

                AlertDialog.Builder alert = new AlertDialog.Builder(AssignmentsActivity.this);

                //Set title and message
                alert.setTitle(getString(R.string.delete));
                alert.setMessage(getString(R.string.want_delete_assignment) + mAssignmentArrayList.get(position).getIndex() + "?");

                alert.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Delete course and notify
                        Toast.makeText(getApplicationContext(), mAssignmentArrayList.get(position).getIndex() + getString(R.string.deleted), Toast.LENGTH_LONG).show();
                        mAssignmentArrayList.remove(position);

                        //Update list
                        mAssignmentAdapter.notifyDataSetChanged();
                    }
                });

                alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();


                return true;
            }
        });

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
        }
    }


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
        mTextViewAverage.setText(course.getOverAllPercentage().toString() + " %"); //Warning: "getOverAll = average in course classe"
        mTextViewOverall.setText(course.getEndPercentage().toString() + " %");
        mTextViewNecPoiPerAss.setText(course.getNecessaryPointsPerAssignmentUntilFin().toString());
        mTextViewAssUntilFin.setText(String.valueOf(course.getNumberOfAssUntilFin()));
    }
}
