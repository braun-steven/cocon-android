package com.tak3r07.CourseStatistics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by tak on 8/26/15.
 *
 * This class serves as a template to do different operations after it downloads all courses
 * from the server
 *
 * Classes which inherit from this class only should implement onPostExecute(...) and use the
 * global coursesFromServer object
 *
 * in onPostExecute(..) the super method super.onPostExecute(...) should always be called first!
 *
 */
public abstract class AsyncJSONTask extends AsyncTask<String, Void, String> {
    protected Activity activity;
    protected ProgressDialog dialog;
    protected ArrayList<Course> coursesFromServer;

    public AsyncJSONTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(activity);
        dialog.setMessage("Please wait...");
        dialog.setIndeterminate(true);
        dialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        String result = "";
        try {
            OkHttpClient client = new OkHttpClient();


            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            result = response.body().string();

        } catch (IOException e) {
            //TODO: catch exception
        }
        return result;
    }

    @Override
    protected void onPostExecute(String jsonString) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        coursesFromServer = JSONParser.jsonArrayToCourseArray(jsonString);

        super.onPostExecute(jsonString);
    }
}
