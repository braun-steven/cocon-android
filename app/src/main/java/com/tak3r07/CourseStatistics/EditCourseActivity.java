package com.tak3r07.CourseStatistics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.Toast;

import com.tak3r07.unihelper.R;

import java.util.Iterator;

public class EditCourseActivity extends ActionBarActivity {

    private final String COURSE_TAG = "COURSE_TAG";
    private final String COURSE_TAG_ID = "COURSE_TAG_ID";

    private Course course;
    private EditText mNameEditText;
    private EditText mNumberEditText;
    private EditText mMaxPointsEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        //Get intent data
        Intent intent = getIntent();
        int courseId = intent.getExtras().getInt(COURSE_TAG_ID);

        // Restore from storage
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        course = dbHelper.getCourse(courseId);

        //Get View-References
        mNameEditText = (EditText) findViewById(R.id.course_editname_edittext);
        mNumberEditText = (EditText) findViewById(R.id.edit_text_numberofassignments);
        mMaxPointsEditText = (EditText) findViewById(R.id.edit_text_maxpointsperassignment);


        //Setup Text
        mNameEditText.setText(course.getCourseName());
        mNumberEditText.setText(String.valueOf(course.getNumberOfAssignments()));

        //FPC: Set maxPoints
        //DPC: Remove edittext + description
        if(course.hasFixedPoints()) {
            mMaxPointsEditText.setText(course.toFPC().getMaxPoints().toString());
        } else {

            ViewManager viewManager = (ViewManager) mMaxPointsEditText.getParent();

            //remove edittext
            viewManager.removeView(mMaxPointsEditText);
            //remove description
            viewManager.removeView(findViewById(R.id.textView3));

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_course, menu);
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

    public void onClickSave(View view) {
        String name = mNameEditText.getText().toString();
        int numberOfAssignments = Integer.parseInt(mNumberEditText.getText().toString());



        //Count extra-assignments
        int countExtraAssignments = 0;
        for (Iterator<Assignment> it = course.getAssignments().iterator(); it.hasNext(); ) {
            if (it.next().isExtraAssignment()) countExtraAssignments++;
        }

        //Check if new numberOfAssignments Value is still smaller than the existing size
        if (numberOfAssignments >= (course.getAssignments().size() - countExtraAssignments)) {

            //Save new values in course
            course.setCourseName(name);
            course.setNumberOfAssignments(numberOfAssignments);

            //FPC: Add maxpoints
            if(course.hasFixedPoints()) {
                double maxPoints = Double.parseDouble(mMaxPointsEditText.getText().toString());
                course.toFPC().setMaxPoints(maxPoints);
            }
            //Create empty intent
            Intent data = new Intent();

            //Update course
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            dbHelper.updateCourse(course);

            setResult(RESULT_OK, data);
            finish();
        } else {
            //Notify user about too low numberOfAssignments
            Toast.makeText(getApplicationContext(), getString(R.string.number_assignments_too_low), Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED, null);
        finish();
    }


}
