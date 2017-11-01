package com.tak3r07.CourseStatistics.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.tak3r07.CourseStatistics.activities.AssignmentsActivity
import com.tak3r07.CourseStatistics.activities.MainActivity
import com.tak3r07.CourseStatistics.database.DataHelper
import com.tak3r07.unihelper.R

import java.util.ArrayList

import objects.Course

/**
 * Created by tak3r07 on 12/8/14.
 * Adapter for RecyclerView in MainActivity
 */
class RecyclerViewCourseAdapter(activity: MainActivity, private val context: Context, private var mCourseArrayList: ArrayList<Course>) : RecyclerView.Adapter<RecyclerViewCourseAdapter.ViewHolder>() {
    private val dataHelper: DataHelper<MainActivity>

    init {
        dataHelper = DataHelper(activity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Inflate layout
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_course_altern,
                        parent,
                        false)

        return ViewHolder(itemView, context, this)
    }


    //Sets up the view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = mCourseArrayList[position]
        //Set text
        holder.mTextViewName.text = course.courseName
        holder.mTextViewProgress.text = course.getAverage(true)!!.toString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        holder.mTextViewAverage.text = course.progress!!.toString() + " %"

        //Set OverAllPercentage background Color
        holder.mTextViewProgress.background = getDrawableColorForPercentage(course.getAverage(true))

    }

    override fun getItemCount(): Int {
        return mCourseArrayList.size
    }

    /**
     * This method receives a percentage and retrieves a color
     */
    fun getDrawableColorForPercentage(percentage: Double): Drawable {

        if (percentage < 30)
            return context.resources.getDrawable(R.drawable.circular_shape_red)
        if (percentage < 40)
            return context.resources.getDrawable(R.drawable.circular_shape_deep_orange)
        if (percentage < 55)
            return context.resources.getDrawable(R.drawable.circular_shape_orange)
        if (percentage < 65)
            return context.resources.getDrawable(R.drawable.circular_shape_yellow)
        if (percentage < 80)
            return context.resources.getDrawable(R.drawable.circular_shape_lime)
        if (percentage >= 80)
            return context.resources.getDrawable(R.drawable.circular_shape_green)

        return context.resources.getDrawable(R.drawable.circular_shape_red)
    }

    //Adds Course
    fun addCourse(course: Course?) {
        if (course != null) {
            mCourseArrayList!!.add(course)
            //Doesnt work atm
            //notifyItemInserted(mCourseArrayList.size());
            notifyDataSetChanged()

            //save in data

            dataHelper.addCourse(course)
        }
    }

    /**
     * Removes the course at 'position' from the internal arraylist and from the database
     * and animates the removal in recyclerview

     * @param position Position of course which is to be removed
     */
    fun removeCourse(position: Int) {
        //Log removal
        Log.d("Tak3r07", "Removing at pos: " + position + ", size: " + mCourseArrayList!!.size)
        if (position >= 0) {
            //Delete from database
            dataHelper.deleteCourse(mCourseArrayList!![position])

            //Delete from internal arraylist
            mCourseArrayList!!.removeAt(position)

            //Animate
            notifyItemRemoved(position)

        } else {
            //Log error
            Log.e("Tak3r07", "position = $position < 0")
        }
    }

    fun getmCourseArrayList(): ArrayList<Course> {
        return mCourseArrayList
    }

    fun setmCourseArrayList(mCourseArrayList: ArrayList<Course>) {
        this.mCourseArrayList = mCourseArrayList
    }

    class ViewHolder//Holds views
    (itemView: View,
            //Context to refer to app context (for intent, dialog etc)
     internal var context: Context,
            //Adapter to notifiy data set changed
     internal var mCourseAdapter: RecyclerViewCourseAdapter) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        //Initialize views in Viewholder
        internal var mTextViewName: TextView
        internal var mTextViewProgress: TextView
        //ImageView mImageView;
        internal var mTextViewAverage: TextView

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            mTextViewName = itemView.findViewById<View>(R.id.course_textview_name) as TextView
            mTextViewProgress = itemView.findViewById<View>(R.id.course_letter) as TextView
            mTextViewAverage = itemView.findViewById<View>(R.id.end_percentage_textview) as TextView
        }


        /*
        OnClick: Course at the specific position shall be opened
         */
        override fun onClick(v: View) {

            val course = mCourseAdapter.getmCourseArrayList()[layoutPosition]

            //Setup new Intent
            val intent = Intent()
            intent.setClass(context, AssignmentsActivity::class.java)

            //add course position to update assignments when result comes back
            intent.putExtra("COURSE_TAG_ID",
                    course.id.toString()
            )

            //Start activity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        override fun onLongClick(v: View): Boolean {
            //Open Alert dialog to delete item

            val alert = AlertDialog.Builder(v.context)

            //Set title and message
            alert.setTitle(context.getString(R.string.delete))
            alert.setMessage(context.getString(R.string.do_you_want_to_delete) + mTextViewName.text.toString() + "?")

            //Set positive button behaviour
            alert.setPositiveButton(context.getString(R.string.delete)) { dialog, whichButton ->
                //Delete course and notify
                Toast.makeText(
                        context,
                        mTextViewName.text.toString() + context.getString(R.string.deleted),
                        Toast.LENGTH_LONG).show()


                //Remove Course
                mCourseAdapter.removeCourse(position)
            }

            alert.setNegativeButton(context.getString(R.string.cancel)) { dialog, whichButton ->
                // Canceled.
            }

            alert.show()


            return true
        }


    }

    companion object {

        /**
         * Returns correct item position since first item is header

         * @return item positon
         */
        fun getItemPosition(position: Int): Int {
            return position - 1
        }
    }
}
