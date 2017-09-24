package com.tak3r07.CourseStatistics.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast

import com.tak3r07.CourseStatistics.database.DataHelper
import com.tak3r07.CourseStatistics.sync.CourseNotifiable
import com.tak3r07.unihelper.R

import java.util.ArrayList
import java.util.UUID

import objects.Assignment
import objects.Course

class EditCourseActivity : AppCompatActivity(), CourseNotifiable {

    private val COURSE_TAG = "COURSE_TAG"
    private val COURSE_TAG_ID = "COURSE_TAG_ID"

    private var course: Course? = null
    private var dataHelper: DataHelper<EditCourseActivity>? = null
    private var mNameEditText: EditText? = null
    private var mNumberEditText: EditText? = null
    private var mMaxPointsEditText: EditText? = null
    private var mNecPercentToPassEditText: EditText? = null
    private var mFixedPointsChechbox: CheckBox? = null
    private var mTextViewMaxPoints: View? = null
    private var hasFixedPoints: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        //Get intent data
        val intent = intent
        val courseId = UUID.fromString(intent.extras!!.getString(COURSE_TAG_ID))

        //Get datahelper instance
        dataHelper = DataHelper(this)

        // Restore from storage
        course = dataHelper!!.getCourse(courseId)
        hasFixedPoints = course!!.hasFixedPoints()

        //Get View-References
        mNameEditText = findViewById<View>(R.id.course_editname_edittext) as EditText
        mNumberEditText = findViewById<View>(R.id.edit_text_numberofassignments) as EditText
        mMaxPointsEditText = findViewById<View>(R.id.edit_text_maxpointsperassignment) as EditText
        mNecPercentToPassEditText = findViewById<View>(R.id.edit_text_nec_percent_to_pass) as EditText
        mFixedPointsChechbox = findViewById<View>(R.id.checkBox) as CheckBox
        mTextViewMaxPoints = findViewById<View>(R.id.textView3)


        //Setup Text
        mNameEditText!!.setText(course!!.courseName)
        mNumberEditText!!.setText(course!!.numberOfAssignments.toString())
        mNecPercentToPassEditText!!.setText(course!!.necPercentToPass.toString())
        mFixedPointsChechbox!!.isChecked = hasFixedPoints

        //Add onclick listener for checkbox
        mFixedPointsChechbox!!.setOnClickListener {
            //Toggle
            toggleDynamicView()

            hasFixedPoints = !hasFixedPoints
        }

        //FPC: Set maxPoints
        //DPC: Remove edittext + description
        if (course!!.hasFixedPoints()) {
            mMaxPointsEditText!!.setText(course!!.toFPC().maxPoints!!.toString())
        } else {
            hideMaxPointsEdit()

        }

    }

    private fun hideMaxPointsEdit() {
        mTextViewMaxPoints!!.visibility = View.GONE
        mMaxPointsEditText!!.visibility = View.GONE
    }

    private fun showMaxPointsEdit() {
        mMaxPointsEditText!!.visibility = View.VISIBLE
        mTextViewMaxPoints!!.visibility = View.VISIBLE

    }

    private fun toggleDynamicView() {
        if (hasFixedPoints) {
            hideMaxPointsEdit()
        } else {
            showMaxPointsEdit()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_edit_course, menu)
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

    fun onClickSave(view: View) {
        val name = mNameEditText!!.text.toString()
        val numberOfAssignments = Integer.parseInt(mNumberEditText!!.text.toString())
        val necPercentToPass = java.lang.Double.parseDouble(mNecPercentToPassEditText!!.text.toString())


        //Count extra-assignments
        var countExtraAssignments = 0
        for (assignment in course!!.assignments) {
            if (assignment.isExtraAssignment) countExtraAssignments++
        }

        //Check if new numberOfAssignments Value is still smaller than the existing size
        if (numberOfAssignments >= course!!.assignments.size - countExtraAssignments) {

            //Save new values in course
            course!!.courseName = name
            course!!.numberOfAssignments = numberOfAssignments
            course!!.necPercentToPass = necPercentToPass


            //Create empty intent
            val data = Intent()

            //Set new date for update
            course!!.updateDate()


            //FPC: Add maxpoints
            if (hasFixedPoints) {
                val maxPoints = java.lang.Double.parseDouble(mMaxPointsEditText!!.text.toString())
                course!!.toFPC().maxPoints = maxPoints
                dataHelper!!.updateCourse(course!!.toFPC())
            } else {
                dataHelper!!.updateCourse(course!!.toDPC())
            }

            setResult(Activity.RESULT_OK, data)
            finish()
        } else {
            //Notify user about too low numberOfAssignments
            Toast.makeText(applicationContext, getString(R.string.number_assignments_too_low), Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickCancel(view: View) {
        setResult(Activity.RESULT_CANCELED, null)
        finish()
    }


    override fun notifyDataChanged() {
        //Do nothing since this activity does not provide a view
    }

    override fun getCourses(): ArrayList<Course> {
        return dataHelper!!.allCourses
    }
}
