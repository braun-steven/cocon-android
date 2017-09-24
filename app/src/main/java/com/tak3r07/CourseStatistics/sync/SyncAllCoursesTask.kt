package com.tak3r07.CourseStatistics.sync

import android.app.Activity

import com.tak3r07.CourseStatistics.database.DataHelper

class SyncAllCoursesTask(activity: Activity) : AsyncJSONTask(activity) {

    override fun onPostExecute(jsonString: String) {
        super.onPostExecute(jsonString)
        //DataHelper(activity).onCoursesDownloaded(coursesFromServer)
    }
}
