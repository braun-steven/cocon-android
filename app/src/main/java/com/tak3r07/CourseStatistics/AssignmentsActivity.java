package com.tak3r07.CourseStatistics;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tak3r07.unihelper.R;

import java.util.ArrayList;


public class AssignmentsActivity extends Activity {


    private final String COURSE_TAG = "COURSE_TAG";
    private final String COURSE_TAG_POSITION = "COURSE_TAG_POSITION";
    private final String ARRAYLIST_SIZE = "ARRAYLIST_SIZE";


    private AssignmentAdapter mAssignmentAdapter;
    private ArrayList<Assignment> mAssignmentArrayList;
    private ListView mListView;
    private int coursePositionInArray;
    private Course course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        //Get intent and implement received data
        Intent intent = getIntent();
        course = (Course) intent.getExtras().getSerializable(COURSE_TAG);

        //position of the course which was opened
        coursePositionInArray = intent.getExtras().getInt(COURSE_TAG_POSITION);

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
            case android.R.id.home:
            {
                onBackPressed();
                return true;
            }
        }



        return super.onOptionsItemSelected(item);
    }

    public void onClickAddAssignment(MenuItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("New Assignment");
        alert.setMessage("Enter assignment points");

        // Set an custom dialog view to get user input
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = View.inflate(this, R.layout.dialog_add_assignment, null);
        alert.setView(view);

        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Get EditText views
                EditText mEditTextAchievedPoints = (EditText) view.findViewById(R.id.editText_achievedPoints);
                EditText mEditTextMaxPoints = (EditText) view.findViewById(R.id.editText_maxPoints);

                //Get data from edittext
                String achievedPointsString = mEditTextAchievedPoints.getText().toString().replace(',','.');
                String maxPointsString = mEditTextMaxPoints.getText().toString().replace(',','.');

                //Check if the entered Values are numeric (doubles)
                if (isNumeric(achievedPointsString) && isNumeric(maxPointsString)) {

                    Double achievedPoints = Double.parseDouble(achievedPointsString);
                    Double maxPoints = Double.parseDouble(maxPointsString);


                    // Index is arraylist size + 1 (so another item is added)
                    int index = mAssignmentArrayList.size() + 1;


                    //Create new assignment from pulled data
                    Assignment newAssignment = new Assignment(index, maxPoints, achievedPoints);

                    //add new assignment
                    addAssignment(newAssignment);
                } else {
                    //If data was not numeric
                    Toast.makeText(getApplicationContext(), "Invalid values", Toast.LENGTH_LONG).show();

                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public void addAssignment(Assignment assignment) {
        mAssignmentAdapter.addAssignment(assignment);
        Toast.makeText(getApplicationContext(), "New assignment added", Toast.LENGTH_SHORT).show();
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

    public void onBackPressed(){


        Intent data = new Intent();
        data.putExtra(COURSE_TAG_POSITION,coursePositionInArray);
        data.putExtra(COURSE_TAG, course);
        setResult(RESULT_OK,data);
        finish();
    }

    public void setClickListener(){
        //Set click listener for Listview

        //On LONG click:
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int position, long id) {
                //Open Alert dialog to delete item

                AlertDialog.Builder alert = new AlertDialog.Builder(AssignmentsActivity.this);

                //Set title and message
                alert.setTitle("Delete");
                alert.setMessage("Do you want to delete Assignment Nr." + mAssignmentArrayList.get(position).getIndex()+ "?");

                alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Delete course and notify
                        Toast.makeText(getApplicationContext(),mAssignmentArrayList.get(position).getIndex() + " deleted",Toast.LENGTH_LONG).show();
                        mAssignmentArrayList.remove(position);

                        //Update list
                        mAssignmentAdapter.notifyDataSetChanged();
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();



                return true;
            }
        });

    }
}
