package com.tak3r07.CourseStatistics.sync

import java.util.ArrayList

import objects.Course

interface CourseNotifiable {
    fun notifyDataChanged()
    fun getCourses():ArrayList<Course>
}
