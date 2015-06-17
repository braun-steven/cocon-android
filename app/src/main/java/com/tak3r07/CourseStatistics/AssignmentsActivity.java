package com.tak3r07.CourseStatistics;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tak3r07.unihelper.R;

import java.util.ArrayList;


public class AssignmentsActivity extends ActionBarActivity {


    private final String COURSE_TAG_ID = "COURSE_TAG_ID";
    private RecyclerViewAssignmentAdapter mAssignmentAdapter;
    private Course currentCourse;
    private ArrayList<Assignment> mAssignments;
    private int courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        //setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);


        //Get intent and implement received data
        Intent intent = getIntent();

        //position of the course which was opened
        courseId = intent.getExtras().getInt(COURSE_TAG_ID);
        currentCourse = new DatabaseHelper(getApplicationContext())
                .getCourse(courseId);


        //Get assignments
        mAssignments = currentCourse.getAssignments();


        //Set activity title
        this.setTitle(currentCourse.getCourseName());


        //RecyclerView Setup
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_assignments);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mAssignmentAdapter =
                new RecyclerViewAssignmentAdapter(getApplicationContext(), currentCourse, this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAssignmentAdapter);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
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
        final View view = View.inflate(this, R.layout.dialog_add_assignment, null);
        alert.setView(view);

        final EditText mEditTextMaxPoints = (EditText) view.findViewById(R.id.editText_maxPoints);
        final TextView textView = (TextView) view.findViewById(R.id.textView_maxPoints);

        //If this course has fixed Points, dont show the possibilities of setting maxPoints
        if (currentCourse.hasFixedPoints()) {
            ViewManager viewManager = (ViewManager) mEditTextMaxPoints.getParent();
            viewManager.removeView(mEditTextMaxPoints);
            viewManager.removeView(textView);
        }

        alert.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                //First check if there are already too many assignments
                //Count extra-assignments
                int countExtraAssignments = 0;
                for (Assignment aMAssignmentArrayList : mAssignments) {
                    if (aMAssignmentArrayList.isExtraAssignment()) countExtraAssignments++;
                }

                CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.checkBox_extra_assignment);

                if (currentCourse.getNumberOfAssignments() - (mAssignments.size() - countExtraAssignments) > 0 || mCheckBox.isChecked()) {
                    //Get EditText views
                    EditText mEditTextAchievedPoints = (EditText) view.findViewById(R.id.editText_achievedPoints);
                    EditText mEditTextMaxPoints = (EditText) view.findViewById(R.id.editText_maxPoints);

                    //Get data from edittexts
                    String achievedPointsString = mEditTextAchievedPoints.getText().toString().replace(',', '.');
                    String maxPointsString = "";
                    if (!currentCourse.hasFixedPoints()) {
                        maxPointsString = mEditTextMaxPoints.getText().toString().replace(',', '.');
                    }

                    //Check if the entered Values are numeric (doubles) and (or) maxPointsString is empty
                    if (isNumeric(achievedPointsString) && (isNumeric(maxPointsString) || maxPointsString.equals(""))) {

                        Double achievedPoints = Double.parseDouble(achievedPointsString);
                        Double maxPoints;
                        if (currentCourse.hasFixedPoints()) {
                            maxPoints = currentCourse.toFPC().getMaxPoints();
                        } else {

                            maxPoints = Double.parseDouble(maxPointsString);
                        }


                        // Index is lists' last element index + 1 (so another item is added)
                        int index;
                        //First check if list is empty
                        if (mAssignments.isEmpty()) {
                            index = 1;
                        } else {

                            //get List-size
                            int size = mAssignments.size();
                            index = mAssignments.get(size - 1).getIndex() + 1;
                        }


                        int NEW_ASSIGNMENT = -1;


                        //Create new assignment from pulled data
                        Assignment newAssignment = new Assignment(
                                NEW_ASSIGNMENT,
                                index,
                                maxPoints,
                                achievedPoints,
                                currentCourse.getId());

                        //Check if this is an extra assignment
                        if (mCheckBox.isChecked()) {
                            newAssignment.isExtraAssignment(true);
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
        currentCourse.addAssignment(assignment);


        //Store assignment in database
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        dbHelper.addAssignment(assignment);

        //Update list
        mAssignmentAdapter.notifyDataSetChanged();

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
        //Set result and finish
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }

    public void onEditCourseClick(MenuItem item) {
        //Put data into intent
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), EditCourseActivity.class);

        //Add Course-Number
        intent.putExtra(COURSE_TAG_ID, currentCourse.getId());
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {


            // Restore from storage
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            currentCourse = dbHelper.getCourse(courseId);

            //Set new Title

            setTitle(currentCourse.getCourseName());

            //Notify adapter for changes
            mAssignmentAdapter.notifyDataSetChanged();
        }
    }

    //Restore data if resumed
    @Override
    protected void onResume() {
        // Restore from storage
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        currentCourse = dbHelper.getCourse(courseId);
        super.onResume();
    }
}
