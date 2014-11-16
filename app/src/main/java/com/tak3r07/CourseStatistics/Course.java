package com.tak3r07.CourseStatistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by tak3r07 on 11/9/14.
 */
public class Course implements Serializable{
    //Coursename
    private String courseName;

    //List of the course assignments
    private ArrayList<Assignment> mAssignmentArrayList = new ArrayList<Assignment>();



    //Number of how many assignments there are
    private int numberOfAssignments;
    private Double reachablePointsPerAssignment;


    public Course(String courseName) {

        //Sets course name
        setCourseName(courseName);

        //usually 13 assignments in one semester
        numberOfAssignments = 13;

        //Set reachablePointsPerAssignment
        reachablePointsPerAssignment = 100d;

    }





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

    //Calculate overall percentage of current assignments
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
        double overAllPercentage = Math.round(overAllAchievedPoints / overAllMaxPoints * 1000) / 10d;


        //return result
        return overAllPercentage;

    }

    //calculate endpercentage: (all points of current assignments) / ((max number of point per assignment) * (number of assignments))
    public Double getEndPercentage() {
        double overAllAchievedPoints = 0;

        //Iterate on the array and sum up all its max and achieved points
        Assignment currentAssignment;
        for (Iterator<Assignment> iterator = mAssignmentArrayList.iterator(); iterator.hasNext(); ) {

            overAllAchievedPoints += iterator.next().getAchievedPoints();

        }

        //Round on 4 digits

        double endPercentage = Math.round(overAllAchievedPoints/(reachablePointsPerAssignment*numberOfAssignments)*1000)/10d;

        //return result
        return endPercentage;
    }


     public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public ArrayList<Assignment> getAssignments(){ return mAssignmentArrayList;}
    public void setAssignments(ArrayList<Assignment> newAssignments){
        mAssignmentArrayList = newAssignments;
    }

    public int getNumberOfAssignments() {
        return numberOfAssignments;
    }

    public void setNumberOfAssignments(int numberOfAssignments) {
        this.numberOfAssignments = numberOfAssignments;
    }

    public Double getReachablePointsPerAssignment() {
        return reachablePointsPerAssignment;
    }

    public void setReachablePointsPerAssignment(Double reachablePointsPerAssignment) {
        this.reachablePointsPerAssignment = reachablePointsPerAssignment;
    }


}
