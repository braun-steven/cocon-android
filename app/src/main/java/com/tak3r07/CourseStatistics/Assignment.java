package com.tak3r07.CourseStatistics;

import java.io.Serializable;

/**
 * Created by tak3r07 on 11/9/14.
 */
public class Assignment implements Serializable {

    String TAG = "UEBUNGSPUNKTE-Log";

    //Index of the Assignment
    int index;

    // Maximal reachable points of this assignment
    double maxPoints;

    //Achieved number of points of this assignment
    double achievedPoints;

    //Constructor
    public Assignment(int index, double maxPoints, double achievedPoints) {
        setIndex(index);
        setMaxPoints(maxPoints);
        setAchievedPoints(achievedPoints);
    }

    //Calculates and return the achieved percentage of max points of this assignment
    public Double getPercentage() {
        try {
            //Round on 4 digits
            Double percentage = Math.round(achievedPoints / maxPoints * 1000) / 10d;

            return percentage;

        } catch (Exception e) {

            e.printStackTrace();
            return 0.;
        }

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
}
