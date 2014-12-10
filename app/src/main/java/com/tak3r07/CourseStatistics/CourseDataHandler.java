package com.tak3r07.CourseStatistics;

import android.content.Context;

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
            for (Iterator<Course> it = newArraylist.iterator(); it.hasNext(); ) {
                mCourseArrayList.add(it.next());
            }
            ois.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return mCourseArrayList;
    }
}
