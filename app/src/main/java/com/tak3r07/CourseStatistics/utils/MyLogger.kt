package com.tak3r07.CourseStatistics.utils

import android.util.Log

import objects.Assignment
import objects.Course
import utils.JSONParser

/**
 * Created by tak on 9/25/15.
 */
object MyLogger {
    fun logCourse(course: Course) {
        val tag = "COURSE"
        Log.d(tag, JSONParser.courseToJSON(course).toString())
    }

    internal fun logAssignment(assignment: Assignment) {
        val tag = "ASSIGNMENT"
        Log.d(tag, JSONParser.assignmentToJSON(assignment).toString())
    }
}
