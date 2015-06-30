package com.tak3r07.CourseStatistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by tak3r07 on 6/16/15.
 */
public class DynamicPointsCourse extends Course{

    static final long serialVersionUID = 2099962292244075360L;

    public DynamicPointsCourse(String courseName, int index) {
        super(courseName, index);

    }

    @Override
    public Double getAverage(boolean extraAssignments) {

        if(getAssignments().size() == 0) return 0d;

        double percentageSum =0;

        for(Assignment assignment : getAssignments()){
            percentageSum += assignment.getPercentage();
        }

        double average = Math.round(percentageSum/getAssignments().size()*10)/10d;
        return average;
    }


    @Override
    public DynamicPointsCourse clone() {
        DynamicPointsCourse clone = new DynamicPointsCourse(getCourseName(), getIndex());
        clone.setAssignments((ArrayList<Assignment>)getAssignments().clone());
        clone.setId(getId());

        return clone;
    }

    @Override
    public Double getTotalPoints() {
        double sum = 0;
        for(Assignment assignment : getAssignments()){
            sum += assignment.getAchievedPoints();
        }
        return sum;
    }

    @Override
    public Double getProgress() {

        if(getAssignments().size() == 0) return 0d;

        //Calculate average max-points for each assignment
        double currentMaxPointsSum =0;
        double achievedPointsAtAll = 0;
        for(Assignment assignment: getAssignments()){
            currentMaxPointsSum += assignment.getMaxPoints();
            achievedPointsAtAll += assignment.getAchievedPoints();
        }

        double maxPointAverage = currentMaxPointsSum/getAssignments().size();
        double calculatedMaxPoints = maxPointAverage * getNumberOfAssignments();

        double progress = Math.round(achievedPointsAtAll / calculatedMaxPoints*1000)/10d;
        return progress;

    }

    @Override
    public boolean hasFixedPoints() {
        return false;
    }
}
