package com.tak3r07.CourseStatistics.database;

/**
 * Created by tak on 1/2/17.
 */

public final class DatabaseVocab {

    //Table names
    protected  static final String TABLE_COURSES = "courses";
    protected  static final String TABLE_ASSIGNMENTS = "assignments";

    //Column names: common
    protected static final String KEY_ID = "id";
    protected  static final String KEY_DATE = "date";
    protected  static final String KEY_ASSIGNMENTS = "assignments";

    //Column names: courses
    protected  static final String KEY_COURSENAME = "course_name";
    protected  static final String KEY_NUMBER_OF_ASSIGNMENTS = "number_of_assignments";
    protected  static final String KEY_REACHABLE_POINTS_PER_ASSIGNMENT = "reachable_points_per_assignment";
    protected  static final String KEY_COURSE_INDEX = "course_index";
    protected  static final String KEY_HAS_FIXED_POINTS = "has_fixed_points";
    protected  static final String KEY_NEC_PERCENT_TO_PASS = "nec_percent_to_pass";

    //Column names: assignments
    protected  static final String KEY_ASSIGNMENT_INDEX = "assignment_index";
    protected  static final String KEY_MAX_POINTS = "max_points";
    protected  static final String KEY_ACHIEVED_POINTS = "achieved_points";
    protected  static final String KEY_IS_EXTRA_ASSIGNMENT = "is_extra_assignment";
    protected  static final String KEY_COURSE_ID = "course_id";



    //Table create statements
    protected  static final String CREATE_COURSE_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
            + KEY_ID + " TEXT PRIMARY KEY,"
            + KEY_COURSENAME + " TEXT,"
            + KEY_NUMBER_OF_ASSIGNMENTS + " INTEGER,"
            + KEY_REACHABLE_POINTS_PER_ASSIGNMENT + " REAL,"
            + KEY_COURSE_INDEX + " INTEGER,"
            + KEY_HAS_FIXED_POINTS + " INTEGER,"
            + KEY_NEC_PERCENT_TO_PASS + " REAL DEFAULT 0.5,"
            + KEY_DATE + " INTEGER DEFAULT 0)";

    protected  static final String CREATE_ASSIGNMENT_TABLE = "CREATE TABLE " + TABLE_ASSIGNMENTS + "("
            + KEY_ID + " TEXT PRIMARY KEY,"
            + KEY_ASSIGNMENT_INDEX + " INTEGER,"
            + KEY_MAX_POINTS + " REAL,"
            + KEY_IS_EXTRA_ASSIGNMENT + " INTEGER,"
            + KEY_COURSE_ID + " TEXT,"
            + KEY_ACHIEVED_POINTS + " REAL,"
            + KEY_DATE + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + KEY_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + KEY_COURSE_ID + "))";

}
