package database;

/**
 * Created by tak on 1/2/17.
 */

public final class DatabaseVocab {

    //Table names
    public static final String TABLE_COURSES = "courses";
    public static final String TABLE_ASSIGNMENTS = "assignments";
    public static final String TABLE_USER = "user";

    //Column names: common
    public static final String KEY_ID = "id";
    public static final String KEY_DATE = "date";
    public static final String KEY_ASSIGNMENTS = "assignments";

    //Column names: courses
    public static final String KEY_COURSENAME = "course_name";
    public static final String KEY_NUMBER_OF_ASSIGNMENTS = "number_of_assignments";
    public static final String KEY_REACHABLE_POINTS_PER_ASSIGNMENT = "reachable_points_per_assignment";
    public static final String KEY_HAS_FIXED_POINTS = "has_fixed_points";
    public static final String KEY_NEC_PERCENT_TO_PASS = "nec_percent_to_pass";

    //Column names: assignments
    public static final String KEY_ASSIGNMENT_INDEX = "assignment_index";
    public static final String KEY_MAX_POINTS = "max_points";
    public static final String KEY_ACHIEVED_POINTS = "achieved_points";
    public static final String KEY_IS_EXTRA_ASSIGNMENT = "is_extra_assignment";
    public static final String KEY_COURSE_ID = "course_id";


    //Table create statements
    public static final String CREATE_COURSE_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
            + KEY_ID + " TEXT PRIMARY KEY,"
            + KEY_COURSENAME + " TEXT,"
            + KEY_NUMBER_OF_ASSIGNMENTS + " INTEGER,"
            + KEY_REACHABLE_POINTS_PER_ASSIGNMENT + " REAL,"
            + KEY_HAS_FIXED_POINTS + " INTEGER,"
            + KEY_NEC_PERCENT_TO_PASS + " REAL DEFAULT 0.5,"
            + KEY_DATE + " INTEGER DEFAULT 0)";

    public static final String CREATE_ASSIGNMENT_TABLE = "CREATE TABLE " + TABLE_ASSIGNMENTS + "("
            + KEY_ID + " TEXT PRIMARY KEY,"
            + KEY_ASSIGNMENT_INDEX + " INTEGER,"
            + KEY_MAX_POINTS + " REAL,"
            + KEY_IS_EXTRA_ASSIGNMENT + " INTEGER,"
            + KEY_COURSE_ID + " TEXT,"
            + KEY_ACHIEVED_POINTS + " REAL,"
            + KEY_DATE + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + KEY_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + KEY_COURSE_ID + "))";

    public static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " ("
            + KEY_ID + " TEXT,"
            + KEY_COURSE_ID + " TEXT,"
            + "FOREIGN KEY(" + KEY_COURSE_ID + ") REFERENCES " + TABLE_COURSES + "(" + KEY_COURSE_ID + "))";

    public static final String INDEX_COURSE_ID = "idx_course_id";
    public static final String INDEX_ASSIGMENT_ID = "idx_assigment_id";

    public static final String CREATE_UNIQUE_INDEX_COURSE =
            "CREATE UNIQUE INDEX " + INDEX_COURSE_ID + " ON " + TABLE_COURSES + " (" + KEY_ID + ");";
    public static final String CREATE_UNIQUE_INDEX_ASSIGNMENT =
            "CREATE UNIQUE INDEX " + INDEX_ASSIGMENT_ID + " ON " + TABLE_ASSIGNMENTS + " (" + KEY_ID + ");";
    public static final String CREATE_UNIQUE_INDEX_USER_COURSE =
            "CREATE UNIQUE INDEX " + INDEX_ASSIGMENT_ID + " ON " + TABLE_USER + " (" + KEY_ID + ", " + KEY_COURSE_ID + ");";
}
