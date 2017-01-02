package com.tak3r07.CourseStatistics.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

/**
 * Created by tak3r07 on 11/9/14.
 */
public abstract class Course implements Serializable {
    static final long serialVersionUID = 2099962292244075360L;

    //Coursename
    private String courseName;

    //List of the course assignments
    private ArrayList<Assignment> mAssignmentArrayList = new ArrayList<>();


    //Number of how many assignments there are
    private int numberOfAssignments;

    //Unique course-id
    private UUID id;

    //Item-Index in course-list
    private int index;

    //Date of creation
    private long date;



    //Neccessary points to pass the course
    private double necPercentToPass;

    public Course(String courseName, int index) {
        //Initialize
        this.index = index;
        this.courseName = courseName;

        //usually 13 assignments in one semester
        numberOfAssignments = 13;

        //Usually 50% necessary to pass the course
        necPercentToPass = 0.5d;

        //Set date default to 0 and only set to Calendar.getInstance().getTimeInMillis(); when created /updated
        date = 0;

        //Set random id
        Random rand = new Random();
        this.id = UUID.randomUUID();

    }

    public Assignment getAssignment(int index) {
        try {
            return mAssignmentArrayList.get(index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Add new Assignment
    public void addAssignment(Assignment assignment) {
        mAssignmentArrayList.add(assignment);
    }

    //Calculate average of current assignments
    public abstract Double getAverage(boolean extraAssignments);

    /**
     * Simple Clone code (deep copy)
     */
    public abstract Course clone();


    /**
     * Returns total points of all assignments of this course
     *
     * @return achievedPointsAtAll Double
     */
    public Double getTotalPoints() {

        //Start sum at 0 points
        Double achievedPointsAtAll = 0.;

        //Iterate on the array and sum up all its achieved points
        for (Assignment assignment : mAssignmentArrayList) {
            achievedPointsAtAll += assignment.getAchievedPoints();
        }

        return achievedPointsAtAll;
    }

    public int getIndex() {
        return this.index;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public ArrayList<Assignment> getAssignments() {
        return mAssignmentArrayList;
    }

    public void setAssignments(ArrayList<Assignment> newAssignments) {
        mAssignmentArrayList = newAssignments;
    }

    public int getNumberOfAssignments() {
        return numberOfAssignments;
    }

    public void setNumberOfAssignments(int numberOfAssignments) {
        this.numberOfAssignments = numberOfAssignments;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Creates a FixedPointsCourse of this course
     * @return new FixedPointsCourse
     */
    public FixedPointsCourse toFPC() {
        /*
        final int maxPoints = 100;
        FixedPointsCourse fpc = new FixedPointsCourse(courseName, index, maxPoints);
        fpc.setId(this.id);
        fpc.setAssignments(this.mAssignmentArrayList);
        fpc.setDate(this.date);
        fpc.setNecPercentToPass(this.necPercentToPass);
        fpc.setNumberOfAssignments(this.numberOfAssignments);
        */
        return (FixedPointsCourse) this;
    }

    /**
     * Creates a DynamicPointsCourse of this course
     * @return new DynamicPointsCourse
     */
    public DynamicPointsCourse toDPC() {
        DynamicPointsCourse dpc = new DynamicPointsCourse(courseName, index);
        dpc.setId(this.id);
        dpc.setAssignments(this.mAssignmentArrayList);
        dpc.setNumberOfAssignments(this.numberOfAssignments);
        dpc.setNecPercentToPass(this.necPercentToPass);
        dpc.setDate(this.date);
        return dpc;
    }

    public abstract Double getProgress();

    public abstract boolean hasFixedPoints();

    public double getNecPercentToPass() {
        return necPercentToPass;
    }

    public void setNecPercentToPass(double necPercentToPass) {
        this.necPercentToPass = necPercentToPass;
    }

    /**
     * This method counts the number of extra assignment of this course
     * @return number of extra assignments
     */
    public int countExtraAssignments(){
        int count = 0;
        for(Assignment a : mAssignmentArrayList){
            if(a.isExtraAssignment()) count++;
        }
        return count;
    }

    /**
     * This method gives the number of assignments left
     * @return number of assignments left
     */
    public int numberAssignmentsLeft(){
        return numberOfAssignments - (mAssignmentArrayList.size() - countExtraAssignments());
    }

    public long getDate() {
        return this.date;
    }

    public void setDate(long date){
        this.date = date;
    }

    /**
     * This tells wether "this" was created before or after "otherCourse" chronologically
     * @param otherCourse
     * @return
     */
    public boolean isOutdated(Course otherCourse){
        return this.getDate() < otherCourse.getDate();
    }

    public void updateDate(){
        setDate(Calendar.getInstance().getTimeInMillis());
    }
}
