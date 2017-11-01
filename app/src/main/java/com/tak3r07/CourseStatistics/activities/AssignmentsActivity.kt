package com.tak3r07.CourseStatistics.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.tak3r07.CourseStatistics.adapter.RecyclerViewAssignmentAdapter
import com.tak3r07.CourseStatistics.database.DataHelper
import com.tak3r07.CourseStatistics.database.DatabaseHelper
import com.tak3r07.CourseStatistics.sync.CourseNotifiable
import com.tak3r07.unihelper.R
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration

import java.util.ArrayList
import java.util.UUID

import objects.Assignment
import objects.Course


class AssignmentsActivity : AppCompatActivity(), CourseNotifiable {
    private val COURSE_TAG_ID = "COURSE_TAG_ID"
    private var mAssignmentAdapter: RecyclerViewAssignmentAdapter? = null
    private lateinit var mCurrentCourse: Course
    private var mAssignments: ArrayList<Assignment>? = null
    private lateinit var courseId: UUID
    private var mFab: FloatingActionButton? = null
    private var dataHelper: DataHelper<AssignmentsActivity>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        //setup toolbar
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)


        //Get intent and implement received data
        val intent = intent

        //position of the course which was opened
        val string = intent.extras!!.getString(COURSE_TAG_ID)
        courseId = UUID.fromString(string)
        mCurrentCourse = DatabaseHelper(applicationContext)
                .getCourse(courseId)


        //Get assignments
        mAssignments = mCurrentCourse.assignments


        //Set activity title
        this.title = mCurrentCourse.courseName


        //RecyclerView Setup
        val mRecyclerView = findViewById<View>(R.id.recyclerview_assignments) as RecyclerView
        val mLinearLayoutManager = LinearLayoutManager(applicationContext)
        mAssignmentAdapter = RecyclerViewAssignmentAdapter(applicationContext, mCurrentCourse, this)
        mLinearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mRecyclerView.layoutManager = mLinearLayoutManager
        mRecyclerView.adapter = mAssignmentAdapter
        mRecyclerView.isLongClickable = true
        mRecyclerView.addItemDecoration(HorizontalDividerItemDecoration.Builder(this).build())
        mRecyclerView.itemAnimator = DefaultItemAnimator()
        mRecyclerView.setHasFixedSize(true)

        //Setup FAB
        setupFAB()

        //Get datahelper instance
        dataHelper = DataHelper(this)
    }

    private fun setupFAB() {
        mFab = findViewById<View>(R.id.fab_add_assignment) as FloatingActionButton
        mFab!!.setOnClickListener { onClickAddAssignment() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_course, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when (id) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }


        return super.onOptionsItemSelected(item)
    }

    private fun onClickAddAssignment() {
        val alert = AlertDialog.Builder(this)

        alert.setTitle(getString(R.string.new_assignment))
        alert.setMessage(getString(R.string.enter_assignment_points))

        // Set an custom dialog view to get user input
        val view = View.inflate(this, R.layout.dialog_add_assignment, null)
        alert.setView(view)

        val mEditTextMaxPoints = view.findViewById<View>(R.id.editText_maxPoints) as EditText
        val textView = view.findViewById<View>(R.id.textView_maxPoints) as TextView

        //If this course has fixed Points, dont show the possibilities of setting maxPoints
        if (mCurrentCourse.hasFixedPoints()) {
            val viewManager = mEditTextMaxPoints.parent as ViewManager
            viewManager.removeView(mEditTextMaxPoints)
            viewManager.removeView(textView)
        }

        alert.setPositiveButton(getString(R.string.add)) { dialog, whichButton ->
            //First check if there are already too many assignments
            //Count extra-assignments
            var countExtraAssignments = 0
            for (aMAssignmentArrayList in mAssignments!!) {
                if (aMAssignmentArrayList.isExtraAssignment) countExtraAssignments++
            }

            val mCheckBox = view.findViewById<View>(R.id.checkBox_extra_assignment) as CheckBox

            if (mCurrentCourse.numberOfAssignments - (mAssignments!!.size - countExtraAssignments) > 0 || mCheckBox.isChecked) {
                //Get EditText views
                val mEditTextAchievedPoints = view.findViewById<EditText>(R.id.editText_achievedPoints)
                val mEditTextMaxPoints = view.findViewById<EditText>(R.id.editText_maxPoints)

                //Get data from edittexts
                val achievedPointsString = mEditTextAchievedPoints.text.toString().replace(',', '.')
                var maxPointsString = ""
                if (!mCurrentCourse.hasFixedPoints()) {
                    maxPointsString = mEditTextMaxPoints.text.toString().replace(',', '.')
                }

                //Check if the entered Values are
                // numeric (doubles) and (or) maxPointsString is empty
                if (isNumeric(achievedPointsString) && (isNumeric(maxPointsString) || maxPointsString == "")) {

                    val achievedPoints = java.lang.Double.parseDouble(achievedPointsString)
                    val maxPoints: Double?
                    if (mCurrentCourse.hasFixedPoints()) {
                        maxPoints = mCurrentCourse.toFPC().maxPoints
                    } else {

                        maxPoints = java.lang.Double.parseDouble(maxPointsString)
                    }


                    // Index is lists' last element index + 1 (so another item is added)
                    val index: Int
                    //First check if list is empty
                    if (mAssignments!!.isEmpty()) {
                        index = 1
                    } else {

                        //get List-size
                        val size = mAssignments!!.size
                        index = mAssignments!![size - 1].index + 1
                    }


                    val NEW_ASSIGNMENT: UUID? = null


                    //Create new assignment from pulled data
                    val newAssignment = Assignment(
                            NEW_ASSIGNMENT,
                            index,
                            maxPoints!!,
                            achievedPoints,
                            mCurrentCourse.id)

                    //Check if this is an extra assignment
                    if (mCheckBox.isChecked) {
                        newAssignment.isExtraAssignment(true)
                    }

                    //add new assignment
                    addAssignment(newAssignment)
                } else {
                    //If data was not numeric
                    Toast.makeText(applicationContext,
                            getString(R.string.invalid_values),
                            Toast.LENGTH_LONG).show()

                }
            } else {
                //already too many assignments
                Toast.makeText(applicationContext,
                        getString(R.string.reached_assignments_limit),
                        Toast.LENGTH_LONG).show()
            }
        }

        alert.setNegativeButton(getString(R.string.cancel)) { dialog, whichButton ->
            // Canceled.
        }

        alert.show()

    }

    //Add assignment to adapter (Eventually add to course necessary?)
    fun addAssignment(assignment: Assignment) {
        //TODO: Check if "double" adding is necessary?
        mAssignmentAdapter!!.addAssignment(assignment)
        mCurrentCourse.addAssignment(assignment)
        mCurrentCourse.updateDate()
        dataHelper!!.updateCourse(mCurrentCourse)


        //Store assignment
        dataHelper!!.addAssignment(assignment)

        //Update list
        mAssignmentAdapter!!.notifyDataSetChanged()


        //Notify user
        val cl = findViewById<View>(R.id.coordinatorlayout_assignmentsactivity) as CoordinatorLayout
        Snackbar.make(cl,
                getString(R.string.new_assignment_added),
                Snackbar.LENGTH_LONG).setAction(getString(R.string.undo)) //Undo: remove course
        {
            //Get last element of the list and remove it
            val index = mAssignments!!.size - 1
            mAssignmentAdapter!!.removeAssignment(index)
        }.show()
    }

    override fun onBackPressed() {
        //Set result and finish
        val data = Intent()
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    fun onEditCourseClick(item: MenuItem) {
        //Put data into intent
        val intent = Intent()
        intent.setClass(applicationContext, EditCourseActivity::class.java)

        //Add Course-Number
        intent.putExtra(COURSE_TAG_ID, mCurrentCourse.id.toString())
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {


            // Restore from storage
            mCurrentCourse = dataHelper!!.getCourse(courseId)

            mAssignmentAdapter!!.setCurrentCourse(mCurrentCourse)
            mAssignments = mCurrentCourse.assignments
            mAssignmentAdapter!!.assignments = mCurrentCourse.assignments
            //Set new Title

            title = mCurrentCourse.courseName

            //Notify adapter for changes
            mAssignmentAdapter!!.notifyDataSetChanged()
        }
    }

    //Restore data if resumed
    override fun onResume() {
        // Restore
        mCurrentCourse = dataHelper!!.getCourse(courseId)
        super.onResume()
    }

    override fun notifyDataChanged() {
        mAssignmentAdapter!!.notifyDataSetChanged()
    }

    companion object {

        //Checks if a string is Numeric (Source: http://goo.gl/mGQ3Sp)
        fun isNumeric(str: String): Boolean {
            try {
                val d = java.lang.Double.parseDouble(str)
            } catch (nfe: NumberFormatException) {
                return false
            }

            return true
        }
    }

    override fun getCourses(): ArrayList<Course> {
        return dataHelper!!.allCourses
    }

}
