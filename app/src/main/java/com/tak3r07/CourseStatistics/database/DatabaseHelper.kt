package com.tak3r07.CourseStatistics.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.util.ArrayList
import java.util.UUID

import objects.Assignment
import objects.Course
import objects.DynamicPointsCourse
import objects.FixedPointsCourse

import database.DatabaseVocab.CREATE_ASSIGNMENT_TABLE
import database.DatabaseVocab.CREATE_COURSE_TABLE
import database.DatabaseVocab.KEY_ACHIEVED_POINTS
import database.DatabaseVocab.KEY_ASSIGNMENT_INDEX
import database.DatabaseVocab.KEY_COURSENAME
import database.DatabaseVocab.KEY_COURSE_ID
import database.DatabaseVocab.KEY_DATE
import database.DatabaseVocab.KEY_HAS_FIXED_POINTS
import database.DatabaseVocab.KEY_ID
import database.DatabaseVocab.KEY_IS_EXTRA_ASSIGNMENT
import database.DatabaseVocab.KEY_MAX_POINTS
import database.DatabaseVocab.KEY_NEC_PERCENT_TO_PASS
import database.DatabaseVocab.KEY_NUMBER_OF_ASSIGNMENTS
import database.DatabaseVocab.KEY_REACHABLE_POINTS_PER_ASSIGNMENT
import database.DatabaseVocab.TABLE_ASSIGNMENTS
import database.DatabaseVocab.TABLE_COURSES

/**
 * Created by tak3r07 on 5/5/15.
 */
