package com.tak3r07.CourseStatistics.sync

import java.util.ArrayList

import objects.Course

/**
 * Created by tak on 8/27/15.
 */
interface CourseNotifiable {
    fun notifyDataChanged()

    val courses: ArrayList<Course>
}
