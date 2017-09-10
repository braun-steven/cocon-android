package com.tak3r07.CourseStatistics.sync

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response

import java.io.IOException
import java.util.ArrayList

import objects.Course
import utils.JSONParser

/**
 * Created by tak on 8/26/15.
 *
 *
 * This class serves as a template to do different operations after it downloads all courses
 * from the server
 *
 *
 * Classes which inherit from this class only should implement onPostExecute(...) and use the
 * global coursesFromServer object
 *
 *
 * in onPostExecute(..) the super method super.onPostExecute(...) should always be called first!
 */
abstract class AsyncJSONTask(protected var activity: Activity) : AsyncTask<String, Void, String>() {
    protected var dialog: ProgressDialog
    protected var coursesFromServer: ArrayList<Course>

    override fun onPreExecute() {
        dialog = ProgressDialog(activity)
        dialog.setMessage("Please wait...")
        dialog.isIndeterminate = true
        dialog.show()
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String): String {
        val url = params[0]
        var result = ""
        try {
            val client = OkHttpClient()


            val request = Request.Builder()
                    .url(url)
                    .build()

            val response = client.newCall(request).execute()
            result = response.body().string()

        } catch (e: IOException) {
            //TODO: catch exception
        }

        return result
    }

    override fun onPostExecute(jsonString: String) {
        if (dialog.isShowing) {
            dialog.dismiss()
        }

        coursesFromServer = JSONParser.jsonArrayToCourseArray(jsonString)

        super.onPostExecute(jsonString)
    }
}
