package com.tak3r07.CourseStatistics.database

import android.app.Activity
import android.content.Context

import com.tak3r07.CourseStatistics.sync.CourseNotifiable
import com.tak3r07.CourseStatistics.sync.ServerHelper
import com.tak3r07.CourseStatistics.sync.SyncAllCoursesTask
import com.tak3r07.CourseStatistics.utils.Vocab

import java.util.ArrayList
import java.util.UUID

import objects.Assignment
import objects.Course

/**
 * Created by tak on 8/27/15.
 * This class is an abstraction for the data-handling within the whole app.
 * Nothing else should communicate with the DatabaseHelper-Class or the ServerHelper-Class
 */
class DataHelper<MyActivity : Activity>(private val activity: MyActivity) where MyActivity : CourseNotifiable {
    private val serverHelper: ServerHelper
    private val databaseHelper: DatabaseHelper
    private val context: Context
    private val SERVER_ON = false

    init {
        this.context = activity.applicationContext
        this.serverHelper = ServerHelper(activity)
        this.databaseHelper = DatabaseHelper(context)
    }

    fun addCourse(course: Course) {
        if (SERVER_ON) serverHelper.addCourse(course)
        databaseHelper.addCourse(course)

    }

    fun addAssignment(assignment: Assignment) {
        if (SERVER_ON) serverHelper.addAssignment(assignment)
        databaseHelper.addAssignment(assignment)
    }

    //first sync
    //Then get from local
    val allCourses: ArrayList<Course>
        get() {
            if (SERVER_ON) syncAllCourses()
            return databaseHelper.allCourses
        }

    fun updateAllCourses(courses: ArrayList<Course>) {
        //first update locally
        databaseHelper.updateCourses(courses)

        //then sync with server
        if (SERVER_ON) syncAllCourses()
    }

    fun updateCourse(course: Course) {
        databaseHelper.updateCourse(course)
        if (SERVER_ON) serverHelper.updateCourse(course)

    }

    fun updateAssignment(assignment: Assignment) {
        databaseHelper.updateAssignment(assignment)
        if (SERVER_ON) serverHelper.updateAssignment(assignment)
    }

    fun getCourse(courseId: UUID): Course {
        return databaseHelper.getCourse(courseId)
    }

    fun getAssignmentsOfCourse(courseId: UUID): ArrayList<Assignment> {
        return databaseHelper.getAssignmentsOfCourse(courseId)
    }

    fun deleteCourse(course: Course) {
        databaseHelper.deleteCourse(course)
        if (SERVER_ON) serverHelper.deleteCourse(course)
    }

    fun deleteAssignment(assignment: Assignment): Boolean {
        databaseHelper.deleteAssignment(assignment)
        if (SERVER_ON) serverHelper.deleteAssignment(assignment)
        return false
    }

    fun syncAllCourses() {
        if (serverHelper.canConnect()) {
            // fetch data
            val suffix = "/getAllCourses?userId=" + DatabaseHelper(context).userUUID
            val url = Vocab.URL_PREFIX + suffix
            //calls onCoursesDownloaded(...) when finished:
            SyncAllCoursesTask(activity).execute(url)
        } else {
            // display error
        }
    }

    fun onCoursesDownloaded(coursesFromServer: ArrayList<Course>) {
        compareAndUpdateCourses(coursesFromServer)
    }

    fun compareAndUpdateCourses(coursesFromServer: ArrayList<Course>) {
        //For each course on the server check which one is more up to date and update the other side
        for (serverCourse in coursesFromServer) {
            for (localCourse in databaseHelper.allCourses) {
                if (serverCourse.id == localCourse.id) {

                    // Skip if equal
                    if (serverCourse.date != localCourse.date) {
                        // Else sync
                        if (serverCourse.isNewerThan(localCourse)) {
                            // sync serverCourse to local
                            databaseHelper.updateCourse(serverCourse)
                        } else {
                            // sync localCourse to server
                            serverHelper.updateCourse(localCourse)
                        }
                    }
                }
            }
        }

        //For each course on the server check if it exists local, if not -> add locally
        for (serverCourse in coursesFromServer) {
            var isLocal = false
            for (localCourse in activity.courses) {
                if (serverCourse.id == localCourse.id) {
                    isLocal = true
                }
            }
            //If isLocal is still false -> add serverCourse to sqliteDB
            if (!isLocal) {
                databaseHelper.addCourse(serverCourse)
            }
        }
        //Notify the activity
        activity.notifyDataChanged()
    }
}
