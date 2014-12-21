package com.tak3r07.CourseStatistics;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
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
public class RecyclerViewCourseAdapter extends RecyclerView.Adapter<RecyclerViewCourseAdapter.ViewHolder> {



    private ArrayList<Course> mCourseArrayList;
    private Context context;

    private final String COURSE_TAG_POSITION = "COURSE_TAG_POSITION";

    RecyclerViewCourseAdapter(ArrayList<Course> courses, Context context) {
        this.context = context;
        if (courses == null) {
            throw new IllegalArgumentException("courses ArrayList must not be null");
        }

        mCourseArrayList = courses;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        //Initialize views in Viewholder
        TextView mTextViewFirst;
        TextView mTextViewSecond;
        //ImageView mImageView;
        TextView mLetter;
        TextView mEndPercentageTextView;
        //Context to refer to app context (for intent, dialog etc)
        Context context;
        //Adapter to notifiy data set changed
        RecyclerViewCourseAdapter mCourseAdapter;

        //Holds views
        public ViewHolder(View itemView, Context context, RecyclerViewCourseAdapter mCourseAdapter) {

            super(itemView);
            this.context = context;
            this.mCourseAdapter = mCourseAdapter;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mTextViewFirst = (TextView) itemView.findViewById(R.id.course_firstLine);
            mTextViewSecond = (TextView) itemView.findViewById(R.id.course_secondLine);
            //mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mEndPercentageTextView = (TextView) itemView.findViewById(R.id.end_percentage_textview);
            mLetter = (TextView) itemView.findViewById(R.id.course_letter);
        }


        /*
        OnClick: Course at the specific position shall be opened
         */
        @Override
        public void onClick(View v) {
            //Setup new Intent
            Intent intent = new Intent();
            intent.setClass(context, AssignmentsActivity.class);

            //add course position to update assignments when result comes back
            intent.putExtra("COURSE_TAG_POSITION", getPosition());
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
            alert.setMessage(context.getString(R.string.do_you_want_to_delete) + mTextViewFirst.getText().toString() + "?");

            //Set positive button behaviour
            alert.setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    //Delete course and notify
                    Toast.makeText(context, mTextViewFirst.getText().toString() + context.getString(R.string.deleted), Toast.LENGTH_LONG).show();

                    //Remove Course
                    mCourseAdapter.removeCourse(getPosition());

                    //Save Course ArrayList to storage
                    CourseDataHandler.save(context, mCourseAdapter.getmCourseArrayList());
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_course_alternative, parent, false);
        ViewHolder vh = new ViewHolder(itemView, context, this);

        return vh;
    }


    //Sets up the view
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Course course = mCourseArrayList.get(position);
        //Set Icon
        Character firstChar = course.getCourseName().toLowerCase().charAt(0);
        holder.mLetter.setText(firstChar.toString().toUpperCase());
        holder.mLetter.setWidth(holder.mLetter.getLineHeight());
        //holder.mImageView.setImageResource(getLetterIconId(firstChar));
        //Set text
        holder.mTextViewFirst.setText(course.getCourseName());
        holder.mTextViewSecond.setText("\u00d8 " + course.getOverAllPercentage().toString() + " %");
        holder.mEndPercentageTextView.setText(course.getAverage().toString() + " %");

        //Set Percentage Color
        holder.mEndPercentageTextView.setTextColor(getColorForPercentage(course.getAverage()));


    }

    @Override
    public int getItemCount() {
        return mCourseArrayList.size();
    }

    /**
     * This method receives a percentage and retrieves a color
     */
    public int getColorForPercentage(Double percentage) {

        if (percentage < 10) return context.getResources().getColor(R.color.red_500);
        if (percentage < 20) return context.getResources().getColor(R.color.deep_orange_500);
        if (percentage < 30) return context.getResources().getColor(R.color.orange_500);
        if (percentage < 40) return context.getResources().getColor(R.color.yellow_500);
        if (percentage < 50) return context.getResources().getColor(R.color.light_green_500);
        if (percentage > 50) return context.getResources().getColor(R.color.green_500);
        return Color.BLACK;
    }

    //Adds Course
    public void addCourse(Course course) {
        if (course != null) {
            mCourseArrayList.add(course);
            notifyItemInserted(mCourseArrayList.size());
        }
    }

    //Removes Course
    public void removeCourse(int position) {
        if (position>=0){
            mCourseArrayList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public ArrayList<Course> getmCourseArrayList() {
        return mCourseArrayList;
    }

    public void setmCourseArrayList(ArrayList<Course> mCourseArrayList) {
        this.mCourseArrayList = mCourseArrayList;
    }
}
