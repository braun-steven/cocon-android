package com.tak3r07.CourseStatistics;

import android.util.Log;

/**
 * Created by tak on 9/25/15.
 */
public class MyLogger {
    static void logCourse(Course course){
        String tag = "COURSE";
        Log.d(tag, JSONParser.courseToJSON(course).toString());
    }

    static void logAssignment(Assignment assignment){
        String tag = "ASSIGNMENT";
        Log.d(tag, JSONParser.assignmentToJSON(assignment).toString());
    }
}
