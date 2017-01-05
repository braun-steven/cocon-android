package com.tak3r07.CourseStatistics.database;

import android.app.Activity;
import android.content.Context;

import com.tak3r07.CourseStatistics.sync.CourseNotifiable;
import com.tak3r07.CourseStatistics.sync.ServerHelper;
import com.tak3r07.CourseStatistics.sync.SyncAllCoursesTask;
import com.tak3r07.CourseStatistics.utils.Vocab;

import java.util.ArrayList;
import java.util.UUID;

import objects.Assignment;
import objects.Course;

/**
 * Created by tak on 8/27/15.
 * This class is an abstraction for the data-handling within the whole app.
 * Nothing else should communicate with the DatabaseHelper-Class or the ServerHelper-Class
 */
public class DataHelper<MyActivity extends Activity & CourseNotifiable> {
    private ServerHelper serverHelper;
    private DatabaseHelper databaseHelper;
    private MyActivity activity;
    private Context context;
    private boolean SERVER_ON = true;

    public DataHelper(MyActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.serverHelper = new ServerHelper(activity);
        this.databaseHelper = new DatabaseHelper(context);
    }

    public void addCourse(Course course) {
        if (SERVER_ON) serverHelper.addCourse(course);
        databaseHelper.addCourse(course);

    }

    public void addAssignment(Assignment assignment) {
        if (SERVER_ON) serverHelper.addAssignment(assignment);
        databaseHelper.addAssignment(assignment);
    }

    public ArrayList<Course> getAllCourses() {
        //first sync
        if (SERVER_ON) syncAllCourses();

        //Then get from local
        return databaseHelper.getAllCourses();
    }

    public void updateAllCourses(ArrayList<Course> courses) {
        //first update locally
        databaseHelper.updateCourses(courses);

        //then sync with server
        if (SERVER_ON) syncAllCourses();
    }

    public void updateCourse(Course course) {
        databaseHelper.updateCourse(course);
        if (SERVER_ON) serverHelper.updateCourse(course);

    }

    public void updateAssignment(Assignment assignment) {
        databaseHelper.updateAssignment(assignment);
        if (SERVER_ON) serverHelper.updateAssignment(assignment);
    }

    public Course getCourse(UUID courseId) {
        return databaseHelper.getCourse(courseId);
    }

    public ArrayList<Assignment> getAssignmentsOfCourse(UUID courseId) {
        return databaseHelper.getAssignmentsOfCourse(courseId);
    }

    public void deleteCourse(Course course) {
        databaseHelper.deleteCourse(course);
        if (SERVER_ON) serverHelper.deleteCourse(course);
    }

    public boolean deleteAssignment(Assignment assignment) {
        databaseHelper.deleteAssignment(assignment);
        if (SERVER_ON) serverHelper.deleteAssignment(assignment);
        return false;
    }

    public void syncAllCourses() {
        if (serverHelper.canConnect()) {
            // fetch data
            final String suffix = "/getAllCourses?userId=" + new DatabaseHelper(context).getUserUUID();
            final String url = Vocab.URL_PREFIX + suffix;
            //calls onCoursesDownloaded(...) when finished:
            new SyncAllCoursesTask(activity).execute(url);
        } else {
            // display error
        }
    }

    public void onCoursesDownloaded(ArrayList<Course> coursesFromServer) {
        compareAndUpdateCourses(coursesFromServer);
    }

    public void compareAndUpdateCourses(ArrayList<Course> coursesFromServer) {
        //For each course on the server check which one is more up to date and update the other side
        for (Course serverCourse : coursesFromServer) {
            for (final Course localCourse : databaseHelper.getAllCourses()) {
                if (serverCourse.getId().equals(localCourse.getId())) {

                    // Skip if equal
                    if (serverCourse.getDate() != localCourse.getDate()) {
                        // Else sync
                        if (serverCourse.isNewerThan(localCourse)) {
                            // sync serverCourse to local
                            databaseHelper.updateCourse(serverCourse);
                        } else {
                            // sync localCourse to server
                            serverHelper.updateCourse(localCourse);
                        }
                    }
                }
            }
        }

        //For each course on the server check if it exists local, if not -> add locally
        for (Course serverCourse : coursesFromServer) {
            boolean isLocal = false;
            for (Course localCourse : activity.getCourses()) {
                if (serverCourse.getId().equals(localCourse.getId())) {
                    isLocal = true;
                }
            }
            //If isLocal is still false -> add serverCourse to sqliteDB
            if (!isLocal) {
                databaseHelper.addCourse(serverCourse);
            }
        }
        //Notify the activity
        activity.notifyDataChanged();
    }
}
