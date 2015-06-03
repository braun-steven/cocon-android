package com.tak3r07.CourseStatistics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tak3r07 on 5/5/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //Logcat tag
    private static final String LOG = "DatabaseHelper";

    //Database version
    private static final int DATABASE_VERSION = 7;

    //Database name
    private static final String DATABASE_NAME = "courses";

    //Table names
    private static final String TABLE_COURSES = "courses";
    private static final String TABLE_ASSIGNMENTS = "assignments";

    //Column names: common
    private static final String KEY_ID = "id";

    //Column names: courses
    private static final String KEY_COURSENAME = "course_name";
    private static final String KEY_NUMBER_OF_ASSIGNMENTS = "number_of_assignments";
    private static final String KEY_REACHABLE_POINTS_PER_ASSIGNMENT = "reachable_points_per_assignment";
    private static final String KEY_COURSE_INDEX = "course_index";
    private static final String KEY_HAS_FIXED_POINTS = "has_fixed_points";

    //Column names: assignments
    private static final String KEY_INDEX = "assignment_index";
    private static final String KEY_MAX_POINTS = "max_points";
    private static final String KEY_ACHIEVED_POINTS = "achieved_points";
    private static final String KEY_IS_EXTRA_ASSIGNMENT = "is_extra_assignment";
    private static final String KEY_COURSE_ID = "course_id";


    //Table create statements
    private static final String CREATE_COURSE_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_COURSENAME + " TEXT,"
            + KEY_NUMBER_OF_ASSIGNMENTS + " INTEGER,"
            + KEY_REACHABLE_POINTS_PER_ASSIGNMENT + " REAL,"
            + KEY_COURSE_INDEX + " INTEGER,"
            + KEY_HAS_FIXED_POINTS + " INTEGER)";

    private static final String CREATE_ASSIGNMENT_TABLE = "CREATE TABLE " + TABLE_ASSIGNMENTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_INDEX + " INTEGER,"
            + KEY_MAX_POINTS + " REAL,"
            + KEY_IS_EXTRA_ASSIGNMENT + " INTEGER,"
            + KEY_COURSE_ID + " INTEGER,"
            + KEY_ACHIEVED_POINTS + " REAL)";

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

        /*
        Each case 'n' describes an upgrade from db-version 'n' to 'n+1'
         */
        while(oldVersion < newVersion) {
            switch (oldVersion) {
                case 6:
                    //Upgrade from 6 to 7
                    db.execSQL("ALTER TABLE " + TABLE_COURSES
                            + "ADD " + KEY_HAS_FIXED_POINTS + " INTEGER");
                    break;
            }
            oldVersion++;
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

    public Course getCourse(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        //search for course with specific id
        Cursor cursor = db.query(TABLE_COURSES,
                new String[]{
                        KEY_ID,
                        KEY_COURSENAME,
                        KEY_NUMBER_OF_ASSIGNMENTS,
                        KEY_REACHABLE_POINTS_PER_ASSIGNMENT,
                        KEY_COURSE_INDEX,
                        KEY_HAS_FIXED_POINTS
                },
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        Course course = new Course(cursor.getString(1), cursor.getInt(4));
        course.setId(id);
        course.setNumberOfAssignments(Integer.parseInt(cursor.getString(2)));
        course.setReachablePointsPerAssignment(Double.parseDouble(cursor.getString(3)));
        course.hasFixedPoints(Boolean.parseBoolean(cursor.getString(5)));

        //get assignments
        course.setAssignments(getAssignmentsOfCourse(id));
        db.close();
        cursor.close();
        return course;
    }

    //Restores all assignments of a specific course
    public ArrayList<Assignment> getAssignmentsOfCourse(int course_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Assignment> assignments = new ArrayList<>();

        //Query to search for all assignments with the given course_id
        Cursor cursor = db.query(TABLE_ASSIGNMENTS,
                new String[]{
                        KEY_ID,
                        KEY_INDEX,
                        KEY_MAX_POINTS,
                        KEY_IS_EXTRA_ASSIGNMENT,
                        KEY_COURSE_ID,
                        KEY_ACHIEVED_POINTS
                },
                KEY_COURSE_ID + "=?",
                new String[]{String.valueOf(course_id)},
                null, null, KEY_INDEX, null);

        //Use cursor to get all results from query
        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                int index = Integer.parseInt(cursor.getString(1));
                double maxPoints = Double.parseDouble(cursor.getString(2));
                double achievedPoints = Double.parseDouble(cursor.getString(5));

                Assignment assignment = new Assignment(id, index, maxPoints, achievedPoints, course_id);
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
                        + " ORDER BY " + KEY_COURSE_INDEX + " ASC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        int id;
        if (cursor.moveToFirst()) {
            do {
                id = Integer.parseInt(cursor.getString(0));

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

    private ContentValues getAssignmentsContentValues(Assignment assignment) {
        //Put all members of assignment into contentvalues
        ContentValues values = new ContentValues();
        values.put(KEY_ID, assignment.getId());
        values.put(KEY_INDEX, assignment.getIndex());
        values.put(KEY_MAX_POINTS, assignment.getMaxPoints());
        values.put(KEY_ACHIEVED_POINTS, assignment.getAchievedPoints());
        values.put(KEY_IS_EXTRA_ASSIGNMENT, assignment.isExtraAssignment());
        values.put(KEY_COURSE_ID, assignment.getCourse_id());
        return values;
    }

    //update a single course
    public int updateCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = getCourseContentValues(course);


        return db.update(TABLE_COURSES, values, KEY_ID + " = ?", new String[]{String.valueOf(course.getId())});
    }

    private ContentValues getCourseContentValues(Course course) {
        //Put all members of course into contentvalues
        ContentValues values = new ContentValues();
        values.put(KEY_ID, course.getId());
        values.put(KEY_COURSENAME, course.getCourseName());
        values.put(KEY_NUMBER_OF_ASSIGNMENTS, course.getNumberOfAssignments());
        values.put(KEY_REACHABLE_POINTS_PER_ASSIGNMENT, course.getReachablePointsPerAssignment());
        values.put(KEY_COURSE_INDEX, course.getIndex());
        values.put(KEY_HAS_FIXED_POINTS, course.hasFixedPoints());
        return values;
    }

    //Delete Course
    public void deleteCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COURSES, KEY_ID + " = ?", new String[]{String.valueOf(course.getId())});
        db.close();
    }

    /**
     * Delete a specific assignment
     *
     * @param assignment which is to be deleted
     * @return result wether delete has failed(false) or not(true)
     */
    public boolean deleteAssignment(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_ASSIGNMENTS, KEY_ID + " = ?", new String[]{String.valueOf(assignment.getId())});
        db.close();

        return result > 0;
    }
}
