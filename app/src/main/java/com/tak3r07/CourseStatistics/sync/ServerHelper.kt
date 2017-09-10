package com.tak3r07.CourseStatistics.sync

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.Response
import com.tak3r07.CourseStatistics.database.DatabaseHelper
import com.tak3r07.CourseStatistics.utils.Vocab

import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.util.ArrayList

import objects.Assignment
import objects.Course
import utils.JSONParser

/**
 * Created by tak on 8/26/15.
 * This class provides methods to communicate with the application server
 */
class ServerHelper(private val activity: Activity) {
    private val context: Context

    init {
        this.context = activity.applicationContext
    }

    fun addCourse(course: Course) {
        if (!canConnect()) {
            alertNoConnection()
            return
        }

        val suffix = "/updateCourse"
        val url = Vocab.URL_PREFIX + suffix
        postCourse(course, url)

        //Add all its assignments
        //for (Assignment a : course.getAssignments()) {
        //    addAssignment(a);
        //}
    }

    fun addAssignment(assignment: Assignment) {
        if (!canConnect()) {
            alertNoConnection()
            return
        }
        val suffix = "/insertAssignment"
        val url = Vocab.URL_PREFIX + suffix
        postAssignment(assignment, url)
    }

    fun updateAllCourses(courses: ArrayList<Course>) {
        if (!canConnect()) {
            alertNoConnection()
            return
        }
        for (c in courses) {
            updateCourse(c)

        }
    }

    fun updateCourse(course: Course) {
        addCourse(course)
    }

    fun updateAssignment(assignment: Assignment) {
        if (!canConnect()) {
            alertNoConnection()
            return
        }
        val suffix = "/updateAssignment"
        val url = Vocab.URL_PREFIX + suffix
        postAssignment(assignment, url)
    }

    fun deleteCourse(course: Course) {
        if (!canConnect()) {
            alertNoConnection()
            return
        }
        val suffix = "/deleteCourse"
        val parameter = "?id=" + course.id
        val url = Vocab.URL_PREFIX + suffix + parameter

        val json = JSONObject()
        try {
            json.put("userId", DatabaseHelper(context).userUUID)
            json.put("course", JSONParser.courseToJSON(course))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        delete(json, url)
    }

    fun deleteAssignment(assignment: Assignment) {
        if (!canConnect()) {
            alertNoConnection()
            return
        }
        val suffix = "/deleteAssignment"
        val parameter = "?id=" + assignment.id
        val url = Vocab.URL_PREFIX + suffix + parameter


        val json = JSONObject()
        try {
            json.put("userId", DatabaseHelper(context).userUUID)
            json.put("course", JSONParser.assignmentToJSON(assignment))
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        delete(json, url)
    }

    fun delete(job: JSONObject, url: String) {

        object : AsyncTask<String, Void, String>() {
            override fun doInBackground(vararg params: String): String {
                val JSON = MediaType.parse("application/json; charset=utf-8")

                val client = OkHttpClient()


                val body = RequestBody.create(JSON, job.toString())
                val request = Request.Builder()
                        .url(url)
                        .delete(body)
                        .build()
                var response: Response? = null
                try {
                    response = client.newCall(request).execute()
                    val result = response!!.body().string()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                return ""
            }
        }.execute(url)

    }

    fun postCourse(course: Course, url: String) {

        object : AsyncTask<String, Void, String>() {
            override fun doInBackground(vararg params: String): String {
                val JSON = MediaType.parse("application/json; charset=utf-8")

                val client = OkHttpClient()

                val json = JSONObject()
                try {
                    json.put("userId", DatabaseHelper(context).userUUID)
                    json.put("course", JSONParser.courseToJSON(course))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                val body = RequestBody.create(JSON, json.toString())
                val request = Request.Builder()
                        .url(url)
                        .post(body)
                        .build()
                var response: Response? = null
                try {
                    response = client.newCall(request).execute()
                    val result = response!!.body().string()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                return ""
            }
        }.execute(url)
    }

    fun canConnect(): Boolean {
        val connMgr = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            return true
        } else {
            return false
        }
    }

    fun alertNoConnection() {
        //ALERT!!
        //TODO: popup toast or snackbar to notify user about no connection
    }

    companion object {

        fun postAssignment(assignment: Assignment, url: String) {

            object : AsyncTask<String, Void, String>() {
                override fun doInBackground(vararg params: String): String {
                    val JSON = MediaType.parse("application/json; charset=utf-8")

                    val client = OkHttpClient()

                    //Get jsonobject from course
                    val json = JSONParser.assignmentToJSON(assignment)

                    val body = RequestBody.create(JSON, json.toString())
                    val request = Request.Builder()
                            .url(url)
                            .post(body)
                            .build()
                    var response: Response? = null
                    try {
                        response = client.newCall(request).execute()
                        val result = response!!.body().string()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    return ""
                }
            }.execute(url)
        }
    }

}
