package com.tak3r07.CourseStatistics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

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

public class EditCourseActivity extends Activity {

    private final String COURSE_TAG = "COURSE_TAG";
    private final String COURSE_TAG_POSITION = "COURSE_TAG_POSITION";

    private Course course;
    private ArrayList<Course> mCourseArrayList = new ArrayList<Course>();
    private int coursePositionInArray;
    private EditText mNameEditText;
    private EditText mNumberEditText;
    private EditText mMaxPointsEditText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        //Get intent data
        Intent intent = getIntent();
        coursePositionInArray = intent.getExtras().getInt(COURSE_TAG_POSITION);

        //restore from data
        restore();

        //get course in array
        course = mCourseArrayList.get(coursePositionInArray);

        //Get View-References
        mNameEditText = (EditText) findViewById(R.id.course_editname_edittext);
        mNumberEditText = (EditText) findViewById(R.id.edit_text_numberofassignments);
        mMaxPointsEditText = (EditText) findViewById(R.id.edit_text_maxpointsperassignment);


        //Setup Text
        mNameEditText.setText(course.getCourseName());
        mNumberEditText.setText(String.valueOf(course.getNumberOfAssignments()));
        mMaxPointsEditText.setText(course.getReachablePointsPerAssignment().toString());


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

    public void onClickSave(View view){
        //Save new values in course
        course.setCourseName(mNameEditText.getText().toString());
        course.setNumberOfAssignments(Integer.parseInt(mNumberEditText.getText().toString()));
        course.setReachablePointsPerAssignment(Double.parseDouble(mMaxPointsEditText.getText().toString()));

        //Create empty intent
        Intent data = new Intent();

        //Store data
        save();

        setResult(RESULT_OK, data);
        finish();
    }

    public void onClickCancel(View view){
        setResult(RESULT_CANCELED,null);
        finish();
    }

    public void save() {
        //Store Data into InternalStorage
        try {
            FileOutputStream fos = getApplicationContext().openFileOutput("data", MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mCourseArrayList);
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void restore(){
        //Restore data

        try {
            FileInputStream fis = getApplicationContext().openFileInput("data");
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<Course> newArraylist = (ArrayList<Course>) ois.readObject();

            //clear current arraylist to avoid double input
            if (mCourseArrayList != null) {
                mCourseArrayList.clear();
            } else{
                //if null -> create new
                mCourseArrayList = new ArrayList<Course>();
            }

            //add each stored course item
            for(Iterator<Course> it = newArraylist.iterator();it.hasNext();){
                mCourseArrayList.add(it.next());
            }
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
