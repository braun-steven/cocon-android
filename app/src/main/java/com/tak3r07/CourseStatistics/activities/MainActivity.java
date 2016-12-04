package com.tak3r07.CourseStatistics.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bartoszlipinski.recyclerviewheader.RecyclerViewHeader;
import com.tak3r07.CourseStatistics.objects.Course;
import com.tak3r07.CourseStatistics.sync.CourseNotifiable;
import com.tak3r07.CourseStatistics.database.DataHelper;
import com.tak3r07.CourseStatistics.objects.DynamicPointsCourse;
import com.tak3r07.CourseStatistics.objects.FixedPointsCourse;
import com.tak3r07.CourseStatistics.adapter.PathAdapter;
import com.tak3r07.CourseStatistics.adapter.RecyclerViewCourseAdapter;
import com.tak3r07.unihelper.R;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements CourseNotifiable {
    private final String COURSE_ARRAY_LIST = "COURSE_ARRAY_LIST";


    private ArrayList<Course> mCourseArrayList = new ArrayList<Course>();
    private RecyclerViewCourseAdapter mCourseAdapter;
    private FloatingActionButton mFab;
    private DataHelper<MainActivity> dataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /*//Check if Google Playe Services is available
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext())
                == ConnectionResult.SUCCESS) {
*/

        //get datahelper instance
        dataHelper = new DataHelper<MainActivity>(this);

        /*
         *Dont use since onResume()  always refreshes data from server

        if (savedInstanceState != null) {

            //Get back Course-Arraylist from savedInstanceState
            mCourseArrayList = (ArrayList<Course>) savedInstanceState.getSerializable(COURSE_ARRAY_LIST);

        } else {
            // Restore courses
            mCourseArrayList = dataHelper.getAllCourses();
        }
        */


        //RecyclerView Setup
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_courses);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mCourseAdapter = new RecyclerViewCourseAdapter(this, getApplicationContext(), mCourseArrayList);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mCourseAdapter);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);


        //Add header for column descriptions
        RecyclerViewHeader header =
                RecyclerViewHeader.fromXml(getApplicationContext(), R.layout.layout_courselist_header);
        header.attachTo(mRecyclerView);
        mCourseAdapter.notifyDataSetChanged();


        //Setup FAB
        setupFAB();

        /*
        } else {
            //TODO: Show error dialog that google play services is not available
        }*/
    }

    private void setupFAB() {
        mFab = (FloatingActionButton) findViewById(R.id.fab_add_course);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddCourse(null);
            }
        });
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

        /*if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public void onClickAddCourse(MenuItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.new_course));
        alert.setMessage(getString(R.string.enter_course_name_and_max_p_p_a));

        //Set dialog_add_course layout
        final View view = View.inflate(this, R.layout.dialog_add_course, null);
        alert.setView(view);


        //Checkbox
        final CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.checkBox_fixed_points);
        mCheckBox.setChecked(true);
        //Get views
        final EditText mCourseNameEditText =
                (EditText) view.findViewById(R.id.course_name_edittext);
        final EditText mReachablePointsEditText =
                (EditText) view.findViewById(R.id.max_reachable_points_edittext);

        //Toggle Edittext on checkbox toggle
        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = mCheckBox.isChecked();
                //Toggle Eddittext
                mReachablePointsEditText.setEnabled(isChecked);
            }
        });

        //Add Course Operation
        alert.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


                //Get values
                String courseName = mCourseNameEditText.getText().toString();
                String reachablePointsString =
                        mReachablePointsEditText.getText().toString().replace(',', '.');
                Boolean hasFixedPoints = mCheckBox.isChecked();

                if (hasFixedPoints) {
                    //Convert
                    if (AssignmentsActivity.isNumeric(reachablePointsString)) {
                        Double reachablePoints = Double.parseDouble(reachablePointsString);

                        addCourse(courseName, reachablePoints, hasFixedPoints);


                    } else {
                        //If data was not numeric
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.invalid_values),
                                Toast.LENGTH_LONG).show();

                    }
                } else {
                    addCourse(courseName, 0d, hasFixedPoints);
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
    public void addCourse(String title, Double maxPoints, boolean hasFixedPoints) {
        //Check if string is empty
        if (!title.isEmpty()) {

            //boolean to check if coursename already exists
            boolean courseExists = false;

            //Loop to check all coursenames
            for (Course course : mCourseArrayList) {
                if (title.replace(" ", "").equals(course.getCourseName().replace(" ", "")))
                    courseExists = true;
            }

            //Only create new course if "courseexists" is false
            if (courseExists) {
                //Notify user
                Toast.makeText(getApplicationContext(),
                        getString(R.string.a_course_same_name),
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                //Add course
                final int index = mCourseArrayList.size();

                long date = Calendar.getInstance().getTimeInMillis();
                //If course has fixed points -> create instance of FixedPointsCourse

                Course course;
                if (hasFixedPoints) {
                    course = new FixedPointsCourse(title, index, maxPoints);
                    mCourseAdapter.addCourse(course);
                } else {
                    //Else create instance of DynamicPointsCourse
                    course = new DynamicPointsCourse(title, index);
                    mCourseAdapter.addCourse(course);
                }

                course.setDate(date);

                //Notify user with snackbar
                CoordinatorLayout cl =
                        (CoordinatorLayout) findViewById(R.id.coordinatorlayout_mainactivity);
                Snackbar.make(cl,
                        getString(R.string.course) + title + getString(R.string.has_been_added),
                        Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    //Undo: remove course
                    @Override
                    public void onClick(View v) {
                        //Get last element of the list and remove it
                        int index = mCourseArrayList.size() - 1;
                        mCourseAdapter.removeCourse(index);
                    }
                }).show();
            }
        } else {
            //Notify about empty string
            Toast.makeText(getApplicationContext(),
                    getString(R.string.abort_no_title),
                    Toast.LENGTH_SHORT)
                    .show();
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


    public void onClickMenuRestore(MenuItem item) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.restore));
        alert.setMessage(getString(R.string.do_you_want_restore));

        //Inflate view from resource
        final View view = View.inflate(this, R.layout.dialog_restore, null);
        alert.setView(view);


        //Create ArrayAdapter
        final ArrayList<String> backupPaths = new ArrayList<String>();

        //Create listview
        ListView listView = (ListView) view.findViewById(R.id.listView_restore_dialog);


        //Inflate Edittext
        final EditText editText = (EditText) view.findViewById(R.id.editText_restore_dialog);


        //check if external storage is readable
        if (isExternalStorageReadable()) {
            File myFilesDir = getBackupDir();

            //Get all backup files

            File files[] = myFilesDir.listFiles();
            //Iterate over all files
            for (File file : files) {
                //Get only filename
                String[] path = file.getPath().split("/");
                String filename = path[path.length - 1];
                if (filename.endsWith(".backup")) {
                    //Add filename to array
                    backupPaths.add(path[path.length - 1]);
                }
            }

            //Reverse brings the latest backup to the top
            Collections.reverse(backupPaths);


        }

        final PathAdapter<String> pathAdapter = new PathAdapter<String>(MainActivity.this, backupPaths);

        listView.setAdapter(pathAdapter);
        //Listview onclick

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editText.setText(pathAdapter.getItem(position));
            }
        });


        /**
         * Remove Selected Backup
         */
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removeBackup(position, pathAdapter);
                return true;
            }
        });


        alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                try {
                    File myFilesDir = getBackupDir();
                    if (editText.getText().toString().isEmpty())
                        Toast.makeText(getApplicationContext(), "No backup selected", Toast.LENGTH_SHORT).show();
                    //Restore from chosen Backup, which lays in the top textfield
                    FileInputStream fis = new FileInputStream(myFilesDir.getPath() + "/" + editText.getText().toString());
                    ObjectInputStream ois = new ObjectInputStream(fis);

                    //New Arraylist
                    ArrayList<Course> newArraylist = (ArrayList<Course>) ois.readObject();

                    //clear current arraylist to avoid double input
                    mCourseArrayList.clear();

                    //add each stored course item
                    for (Course course : newArraylist) {
                        mCourseArrayList.add(course);
                    }
                    ois.close();

                    //Notify course adapter
                    mCourseAdapter.notifyDataSetChanged();

                    //Notify user about completed restore
                    Toast.makeText(getApplicationContext(), getString(R.string.restore_complete), Toast.LENGTH_LONG).show();


                    //Update data
                    dataHelper.updateAllCourses(mCourseArrayList);

                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
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

    private void removeBackup(int position, final PathAdapter<String> pathAdapter) {
        final String fileName = pathAdapter.getItem(position);
        final File myFile = new File(
                Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath()
                        + "/CourseStatistics/files/"
                        + fileName);


        //Open Alert dialog to delete item

        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        //Set title and message
        alert.setTitle(getApplicationContext().getString(R.string.delete));
        alert.setMessage(getApplicationContext().getString(R.string.do_you_want_to_delete) + fileName + "?");

        //Set positive button behaviour
        alert.setPositiveButton(getApplicationContext().getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                pathAdapter.remove(fileName);
                pathAdapter.notifyDataSetChanged();
                myFile.delete();
            }
        });

        alert.setNegativeButton(getApplicationContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

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
                    File myFilesDir = getBackupDir();
                    myFilesDir.mkdirs();

                    //Write Course-Array-List to storage
                    try {
                        //add backup-string to date
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy-HH:mm:ss", Locale.US);
                        String formattedDate = df.format(c.getTime());

                        Toast.makeText(getApplicationContext(), formattedDate, Toast.LENGTH_LONG).show();
                        FileOutputStream fos = new FileOutputStream(myFilesDir.getPath() + "/" + formattedDate + ".backup");
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(mCourseArrayList);
                        oos.close();

                        //Notify user about completed backup
                        Toast.makeText(getApplicationContext(), getString(R.string.backup_complete), Toast.LENGTH_LONG).show();

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

    /**
     * Creates the Path to the Backup directory
     *
     * @return Path to file dir
     */
    @NonNull
    private File getBackupDir() {
        return new File(Environment
                .getExternalStorageDirectory()
                .getAbsolutePath()
                + "/CourseStatistics/files");
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    //Restore data if resumed
    @Override
    protected void onResume() {
        //Restore data
        mCourseArrayList = dataHelper.getAllCourses();

        //Notify Adapter
        mCourseAdapter.setmCourseArrayList(mCourseArrayList);
        mCourseAdapter.notifyDataSetChanged();
        super.onResume();
    }

    public ArrayList<Course> getCourseArrayList() {
        return this.mCourseArrayList;
    }

    public void notifiyCoursesChanged() {
        //Restore data
        mCourseAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyDataChanged() {
        notifiyCoursesChanged();
    }

    @Override
    public ArrayList<Course> getCourses() {
        return getCourseArrayList();
    }
}