class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_COURSE_TABLE)
        db.execSQL(CREATE_ASSIGNMENT_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val DATABASE_ALTER_COURSE_ADD_NEC_PERCENT_TO_PASS = " ALTER TABLE "
        +TABLE_COURSES + " ADD COLUMN " + KEY_NEC_PERCENT_TO_PASS + " REAL DEFAULT 0.5;"

        //Add necessary percent to pass value to course table
        if (oldVersion < 9) {
            db.execSQL(DATABASE_ALTER_COURSE_ADD_NEC_PERCENT_TO_PASS)
        }

        //Add date to table course and assignment
        val DATABASE_ALTER_COURSE_DATE = "ALTER TABLE " + TABLE_COURSES + " ADD COLUMN " +
                KEY_DATE + " INTEGER DEFAULT 0;"
        val DATABASE_ALTER_ASSIGNMENTS_DATE = "ALTER TABLE " + TABLE_ASSIGNMENTS + " ADD COLUMN " +
                KEY_DATE + " INTEGER DEFAULT 0;"

        if (oldVersion < 10) {
            db.execSQL(DATABASE_ALTER_COURSE_DATE)
            db.execSQL(DATABASE_ALTER_ASSIGNMENTS_DATE)
        }
    }

    //add course
    fun addCourse(course: Course) {
        val db = this.writableDatabase

        //Put all members of course into contentvalues
        val values = getCourseContentValues(course)

        //Insert Row
        db.insert(TABLE_COURSES, null, values)

        //add each assignment
        for (assignment in course.assignments) {
            addAssignment(assignment)
        }

        db.close()
    }

    //add assignment
    fun addAssignment(assignment: Assignment) {
        val db = this.writableDatabase


        //Put all members of assignment into contentvalues
        val values = getAssignmentsContentValues(assignment)

        //Insert Row

        val result = db.insert(TABLE_ASSIGNMENTS, null, values)
        db.close()
    }

    fun getCourse(id: UUID): Course {
        val db = this.readableDatabase

        //search for course with specific id
        val cursor = db.query(TABLE_COURSES,
                arrayOf(KEY_ID, KEY_COURSENAME, KEY_NUMBER_OF_ASSIGNMENTS, KEY_REACHABLE_POINTS_PER_ASSIGNMENT, KEY_NEC_PERCENT_TO_PASS, KEY_DATE, KEY_HAS_FIXED_POINTS),
                KEY_ID + "=?",
                arrayOf(id.toString()), null, null, null, null)

        cursor?.moveToFirst()

        val courseName = cursor!!.getString(1)
        val numberOfAssignments = Integer.parseInt(cursor.getString(2))

        //Get max points
        val maxPoints: Double
        val maxPointsString = cursor.getString(3)
        if (maxPointsString == null) {
            maxPoints = 0.0
        } else {
            maxPoints = java.lang.Double.parseDouble(maxPointsString)
        }
        //get necPercentToPass
        val necPercentToPass = java.lang.Double.parseDouble(cursor.getString(4))

        //get date
        val date = cursor.getLong(5)

        //Get has fixed points (1 == true, 0 == false in sqlite)
        val hasFixedPoints = cursor.getInt(6) == 1

        val course: Course

        //Create specific course instance depending on "hasFixedPoints"
        if (hasFixedPoints) {
            course = FixedPointsCourse(courseName, maxPoints)
        } else {
            course = DynamicPointsCourse(courseName)
        }
        course.id = id
        course.numberOfAssignments = numberOfAssignments
        course.necPercentToPass = necPercentToPass
        course.date = date
        //get assignments
        course.assignments = getAssignmentsOfCourse(id)
        db.close()
        cursor.close()
        return course
    }

    //Restores all assignments of a specific course
    fun getAssignmentsOfCourse(course_id: UUID): ArrayList<Assignment> {
        val db = this.readableDatabase

        val assignments = ArrayList<Assignment>()

        //Query to search for all assignments with the given course_id
        val cursor = db.query(TABLE_ASSIGNMENTS,
                arrayOf(KEY_ID, KEY_ASSIGNMENT_INDEX, KEY_MAX_POINTS, KEY_IS_EXTRA_ASSIGNMENT, KEY_COURSE_ID, KEY_ACHIEVED_POINTS, KEY_DATE),
                KEY_COURSE_ID + "=?",
                arrayOf(course_id.toString()), null, null, KEY_ASSIGNMENT_INDEX, null)

        //Use cursor to get all results from query
        if (cursor.moveToFirst()) {
            do {
                val id = UUID.fromString(cursor.getString(0))
                val index = Integer.parseInt(cursor.getString(1))
                val maxPoints = java.lang.Double.parseDouble(cursor.getString(2))
                val isExtraAssignment = cursor.getInt(3) > 0
                val achievedPoints = java.lang.Double.parseDouble(cursor.getString(5))
                val date = cursor.getLong(6)


                val assignment = Assignment(id, index, maxPoints, achievedPoints, course_id)
                assignment.isExtraAssignment(isExtraAssignment)
                assignment.date = date
                assignments.add(assignment)

            } while (cursor.moveToNext())
        }

        db.close()
        cursor.close()
        return assignments
    }

    //Update everything
    fun updateCourses(courses: List<Course>) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM " + TABLE_COURSES)
        db.execSQL("DELETE FROM " + TABLE_ASSIGNMENTS)

        for (course in courses) {
            addCourse(course)
        }

        db.close()
    }

    //Select all query
    val allCourses: ArrayList<Course>
        get() {
            val courses = ArrayList<Course>()
            val db = this.readableDatabase
            val selectQuery = "SELECT " + KEY_ID
            +" FROM " + TABLE_COURSES
            +" ORDER BY " + KEY_COURSENAME + " ASC"

            val cursor = db.rawQuery(selectQuery, null)

            var id: UUID
            if (cursor.moveToFirst()) {
                do {
                    id = UUID.fromString(cursor.getString(0))

                    val course = getCourse(id)
                    courses.add(course)
                } while (cursor.moveToNext())
            }

            db.close()
            cursor.close()

            return courses
        }

    //update a single assignment
    fun updateAssignment(assignment: Assignment): Int {
        val db = this.writableDatabase
        val values = getAssignmentsContentValues(assignment)


        return db.update(TABLE_ASSIGNMENTS, values, KEY_ID + " = ?", arrayOf(assignment.id.toString()))
    }

    /**
     * Creates ContentValues which provides the content of a assignment

     * @param assignment The assignment for which the contentValues should be created
     * *
     * @return Contentvalues
     */

    private fun getAssignmentsContentValues(assignment: Assignment): ContentValues {
        //Put all members of assignment into contentvalues
        val values = ContentValues()
        values.put(KEY_ID, assignment.id.toString())
        values.put(KEY_ASSIGNMENT_INDEX, assignment.index)
        values.put(KEY_MAX_POINTS, assignment.maxPoints)
        values.put(KEY_ACHIEVED_POINTS, assignment.achievedPoints)
        values.put(KEY_IS_EXTRA_ASSIGNMENT, assignment.isExtraAssignment)
        values.put(KEY_COURSE_ID, assignment.course_id.toString())
        values.put(KEY_DATE, assignment.date)
        return values
    }

    //update a single course
    fun updateCourse(course: Course): Int {
        val db = this.writableDatabase
        val values = getCourseContentValues(course)

        //Update all assignments
        for (a in course.assignments) {
            updateAssignment(a)
        }

        return db.update(TABLE_COURSES, values, KEY_ID + " = ?", arrayOf(course.id.toString()))
    }

    /**
     * Creates ContentValues which provides the content of a course

     * @param course The course for which the contentValues should be created
     * *
     * @return Contentvalues
     */
    private fun getCourseContentValues(course: Course): ContentValues {
        //Put all members of course into contentvalues


        val values = ContentValues()
        values.put(KEY_ID, course.id.toString())
        values.put(KEY_COURSENAME, course.courseName)
        values.put(KEY_NUMBER_OF_ASSIGNMENTS, course.numberOfAssignments)
        values.put(KEY_NEC_PERCENT_TO_PASS, course.necPercentToPass)
        values.put(KEY_DATE, course.date)
        values.put(KEY_HAS_FIXED_POINTS, course.hasFixedPoints())

        /*
        If course is a "FixedPointsCourse" -> insert course.getMaxPoints
        Else insert "Null-Value"
         */
        val maxPoints: Double
        if (course.hasFixedPoints()) {
            maxPoints = (course as FixedPointsCourse).maxPoints!!
            values.put(KEY_REACHABLE_POINTS_PER_ASSIGNMENT, maxPoints)
        } else {
            values.putNull(KEY_REACHABLE_POINTS_PER_ASSIGNMENT)
        }

        return values
    }


    /**
     * Delete specific course

     * @param course which is to be deleted
     * *
     * @return result whether delete has failed(false) or not (true)
     */
    fun deleteCourse(course: Course): Boolean {
        val db = this.writableDatabase
        val result = db.delete(
                TABLE_COURSES,
                KEY_ID + " = ?",
                arrayOf(course.id.toString()))
        db.close()
        return result > 0
    }

    /**
     * Delete a specific assignment

     * @param assignment which is to be deleted
     * *
     * @return result whether delete has failed(false) or not(true)
     */
    fun deleteAssignment(assignment: Assignment): Boolean {
        val db = this.writableDatabase
        val result = db.delete(
                TABLE_ASSIGNMENTS,
                KEY_ID + " = ?",
                arrayOf(assignment.id.toString()))
        db.close()

        return result > 0
    }

    /**
     * Reads the user uuid

     * @return User uuid
     */
    val userUUID: UUID?
        get() {

            val FILENAME = "user.uuid"

            val string = UUID.randomUUID().toString()
            val file = context.getFileStreamPath(FILENAME)
            if (!file.exists()) {
                try {

                    val fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)
                    fos.write(string.toByteArray())
                    fos.close()
                    return UUID.fromString(string)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            try {
                val br = BufferedReader(FileReader(file))
                val uuidString = br.readLine()
                return UUID.fromString(uuidString)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

    companion object {

        //Logcat tag
        private val LOG = "DatabaseHelper"

        //Database version
        private val DATABASE_VERSION = 10

        //Database name
        private val DATABASE_NAME = "courses"
    }

}
