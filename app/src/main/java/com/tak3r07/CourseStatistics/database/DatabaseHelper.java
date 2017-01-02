package com.tak3r07.CourseStatistics.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tak3r07.CourseStatistics.objects.Assignment;
import com.tak3r07.CourseStatistics.objects.Course;
import com.tak3r07.CourseStatistics.objects.DynamicPointsCourse;
import com.tak3r07.CourseStatistics.objects.FixedPointsCourse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.tak3r07.CourseStatistics.database.DatabaseVocab.*;

/**
 * Created by tak3r07 on 5/5/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //Logcat tag
    private static final String LOG = "DatabaseHelper";

    //Database version
    private static final int DATABASE_VERSION = 10;

    //Database name
    private static final String DATABASE_NAME = "courses";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COURSE_TABLE);
        db.execSQL(CREATE_ASSIGNMENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String DATABASE_ALTER_COURSE_ADD_NEC_PERCENT_TO_PASS = " ALTER TABLE "
                + TABLE_COURSES + " ADD COLUMN " + KEY_NEC_PERCENT_TO_PASS + " REAL DEFAULT 0.5;";

        //Add necessary percent to pass value to course table
        if (oldVersion < 9) {
            db.execSQL(DATABASE_ALTER_COURSE_ADD_NEC_PERCENT_TO_PASS);
        }

        //Add date to table course and assignment
        final String DATABASE_ALTER_COURSE_DATE =
                "ALTER TABLE " + TABLE_COURSES + " ADD COLUMN " +
                        KEY_DATE + " INTEGER DEFAULT 0;";
        final String DATABASE_ALTER_ASSIGNMENTS_DATE =
                "ALTER TABLE " + TABLE_ASSIGNMENTS + " ADD COLUMN " +
                        KEY_DATE + " INTEGER DEFAULT 0;";

        if (oldVersion < 10) {
            db.execSQL(DATABASE_ALTER_COURSE_DATE);
            db.execSQL(DATABASE_ALTER_ASSIGNMENTS_DATE);
        }
    }

    //add course
    public void addCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Put all members of course into contentvalues
        ContentValues values = getCourseContentValues(course);

        //Insert Row
        db.insert(TABLE_COURSES, null, values);

        //add each assignment
        for (Assignment assignment : course.getAssignments()) {
            addAssignment(assignment);
        }

        db.close();
    }

    //add assignment
    public void addAssignment(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();


        //Put all members of assignment into contentvalues
        ContentValues values = getAssignmentsContentValues(assignment);

        //Insert Row

        long result = db.insert(TABLE_ASSIGNMENTS, null, values);
        db.close();
    }

    public Course getCourse(UUID id) {
        SQLiteDatabase db = this.getReadableDatabase();

        //search for course with specific id
        Cursor cursor = db.query(TABLE_COURSES,
                new String[]{
                        KEY_ID,
                        KEY_COURSENAME,
                        KEY_NUMBER_OF_ASSIGNMENTS,
                        KEY_REACHABLE_POINTS_PER_ASSIGNMENT,
                        KEY_NEC_PERCENT_TO_PASS,
                        KEY_DATE,
                        KEY_HAS_FIXED_POINTS
                },
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        String courseName = cursor.getString(1);
        int numberOfAssignments = Integer.parseInt(cursor.getString(2));

        //Get max points
        double maxPoints;
        String maxPointsString = cursor.getString(3);
        if (maxPointsString == null) {
            maxPoints = 0;
        } else {
            maxPoints = Double.parseDouble(maxPointsString);
        }
        //get index
        int index = cursor.getInt(4);

        //get necPercentToPass
        double necPercentToPass = Double.parseDouble(cursor.getString(5));

        //get date
        long date = cursor.getLong(6);

        //Get has fixed points (1 == true, 0 == false in sqlite)
        boolean hasFixedPoints = cursor.getInt(7) == 1;

        Course course;

        //Create specific course instance depending on "hasFixedPoints"
        if (hasFixedPoints) {
            course = new FixedPointsCourse(courseName, maxPoints);
        } else {
            course = new DynamicPointsCourse(courseName);
        }
        course.setId(id);
        course.setNumberOfAssignments(numberOfAssignments);
        course.setNecPercentToPass(necPercentToPass);
        course.setDate(date);
        //get assignments
        course.setAssignments(getAssignmentsOfCourse(id));
        db.close();
        cursor.close();
        return course;
    }

    //Restores all assignments of a specific course
    public ArrayList<Assignment> getAssignmentsOfCourse(UUID course_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Assignment> assignments = new ArrayList<>();

        //Query to search for all assignments with the given course_id
        Cursor cursor = db.query(TABLE_ASSIGNMENTS,
                new String[]{
                        KEY_ID,
                        KEY_ASSIGNMENT_INDEX,
                        KEY_MAX_POINTS,
                        KEY_IS_EXTRA_ASSIGNMENT,
                        KEY_COURSE_ID,
                        KEY_ACHIEVED_POINTS,
                        KEY_DATE
                },
                KEY_COURSE_ID + "=?",
                new String[]{String.valueOf(course_id)},
                null, null, KEY_ASSIGNMENT_INDEX, null);

        //Use cursor to get all results from query
        if (cursor.moveToFirst()) {
            do {
                UUID id = UUID.fromString(cursor.getString(0));
                int index = Integer.parseInt(cursor.getString(1));
                double maxPoints = Double.parseDouble(cursor.getString(2));
                boolean isExtraAssignment = cursor.getInt(3) > 0;
                double achievedPoints = Double.parseDouble(cursor.getString(5));
                long date = cursor.getLong(6);


                Assignment assignment = new Assignment(id, index, maxPoints, achievedPoints, course_id);
                assignment.isExtraAssignment(isExtraAssignment);
                assignment.setDate(date);
                assignments.add(assignment);

            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();
        return assignments;
    }

    //Update everything
    public void updateCourses(List<Course> courses) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_COURSES);
        db.execSQL("DELETE FROM " + TABLE_ASSIGNMENTS);

        for (Course course : courses) {
            addCourse(course);
        }

        db.close();
    }

    public ArrayList<Course> getAllCourses() {
        ArrayList<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        //Select all query
        String selectQuery =
                "SELECT " + KEY_ID
                        + " FROM " + TABLE_COURSES
                        + " ORDER BY " + KEY_COURSENAME + " ASC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        UUID id;
        if (cursor.moveToFirst()) {
            do {
                id = UUID.fromString(cursor.getString(0));

                Course course = getCourse(id);
                courses.add(course);
            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return courses;
    }

    //update a single assignment
    public int updateAssignment(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = getAssignmentsContentValues(assignment);


        return db.update(TABLE_ASSIGNMENTS, values, KEY_ID + " = ?", new String[]{String.valueOf(assignment.getId())});
    }

    /**
     * Creates ContentValues which provides the content of a assignment
     *
     * @param assignment The assignment for which the contentValues should be created
     * @return Contentvalues
     */

    private ContentValues getAssignmentsContentValues(Assignment assignment) {
        //Put all members of assignment into contentvalues
        ContentValues values = new ContentValues();
        values.put(KEY_ID, assignment.getId().toString());
        values.put(KEY_ASSIGNMENT_INDEX, assignment.getIndex());
        values.put(KEY_MAX_POINTS, assignment.getMaxPoints());
        values.put(KEY_ACHIEVED_POINTS, assignment.getAchievedPoints());
        values.put(KEY_IS_EXTRA_ASSIGNMENT, assignment.isExtraAssignment());
        values.put(KEY_COURSE_ID, assignment.getCourse_id().toString());
        values.put(KEY_DATE, assignment.getDate());
        return values;
    }

    //update a single course
    public int updateCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = getCourseContentValues(course);

        //Update all assignments
        for (Assignment a : course.getAssignments()) {
            updateAssignment(a);
        }

        return db.update(TABLE_COURSES, values, KEY_ID + " = ?", new String[]{String.valueOf(course.getId())});
    }

    /**
     * Creates ContentValues which provides the content of a course
     *
     * @param course The course for which the contentValues should be created
     * @return Contentvalues
     */
    private ContentValues getCourseContentValues(Course course) {
        //Put all members of course into contentvalues


        ContentValues values = new ContentValues();
        values.put(KEY_ID, course.getId().toString());
        values.put(KEY_COURSENAME, course.getCourseName());
        values.put(KEY_NUMBER_OF_ASSIGNMENTS, course.getNumberOfAssignments());
        values.put(KEY_NEC_PERCENT_TO_PASS, course.getNecPercentToPass());
        values.put(KEY_DATE, course.getDate());
        values.put(KEY_HAS_FIXED_POINTS, course.hasFixedPoints());

        /*
        If course is a "FixedPointsCourse" -> insert course.getMaxPoints
        Else insert "Null-Value"
         */
        double maxPoints;
        if (course.hasFixedPoints()) {
            maxPoints = ((FixedPointsCourse) course).getMaxPoints();
            values.put(KEY_REACHABLE_POINTS_PER_ASSIGNMENT, maxPoints);
        } else {
            values.putNull(KEY_REACHABLE_POINTS_PER_ASSIGNMENT);
        }

        return values;
    }


    /**
     * Delete specific course
     *
     * @param course which is to be deleted
     * @return result whether delete has failed(false) or not (true)
     */
    public boolean deleteCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(
                TABLE_COURSES,
                KEY_ID + " = ?",
                new String[]{String.valueOf(course.getId())});
        db.close();
        return result > 0;
    }

    /**
     * Delete a specific assignment
     *
     * @param assignment which is to be deleted
     * @return result wether delete has failed(false) or not(true)
     */
    public boolean deleteAssignment(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(
                TABLE_ASSIGNMENTS,
                KEY_ID + " = ?",
                new String[]{String.valueOf(assignment.getId())});
        db.close();

        return result > 0;
    }
}
