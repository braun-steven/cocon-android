package com.tak3r07.CourseStatistics.sync;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.tak3r07.CourseStatistics.objects.Assignment;
import com.tak3r07.CourseStatistics.objects.Course;
import com.tak3r07.CourseStatistics.utils.JSONParser;
import com.tak3r07.CourseStatistics.utils.Vocab;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by tak on 8/26/15.
 * This class provides methods to communicate with the application server
 */
public class ServerHelper {
    private Activity activity;
    private Context context;

    public ServerHelper(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public void addCourse(Course course) {
        if (!canConnect()) {
            alertNoConnection();
            return;
        }

        String suffix = "/insertCourse";
        String url = Vocab.URL_PREFIX + suffix;
        postCourse(course, url);

        //Add all its assignments
        for (Assignment a : course.getAssignments()) {
            addAssignment(a);
        }
    }

    public void addAssignment(Assignment assignment) {
        if (!canConnect()) {
            alertNoConnection();
            return;
        }
        String suffix = "/insertAssignment";
        String url = Vocab.URL_PREFIX + suffix;
        postAssignment(assignment, url);
    }


    public void updateAllCourses(ArrayList<Course> courses) {
        if (!canConnect()) {
            alertNoConnection();
            return;
        }
        for (Course c : courses) {
            updateCourse(c);

        }
    }

    public void updateCourse(Course course) {
        if (!canConnect()) {
            alertNoConnection();
            return;
        }
        String suffix = "/updateCourse";
        String url = Vocab.URL_PREFIX + suffix;
        postCourse(course, url);
    }

    public void updateAssignment(Assignment assignment) {
        if (!canConnect()) {
            alertNoConnection();
            return;
        }
        String suffix = "/updateAssignment";
        String url = Vocab.URL_PREFIX + suffix;
        postAssignment(assignment, url);
    }

    public void deleteCourse(Course course) {
        if (!canConnect()){
            alertNoConnection();
            return;
        }
        String suffix = "/deleteCourse";
        String parameter = "?id=" + course.getId();
        String url = Vocab.URL_PREFIX + suffix + parameter;
        delete(url);
    }

    public void deleteAssignment(Assignment assignment) {
        if (!canConnect()){
            alertNoConnection();
            return;
        }
        String suffix = "/deleteAssignment";
        String parameter = "?id=" + assignment.getId();
        String url = Vocab.URL_PREFIX + suffix + parameter;
        delete(url);
    }


    public void delete(String url){
        new AsyncJSONTask(activity){
            @Override
            protected void onPostExecute(String jsonString) {
                super.onPostExecute(jsonString);
            }
        }.execute(url);
    }


    public static void postCourse(final Course course, final String url) {

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                final MediaType JSON
                        = MediaType.parse("application/json; charset=utf-8");

                OkHttpClient client = new OkHttpClient();

                //Get jsonobject from course
                JSONObject json = JSONParser.courseToJSON(course);

                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    String result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            }
        }.execute(url);
    }

    public static void postAssignment(final Assignment assignment, final String url) {

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                final MediaType JSON
                        = MediaType.parse("application/json; charset=utf-8");

                OkHttpClient client = new OkHttpClient();

                //Get jsonobject from course
                JSONObject json = JSONParser.assignmentToJSON(assignment);

                RequestBody body = RequestBody.create(JSON, json.toString());
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    String result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            }
        }.execute(url);
    }


    public boolean canConnect() {
        ConnectivityManager connMgr = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public void alertNoConnection() {
        //ALERT!!
        //TODO: popup toast or snackbar to notify user about no connection
    }

}
