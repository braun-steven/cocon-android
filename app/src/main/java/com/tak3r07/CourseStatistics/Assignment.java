package com.tak3r07.CourseStatistics;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created by tak3r07 on 11/9/14.
 */
public class Assignment implements Serializable {

    String TAG = "UEBUNGSPUNKTE-Log";
    static final long serialVersionUID = 62872256170663659L;

    //Index of the Assignment
    private int index;

    // Maximal reachable points of this assignment
    private double maxPoints;

    //Achieved number of points of this assignment
    private double achievedPoints;


    //Boolean whether this is an extra-assignment or not
    private boolean isExtraAssignment = false;

    //Constructor
    public Assignment(int index, double maxPoints, double achievedPoints) {
        setIndex(index);
        setMaxPoints(maxPoints);
        setAchievedPoints(achievedPoints);
    }

    //Calculates and return the achieved percentage of max points of this assignment
    public Double getPercentage() {
        Double percentage = 0d;
        //Round on 4 digits
        if (maxPoints != 0) {
            percentage = Math.round(achievedPoints / maxPoints * 1000) / 10d;
        } else {
            return percentage;
        }

        return percentage;


    }


    public double getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(double maxPoints) {
        this.maxPoints = maxPoints;
    }

    public double getAchievedPoints() {
        return achievedPoints;
    }

    public void setAchievedPoints(double achievedPoints) {
        this.achievedPoints = achievedPoints;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isExtraAssignment() {
        return isExtraAssignment;
    }

    public void setExtraAssignment(boolean isExtraAssignment) {

        this.isExtraAssignment = isExtraAssignment;
        this.maxPoints = 0d;
    }

    public void setExtraAssignment(boolean isExtraAssignment, double maxPoints){
        this.isExtraAssignment = isExtraAssignment;
        this.maxPoints = maxPoints;
    }
}
