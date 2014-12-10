package com.tak3r07.CourseStatistics;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tak3r07.unihelper.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Iterator;


public class MainActivity extends ActionBarActivity {

    private final String COURSE_TAG = "COURSE_TAG";
    private final String COURSE_TAG_POSITION = "COURSE_TAG_POSITION";
    private final String ARRAYLIST_SIZE = "ARRAYLIST_SIZE";
    private final String COURSE_ARRAY_LIST = "COURSE_ARRAY_LIST";


    private ListView mListView;


    private ArrayList<Course> mCourseArrayList = new ArrayList<Course>();
    private CourseAdapter mCourseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {

            //Get back Course-Arraylist from savedInstanceState
            mCourseArrayList = (ArrayList<Course>) savedInstanceState.getSerializable(COURSE_ARRAY_LIST);

        }

        /* LISTVIEW SETUP NOW UNCOMMENTED FOR RECYCLERVIEW
        //ListView setup
        mListView = (ListView) findViewById(R.id.listView_courses);

        //Add adapter to listview
        mCourseAdapter = new CourseAdapter(this, mCourseArrayList);
        mListView.setAdapter(mCourseAdapter);

        //Set onClick and onLongClick
        setClickListener();
        */

        //RecyclerView Setup
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_courses);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        RecyclerViewCourseAdapter mCourseAdapter = new RecyclerViewCourseAdapter(mCourseArrayList, getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mCourseAdapter);


        //If activity is freshly started (SavedInstanceState == null): restore data from storage
        if (savedInstanceState == null) {
            mCourseArrayList = CourseDataHandler.restore(getApplicationContext(), mCourseArrayList);
            mCourseAdapter.notifyDataSetChanged();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /* Uncomment to add Settings
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    public void onClickAddCourse(MenuItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.new_course));
        alert.setMessage(getString(R.string.enter_course_name_and_max_p_p_a));

        //Set dialog_add_course layout
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = View.inflate(this, R.layout.dialog_add_course, null);
        alert.setView(view);

        //Add Course Operation
        alert.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Get views
                EditText mCourseNameEditText = (EditText) view.findViewById(R.id.course_name_edittext);
                EditText mReachablePointsEditText = (EditText) view.findViewById(R.id.max_reachable_points_edittext);

                //Get values
                String courseName = mCourseNameEditText.getText().toString();
                String reachablePointsString = mReachablePointsEditText.getText().toString().replace(',', '.');

                //Convert
                if (AssignmentsActivity.isNumeric(reachablePointsString)) {
                    Double reachablePoints = Double.parseDouble(reachablePointsString);

                    addCourse(courseName, reachablePoints);

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

    //Adds a new course to the mCourseArrayList
    public void addCourse(String title, Double reachablePointsPerAssignment) {
        //Check if string is empty
        if (!title.isEmpty()) {

            //boolean to check if coursename already exists
            boolean courseExists = false;

            //Loop to check all coursenames
            for (int i = 0; i < mCourseArrayList.size(); i++) {
                if (title.replace(" ", "").equals(mCourseArrayList.get(i).getCourseName().replace(" ", "")))
                    courseExists = true;
            }

            //Only create new course if "courseexists" is false
            if (courseExists) {
                //Notify user
                Toast.makeText(getApplicationContext(), getString(R.string.a_course_same_name), Toast.LENGTH_LONG).show();
            } else {
                //Add course
                Course course = new Course(title);
                course.setReachablePointsPerAssignment(reachablePointsPerAssignment);
                mCourseAdapter.addCourse(course);

                //save in data
                CourseDataHandler.save(getApplicationContext(), mCourseArrayList);

                Toast.makeText(getApplicationContext(), getString(R.string.course) + title + getString(R.string.has_been_added), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Notify about empty string
            Toast.makeText(getApplicationContext(), getString(R.string.abort_no_title), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        // Save array list
        savedInstanceState.putSerializable(COURSE_ARRAY_LIST, mCourseArrayList);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {


            //Update courselist from database
            mCourseArrayList = CourseDataHandler.restore(getApplicationContext(), mCourseArrayList);

            //Update adapter data
            mCourseAdapter.notifyDataSetChanged();


            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        CourseDataHandler.save(getApplicationContext(), mCourseArrayList);
        super.onDestroy();

    }


    public void setClickListener() {
        //Listview onlclick and open Assignments-Activity

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Create new Intent
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), AssignmentsActivity.class);

                //add  course
                intent.putExtra(COURSE_TAG, mCourseArrayList.get(position));

                //add course position to update assignments when result comes back
                intent.putExtra(COURSE_TAG_POSITION, position);

                startActivityForResult(intent, 0);

            }
        });


        //On LONG click:
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int position, long id) {
                //Open Alert dialog to delete item

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                //Set title and message
                alert.setTitle(getString(R.string.delete));
                alert.setMessage(getString(R.string.do_you_want_to_delete) + mCourseArrayList.get(position).getCourseName() + "?");

                alert.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Delete course and notify
                        Toast.makeText(getApplicationContext(), mCourseArrayList.get(position).getCourseName() + getString(R.string.deleted), Toast.LENGTH_LONG).show();
                        mCourseArrayList.remove(position);

                        //Update list
                        mCourseAdapter.notifyDataSetChanged();
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

    public void onClickMenuRestore(MenuItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.restore));
        alert.setMessage(getString(R.string.do_you_want_restore));


        alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //check if external storage is readable
                if (isExternalStorageReadable()) {
                    File myFilesDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CourseStatistics/files");
                    try {
                        FileInputStream fis = new FileInputStream(myFilesDir.getPath() + "/data.backup");
                        ObjectInputStream ois = new ObjectInputStream(fis);

                        //New Arraylist
                        ArrayList<Course> newArraylist = (ArrayList<Course>) ois.readObject();

                        //clear current arraylist to avoid double input
                        mCourseArrayList.clear();

                        //add each stored course item
                        for (Iterator<Course> it = newArraylist.iterator(); it.hasNext(); ) {
                            mCourseArrayList.add(it.next());
                        }
                        ois.close();

                        //Notify course adapter
                        mCourseAdapter.notifyDataSetChanged();

                        //Notify user about completed restore
                        Toast.makeText(getApplicationContext(), getString(R.string.restore_complete), Toast.LENGTH_LONG).show();
                        CourseDataHandler.save(getApplicationContext(), mCourseArrayList);
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
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

    //Restore Button in Menu
    public void onClickMenuBackup(MenuItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.backup));
        alert.setMessage(getString(R.string.backup_to_storage));


        alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                //Check for RW permissions
                if (isExternalStorageWritable()) {
                    File myFilesDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CourseStatistics/files");
                    myFilesDir.mkdirs();

                    //Write Course-Array-List to storage
                    try {
                        FileOutputStream fos = new FileOutputStream(myFilesDir.getPath() + "/data.backup");
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(mCourseArrayList);
                        oos.close();

                        //Notify user about completed backup
                        Toast.makeText(getApplicationContext(), getString(R.string.backup_complete), Toast.LENGTH_LONG).show();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
