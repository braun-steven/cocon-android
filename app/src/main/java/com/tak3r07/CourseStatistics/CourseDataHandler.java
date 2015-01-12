package com.tak3r07.CourseStatistics;

import android.content.Context;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by tak3r07 on 12/3/14.
 */
public class CourseDataHandler {

    public static void save(Context context, ArrayList<Course> mCourseArrayList) {
        //Store Data into InternalStorage
        try {
            FileOutputStream fos = context.openFileOutput("data", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mCourseArrayList);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Data could not be stored", Toast.LENGTH_LONG).show();
        }
    }

    public static ArrayList<Course> restore(Context context, ArrayList<Course> mCourseArrayList) {
        //Restore data

        try {
            FileInputStream fis = context.openFileInput("data");
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<Course> newArraylist = (ArrayList<Course>) ois.readObject();

            //clear current arraylist to avoid double input
            if (mCourseArrayList != null) {
                mCourseArrayList.clear();
            } else {
                //if null -> create new
                mCourseArrayList = new ArrayList<Course>();
            }

            //add each stored course item
            for (Course course : newArraylist) {
                mCourseArrayList.add(course);
            }
            ois.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Data could not be read", Toast.LENGTH_LONG).show();
        }
        return mCourseArrayList;
    }
}
