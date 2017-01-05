package com.tak3r07.CourseStatistics.sync;

import java.util.ArrayList;

import objects.Course;

/**
 * Created by tak on 8/27/15.
 */
public interface CourseNotifiable {
    public void notifyDataChanged();

    public ArrayList<Course> getCourses();
}
