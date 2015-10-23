package com.tak3r07.CourseStatistics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.Toast;

import com.tak3r07.unihelper.R;

import java.util.ArrayList;
import java.util.Calendar;

public class EditCourseActivity extends AppCompatActivity implements CourseNotifiable{

    private final String COURSE_TAG = "COURSE_TAG";
    private final String COURSE_TAG_ID = "COURSE_TAG_ID";

    private Course course;
    private DataHelper<EditCourseActivity> dataHelper;
    private EditText mNameEditText;
    private EditText mNumberEditText;
    private EditText mMaxPointsEditText;
    private EditText mNecPercentToPassEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get intent data
        Intent intent = getIntent();
        int courseId = intent.getExtras().getInt(COURSE_TAG_ID);

        //Get datahelper instance
        dataHelper = new DataHelper<>(this);

        // Restore from storage
        course = dataHelper.getCourse(courseId);

        //Get View-References
        mNameEditText = (EditText) findViewById(R.id.course_editname_edittext);
        mNumberEditText = (EditText) findViewById(R.id.edit_text_numberofassignments);
        mMaxPointsEditText = (EditText) findViewById(R.id.edit_text_maxpointsperassignment);
        mNecPercentToPassEditText = (EditText) findViewById(R.id.edit_text_nec_percent_to_pass);


        //Setup Text
        mNameEditText.setText(course.getCourseName());
        mNumberEditText.setText(String.valueOf(course.getNumberOfAssignments()));
        mNecPercentToPassEditText.setText(String.valueOf(course.getNecPercentToPass()));

        //FPC: Set maxPoints
        //DPC: Remove edittext + description
        if (course.hasFixedPoints()) {
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
        double necPercentToPass = Double.parseDouble(mNecPercentToPassEditText.getText().toString());


        //Count extra-assignments
        int countExtraAssignments = 0;
        for (Assignment assignment : course.getAssignments()) {
            if (assignment.isExtraAssignment()) countExtraAssignments++;
        }

        //Check if new numberOfAssignments Value is still smaller than the existing size
        if (numberOfAssignments >= (course.getAssignments().size() - countExtraAssignments)) {

            //Save new values in course
            course.setCourseName(name);
            course.setNumberOfAssignments(numberOfAssignments);
            course.setNecPercentToPass(necPercentToPass);

            //FPC: Add maxpoints
            if (course.hasFixedPoints()) {
                double maxPoints = Double.parseDouble(mMaxPointsEditText.getText().toString());
                course.toFPC().setMaxPoints(maxPoints);
            }
            //Create empty intent
            Intent data = new Intent();

            //Set new date for update
            long date = Calendar.getInstance().getTimeInMillis();
            course.setDate(date);

            //Update course
            dataHelper.updateCourse(course);

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


    @Override
    public void notifyDataChanged() {
        //Do nothing since this activity does not provide a view
    }

    @Override
    public ArrayList<Course> getCourses() {
        return dataHelper.getAllCourses();
    }
}
