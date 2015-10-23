package com.tak3r07.CourseStatistics;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tak3r07.unihelper.R;

import java.util.ArrayList;

/**
 * Created by tak3r07 on 12/8/14.
 * Adapter for RecyclerView in MainActivity
 */
public class RecyclerViewCourseAdapter
        extends RecyclerView.Adapter<RecyclerViewCourseAdapter.ViewHolder> {


    private ArrayList<Course> mCourseArrayList;
    private Context context;
    private DataHelper<MainActivity> dataHelper;

    RecyclerViewCourseAdapter(MainActivity activity, Context context, ArrayList<Course> mCourseArrayList) {
        this.context = context;
        this.mCourseArrayList = mCourseArrayList;
        dataHelper = new DataHelper<>(activity);
        if (mCourseArrayList == null) {
            throw new IllegalArgumentException("courses ArrayList must not be null");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        //Initialize views in Viewholder
        TextView mTextViewName;
        TextView mTextViewProgress;
        //ImageView mImageView;
        TextView mTextViewAverage;
        //Context to refer to app context (for intent, dialog etc)
        Context context;
        //Adapter to notifiy data set changed
        RecyclerViewCourseAdapter mCourseAdapter;

        //Holds views
        public ViewHolder(View itemView,
                          Context context,
                          RecyclerViewCourseAdapter mCourseAdapter) {

            super(itemView);
            this.context = context;
            this.mCourseAdapter = mCourseAdapter;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mTextViewName = (TextView) itemView.findViewById(R.id.course_textview_name);
            mTextViewProgress = (TextView) itemView.findViewById(R.id.course_letter);
            mTextViewAverage = (TextView) itemView.findViewById(R.id.end_percentage_textview);
        }


        /*
        OnClick: Course at the specific position shall be opened
         */
        @Override
        public void onClick(View v) {

            Course course = mCourseAdapter.getmCourseArrayList().get(getLayoutPosition());
            //Log course
            MyLogger.logCourse(course);
            //Setup new Intent
            Intent intent = new Intent();
            intent.setClass(context, AssignmentsActivity.class);

            //add course position to update assignments when result comes back
            intent.putExtra("COURSE_TAG_ID",
                    course.getId()
                    );

            //Start activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            //Open Alert dialog to delete item

            AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

            //Set title and message
            alert.setTitle(context.getString(R.string.delete));
            alert.setMessage(context.getString(R.string.do_you_want_to_delete) + mTextViewName.getText().toString() + "?");

            //Set positive button behaviour
            alert.setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    //Delete course and notify
                    Toast.makeText(
                            context,
                            mTextViewName.getText().toString() + context.getString(R.string.deleted),
                            Toast.LENGTH_LONG).show();


                    //Remove Course
                    mCourseAdapter.removeCourse(getPosition());

                }
            });

            alert.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();


            return true;
        }


    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_course_altern,
                        parent,
                        false);

        return new ViewHolder(itemView, context, this);
    }


    //Sets up the view
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Course course = mCourseArrayList.get(position);
        //Set text
        holder.mTextViewName.setText(course.getCourseName());
        holder.mTextViewProgress.setText(course.getAverage(true).toString().split("\\.")[0]);
        holder.mTextViewAverage.setText(course.getProgress().toString() + " %");

        //Set OverAllPercentage background Color
        holder.mTextViewProgress.setBackground(getDrawableColorForPercentage(course.getAverage(true)));

    }

    @Override
    public int getItemCount() {
        return mCourseArrayList.size();
    }

    /**
     * This method receives a percentage and retrieves a color
     */
    public Drawable getDrawableColorForPercentage(Double percentage) {

        if (percentage < 30)
            return context.getResources().getDrawable(R.drawable.circular_shape_red);
        if (percentage < 40)
            return context.getResources().getDrawable(R.drawable.circular_shape_deep_orange);
        if (percentage < 55)
            return context.getResources().getDrawable(R.drawable.circular_shape_orange);
        if (percentage < 65)
            return context.getResources().getDrawable(R.drawable.circular_shape_yellow);
        if (percentage < 80)
            return context.getResources().getDrawable(R.drawable.circular_shape_lime);
        if (percentage >= 80)
            return context.getResources().getDrawable(R.drawable.circular_shape_green);

        return context.getResources().getDrawable(R.drawable.circular_shape_red);
    }

    //Adds Course
    public void addCourse(Course course) {
        if (course != null) {
            mCourseArrayList.add(course);
            notifyItemInserted(mCourseArrayList.size());

            //save in data

            dataHelper.addCourse(course);

        }
    }

    /**
     * Removes the course at 'position' from the internal arraylist and from the database
     * and animates the removal in recyclerview
     *
     * @param position Position of course which is to be removed
     */
    public void removeCourse(int position) {
        //Log removal
        Log.d("Tak3r07", "Removing at pos: " + position + ", size: " + mCourseArrayList.size());
        if (position >= 0) {
            //Delete from database
            dataHelper.deleteCourse(mCourseArrayList.get(position));

            //Delete from internal arraylist
            mCourseArrayList.remove(position);

            //Animate
            notifyItemRemoved(position);

        } else {
            //Log error
            Log.e("Tak3r07", "position = " + position + " < 0");
        }
    }

    public ArrayList<Course> getmCourseArrayList() {
        return mCourseArrayList;
    }

    public void setmCourseArrayList(ArrayList<Course> mCourseArrayList) {
        this.mCourseArrayList = mCourseArrayList;
    }

    /**
     * Returns correct item position since first item is header
     *
     * @return item positon
     */
    public static int getItemPosition(int position) {
        return position - 1;
    }
}
