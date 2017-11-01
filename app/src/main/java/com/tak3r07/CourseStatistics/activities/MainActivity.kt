package com.tak3r07.CourseStatistics.activities

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast

import com.bartoszlipinski.recyclerviewheader.RecyclerViewHeader
import com.tak3r07.CourseStatistics.adapter.PathAdapter
import com.tak3r07.CourseStatistics.adapter.RecyclerViewCourseAdapter
import com.tak3r07.CourseStatistics.database.DataHelper
import com.tak3r07.CourseStatistics.sync.CourseNotifiable
import com.tak3r07.unihelper.R
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Locale

import objects.Course
import objects.DynamicPointsCourse
import objects.FixedPointsCourse


class MainActivity : AppCompatActivity(), CourseNotifiable {
    private val COURSE_ARRAY_LIST = "COURSE_ARRAY_LIST"


    var courseArrayList = ArrayList<Course>()
        private set
    private lateinit var mCourseAdapter: RecyclerViewCourseAdapter
    private lateinit var mFab: FloatingActionButton
    private lateinit var dataHelper: DataHelper<MainActivity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Setup toolbar
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        //get datahelper instance
        dataHelper = DataHelper(this)



        //RecyclerView Setup
        val mRecyclerView = findViewById<View>(R.id.recyclerview_courses) as RecyclerView
        val mLinearLayoutManager = LinearLayoutManager(applicationContext)
        mCourseAdapter = RecyclerViewCourseAdapter(this, applicationContext, courseArrayList)
        mLinearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mRecyclerView.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).build())
        mRecyclerView.layoutManager = mLinearLayoutManager
        mRecyclerView.adapter = mCourseAdapter
        mRecyclerView.isLongClickable = true
        mRecyclerView.itemAnimator = DefaultItemAnimator()
        mRecyclerView.setHasFixedSize(true)


        //Add header for column descriptions
        val header = RecyclerViewHeader.fromXml(applicationContext, R.layout.layout_courselist_header)
        header.attachTo(mRecyclerView)
        mCourseAdapter.notifyDataSetChanged()


        //Setup FAB
        setupFAB()
    }

    private fun setupFAB() {
        mFab = findViewById<FloatingActionButton>(R.id.fab_add_course)
        mFab.setOnClickListener { onClickAddCourse(null) }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        /*if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }*/

        return super.onOptionsItemSelected(item)
    }

    fun onClickAddCourse(item: MenuItem?) {
        val alert = AlertDialog.Builder(this)

        alert.setTitle(getString(R.string.new_course))
        alert.setMessage(getString(R.string.enter_course_name_and_max_p_p_a))

        //Set dialog_add_course layout
        val view = View.inflate(this, R.layout.dialog_add_course, null)
        alert.setView(view)


        //Checkbox
        val mCheckBox = view.findViewById<View>(R.id.checkBox_fixed_points) as CheckBox
        mCheckBox.isChecked = true
        //Get views
        val mCourseNameEditText = view.findViewById<View>(R.id.course_name_edittext) as EditText
        val mReachablePointsEditText = view.findViewById<View>(R.id.max_reachable_points_edittext) as EditText

        //Toggle Edittext on checkbox toggle
        mCheckBox.setOnClickListener {
            val isChecked = mCheckBox.isChecked

            //Toggle Eddittext
            mReachablePointsEditText.isEnabled = isChecked
        }

        //Add Course Operation
        alert.setPositiveButton(getString(R.string.add)) { dialog, whichButton ->
            //Get values
            val courseName = mCourseNameEditText.text.toString()
            val reachablePointsString = mReachablePointsEditText.text.toString().replace(',', '.')
            val hasFixedPoints = mCheckBox.isChecked

            if (hasFixedPoints) {
                //Convert
                if (AssignmentsActivity.isNumeric(reachablePointsString)) {
                    val reachablePoints = java.lang.Double.parseDouble(reachablePointsString)

                    addCourse(courseName, reachablePoints, hasFixedPoints)


                } else {
                    //If data was not numeric
                    Toast.makeText(applicationContext,
                            getString(R.string.invalid_values),
                            Toast.LENGTH_LONG).show()

                }
            } else {
                addCourse(courseName, 0.0, hasFixedPoints)
            }
        }

        alert.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton ->
            // Canceled.
        }

        alert.show()
    }

    //Adds a new course to the mCourseArrayList
    fun addCourse(title: String, maxPoints: Double, hasFixedPoints: Boolean) {
        //Check if string is empty
        if (!title.isEmpty()) {

            //boolean to check if coursename already exists
            var courseExists = false

            //Loop to check all coursenames
            for (course in courseArrayList) {
                if (title.replace(" ", "") == course.courseName.replace(" ", ""))
                    courseExists = true
            }

            //Only create new course if "courseexists" is false
            if (courseExists) {
                //Notify user
                Toast.makeText(applicationContext,
                        getString(R.string.a_course_same_name),
                        Toast.LENGTH_LONG)
                        .show()
            } else {
                //Add course
                val date = Calendar.getInstance().timeInMillis
                //If course has fixed points -> create instance of FixedPointsCourse

                val course: Course
                if (hasFixedPoints) {
                    course = FixedPointsCourse(title, maxPoints)
                    mCourseAdapter.addCourse(course)
                } else {
                    //Else create instance of DynamicPointsCourse
                    course = DynamicPointsCourse(title)
                    mCourseAdapter.addCourse(course)
                }

                course.date = date

                //Notify user with snackbar
                val cl = findViewById<View>(R.id.coordinatorlayout_mainactivity) as CoordinatorLayout
                Snackbar.make(cl,
                        getString(R.string.course) + title + getString(R.string.has_been_added),
                        Snackbar.LENGTH_LONG).setAction("Undo") //Undo: remove course
                {
                    //Get last element of the list and remove it
                    val index = courseArrayList.size - 1
                    mCourseAdapter.removeCourse(index)
                }.show()
            }
        } else {
            //Notify about empty string
            Toast.makeText(applicationContext,
                    getString(R.string.abort_no_title),
                    Toast.LENGTH_SHORT)
                    .show()
        }

    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save the user's current state
        // Save array list
        savedInstanceState.putSerializable(COURSE_ARRAY_LIST, courseArrayList)

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState)
    }


    fun onClickMenuRestore(item: MenuItem) {
        val alert = AlertDialog.Builder(this)

        alert.setTitle(getString(R.string.restore))
        alert.setMessage(getString(R.string.do_you_want_restore))

        //Inflate view from resource
        val view = View.inflate(this, R.layout.dialog_restore, null)
        alert.setView(view)


        //Create ArrayAdapter
        val backupPaths = ArrayList<String>()

        //Create listview
        val listView = view.findViewById<ListView>(R.id.listView_restore_dialog)


        //Inflate Edittext
        val editText = view.findViewById<EditText>(R.id.editText_restore_dialog)


        //check if external storage is readable
        if (isExternalStorageReadable) {
            val myFilesDir = backupDir

            //Get all backup files

            val files = myFilesDir.listFiles()
            //Iterate over all files
            for (file in files) {
                //Get only filename
                val path = file.path.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val filename = path[path.size - 1]
                if (filename.endsWith(".backup")) {
                    //Add filename to array
                    backupPaths.add(path[path.size - 1])
                }
            }

            //Reverse brings the latest backup to the top
            Collections.reverse(backupPaths)


        }

        val pathAdapter = PathAdapter(this@MainActivity, backupPaths)

        listView.adapter = pathAdapter
        //Listview onclick

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> editText.setText(pathAdapter.getItem(position)) }


        /**
         * Remove Selected Backup
         */
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            removeBackup(position, pathAdapter)
            true
        }


        alert.setPositiveButton(getString(R.string.confirm)) { dialog, whichButton ->
            try {
                val myFilesDir = backupDir
                if (editText.text.toString().isEmpty())
                    Toast.makeText(applicationContext, "No backup selected", Toast.LENGTH_SHORT).show()
                //Restore from chosen Backup, which lays in the top textfield
                val fis = FileInputStream(myFilesDir.path + "/" + editText.text.toString())
                val ois = ObjectInputStream(fis)

                //New Arraylist
                val newArraylist = ois.readObject() as ArrayList<Course>

                //clear current arraylist to avoid double input
                courseArrayList.clear()

                //add each stored course item
                for (course in newArraylist) {
                    courseArrayList.add(course)
                }
                ois.close()

                //Notify course adapter
                mCourseAdapter.notifyDataSetChanged()

                //Notify user about completed restore
                Toast.makeText(applicationContext, getString(R.string.restore_complete), Toast.LENGTH_LONG).show()


                //Update data
                dataHelper.updateAllCourses(courseArrayList)

            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        alert.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton ->
            // Canceled.
        }

        alert.show()
    }

    private fun removeBackup(position: Int, pathAdapter: PathAdapter<String>) {
        val fileName = pathAdapter.getItem(position)
        val myFile = File(
                Environment
                        .getExternalStorageDirectory()
                        .absolutePath
                        + "/CourseStatistics/files/"
                        + fileName)


        //Open Alert dialog to delete item

        val alert = AlertDialog.Builder(this@MainActivity)

        //Set title and message
        alert.setTitle(applicationContext.getString(R.string.delete))
        alert.setMessage(applicationContext.getString(R.string.do_you_want_to_delete) + fileName + "?")

        //Set positive button behaviour
        alert.setPositiveButton(applicationContext.getString(R.string.delete)) { dialog, whichButton ->
            pathAdapter.remove(fileName)
            pathAdapter.notifyDataSetChanged()
            myFile.delete()
        }

        alert.setNegativeButton(applicationContext.getString(R.string.cancel)) { dialog, which -> }

        alert.show()
    }

    //Restore Button in Menu
    fun onClickMenuBackup(item: MenuItem) {
        val alert = AlertDialog.Builder(this)

        alert.setTitle(getString(R.string.backup))
        alert.setMessage(getString(R.string.backup_to_storage))


        alert.setPositiveButton(getString(R.string.confirm)) { dialog, whichButton ->
            //Check for RW permissions
            if (isExternalStorageWritable) {

                if (ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(this@MainActivity,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),0);

                }


                val myFilesDir = backupDir
                myFilesDir.mkdirs()

                //Write Course-Array-List to storage
                try {
                    //add backup-string to date
                    val c = Calendar.getInstance()
                    val df = SimpleDateFormat("dd-MMM-yyyy-HH:mm:ss", Locale.US)
                    val formattedDate = df.format(c.time)

                    Toast.makeText(applicationContext, formattedDate, Toast.LENGTH_LONG).show()
                    val backupFile = File(myFilesDir.path + "/" + formattedDate + ".backup")
                    val fos = FileOutputStream(myFilesDir.path + "/" + formattedDate + ".backup")
                    val oos = ObjectOutputStream(fos)
                    oos.writeObject(courseArrayList)
                    oos.close()

                    //Notify user about completed backup
                    Toast.makeText(applicationContext, getString(R.string.backup_complete), Toast.LENGTH_LONG).show()

                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

        alert.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton ->
            // Canceled.
        }

        alert.show()
    }

    /**
     * Creates the Path to the Backup directory

     * @return Path to file dir
     */
    private val backupDir: File
        get() = File(Environment.getExternalStorageDirectory().path + "/CourseStatistics/files")

    /* Checks if external storage is available for read and write */
    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    /* Checks if external storage is available to at least read */
    val isExternalStorageReadable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

    //Restore data if resumed
    override fun onResume() {
        //Restore data
        courseArrayList = dataHelper.allCourses

        //Notify Adapter
        mCourseAdapter.setmCourseArrayList(courseArrayList)
        mCourseAdapter.notifyDataSetChanged()
        super.onResume()
    }

    fun notifiyCoursesChanged() {
        //Restore data
        mCourseAdapter.notifyDataSetChanged()
    }

    override fun notifyDataChanged() {
        notifiyCoursesChanged()
    }

    override fun getCourses(): ArrayList<Course> {
        return dataHelper.allCourses
    }

}
