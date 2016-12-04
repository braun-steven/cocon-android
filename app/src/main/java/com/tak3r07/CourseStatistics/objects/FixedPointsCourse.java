package com.tak3r07.CourseStatistics.objects;

import java.util.ArrayList;

/**
 * Created by tak3r07 on 6/15/15.
 */
public class FixedPointsCourse extends Course {

    static final long serialVersionUID = 2099962292244075360L;


    private double maxPoints;


    public FixedPointsCourse(String courseName, int index, double maxPoints) {
        super(courseName, index);

        //Set maxPoints
        this.maxPoints = maxPoints;
    }


    //Calculate average of current assignments
    public Double getAverage(boolean extraAssignments) {
        double overAllAchievedPoints = 0;
        double overAllMaxPoints = 0;

        if (extraAssignments) {
            //Iterate on the array and sum up all its max and achieved points
            Assignment currentAssignment;
            for (Assignment assignment : getAssignments()) {
                currentAssignment = assignment;
                overAllAchievedPoints += currentAssignment.getAchievedPoints();
                overAllMaxPoints += currentAssignment.getMaxPoints();
            }
        } else {
            //Iterate on the array and sum up all its max and achieved points excluding the extra assignments
            Assignment currentAssignment;
            for (Assignment assignment : getAssignments()) {
                currentAssignment = assignment;
                if (currentAssignment.isExtraAssignment()) continue;
                overAllAchievedPoints += currentAssignment.getAchievedPoints();
                overAllMaxPoints += currentAssignment.getMaxPoints();
            }
        }
        //Round on 4 digits
        double average = Math.round(overAllAchievedPoints / overAllMaxPoints * 1000) / 10d;


        //return result
        return average;

    }

    /**
     * calculate progress :
     * (all points of current assignments)
     * /
     * ((max points per assignment) * (number of assignments))
     */
    public Double getProgress() {
        double overAllAchievedPoints = 0;

        //Iterate on the array and sum up all its max and achieved points

        for (Assignment assignment : getAssignments()) {

            overAllAchievedPoints += assignment.getAchievedPoints();

        }

        //Round on 4 digits

        double progress =
                Math.round(
                        overAllAchievedPoints / (maxPoints * getNumberOfAssignments())
                                * 1000)
                        / 10d;

        //return result
        return progress;
    }

    public Double getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(Double maxPoints) {
        this.maxPoints = maxPoints;

        //Set in each Assignment
        Assignment currentAssignment;
        for (Assignment assignment : getAssignments()) {

            //Do not update extraassignments (those max-points are 0)
            if (!assignment.isExtraAssignment()) {
                assignment.setMaxPoints(maxPoints);
            }
        }
    }

    /**
     * Simple Clone code (deep copy)
     */
    public Course clone() {
        FixedPointsCourse clone = new FixedPointsCourse(getCourseName(), getIndex(), getMaxPoints());
        clone.setMaxPoints(this.maxPoints);
        clone.setNumberOfAssignments(getNumberOfAssignments());

        ArrayList<Assignment> cloneList = new ArrayList<Assignment>();
        for (Assignment a : getAssignments()) {
            cloneList.add(a);
        }

        return clone;
    }

    /**
     * Return necessary points per assignment until 50% reached
     */
    public Double getNecessaryPointsPerAssignmentUntilFin() {

        //Necessary points for the whole course to reach 50%
        Double necPointsAtAll = getNumberOfAssignments() * maxPoints * getNecPercentToPass();

        //yet achieved points
        Double achievedPointsAtAll = getTotalPoints();

        //Number of assignments left for this semester
        int numberAssignmentsLeft = numberAssignmentsLeft();

        //if numberAssignmentsLeft = 0 use set it to "1" since this will display in the result
        // the points overall which are left
        numberAssignmentsLeft = (numberAssignmentsLeft > 0)? numberAssignmentsLeft : 1;

        //Necessary points left to pass the course
        Double numberOfPointsLeft = necPointsAtAll - achievedPointsAtAll;

        //Missing points divided by missing assignments
        Double necPointsPerAssUntilFin = Math.round(numberOfPointsLeft / numberAssignmentsLeft * 100) / 100d;



        if (necPointsPerAssUntilFin < 0) return 0.;

        return necPointsPerAssUntilFin;

    }

    /**
     * Return the number of assignments which are necessary until
     * one would reach 50% with its current performance per assignment
     */
    public int getNumberOfAssUntilFin() {

        //Necessary points for the whole course to reach 50%
        Double necPointsAtAll = getNumberOfAssignments() * maxPoints * 0.5;

        //Yet achieved points
        Double achievedPointsAtAll = getTotalPoints();

        //Average points per assignment (false will exclude the extra assignments)
        Double averagePointsPerAssignment = getAveragePointsPerAssignment(false);

        //Scenario: Course has been initialized for the first time
        if (averagePointsPerAssignment == 0) return 0;

        //Predicted points if you get your average points in all your next assignments
        Double predictedPoints = achievedPointsAtAll;

        //Counts the number of assignments
        int count = 0;

        //Each loop adds the averagepoints per assignment so it predicts your future results
        while (predictedPoints < necPointsAtAll) {
            count++;
            predictedPoints += averagePointsPerAssignment;
        }

        return count;
    }

    /**
     * Returns Average Points per Assignment
     */
    public Double getAveragePointsPerAssignment(boolean extraAssignments) {
        return Math.round(
                getAverage(extraAssignments) * getMaxPoints() * 1)
                / 100d;
    }


    @Override
    public boolean hasFixedPoints() {
        return true;
    }
}
