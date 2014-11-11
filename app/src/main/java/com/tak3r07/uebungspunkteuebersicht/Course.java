package com.tak3r07.uebungspunkteuebersicht;

import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by tak3r07 on 11/9/14.
 */
public class Course {

    public Course(String courseName) {
        setCourseName(courseName);
    }

    //Coursename
    private String courseName;

    //List of the course assignments
    private ArrayList<Assignment> mAssignmentArrayList = new ArrayList<Assignment>();


    //Constructor
    public Assignment getAssignment(int index) {
        try {
            return mAssignmentArrayList.get(index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Add new Course
    public void addAssignment(Assignment assignment) {
        mAssignmentArrayList.add(assignment);
    }

    //Calculate overall percentage
    public Double getOverAllPercentage() {
        double overAllAchievedPoints = 0;
        double overAllMaxPoints = 0;


        //Iterate on the array and sum up all its max and achieved points
        Assignment currentAssignment;
        for (Iterator<Assignment> iterator = mAssignmentArrayList.iterator(); iterator.hasNext(); ) {
            currentAssignment = iterator.next();
            overAllAchievedPoints += currentAssignment.getAchievedPoints();
            overAllMaxPoints += currentAssignment.getMaxPoints();
        }

        //Round on 4 digits
        double overAllPercentage = Math.round(overAllAchievedPoints / overAllMaxPoints * 1000) / 1000d;


        //return result
        return overAllPercentage;

    }


    public ArrayList<Assignment> getmAssignmentArrayList() {
        return mAssignmentArrayList;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
