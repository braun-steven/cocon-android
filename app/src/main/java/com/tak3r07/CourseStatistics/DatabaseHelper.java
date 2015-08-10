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
    private static final int DATABASE_VERSION = 9;

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
    private static final String KEY_MAX_POINTS_COURSE = "reachable_points_per_assignment";
    private static final String KEY_COURSE_INDEX = "course_index";
    private static final String KEY_HAS_FIXED_POINTS = "has_fixed_points";
    private static final String KEY_NEC_PERCENT_TO_PASS = "nec_percent_to_pass";

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
            + KEY_MAX_POINTS_COURSE + " REAL,"
            + KEY_COURSE_INDEX + " INTEGER,"
            + KEY_HAS_FIXED_POINTS + " INTEGER,"
            + KEY_NEC_PERCENT_TO_PASS + " REAL DEFAULT 0.5)";

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
        final String DATABASE_ALTER_COURSE_ADD_NEC_PERCENT_TO_PASS = " ALTER TABLE "
                + TABLE_COURSES + " ADD COLUMN " + KEY_NEC_PERCENT_TO_PASS + " REAL DEFAULT 0.5;";

        //Add necessary percent to pass value to course table
        if (oldVersion < 9) {
            db.execSQL(DATABASE_ALTER_COURSE_ADD_NEC_PERCENT_TO_PASS);
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
                        KEY_MAX_POINTS_COURSE,
                        KEY_COURSE_INDEX,
                        KEY_NEC_PERCENT_TO_PASS
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

        int index = cursor.getInt(4);

        double necPercentToPass = Double.parseDouble(cursor.getString(5));

        Course course;
        //If maxPoints was a "Null-value" in the database, create a DynamicPointsCourse
        if (maxPoints == 0) {
            course = new DynamicPointsCourse(courseName, index);
        } else {
            course = new FixedPointsCourse(courseName, index, maxPoints);
        }
        course.setId(id);
        course.setNumberOfAssignments(numberOfAssignments);
        course.setNecPercentToPass(necPercentToPass);

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

    /**
     * Creates ContentValues which provides the content of a assignment
     *
     * @param assignment The assignment for which the contentValues should be created
     * @return Contentvalues
     */

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

    /**
     * Creates ContentValues which provides the content of a course
     *
     * @param course The course for which the contentValues should be created
     * @return Contentvalues
     */
    private ContentValues getCourseContentValues(Course course) {
        //Put all members of course into contentvalues


        ContentValues values = new ContentValues();
        values.put(KEY_ID, course.getId());
        values.put(KEY_COURSENAME, course.getCourseName());
        values.put(KEY_NUMBER_OF_ASSIGNMENTS, course.getNumberOfAssignments());
        values.put(KEY_COURSE_INDEX, course.getIndex());
        values.put(KEY_NEC_PERCENT_TO_PASS, course.getNecPercentToPass());


        /*
        If course is a "FixedPointsCourse" -> insert course.getMaxPoints
        Else insert "Null-Value"
         */
        double maxPoints;
        if (course.hasFixedPoints()) {
            maxPoints = ((FixedPointsCourse) course).getMaxPoints();
            values.put(KEY_MAX_POINTS_COURSE, maxPoints);
        } else {
            values.putNull(KEY_MAX_POINTS_COURSE);
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
