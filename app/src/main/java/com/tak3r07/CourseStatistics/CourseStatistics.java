package com.tak3r07.CourseStatistics;

import android.app.Application;
import android.content.Context;

/**
 * Created by tak3r07 on 12/17/14.
 */
public class CourseStatistics extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        CourseStatistics.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return CourseStatistics.context;
    }
}