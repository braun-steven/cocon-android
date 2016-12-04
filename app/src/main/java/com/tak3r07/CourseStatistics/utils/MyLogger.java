package com.tak3r07.CourseStatistics.utils;

import android.util.Log;

import com.tak3r07.CourseStatistics.objects.Assignment;
import com.tak3r07.CourseStatistics.objects.Course;

/**
 * Created by tak on 9/25/15.
 */
public class MyLogger {
    public static void logCourse(Course course){
        String tag = "COURSE";
        Log.d(tag, JSONParser.courseToJSON(course).toString());
    }

    static void logAssignment(Assignment assignment){
        String tag = "ASSIGNMENT";
        Log.d(tag, JSONParser.assignmentToJSON(assignment).toString());
    }
}
