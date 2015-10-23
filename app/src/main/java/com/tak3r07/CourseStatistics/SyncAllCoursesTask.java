package com.tak3r07.CourseStatistics;

import android.app.Activity;

/**
 * Created by tak on 8/27/15.
 */
public class SyncAllCoursesTask extends AsyncJSONTask {
    public SyncAllCoursesTask(Activity activity) {
        super(activity);
    }

    @Override
    protected void onPostExecute(String jsonString) {
        super.onPostExecute(jsonString);

        new DataHelper(activity).onCoursesDownloaded(coursesFromServer);
    }
}
