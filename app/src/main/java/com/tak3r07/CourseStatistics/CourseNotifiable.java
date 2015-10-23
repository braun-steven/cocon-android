package com.tak3r07.CourseStatistics;

import java.util.ArrayList;

/**
 * Created by tak on 8/27/15.
 */
public interface CourseNotifiable {
    public void notifyDataChanged();
    public ArrayList<Course> getCourses();
}
